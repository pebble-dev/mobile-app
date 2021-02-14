package io.rebble.cobble.transport.bluetooth

import android.app.Instrumentation
import androidx.test.platform.app.InstrumentationRegistry
import io.rebble.cobble.CobbleApplication
import io.rebble.cobble.di.DaggerAppComponent
import io.rebble.libpebblecommon.util.runBlocking
import kotlinx.coroutines.flow.collect
import org.junit.Before
import org.junit.Test
import timber.log.Timber

class ConnectionLooperTest {
    val instrument = InstrumentationRegistry.getInstrumentation()
    val context = instrument.targetContext
    val component = DaggerAppComponent.factory().build(Instrumentation.newApplication(CobbleApplication::class.java, context))

    @Test
    fun testConnectToEmulator() {
        runBlocking {
            val looper = component.createConnectionLooper()
            looper.connectToEmulator("10.0.2.2", 8070)
            var cont = true
            while (cont) {
                looper.connectionState.collect {
                    Timber.d("State: ${it::class.simpleName}")
                }
            }
        }
    }
}