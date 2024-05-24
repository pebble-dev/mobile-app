package io.rebble.cobble.bluetooth.ble

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.verify
import io.rebble.libpebblecommon.ble.LEConstants
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.*
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import org.junit.function.ThrowingRunnable
import timber.log.Timber
import java.util.UUID
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
class PPoGServiceTest {

    @Before
    fun setup() {
        Timber.plant(object : Timber.DebugTree() {
            override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
                println("$tag: $message")
                t?.printStackTrace()
                System.out.flush()
            }
        })
    }
    private fun makeMockDevice(): BluetoothDevice {
        val device = mockk<BluetoothDevice>()
        every { device.address } returns "00:00:00:00:00:00"
        every { device.name } returns "Test Device"
        every { device.type } returns BluetoothDevice.DEVICE_TYPE_LE
        return device
    }

    private fun mockBtGattServiceConstructors() {
        mockkConstructor(BluetoothGattService::class)
        every { anyConstructed<BluetoothGattService>().uuid } answers {
            fieldValue
        }
        every { anyConstructed<BluetoothGattService>().addCharacteristic(any()) } returns true
    }

    private fun mockBtCharacteristicConstructors() {
        mockkConstructor(BluetoothGattCharacteristic::class)
        every { anyConstructed<BluetoothGattCharacteristic>().uuid } answers {
            fieldValue
        }
        every { anyConstructed<BluetoothGattCharacteristic>().addDescriptor(any()) } returns true
    }

    @Test
    fun `Characteristics created on service registration`(): Unit = runTest {
        mockBtGattServiceConstructors()
        mockBtCharacteristicConstructors()

        val scope = CoroutineScope(testScheduler)
        val ppogService = PPoGService(scope)
        val serverEventFlow = MutableSharedFlow<ServerEvent>()
        val rawBtService = ppogService.register(serverEventFlow)
        runCurrent()
        scope.cancel()

        verify(exactly = 2) { anyConstructed<BluetoothGattService>().addCharacteristic(any()) }
        verify(exactly = 1) { anyConstructed<BluetoothGattCharacteristic>().addDescriptor(any()) }
    }

    @Test
    fun `Service handshake has link state timeout`() = runTest {
        mockBtGattServiceConstructors()
        mockBtCharacteristicConstructors()
        val serverEventFlow = MutableSharedFlow<ServerEvent>()
        val deviceMock = makeMockDevice()
        val ppogService = PPoGService(backgroundScope)
        val rawBtService = ppogService.register(serverEventFlow)
        val flow = ppogService.rxFlowFor(deviceMock)
        val result = async {
            flow.first()
        }
        launch {
            serverEventFlow.emit(ServerInitializedEvent(mockk()))
            serverEventFlow.emit(ConnectionStateEvent(deviceMock, BluetoothGatt.GATT_SUCCESS, BluetoothGatt.STATE_CONNECTED))
        }
        runCurrent()
        assertTrue("Flow prematurely emitted a value", result.isActive)
        advanceTimeBy((10+1).seconds.inWholeMilliseconds)
        assertTrue("Flow still hasn't emitted", !result.isActive)
        assertTrue("Flow result wasn't link error, timeout hasn't triggered", result.await() is PPoGService.PPoGConnectionEvent.LinkError)
    }
}