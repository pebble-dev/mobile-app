package io.rebble.cobble.handlers

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
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
        private val systemService: SystemService
) : PebbleMessageHandler {
    init {
        listenForTimeChange()

        coroutineScope.launch {
            sendCurrentTime()
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
}