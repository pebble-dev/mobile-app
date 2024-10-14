package io.rebble.cobble.shared.middleware

import io.rebble.cobble.shared.Logging
import io.rebble.cobble.shared.domain.common.PebbleDevice
import io.rebble.libpebblecommon.packets.LogDump
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlin.random.Random

class DeviceLogController(
        private val device: PebbleDevice
) {
    private val deviceLogsService = device.logDumpService
    private val scope = CoroutineScope(Dispatchers.IO)
    private val mutex = Mutex()

    /**
     * Request a log dump from the watch.
     * @param generation The generation of the log dump to request. 0 for the latest. (1 for the previous, etc.)
     * @return A flow of log dump messages, first check for [LogDump.NoLogs].
     */
    suspend fun requestLogDump(generation: Int = 0): Flow<LogDump.ReceivedLogDumpMessage> {
        val cookie = Random.Default.nextInt().toUInt()
        val flow = deviceLogsService.receivedMessages.receiveAsFlow()
                .onStart {
                    mutex.lock()
                }
                .filterIsInstance<LogDump.ReceivedLogDumpMessage>()
                .filter { it.cookie.get() == cookie }
                .takeWhile {
                    it !is LogDump.Done
                }
                .onCompletion {
                    Logging.d("Log dump completed")
                    mutex.unlock()
                }
        deviceLogsService.send(
                LogDump.RequestLogDump(generation.toUByte(), cookie)
        )
        return flow
    }
}