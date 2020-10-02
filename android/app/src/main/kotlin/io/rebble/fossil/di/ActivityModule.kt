package io.rebble.fossil.di

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import dagger.Module
import dagger.Provides
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.BinaryMessenger
import io.rebble.fossil.MainActivity
import kotlinx.coroutines.CoroutineScope

@Module
class ActivityModule {
    @Provides
    fun provideLifecycle(mainActivity: MainActivity): Lifecycle {
        return mainActivity.lifecycle
    }

    @Provides
    fun provideCoroutineScope(mainActivity: MainActivity): CoroutineScope {
        return mainActivity.lifecycleScope
    }

    @Provides
    fun provideFlutterEngine(mainActivity: MainActivity): FlutterEngine {
        return mainActivity.flutterEngine
                ?: error("Flutter engine should be initialized before creating flutter bridges")
    }

    @Provides
    fun provideBinaryMessenger(flutterEngine: FlutterEngine): BinaryMessenger {
        return flutterEngine.dartExecutor.binaryMessenger
    }
}