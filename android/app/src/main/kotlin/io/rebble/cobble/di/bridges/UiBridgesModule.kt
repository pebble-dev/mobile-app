package io.rebble.cobble.di.bridges

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoSet
import io.rebble.cobble.bridges.FlutterBridge
import io.rebble.cobble.bridges.ui.AppLifecycle
import io.rebble.cobble.bridges.ui.ConnectionUi
import io.rebble.cobble.bridges.ui.Debug
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
    abstract fun bindConnectionUiBridge(connection: ConnectionUi): FlutterBridge

    @Binds
    @IntoSet
    @UiBridge
    abstract fun bindAppLifecycleBridge(appLifecycle: AppLifecycle): FlutterBridge

    @Binds
    @IntoSet
    @UiBridge
    abstract fun bindDebugBridge(debug: Debug): FlutterBridge
}

@Qualifier
annotation class UiBridge