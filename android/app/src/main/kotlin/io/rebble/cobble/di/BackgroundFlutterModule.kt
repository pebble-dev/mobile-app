package io.rebble.cobble.di

import dagger.Module
import dagger.Provides
import dagger.Reusable
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.BinaryMessenger
import io.rebble.cobble.bridges.ui.BridgeLifecycleController
import kotlinx.coroutines.CoroutineScope

@Module
class BackgroundFlutterModule {
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