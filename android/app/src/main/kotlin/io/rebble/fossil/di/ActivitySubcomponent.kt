package io.rebble.fossil.di

import dagger.BindsInstance
import dagger.Subcomponent
import io.rebble.fossil.MainActivity
import io.rebble.fossil.bridges.FlutterBridge

@Subcomponent(
        modules = [
            ActivityModule::class,
            FlutterBridgesModule::class
        ]
)
interface ActivitySubcomponent {
    fun createFlutterBridges(): Set<FlutterBridge>

    @Subcomponent.Factory
    interface Factory {
        fun create(@BindsInstance mainActivity: MainActivity): ActivitySubcomponent
    }
}