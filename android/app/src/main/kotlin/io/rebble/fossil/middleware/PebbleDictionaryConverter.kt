package io.rebble.fossil.middleware

import com.getpebble.android.kit.util.PebbleDictionary
import com.getpebble.android.kit.util.PebbleTuple
import io.rebble.libpebblecommon.packets.AppMessage
import io.rebble.libpebblecommon.packets.AppMessageTuple
import java.util.*

@OptIn(ExperimentalUnsignedTypes::class)
fun PebbleDictionary.toPacket(uuid: UUID, transactionId: Int): AppMessage.AppMessagePush {
    val tuples = map { pebbleTuple ->
        val key = pebbleTuple.key.toUInt()
        val value = pebbleTuple.value
        when (pebbleTuple.type) {
            PebbleTuple.TupleType.BYTES -> {
                AppMessageTuple.createUByteArray(
                        key,
                        (value as ByteArray).toUByteArray()
                )
            }
            PebbleTuple.TupleType.STRING -> {
                AppMessageTuple.createString(key, value as String)

            }
            PebbleTuple.TupleType.UINT -> {
                when (pebbleTuple.width) {
                    null, PebbleTuple.Width.NONE ->
                        throw IllegalArgumentException("NONE width not supported")
                    PebbleTuple.Width.BYTE ->
                        AppMessageTuple.createUByte(key, (value as Long).toUByte())
                    PebbleTuple.Width.SHORT ->
                        AppMessageTuple.createUShort(key, (value as Long).toUShort())
                    PebbleTuple.Width.WORD ->
                        AppMessageTuple.createUInt(key, (value as Long).toUInt())
                }

            }
            PebbleTuple.TupleType.INT -> {
                when (pebbleTuple.width) {
                    null, PebbleTuple.Width.NONE ->
                        throw IllegalArgumentException("NONE width not supported")
                    PebbleTuple.Width.BYTE ->
                        AppMessageTuple.createByte(key, (value as Long).toByte())
                    PebbleTuple.Width.SHORT ->
                        AppMessageTuple.createShort(key, (value as Long).toShort())
                    PebbleTuple.Width.WORD ->
                        AppMessageTuple.createInt(key, (value as Long).toInt())
                }
            }
            null -> throw IllegalArgumentException("Tuple type shouldn't be null")
        }
    }

    return AppMessage.AppMessagePush(
            transactionId.toUByte(),
            uuid,
            tuples
    )
}

@OptIn(ExperimentalUnsignedTypes::class)
fun AppMessage.AppMessagePush.getPebbleDictionary(): PebbleDictionary {
    val pebbleDictionary = PebbleDictionary()
    for (item in this.dictionary.list) {
        val intKey = item.key.get().toInt()
        val size = item.dataLength.get().toInt()
        when (AppMessageTuple.Type.fromValue(item.type.get())) {
            AppMessageTuple.Type.ByteArray -> {
                pebbleDictionary.addBytes(intKey, item.data.get().toByteArray())
            }
            AppMessageTuple.Type.CString -> {
                pebbleDictionary.addString(intKey, item.dataAsString)
            }
            AppMessageTuple.Type.UInt -> {
                pebbleDictionary.addTuple(
                        PebbleTuple.create(
                                intKey,
                                PebbleTuple.TupleType.UINT,
                                PebbleTuple.Width.fromValue(size),
                                item.dataAsUnsignedNumber
                        )
                )
            }
            AppMessageTuple.Type.Int -> {
                pebbleDictionary.addTuple(
                        PebbleTuple.create(
                                intKey,
                                PebbleTuple.TupleType.INT,
                                PebbleTuple.Width.fromValue(size),
                                item.dataAsSignedNumber
                        )
                )
            }
        }
    }

    return pebbleDictionary
}