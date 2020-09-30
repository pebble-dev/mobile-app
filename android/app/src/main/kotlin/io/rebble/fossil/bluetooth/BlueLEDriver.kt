package io.rebble.fossil.bluetooth

import android.bluetooth.*
import android.content.Context
import android.os.Build
import android.util.Log
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.*
import kotlin.coroutines.suspendCoroutine
import kotlin.experimental.or

class BlueLEDriver(private val targetPebble: BluetoothDevice, private val context: Context, private val packetCallback: suspend (ByteArray) -> Unit) : BlueIO {
    private val logTag = "BlueLE"
    private var connectivityWatcher: ConnectivityWatcher? = null
    private var gattClient: BlueGATTClient? = null

    init {
        if (targetPebble.type == BluetoothDevice.DEVICE_TYPE_CLASSIC || targetPebble.type == BluetoothDevice.DEVICE_TYPE_UNKNOWN) {
            throw IllegalArgumentException("Non-LE device should not use LE driver")
        }
    }

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
        if (gattClient == null) return false

        while (!gattClient!!.sendLock.isLocked && isConnected) {
            delay(500)
        }
        return if (isConnected) {
            gattClient!!.sendBytes(bytes)
            true
        } else {
            false
        }
    }

    fun closePebble() {
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
     * @param modelCompatThing Official app checks for some phone models / android versions and sets this if they match, probably for LE weirdness
     */
    private fun pairTriggerFlagsToBytes(supportsPinningWithoutSlaveSecurity: Boolean, belowLollipop: Boolean, modelCompatThing: Boolean): ByteArray {
        val bArr = ByteArray(2)
        bArr[1] = bArr[1] or 0b1
        if (supportsPinningWithoutSlaveSecurity) bArr[1] = bArr[1] or 0b10
        if (belowLollipop) bArr[1] = bArr[1] or 0b1000
        if (modelCompatThing) bArr[1] = bArr[1] or 0b10000

        return bArr
    }

    private val gattCallbacks = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            if (gatt?.device?.address != targetPebble.address) return
            if (status == BluetoothGatt.GATT_SUCCESS) {
                when (newState) {
                    BluetoothGatt.STATE_CONNECTED -> {
                        Log.i(logTag, "Pebble connected (initial)")
                        gatt?.discoverServices()
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
            if (connectionState == LEConnectionState.CONNECTING) {
                if (!connectivityWatcher?.subscribe()!!) closePebble()
            } else if (connectionState == LEConnectionState.CONNECTING_GATT) {
                gattClient = BlueGATTClient(gatt!!) {

                }
                if (!gattClient!!.connect()) {
                    closePebble()
                } else {
                    connectionState = LEConnectionState.CONNECTED
                }
            }
        }

        override fun onDescriptorWrite(gatt: BluetoothGatt?, descriptor: BluetoothGattDescriptor?, status: Int) {
            if (gatt?.device?.address != targetPebble.address) return
            connectivityWatcher?.onDescriptorWrite(gatt, descriptor, status)
        }

        override fun onCharacteristicChanged(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?) {
            if (gatt?.device?.address != targetPebble.address) return
            gattClient?.onCharacteristicChanged(gatt, characteristic)
            connectivityWatcher?.onCharacteristicChanged(gatt, characteristic)
        }

        override fun onCharacteristicWrite(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?, status: Int) {
            if (gatt?.device?.address != targetPebble.address) return
            gattClient?.onCharacteristicWrite(gatt, characteristic, status)
        }
    }

    override fun startSingleWatchConnection(device: BluetoothDevice): Flow<SingleConnectionStatus> = flow {
        if (connectionState != LEConnectionState.IDLE) return@flow
        connectionState = LEConnectionState.CONNECTING
        gatt = targetPebble.connectGatt(context, false, gattCallbacks)

        if (gatt == null) {
            Log.e(logTag, "connectGatt failed")
            return@flow
        } else {
            connectivityWatcher = ConnectivityWatcher(gatt!!) {
                if (!it.paired || targetPebble.bondState == BluetoothDevice.BOND_NONE) {
                    if (connectionState == LEConnectionState.PAIRING) {
                        if (it.pairingErrorCode == ConnectivityWatcher.PairingErrorCode.CONFIRM_VALUE_FAILED) {
                            Log.e(logTag, "Failed to pair")
                            closePebble()
                        }
                    } else if (connectionState != LEConnectionState.CONNECTED) {
                        connectionState = LEConnectionState.PAIRING
                        val pairService = gatt?.getService(BlueGATTConstants.UUIDs.PAIRING_SERVICE_UUID)
                        if (pairService == null) {
                            Log.e(logTag, "pairService is null")
                        } else {
                            val pairTrigger = pairService.getCharacteristic(BlueGATTConstants.UUIDs.PAIRING_TRIGGER_CHARACTERISTIC)
                            if (pairTrigger == null) {
                                Log.e(logTag, "pairTrigger is null")
                            } else {
                                if (pairTrigger.properties and BluetoothGattCharacteristic.PROPERTY_WRITE > 0) {
                                    pairTrigger.setValue(pairTriggerFlagsToBytes(it.supportsPinningWithoutSlaveSecurity, Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP, false))
                                    if (!gatt!!.writeCharacteristic(pairTrigger)) {
                                        Log.e(logTag, "Failed to write to pair characteristic")
                                        closePebble()
                                    }
                                }
                                if (!gatt?.device?.createBond()!!) {
                                    Log.e(logTag, "Failed to create bond")
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