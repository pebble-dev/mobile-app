package io.rebble.cobble.bluetooth.ble

import android.content.Context
import androidx.annotation.RequiresPermission
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class GattServerManager(
        private val context: Context,
        private val ioDispatcher: CoroutineContext = Dispatchers.IO
) {
    private val _gattServer: MutableStateFlow<NordicGattServer?> = MutableStateFlow(null)
    val gattServer = _gattServer.asStateFlow().filterNotNull()

    @RequiresPermission(android.Manifest.permission.BLUETOOTH_CONNECT)
    fun initIfNeeded(): NordicGattServer {
        val gattServer = _gattServer.value
        if (gattServer?.isOpened != true) {
            gattServer?.close()
            _gattServer.value = NordicGattServer(
                    ioDispatcher = ioDispatcher,
                    context = context
            ).also {
                CoroutineScope(ioDispatcher).launch {
                    it.open()
                }
            }
        }
        return _gattServer.value!!
    }

    fun close() {
        _gattServer.value?.close()
        _gattServer.value = null
    }

}