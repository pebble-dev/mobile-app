package io.rebble.cobble.bluetooth.gatt

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattServer
import android.bluetooth.BluetoothGattServerCallback
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import io.rebble.libpebblecommon.ble.GATTPacket
import io.rebble.libpebblecommon.ble.LEConstants
import io.rebble.libpebblecommon.protocolhelpers.PebblePacket
import io.rebble.libpebblecommon.util.runBlocking
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.test.*
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.mockito.*
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyCollection
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnitRunner
import timber.log.Timber
import java.util.*

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(MockitoJUnitRunner::class)
class PPoGATTProtocolHandlerTest {
    @Mock
    lateinit var context: Context
    @Mock
    lateinit var bluetoothManager: BluetoothManager
    @Mock
    lateinit var bluetoothGattServer: BluetoothGattServer
    lateinit var callbacks: BluetoothGattServerCallback
    @Mock
    lateinit var device: BluetoothDevice
    @Mock
    lateinit var gatt: BluetoothGatt

    lateinit var gattServiceMock: MockedConstruction<BluetoothGattService>
    lateinit var gattCharMock: MockedConstruction<BluetoothGattCharacteristic>
    lateinit var activityCompatMock: MockedStatic<ContextCompat>

    private val charValues = mutableMapOf<UUID, ByteArray?>()

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        Timber.plant(object : Timber.Tree() {
            override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
                println("[Test] $message")
            }

        })

        gattServiceMock = mockConstruction(BluetoothGattService::class.java, MockedConstruction.MockInitializer() {mock, context ->
            `when`(mock.addCharacteristic(any(BluetoothGattCharacteristic::class.java))).thenReturn(true)
        })
        gattCharMock = mockConstruction(BluetoothGattCharacteristic::class.java, MockedConstruction.MockInitializer() {mock, context ->
            val uuid = context.arguments().first { it is UUID } as UUID
            `when`(mock.addDescriptor(any(BluetoothGattDescriptor::class.java))).thenReturn(true)
            `when`(mock.uuid).thenReturn(uuid)
            `when`(mock.getValue()).thenReturn(charValues[uuid])
            `when`(mock.setValue(any(ByteArray::class.java))).then {
                charValues[uuid] = it.getArgument<ByteArray>(0)
                return@then true
            }
        })

        activityCompatMock = mockStatic(ContextCompat::class.java, Answers.CALLS_REAL_METHODS)
        activityCompatMock.`when`<Int>{
            ContextCompat.checkSelfPermission(any(Context::class.java), any(String::class.java))
        }.thenReturn(PackageManager.PERMISSION_GRANTED)

        `when`(bluetoothManager.openGattServer(any(Context::class.java), any(BluetoothGattServerCallback::class.java))).then {
            callbacks = it.getArgument<BluetoothGattServerCallback>(1)
            return@then bluetoothGattServer
        }
        `when`(bluetoothGattServer.addService(any(BluetoothGattService::class.java))).then {
            callbacks.onServiceAdded(BluetoothGatt.GATT_SUCCESS, it.getArgument(0))
            return@then true
        }
        `when`(bluetoothGattServer.sendResponse(any(BluetoothDevice::class.java), any(Int::class.java), any(Int::class.java), any(Int::class.java), any(ByteArray::class.java)))
                .thenReturn(true)
        `when`(bluetoothGattServer.notifyCharacteristicChanged(any(BluetoothDevice::class.java), any(BluetoothGattCharacteristic::class.java), any(Boolean::class.java)))
                .then {
                    callbacks.onNotificationSent(it.getArgument(0), BluetoothGatt.GATT_SUCCESS)
                    return@then true
                }

        `when`(context.applicationContext).thenReturn(context)
        `when`(context.getSystemService(Context.BLUETOOTH_SERVICE)).thenReturn(bluetoothManager)

        `when`(gatt.device).thenReturn(device)
        `when`(device.address).thenReturn("12:23:34:98:76:54")
        `when`(device.connectGatt(any(Context::class.java), anyBoolean(), any(BluetoothGattCallback::class.java), anyInt())).thenReturn(gatt)
    }

    @After
    fun tearDown() {
        gattServiceMock.close()
        gattCharMock.close()
        activityCompatMock.close()
    }

    @Test
    fun `Connection after initial reset`() = runTest {
        val scope = CoroutineScope(testScheduler)
        val leServer = PPoGATTServer(context, scope)
        val protocolHandler = PPoGATTProtocolHandler(scope, leServer)

        leServer.init()
        advanceUntilIdle()

        leServer.setTarget(device)

        val defer = async {
            protocolHandler.connectionStateChannel.receive()
        }
        callbacks.onCharacteristicReadRequest(device, 0, 0, BluetoothGattCharacteristic(UUID.fromString(LEConstants.UUIDs.META_CHARACTERISTIC_SERVER), 0, 0))
        callbacks.onCharacteristicWriteRequest(
                device,
                1,
                BluetoothGattCharacteristic(UUID.fromString(LEConstants.UUIDs.PPOGATT_DEVICE_CHARACTERISTIC_SERVER), 0, 0),
                false,
                false,
                0,
                GATTPacket(GATTPacket.PacketType.RESET, 0, byteArrayOf(0, 0)).data
        )
        callbacks.onCharacteristicWriteRequest(
                device,
                1,
                BluetoothGattCharacteristic(UUID.fromString(LEConstants.UUIDs.PPOGATT_DEVICE_CHARACTERISTIC_SERVER), 0, 0),
                false,
                false,
                0,
                GATTPacket(GATTPacket.PacketType.DATA, 0, byteArrayOf(0)).data
        )
        advanceUntilIdle()

        assert(defer.await())
    }
}