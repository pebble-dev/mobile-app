package io.rebble.cobble.di

import dagger.BindsInstance
import dagger.Subcomponent
import io.rebble.cobble.MainActivity
import io.rebble.cobble.bridges.FlutterBridge
import io.rebble.cobble.di.bridges.CommonBridge
import io.rebble.cobble.di.bridges.CommonBridgesModule
import io.rebble.cobble.di.bridges.UiBridge
import io.rebble.cobble.di.bridges.UiBridgesModule
import javax.inject.Scope

@PerActivity
@Subcomponent(
        modules = [
            ActivityModule::class,
            CommonBridgesModule::class,
            UiBridgesModule::class,
        ]
)
interface ActivitySubcomponent {
    @CommonBridge
    fun createCommonBridges(): Set<FlutterBridge>

    @UiBridge
    fun createUiBridges(): Set<FlutterBridge>

    @Subcomponent.Factory
    interface Factory {
        fun create(@BindsInstance mainActivity: MainActivity): ActivitySubcomponent
    }
}

@Scope
annotation class PerActivity