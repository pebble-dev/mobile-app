package io.rebble.cobble.di

import dagger.Module
import dagger.Provides
import dagger.Reusable
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.BinaryMessenger
import io.rebble.cobble.bridges.ui.BridgeLifecycleController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope

@Module
class BackgroundFlutterModule {
    /**
     * Background flutter module is always active. Use GlobalScope.
     */
    @Provides
    fun provideCoroutineScope(): CoroutineScope = GlobalScope

    @Provides
    fun provideBinaryMessenger(flutterEngine: FlutterEngine): BinaryMessenger {
        return flutterEngine.dartExecutor.binaryMessenger
    }

    @Provides
    @Reusable
    fun provideBridgeLifecycleController(
            binaryMessenger: BinaryMessenger,
            coroutineScope: CoroutineScope
    ) = BridgeLifecycleController(binaryMessenger, coroutineScope)
}