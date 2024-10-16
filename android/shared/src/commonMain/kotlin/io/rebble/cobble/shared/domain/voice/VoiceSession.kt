package io.rebble.cobble.shared.domain.voice

import com.benasher44.uuid.Uuid
import io.rebble.libpebblecommon.packets.VoiceAttribute
import kotlinx.coroutines.flow.MutableSharedFlow

sealed class AudioStreamFrame {
    data class AudioData(val data: ByteArray) : AudioStreamFrame() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false

            other as AudioData

            return data.contentEquals(other.data)
        }

        override fun hashCode(): Int {
            return data.contentHashCode()
        }
    }

    data class Stop(val sessionId: Int) : AudioStreamFrame()
}

class VoiceSession(
        val appUuid: Uuid?,
        val sessionId: Int,
        val encoderInfo: SpeexEncoderInfo,
        val recognizer: DictationService
) {
    val audioStreamFrames = MutableSharedFlow<AudioStreamFrame>(extraBufferCapacity = 16)
    override fun toString(): String {
        return "VoiceSession(appUuid=$appUuid, sessionId=$sessionId, encoderInfo=$encoderInfo)"
    }
}

data class SpeexEncoderInfo(
        val version: String,
        val sampleRate: Long,
        val bitRate: Int,
        val bitstreamVersion: Int,
        val frameSize: Int
) {
    companion object {
        fun fromPacketData(data: VoiceAttribute.SpeexEncoderInfo): SpeexEncoderInfo {
            return SpeexEncoderInfo(
                    version = data.version.get(),
                    sampleRate = data.sampleRate.get().toLong(),
                    bitRate = data.bitRate.get().toInt(),
                    bitstreamVersion = data.bitstreamVersion.get().toInt(),
                    frameSize = data.frameSize.get().toInt()
            )
        }
    }
}