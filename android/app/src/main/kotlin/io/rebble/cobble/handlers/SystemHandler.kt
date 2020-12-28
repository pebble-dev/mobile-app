package io.rebble.cobble.handlers

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import io.rebble.cobble.bluetooth.ConnectionLooper
import io.rebble.cobble.bluetooth.ConnectionState
import io.rebble.cobble.datasources.WatchMetadataStore
import io.rebble.cobble.di.PerService
import io.rebble.cobble.util.coroutines.asFlow
import io.rebble.libpebblecommon.PacketPriority
import io.rebble.libpebblecommon.packets.TimeMessage
import io.rebble.libpebblecommon.services.SystemService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@OptIn(ExperimentalUnsignedTypes::class, ExperimentalStdlibApi::class)
@PerService
class SystemHandler @Inject constructor(
        private val context: Context,
        private val coroutineScope: CoroutineScope,
        private val systemService: SystemService,
        private val connectionLooper: ConnectionLooper,
        private val watchMetadataStore: WatchMetadataStore
) : CobbleHandler {
    init {
        listenForTimeChange()

        coroutineScope.launch {
            sendCurrentTime()
        }

        coroutineScope.launch {
            connectionLooper.connectionState.collect {
                if (it is ConnectionState.Connected) {
                    refreshWatchMetadata()
                } else {
                    watchMetadataStore.lastConnectedWatchMetadata.value = null
                    watchMetadataStore.lastConnectedWatchModel.value = null
                }
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
}