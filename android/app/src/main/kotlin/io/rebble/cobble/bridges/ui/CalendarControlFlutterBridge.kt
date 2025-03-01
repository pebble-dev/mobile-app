package io.rebble.cobble.bridges.ui

import io.rebble.cobble.bluetooth.ConnectionLooper
import io.rebble.cobble.bridges.FlutterBridge
import io.rebble.cobble.pigeons.Pigeons
import io.rebble.cobble.shared.datastore.KMPPrefs
import io.rebble.cobble.shared.domain.calendar.CalendarSync
import io.rebble.cobble.shared.domain.state.ConnectionState
import io.rebble.cobble.util.Debouncer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class CalendarControlFlutterBridge
    @Inject
    constructor(
        private val connectionLooper: ConnectionLooper,
        private val calendarSync: CalendarSync,
        private val coroutineScope: CoroutineScope,
        private val kmpPrefs: KMPPrefs,
        bridgeLifecycleController: BridgeLifecycleController
    ) : Pigeons.CalendarControl, FlutterBridge {
        private val debouncer = Debouncer(debouncingTimeMs = 5_000L, scope = coroutineScope)
        private val calendarCallbacks =
            bridgeLifecycleController.createCallbacks(
                Pigeons::CalendarCallbacks
            )

        init {
            bridgeLifecycleController.setupControl(Pigeons.CalendarControl::setup, this)
            calendarSync.getUpdatesFlow().debounce(50).onEach { calendars ->
                Timber.d("Calendar list updated: %d", calendars.size)
                calendarCallbacks.onCalendarListUpdated(
                    calendars.map {
                        Pigeons.CalendarPigeon.Builder()
                            .setId(it.id.toLong())
                            .setAccount(it.ownerName)
                            .setName(it.name)
                            .setColor(it.color.toLong())
                            .setEnabled(it.enabled)
                            .build()
                    }.toMutableList()
                ) {}
            }.launchIn(coroutineScope)
        }

        override fun requestCalendarSync(forceResync: Boolean) {
            Timber.d("Request calendar sync %s", connectionLooper.connectionState.value)
            if (connectionLooper.connectionState.value is ConnectionState.Disconnected) {
                // No need to do anything. Calendar will be re-synced automatically when service
                // is restarted
                return
            }

            if (forceResync) {
                coroutineScope.launch {
                    calendarSync.forceFullResync()
                }
            } else {
                // Use debouncer to ensure user quickly selecting calendars will not trigger too
                // many sync requests
                debouncer.executeDebouncing {
                    Timber.d("Sync calendar on request after debounce")
                    calendarSync.doFullCalendarSync()
                }
            }
        }

        override fun getCalendarSyncEnabled(result: Pigeons.Result<Boolean>) {
            coroutineScope.launch {
                kmpPrefs.calendarSyncEnabled.first().let {
                    result.success(it)
                }
            }
        }

        override fun deleteAllCalendarPins(result: Pigeons.Result<Void>) {
            coroutineScope.launch {
                calendarSync.deleteCalendarPinsFromWatch()
                result.success(null)
            }
        }

        override fun getCalendars(result: Pigeons.Result<MutableList<Pigeons.CalendarPigeon>>) {
            coroutineScope.launch {
                val calendars = calendarSync.getCalendars()
                result.success(
                    calendars.map {
                        Pigeons.CalendarPigeon.Builder()
                            .setId(it.id.toLong())
                            .setAccount(it.ownerName)
                            .setName(it.name)
                            .setColor(it.color.toLong())
                            .setEnabled(it.enabled)
                            .build()
                    }.toMutableList()
                )
            }
        }

        override fun setCalendarEnabled(
            id: Long,
            enabled: Boolean,
            result: Pigeons.Result<Void>
        ) {
            coroutineScope.launch {
                calendarSync.setCalendarEnabled(id, enabled)
                calendarSync.doFullCalendarSync()
                result.success(null)
            }
        }

        override fun setCalendarSyncEnabled(
            enabled: Boolean,
            result: Pigeons.Result<Void>
        ) {
            coroutineScope.launch {
                kmpPrefs.setCalendarSyncEnabled(enabled)
                result.success(null)
            }
        }
    }