package io.rebble.cobble.shared.jobs

import com.benasher44.uuid.uuidFrom
import io.rebble.cobble.shared.Logging
import io.rebble.cobble.shared.PlatformContext
import io.rebble.cobble.shared.api.RWS
import io.rebble.cobble.shared.database.NextSyncAction
import io.rebble.cobble.shared.database.dao.LockerDao
import io.rebble.cobble.shared.database.entity.dataEqualTo
import io.rebble.cobble.shared.database.entity.getBestPlatformForDevice
import io.rebble.cobble.shared.database.entity.getSdkVersion
import io.rebble.cobble.shared.database.entity.getVersion
import io.rebble.cobble.shared.domain.api.appstore.toEntity
import io.rebble.cobble.shared.domain.state.ConnectionStateManager
import io.rebble.cobble.shared.domain.state.watchOrNull
import io.rebble.libpebblecommon.metadata.WatchHardwarePlatform
import io.rebble.libpebblecommon.packets.blobdb.AppMetadata
import io.rebble.libpebblecommon.packets.blobdb.BlobCommand
import io.rebble.libpebblecommon.packets.blobdb.BlobResponse
import io.rebble.libpebblecommon.structmapper.SUUID
import io.rebble.libpebblecommon.structmapper.StructMapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.random.Random

class LockerSyncJob: KoinComponent {
    private val lockerDao: LockerDao by inject()
    suspend fun beginSync(): Boolean {
        val locker = withContext(Dispatchers.IO) {
            RWS.appstoreClient?.getLocker()
        } ?: return false
        val storedLocker = withContext(Dispatchers.IO) {
            lockerDao.getAllEntries()
        }

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
        val entries = lockerDao.getEntriesForSync().sortedBy { it.entry.title }
        val connectedWatch = ConnectionStateManager.connectionState.value.watchOrNull
        connectedWatch?.let {
            val connectedWatchType = WatchHardwarePlatform
                    .fromProtocolNumber(connectedWatch.metadata.value?.running?.hardwarePlatform?.get() ?: 0u)
            connectedWatchType?.let {
                val blobDBService = connectedWatch.blobDBService
                return withContext(Dispatchers.IO) {
                    entries.forEach { row ->
                        val entry = row.entry
                        val platform = row.getBestPlatformForDevice(connectedWatchType.watchType)
                        val res = platform?.let {
                            when (entry.nextSyncAction) {
                                NextSyncAction.Upload -> {
                                    val (appVersionMajor, appVersionMinor) = row.getVersion()
                                    val (sdkVersionMajor, sdkVersionMinor) = platform.getSdkVersion()
                                    val packet = BlobCommand.InsertCommand(
                                            Random.nextInt(0, UShort.MAX_VALUE.toInt()).toUShort(),
                                            BlobCommand.BlobDatabase.App,
                                            SUUID(StructMapper(), uuidFrom(entry.uuid)).toBytes(),
                                            AppMetadata().also { meta ->
                                                meta.uuid.set(uuidFrom(entry.uuid))
                                                meta.flags.set(platform.processInfoFlags.toUInt())
                                                meta.icon.set(entry.pbwIconResourceId.toUInt())
                                                meta.appVersionMajor.set(appVersionMajor)
                                                meta.appVersionMinor.set(appVersionMinor)
                                                meta.sdkVersionMajor.set(sdkVersionMajor)
                                                meta.sdkVersionMinor.set(sdkVersionMinor)
                                                meta.appName.set(entry.title)
                                            }.toBytes()
                                    )
                                    return@let blobDBService.send(packet)
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
                                return@withContext true
                            }
                            BlobResponse.BlobStatus.WatchDisconnected -> {
                                Logging.w("Watch disconnected, stopping sync")
                                return@withContext false
                            }
                            else -> {
                                Logging.w("Failed to sync app ${entry.id}: ${res?.responseValue}")
                            }
                        }
                    }
                    return@withContext true
                }
            } ?: run {
                Logging.w("Unknown watch type")
                return false
            }
        } ?: run {
            Logging.w("No connected watch to sync to")
            return false
        }
    }

    companion object {
        fun schedule(context: PlatformContext) {
            scheduleLockerSyncJob(context)
        }
    }
}

expect fun scheduleLockerSyncJob(context: PlatformContext)