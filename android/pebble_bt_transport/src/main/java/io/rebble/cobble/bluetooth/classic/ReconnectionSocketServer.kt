package io.rebble.cobble.bluetooth.classic

import android.bluetooth.BluetoothAdapter
import androidx.annotation.RequiresPermission
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive
import timber.log.Timber
import java.io.IOException
import java.util.UUID
import kotlin.coroutines.CoroutineContext

class ReconnectionSocketServer(private val adapter: BluetoothAdapter, private val ioDispatcher: CoroutineContext = Dispatchers.IO) {
    companion object {
        private val socketUUID = UUID.fromString("a924496e-cc7c-4dff-8a9f-9a76cc2e9d50")
        private val socketName = "PebbleBluetoothServerSocket"
    }

    @RequiresPermission(android.Manifest.permission.BLUETOOTH_CONNECT)
    suspend fun start() = flow {
        val serverSocket = adapter.listenUsingRfcommWithServiceRecord(socketName, socketUUID)
        serverSocket.use {
            Timber.d("Starting reconnection socket server")
            while (true) {
                //XXX: This is a blocking call, but runInterruptible doesn't seem to work here
                val socket = try {
                    serverSocket.accept(2000)
                } catch (e: IOException) {
                    if (currentCoroutineContext().isActive) {
                        continue
                    } else {
                        break
                    }
                } ?: break
                Timber.d("Accepted connection from ${socket.remoteDevice.address}")
                emit(socket.remoteDevice.address)
                socket.close()
            }
        }
    }.flowOn(ioDispatcher)
}