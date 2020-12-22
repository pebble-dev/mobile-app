package io.rebble.cobble.bridges.common

import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import io.flutter.plugin.common.BinaryMessenger
import io.rebble.cobble.bridges.FlutterBridge
import io.rebble.cobble.data.TimelineAction
import io.rebble.cobble.data.TimelineAttribute
import io.rebble.cobble.pigeons.*
import io.rebble.cobble.util.registerAsyncPigeonCallback
import io.rebble.libpebblecommon.PacketPriority
import io.rebble.libpebblecommon.packets.blobdb.BlobCommand
import io.rebble.libpebblecommon.packets.blobdb.BlobResponse
import io.rebble.libpebblecommon.packets.blobdb.TimelineItem
import io.rebble.libpebblecommon.services.blobdb.BlobDBService
import io.rebble.libpebblecommon.structmapper.SUUID
import io.rebble.libpebblecommon.structmapper.StructMapper
import kotlinx.coroutines.CoroutineScope
import java.util.*
import javax.inject.Inject
import kotlin.random.Random

class TimelineControlFlutterBridge @Inject constructor(
        binaryMessenger: BinaryMessenger,
        coroutineScope: CoroutineScope,
        private val moshi: Moshi,
        private val blobDBService: BlobDBService
) : FlutterBridge {
    init {
        binaryMessenger.registerAsyncPigeonCallback(
                coroutineScope,
                "dev.flutter.pigeon.TimelineControl.addPin"
        ) {
            val result = addTimelinePin(timelinePinPigeonFromMap(
                    it ?: error("Missing argument"))
            )

            NumberWrapper(result.value.toInt()).toMapExt()
        }

        binaryMessenger.registerAsyncPigeonCallback(
                coroutineScope,
                "dev.flutter.pigeon.TimelineControl.removePin"
        ) {
            val result = removeTimelinePin(UUID.fromString(
                    stringWrapperFromMap(it ?: error("Missing argument")).value
            ))

            NumberWrapper(result.value.toInt()).toMapExt()
        }

        binaryMessenger.registerAsyncPigeonCallback(
                coroutineScope,
                "dev.flutter.pigeon.TimelineControl.removeAllPins"
        ) {
            val result = removeAllPins()
            NumberWrapper(result.value.toInt()).toMapExt()
        }
    }

    @OptIn(ExperimentalStdlibApi::class)
    @Suppress("BlockingMethodInNonBlockingContext")
    private suspend fun addTimelinePin(pin: Pigeons.TimelinePinPigeon): BlobResponse.BlobStatus {
        val parsedAttributes = moshi
                .adapter<List<TimelineAttribute>>(
                        Types.newParameterizedType(List::class.java, TimelineAttribute::class.java)
                )
                .fromJson(pin.attributesJson) ?: emptyList()

        val parsedActions = moshi
                .adapter<List<TimelineAction>>(
                        Types.newParameterizedType(List::class.java, TimelineAction::class.java)
                )
                .fromJson(pin.actionsJson) ?: emptyList()

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

        val itemId = UUID.fromString(pin.itemId)
        val timelineItem = TimelineItem(
                itemId,
                UUID.fromString(pin.parentId),
                pin.timestamp.toUInt(),
                pin.duration.toUShort(),
                TimelineItem.Type.Pin,
                TimelineItem.Flag.makeFlags(flags),
                pin.layout.toUByte(),
                parsedAttributes.map { it.toProtocolAttribute() },
                parsedActions.map { it.toProtocolAction() }
        )
        val packet = BlobCommand.InsertCommand(
                Random.nextInt(0, UShort.MAX_VALUE.toInt()).toUShort(),
                BlobCommand.BlobDatabase.Pin,
                SUUID(StructMapper(), itemId).toBytes(),
                timelineItem.toBytes(),
        )

        // This packet is usually sent as part of the background sync, so we use low priority
        // to not disturb any user experience with our sync
        return blobDBService.send(packet, PacketPriority.LOW).responseValue
    }

    private suspend fun removeTimelinePin(id: UUID): BlobResponse.BlobStatus {
        // This packet is usually sent as part of the background sync, so we use low priority
        // to not disturb any user experience with our sync

        return blobDBService.send(BlobCommand.DeleteCommand(
                Random.nextInt(0, UShort.MAX_VALUE.toInt()).toUShort(),
                BlobCommand.BlobDatabase.Pin,
                SUUID(StructMapper(), id).toBytes(),
        ), PacketPriority.LOW).responseValue
    }

    private suspend fun removeAllPins(): BlobResponse.BlobStatus {
        return blobDBService.send(BlobCommand.ClearCommand(
                Random.nextInt(0, UShort.MAX_VALUE.toInt()).toUShort(),
                BlobCommand.BlobDatabase.Pin
        )).responseValue
    }
}