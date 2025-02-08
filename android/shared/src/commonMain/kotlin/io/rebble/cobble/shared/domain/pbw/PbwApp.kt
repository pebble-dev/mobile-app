package io.rebble.cobble.shared.domain.pbw

import io.rebble.cobble.shared.Logging
import io.rebble.cobble.shared.PlatformContext
import io.rebble.cobble.shared.database.AppDatabase
import io.rebble.cobble.shared.database.NextSyncAction
import io.rebble.cobble.shared.database.dao.LockerDao
import io.rebble.cobble.shared.database.entity.SyncedLockerEntry
import io.rebble.cobble.shared.database.entity.SyncedLockerEntryPlatform
import io.rebble.cobble.shared.database.entity.SyncedLockerEntryPlatformImages
import io.rebble.cobble.shared.database.getDatabase
import io.rebble.cobble.shared.handlers.savePbwFile
import io.rebble.cobble.shared.jobs.LockerSyncJob
import io.rebble.cobble.shared.util.*
import io.rebble.libpebblecommon.disk.PbwBinHeader
import io.rebble.libpebblecommon.metadata.WatchType
import io.rebble.libpebblecommon.util.DataBuffer
import okio.buffer
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.random.Random
import kotlin.random.nextUInt

class PbwApp(uri: String): KoinComponent {
    private val platformContext: PlatformContext by inject()
    private val file = File(uri)
    private val lockerDao: LockerDao by inject()
    init {
        require(file.exists())
    }

    val info by lazy { requirePbwAppInfo(file) }

    fun getManifest(watchType: WatchType) = requirePbwManifest(file, watchType)

    suspend fun installToLockerCache(): Boolean {
        val exists = lockerDao.getEntryByUuid(info.uuid)
        lockerDao.insertOrReplace(
                SyncedLockerEntry(
                        id = exists?.entry?.id ?: "local-${Random.nextUInt()}",
                        uuid = info.uuid,
                        version = info.versionLabel,
                        title = info.shortName,
                        type = if (info.watchapp.watchface) "watchface" else "watchapp",
                        hearts = 0,
                        developerName = info.companyName,
                        developerId = null,
                        configurable = info.capabilities.any { it == "configurable" },
                        timelineEnabled = false, //TODO
                        removeLink = "",
                        shareLink = "",
                        pbwLink = "",
                        pbwReleaseId = info.versionCode.toString(),
                        pbwIconResourceId = 0, //TODO,
                        NextSyncAction.Upload,
                        order = -1,
                        lastOpened = null,
                        local = true,
                        userToken = null
                )
        )

        val inserted = lockerDao.getEntryByUuid(info.uuid) ?: return false

        if (exists != null) {
            lockerDao.clearPlatformsFor(exists.entry.id)
        }

        val platforms = WatchType.entries.mapNotNull {
            val manifest = getPbwManifest(file, it)
            if (manifest != null) {
                val header = PbwBinHeader.parseFileHeader(
                        requirePbwBinaryBlob(file, it, "pebble-app.bin")
                                .buffer()
                                .readByteArray((PbwBinHeader.SIZE).toLong()).asUByteArray()
                )

                SyncedLockerEntryPlatform(
                        platformEntryId = 0,
                        lockerEntryId = inserted.entry.id,
                        sdkVersion = "${header.sdkVersionMajor.get()}.${header.sdkVersionMinor.get()}",
                        processInfoFlags = header.flags.get().toInt(),
                        name = it.codename,
                        description = "",
                        images = SyncedLockerEntryPlatformImages(
                                null,
                                null,
                                null
                        )
                )
            } else {
                null
            }
        }
        if (platforms.isEmpty()) {
            Logging.e("No platforms in PBW")
            return false
        }

        lockerDao.insertOrReplaceAllPlatforms(platforms)

        savePbwFile(platformContext, info.uuid, file.readChannel())
        if (!LockerSyncJob().syncToDevice()) {
            Logging.e("Failed to sync locker to device")
            return false
        }
        return true
    }
}