package io.rebble.fossil.bluetooth

import android.bluetooth.*
import android.content.Context
import android.os.Build
import io.rebble.fossil.util.toBytes
import io.rebble.fossil.util.toHexString
import io.rebble.libpebblecommon.ProtocolHandler
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import timber.log.Timber
import java.nio.ByteBuffer
import kotlin.math.ceil
import kotlin.math.min


class BlueLEDriver(
        private val targetPebble: BluetoothDevice,
        private val context: Context,
        private val protocolHandler: ProtocolHandler
) : BlueIO {
    private var connectivityWatcher: ConnectivityWatcher? = null
    private var connectionParamManager: ConnectionParamManager? = null
    private var gattDriver: BlueGATTIO? = null

    init {
        if (targetPebble.type == BluetoothDevice.DEVICE_TYPE_CLASSIC || targetPebble.type == BluetoothDevice.DEVICE_TYPE_UNKNOWN) {
            throw IllegalArgumentException("Non-LE device should not use LE driver")
        }
    }

    companion object {
        fun splitBytesByMTU(bytes: ByteArray, mtu: Int): List<ByteArray> {
            val count = ceil(bytes.size.toFloat() / (mtu.toFloat() - 1)).toInt()
            val buf = ByteBuffer.wrap(bytes)
            val splitList = mutableListOf<ByteArray>()
            for (i in 0..count - 1) {
                var payload = ByteArray(min(mtu, buf.array().size - buf.position()))
                buf.get(payload)
                if (payload.size < mtu - 1) {
                    payload += ByteArray((mtu - 1) - payload.size)
                }
                splitList.add(payload)
            }
            return splitList
        }
    }

    val connectionStatusChannel = Channel<Boolean>(0)

    val isConnected: Boolean get() = connectionState == LEConnectionState.CONNECTED

    enum class LEConnectionState {
        IDLE,
        CONNECTING,
        CONNECTING_CONNECTIVITY,
        PAIRING,
        CONNECTING_GATT,
        CONNECTED,
        CLOSED
    }

    var connectionState = LEConnectionState.IDLE
    private var gatt: BluetoothGatt? = null

    private fun sendPacket(bytes: UByteArray): Boolean {
        gattDriver?.sendPacket(bytes.toByteArray()) {
            if (!it) {
                closePebble()
            }
        }
        //TODO return properly based on success?
        return true
    }

    fun closePebble() {
        gatt?.close()
        gatt = null
        connectionState = LEConnectionState.CLOSED
        connectionStatusChannel.offer(false)
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

    private var remaining = 0
    private var packetBuf: ByteBuffer? = null

    private suspend fun processGattPacket(packet: GATTPacket) {
        val payload = packet.data.copyOfRange(1, packet.data.size)
        if (packetBuf != null) {
            remaining = remaining - payload.size
            if (remaining < 0) {
                remaining = 0
                packetBuf = null
                processGattPacket(packet)
                return
            }

            packetBuf!!.put(payload)
            if (remaining == 0) {
                protocolHandler.receivePacket(packetBuf!!.array().toUByteArray())
                packetBuf = null
            }
        } else {
            val header = ByteBuffer.wrap(payload)
            val length = header.getShort().toInt() + 4
            Timber.d("Size ${length}")
            if (length > payload.size) {
                remaining = length - payload.size
                packetBuf = ByteBuffer.allocate(length)
                packetBuf!!.put(payload)
            } else {
                protocolHandler.receivePacket(payload.toUByteArray())
            }
        }
    }

    private val gattCallbacks = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            if (gatt?.device?.address != targetPebble.address) return
            if (status == BluetoothGatt.GATT_SUCCESS) {
                when (newState) {
                    BluetoothGatt.STATE_CONNECTED -> {
                        Timber.i("Pebble connected (initial)")
                        gattDriver = /*BlueGATTClient(gatt!!)*/ BlueGATTServer(context) {
                            protocolHandler.receivePacket(it.toByteArray().sliceArray(1..it.toByteArray().size - 1).toUByteArray())
                        }

                        connectionParamManager = ConnectionParamManager(gatt!!) {
                            connectionState = LEConnectionState.CONNECTING_CONNECTIVITY
                            if (!connectivityWatcher?.subscribe()!!) {
                                closePebble()
                            }
                        }

                        connectivityWatcher = ConnectivityWatcher(gatt) {
                            Timber.d("Connectivity status changed: ${it}")
                            if (connectionState == LEConnectionState.PAIRING) {
                                if (it.pairingErrorCode == ConnectivityWatcher.PairingErrorCode.CONFIRM_VALUE_FAILED) {
                                    Timber.e("Failed to pair")
                                    closePebble()
                                } else if (it.paired) {
                                    connectionState = LEConnectionState.CONNECTING_GATT
                                    if (!(gattDriver?.isConnected ?: false)) {
                                        gatt.discoverServices()
                                    } else {
                                        connectionState = LEConnectionState.CONNECTED
                                    }
                                }
                            } else if (connectionState == LEConnectionState.CONNECTING_CONNECTIVITY) {
                                if (it.paired) {
                                    if (targetPebble.bondState != BluetoothDevice.BOND_BONDED) {
                                        Timber.w("Watch bonded, phone not bonded")
                                    } else {
                                        connectionState = LEConnectionState.CONNECTING_GATT
                                        gatt.discoverServices()
                                    }
                                } else {
                                    connectionState = LEConnectionState.PAIRING
                                    if (targetPebble.bondState == BluetoothDevice.BOND_BONDED) {
                                        Timber.w("Phone bonded, watch not bonded")
                                    }
                                    val pairService = gatt.getService(BlueGATTConstants.UUIDs.PAIRING_SERVICE_UUID)
                                    if (pairService == null) {
                                        Timber.e("pairService is null")
                                    } else {
                                        val pairTrigger = pairService.getCharacteristic(BlueGATTConstants.UUIDs.PAIRING_TRIGGER_CHARACTERISTIC)
                                        if (pairTrigger == null) {
                                            Timber.e("pairTrigger is null")
                                        } else {
                                            Timber.d("Pairing device")
                                            if (pairTrigger.properties and BluetoothGattCharacteristic.PROPERTY_WRITE > 0) {
                                                pairTrigger.setValue(pairTriggerFlagsToBytes(it.supportsPinningWithoutSlaveSecurity, Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP, gattDriver is BlueGATTClient))
                                                if (!gatt.writeCharacteristic(pairTrigger)) {
                                                    Timber.e("Failed to write to pair characteristic")
                                                    closePebble()
                                                }
                                            }
                                            if (!gatt.device?.createBond()!!) {
                                                Timber.e("Failed to create bond")
                                                closePebble()
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        gatt.discoverServices()
                    }

                    BluetoothGatt.STATE_DISCONNECTED -> {
                        Timber.i("Pebble disconnected")
                        connectionState = LEConnectionState.IDLE
                    }
                }
            } else {
                Timber.e("Connection error: ${GattStatus(status)})")
            }
        }
        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            if (gatt?.device?.address != targetPebble.address) return
            Timber.d("Services discovered")
            gatt?.services?.forEach {
                Timber.d(it.uuid.toString())
            }
            if (connectionState == LEConnectionState.CONNECTING) {
                if (gatt?.getService(BlueGATTConstants.UUIDs.PAIRING_SERVICE_UUID)?.getCharacteristic(BlueGATTConstants.UUIDs.CONNECTION_PARAMETERS_CHARACTERISTIC) != null) {
                    connectionParamManager?.subscribe()
                } else {
                    connectionState = LEConnectionState.CONNECTING_CONNECTIVITY
                    if (!connectivityWatcher?.subscribe()!!) {
                        closePebble()
                    }
                }
            } else if (connectionState == LEConnectionState.CONNECTING_GATT) {
                connect()
            }
        }

        override fun onMtuChanged(gatt: BluetoothGatt?, mtu: Int, status: Int) {
            if (gatt?.device?.address != targetPebble.address) return
            Timber.d("MTU Changed, new mtu ${mtu}")
            gattDriver?.setMTU(mtu)
        }

        override fun onDescriptorWrite(gatt: BluetoothGatt?, descriptor: BluetoothGattDescriptor?, status: Int) {
            if (gatt?.device?.address != targetPebble.address) return
            Timber.d("onDescriptorWrite ${descriptor?.uuid}")
            connectivityWatcher?.onDescriptorWrite(gatt, descriptor, status)
            connectionParamManager?.onDescriptorWrite(gatt, descriptor, status)
        }

        override fun onCharacteristicChanged(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?) {
            if (gatt?.device?.address != targetPebble.address) return
            Timber.d("onCharacteristicChanged ${characteristic?.uuid}")
            gattDriver?.onCharacteristicChanged(gatt, characteristic)
            connectivityWatcher?.onCharacteristicChanged(gatt, characteristic)
            connectionParamManager?.onCharacteristicChanged(gatt, characteristic)
        }

        override fun onCharacteristicWrite(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?, status: Int) {
            if (gatt?.device?.address != targetPebble.address) return
            Timber.d("onCharacteristicWrite ${characteristic?.uuid}")
            gattDriver?.onCharacteristicWrite(gatt, characteristic, status)
            connectionParamManager?.onCharacteristicWrite(gatt, characteristic, status)
        }
    }

    override fun startSingleWatchConnection(device: BluetoothDevice): Flow<SingleConnectionStatus> = flow {
        if (connectionState != LEConnectionState.IDLE) return@flow
        connectionState = LEConnectionState.CONNECTING
        if (Build.VERSION.SDK_INT >= 23) {
            gatt = targetPebble.connectGatt(context, false, gattCallbacks, BluetoothDevice.TRANSPORT_LE) // Fixes ConnectionState error status 133
        } else {
            gatt = targetPebble.connectGatt(context, false, gattCallbacks)
        }

        if (gatt == null) {
            Timber.e("connectGatt failed")
            return@flow
        } else {
            //gatt!!.discoverServices()
            if (connectionStatusChannel.receive()) {
                emit(SingleConnectionStatus.Connected(device))
            } else {
                return@flow
            }

            // Wait forever - eventually this needs to be changed to stop waiting when BLE
            // disconnects, but I don't want to mess with this code too much
            protocolHandler.startPacketSendingLoop(::sendPacket)
        }
    }

    private fun connect() {
        if (gattDriver?.connectPebble()!!) {
            connectionStatusChannel.offer(true)
            gatt?.requestMtu(339)
            connectionState = LEConnectionState.CONNECTED
        } else {
            closePebble()
        }
    }
}