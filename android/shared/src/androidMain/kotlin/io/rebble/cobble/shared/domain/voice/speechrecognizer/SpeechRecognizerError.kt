package io.rebble.cobble.shared.domain.voice.speechrecognizer

enum class SpeechRecognizerError {
    ERROR_NETWORK_TIMEOUT,
    ERROR_NETWORK,
    ERROR_AUDIO,
    ERROR_SERVER,
    ERROR_CLIENT,
    ERROR_SPEECH_TIMEOUT,
    ERROR_NO_MATCH,
    ERROR_RECOGNIZER_BUSY,
    ERROR_INSUFFICIENT_PERMISSIONS,
    ERROR_TOO_MANY_REQUESTS,
    ERROR_SERVER_DISCONNECTED,
    ERROR_LANGUAGE_NOT_SUPPORTED,
    ERROR_LANGUAGE_UNAVAILABLE,
    ERROR_CANNOT_CHECK_SUPPORT,
    ERROR_CANNOT_LISTEN_TO_DOWNLOAD_EVENTS;

    companion object {
        fun fromInt(value: Int): SpeechRecognizerError {
            return when (value) {
                1 -> ERROR_NETWORK_TIMEOUT
                2 -> ERROR_NETWORK
                3 -> ERROR_AUDIO
                4 -> ERROR_SERVER
                5 -> ERROR_CLIENT
                6 -> ERROR_SPEECH_TIMEOUT
                7 -> ERROR_NO_MATCH
                8 -> ERROR_RECOGNIZER_BUSY
                9 -> ERROR_INSUFFICIENT_PERMISSIONS
                10 -> ERROR_TOO_MANY_REQUESTS
                11 -> ERROR_SERVER_DISCONNECTED
                12 -> ERROR_LANGUAGE_NOT_SUPPORTED
                13 -> ERROR_LANGUAGE_UNAVAILABLE
                14 -> ERROR_CANNOT_CHECK_SUPPORT
                15 -> ERROR_CANNOT_LISTEN_TO_DOWNLOAD_EVENTS
                else -> throw IllegalArgumentException("Unknown error code: $value")
            }
        }
    }
}