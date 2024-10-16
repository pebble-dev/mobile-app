package io.rebble.cobble.shared.domain.voice

import kotlinx.coroutines.flow.Flow

interface DictationService {
    fun handleSpeechStream(audioStreamFrames: Flow<AudioStreamFrame>): Flow<DictationServiceResponse>
}