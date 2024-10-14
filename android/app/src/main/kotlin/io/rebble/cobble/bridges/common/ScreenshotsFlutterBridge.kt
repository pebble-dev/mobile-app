package io.rebble.cobble.bridges.common

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import io.rebble.cobble.bridges.FlutterBridge
import io.rebble.cobble.bridges.ui.BridgeLifecycleController
import io.rebble.cobble.pigeons.Pigeons
import io.rebble.cobble.shared.domain.state.ConnectionStateManager
import io.rebble.cobble.shared.domain.state.watchOrNull
import io.rebble.cobble.util.launchPigeonResult
import io.rebble.libpebblecommon.packets.*
import io.rebble.libpebblecommon.util.DataBuffer
import io.rebble.libpebblecommon.util.ushr
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import okio.Buffer
import timber.log.Timber
import java.io.File
import javax.inject.Inject
import kotlin.experimental.and

class ScreenshotsFlutterBridge @Inject constructor(
        private val context: Context,
        bridgeLifecycleController: BridgeLifecycleController,
        private val coroutineScope: CoroutineScope
) : FlutterBridge, Pigeons.ScreenshotsControl {
    init {
        bridgeLifecycleController.setupControl(Pigeons.ScreenshotsControl::setup, this)
    }

    override fun takeWatchScreenshot(result: Pigeons.Result<Pigeons.ScreenshotResult>) {
        coroutineScope.launchPigeonResult(result) {

            try {
                ConnectionStateManager.connectionState.value.watchOrNull?.screenshotService?.send(ScreenshotRequest()) ?: return@launchPigeonResult Pigeons.ScreenshotResult.Builder().setSuccess(false).build()

                val firstResult = receiveScreenshotResponse() ?: return@launchPigeonResult Pigeons.ScreenshotResult.Builder().setSuccess(false).build()

                val header = ScreenshotHeader().apply {
                    m.fromBytes(DataBuffer(firstResult.data.get()))
                }

                if (header.responseCode.get() != ScreenshotResponseCode.OK.rawCode) {
                    Timber.e(
                            "Screenshot fail: %s",
                            ScreenshotResponseCode.fromRawCode(header.responseCode.get())
                    )

                    return@launchPigeonResult Pigeons.ScreenshotResult.Builder().setSuccess(false).build()
                }

                val width = header.width.get().toInt()
                val height = header.height.get().toInt()
                val version = ScreenshotVersion.fromRawCode(header.version.get())

                val buffer = Buffer()

                val expectedBytes = when (version) {
                    ScreenshotVersion.BLACK_WHITE_1_BIT -> {
                        width * height / 8
                    }

                    ScreenshotVersion.COLOR_8_BIT -> {
                        width * height
                    }
                }

                buffer.request(expectedBytes.toLong())

                buffer.write(header.data.get().asByteArray())

                while (buffer.size < expectedBytes) {
                    val nextSegment = receiveScreenshotResponse() ?: return@launchPigeonResult Pigeons.ScreenshotResult.Builder().setSuccess(false).build()
                    buffer.write(nextSegment.data.get().asByteArray())
                }

                val pixels = when (version) {
                    ScreenshotVersion.BLACK_WHITE_1_BIT -> {
                        decodeBlackWhite1BitImagePixels(width, height, buffer)
                    }

                    ScreenshotVersion.COLOR_8_BIT -> {
                        decodeColor8BitImagePixels(width, height, buffer)
                    }
                }

                val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                bitmap.setPixels(pixels, 0, width, 0, 0, width, height)

                val targetFile = File(context.cacheDir, "screenshot.png")

                withContext(Dispatchers.IO) {
                    targetFile.outputStream().buffered().use {
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
                    }
                }

                Pigeons.ScreenshotResult.Builder()
                        .setSuccess(true)
                        .setImagePath(targetFile.absolutePath)
                        .build()
            } catch (e: Exception) {
                Timber.e(e, "Screenshot fetch failed")

                Pigeons.ScreenshotResult.Builder().setSuccess(false).build()
            }
        }
    }

    private suspend fun receiveScreenshotResponse(): ScreenshotResponse? {
        return withTimeout(10_000) {
            ConnectionStateManager.connectionState.value.watchOrNull?.screenshotService?.receivedMessages?.receive()
        }
    }
}

private fun decodeBlackWhite1BitImagePixels(width: Int, height: Int, buffer: Buffer): IntArray {
    val pixels = IntArray(width * height)
    val rawPixelData = buffer.readByteArray()

    val bytesPerRow = width / 8

    for (y in 0 until height) {
        val rowPosition = y * width
        val rawRowPosition = y * bytesPerRow

        for (x in 0 until width) {
            val white = ((rawPixelData[rawRowPosition + x / 8] ushr (x % 8)) and 0x1.toByte()) != 0.toByte()

            pixels[rowPosition + x] = if (white) {
                Color.WHITE
            } else {
                Color.BLACK
            }
        }
    }

    return pixels
}

private fun decodeColor8BitImagePixels(width: Int, height: Int, buffer: Buffer): IntArray {
    val pixels = IntArray(width * height)
    val rawPixelData = buffer.readByteArray()

    for (y in 0 until height) {
        val rowPosition = y * width

        for (x in 0 until width) {
            val position = rowPosition + x

            val pixel = rawPixelData[position]

            val red = ((pixel ushr 4) and 0b11) * 85
            val green = ((pixel ushr 2) and 0b11) * 85
            val blue = ((pixel ushr 0) and 0b11) * 85

            pixels[position] = Color.rgb(red, green, blue)
        }
    }

    return pixels
}