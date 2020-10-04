package io.rebble.fossil.di

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoSet
import io.rebble.fossil.bridges.*

@Module
abstract class FlutterBridgesModule {
    @Binds
    @IntoSet
    abstract fun bindScanBridge(scan: Scan): FlutterBridge

    @Binds
    @IntoSet
    abstract fun bindConnectionBridge(connection: Connection): FlutterBridge

    @Binds
    @IntoSet
    abstract fun bindNotificationsBridge(notifications: Notifications): FlutterBridge

    @Binds
    @IntoSet
    abstract fun bindAppLifecycleBridge(appLifecycle: AppLifecycle): FlutterBridge

    @Binds
    @IntoSet
    abstract fun bindDebugBridge(debug: Debug): FlutterBridge
}