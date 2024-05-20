package io.rebble.cobble.bluetooth.ble

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.os.ParcelUuid
import androidx.test.filters.RequiresDevice
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import io.rebble.cobble.bluetooth.ble.connectGatt
import io.rebble.libpebblecommon.ble.LEConstants
import io.rebble.libpebblecommon.util.runBlocking
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.withTimeout
import org.junit.Test
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import timber.log.Timber
import java.util.UUID

@RequiresDevice
@OptIn(FlowPreview::class)
class PebbleLEConnectorTest {
    @JvmField
    @Rule
    val mGrantPermissionRule: GrantPermissionRule = GrantPermissionRule.grant(
            android.Manifest.permission.BLUETOOTH_SCAN,
            android.Manifest.permission.BLUETOOTH_CONNECT,
            android.Manifest.permission.BLUETOOTH_ADMIN,
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
            android.Manifest.permission.BLUETOOTH
    )

    lateinit var context: Context
    lateinit var bluetoothAdapter: BluetoothAdapter

    companion object {
        private val DEVICE_ADDRESS_LE = "6F:F1:85:CA:8B:20"
    }

    @Before
    fun setUp() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        Timber.plant(Timber.DebugTree())
        val bluetoothManager = context.getSystemService(BluetoothManager::class.java)
        bluetoothAdapter = bluetoothManager.adapter
    }
    private fun removeBond(device: BluetoothDevice) {
        device::class.java.getMethod("removeBond").invoke(device) // Internal API
    }

    @Suppress("DEPRECATION") // we are an exception as a test
    private suspend fun restartBluetooth() {
        bluetoothAdapter.disable()
        while (bluetoothAdapter.isEnabled) {
            delay(100)
        }
        delay(1000)
        bluetoothAdapter.enable()
        while (!bluetoothAdapter.isEnabled) {
            delay(100)
        }
    }

    @Test
    fun testConnectPebble() = runBlocking {
        withTimeout(10000) {
            restartBluetooth()
        }
        val remoteDevice = bluetoothAdapter.getRemoteLeDevice(DEVICE_ADDRESS_LE, BluetoothDevice.ADDRESS_TYPE_RANDOM)
        removeBond(remoteDevice)
        val connection = remoteDevice.connectGatt(context, false)
        assertNotNull(connection)
        val connector = PebbleLEConnector(connection!!, context, GlobalScope)
        val order = mutableListOf<PebbleLEConnector.ConnectorState>()
        connector.connect().collect {
            println(it)
            order.add(it)
        }
        assertEquals(
            listOf(
                PebbleLEConnector.ConnectorState.CONNECTING,
                PebbleLEConnector.ConnectorState.PAIRING,
                PebbleLEConnector.ConnectorState.CONNECTED
            ),
            order
        )
        connection.close()
    }

    @Test
    fun testConnectPebbleWithBond() = runBlocking {
        withTimeout(10000) {
            restartBluetooth()
        }
        val remoteDevice = bluetoothAdapter.getRemoteLeDevice(DEVICE_ADDRESS_LE, BluetoothDevice.ADDRESS_TYPE_RANDOM)
        val connection = remoteDevice.connectGatt(context, false)
        assertNotNull(connection)
        val connector = PebbleLEConnector(connection!!, context, GlobalScope)
        val order = mutableListOf<PebbleLEConnector.ConnectorState>()
        connector.connect().collect {
            println(it)
            order.add(it)
        }
        assertEquals(
            listOf(
                PebbleLEConnector.ConnectorState.CONNECTING,
                PebbleLEConnector.ConnectorState.CONNECTED
            ),
            order
        )
        connection.close()
    }
}