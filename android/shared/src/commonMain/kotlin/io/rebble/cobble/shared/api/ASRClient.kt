package io.rebble.cobble.shared.api

import io.ktor.client.HttpClient
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.utils.EmptyContent.headers
import io.ktor.utils.io.writer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.flow.flow
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class ASRClient(
    val baseUrl: String,
    private val token: String
) : KoinComponent {
    private val client: HttpClient by inject()
    private val asrInfo = "{}"
    private val scope = CoroutineScope(Dispatchers.IO)

    fun createStreamingSession(block: suspend StreamingSessionScope.() -> Unit) =
        flow {
            val channel = Channel<ByteArray>(capacity = Channel.BUFFERED)
            val session = StreamingSessionScope(channel)
            val request =
                client.post("$baseUrl/NmspServlet/") {
                    headers {
                        append("Connection", "Keep-Alive")
                        append("Authorization", "Bearer $token")
                    }
                }
            request.writer {
                channel.send(asrInfo.encodeToByteArray())
                channel.consumeEach { frameData ->
                    channel.send(frameData)
                }
            }
            block(session)
            channel.send("--boundary--\r\n".encodeToByteArray())
            emit(TODO())
            // TODO: Implement response handling
        }

    class StreamingSessionScope(private val channel: Channel<ByteArray>) {
        suspend fun writeFrames(frameData: ByteArray) {
            writePart(
                frameData,
                "ConcludingAudioParameter",
                "AUDIO_INFO",
                "audio/x-speex-with-header-byte"
            )
        }

        private suspend fun writePart(
            data: ByteArray,
            name: String,
            paramName: String,
            contentType: String
        ) {
            val part =
                """
                --boundary
                Content-Disposition: form-data; name="$name"${if (paramName.isNotEmpty()) "; paramName=\"$paramName\"" else ""}
                Content-Type: $contentType
                Content-Transfer-Encoding: 8bit
                
                """.trimIndent()
            channel.send(part.encodeToByteArray())
            channel.send(data)
            channel.send("\r\n".encodeToByteArray())
        }
    }
}