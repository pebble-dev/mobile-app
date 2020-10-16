package io.rebble.fossil.bluetooth

import android.bluetooth.*
import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper.getMainLooper
import android.util.Log
import android.widget.Toast
import io.rebble.fossil.util.toHexString
import kotlinx.coroutines.channels.Channel
import java.nio.ByteBuffer
import java.util.*
import kotlin.experimental.or


class BlueLEDriver(private val targetPebble: BluetoothDevice, private val context: Context, private val packetCallback: suspend (ByteArray) -> Unit) : BlueIO {
    private val logTag = "BlueLE"
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

    override val isConnected: Boolean get() = connectionState == LEConnectionState.CONNECTED

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

    private val bondChannel = Channel<Boolean>()

    override suspend fun sendPacket(bytes: ByteArray) {
        gattDriver?.sendPacket(bytes) {
            if (!it) {
                closePebble()
            }
        }
    }

    override fun readStream(buffer: ByteBuffer, offset: Int, count: Int): Int {
        return -1
    }

    override fun closePebble() {
        mainHandler.post {
            //TODO: Remove
            Toast.makeText(context, "Watch connection failed", Toast.LENGTH_LONG).show()
        }
        gatt?.close()
        gatt = null
        connectionState = LEConnectionState.CLOSED
    }

    override fun getTarget(): BluetoothDevice? {
        return targetPebble
    }

    override fun setOnConnectionChange(f: (Boolean) -> Unit) {

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
        Log.d(logTag, "Pair trigger flags ${bArr.toHexString()}")
        return bArr
    }

    private val gattCallbacks = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            if (gatt?.device?.address != targetPebble.address) return
            if (status == BluetoothGatt.GATT_SUCCESS) {
                when (newState) {
                    BluetoothGatt.STATE_CONNECTED -> {
                        Log.i(logTag, "Pebble connected (initial)")
                        gattDriver = BlueGATTClient(gatt!!)

                        connectionParamManager = ConnectionParamManager(gatt) {
                            connectionState = LEConnectionState.CONNECTING_GATT
                            if (!connectivityWatcher?.subscribe()!!) {
                                closePebble()
                            }
                        }

                        connectivityWatcher = ConnectivityWatcher(gatt) {
                            Log.d(logTag, "Connectivity status changed: ${it}")
                            if (connectionState == LEConnectionState.PAIRING) {
                                if (it.pairingErrorCode == ConnectivityWatcher.PairingErrorCode.CONFIRM_VALUE_FAILED) {
                                    Log.e(logTag, "Failed to pair")
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
                                        Log.w(logTag, "Watch bonded, phone not bonded")
                                    } else {
                                        connectionState = LEConnectionState.CONNECTING_GATT
                                        gatt.discoverServices()
                                    }
                                } else {
                                    connectionState = LEConnectionState.PAIRING
                                    if (targetPebble.bondState == BluetoothDevice.BOND_BONDED) {
                                        Log.w(logTag, "Phone bonded, watch not bonded")
                                    }
                                    val pairService = gatt.getService(BlueGATTConstants.UUIDs.PAIRING_SERVICE_UUID)
                                    if (pairService == null) {
                                        Log.e(logTag, "pairService is null")
                                    } else {
                                        val pairTrigger = pairService.getCharacteristic(BlueGATTConstants.UUIDs.PAIRING_TRIGGER_CHARACTERISTIC)
                                        if (pairTrigger == null) {
                                            Log.e(logTag, "pairTrigger is null")
                                        } else {
                                            Log.d(logTag, "Pairing device")
                                            if (pairTrigger.properties and BluetoothGattCharacteristic.PROPERTY_WRITE > 0) {
                                                pairTrigger.setValue(pairTriggerFlagsToBytes(it.supportsPinningWithoutSlaveSecurity, Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP, true))
                                                if (!gatt.writeCharacteristic(pairTrigger)) {
                                                    Log.e(logTag, "Failed to write to pair characteristic")
                                                    closePebble()
                                                }
                                            }
                                            if (!gatt.device?.createBond()!!) {
                                                Log.e(logTag, "Failed to create bond")
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
                        Log.i(logTag, "Pebble disconnected")
                        connectionState = LEConnectionState.IDLE
                    }
                }
            } else {
                Log.e(logTag, "Connection error: ${resolveGattError(status)}")
            }
        }
        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            if (gatt?.device?.address != targetPebble.address) return
            Log.d(logTag, "Services discovered")
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
            Log.d(logTag, "MTU Changed, new mtu ${mtu}")
            gattDriver?.setMTU(mtu)
        }

        override fun onDescriptorWrite(gatt: BluetoothGatt?, descriptor: BluetoothGattDescriptor?, status: Int) {
            if (gatt?.device?.address != targetPebble.address) return
            Log.d(logTag, "onDescriptorWrite ${descriptor?.uuid}")
            connectivityWatcher?.onDescriptorWrite(gatt, descriptor, status)
        }

        override fun onCharacteristicChanged(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?) {
            if (gatt?.device?.address != targetPebble.address) return
            Log.d(logTag, "onCharacteristicChanged ${characteristic?.uuid}")
            gattDriver?.onCharacteristicChanged(gatt, characteristic)
            connectivityWatcher?.onCharacteristicChanged(gatt, characteristic)
        }

        override fun onCharacteristicWrite(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?, status: Int) {
            if (gatt?.device?.address != targetPebble.address) return
            Log.d(logTag, "onCharacteristicWrite ${characteristic?.uuid}")
            gattDriver?.onCharacteristicWrite(gatt, characteristic, status)
            connectionParamManager?.onCharacteristicWrite(gatt, characteristic, status)
        }
    }

    override fun connectPebble(): Boolean {
        if (connectionState != LEConnectionState.IDLE) return false
        connectionState = LEConnectionState.CONNECTING
        gatt = targetPebble.connectGatt(context, false, gattCallbacks)

        if (gatt == null) {
            Log.e(logTag, "connectGatt failed")
            return false
        } else {
            return true
        }
    }
}