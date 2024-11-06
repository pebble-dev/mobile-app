package io.rebble.cobble.shared.domain.voice.speechrecognizer

import android.content.Context
import android.content.Intent
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build.VERSION_CODES
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.speech.*
import androidx.annotation.RequiresApi
import androidx.compose.ui.text.intl.Locale
import com.example.speex_codec.SpeexCodec
import com.example.speex_codec.SpeexDecodeResult
import io.rebble.cobble.shared.Logging
import io.rebble.cobble.shared.domain.voice.*
import io.rebble.libpebblecommon.packets.Result
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.ShortBuffer
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.time.Duration.Companion.milliseconds


@RequiresApi(VERSION_CODES.TIRAMISU)
class SpeechRecognizerDictationService: DictationService, KoinComponent {
    private val context: Context by inject()
    private val scope = CoroutineScope(Dispatchers.IO)
    private val audioTrack = AudioTrack.Builder()
            .setAudioFormat(AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(16000)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build())
            .setBufferSizeInBytes(16000)
            .setTransferMode(AudioTrack.MODE_STREAM)
            .build()

    companion object {
        private val AUDIO_LATENCY = 600.milliseconds
        fun buildRecognizerIntent(audioSource: ParcelFileDescriptor? = null, encoding: Int = AudioFormat.ENCODING_PCM_16BIT, sampleRate: Int = 16000) = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            audioSource?.let {
                putExtra(RecognizerIntent.EXTRA_AUDIO_SOURCE, audioSource)
                putExtra(RecognizerIntent.EXTRA_AUDIO_SOURCE_ENCODING, encoding)
                putExtra(RecognizerIntent.EXTRA_AUDIO_SOURCE_CHANNEL_COUNT, 1)
                putExtra(RecognizerIntent.EXTRA_AUDIO_SOURCE_SAMPLING_RATE, sampleRate)
            }
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.current.toLanguageTag())
        }
    }

    sealed class SpeechRecognizerStatus {
        data object Ready: SpeechRecognizerStatus()
        class Error(val error: Int): SpeechRecognizerStatus()
        class Results(val results: List<Pair<Float, String>>): SpeechRecognizerStatus()
    }

    private fun beginSpeechRecognition(speechRecognizer: SpeechRecognizer, intent: Intent) = callbackFlow {
        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            private var lastPartials = emptyList<Pair<Float, String>>()
            override fun onReadyForSpeech(params: Bundle?) {
                trySend(SpeechRecognizerStatus.Ready)
            }

            override fun onBeginningOfSpeech() {
                Logging.i("Speech start detected")
            }

            override fun onRmsChanged(rmsdB: Float) {
                //Logging.d("RMS: $rmsdB")
            }

            override fun onBufferReceived(buffer: ByteArray?) {

            }

            override fun onEndOfSpeech() {
                Logging.i("Speech end detected")
            }

            override fun onError(error: Int) {
                trySend(SpeechRecognizerStatus.Error(error))
            }

            override fun onResults(results: Bundle?) {
                //XXX: appears that with offline on a pixel we only get partials? with scores when they're final
                trySend(SpeechRecognizerStatus.Results(lastPartials))
            }

            override fun onPartialResults(results: Bundle?) {
                val result = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.toList()
                val confidence = results?.getFloatArray(SpeechRecognizer.CONFIDENCE_SCORES)?.toList()
                if (confidence != null && result != null) {
                    lastPartials = confidence.zip(result)
                }
            }

            override fun onEvent(eventType: Int, params: Bundle?) {

            }

        })
        speechRecognizer.startListening(intent)
        awaitClose {

        }
    }.flowOn(Dispatchers.Main)

    override fun handleSpeechStream(speexEncoderInfo: SpeexEncoderInfo, audioStreamFrames: Flow<AudioStreamFrame>) = flow {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            emit(DictationServiceResponse.Error(Result.FailServiceUnavailable))
            return@flow
        }
        val decoder = SpeexCodec(speexEncoderInfo.sampleRate, speexEncoderInfo.bitRate, speexEncoderInfo.frameSize, setOf(SpeexCodec.Preprocessor.DENOISE, SpeexCodec.Preprocessor.AGC))
        val decodeBufLength = Short.SIZE_BYTES * speexEncoderInfo.frameSize
        val decodedBuf = ByteBuffer.allocateDirect(decodeBufLength)
        decodedBuf.order(ByteOrder.nativeOrder())
        val recognizerPipes = ParcelFileDescriptor.createSocketPair()
        val recognizerReadPipe = recognizerPipes[0]
        val recognizerWritePipe = ParcelFileDescriptor.AutoCloseOutputStream(recognizerPipes[1])
        val recognizerIntent = buildRecognizerIntent(recognizerReadPipe, AudioFormat.ENCODING_PCM_16BIT, speexEncoderInfo.sampleRate.toInt())
        //val recognizerIntent = buildRecognizerIntent()
        val speechRecognizer = withContext(Dispatchers.Main) {
            SpeechRecognizer.createOnDeviceSpeechRecognizer(context)
        }
        val supported = withContext(Dispatchers.Main) {
            speechRecognizer.checkRecognitionSupport(recognizerIntent)
        }

        //TODO: handle downloads, etc
        Logging.d("Recognition support: $supported")
        if (supported == RecognitionSupportResult.Unsupported) {
            Logging.e("Speech recognition language/type not supported")
            emit(DictationServiceResponse.Error(Result.FailServiceUnavailable))
            return@flow
        }
        audioTrack.play()

        val audioJob = scope.launch {
            audioStreamFrames
                    .onEach { frame ->
                        if (frame is AudioStreamFrame.Stop) {
                            //Logging.v("Stop")
                            withContext(Dispatchers.IO) {
                                // Pad with extra frame of silence
                                recognizerWritePipe.write(ByteArray(speexEncoderInfo.frameSize * Short.SIZE_BYTES))
                            }
                            recognizerWritePipe.flush()
                            delay(AUDIO_LATENCY)
                            withContext(Dispatchers.Main) {
                                //XXX: Shouldn't use main here for I/O call but recognizer has weird thread behaviour
                                recognizerWritePipe.close()
                                speechRecognizer.stopListening()
                            }
                        } else if (frame is AudioStreamFrame.AudioData) {
                            val result = decoder.decodeFrame(frame.data, decodedBuf, hasHeaderByte = true)
                            if (result != SpeexDecodeResult.Success) {
                                Logging.e("Speex decode error: ${result.name}")
                            }
                            decodedBuf.rewind()
                            withContext(Dispatchers.IO) {
                                audioTrack.write(decodedBuf.array(), decodedBuf.arrayOffset(), decodeBufLength)
                                recognizerWritePipe.write(decodedBuf.array(), decodedBuf.arrayOffset(), decodeBufLength)
                            }
                        }
                    }
                    .flowOn(Dispatchers.IO)
                    .catch {
                        Logging.e("Error in audio stream: $it")
                    }
                    .collect()
        }
        try {
            beginSpeechRecognition(speechRecognizer, recognizerIntent).collect { status ->
                when (status) {
                    is SpeechRecognizerStatus.Ready -> emit(DictationServiceResponse.Ready)
                    is SpeechRecognizerStatus.Error -> {
                        val error = SpeechRecognizerError.fromInt(status.error)
                        Logging.e("Speech recognition error: ${error.name}")
                        when (error) {
                            SpeechRecognizerError.ERROR_NETWORK -> emit(DictationServiceResponse.Error(Result.FailServiceUnavailable))
                            SpeechRecognizerError.ERROR_SPEECH_TIMEOUT -> emit(DictationServiceResponse.Error(Result.FailTimeout))
                            SpeechRecognizerError.ERROR_NO_MATCH -> emit(DictationServiceResponse.Transcription(emptyList()))
                            else -> emit(DictationServiceResponse.Error(Result.FailServiceUnavailable))
                        }
                        emit(DictationServiceResponse.Complete)
                    }
                    is SpeechRecognizerStatus.Results -> {
                        Logging.d("Speech recognition results: ${status.results}")
                        if (status.results.firstOrNull()?.second?.isBlank() != false) {
                            emit(DictationServiceResponse.Transcription(emptyList()))
                            return@collect
                        }
                        emit(DictationServiceResponse.Transcription(
                                listOf(
                                        buildList {
                                            status.results.firstOrNull()?.second?.split(" ")?.forEach {
                                                add(Word(it, 100u))
                                            }
                                        }
                                )
                        ))
                        emit(DictationServiceResponse.Complete)
                    }
                }
            }
        } finally {
            audioTrack.stop()
            audioJob.cancel()
            speechRecognizer.destroy()
            decoder.close()
        }

    }
}