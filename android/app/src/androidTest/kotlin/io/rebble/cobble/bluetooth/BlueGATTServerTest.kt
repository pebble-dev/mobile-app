package io.rebble.cobble.bluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.util.Log
import androidx.test.filters.RequiresDevice
import androidx.test.platform.app.InstrumentationRegistry
import io.rebble.cobble.datasources.FlutterPreferences
import io.rebble.cobble.datasources.IncomingPacketsListener
import io.rebble.libpebblecommon.ProtocolHandlerImpl
import io.rebble.libpebblecommon.packets.PhoneAppVersion
import io.rebble.libpebblecommon.packets.PingPong
import io.rebble.libpebblecommon.packets.ProtocolCapsFlag
import io.rebble.libpebblecommon.protocolhelpers.ProtocolEndpoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import org.junit.Before
import org.junit.Test

@FlowPreview
@RequiresDevice
class BlueGATTServerTest {
    lateinit var blueLEDriver: BlueLEDriver
    val protocolHandler = ProtocolHandlerImpl()
    val incomingPacketsListener = IncomingPacketsListener()
    val flutterPreferences = FlutterPreferences(InstrumentationRegistry.getInstrumentation().targetContext)
    lateinit var remoteDevice: BluetoothDevice

    @Before
    fun setUp() {
        blueLEDriver = BlueLEDriver(InstrumentationRegistry.getInstrumentation().targetContext, protocolHandler, incomingPacketsListener = incomingPacketsListener, flutterPreferences = flutterPreferences)
        remoteDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice("48:91:52:CC:D1:D5")
        if (remoteDevice.bondState != BluetoothDevice.BOND_NONE) remoteDevice::class.java.getMethod("removeBond").invoke(remoteDevice)
    }

    @Test
    fun testConnectPebble() {
        protocolHandler.registerReceiveCallback(ProtocolEndpoint.PHONE_VERSION) {
            protocolHandler.send(PhoneAppVersion.AppVersionResponse(
                    0U,
                    0U,
                    PhoneAppVersion.PlatformFlag.makeFlags(PhoneAppVersion.OSType.Android, listOf(PhoneAppVersion.PlatformFlag.BTLE)),
                    2U,
                    2U, 3U, 0U,
                    ProtocolCapsFlag.makeFlags(listOf())
            ))
        }

        runBlocking {
            while (true) {
                blueLEDriver.startSingleWatchConnection(PebbleBluetoothDevice(remoteDevice)).collect { value ->
                    when (value) {
                        is SingleConnectionStatus.Connected -> {
                            Log.d("Test", "Connected")
                            GlobalScope.launch {
                                delay(5000)
                                protocolHandler.send(PingPong.Ping(0x1337u))
                            }
                        }
                        is SingleConnectionStatus.Connecting -> {
                            Log.d("Test", "Connecting")
                        }
                    }
                }
            }
        }
    }
}