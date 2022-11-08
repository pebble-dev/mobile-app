package io.rebble.cobble.handlers

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.Sensor
import android.hardware.SensorManager
import android.location.LocationManager
import androidx.core.content.ContextCompat.getSystemService
import io.rebble.cobble.bluetooth.ConnectionLooper
import io.rebble.cobble.bluetooth.ConnectionState
import io.rebble.cobble.datasources.WatchMetadataStore
import io.rebble.cobble.util.coroutines.asFlow
import io.rebble.libpebblecommon.PacketPriority
import io.rebble.libpebblecommon.packets.PhoneAppVersion
import io.rebble.libpebblecommon.packets.ProtocolCapsFlag
import io.rebble.libpebblecommon.packets.TimeMessage
import io.rebble.libpebblecommon.services.SystemService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@OptIn(ExperimentalUnsignedTypes::class, ExperimentalStdlibApi::class)
class SystemHandler @Inject constructor(
        private val context: Context,
        private val coroutineScope: CoroutineScope,
        private val systemService: SystemService,
        private val connectionLooper: ConnectionLooper,
        private val watchMetadataStore: WatchMetadataStore
) : CobbleHandler {
    init {
        systemService.appVersionRequestHandler = this::handleAppVersionRequest
        listenForTimeChange()

        coroutineScope.launch {
            // Wait until watch is connected before sending time
            connectionLooper.connectionState.first { it is ConnectionState.Connected }

            sendCurrentTime()
        }

        coroutineScope.launch {
            try {
                refreshWatchMetadata()
                awaitCancellation()
            } finally {
                watchMetadataStore.lastConnectedWatchMetadata.value = null
                watchMetadataStore.lastConnectedWatchModel.value = null
            }
        }
    }

    private suspend fun refreshWatchMetadata() {
        val watchInfo = systemService.requestWatchVersion()
        watchMetadataStore.lastConnectedWatchMetadata.value = watchInfo

        val watchModel = systemService.requestWatchModel()
        watchMetadataStore.lastConnectedWatchModel.value = watchModel
    }


    @OptIn(ExperimentalCoroutinesApi::class)
    private fun listenForTimeChange() {
        val timeChangeFlow = IntentFilter(Intent.ACTION_TIME_CHANGED).asFlow(context)
        val timezoneChangeFlow = IntentFilter(Intent.ACTION_TIMEZONE_CHANGED).asFlow(context)

        val mergedFlow = merge(timeChangeFlow, timezoneChangeFlow)

        coroutineScope.launch {
            mergedFlow.collect {
                sendCurrentTime()
            }
        }
    }

    private suspend fun sendCurrentTime() {
        val timezone = TimeZone.getDefault()
        val now = System.currentTimeMillis()

        val updateTimePacket = TimeMessage.SetUTC(
                (now / 1000).toUInt(),
                timezone.getOffset(now).toShort(),
                timezone.id
        )

        systemService.send(updateTimePacket, PacketPriority.LOW)
    }

    private suspend fun handleAppVersionRequest(): PhoneAppVersion.AppVersionResponse {
        val sensorManager = getSystemService(context, SensorManager::class.java)
        val platflormFlags = mutableListOf(PhoneAppVersion.PlatformFlag.BTLE)
        if (!sensorManager?.getSensorList(Sensor.TYPE_ACCELEROMETER).isNullOrEmpty()) platflormFlags.add(PhoneAppVersion.PlatformFlag.Accelerometer)
        if (!sensorManager?.getSensorList(Sensor.TYPE_GYROSCOPE).isNullOrEmpty()) platflormFlags.add(PhoneAppVersion.PlatformFlag.Gyroscope)
        if (!sensorManager?.getSensorList(Sensor.TYPE_MAGNETIC_FIELD).isNullOrEmpty()) platflormFlags.add(PhoneAppVersion.PlatformFlag.Compass)

        val locationManager = getSystemService(context, LocationManager::class.java)
        if (locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER) == true || locationManager?.isProviderEnabled(LocationManager.NETWORK_PROVIDER) == true) platflormFlags.add(PhoneAppVersion.PlatformFlag.GPS)
        //TODO: check phone and sms capabilities

        return PhoneAppVersion.AppVersionResponse(
                UInt.MAX_VALUE,
                0u,
                PhoneAppVersion.PlatformFlag.makeFlags(
                        PhoneAppVersion.OSType.Android,
                        platflormFlags
                ),
                2u,
                4u,
                4u,
                2u,
                ProtocolCapsFlag.makeFlags(
                        listOf(
                                ProtocolCapsFlag.Supports8kAppMessage,
                                ProtocolCapsFlag.SupportsExtendedMusicProtocol,
                                ProtocolCapsFlag.SupportsAppRunStateProtocol
                        )
                )

        )
    }
}