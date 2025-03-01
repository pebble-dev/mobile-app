package io.rebble.cobble.shared.handlers

import android.content.Intent
import android.content.IntentFilter
import android.hardware.Sensor
import android.hardware.SensorManager
import android.location.LocationManager
import androidx.core.content.ContextCompat.getSystemService
import io.rebble.cobble.shared.AndroidPlatformContext
import io.rebble.cobble.shared.PlatformContext
import io.rebble.cobble.shared.util.coroutines.asFlow
import io.rebble.libpebblecommon.packets.PhoneAppVersion
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge

actual fun platformTimeChangedFlow(context: PlatformContext): Flow<Unit> {
    context as AndroidPlatformContext
    val timeChangeFlow = IntentFilter(Intent.ACTION_TIME_CHANGED).asFlow(context.applicationContext)
    val timezoneChangeFlow =
        IntentFilter(
            Intent.ACTION_TIMEZONE_CHANGED
        ).asFlow(context.applicationContext)

    return merge(timeChangeFlow, timezoneChangeFlow).map { }
}

actual fun getPlatformPebbleFlags(context: PlatformContext): Set<PhoneAppVersion.PlatformFlag> {
    context as AndroidPlatformContext
    val sensorManager = getSystemService(context.applicationContext, SensorManager::class.java)
    val locationManager = getSystemService(context.applicationContext, LocationManager::class.java)

    return buildSet {
        add(PhoneAppVersion.PlatformFlag.BTLE)
        // TODO: Check for SMS
        add(PhoneAppVersion.PlatformFlag.Telephony)

        if (!sensorManager?.getSensorList(
                Sensor.TYPE_ACCELEROMETER
            ).isNullOrEmpty()
        ) {
            add(PhoneAppVersion.PlatformFlag.Accelerometer)
        }
        if (!sensorManager?.getSensorList(
                Sensor.TYPE_GYROSCOPE
            ).isNullOrEmpty()
        ) {
            add(PhoneAppVersion.PlatformFlag.Gyroscope)
        }
        if (!sensorManager?.getSensorList(
                Sensor.TYPE_MAGNETIC_FIELD
            ).isNullOrEmpty()
        ) {
            add(PhoneAppVersion.PlatformFlag.Compass)
        }
        if (
            locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER) == true ||
            locationManager?.isProviderEnabled(LocationManager.NETWORK_PROVIDER) == true
        ) {
            add(PhoneAppVersion.PlatformFlag.GPS)
        }
    }
}