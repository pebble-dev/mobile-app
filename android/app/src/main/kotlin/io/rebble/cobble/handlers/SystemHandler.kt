package io.rebble.cobble.handlers

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.Sensor
import android.hardware.SensorManager
import android.location.LocationManager
import androidx.core.content.ContextCompat.getSystemService
import io.rebble.cobble.bluetooth.ConnectionLooper
import io.rebble.cobble.datasources.WatchMetadataStore
import io.rebble.cobble.shared.domain.state.ConnectionState
import io.rebble.cobble.shared.domain.state.ConnectionStateManager
import io.rebble.cobble.shared.domain.state.watchOrNull
import io.rebble.cobble.util.coroutines.asFlow
import io.rebble.libpebblecommon.PacketPriority
import io.rebble.libpebblecommon.packets.PhoneAppVersion
import io.rebble.libpebblecommon.packets.ProtocolCapsFlag
import io.rebble.libpebblecommon.packets.TimeMessage
import io.rebble.libpebblecommon.services.SystemService
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.merge
import timber.log.Timber
import java.util.TimeZone
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

        negotiate()
    }

    fun negotiate() {
        coroutineScope.launch {
            connectionLooper.connectionState.first { it is ConnectionState.Negotiating }
            Timber.i("Negotiating with watch")
            try {
                refreshWatchMetadata()
                watchMetadataStore.lastConnectedWatchMetadata.value?.let {
                    if (it.running.isRecovery.get()) {
                        Timber.i("Watch is in recovery mode, switching to recovery state")
                        connectionLooper.connectionState.value.watchOrNull?.let { it1 -> connectionLooper.recoveryMode(it1) }
                    } else {
                        connectionLooper.connectionState.value.watchOrNull?.let { it1 -> connectionLooper.negotiationsComplete(it1) }
                    }
                }
                awaitCancellation()
            } finally {
                watchMetadataStore.lastConnectedWatchMetadata.value = null
                watchMetadataStore.lastConnectedWatchModel.value = null
            }
        }
    }

    private suspend fun refreshWatchMetadata() {
        var retries = 0
        while (retries < 3) {
            try {
                withTimeout(3000) {
                    val watchInfo = systemService.requestWatchVersion()
                    //FIXME: Possible race condition here
                    ConnectionStateManager.connectionState.value.watchOrNull?.metadata?.value = watchInfo
                    watchMetadataStore.lastConnectedWatchMetadata.value = watchInfo
                    val watchModel = systemService.requestWatchModel()
                    watchMetadataStore.lastConnectedWatchModel.value = watchModel
                }
                break
            } catch (e: TimeoutCancellationException) {
                Timber.e(e, "Failed to get watch metadata, retrying")
                retries++
            } catch (e: Exception) {
                Timber.e(e, "Failed to get watch metadata")
                break
            }
        }
        if (retries >= 3) {
            Timber.e("Failed to get watch metadata after 3 retries, giving up and reconnecting")
            connectionLooper.tryReconnect()
        }
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
        platflormFlags.add(PhoneAppVersion.PlatformFlag.Telephony)

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
                                ProtocolCapsFlag.SupportsTwoWayDismissal,
                                ProtocolCapsFlag.SupportsAppRunStateProtocol
                        )
                )

        )
    }
}