package io.rebble.cobble.bluetooth.gatt

import android.bluetooth.*
import io.rebble.libpebblecommon.ProtocolHandlerImpl
import io.rebble.libpebblecommon.ble.GATTPacket
import io.rebble.libpebblecommon.packets.PhoneAppVersion
import io.rebble.libpebblecommon.packets.PingPong
import io.rebble.libpebblecommon.protocolhelpers.PebblePacket
import io.rebble.libpebblecommon.protocolhelpers.ProtocolEndpoint
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.test.*
import org.junit.*
import org.junit.Assert.*
import org.mockito.*
import org.mockito.Mockito.*
import timber.log.Timber
import java.util.*

@OptIn(ExperimentalCoroutinesApi::class)
class PPoGATTProtocolHandlerTest {
    private val leServer = FakePPoGATTServer()

    private val charValues = mutableMapOf<UUID, ByteArray?>()

    private val dataUpdates = Channel<ByteArray>(Channel.UNLIMITED)

    private var seq = 0

    companion object {
        @BeforeClass
        @JvmStatic
        fun init() {
            Timber.plant(object : Timber.Tree() {
                override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
                    val stackTrace = Throwable().stackTrace
                    val classTag = stackTrace[5].className.split(".").last()
                    println("[$classTag] $message")
                }
            })
        }
    }

    @Before
    fun setUp() {
//        MockitoAnnotations.openMocks(this)
//        gattServiceMock = mockConstruction(BluetoothGattService::class.java) { mock, _ ->
//            `when`(mock.addCharacteristic(any(BluetoothGattCharacteristic::class.java))).thenReturn(true)
//        }
//        gattCharMock = mockConstruction(BluetoothGattCharacteristic::class.java) { mock, context ->
//            val uuid = context.arguments().first { it is UUID } as UUID
//            `when`(mock.addDescriptor(any(BluetoothGattDescriptor::class.java))).thenReturn(true)
//            `when`(mock.uuid).thenReturn(uuid)
//            `when`(mock.value).thenReturn(charValues[uuid])
//            `when`(mock.setValue(any(ByteArray::class.java))).then {
//                charValues[uuid] = it.getArgument<ByteArray>(0)
//                if (UUID.fromString(LEConstants.UUIDs.PPOGATT_DEVICE_CHARACTERISTIC_SERVER) == uuid) {
//                    dataUpdates.trySend(charValues[UUID.fromString(LEConstants.UUIDs.PPOGATT_DEVICE_CHARACTERISTIC_SERVER)]!!)
//                }
//                return@then true
//            }
//        }
//
//        activityCompatMock = mockStatic(ContextCompat::class.java, Answers.CALLS_REAL_METHODS)
//        activityCompatMock.`when`<Int>{
//            ContextCompat.checkSelfPermission(any(Context::class.java), any(String::class.java))
//        }.thenReturn(PackageManager.PERMISSION_GRANTED)
//
//        `when`(bluetoothManager.openGattServer(any(Context::class.java), any(BluetoothGattServerCallback::class.java))).then {
//            callbacks = it.getArgument(1)
//            return@then bluetoothGattServer
//        }
//        `when`(bluetoothGattServer.addService(any(BluetoothGattService::class.java))).then {
//            callbacks.onServiceAdded(BluetoothGatt.GATT_SUCCESS, it.getArgument(0))
//            return@then true
//        }
//        `when`(bluetoothGattServer.sendResponse(any(BluetoothDevice::class.java), any(Int::class.java), any(Int::class.java), any(Int::class.java), any(ByteArray::class.java)))
//                .thenReturn(true)
//        `when`(bluetoothGattServer.notifyCharacteristicChanged(any(BluetoothDevice::class.java), any(BluetoothGattCharacteristic::class.java), any(Boolean::class.java)))
//                .then {
//                    callbacks.onNotificationSent(it.getArgument(0), BluetoothGatt.GATT_SUCCESS)
//                    return@then true
//                }
//
//        `when`(context.applicationContext).thenReturn(context)
//        `when`(context.getSystemService(Context.BLUETOOTH_SERVICE)).thenReturn(bluetoothManager)
//
//        //`when`(gatt.device).thenReturn(device)
//        `when`(device.address).thenReturn("12:23:34:98:76:54")
//        //`when`(device.connectGatt(any(Context::class.java), anyBoolean(), any(BluetoothGattCallback::class.java), anyInt())).thenReturn(gatt)
//        seq = 0
    }

    @After
    fun tearDown() {
//        gattServiceMock.close()
//        gattCharMock.close()
//        activityCompatMock.close()
    }

    @Test
    fun `Send reset packet to watch and enable connection after first packet`() = runTest {
        val protocolHandler = PPoGATTProtocolHandler(backgroundScope, leServer)

        watchConnection()
        leServer.emitPacketFromWatch(PingPong.Ping(1u))

        runCurrent()

        leServer.assertPacketsSentToWatch(
                GATTPacket(GATTPacket.PacketType.RESET_ACK, 0, byteArrayOf()),
                GATTPacket(GATTPacket.PacketType.ACK, 0),
        )

        assert(protocolHandler.connectionStateChannel.receive()) {
            "Connection state is set as connected"
        }
    }


    @Test
    fun `Watch handshake with libpebblecommon`() = runTest {
        val protocolHandler = PPoGATTProtocolHandler(backgroundScope, leServer)
        val pebbleProtocol = createTestPebbleProtocol(protocolHandler)

        val resp = PhoneAppVersion.AppVersionResponse(
                1u,
                1u,
                1u,
                1u,
                1u,
                1u,
                1u,
                ubyteArrayOf(1u)
        )

        pebbleProtocol.registerReceiveCallback(ProtocolEndpoint.PHONE_VERSION) {
            assert(it is PhoneAppVersion.AppVersionRequest)
            pebbleProtocol.send(resp)
        }


        watchConnection()
        leServer.emitPacketFromWatch(PhoneAppVersion.AppVersionRequest())
        leServer.emitPacketFromWatch(GATTPacket(GATTPacket.PacketType.ACK, 0))

        runCurrent()

        leServer.assertPacketsSentToWatch(
                GATTPacket(GATTPacket.PacketType.RESET_ACK, 0, byteArrayOf()),
                GATTPacket(GATTPacket.PacketType.ACK, 0),
                GATTPacket(GATTPacket.PacketType.DATA, 0, resp.serialize().asByteArray().take(21).toByteArray()),
                GATTPacket(GATTPacket.PacketType.DATA, 1, resp.serialize().asByteArray().takeLast(8).toByteArray()),
        )
    }

//    @Test
//    fun `Test connection version 0 correctly found`() = runTest {
//        val scope = CoroutineScope(testScheduler)
//        val leServer = PPoGATTServerImpl(context, scope)
//        val protocolHandler = PPoGATTProtocolHandler(scope, leServer)
//        leServer.init()
//        advanceUntilIdle()
//        leServer.setTarget(device)
//
//        callbacks.onCharacteristicReadRequest(device, 0, 0, BluetoothGattCharacteristic(UUID.fromString(LEConstants.UUIDs.META_CHARACTERISTIC_SERVER), 0, 0))
//        callbacks.onCharacteristicWriteRequest(
//                device,
//                1,
//                BluetoothGattCharacteristic(UUID.fromString(LEConstants.UUIDs.PPOGATT_DEVICE_CHARACTERISTIC_SERVER), 0, 0),
//                false,
//                false,
//                0,
//                GATTPacket(GATTPacket.PacketType.RESET, 0, byteArrayOf(0)).data
//        )
//        val ack = GATTPacket(dataUpdates.receive())
//        assert(ack.type == GATTPacket.PacketType.RESET_ACK)
//        assertEquals(0, ack.sequence)
//        assertFalse(ack.hasWindowSizes())
//        assertEquals(GATTPacket.PPoGConnectionVersion.ZERO, protocolHandler.connectionVersion)
//    }
//
//    @Test
//    fun `Test connection version 1 correctly found`() = runTest {
//        val scope = CoroutineScope(testScheduler)
//        val leServer = PPoGATTServerImpl(context, scope)
//        val protocolHandler = PPoGATTProtocolHandler(scope, leServer)
//        leServer.init()
//        advanceUntilIdle()
//        leServer.setTarget(device)
//
//        callbacks.onCharacteristicReadRequest(device, 0, 0, BluetoothGattCharacteristic(UUID.fromString(LEConstants.UUIDs.META_CHARACTERISTIC_SERVER), 0, 0))
//        callbacks.onCharacteristicWriteRequest(
//                device,
//                1,
//                BluetoothGattCharacteristic(UUID.fromString(LEConstants.UUIDs.PPOGATT_DEVICE_CHARACTERISTIC_SERVER), 0, 0),
//                false,
//                false,
//                0,
//                GATTPacket(GATTPacket.PacketType.RESET, 0, byteArrayOf(1, 1)).data
//        )
//        val ack = GATTPacket(dataUpdates.receive())
//        assert(ack.type == GATTPacket.PacketType.RESET_ACK)
//        assertEquals(0, ack.sequence)
//        assert(ack.hasWindowSizes())
//        assertEquals(GATTPacket.PPoGConnectionVersion.ONE, protocolHandler.connectionVersion)
//        assertEquals(protocolHandler.maxTXWindow, ack.getMaxTXWindow())
//        assertEquals(protocolHandler.maxRXWindow, ack.getMaxRXWindow())
//    }
//
//    @Test
//    fun `Test connection version downgrades if no windows in reset ACK`() = runTest {
//        val scope = CoroutineScope(testScheduler)
//        val leServer = PPoGATTServerImpl(context, scope)
//        val protocolHandler = PPoGATTProtocolHandler(scope, leServer)
//        leServer.init()
//        advanceUntilIdle()
//        leServer.setTarget(device)
//
//        callbacks.onCharacteristicReadRequest(device, 0, 0, BluetoothGattCharacteristic(UUID.fromString(LEConstants.UUIDs.META_CHARACTERISTIC_SERVER), 0, 0))
//        callbacks.onCharacteristicWriteRequest(
//                device,
//                1,
//                BluetoothGattCharacteristic(UUID.fromString(LEConstants.UUIDs.PPOGATT_DEVICE_CHARACTERISTIC_SERVER), 0, 0),
//                false,
//                false,
//                0,
//                GATTPacket(GATTPacket.PacketType.RESET, 0, byteArrayOf(1, 1)).data
//        )
//        val ack = GATTPacket(dataUpdates.receive())
//        advanceUntilIdle()
//        assert(ack.type == GATTPacket.PacketType.RESET_ACK)
//        assertEquals(0, ack.sequence)
//        assert(ack.hasWindowSizes())
//        assertEquals(GATTPacket.PPoGConnectionVersion.ONE, protocolHandler.connectionVersion)
//        assertEquals(protocolHandler.maxTXWindow, ack.getMaxTXWindow())
//        assertEquals(protocolHandler.maxRXWindow, ack.getMaxRXWindow())
//
//        callbacks.onCharacteristicWriteRequest(
//                device,
//                1,
//                BluetoothGattCharacteristic(UUID.fromString(LEConstants.UUIDs.PPOGATT_DEVICE_CHARACTERISTIC_SERVER), 0, 0),
//                false,
//                false,
//                0,
//                GATTPacket(GATTPacket.PacketType.RESET_ACK, 0, null).data
//        )
//        advanceUntilIdle()
//        withContext(Dispatchers.Default) {
//            delay(100) //XXX
//        }
//        assertEquals(GATTPacket.PPoGConnectionVersion.ZERO, protocolHandler.connectionVersion)
//    }
//
    private fun watchConnection() {
        leServer.emitPacketFromWatch(GATTPacket(GATTPacket.PacketType.RESET, 0, byteArrayOf(0, 0)))
    }
//
//    private suspend fun watchSendPacket(data: ByteArray, chunkSize: Int) {
//        val chunks = data.toList().chunked(chunkSize)
//        for (chunk in chunks) {
//            callbacks.onCharacteristicWriteRequest(
//                    device,
//                    1,
//                    BluetoothGattCharacteristic(UUID.fromString(LEConstants.UUIDs.PPOGATT_DEVICE_CHARACTERISTIC_SERVER), 0, 0),
//                    false,
//                    false,
//                    0,
//                    GATTPacket(GATTPacket.PacketType.DATA, seq, chunk.toByteArray()).data
//            )
//            seq++
//            delay(kotlin.random.Random.nextLong(50))
//        }
//    }
//
//    private suspend fun readPacketIn(firstPacket: ByteArray): UByteArray {
//        val first = GATTPacket(firstPacket)
//        require(first.type == GATTPacket.PacketType.DATA)
//        callbacks.onCharacteristicWriteRequest(
//                device,
//                1,
//                BluetoothGattCharacteristic(UUID.fromString(LEConstants.UUIDs.PPOGATT_DEVICE_CHARACTERISTIC_SERVER), 0, 0),
//                false,
//                false,
//                0,
//                GATTPacket(GATTPacket.PacketType.ACK, first.sequence).data
//        )
//        val pblData = firstPacket.drop(1).toMutableList()
//        val length = (DataBuffer(pblData.toByteArray().asUByteArray()).getUShort()+4u).toInt()
//        Timber.d("Read ${pblData.size}/$length")
//        while (pblData.size < length) {
//            val more = dataUpdates.receive()
//            val packet = GATTPacket(more)
//            check(packet.type == GATTPacket.PacketType.DATA)
//            pblData.addAll(packet.data.drop(1))
//            Timber.d("Read ${pblData.size}/$length")
//            callbacks.onCharacteristicWriteRequest(
//                    device,
//                    1,
//                    BluetoothGattCharacteristic(UUID.fromString(LEConstants.UUIDs.PPOGATT_DEVICE_CHARACTERISTIC_SERVER), 0, 0),
//                    false,
//                    false,
//                    0,
//                    GATTPacket(GATTPacket.PacketType.ACK, packet.sequence).data
//            )
//        }
//        check(pblData.size == length)
//        return pblData.toByteArray().asUByteArray()
//    }
}