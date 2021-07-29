package io.rebble.cobble.bluetooth

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGattCharacteristic
import android.content.Context
import io.rebble.cobble.datasources.FlutterPreferences
import io.rebble.cobble.datasources.IncomingPacketsListener
import io.rebble.cobble.receivers.BluetoothBondReceiver
import io.rebble.cobble.util.toBytes
import io.rebble.cobble.util.toHexString
import io.rebble.libpebblecommon.ProtocolHandler
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import timber.log.Timber


class BlueLEDriver(
        private val context: Context,
        private val protocolHandler: ProtocolHandler,
        private val flutterPreferences: FlutterPreferences,
        private val incomingPacketsListener: IncomingPacketsListener
) : BlueIO {
    private var connectivityWatcher: ConnectivityWatcher? = null
    private var connectionParamManager: ConnectionParamManager? = null
    private var gattDriver: BlueGATTServer? = null
    lateinit var targetPebble: BluetoothDevice

    private val connectionStatusFlow = MutableStateFlow<Boolean?>(null)

    private var readLoopJob: Job? = null

    enum class LEConnectionState {
        IDLE,
        CONNECTING,
        CONNECTED,
        CLOSED
    }

    var connectionState = LEConnectionState.IDLE
    private var gatt: BlueGATTConnection? = null

    private suspend fun closePebble() {
        Timber.d("Driver shutting down")
        gattDriver?.closePebble()
        gatt?.disconnect()
        gatt?.close()
        gatt = null
        connectionState = LEConnectionState.CLOSED
        connectionStatusFlow.value = false
        readLoopJob?.cancel()
        protocolHandler.closeProtocol()
    }

    /**
     * @param supportsPinningWithoutSlaveSecurity ??
     * @param belowLollipop Used by official app to indicate a device below lollipop?
     * @param clientMode Forces phone-as-client mode
     */
    @Suppress("SameParameterValue")
    private fun pairTriggerFlagsToBytes(supportsPinningWithoutSlaveSecurity: Boolean, belowLollipop: Boolean, clientMode: Boolean): ByteArray {
        val boolArr = booleanArrayOf(true, supportsPinningWithoutSlaveSecurity, false, belowLollipop, clientMode, false)
        val byteArr = boolArr.toBytes()
        Timber.d("Pair trigger flags ${byteArr.toHexString()}")
        return byteArr
    }

    /**
     * Subscribes to connectivity and ensures watch is paired before initiating the connection
     */
    private suspend fun deviceConnectivity() {
        if (connectivityWatcher!!.subscribe()) {
            val status = connectivityWatcher!!.getStatus()
            if (status.connected) {
                if (status.paired && targetPebble.bondState == BluetoothDevice.BOND_BONDED) {
                    Timber.d("Paired, connecting gattDriver")
                    connect()
                } else {
                    Timber.d("Not yet paired, pairing...")
                    if (targetPebble.bondState == BluetoothDevice.BOND_BONDED) {
                        Timber.d("Phone already paired but watch not paired, removing bond and re-pairing")
                        targetPebble::class.java.getMethod("removeBond").invoke(targetPebble)
                    }
                    val pairService = gatt!!.getService(BlueGATTConstants.UUIDs.PAIRING_SERVICE_UUID)
                    if (pairService != null) {
                        val pairTrigger = pairService.getCharacteristic(BlueGATTConstants.UUIDs.PAIRING_TRIGGER_CHARACTERISTIC)
                        if (pairTrigger != null) {
                            val bondReceiver = BluetoothBondReceiver.registerBondReceiver(context, targetPebble.address)
                            if (pairTrigger.properties and BluetoothGattCharacteristic.PROPERTY_WRITE > 0) {
                                GlobalScope.launch(Dispatchers.Main.immediate) { gatt!!.writeCharacteristic(pairTrigger, pairTriggerFlagsToBytes(status.supportsPinningWithoutSlaveSecurity, belowLollipop = false, clientMode = false)) }
                            } else {
                                Timber.d("Pair characteristic can't be written, won't use")
                            }
                            targetPebble.createBond()
                            var bondResult = BluetoothDevice.BOND_NONE
                            try {
                                withTimeout(30000) {
                                    bondResult = bondReceiver.awaitBondResult()
                                }
                            } catch (e: TimeoutCancellationException) {
                                Timber.w("Timed out waiting for bond result")
                            } finally {
                                bondReceiver.unregister()
                            }
                            if (bondResult == BluetoothDevice.BOND_BONDED) {
                                Timber.d("Paired successfully, connecting gattDriver")
                                connect()
                                return
                            } else {
                                Timber.e("Failed to pair")
                            }
                        } else {
                            Timber.e("pairTrigger is null")
                        }
                    } else {
                        Timber.e("pairService is null")
                    }
                }
            }
        } else if (gattDriver?.connected == true) {
            Timber.d("Connectivity: device already connected")
            connect()
        } else {
            closePebble()
        }
    }

    @FlowPreview
    override fun startSingleWatchConnection(device: BluetoothDevice): Flow<SingleConnectionStatus> = flow {
        try {
            coroutineScope {
                if (device.type == BluetoothDevice.DEVICE_TYPE_CLASSIC || device.type == BluetoothDevice.DEVICE_TYPE_UNKNOWN) {
                    throw IllegalArgumentException("Non-LE device should not use LE driver")
                }

                if (connectionState == LEConnectionState.CONNECTED && device.address == this@BlueLEDriver.targetPebble.address) {
                    Timber.w("startSingleWatchConnection called on already connected driver")
                    emit(SingleConnectionStatus.Connected(device))
                } else if (connectionState != LEConnectionState.IDLE) { // If not in idle state this is a stale instance
                    Timber.e("Stale instance used for new connection")
                    return@coroutineScope
                } else {
                    emit(SingleConnectionStatus.Connecting(device))

                    protocolHandler.openProtocol()

                    this@BlueLEDriver.targetPebble = device

                    val server = BlueGATTServer(
                            device,
                            context,
                            this,
                            protocolHandler,
                            incomingPacketsListener
                    )
                    gattDriver = server

                    connectionState = LEConnectionState.CONNECTING
                    launch {
                        if (!server.initServer()) {
                            Timber.e("initServer failed")
                            connectionStatusFlow.value = false
                            return@launch
                        }
                        gatt = targetPebble.connectGatt(context, flutterPreferences)
                        if (gatt == null) {
                            Timber.e("connectGatt null")
                            connectionStatusFlow.value = false
                            return@launch
                        }

                        val mtu = gatt?.requestMtu(BlueGATTConstants.TARGET_MTU)
                        if (mtu?.isSuccess() == true) {
                            Timber.d("MTU Changed, new mtu ${mtu.mtu}")
                            gattDriver!!.setMTU(mtu.mtu)
                        }

                        Timber.i("Pebble connected (initial)")

                        launch {
                            while (true) {
                                gatt!!.characteristicChanged.collect {
                                    Timber.d("onCharacteristicChanged ${it.characteristic?.uuid}")
                                    connectivityWatcher?.onCharacteristicChanged(it.characteristic)
                                }
                            }
                        }

                        connectionParamManager = ConnectionParamManager(gatt!!)
                        connectivityWatcher = ConnectivityWatcher(gatt!!)
                        val servicesRes = gatt!!.discoverServices()
                        if (servicesRes != null && servicesRes.isSuccess()) {
                            if (gatt?.getService(BlueGATTConstants.UUIDs.PAIRING_SERVICE_UUID)?.getCharacteristic(BlueGATTConstants.UUIDs.CONNECTION_PARAMETERS_CHARACTERISTIC) != null) {
                                Timber.d("Subscribing to connparams")
                                if (connectionParamManager!!.subscribe() || gattDriver?.connected == true) {
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
                }

                if (connectionStatusFlow.first { it != null } == true) {
                    connectionState = LEConnectionState.CONNECTED
                    emit(SingleConnectionStatus.Connected(device))
                    packetReadLoop()
                } else {
                    Timber.e("connectionStatus was false")
                }

                cancel()
            }
        } finally {
            closePebble()
        }
    }

    @OptIn(FlowPreview::class)
    private suspend fun packetReadLoop() = coroutineScope {
        val job = launch {
            while (connectionStatusFlow.value == true) {
                val nextPacket = protocolHandler.waitForNextPacket()
                val driver = gattDriver ?: break

                driver.onNewPacketToSend(nextPacket)
            }
        }

        readLoopJob = job
    }

    private suspend fun connect() {
        Timber.d("Connect called")

        if (!gattDriver?.connectPebble()!!) {
            closePebble()
        } else {
            connectionStatusFlow.value = true
        }
    }
}