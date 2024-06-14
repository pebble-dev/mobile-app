package io.rebble.cobble.bluetooth.ble

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import androidx.test.filters.RequiresDevice
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import io.rebble.libpebblecommon.util.runBlocking
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import timber.log.Timber

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
        private val DEVICE_ADDRESS_LE = "71:D2:AE:CE:30:C1"
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

    @Test
    fun testConnectPebble() = runBlocking {
        withTimeout(10000) {
            restartBluetooth(bluetoothAdapter)
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
            restartBluetooth(bluetoothAdapter)
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