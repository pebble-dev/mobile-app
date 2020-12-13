package io.rebble.cobble.di.bridges

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoSet
import io.rebble.cobble.bridges.FlutterBridge
import io.rebble.cobble.bridges.common.ConnectionFlutterBridge
import io.rebble.cobble.bridges.common.NotificationsFlutterBridge
import io.rebble.cobble.bridges.common.ScanFlutterBridge
import io.rebble.cobble.bridges.common.TimelineFlutterBridge
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
    abstract fun bindScanBridge(scanFlutterBridge: ScanFlutterBridge): FlutterBridge

    @Binds
    @IntoSet
    @CommonBridge
    abstract fun bindConnectionBridge(connectionFlutterBridge: ConnectionFlutterBridge): FlutterBridge

    @Binds
    @IntoSet
    @CommonBridge
    abstract fun bindNotificationsBridge(notificationsFlutter: NotificationsFlutterBridge): FlutterBridge

    @Binds
    @IntoSet
    @CommonBridge
    abstract fun bindTimelineBridge(timelineFlutterBridge: TimelineFlutterBridge): FlutterBridge
}

@Qualifier
annotation class CommonBridge