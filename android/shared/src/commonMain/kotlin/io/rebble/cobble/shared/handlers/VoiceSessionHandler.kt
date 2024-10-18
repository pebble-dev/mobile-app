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
        private val pebbleDevice: PebbleDevice,
): CobbleHandler, KoinComponent {
    init {
        pebbleDevice.negotiationScope.launch {
            val deviceScope = pebbleDevice.connectionScope.filterNotNull().first()
            deviceScope.launch { listenForVoiceSessions() }.invokeOnCompletion {
                pebbleDevice.activeVoiceSession.value = null
            }
        }
    }

    private fun makeTranscription(sentences: List<List<io.rebble.cobble.shared.domain.voice.Word>>): VoiceAttribute {
        val data = VoiceAttribute.Transcription(
                sentences = sentences.map { sentence ->
                    Sentence(
                            words = sentence.map { word ->
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

    private suspend fun listenForVoiceSessions() {
        for (message in pebbleDevice.voiceService.receivedMessages) {
            when (message) {
                is SessionSetupCommand -> {
                    if (message.sessionType.get() == SessionType.Dictation.value) {
                        val appInitiated = message.flags.get() and 1u != 0u
                        if (appInitiated && !message.attributes.list.any { it.id.get() == VoiceAttributeType.AppUuid.value }) {
                            Logging.e("Received app dictation session without app UUID attribute")
                            return
                        }
                        val appUuid = message.attributes.list.firstOrNull { it.id.get() == VoiceAttributeType.AppUuid.value }?.content?.get()?.let {
                            VoiceAttribute.AppUuid().apply { fromBytes(DataBuffer(it)) }
                        }?.uuid?.get()
                        val encoderInfo = message.attributes.list.firstOrNull { it.id.get() == VoiceAttributeType.SpeexEncoderInfo.value }?.content?.get()?.let {
                            VoiceAttribute.SpeexEncoderInfo().apply { fromBytes(DataBuffer(it)) }
                        }?.let { SpeexEncoderInfo.fromPacketData(it) }
                        if (encoderInfo == null) {
                            Logging.e("Received dictation session without encoder info attribute")
                            return
                        }
                        if (pebbleDevice.activeVoiceSession.value != null) {
                            Logging.w("Received voice session while another one is active")
                        }
                        val dictationService: DictationService by inject()
                        val voiceSession = VoiceSession(appUuid, message.sessionId.get().toInt(), encoderInfo, dictationService)
                        Logging.d("Received voice session: $voiceSession")

                        var sentReady = false
                        dictationService.handleSpeechStream(voiceSession.encoderInfo, voiceSession.audioStreamFrames)
                                .takeWhile { it !is DictationServiceResponse.Complete }
                                .onEach {
                                    Logging.v("DictationServiceResponse: $it")
                                }
                                .collect {
                                    when (it) {
                                        is DictationServiceResponse.Ready -> {
                                            pebbleDevice.activeVoiceSession.value = voiceSession
                                            pebbleDevice.voiceService.send(SessionSetupResult(
                                                    sessionType = SessionType.Dictation,
                                                    result = Result.Success
                                            ))
                                            sentReady = true
                                        }
                                        is DictationServiceResponse.Error -> {
                                            if (sentReady) {
                                                pebbleDevice.voiceService.send(DictationResult(
                                                        voiceSession.sessionId.toUShort(),
                                                        it.result,
                                                        emptyList()
                                                ))
                                            } else {
                                                pebbleDevice.voiceService.send(SessionSetupResult(
                                                        sessionType = SessionType.Dictation,
                                                        result = it.result
                                                ))
                                            }
                                        }
                                        is DictationServiceResponse.Transcription -> {
                                            val a = DictationResult(
                                                    voiceSession.sessionId.toUShort(),
                                                    Result.Success,
                                                    listOf(
                                                            makeTranscription(it.sentences)
                                                    )
                                            )
                                            pebbleDevice.voiceService.send(a)
                                        }
                                    }
                                }
                    }
                }

                else -> Logging.e("Received unknown voice session message: $message")
            }
        }
    }
}