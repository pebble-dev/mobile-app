package io.rebble.cobble.shared.domain.voice.speechrecognizer

import android.content.Intent
import android.os.Build.VERSION_CODES
import android.speech.RecognitionSupport
import android.speech.RecognitionSupportCallback
import android.speech.SpeechRecognizer
import androidx.annotation.RequiresApi
import androidx.compose.ui.text.intl.Locale
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor

enum class RecognitionSupportResult {
    SupportedOnDevice,
    SupportedOnline,
    NeedsDownload,
    Unsupported
}

@RequiresApi(VERSION_CODES.TIRAMISU)
suspend fun SpeechRecognizer.checkRecognitionSupport(intent: Intent): RecognitionSupportResult {
    val result = CompletableDeferred<RecognitionSupport>()
    val language = Locale.current.toLanguageTag()
    val executor = Dispatchers.IO.asExecutor()
    checkRecognitionSupport(intent, executor, object : RecognitionSupportCallback {
        override fun onSupportResult(recognitionSupport: RecognitionSupport) {
            //TODO: override locale depending on user choice
            result.complete(recognitionSupport)
        }

        override fun onError(error: Int) {
            result.completeExceptionally(Exception("Error checking recognition support: $error"))
        }
    })
    val support = result.await()
    return when {
        support.supportedOnDeviceLanguages.contains(language) -> RecognitionSupportResult.SupportedOnDevice
        support.installedOnDeviceLanguages.contains(language) -> RecognitionSupportResult.SupportedOnDevice
        support.onlineLanguages.contains(language) -> RecognitionSupportResult.SupportedOnline
        support.pendingOnDeviceLanguages.contains(language) -> RecognitionSupportResult.NeedsDownload
        else -> RecognitionSupportResult.Unsupported
    }
}