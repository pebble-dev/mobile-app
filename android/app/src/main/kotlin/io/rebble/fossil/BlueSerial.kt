package io.rebble.fossil

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.widget.Toast
import io.flutter.Log
import kotlinx.coroutines.*
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.*

class BlueSerial(
        private val bluetoothAdapter: BluetoothAdapter,
        private val context: Context,
        private val coroutineExceptionHandler: CoroutineExceptionHandler,
        private val packetCallback: suspend (ByteArray) -> Unit
) : BlueIO {
    private val logTag = "BlueSerial"

    private var coroutineScope: CoroutineScope? = null

    private var targetPebble: BluetoothDevice? = null
    private var serialSocket: BluetoothSocket? = null
    private var inputStream: InputStream? = null
    private var outputStream: OutputStream? = null

    override val isConnected get() = targetPebble != null
    private var onConChange: ((Boolean) -> Unit)? = null

    private var runThread = false

    override suspend fun sendPacket(bytes: ByteArray) {
        @Suppress("BlockingMethodInNonBlockingContext")
        withContext(Dispatchers.IO) {
            outputStream?.write(bytes)
        }
    }

    override fun readStream(buffer: ByteBuffer, offset: Int, count: Int): Int {
        if (inputStream == null) {
            throw IOException("Attempted to read null stream")
        }
        val bytes = inputStream?.read(buffer.array(), offset, count)!!
        if (bytes < 0) {
            throw IOException()
        }
        return bytes
    }


    override fun targetPebble(device: BluetoothDevice): Boolean {
        targetPebble = device
        return connectPebble()
    }

    override fun closePebble() {
        serialSocket?.close()
        runThread = false
        targetPebble = null
        onConChange?.invoke(false)
        coroutineScope?.cancel()
        coroutineScope = null
    }

    override fun getTarget(): BluetoothDevice? {
        return targetPebble
    }

    override fun setOnConnectionChange(f: (Boolean) -> Unit) {
        onConChange = f
    }

    private fun connectPebble(): Boolean {
        val scope = CoroutineScope(SupervisorJob() + coroutineExceptionHandler)
        this.coroutineScope = scope

        val btSerialUUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb")
        serialSocket = targetPebble?.createRfcommSocketToServiceRecord(btSerialUUID)
        try {
            serialSocket?.connect()
        } catch (e: IOException) {
            Log.e(logTag, "Error setting up socket to Pebble: '${e.message}' (Probably didn't pair)")
            Toast.makeText(context, "Failed to connect to Pebble", Toast.LENGTH_SHORT).show()
            targetPebble = null
            return false
        }
        outputStream = serialSocket!!.outputStream
        inputStream = serialSocket!!.inputStream
        runThread = true
        onConChange?.invoke(true)
        with(scope) {
            startPebbleCoroutine()
        }

        return true
    }

    private fun CoroutineScope.startPebbleCoroutine() {
        launch(Dispatchers.IO) {
            val buf: ByteBuffer = ByteBuffer.allocate(8192)

            while (runThread) {
                try {
                    /* READ PACKET META */
                    var count = readStream(buf, 0, 4)
                    while (count < 4) {
                        count = readStream(buf, count, 4 - count)
                    }
                    val metBuf = ByteBuffer.wrap(buf.array())
                    metBuf.order(ByteOrder.BIG_ENDIAN)
                    val length = metBuf.short
                    val endpoint = metBuf.short
                    if (length < 0 || length > buf.capacity()) {
                        Log.w(logTag, "Invalid length in packet (EP $endpoint): got $length")
                        continue
                    }

                    /* READ PACKET CONTENT */
                    count = readStream(buf, 4, length.toInt())
                    while (count < length) {
                        count += readStream(buf, count + 4, length - count)
                    }
                    Log.d(logTag, "Got packet: EP $endpoint | Length $length")

                    buf.rewind()
                    val packet = ByteArray(length.toInt() + 2 * (Short.SIZE_BYTES))
                    buf.get(packet, 0, packet.size)
                    packetCallback.invoke(packet)
                } catch (e: IOException) {
                    if (!serialSocket?.isConnected!!) {
                        Log.i(logTag, "Socket closed / broke (got message ${e.message}), quitting IO thread")
                        break
                    }
                }
                Thread.sleep(10)
            }

            try {
                if (serialSocket != null) {
                    serialSocket?.close()
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
}