package io.rebble.cobble.service

import android.companion.AssociationInfo
import android.companion.CompanionDeviceService
import android.os.Build
import androidx.annotation.RequiresApi
import io.rebble.cobble.CobbleApplication
import io.rebble.cobble.bluetooth.ConnectionLooper
import io.rebble.cobble.bluetooth.ConnectionState
import io.rebble.cobble.bluetooth.watchOrNull
import timber.log.Timber

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
class CompanionDeviceService : CompanionDeviceService() {
    lateinit var connectionLooper: ConnectionLooper
    override fun onCreate() {
        val injectionComponent = (applicationContext as CobbleApplication).component
        connectionLooper = injectionComponent.createConnectionLooper()
        super.onCreate()
    }

    override fun onDeviceAppeared(associationInfo: AssociationInfo) {
        Timber.d("Device appeared: $associationInfo")
        if (connectionLooper.connectionState.value is ConnectionState.WaitingForReconnect) {
            associationInfo.deviceMacAddress?.toString()?.uppercase()?.let {
                connectionLooper.signalWatchPresence(it)
            }
        } else {
            Timber.i("Ignoring device appeared event (${connectionLooper.connectionState.value})")
        }
    }

    override fun onDeviceDisappeared(associationInfo: AssociationInfo) {
        Timber.d("Device disappeared: $associationInfo")
        if (connectionLooper.connectionState.value !is ConnectionState.Disconnected &&
                connectionLooper.connectionState.value.watchOrNull?.address == associationInfo.deviceMacAddress?.toString()?.uppercase()) {
            connectionLooper.signalWatchAbsence()
        } else {
            Timber.i("Ignoring device disappeared event (${associationInfo.deviceMacAddress?.toString()?.uppercase()}, ${connectionLooper.connectionState.value})")
        }
    }
}