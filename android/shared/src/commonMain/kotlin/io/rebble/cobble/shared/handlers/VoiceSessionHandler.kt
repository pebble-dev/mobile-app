package io.rebble.cobble.shared.handlers

import io.rebble.cobble.shared.Logging
import io.rebble.cobble.shared.domain.common.PebbleDevice
import io.rebble.cobble.shared.domain.voice.DictationService
import io.rebble.cobble.shared.domain.voice.DictationServiceResponse
import io.rebble.cobble.shared.domain.voice.SpeexEncoderInfo
import io.rebble.cobble.shared.domain.voice.VoiceSession
import io.rebble.libpebblecommon.packets.*
import io.rebble.libpebblecommon.util.DataBuffer
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class VoiceSessionHandler(
    private val pebbleDevice: PebbleDevice
) : CobbleHandler, KoinComponent {
    init {
        pebbleDevice.negotiationScope.launch {
            val deviceScope = pebbleDevice.connectionScope.filterNotNull().first()
            deviceScope.launch { listenForVoiceSessions() }.invokeOnCompletion {
                pebbleDevice.activeVoiceSession.value = null
            }
        }
    }

    private fun makeTranscription(
        sentences: List<List<io.rebble.cobble.shared.domain.voice.Word>>
    ): VoiceAttribute {
        val data =
            VoiceAttribute.Transcription(
                sentences =
                    sentences.map { sentence ->
                        Sentence(
                            words =
                                sentence.map { word ->
                                    Word(
                                        word.confidence,
                                        word.text
                                    )
                                }
                        )
                    }
            )
        return VoiceAttribute(
            id = VoiceAttributeType.Transcription.value,
            content = data
        )
    }

    private suspend fun handleSpeechStream(voiceSession: VoiceSession) {
        val appInitiated = voiceSession.appUuid != null
        var sentReady = false
        voiceSession.recognizer.handleSpeechStream(
            voiceSession.encoderInfo,
            voiceSession.audioStreamFrames
        )
            .takeWhile { it !is DictationServiceResponse.Complete }
            .onEach {
                Logging.v("DictationServiceResponse: $it")
                when (it) {
                    is DictationServiceResponse.Ready -> {
                        pebbleDevice.activeVoiceSession.value = voiceSession
                        val result =
                            SessionSetupResult(
                                sessionType = SessionType.Dictation,
                                result = Result.Success
                            )
                        if (appInitiated) {
                            result.flags.set(1u)
                        }
                        pebbleDevice.voiceService.send(result)
                        sentReady = true
                    }
                    is DictationServiceResponse.Error -> {
                        val result =
                            if (sentReady) {
                                DictationResult(
                                    voiceSession.sessionId.toUShort(),
                                    it.result,
                                    buildList {
                                        if (appInitiated && voiceSession.appUuid != null) {
                                            add(
                                                VoiceAttribute.AppUuid().apply {
                                                    uuid.set(voiceSession.appUuid)
                                                }
                                            )
                                        }
                                    }
                                )
                            } else {
                                SessionSetupResult(
                                    sessionType = SessionType.Dictation,
                                    result = it.result
                                )
                            }
                        if (appInitiated) {
                            result.flags.set(1u)
                        }
                        pebbleDevice.voiceService.send(result)
                    }
                    is DictationServiceResponse.Transcription -> {
                        val resp =
                            DictationResult(
                                voiceSession.sessionId.toUShort(),
                                Result.Success,
                                buildList {
                                    add(makeTranscription(it.sentences))
                                    if (appInitiated && voiceSession.appUuid != null) {
                                        add(
                                            VoiceAttribute(
                                                id = VoiceAttributeType.AppUuid.value,
                                                content =
                                                    VoiceAttribute.AppUuid().apply {
                                                        uuid.set(voiceSession.appUuid)
                                                    }
                                            )
                                        )
                                    }
                                }
                            )
                        if (appInitiated) {
                            resp.flags.set(1u)
                        }
                        pebbleDevice.voiceService.send(resp)
                    }
                }
            }
            .catch {
                Logging.e("Error in voice session: $it")
                val result =
                    if (sentReady) {
                        DictationResult(
                            voiceSession.sessionId.toUShort(),
                            Result.FailRecognizerError,
                            buildList {
                                if (appInitiated && voiceSession.appUuid != null) {
                                    add(
                                        VoiceAttribute.AppUuid().apply {
                                            uuid.set(voiceSession.appUuid)
                                        }
                                    )
                                }
                            }
                        )
                    } else {
                        SessionSetupResult(
                            sessionType = SessionType.Dictation,
                            result = Result.FailRecognizerError
                        )
                    }
                if (appInitiated) {
                    result.flags.set(1u)
                }
                pebbleDevice.voiceService.send(result)
            }
            .collect()
    }

    private suspend fun listenForVoiceSessions() {
        for (message in pebbleDevice.voiceService.receivedMessages) {
            when (message) {
                is SessionSetupCommand -> {
                    if (message.sessionType.get() == SessionType.Dictation.value) {
                        val appInitiated = message.flags.get() and 1u != 0u
                        if (appInitiated &&
                            !message.attributes.list.any {
                                it.id.get() == VoiceAttributeType.AppUuid.value
                            }
                        ) {
                            Logging.e("Received app dictation session without app UUID attribute")
                            return
                        }
                        val appUuid =
                            message.attributes.list.firstOrNull {
                                it.id.get() == VoiceAttributeType.AppUuid.value
                            }?.content?.get()?.let {
                                VoiceAttribute.AppUuid().apply { fromBytes(DataBuffer(it)) }
                            }?.uuid?.get()
                        val encoderInfo =
                            message.attributes.list.firstOrNull {
                                it.id.get() == VoiceAttributeType.SpeexEncoderInfo.value
                            }?.content?.get()?.let {
                                VoiceAttribute.SpeexEncoderInfo().apply {
                                    fromBytes(
                                        DataBuffer(it)
                                    )
                                }
                            }?.let { SpeexEncoderInfo.fromPacketData(it) }
                        if (encoderInfo == null) {
                            Logging.e("Received dictation session without encoder info attribute")
                            return
                        }
                        if (pebbleDevice.activeVoiceSession.value != null) {
                            Logging.w("Received voice session while another one is active")
                        }
                        val dictationService: DictationService by inject()
                        val voiceSession =
                            VoiceSession(
                                appUuid,
                                message.sessionId.get().toInt(),
                                encoderInfo,
                                dictationService
                            )
                        Logging.d("Received voice session: $voiceSession")
                        handleSpeechStream(voiceSession)
                    }
                }

                else -> Logging.e("Received unknown voice session message: $message")
            }
        }
    }
}