package io.rebble.cobble.shared.domain.voice

import io.rebble.cobble.shared.Logging
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.datetime.Clock
import kotlin.time.Duration.Companion.seconds

class NullDictationService: DictationService {
    override fun handleSpeechStream(speexEncoderInfo: SpeexEncoderInfo, audioStreamFrames: Flow<AudioStreamFrame>) = flow {
        val frames = mutableListOf<AudioStreamFrame.AudioData>()
        audioStreamFrames.onStart {
            emit(DictationServiceResponse.Ready)
        }
        .onEach {
            Logging.v("AudioStreamFrame: $it")
        }
        .collect {
            if (it is AudioStreamFrame.Stop) {
                emit(DictationServiceResponse.Transcription(listOf(
                        "Hello World!".split(" ").map { word -> Word(word, 100u) })
                ))
                withContext(Dispatchers.IO) {
                    writeRecording(speexEncoderInfo, frames)
                }
                emit(DictationServiceResponse.Complete)
            } else if (it is AudioStreamFrame.AudioData) {
                frames.add(it)
            }
        }
    }
}

expect suspend fun writeRecording(encoderInfo: SpeexEncoderInfo, frames: List<AudioStreamFrame.AudioData>)