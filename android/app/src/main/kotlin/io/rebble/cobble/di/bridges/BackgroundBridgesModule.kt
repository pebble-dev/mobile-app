package io.rebble.cobble.di.bridges

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoSet
import io.rebble.cobble.bridges.FlutterBridge
import io.rebble.cobble.bridges.background.TimelineSyncFlutterBridge
import javax.inject.Qualifier

@Module
/**
 * Module that binds all flutter bridges that are background-only (can only be used from
 * background module)
 */
abstract class BackgroundBridgesModule {
    @Binds
    @IntoSet
    @BackgroundBridge
    abstract fun bindTimelineSyncBridge(
            timelineSyncFlutterBridge: TimelineSyncFlutterBridge
    ): FlutterBridge
}

@Qualifier
annotation class BackgroundBridge