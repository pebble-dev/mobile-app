package io.rebble.cobble.di

import dagger.BindsInstance
import dagger.Subcomponent
import io.flutter.embedding.engine.FlutterEngine
import io.rebble.cobble.bridges.FlutterBridge
import io.rebble.cobble.bridges.ui.BridgeLifecycleController
import io.rebble.cobble.di.bridges.CommonBridge
import io.rebble.cobble.di.bridges.CommonBridgesModule
import kotlinx.coroutines.CoroutineScope

@Subcomponent(modules = [
    CommonBridgesModule::class,
    BackgroundFlutterModule::class
])
interface BackgroundFlutterSubcomponent {
    @CommonBridge
    fun createCommonBridges(): Set<FlutterBridge>

    fun createBridgeLifecycleController(): BridgeLifecycleController

    @Subcomponent.Factory
    interface Factory {
        fun create(
                @BindsInstance flutterEngine: FlutterEngine,
                @BindsInstance coroutineScope: CoroutineScope
        ): BackgroundFlutterSubcomponent
    }
}