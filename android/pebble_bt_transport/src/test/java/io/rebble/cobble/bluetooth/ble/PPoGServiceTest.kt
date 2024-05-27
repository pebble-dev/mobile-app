package io.rebble.cobble.bluetooth.ble

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattServer
import android.bluetooth.BluetoothGattService
import io.mockk.core.ValueClassSupport.boxedValue
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.verify
import io.rebble.libpebblecommon.ble.GATTPacket
import io.rebble.libpebblecommon.ble.LEConstants
import io.rebble.libpebblecommon.packets.PingPong
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
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
        assertFalse("Flow still hasn't emitted", result.isActive)
        assertTrue("Flow result wasn't link error, timeout hasn't triggered", result.await() is PPoGService.PPoGConnectionEvent.LinkError)
    }

    @Test
    fun `PPoG handshake completes`() = runTest {
        mockBtGattServiceConstructors()
        mockBtCharacteristicConstructors()
        val serverEventFlow = MutableSharedFlow<ServerEvent>()
        serverEventFlow.subscriptionCount.onEach {
            println("Updated server subscription count: $it")
        }.launchIn(backgroundScope)

        val deviceMock = makeMockDevice()
        val ppogService = PPoGService(backgroundScope)
        val rawBtService = ppogService.register(serverEventFlow)
        val flow = ppogService.rxFlowFor(deviceMock)

        val metaCharacteristic: BluetoothGattCharacteristic = mockk() {
            every { uuid } returns UUID.fromString(LEConstants.UUIDs.META_CHARACTERISTIC_SERVER)
            every { value } throws NotImplementedError()
        }
        val dataCharacteristic: BluetoothGattCharacteristic = mockk() {
            every { uuid } returns UUID.fromString(LEConstants.UUIDs.PPOGATT_DEVICE_CHARACTERISTIC_SERVER)
            every { value } throws NotImplementedError()
        }
        val dataCharacteristicConfigDescriptor: BluetoothGattDescriptor = mockk() {
            every { uuid } returns UUID.fromString(LEConstants.UUIDs.CHARACTERISTIC_CONFIGURATION_DESCRIPTOR)
            every { value } throws NotImplementedError()
            every { characteristic } returns dataCharacteristic
        }
        val metaResponse = CompletableDeferred<CharacteristicResponse>()
        val mockServer = MockGattServer(serverEventFlow, backgroundScope)

        // Connect
        launch {
            serverEventFlow.emit(ServerInitializedEvent(mockServer))
            serverEventFlow.emit(ConnectionStateEvent(deviceMock, BluetoothGatt.GATT_SUCCESS, BluetoothGatt.STATE_CONNECTED))
            PPoGLinkStateManager.updateState(deviceMock.address, PPoGLinkState.ReadyForSession)
        }
        runCurrent()
        assertEquals(2, serverEventFlow.subscriptionCount.value)
        // Read meta
        launch {
            serverEventFlow.emit(CharacteristicReadEvent(deviceMock, 0, 0, metaCharacteristic) {
                metaResponse.complete(it)
            })
        }
        runCurrent()
        val metaValue = metaResponse.await()
        assertEquals(BluetoothGatt.GATT_SUCCESS, metaValue.status)
        // min ppog, max ppog, app uuid, ?
        val expectedMeta = byteArrayOf(0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1)
        assertArrayEquals(expectedMeta, metaValue.value)

        // Subscribe to data
        var result = CompletableDeferred<Int>()
        launch {
            serverEventFlow.emit(DescriptorWriteEvent(deviceMock, 0, dataCharacteristicConfigDescriptor, 0, LEConstants.CHARACTERISTIC_SUBSCRIBE_VALUE) {
                result.complete(it)
            })
        }
        runCurrent()
        assertEquals(BluetoothGatt.GATT_SUCCESS, result.await())

        // Write reset
        val resetPacket = GATTPacket(GATTPacket.PacketType.RESET, 0, byteArrayOf(1)) // V1
        val response = async {
            mockServer.mockServerNotifies.receiveCatching()
        }
        launch {
            serverEventFlow.emit(CharacteristicWriteEvent(deviceMock, 0, dataCharacteristic, false, false, 0, resetPacket.toByteArray()) {
                throw AssertionError("Shouldn't send GATT status in response to PPoGATT request ($it)")
            })
        }
        // RX reset response
        runCurrent()
        val responseValue = response.await().getOrThrow()
        val responsePacket = GATTPacket(responseValue.value)
        assertEquals(GATTPacket.PacketType.RESET_ACK, responsePacket.type)
        assertEquals(0, responsePacket.sequence)
        assertTrue(responsePacket.hasWindowSizes())
        assertEquals(25, responsePacket.getMaxRXWindow().toInt())
        assertEquals(25, responsePacket.getMaxTXWindow().toInt())

        // Write reset ack
        val resetAckPacket = GATTPacket(GATTPacket.PacketType.RESET_ACK, 0, byteArrayOf(25, 25)) // 25 window size
        launch {
            serverEventFlow.emit(CharacteristicWriteEvent(deviceMock, 0, dataCharacteristic, false, false, 0, resetAckPacket.toByteArray()) {
                throw AssertionError("Shouldn't send GATT status in response to PPoGATT request ($it)")
            })
        }
        runCurrent()
        assertEquals(PPoGLinkState.SessionOpen, PPoGLinkStateManager.getState(deviceMock.address).value)

        // Send N packets
        val pebblePacket = PingPong.Ping(1u).serialize().asByteArray()
        val acks: MutableList<GATTPacket> = mutableListOf()
        val acksJob = mockServer.mockServerNotifies.receiveAsFlow().onEach {
            val packet = GATTPacket(it.value)
            if (packet.type == GATTPacket.PacketType.ACK) {
                acks.add(packet)
            }
        }.launchIn(backgroundScope)

        for (i in 0 until 25) {
            val packet = GATTPacket(GATTPacket.PacketType.DATA, i, pebblePacket)
            launch {
                serverEventFlow.emit(CharacteristicWriteEvent(deviceMock, 0, dataCharacteristic, false, false, 0, packet.toByteArray()) {
                    throw AssertionError("Shouldn't send GATT status in response to PPoGATT request ($it)")
                })
            }
            runCurrent()
        }
        acksJob.cancel()
        assertEquals(2, acks.size) // acks are every window/2
    }
}