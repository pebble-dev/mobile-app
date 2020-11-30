package io.rebble.cobble.bluetooth

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.IOException
import java.io.InputStream
import java.nio.ByteBuffer
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

suspend fun InputStream.readFully(buffer: ByteBuffer, offset: Int, count: Int) {
    return withContext(Dispatchers.IO) {
        suspendCancellableCoroutine<Unit> { continuation ->
            continuation.invokeOnCancellation {
                close()
            }

            try {
                var totalRead = 0
                while (coroutineContext.isActive && totalRead < count) {
                    val read = read(buffer.array(), offset + totalRead, count - totalRead)
                    if (read < 0) {
                        throw IOException("Reached end of stream")
                    }

                    totalRead += read
                }

                continuation.resume(Unit)
            } catch (e: Exception) {
                continuation.resumeWithException(e)
            }
        }
    }
}