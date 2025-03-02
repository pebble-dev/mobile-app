package io.rebble.cobble.shared.handlers

import io.rebble.cobble.shared.Logging
import io.rebble.cobble.shared.domain.common.PebbleDevice
import io.rebble.cobble.shared.domain.voice.AudioStreamFrame
import io.rebble.libpebblecommon.packets.AudioStream
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import org.koin.core.component.KoinComponent

class AudioStreamHandler(
    private val pebbleDevice: PebbleDevice
) : CobbleHandler, KoinComponent {
    init {
        pebbleDevice.negotiationScope.launch {
            val deviceScope = pebbleDevice.connectionScope.filterNotNull().first()
            deviceScope.launch { listenForAudioStream() }
        }
    }

    private suspend fun listenForAudioStream() {
        for (message in pebbleDevice.audioStreamService.receivedMessages) {
            when (message) {
                is AudioStream.DataTransfer -> {
                    if (pebbleDevice.activeVoiceSession.value == null) {
                        Logging.e(
                            "Received audio stream data transfer without active voice session"
                        )
                        return
                    }
                    if (pebbleDevice.activeVoiceSession.value?.sessionId != message.sessionId.get().toInt()) {
                        Logging.e(
                            "Received audio stream data transfer for different session ID (expected ${pebbleDevice.activeVoiceSession.value?.sessionId}, got ${message.sessionId.get()})"
                        )
                        pebbleDevice.audioStreamService.send(
                            AudioStream.StopTransfer(message.sessionId.get())
                        )
                        return
                    }
                    try {
                        withTimeout(3000) {
                            pebbleDevice.activeVoiceSession.value?.audioStreamFrames?.emitAll(
                                message.frames.list.map {
                                    AudioStreamFrame.AudioData(it.data.get().asByteArray())
                                }.asFlow()
                            )
                        }
                    } catch (e: TimeoutCancellationException) {
                        Logging.e("Timed out while emitting audio stream frames")
                    }
                }
                is AudioStream.StopTransfer -> {
                    if (pebbleDevice.activeVoiceSession.value == null) {
                        Logging.e(
                            "Received audio stream stop transfer without active voice session"
                        )
                        return
                    }
                    if (pebbleDevice.activeVoiceSession.value?.sessionId != message.sessionId.get().toInt()) {
                        Logging.e("Received audio stream stop transfer for different session ID")
                        return
                    }
                    pebbleDevice.activeVoiceSession.value?.audioStreamFrames?.emit(
                        AudioStreamFrame.Stop(message.sessionId.get().toInt())
                    )
                    pebbleDevice.activeVoiceSession.value = null
                }
                else -> Logging.e("Received unknown audio stream message: $message")
            }
        }
    }
}