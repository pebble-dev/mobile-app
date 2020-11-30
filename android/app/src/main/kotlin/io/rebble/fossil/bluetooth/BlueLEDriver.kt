package io.rebble.fossil.bluetooth

import android.bluetooth.*
import android.content.Context
import com.juul.able.android.connectGatt
import com.juul.able.device.ConnectGattResult
import com.juul.able.gatt.Gatt
import com.juul.able.gatt.GattStatus
import com.juul.able.gatt.OutOfOrderGattCallbackException
import com.juul.able.gatt.isSuccess
import io.rebble.fossil.util.toBytes
import io.rebble.fossil.util.toHexString
import io.rebble.libpebblecommon.ProtocolHandler
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import timber.log.Timber
import java.io.*


class BlueLEDriver(
        private val context: Context,
        private val protocolHandler: ProtocolHandler
) : BlueIO {
    private var connectivityWatcher: ConnectivityWatcher? = null
    private var connectionParamManager: ConnectionParamManager? = null
    private var gattDriver: BlueGATTServer? = null
    private var targetPebble: BluetoothDevice? = null
    private var protocolIO: ProtocolIO? = null

    companion object {
        fun splitBytesByMTU(bytes: ByteArray, mtu: Int): List<ByteArray> {
            val stream = bytes.inputStream()
            val splitList = mutableListOf<ByteArray>()

            var payload = ByteArray(mtu - 1)
            var count = stream.read(payload)
            while (count > -1) {
                splitList.add(payload.copyOfRange(0, count))
                count = stream.read(payload)
            }
            return splitList
        }
    }

    val connectionStatusChannel = Channel<Boolean>(0)

    enum class LEConnectionState {
        IDLE,
        CONNECTING,
        CONNECTED,
        CLOSED
    }

    var connectionState = LEConnectionState.IDLE
    private var gatt: Gatt? = null

    private suspend fun sendPacket(bytes: UByteArray): Boolean {
        val protocolIO = protocolIO ?: return false
        @Suppress("BlockingMethodInNonBlockingContext")
        protocolIO.write(bytes.toByteArray())
        return true
    }

    suspend fun closePebble() {
        gatt?.disconnect()
        gatt = null
        connectionState = LEConnectionState.CLOSED
        connectionStatusChannel.offer(false)
    }

    /**
     * @param supportsPinningWithoutSlaveSecurity ??
     * @param belowLollipop Used by official app to indicate a device below lollipop?
     * @param clientMode Forces phone-as-client mode
     */
    private fun pairTriggerFlagsToBytes(supportsPinningWithoutSlaveSecurity: Boolean, belowLollipop: Boolean, clientMode: Boolean): ByteArray {
        val boolArr = booleanArrayOf(true, supportsPinningWithoutSlaveSecurity, false, belowLollipop, clientMode, false)
        val byteArr = boolArr.toBytes()
        Timber.d("Pair trigger flags ${byteArr.toHexString()}")
        return byteArr
    }

    suspend fun deviceConnectivity() {
        val mtu = gatt?.requestMtu(339)
        if (mtu?.isSuccess == true) {
            Timber.d("MTU Changed, new mtu $mtu")
            gattDriver!!.setMTU(mtu.mtu)
        }
        if (connectivityWatcher!!.subscribe()) {
            var status = connectivityWatcher!!.getStatus()
            if (status.connected) {
                if (status.paired && targetPebble!!.bondState == BluetoothDevice.BOND_BONDED) {
                    Timber.d("Paired, connecting gattDriver")
                    connect()
                } else {
                    Timber.d("Not yet paired, pairing...")
                    if (targetPebble!!.bondState == BluetoothDevice.BOND_BONDED) {
                        Timber.d("Phone already paired but watch not paired, removing bond and re-pairing")
                        targetPebble!!::class.java.getMethod("removeBond").invoke(targetPebble)
                    }
                    val pairService = gatt!!.getService(BlueGATTConstants.UUIDs.PAIRING_SERVICE_UUID)
                    if (pairService != null) {
                        val pairTrigger = pairService.getCharacteristic(BlueGATTConstants.UUIDs.PAIRING_TRIGGER_CHARACTERISTIC)
                        if (pairTrigger != null) {
                            if (pairTrigger.properties and BluetoothGattCharacteristic.PROPERTY_WRITE > 0) {
                                GlobalScope.launch(Dispatchers.IO) { gatt!!.writeCharacteristic(pairTrigger, pairTriggerFlagsToBytes(status.supportsPinningWithoutSlaveSecurity, belowLollipop = false, false), BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT) }
                                if (status.supportsPinningWithoutSlaveSecurity) {
                                    /*if (!targetPebble?.createBond()!!) {
                                        Timber.e("Failed to create bond")
                                        closePebble()
                                        return
                                    }*/
                                    targetPebble?.createBond()
                                }
                                status = connectivityWatcher!!.getStatus()
                                if (status.paired) {
                                    Timber.d("Paired successfully, connecting gattDriver")
                                    connect()
                                    return
                                } else {
                                    Timber.e("Failed to pair")
                                }
                            }
                        } else {
                            Timber.e("pairTrigger is null")
                        }
                    } else {
                        Timber.e("pairService is null")
                    }
                }
            } else {
                closePebble()
                throw Exception() //TODO
            }
        }
        closePebble()
    }

    @FlowPreview
    override fun startSingleWatchConnection(device: BluetoothDevice): Flow<SingleConnectionStatus> = flow {
        coroutineScope {
            if (device.type == BluetoothDevice.DEVICE_TYPE_CLASSIC || device.type == BluetoothDevice.DEVICE_TYPE_UNKNOWN) {
                throw IllegalArgumentException("Non-LE device should not use LE driver")
            }

            if (targetPebble != null && connectionState == LEConnectionState.CONNECTED && device.address == this@BlueLEDriver.targetPebble!!.address) {
                emit(SingleConnectionStatus.Connected(device))
                return@coroutineScope
            } else if (connectionState != LEConnectionState.IDLE) {
                return@coroutineScope
            }
            emit(SingleConnectionStatus.Connecting(device))

            this@BlueLEDriver.targetPebble = device

            val server = BlueGATTServer(device, context)
            gattDriver = server

            connectionState = LEConnectionState.CONNECTING
            launch(Dispatchers.IO) {
                if (!server.initServer()) {
                    Timber.e("initServer failed")
                    return@launch
                }
                var result: ConnectGattResult?
                try {
                    withTimeout(8000) {
                        result = targetPebble!!.connectGatt(context)
                    }
                } catch (e: CancellationException) {
                    Timber.w("connectGatt timed out")
                    delay(1000)
                    result = targetPebble!!.connectGatt(context)
                }
                if (result == null) {
                    Timber.e("connectGatt null")
                }
                when (result) {
                    is ConnectGattResult.Success -> {
                        gatt = (result as ConnectGattResult.Success).gatt

                        Timber.i("Pebble connected (initial)")

                        launch {
                            while (true) {
                                gatt!!.onCharacteristicChanged.collect {
                                    Timber.d("onCharacteristicChanged ${it.characteristic.uuid}")
                                    connectivityWatcher?.onCharacteristicChanged(it.value, it.characteristic)
                                }
                            }
                        }

                        connectionParamManager = ConnectionParamManager(gatt!!)
                        connectivityWatcher = ConnectivityWatcher(gatt!!)
                        val servicesRes: GattStatus = try {
                            gatt!!.discoverServices()
                        } catch (e: OutOfOrderGattCallbackException) {
                            Timber.e(e)
                            closePebble()
                            return@launch
                        }
                        if (servicesRes == BluetoothGatt.GATT_SUCCESS) {
                            if (gatt?.getService(BlueGATTConstants.UUIDs.PAIRING_SERVICE_UUID)?.getCharacteristic(BlueGATTConstants.UUIDs.CONNECTION_PARAMETERS_CHARACTERISTIC) != null) {
                                if (connectionParamManager!!.subscribe()) {
                                    Timber.d("Starting connectivity after connparams")
                                    deviceConnectivity()
                                }
                            } else {
                                Timber.d("Starting connectivity without connparams")
                                deviceConnectivity()
                            }
                        } else {
                            Timber.e("Failed to discover services")
                            closePebble()
                        }

                    }
                    is ConnectGattResult.Failure -> {
                        Timber.e("connectGatt failed")
                        Timber.e((result as ConnectGattResult.Failure).cause)
                    }
                }
            }
            if (connectionStatusChannel.receive()) {
                val sendLoop = launch { protocolHandler.startPacketSendingLoop(::sendPacket) }
                emit(SingleConnectionStatus.Connected(device))
                protocolIO = ProtocolIO(gattDriver!!.inputStream, gattDriver!!.outputStream, protocolHandler)
                protocolIO!!.readLoop()
                gattDriver!!.closePebble()
                closePebble()
                sendLoop.cancel()
            }
        }
    }

    private suspend fun connect() {
        Timber.d("Connect called")
        if (!gattDriver?.connectPebble()!!) {
            closePebble()
        } else {
            connectionStatusChannel.send(true)
        }
    }
}