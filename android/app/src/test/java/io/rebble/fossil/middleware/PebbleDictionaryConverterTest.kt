package io.rebble.fossil.middleware

import io.rebble.libpebblecommon.packets.AppMessage
import io.rebble.libpebblecommon.packets.AppMessageTuple
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.*

@OptIn(ExperimentalUnsignedTypes::class)
internal class PebbleDictionaryConverterTest {
    @Test
    fun convertToDictionaryAndBack() {
        val testPushMessage = AppMessage.AppMessagePush(
                5u,
                UUID.fromString("30880933-cead-49f6-ba94-3a6f8cd3218a"),
                listOf(
                        AppMessageTuple.createUByteArray(77u, ubyteArrayOf(1u, 170u, 245u)),
                        AppMessageTuple.createString(6710u, "Hello World"),
                        AppMessageTuple.createString(7710u, "Emoji: \uD83D\uDC7D."),
                        AppMessageTuple.createByte(38485u, -7),
                        AppMessageTuple.createUByte(2130680u, 177u.toUByte()),
                        AppMessageTuple.createShort(2845647u, -20),
                        AppMessageTuple.createUShort(2845648u, 49885u.toUShort()),
                        AppMessageTuple.createInt(2845649u, -707573),
                        AppMessageTuple.createUInt(2845650u, 2448461u)
                )
        )

        val dictionary = testPushMessage.getPebbleDictionary()
        val newMessage = dictionary.toPacket(
                UUID.fromString("30880933-cead-49f6-ba94-3a6f8cd3218a"),
                5
        )

        val list = newMessage.dictionary.list.sortedBy { it.key.get() }


        val expectedFirstArray = ubyteArrayOf(1u, 170u, 245u)
        assertTrue("${expectedFirstArray.contentToString()} does not" +
                " equal ${list[0].dataAsBytes.contentToString()}",
                expectedFirstArray.contentEquals(list[0].dataAsBytes))
        assertEquals("Hello World", list[1].dataAsString)
        assertEquals("Emoji: \uD83D\uDC7D.", list[2].dataAsString)
        assertEquals(-7, list[3].dataAsSignedNumber)
        assertEquals(177, list[4].dataAsUnsignedNumber)
        assertEquals(-20, list[5].dataAsSignedNumber)
        assertEquals(49885, list[6].dataAsUnsignedNumber)
        assertEquals(-707573, list[7].dataAsSignedNumber)
        assertEquals(2448461, list[8].dataAsUnsignedNumber)

        assertEquals(UUID.fromString("30880933-cead-49f6-ba94-3a6f8cd3218a"),
                testPushMessage.uuid.get())
        assertEquals(5,
                testPushMessage.transactionId.valueNumber)
    }
}