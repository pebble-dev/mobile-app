package io.rebble.cobble.di.bridges

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoSet
import io.rebble.cobble.bridges.FlutterBridge
import io.rebble.cobble.bridges.ui.*
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
    abstract fun bindConnectionUiBridge(
        connectionFlutterBridge: ConnectionUiFlutterBridge
    ): FlutterBridge

    @Binds
    @IntoSet
    @UiBridge
    abstract fun bindIntentsBridge(intentsFlutterBridge: IntentsFlutterBridge): FlutterBridge

    @Binds
    @IntoSet
    @UiBridge
    abstract fun bindDebugBridge(debugFlutterBridge: DebugFlutterBridge): FlutterBridge

    @Binds
    @IntoSet
    @UiBridge
    abstract fun bindPermission(
        permissionControlFlutterBridge: PermissionControlFlutterBridge
    ): FlutterBridge

    @Binds
    @IntoSet
    @UiBridge
    abstract fun bindCalendarControl(
        calendarControlFlutterBridge: CalendarControlFlutterBridge
    ): FlutterBridge

    @Binds
    @IntoSet
    @UiBridge
    abstract fun bindWorkaroundsControl(bridge: WorkaroundsFlutterBridge): FlutterBridge

    @Binds
    @IntoSet
    @UiBridge
    abstract fun bindFirmwareUpdateControl(
        bridge: FirmwareUpdateControlFlutterBridge
    ): FlutterBridge
}

@Qualifier
annotation class UiBridge