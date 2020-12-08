package io.rebble.cobble.di.bridges

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoSet
import io.rebble.cobble.bridges.FlutterBridge
import io.rebble.cobble.bridges.common.Connection
import io.rebble.cobble.bridges.common.Notifications
import io.rebble.cobble.bridges.common.Scan
import io.rebble.cobble.bridges.common.Timeline
import javax.inject.Qualifier

@Module
/**
 * Module that binds all flutter bridges that are common
 * between UI and background (can be used by both)
 */
abstract class CommonBridgesModule {
    @Binds
    @IntoSet
    @CommonBridge
    abstract fun bindScanBridge(scan: Scan): FlutterBridge

    @Binds
    @IntoSet
    @CommonBridge
    abstract fun bindConnectionBridge(connection: Connection): FlutterBridge

    @Binds
    @IntoSet
    @CommonBridge
    abstract fun bindNotificationsBridge(notifications: Notifications): FlutterBridge

    @Binds
    @IntoSet
    @CommonBridge
    abstract fun bindTimelineBridge(timeline: Timeline): FlutterBridge
}

@Qualifier
annotation class CommonBridge