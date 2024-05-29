package io.rebble.cobble.bluetooth.ble

import android.bluetooth.BluetoothManager
import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import timber.log.Timber

object GattServerManager {
    private var gattServer: GattServer? = null
    private var gattServerJob: Job? = null
    private var _ppogService: PPoGService? = null
    val ppogService: PPoGService?
        get() = _ppogService

    fun getGattServer(): GattServer? {
        return gattServer
    }

    fun initIfNeeded(context: Context, scope: CoroutineScope): GattServer {
        if (gattServer?.isOpened() != true || gattServerJob?.isActive != true) {
            gattServer?.close()
            _ppogService = PPoGService(scope)
            gattServer = GattServerImpl(
                    context.getSystemService(BluetoothManager::class.java)!!,
                    context,
                    listOf(ppogService!!, DummyService())
            )
        }
        gattServerJob = gattServer!!.getFlow().onEach {
            Timber.v("Server state: $it")
        }.launchIn(scope)
        return gattServer!!
    }

    fun close() {
        gattServer?.close()
        gattServerJob?.cancel()
        gattServer = null
        gattServerJob = null
    }

}