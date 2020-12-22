package io.rebble.cobble.bridges.background

import io.rebble.cobble.bridges.FlutterBridge
import io.rebble.cobble.pigeons.Pigeons
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@Singleton
class CalendarFlutterBridge @Inject constructor(
        private val flutterBackgroundController: FlutterBackgroundController
) : FlutterBridge {
    private var cachedCalendarCallbacks: Pigeons.CalendarCallbacks? = null

    suspend fun syncCalendar(): Boolean {
        val calendarCallbacks = getCalendarCallbacks() ?: return false
        suspendCoroutine<Unit> { continuation ->
            calendarCallbacks.doFullCalendarSync {
                continuation.resume(Unit)
            }
        }

        return true
    }

    suspend fun deleteCalendarPinsFromWatch(): Boolean {
        val calendarCallbacks = getCalendarCallbacks() ?: return false
        suspendCoroutine<Unit> { continuation ->
            calendarCallbacks.deleteCalendarPinsFromWatch {
                continuation.resume(Unit)
            }
        }

        return true
    }

    private suspend fun getCalendarCallbacks(): Pigeons.CalendarCallbacks? {
        val cachedCalendarCallbacks = cachedCalendarCallbacks
        if (cachedCalendarCallbacks != null) {
            return cachedCalendarCallbacks
        }

        val flutterEngine = flutterBackgroundController.getBackgroundFlutterEngine() ?: return null
        return Pigeons.CalendarCallbacks(flutterEngine.dartExecutor.binaryMessenger)
                .also { this.cachedCalendarCallbacks = it }
    }
}