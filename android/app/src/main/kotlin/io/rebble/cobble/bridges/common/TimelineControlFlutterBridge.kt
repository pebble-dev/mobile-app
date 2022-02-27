package io.rebble.cobble.bridges.common

import io.flutter.plugin.common.BinaryMessenger
import io.rebble.cobble.bridges.FlutterBridge
import io.rebble.cobble.data.TimelineAction
import io.rebble.cobble.data.TimelineAttribute
import io.rebble.cobble.pigeons.*
import io.rebble.cobble.util.launchPigeonResult
import io.rebble.cobble.util.registerAsyncPigeonCallback
import io.rebble.libpebblecommon.PacketPriority
import io.rebble.libpebblecommon.packets.blobdb.BlobCommand
import io.rebble.libpebblecommon.packets.blobdb.BlobResponse
import io.rebble.libpebblecommon.packets.blobdb.TimelineItem
import io.rebble.libpebblecommon.services.blobdb.BlobDBService
import io.rebble.libpebblecommon.structmapper.SUUID
import io.rebble.libpebblecommon.structmapper.StructMapper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.util.*
import javax.inject.Inject
import kotlin.random.Random

class TimelineControlFlutterBridge @Inject constructor(
        binaryMessenger: BinaryMessenger,
        private val coroutineScope: CoroutineScope,
        private val blobDBService: BlobDBService
) : FlutterBridge, Pigeons.TimelineControl {

    @OptIn(ExperimentalStdlibApi::class)
    @Suppress("BlockingMethodInNonBlockingContext")
    private suspend fun addTimelinePin(pin: Pigeons.TimelinePinPigeon): BlobResponse.BlobStatus {
        val parsedAttributes: List<TimelineAttribute> = Json.decodeFromString(pin.attributesJson!!) ?: emptyList()

        val parsedActions: List<TimelineAction> = Json.decodeFromString(pin.actionsJson!!) ?: emptyList()

        val flags = buildList {
            if (pin.isVisible!!) {
                add(TimelineItem.Flag.IS_VISIBLE)
            }

            if (pin.isFloating!!) {
                add(TimelineItem.Flag.IS_FLOATING)
            }

            if (pin.isAllDay!!) {
                add(TimelineItem.Flag.IS_ALL_DAY)
            }

            if (pin.persistQuickView!!) {
                add(TimelineItem.Flag.PERSIST_QUICK_VIEW)
            }
        }

        val itemId = UUID.fromString(pin.itemId)
        val timelineItem = TimelineItem(
                itemId,
                UUID.fromString(pin.parentId),
                pin.timestamp!!.toUInt(),
                pin.duration!!.toUShort(),
                TimelineItem.Type.Pin,
                TimelineItem.Flag.makeFlags(flags),
                pin.layout!!.toUByte(),
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

    override fun addPin(pin: Pigeons.TimelinePinPigeon?, result: Pigeons.Result<Pigeons.NumberWrapper>?) {
        coroutineScope.launchPigeonResult(result!!, coroutineScope.coroutineContext) {
            val res = addTimelinePin(pin!!)
            NumberWrapper(res.value.toInt())
        }
    }

    override fun removePin(pinUuid: Pigeons.StringWrapper?, result: Pigeons.Result<Pigeons.NumberWrapper>?) {
        coroutineScope.launchPigeonResult(result!!, coroutineScope.coroutineContext) {
            val res = removeTimelinePin(UUID.fromString(pinUuid?.value!!))
            NumberWrapper(res.value.toInt())
        }

    }

    override fun removeAllPins(result: Pigeons.Result<Pigeons.NumberWrapper>?) {
        coroutineScope.launchPigeonResult(result!!, coroutineScope.coroutineContext) {
            val res = removeAllPins()
            NumberWrapper(res.value.toInt())
        }

    }
}