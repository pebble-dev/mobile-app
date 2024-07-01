package io.rebble.cobble.shared.domain.timeline

import com.benasher44.uuid.Uuid
import io.rebble.cobble.shared.Logging
import io.rebble.cobble.shared.PlatformContext
import io.rebble.cobble.shared.data.TimelineAction
import io.rebble.cobble.shared.data.TimelineAttribute
import io.rebble.cobble.shared.database.NextSyncAction
import io.rebble.cobble.shared.database.dao.TimelinePinDao
import io.rebble.cobble.shared.database.entity.TimelinePin
import io.rebble.libpebblecommon.PacketPriority
import io.rebble.libpebblecommon.packets.blobdb.BlobCommand
import io.rebble.libpebblecommon.packets.blobdb.BlobResponse
import io.rebble.libpebblecommon.packets.blobdb.TimelineItem
import io.rebble.libpebblecommon.services.blobdb.BlobDBService
import io.rebble.libpebblecommon.structmapper.SUUID
import io.rebble.libpebblecommon.structmapper.StructMapper
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.json.Json
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.math.round
import kotlin.random.Random
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days

@ExperimentalUnsignedTypes
class WatchTimelineSyncer(
        private val blobDBService: BlobDBService
): KoinComponent {
    private val context: PlatformContext by inject()
    private val timelinePinDao: TimelinePinDao by inject()

    suspend fun syncPinDatabaseWithWatch(): Boolean {
        when (val status = performSync()) {
            BlobResponse.BlobStatus.Success -> {
                return true
            }
            BlobResponse.BlobStatus.InvalidOperation,
            BlobResponse.BlobStatus.InvalidDatabaseID,
            BlobResponse.BlobStatus.InvalidData,
            BlobResponse.BlobStatus.KeyDoesNotExist,
            BlobResponse.BlobStatus.DataStale,
            BlobResponse.BlobStatus.NotSupported,
            BlobResponse.BlobStatus.Locked -> {
                Logging.e("Failed to sync pins with watch due to a bug in the sync engine: $status")
                return false
            }
            BlobResponse.BlobStatus.DatabaseFull -> {
                Logging.w("Timeline Pin Sync DB full")
                displayWatchFullWarning(context)
                return true
            }
            BlobResponse.BlobStatus.GeneralFailure,
            BlobResponse.BlobStatus.WatchDisconnected,
            BlobResponse.BlobStatus.TryLater,
            null -> {
                Logging.w("Failed to sync pins with watch ($status) Retrying later...")
                return false
            }
        }
    }

    private suspend fun performSync(): BlobResponse.BlobStatus {
        try {
            for (pinToDelete in timelinePinDao.getAllPinsWithNextSyncAction(NextSyncAction.Delete, NextSyncAction.DeleteThenIgnore)) {
                val res = removeTimelinePin(pinToDelete.itemId)
                if (res != BlobResponse.BlobStatus.Success && res != BlobResponse.BlobStatus.KeyDoesNotExist) {
                    return res
                }

                if (pinToDelete.nextSyncAction == NextSyncAction.DeleteThenIgnore) {
                    timelinePinDao.updatePin(pinToDelete.copy(nextSyncAction = null))
                } else {
                    timelinePinDao.deletePin(pinToDelete)
                }
            }

            for (pinToUpload in timelinePinDao.getAllPinsWithNextSyncAction(NextSyncAction.Upload)) {
                when (val res = addTimelinePin(pinToUpload)) {
                    BlobResponse.BlobStatus.Success -> {
                        timelinePinDao.updatePin(pinToUpload.copy(nextSyncAction = NextSyncAction.Nothing))
                    }
                    BlobResponse.BlobStatus.DatabaseFull -> {
                        // Any pins after 3 day are just buffer to allow for offline
                        // watch operations.
                        // No need to trouble the user if we can't fit that buffer onto
                        // the watch
                        val dateAfterThreeDays = Clock.System.now() + 3.days
                        return if (pinToUpload.timestamp > dateAfterThreeDays) {
                            BlobResponse.BlobStatus.Success
                        } else {
                            BlobResponse.BlobStatus.DatabaseFull
                        }
                    }
                    else -> {
                        return res
                    }
                }
            }
            return BlobResponse.BlobStatus.Success
        } catch (e: Exception) {
            Logging.e("Failed to sync pins with watch", e)
            return BlobResponse.BlobStatus.InvalidData
        }
    }

    suspend fun clearAllPinsFromWatchAndResync(): Boolean {
        val res = removeAllPins()
        if (res != BlobResponse.BlobStatus.Success) {
            Logging.w("Failed to clear all pins from watch: $res")
            return false
        }

        resetSyncStatus()
        return syncPinDatabaseWithWatch()
    }

    private suspend fun resetSyncStatus() {
        timelinePinDao.deletePinsWithNextSyncAction(NextSyncAction.Delete)
        timelinePinDao.replaceNextSyncAction(NextSyncAction.Nothing, NextSyncAction.Upload)
    }

    private suspend fun removeTimelinePin(id: Uuid): BlobResponse.BlobStatus {
        // This packet is usually sent as part of the background sync, so we use low priority
        // to not disturb any user experience with our sync

        return blobDBService.send(BlobCommand.DeleteCommand(
                Random.nextInt(0, UShort.MAX_VALUE.toInt()).toUShort(),
                BlobCommand.BlobDatabase.Pin,
                SUUID(StructMapper(), id).toBytes(),
        ), PacketPriority.LOW).responseValue
    }

    private suspend fun addTimelinePin(pin: TimelinePin): BlobResponse.BlobStatus {
        val parsedAttributes: List<TimelineAttribute> = Json.decodeFromString(pin.attributesJson!!)
                ?: emptyList()

        val parsedActions: List<TimelineAction> = Json.decodeFromString(pin.actionsJson!!)
                ?: emptyList()

        val flags = buildList {
            if (pin.isVisible) {
                add(TimelineItem.Flag.IS_VISIBLE)
            }

            if (pin.isFloating) {
                add(TimelineItem.Flag.IS_FLOATING)
            }

            if (pin.isAllDay) {
                add(TimelineItem.Flag.IS_ALL_DAY)
            }

            if (pin.persistQuickView) {
                add(TimelineItem.Flag.PERSIST_QUICK_VIEW)
            }
        }
        val timelineItem = TimelineItem(
                pin.itemId,
                pin.parentId,
                round(pin.timestamp.toEpochMilliseconds() / 1000f).toUInt(),
                pin.duration?.toUShort() ?: 0u,
                TimelineItem.Type.Pin,
                TimelineItem.Flag.makeFlags(flags),
                pin.layout,
                parsedAttributes.map { it.toProtocolAttribute() },
                parsedActions.map { it.toProtocolAction() }
        )
        val packet = BlobCommand.InsertCommand(
                Random.nextInt(0, UShort.MAX_VALUE.toInt()).toUShort(),
                BlobCommand.BlobDatabase.Pin,
                SUUID(StructMapper(), pin.itemId).toBytes(),
                timelineItem.toBytes(),
        )

        // This packet is usually sent as part of the background sync, so we use low priority
        // to not disturb any user experience with our sync
        return blobDBService.send(packet, PacketPriority.LOW).responseValue
    }

    private suspend fun removeAllPins(): BlobResponse.BlobStatus {
        return blobDBService.send(BlobCommand.ClearCommand(
                Random.nextInt(0, UShort.MAX_VALUE.toInt()).toUShort(),
                BlobCommand.BlobDatabase.Pin
        )).responseValue
    }
}

expect fun displayWatchFullWarning(context: PlatformContext)