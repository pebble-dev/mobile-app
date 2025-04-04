package io.rebble.cobble.bridges.ui

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
import io.rebble.cobble.BuildConfig
import io.rebble.cobble.FlutterMainActivity
import io.rebble.cobble.bluetooth.ConnectionLooper
import io.rebble.cobble.bridges.FlutterBridge
import io.rebble.cobble.pigeons.Pigeons
import io.rebble.cobble.shared.util.coroutines.asFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
class ConnectionUiFlutterBridge @Inject constructor(
        bridgeLifecycleController: BridgeLifecycleController,
        private val connectionLooper: ConnectionLooper,
        coroutineScope: CoroutineScope,
        private val activity: FlutterMainActivity
) : FlutterBridge, Pigeons.UiConnectionControl {
    private val pairCallbacks = bridgeLifecycleController
            .createCallbacks(Pigeons::PairCallbacks)

    private var lastSelectedDeviceAddress: String? = null

    init {
        bridgeLifecycleController.setupControl(Pigeons.UiConnectionControl::setup, this)

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

    override fun connectToWatch(arg: Pigeons.StringWrapper) {
        val address = arg.value!!
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

    @TargetApi(Build.VERSION_CODES.O)
    private fun associateWithCompanionDeviceManager(macAddress: String) {
        if (BuildConfig.DEBUG && !macAddress.contains(":")) {
            openConnectionToWatch(macAddress)
            return
        }

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
                .setDeviceProfile(AssociationRequest.DEVICE_PROFILE_WATCH)
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
    private fun processCompanionDeviceResult(resultCode: Int, data: Intent?) {
        if (resultCode != Activity.RESULT_OK || data == null) {
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

    override fun unpairWatch(arg: Pigeons.StringWrapper) {
        val bluetoothDevice = BluetoothAdapter.getDefaultAdapter()
                ?.getRemoteDevice(arg.value!!)
                ?: return


        // This is not officially supported. We use reflection and hope if it works.
        // If it doesn't, well, we tried.
        try {
            if (bluetoothDevice.bondState == BluetoothDevice.BOND_BONDED) {
                BluetoothDevice::class.java.getMethod("removeBond").invoke(bluetoothDevice)
            }
        } catch (e: ReflectiveOperationException) {
            Timber.e(e, "Unpair error")
        }
    }

    private fun openConnectionToWatch(macAddress: String) {
        pairCallbacks.onWatchPairComplete(Pigeons.StringWrapper.Builder().setValue(macAddress).build()) {}
        connectionLooper.connectToWatch(macAddress)
    }
}

private const val REQUEST_CODE_COMPANION_DEVICE_MANAGER = 1557