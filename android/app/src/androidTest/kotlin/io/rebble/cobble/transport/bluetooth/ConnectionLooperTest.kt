package io.rebble.cobble.transport.bluetooth

import android.app.Instrumentation
import androidx.test.platform.app.InstrumentationRegistry
import io.rebble.cobble.CobbleApplication
import io.rebble.cobble.di.DaggerAppComponent
import io.rebble.libpebblecommon.packets.PingPong
import io.rebble.libpebblecommon.util.runBlocking
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import timber.log.Timber

class ConnectionLooperTest {
    val instrument = InstrumentationRegistry.getInstrumentation()
    val context = instrument.targetContext
    val component = DaggerAppComponent.factory().build(Instrumentation.newApplication(CobbleApplication::class.java, context))

    @Test
    fun testEmulatorTransport() {
        var success = false
        var end = false
        var connecting = false
        runBlocking {
            val looper = component.createConnectionLooper()
            val protocolHandler = component.createProtocolHandler()
            looper.connectToEmulator("10.0.2.2", 8070)
            protocolHandler.registerReceiveCallback(PingPong.endpoint) { packet ->
                if (packet is PingPong.Pong) {
                    success = packet.cookie.get() == 0x1337u
                }
                end = true
            }

            GlobalScope.launch(Dispatchers.Main.immediate) {
                looper.connectionState.collect {
                    assertFalse("Failed to connect to emulator", it is ConnectionState.Disconnected && connecting)
                    if (it is ConnectionState.Connecting) connecting = true
                    if (it is ConnectionState.Connected) {
                        delay(1000)
                        assertTrue("Failed to send ping", protocolHandler.send(PingPong.Ping(0x1337u)))
                    }
                }
            }
            while (!end)  {delay(100)}
        }
        assertTrue("Didn't receive ping / incorrect cookie", success)
    }
}