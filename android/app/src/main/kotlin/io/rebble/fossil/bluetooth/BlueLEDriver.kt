package io.rebble.fossil.bluetooth

import android.bluetooth.*
import android.content.Context
import android.os.Build
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import timber.log.Timber
import android.os.Handler
import android.os.Looper.getMainLooper
import android.util.Log
import android.widget.Toast
import io.rebble.fossil.util.toHexString
import kotlinx.coroutines.channels.Channel
import java.nio.ByteBuffer
import java.util.*
import kotlin.coroutines.suspendCoroutine
import kotlin.experimental.or


class BlueLEDriver(private val targetPebble: BluetoothDevice, private val context: Context, private val packetCallback: suspend (ByteArray) -> Unit) : BlueIO {
    private var connectivityWatcher: ConnectivityWatcher? = null
    private var connectionParamManager: ConnectionParamManager? = null
    private var gattDriver: BlueGATTIO? = null

    init {
        if (targetPebble.type == BluetoothDevice.DEVICE_TYPE_CLASSIC || targetPebble.type == BluetoothDevice.DEVICE_TYPE_UNKNOWN) {
            throw IllegalArgumentException("Non-LE device should not use LE driver")
        }
    }

    //TODO: Remove
    var mainHandler: Handler = Handler(getMainLooper())

    val isConnected: Boolean get() = connectionState == LEConnectionState.CONNECTED

    enum class LEConnectionState {
        IDLE,
        CONNECTING,
        PAIRING,
        CONNECTING_GATT,
        CONNECTED,
        CLOSED
    }

    var connectionState = LEConnectionState.IDLE
    private var gatt: BluetoothGatt? = null

    override suspend fun sendPacket(bytes: ByteArray): Boolean {
        gattDriver?.sendPacket(bytes) {
            if (!it) {
                closePebble()
            }
        }
        //TODO return properly based on success?
        return true
    }

    fun closePebble() {
        mainHandler.post {
            //TODO: Remove
            Toast.makeText(context, "Watch connection failed", Toast.LENGTH_LONG).show()
        }
        gatt?.close()
        gatt = null
        connectionState = LEConnectionState.CLOSED
    }

    fun getTarget(): BluetoothDevice? {
        return targetPebble
    }

    private fun resolveGattError(status: Int): String {
        val err = BluetoothGatt::class.java.declaredFields.find { p ->
            p.type.name == "int" &&
                    p.name.startsWith("GATT_") &&
                    p.getInt(null) == status
        }
        return err?.name?.replace("GATT", "")?.replace("_", "")?.toLowerCase(Locale.ROOT)?.capitalize()
                ?: "Unknown error"
    }

    /**
     * @param supportsPinningWithoutSlaveSecurity ??
     * @param belowLollipop Used by official app to indicate a device below lollipop?
     * @param clientMode Forces phone-as-client mode
     */
    private fun pairTriggerFlagsToBytes(supportsPinningWithoutSlaveSecurity: Boolean, belowLollipop: Boolean, clientMode: Boolean): ByteArray {
        val bArr = ByteArray(2)
        bArr[1] = bArr[1] or 0b1
        if (supportsPinningWithoutSlaveSecurity) bArr[1] = bArr[1] or 0b10
        if (belowLollipop) bArr[1] = bArr[1] or 0b1000
        if (clientMode) bArr[1] = bArr[1] or 0b10000
        Timber.d("Pair trigger flags ${bArr.toHexString()}")
        return bArr
    }

    private val gattCallbacks = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            if (gatt?.device?.address != targetPebble.address) return
            if (status == BluetoothGatt.GATT_SUCCESS) {
                when (newState) {
                    BluetoothGatt.STATE_CONNECTED -> {
                        Timber.i("Pebble connected (initial)")
                        gattDriver = BlueGATTClient(gatt!!)

                        connectionParamManager = ConnectionParamManager(gatt) {
                            connectionState = LEConnectionState.CONNECTING_GATT
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
                            } else if (connectionState == LEConnectionState.CONNECTING_GATT) {
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
                                                pairTrigger.setValue(pairTriggerFlagsToBytes(it.supportsPinningWithoutSlaveSecurity, Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP, true))
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
                Timber.e("Connection error: ${resolveGattError(status)}")
            }
        }
        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            if (gatt?.device?.address != targetPebble.address) return
            Timber.d("Services discovered")
            if (connectionState == LEConnectionState.CONNECTING) {
                if (gatt?.getService(BlueGATTConstants.UUIDs.PAIRING_SERVICE_UUID)?.getCharacteristic(BlueGATTConstants.UUIDs.CONNECTION_PARAMETERS_CHARACTERISTIC) != null) {
                    connectionParamManager?.subscribe()
                } else {
                    connectionState = LEConnectionState.CONNECTING_GATT
                    if (!connectivityWatcher?.subscribe()!!) {
                        closePebble()
                    }
                }
            } else if (connectionState == LEConnectionState.CONNECTING_GATT) {
                if (gattDriver?.connectPebble()!!) {
                    mainHandler.post {
                        //TODO: connection success callback etc.
                        Toast.makeText(context, "Watch connection success", Toast.LENGTH_LONG).show()
                    }
                    gatt?.requestMtu(339)
                    connectionState = LEConnectionState.CONNECTED
                } else {
                    closePebble()
                }
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
        }

        override fun onCharacteristicChanged(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?) {
            if (gatt?.device?.address != targetPebble.address) return
            Timber.d("onCharacteristicChanged ${characteristic?.uuid}")
            gattDriver?.onCharacteristicChanged(gatt, characteristic)
            connectivityWatcher?.onCharacteristicChanged(gatt, characteristic)
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
        gatt = targetPebble.connectGatt(context, false, gattCallbacks)

        if (gatt == null) {
            Timber.e("connectGatt failed")
            return@flow
        } else {
            connectivityWatcher = ConnectivityWatcher(gatt!!) {
                if (!it.paired || targetPebble.bondState == BluetoothDevice.BOND_NONE) {
                    if (connectionState == LEConnectionState.PAIRING) {
                        if (it.pairingErrorCode == ConnectivityWatcher.PairingErrorCode.CONFIRM_VALUE_FAILED) {
                            Timber.e("Failed to pair")
                            closePebble()
                        }
                    } else if (connectionState != LEConnectionState.CONNECTED) {
                        connectionState = LEConnectionState.PAIRING
                        val pairService = gatt?.getService(BlueGATTConstants.UUIDs.PAIRING_SERVICE_UUID)
                        if (pairService == null) {
                            Timber.e("pairService is null")
                        } else {
                            val pairTrigger = pairService.getCharacteristic(BlueGATTConstants.UUIDs.PAIRING_TRIGGER_CHARACTERISTIC)
                            if (pairTrigger == null) {
                                Timber.e("pairTrigger is null")
                            } else {
                                if (pairTrigger.properties and BluetoothGattCharacteristic.PROPERTY_WRITE > 0) {
                                    pairTrigger.setValue(pairTriggerFlagsToBytes(it.supportsPinningWithoutSlaveSecurity, Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP, false))
                                    if (!gatt!!.writeCharacteristic(pairTrigger)) {
                                        Timber.e("Failed to write to pair characteristic")
                                        closePebble()
                                    }
                                }
                                if (!gatt?.device?.createBond()!!) {
                                    Timber.e("Failed to create bond")
                                    closePebble()
                                } else {
                                    connectionState = LEConnectionState.CONNECTING_GATT
                                    gatt!!.discoverServices()
                                }
                            }
                        }
                    }
                }
            }
            gatt!!.discoverServices()
            emit(SingleConnectionStatus.Connected(device))

            // Wait forever - eventually this needs to be changed to stop waiting when BLE
            // disconnects, but I don't want to mess with this code too much
            suspendCoroutine { }
        }
    }
}