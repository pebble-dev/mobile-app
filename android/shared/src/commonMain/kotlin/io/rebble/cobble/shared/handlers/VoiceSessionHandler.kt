package io.rebble.cobble.shared.handlers

import io.rebble.cobble.shared.Logging
import io.rebble.cobble.shared.domain.common.PebbleDevice
import io.rebble.cobble.shared.domain.voice.SpeexEncoderInfo
import io.rebble.cobble.shared.domain.voice.VoiceSession
import io.rebble.libpebblecommon.packets.SessionSetupCommand
import io.rebble.libpebblecommon.packets.SessionType
import io.rebble.libpebblecommon.packets.VoiceAttribute
import io.rebble.libpebblecommon.packets.VoiceAttributeType
import io.rebble.libpebblecommon.util.DataBuffer
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent

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
                        val voiceSession = VoiceSession(appUuid, message.sessionId.get().toInt(), encoderInfo)
                        Logging.d("Received voice session: $voiceSession")
                        pebbleDevice.activeVoiceSession.value = voiceSession
                    }
                }

                else -> Logging.e("Received unknown voice session message: $message")
            }
        }
    }
}