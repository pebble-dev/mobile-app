package io.rebble.cobble.di.bridges

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoSet
import io.rebble.cobble.bridges.FlutterBridge
import io.rebble.cobble.bridges.ui.AppLifecycleFlutterBridge
import io.rebble.cobble.bridges.ui.ConnectionUiFlutterBridge
import io.rebble.cobble.bridges.ui.DebugFlutterBridge
import javax.inject.Qualifier

@Module
/**
 * Module that binds all flutter bridges that are UI-only (can only be used from the main flutter
 * view)
 */
abstract class UiBridgesModule {
    @Binds
    @IntoSet
    @UiBridge
    abstract fun bindConnectionUiBridge(connectionFlutterBridge: ConnectionUiFlutterBridge): FlutterBridge

    @Binds
    @IntoSet
    @UiBridge
    abstract fun bindAppLifecycleBridge(appLifecycleFlutterBridge: AppLifecycleFlutterBridge): FlutterBridge

    @Binds
    @IntoSet
    @UiBridge
    abstract fun bindDebugBridge(debugFlutterBridge: DebugFlutterBridge): FlutterBridge
}

@Qualifier
annotation class UiBridge