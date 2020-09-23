package io.rebble.fossil.di

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoSet
import io.rebble.fossil.bridges.Connection
import io.rebble.fossil.bridges.FlutterBridge
import io.rebble.fossil.bridges.Notifications
import io.rebble.fossil.bridges.Scan

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
}