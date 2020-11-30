package io.rebble.cobble.di

import dagger.BindsInstance
import dagger.Subcomponent
import io.rebble.cobble.MainActivity
import io.rebble.cobble.bridges.FlutterBridge
import javax.inject.Scope

@PerActivity
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

@Scope
annotation class PerActivity