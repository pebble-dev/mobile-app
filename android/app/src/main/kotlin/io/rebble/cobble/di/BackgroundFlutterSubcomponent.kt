package io.rebble.cobble.di

import dagger.BindsInstance
import dagger.Subcomponent
import io.flutter.embedding.engine.FlutterEngine
import io.rebble.cobble.bridges.FlutterBridge
import io.rebble.cobble.di.bridges.BackgroundBridge
import io.rebble.cobble.di.bridges.BackgroundBridgesModule
import io.rebble.cobble.di.bridges.CommonBridge
import io.rebble.cobble.di.bridges.CommonBridgesModule

@Subcomponent(modules = [
    CommonBridgesModule::class,
    BackgroundFlutterModule::class,
    BackgroundBridgesModule::class
])
interface BackgroundFlutterSubcomponent {
    @CommonBridge
    fun createCommonBridges(): Set<FlutterBridge>

    @BackgroundBridge
    fun createBackgroundBridges(): Set<FlutterBridge>

    @Subcomponent.Factory
    interface Factory {
        fun create(@BindsInstance flutterEngine: FlutterEngine): BackgroundFlutterSubcomponent
    }
}