package io.rebble.cobble.shared.domain.voice

import io.rebble.libpebblecommon.packets.Result

open class DictationServiceResponse {
    object Ready: DictationServiceResponse()
    class Error(val result: Result): DictationServiceResponse()
    class Transcription(val words: List<Word>): DictationServiceResponse()
    object Complete : DictationServiceResponse()
}

data class Word(
        val text: String,
        val confidence: UByte
)