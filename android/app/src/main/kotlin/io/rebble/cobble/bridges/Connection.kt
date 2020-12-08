package io.rebble.cobble.bridges

import android.annotation.TargetApi
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.companion.AssociationRequest
import android.companion.BluetoothDeviceFilter
import android.companion.BluetoothLeDeviceFilter
import android.companion.CompanionDeviceManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.IntentSender
import android.os.Build
import io.rebble.cobble.MainActivity
import io.rebble.cobble.bluetooth.BlueCommon
import io.rebble.cobble.bluetooth.ConnectionLooper
import io.rebble.cobble.bluetooth.ConnectionState
import io.rebble.cobble.bluetooth.watchOrNull
import io.rebble.cobble.data.toPigeon
import io.rebble.cobble.datasources.WatchMetadataStore
import io.rebble.cobble.pigeons.BooleanWrapper
import io.rebble.cobble.pigeons.NumberWrapper
import io.rebble.cobble.pigeons.Pigeons
import io.rebble.cobble.util.coroutines.asFlow
import io.rebble.cobble.util.macAddressToLong
import io.rebble.cobble.util.macAddressToString
import io.rebble.libpebblecommon.ProtocolHandler
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import timber.log.Timber
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
class Connection @Inject constructor(
        bridgeLifecycleController: BridgeLifecycleController,
        private val connectionLooper: ConnectionLooper,
        private val blueCommon: BlueCommon,
        private val coroutineScope: CoroutineScope,
        private val activity: MainActivity,
        private val protocolHandler: ProtocolHandler,
        private val watchMetadataStore: WatchMetadataStore
) : FlutterBridge, Pigeons.ConnectionControl {
    private val connectionCallbacks = bridgeLifecycleController
            .createCallbacks(Pigeons::ConnectionCallbacks)
    private val pairCallbacks = bridgeLifecycleController
            .createCallbacks(Pigeons::PairCallbacks)

    private var lastSelectedDeviceAddress: String? = null

    private var statusObservingJob: Job? = null

    init {
        bridgeLifecycleController.setupControl(Pigeons.ConnectionControl::setup, this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            activity.activityResultCallbacks[REQUEST_CODE_COMPANION_DEVICE_MANAGER] =
                    this::processCompanionDeviceResult
        }

        coroutineScope.launch {
            IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED).asFlow(activity)
                    .collect { intent ->
                        val device = intent
                                .getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                                ?: return@collect
                        val newBondState = intent.getIntExtra(
                                BluetoothDevice.EXTRA_BOND_STATE,
                                BluetoothDevice.BOND_NONE
                        )

                        if (device.address == lastSelectedDeviceAddress &&
                                newBondState == BluetoothDevice.BOND_BONDED) {
                            openConnectionToWatch(device.address)
                        }
                    }
        }
    }

    override fun isConnected(): Pigeons.BooleanWrapper {
        return BooleanWrapper(connectionLooper.connectionState.value is ConnectionState.Connected)
    }

    override fun connectToWatch(arg: Pigeons.NumberWrapper) {
        val address = arg.value.macAddressToString()
        lastSelectedDeviceAddress = address

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            associateWithCompanionDeviceManager(address)
        } else {
            val bluetoothDevice = BluetoothAdapter.getDefaultAdapter()?.getRemoteDevice(address)
                    ?: return

            if (bluetoothDevice.type == BluetoothDevice.DEVICE_TYPE_CLASSIC &&
                    bluetoothDevice.bondState != BluetoothDevice.BOND_BONDED) {
                bluetoothDevice.createBond()
            } else {
                openConnectionToWatch(address)
            }
        }
    }

    override fun disconnect() {
        connectionLooper.closeConnection()
    }

    @TargetApi(Build.VERSION_CODES.O)
    private fun associateWithCompanionDeviceManager(macAddress: String) {
        val companionDeviceManager =
                activity.getSystemService(Context.COMPANION_DEVICE_SERVICE) as CompanionDeviceManager

        val existingBoundDevices = companionDeviceManager.associations
        if (existingBoundDevices.contains(macAddress)) {
            openConnectionToWatch(macAddress)
            return
        }

        val deviceType = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(macAddress).type
        val filter = if (deviceType == BluetoothDevice.DEVICE_TYPE_LE) {
            BluetoothLeDeviceFilter.Builder()
                    .setScanFilter(ScanFilter.Builder().setDeviceAddress(macAddress).build())
                    .build()
        } else {
            BluetoothDeviceFilter.Builder()
                    .setAddress(macAddress)
                    .build()
        }


        val associationRequest = AssociationRequest.Builder()
                .addDeviceFilter(filter)
                .setSingleDevice(true)
                .build()

        companionDeviceManager.associate(associationRequest, object : CompanionDeviceManager.Callback() {
            override fun onDeviceFound(chooserLauncher: IntentSender) {
                activity.startIntentSenderForResult(
                        chooserLauncher,
                        REQUEST_CODE_COMPANION_DEVICE_MANAGER,
                        null,
                        0,
                        0,
                        0
                )
            }

            override fun onFailure(error: CharSequence?) {
                Timber.e("Device association failure")
            }
        }, null)
    }

    @TargetApi(Build.VERSION_CODES.O)
    private fun processCompanionDeviceResult(resultCode: Int, data: Intent) {
        if (resultCode != Activity.RESULT_OK) {
            return
        }

        val deviceToPair: Any =
                data.getParcelableExtra(CompanionDeviceManager.EXTRA_DEVICE)!!

        val address = when (deviceToPair) {
            is BluetoothDevice -> {
                if (deviceToPair.bondState != BluetoothDevice.BOND_BONDED) {
                    deviceToPair.createBond()
                    return
                }
                deviceToPair.address
            }
            is ScanResult -> {
                deviceToPair.device.address
            }
            else -> {
                throw IllegalStateException("Unknown device type: $deviceToPair")
            }
        }

        openConnectionToWatch(address)
    }

    private fun openConnectionToWatch(macAddress: String) {
        pairCallbacks.onWatchPairComplete(NumberWrapper(macAddress.macAddressToLong())) {}
        connectionLooper.connectToWatch(macAddress)
    }

    @Suppress("UNCHECKED_CAST")
    override fun sendRawPacket(arg: Pigeons.ListWrapper) {
        coroutineScope.launch {
            val byteArray = (arg.value as List<Number>).map { it.toByte().toUByte() }.toUByteArray()
            protocolHandler.send(byteArray)
        }
    }

    override fun observeConnectionChanges() {
        statusObservingJob = coroutineScope.launch(Dispatchers.Main) {
            combine(
                    connectionLooper.connectionState,
                    watchMetadataStore.lastConnectedWatchMetadata,
                    watchMetadataStore.lastConnectedWatchModel
            ) { connectionState, watchMetadata, model ->
                Pigeons.WatchConnectionStatePigeon().apply {
                    isConnected = connectionState is ConnectionState.Connected
                    isConnecting = connectionState is ConnectionState.Connecting
                    val bluetoothDevice = connectionState.watchOrNull
                    currentWatchAddress = bluetoothDevice?.address?.macAddressToLong()
                    currentConnectedWatch = watchMetadata?.toPigeon(bluetoothDevice, model)
                }
            }.collect {
                connectionCallbacks.onWatchConnectionStateChanged(
                        it
                ) {}
            }
        }
    }

    override fun cancelObservingConnectionChanges() {
        statusObservingJob?.cancel()
    }
}

private const val REQUEST_CODE_COMPANION_DEVICE_MANAGER = 1557