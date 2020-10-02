package io.rebble.fossil.bridges

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
import android.content.IntentSender
import android.os.Build
import io.flutter.Log
import io.rebble.fossil.MainActivity
import io.rebble.fossil.bluetooth.BlueCommon
import io.rebble.fossil.bluetooth.ConnectionLooper
import io.rebble.fossil.bluetooth.ConnectionState
import io.rebble.fossil.pigeons.BooleanWrapper
import io.rebble.fossil.pigeons.Pigeons
import io.rebble.fossil.util.macAddressToString
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
class Connection @Inject constructor(
        bridgeLifecycleController: BridgeLifecycleController,
        private val connectionLooper: ConnectionLooper,
        private val blueCommon: BlueCommon,
        private val coroutineScope: CoroutineScope,
        private val activity: MainActivity
) : FlutterBridge, Pigeons.ConnectionControl {
    init {
        bridgeLifecycleController.setupControl(Pigeons.ConnectionControl::setup, this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            activity.activityResultCallbacks[REQUEST_CODE_COMPANION_DEVICE_MANAGER] =
                    this::processCompanionDeviceResult
        }

    }

    override fun isConnected(): Pigeons.BooleanWrapper {
        return BooleanWrapper(connectionLooper.connectionState.value is ConnectionState.Connected)
    }

    override fun connectToWatch(arg: Pigeons.NumberWrapper) {
        try {
            val address = arg.value.macAddressToString()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                associateWithCompanionDeviceManager(address)
            } else {
                openConnectionToWatch(address)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
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
                Log.e("Connection", "Device association failure")
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
        connectionLooper.connectToWatch(macAddress)
    }

    @Suppress("UNCHECKED_CAST")
    override fun sendRawPacket(arg: Pigeons.ListWrapper) {
        coroutineScope.launch {
            val byteArray = (arg.value as List<Number>).map { it.toByte() }.toByteArray()
            blueCommon.sendPacket(byteArray)
        }
    }
}

private const val REQUEST_CODE_COMPANION_DEVICE_MANAGER = 1557