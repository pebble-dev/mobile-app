package io.rebble.cobble.shared.domain.voice

import io.rebble.libpebblecommon.packets.Result

open class DictationServiceResponse {
    object Ready: DictationServiceResponse()
    data class Error(val result: Result): DictationServiceResponse()
    data class Transcription(val sentences: List<List<Word>>): DictationServiceResponse()
    object Complete : DictationServiceResponse()
}

data class Word(
        val text: String,
        val confidence: UByte
) {
    init {
        require(confidence in 0u..100u) { "Confidence must be between 0 and 100" }
    }
}