package io.rebble.fossil.bluetooth

import android.bluetooth.*
import android.content.Context
import com.juul.able.android.connectGatt
import com.juul.able.device.ConnectGattResult
import com.juul.able.gatt.Gatt
import com.juul.able.gatt.GattStatus
import com.juul.able.gatt.OutOfOrderGattCallbackException
import io.rebble.fossil.util.toBytes
import io.rebble.fossil.util.toHexString
import io.rebble.libpebblecommon.ProtocolHandler
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import timber.log.Timber
import java.io.*
import java.nio.ByteBuffer
import java.nio.ByteOrder


class BlueLEDriver(
        private val context: Context,
        private val protocolHandler: ProtocolHandler
) : BlueIO {
    private var connectivityWatcher: ConnectivityWatcher? = null
    private var connectionParamManager: ConnectionParamManager? = null
    private var gattDriver: BlueGATTServer? = null
    private var targetPebble: BluetoothDevice? = null

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
        if (gattDriver != null) {
            return if (!gattDriver!!.sendPacket(bytes.toByteArray())) {
                closePebble()
                false
            } else {
                true
            }
        }
        return false
    }

    fun closePebble() {
        GlobalScope.launch(Dispatchers.IO) {
            gatt?.disconnect()
            gatt = null
            connectionState = LEConnectionState.CLOSED
            connectionStatusChannel.offer(false)
        }
    }

    fun getTarget(): BluetoothDevice? {
        return targetPebble
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
                                    gattDriver?.onCharacteristicChanged(it.value, it.characteristic)
                                    connectivityWatcher?.onCharacteristicChanged(it.value, it.characteristic)
                                }
                            }
                        }

                        /*gattDriver = BlueGATTClient(gatt) {
                            processGattPacket(it)
                        }*/

                        connectionParamManager = ConnectionParamManager(gatt!!)
                        connectivityWatcher = ConnectivityWatcher(gatt!!) /*{
                            Timber.d("Connectivity status changed: ${it}")
                            if (connectionState == LEConnectionState.PAIRING) {
                                if (it.pairingErrorCode == ConnectivityWatcher.PairingErrorCode.CONFIRM_VALUE_FAILED) {
                                    Timber.e("Failed to pair")
                                    closePebble()
                                } else if (it.paired) {
                                    connectionState = LEConnectionState.CONNECTING_GATT
                                    if (!(gattDriver?.isConnected ?: false)) {
                                        gatt!!.discoverServices()
                                    } else {
                                        connectionState = LEConnectionState.CONNECTED
                                    }
                                }
                            } else if (connectionState == LEConnectionState.CONNECTING_CONNECTIVITY) {
                                if (it.paired) {
                                    if (targetPebble!!.bondState != BluetoothDevice.BOND_BONDED) {
                                        Timber.w("Watch bonded, phone not bonded")
                                    } else {
                                        connectionState = LEConnectionState.CONNECTING_GATT
                                        if (gatt!!.discoverServices() == BluetoothGatt.GATT_SUCCESS) {
                                            connect()
                                        }
                                    }
                                } else {
                                    connectionState = LEConnectionState.PAIRING
                                    if (targetPebble!!.bondState == BluetoothDevice.BOND_BONDED) {
                                        Timber.w("Phone bonded, watch not bonded")
                                    }
                                    val pairService = gatt!!.getService(BlueGATTConstants.UUIDs.PAIRING_SERVICE_UUID)
                                    if (pairService == null) {
                                        Timber.e("pairService is null")
                                    } else {
                                        val pairTrigger = pairService.getCharacteristic(BlueGATTConstants.UUIDs.PAIRING_TRIGGER_CHARACTERISTIC)
                                        if (pairTrigger == null) {
                                            Timber.e("pairTrigger is null")
                                        } else {
                                            Timber.d("Pairing device")
                                            if (pairTrigger.properties and BluetoothGattCharacteristic.PROPERTY_WRITE > 0) {
                                                if (!gatt!!.writeCharacteristic(pairTrigger, pairTriggerFlagsToBytes(it.supportsPinningWithoutSlaveSecurity, Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP, gattDriver is BlueGATTClient),BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT).isSuccess) {
                                                    Timber.e("Failed to write to pair characteristic")
                                                    closePebble()
                                                }
                                            }
                                            if (!device.createBond()) {
                                                Timber.e("Failed to create bond")
                                                closePebble()
                                            }
                                        }
                                    }
                                }
                            }
                        }*/
                        val servicesRes: GattStatus = try {
                            gatt!!.discoverServices()
                        } catch (e: OutOfOrderGattCallbackException) {
                            Timber.e(e)
                            closePebble()
                            return@launch
                        }
                        if (servicesRes == BluetoothGatt.GATT_SUCCESS) {
                            if (gatt?.getService(BlueGATTConstants.UUIDs.PAIRING_SERVICE_UUID)?.getCharacteristic(BlueGATTConstants.UUIDs.CONNECTION_PARAMETERS_CHARACTERISTIC) != null) {
                                /*val mtu = gatt?.requestMtu(339)
                                if (mtu?.isSuccess == true) {
                                    Timber.d("MTU Changed, new mtu $mtu")
                                    gattDriver!!.setMTU(mtu.mtu)
                                }*/
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
                emit(SingleConnectionStatus.Connected(device))
                launch { protocolHandler.startPacketSendingLoop(::sendPacket) }
                try {
                    //TODO: Common class for serial and LE as this is unnecessarily reused code
                    val buf: ByteBuffer = ByteBuffer.allocate(8192)
                    while (true) {
                        gattDriver!!.packetInputStream.readFully(buf, 0, 4)
                        val metBuf = ByteBuffer.wrap(buf.array())
                        metBuf.order(ByteOrder.BIG_ENDIAN)
                        val length = metBuf.short
                        val endpoint = metBuf.short
                        if (length < 0 || length > buf.capacity()) {
                            Timber.w("Invalid length in packet (EP $endpoint): got $length")
                            continue
                        }

                        /* READ PACKET CONTENT */
                        gattDriver!!.packetInputStream.readFully(buf, 4, length.toInt())

                        Timber.d("Got packet: EP $endpoint | Length $length")

                        buf.rewind()
                        val packet = ByteArray(length.toInt() + 2 * (Short.SIZE_BYTES))
                        buf.get(packet, 0, packet.size)
                        protocolHandler.receivePacket(packet.toUByteArray())
                    }
                } finally {
                    gattDriver!!.closePebble()
                    closePebble()
                    //TODO: cleanup needed?
                }
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