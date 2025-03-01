package io.rebble.cobble.shared.domain.voice

import io.rebble.cobble.shared.Logging
import kotlinx.datetime.Clock
import org.koin.mp.KoinPlatformTools

actual suspend fun writeRecording(
    encoderInfo: SpeexEncoderInfo,
    frames: List<AudioStreamFrame.AudioData>
) {
    val koin = KoinPlatformTools.defaultContext().get()
    val context = koin.get<android.content.Context>()
    val timestamp = Clock.System.now().epochSeconds
    val file = context.getExternalFilesDir(null)!!.resolve("recording-$timestamp.spx")
    file.outputStream().use { stream ->
        frames.forEach {
            stream.write(it.data)
        }
    }
    Logging.d("Wrote recording to $file")
    val metadataFile = context.getExternalFilesDir(null)!!.resolve("recording-$timestamp.json")
    metadataFile.writeText(
        """
        {
            "version": "${encoderInfo.version}",
            "sampleRate": ${encoderInfo.sampleRate},
            "bitRate": ${encoderInfo.bitRate},
            "bitstreamVersion": ${encoderInfo.bitstreamVersion},
            "frameSize": ${encoderInfo.frameSize}
        }
        """.trimIndent()
    )
}