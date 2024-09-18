package io.rebble.cobble.shared.jobs

import com.benasher44.uuid.uuidFrom
import io.rebble.cobble.shared.Logging
import io.rebble.cobble.shared.PlatformContext
import io.rebble.cobble.shared.api.RWS
import io.rebble.cobble.shared.database.NextSyncAction
import io.rebble.cobble.shared.database.dao.LockerDao
import io.rebble.cobble.shared.database.entity.dataEqualTo
import io.rebble.cobble.shared.domain.api.appstore.toEntity
import io.rebble.cobble.shared.domain.state.ConnectionState
import io.rebble.cobble.shared.domain.state.ConnectionStateManager
import io.rebble.cobble.shared.domain.state.watchOrNull
import io.rebble.cobble.shared.util.AppCompatibility
import io.rebble.libpebblecommon.metadata.WatchHardwarePlatform
import io.rebble.libpebblecommon.packets.blobdb.AppMetadata
import io.rebble.libpebblecommon.packets.blobdb.BlobCommand
import io.rebble.libpebblecommon.packets.blobdb.BlobResponse
import io.rebble.libpebblecommon.services.blobdb.BlobDBService
import io.rebble.libpebblecommon.structmapper.SUUID
import io.rebble.libpebblecommon.structmapper.StructMapper
import kotlinx.coroutines.flow.first
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.random.Random

class LockerSyncJob: KoinComponent {
    private val lockerDao: LockerDao by inject()
    private val blobDBService: BlobDBService by inject()
    suspend fun beginSync(): Boolean {
        val locker = RWS.appstoreClient?.getLocker() ?: return false
        lockerDao.clearAll()
        val storedLocker = lockerDao.getAllEntries()

        val changedEntries = locker.filter { new ->
                val newPlat = new.hardwarePlatforms.map { it.toEntity(new.id) }
            storedLocker.any { old ->
                old.entry.id == new.id && (old.entry != new.toEntity() || old.platforms.any { oldPlat -> newPlat.none { newPlat -> oldPlat.dataEqualTo(newPlat) } })
            }
        }
        val newEntries = locker.filter { new -> storedLocker.none { old -> old.entry.id == new.id } }
        val removedEntries = storedLocker.filter { old -> locker.none { nw -> nw.id == old.entry.id } }

        lockerDao.insertOrReplaceAll(newEntries.map { it.toEntity() })
        changedEntries.forEach {
            lockerDao.clearPlatformsFor(it.id)
        }
        lockerDao.insertOrReplaceAll(changedEntries.map { it.toEntity() })
        lockerDao.insertOrReplaceAllPlatforms(newEntries.flatMap { new ->
            new.hardwarePlatforms.map { it.toEntity(new.id) }
        })
        lockerDao.insertOrReplaceAllPlatforms(changedEntries.flatMap { new ->
            new.hardwarePlatforms.map { it.toEntity(new.id) }
        })
        lockerDao.setNextSyncAction(removedEntries.map { it.entry.id }.toSet(), NextSyncAction.Delete)
        lockerDao.setNextSyncAction(changedEntries.map { it.id }.toSet(), NextSyncAction.Upload)
        Logging.i("Synced locker: ${newEntries.size} new, ${changedEntries.size} changed, ${removedEntries.size} removed")
        return syncToDevice()
    }

    private suspend fun syncToDevice(): Boolean {
        val entries = lockerDao.getEntriesForSync()
        val connectedWatch = ConnectionStateManager.connectionState.value.watchOrNull
        connectedWatch?.let {
            val connectedWatchType = WatchHardwarePlatform
                    .fromProtocolNumber(connectedWatch.metadata.value?.running?.hardwarePlatform?.get() ?: 0u)
            connectedWatchType?.let {
                entries.forEach { row ->
                    val entry = row.entry
                    val platformName = AppCompatibility.getBestVariant(
                            connectedWatchType.watchType,
                            row.platforms.map { plt -> plt.name }
                    )?.codename
                    val platform = row.platforms.firstOrNull { plt -> plt.name == platformName }
                    val res = platform?.let {
                        when (entry.nextSyncAction) {
                            NextSyncAction.Upload -> {
                                val appVersionMajor = entry.version.split(".").getOrNull(0)?.toUByte() ?: 0u
                                val appVersionMinor = entry.version.split(".").getOrNull(1)?.toUByte() ?: 0u
                                val sdkVersionMajor = platform.sdkVersion.split(".")[0].toUByte()
                                val sdkVersionMinor = platform.sdkVersion.split(".")[1].toUByte()
                                return@let blobDBService.send(
                                        BlobCommand.InsertCommand(
                                                Random.nextInt(0, UShort.MAX_VALUE.toInt()).toUShort(),
                                                BlobCommand.BlobDatabase.App,
                                                SUUID(StructMapper(), uuidFrom(entry.uuid)).toBytes(),
                                                AppMetadata().also { meta ->
                                                    meta.uuid.set(uuidFrom(entry.uuid))
                                                    meta.flags.set(platform.processInfoFlags.toUInt())
                                                    meta.icon.set(0u)
                                                    meta.appVersionMajor.set(appVersionMajor)
                                                    meta.appVersionMinor.set(appVersionMinor)
                                                    meta.sdkVersionMajor.set(sdkVersionMajor)
                                                    meta.sdkVersionMinor.set(sdkVersionMinor)
                                                    meta.appName.set(entry.title)
                                                }.toBytes()
                                        )
                                )
                            }
                            NextSyncAction.Delete -> {
                                return@let blobDBService.send(
                                        BlobCommand.DeleteCommand(
                                                Random.nextInt(0, UShort.MAX_VALUE.toInt()).toUShort(),
                                                BlobCommand.BlobDatabase.App,
                                                SUUID(StructMapper(), uuidFrom(entry.uuid)).toBytes()
                                        )
                                )
                            }
                            else -> {
                                Logging.w("Unknown next sync action ${entry.nextSyncAction}")
                                return@let null
                            }
                        }
                    }
                    when (res?.responseValue) {
                        BlobResponse.BlobStatus.Success -> {
                            lockerDao.setNextSyncAction(entry.id, NextSyncAction.Nothing)
                        }
                        BlobResponse.BlobStatus.DatabaseFull -> {
                            Logging.w("Database full, stopping sync")
                            return true
                        }
                        BlobResponse.BlobStatus.WatchDisconnected -> {
                            Logging.w("Watch disconnected, stopping sync")
                            return false
                        }
                        else -> {
                            Logging.w("Failed to sync app ${entry.id}: ${res?.responseValue}")
                        }
                    }
                }
            } ?: run {
                Logging.w("Unknown watch type")
                return false
            }
        } ?: run {
            Logging.w("No connected watch to sync to")
            return false
        }
        return true
    }

    companion object {
        fun schedule(context: PlatformContext) {
            scheduleLockerSyncJob(context)
        }
    }
}

expect fun scheduleLockerSyncJob(context: PlatformContext)