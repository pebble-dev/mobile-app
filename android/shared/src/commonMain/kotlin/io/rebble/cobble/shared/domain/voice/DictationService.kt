package io.rebble.cobble.shared.domain.voice

import kotlinx.coroutines.flow.Flow

interface DictationService {
    fun handleSpeechStream(
        speexEncoderInfo: SpeexEncoderInfo,
        audioStreamFrames: Flow<AudioStreamFrame>
    ): Flow<DictationServiceResponse>
}