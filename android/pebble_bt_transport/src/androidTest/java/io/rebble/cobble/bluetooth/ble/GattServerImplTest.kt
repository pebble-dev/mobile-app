package io.rebble.cobble.bluetooth.ble

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothManager
import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import io.rebble.libpebblecommon.util.runBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeout
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import timber.log.Timber
import org.junit.Assert.*
import java.util.UUID

class GattServerImplTest {
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
    lateinit var bluetoothManager: BluetoothManager
    lateinit var bluetoothAdapter: BluetoothAdapter

    @Before
    fun setUp() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        Timber.plant(Timber.DebugTree())
        bluetoothManager = context.getSystemService(BluetoothManager::class.java)
        bluetoothAdapter = bluetoothManager.adapter
    }

    @Test
    fun createGattServer() = runTest {
        val server = GattServerImpl(bluetoothManager, context, emptyList())
        val flow = server.getFlow()
        flow.take(1).collect {
            assertTrue(it is ServerInitializedEvent)
        }
    }

    @Test
    fun createGattServerWithServices() = runTest {
        val service = object : GattService {
            override fun register(eventFlow: SharedFlow<ServerEvent>): BluetoothGattService {
                return BluetoothGattService(UUID.randomUUID(), BluetoothGattService.SERVICE_TYPE_PRIMARY)
            }
        }
        val service2 = object : GattService {
            override fun register(eventFlow: SharedFlow<ServerEvent>): BluetoothGattService {
                return BluetoothGattService(UUID.randomUUID(), BluetoothGattService.SERVICE_TYPE_PRIMARY)
            }
        }
        val server = GattServerImpl(bluetoothManager, context, listOf(service, service2))
        val flow = server.getFlow()
        flow.take(1).collect {
            assertTrue(it is ServerInitializedEvent)
            it as ServerInitializedEvent
            assertEquals(2, it.server.getServer()?.services?.size)
        }
    }
}