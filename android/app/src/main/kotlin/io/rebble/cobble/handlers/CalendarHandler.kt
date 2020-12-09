package io.rebble.cobble.handlers

import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import android.provider.CalendarContract
import io.rebble.cobble.bridges.background.CalendarFlutterBridge
import io.rebble.cobble.util.Debouncer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import javax.inject.Inject

class CalendarHandler @Inject constructor(
        private val context: Context,
        coroutineScope: CoroutineScope,
        private val calendarFlutterBridge: CalendarFlutterBridge
) : PebbleMessageHandler {
    val calendarDebouncer = Debouncer(debouncingTimeMs = 1_000, coroutineScope)

    private val contentObserver = object : ContentObserver(null) {
        override fun onChange(selfChange: Boolean, uri: Uri?) {
            // Android seems to fire multiple calendar notifications at the same time.
            // Use debouncer to wait for the last change and then trigger sync
            calendarDebouncer.executeDebouncing {
                calendarFlutterBridge.syncCalendar()
            }
        }
    }

    init {
        coroutineScope.launch {
            // We were not receiving any calendar changes when service was offline.
            // Sync calendar at startup

            calendarFlutterBridge.syncCalendar()
        }

        observeCalendarChanges()

        coroutineScope.coroutineContext.job.invokeOnCompletion {
            context.contentResolver.unregisterContentObserver(contentObserver)
        }
    }

    private fun observeCalendarChanges() {
        context.contentResolver.registerContentObserver(
                CalendarContract.Instances.CONTENT_URI, true, contentObserver
        )
    }

}