package io.rebble.cobble.bridges.background

import android.content.Context
import io.flutter.FlutterInjector
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.embedding.engine.dart.DartExecutor
import io.flutter.embedding.engine.plugins.util.GeneratedPluginRegister
import io.flutter.view.FlutterCallbackInformation
import io.rebble.cobble.datasources.AndroidPreferences
import io.rebble.cobble.di.BackgroundFlutterSubcomponent
import io.rebble.cobble.util.registerAsyncPigeonCallback
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@Singleton
class FlutterBackgroundController @Inject constructor(
        private val context: Context,
        private val androidPreferences: AndroidPreferences,
        private val backgroundFlutterSubcomponentFactory: BackgroundFlutterSubcomponent.Factory
) {
    private val mutex = Mutex()
    private var engine: FlutterEngine? = null

    suspend fun getBackgroundFlutterEngine(): FlutterEngine? {
        engine?.let { return it }

        mutex.withLock {
            val currentEngine = engine
            if (currentEngine != null) {
                return currentEngine
            }

            engine = initEngine()
            return engine
        }
    }

    private suspend fun initEngine(): FlutterEngine? = withContext(Dispatchers.Main) {
        // Flutter must be initialized on the main thread

        coroutineScope {
            val backgroundEndpointMethodHandle = androidPreferences.backgroundEndpoint
                    ?: return@coroutineScope null

            val callbackInformation = FlutterCallbackInformation
                    .lookupCallbackInformation(backgroundEndpointMethodHandle)

            val bundlePath = FlutterInjector.instance().flutterLoader().findAppBundlePath()

            val callback = DartExecutor.DartCallback(
                    context.assets,
                    bundlePath,
                    callbackInformation
            )

            val flutterEngine = FlutterEngine(context)

            val dartExecutor = flutterEngine.dartExecutor
            val binaryMessenger = dartExecutor.binaryMessenger
            val androidSideReadyCompletable = CompletableDeferred<Unit>()

            val dartInitWait = launch {
                suspendCoroutine { continuation ->
                    binaryMessenger.registerAsyncPigeonCallback(
                            GlobalScope + Dispatchers.Main,
                            "dev.flutter.pigeon.BackgroundControl.notifyFlutterBackgroundStarted"
                    ) {
                        continuation.resume(Unit)

                        // Do not return from notifyFlutterBackgroundStarted() method until
                        // initEngine() has completed
                        androidSideReadyCompletable.join()
                        mapOf("result" to null)
                    }
                }
            }

            dartExecutor.executeDartCallback(callback)

            GeneratedPluginRegister.registerGeneratedPlugins(flutterEngine)


            dartInitWait.join()
            backgroundFlutterSubcomponentFactory.create(flutterEngine).createCommonBridges()
            androidSideReadyCompletable.complete(Unit)

            return@coroutineScope flutterEngine
        }
    }
}