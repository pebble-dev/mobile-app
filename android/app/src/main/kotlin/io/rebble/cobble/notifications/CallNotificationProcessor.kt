package io.rebble.cobble.notifications

import android.service.notification.StatusBarNotification
import io.rebble.cobble.errors.GlobalExceptionHandler
import io.rebble.cobble.shared.Logging
import io.rebble.cobble.shared.datastore.KMPPrefs
import io.rebble.cobble.shared.domain.notifications.calls.CallNotification
import io.rebble.cobble.shared.domain.notifications.calls.CallNotificationType
import io.rebble.cobble.shared.domain.notifications.calls.DiscordCallNotificationInterpreter
import io.rebble.cobble.shared.domain.notifications.calls.WhatsAppCallNotificationInterpreter
import io.rebble.cobble.shared.domain.state.ConnectionState
import io.rebble.libpebblecommon.packets.PhoneControl
import io.rebble.libpebblecommon.services.PhoneControlService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.random.Random
import io.rebble.cobble.bluetooth.ConnectionLooper
import javax.inject.Singleton

@Singleton
class CallNotificationProcessor @Inject constructor(
        exceptionHandler: GlobalExceptionHandler,
        private val prefs: KMPPrefs,
        private val phoneControl: PhoneControlService,
        private val connectionLooper: ConnectionLooper
) {
    val coroutineScope = CoroutineScope(
            SupervisorJob() + exceptionHandler
    )

    open class CallState(val cookie: UInt?) {
        object IDLE : CallState(null)
        class RINGING(val notification: CallNotification, cookie: UInt?) : CallState(cookie) {
            override fun toString(): String {
                return "RINGING(cookie=$cookie, notification=$notification)"
            }
        }
        class ONGOING(val notification: CallNotification, cookie: UInt?) : CallState(cookie) {
            override fun toString(): String {
                return "ONGOING(cookie=$cookie, notification=$notification)"
            }
        }
    }

    private val callState = MutableStateFlow<CallState>(CallState.IDLE)

    init {
        var previousState = callState.value
        // Debounce to avoid condition where ring notif is dismissed and then ongoing notif appears
        callState.debounce(1000).onEach { state ->
            val sensitiveLogging = prefs.sensitiveDataLoggingEnabled.first()
            if (sensitiveLogging) {
                Logging.d("Call state changed to $state from $previousState")
            } else {
                Logging.d("Call state changed to ${state::class.simpleName} from ${previousState::class.simpleName}")
            }
            if (state is CallState.RINGING && previousState is CallState.IDLE) {
                state.cookie?.let {
                    Logging.d("Sending incoming call notification")
                    phoneControl.send(
                            PhoneControl.IncomingCall(
                                    it,
                                    state.notification.contactHandle ?: "Unknown",
                                    state.notification.contactName ?: ""
                            )
                    )
                } ?: run {
                    Logging.e("Ringing call state does not have a cookie")
                }
            } else if (state is CallState.IDLE && (previousState is CallState.ONGOING || previousState is CallState.RINGING)) {
                previousState.cookie?.let {
                    phoneControl.send(PhoneControl.End(it))
                } ?: run {
                    Logging.d("Previous call state does not have a cookie, not sending end call notification")
                }
            }
            previousState = state
        }.launchIn(coroutineScope)

    }

    companion object {
        private val callPackages = mapOf(
                "com.whatsapp" to WhatsAppCallNotificationInterpreter(),
                "com.discord" to DiscordCallNotificationInterpreter(),
        )
    }

    fun processCallNotification(sbn: StatusBarNotification) {
        if (sbn.packageName == "com.google.android.dialer" || sbn.packageName == "com.android.server.telecom") {
            return // Ignore system call notifications, we handle those with InCallService
        }
        val interpreter = callPackages[sbn.packageName] ?: run {
            Logging.d("Call notification from ${sbn.packageName} does not have an interpreter")
            return
        }
        coroutineScope.launch {
            val sensitiveLogging = prefs.sensitiveDataLoggingEnabled.first()
            if (sensitiveLogging) {
                Logging.d("Processing call notification from ${sbn.packageName} with actions: ${sbn.notification.actions.joinToString { it.title.toString() }}")
                Logging.d("Call Notification: ${sbn.notification}")
                Logging.d("Extras: ${sbn.notification.extras}")
                Logging.d("Actions: ${
                    sbn.notification.actions.joinToString(", ") {
                        buildString {
                            append("(")
                            append("Action: ${it.title}")
                            append(", Extras: ${it.extras.keySet().joinToString { key -> "$key: ${it.extras[key]}" }}")
                            append(", SemanticAction: ${it.semanticAction}")
                            append(")")
                        }
                    }
                }")
            } else {
                Logging.d("Processing call notification from ${sbn.packageName}")
            }

            val callNotification = interpreter.processCallNotification(sbn) ?: run {
                Logging.d("Call notification from ${sbn.packageName} was not recognized")
                return@launch
            }
            val nwCookie = Random.nextInt().toUInt() and 0xCAu.inv()
            synchronized(this@CallNotificationProcessor) {
                if (callState.value is CallState.IDLE && callNotification.type == CallNotificationType.RINGING) {
                    // Random number that does not end with 0xCA (magic number for phone call)
                    callState.value = CallState.RINGING(callNotification, nwCookie)
                } else if (callState.value !is CallState.ONGOING && callNotification.type == CallNotificationType.ONGOING) {
                    callState.value = CallState.ONGOING(callNotification, (callState.value as? CallState.RINGING)?.cookie ?: nwCookie)
                }
            }
        }
    }

    fun processCallNotificationDismissal(sbn: StatusBarNotification) {
        val interpreter = callPackages[sbn.packageName] ?: return
        synchronized(this@CallNotificationProcessor) {
            val state = callState.value
            val callNotification = interpreter.processCallNotification(sbn) ?: return
            Logging.d("Call notification dismissal from ${sbn.packageName}")
            callNotification.answer?.intentSender
            if (
                    (state is CallState.RINGING && state.notification.packageName == sbn.packageName) ||
                    (state is CallState.ONGOING && state.notification.packageName == sbn.packageName)
            ) {
                callState.value = CallState.IDLE
            }
        }
    }

    fun handleCallAction(action: PhoneControl) {
        if (connectionLooper.connectionState.value !is ConnectionState.Connected) {
            Logging.w("Ignoring phone control message because watch is not connected")
            return
        }
        when (action) {
            is PhoneControl.Answer -> {
                synchronized(this@CallNotificationProcessor) {
                    val state = callState.value as? CallState.RINGING ?: return
                    if (action.cookie.get() == state.cookie) {
                        Logging.d("Answering call")
                        state.notification.answer?.send() ?: run {
                            callState.value = CallState.IDLE
                            return@synchronized
                        }
                        callState.value = CallState.ONGOING(state.notification, state.cookie)
                    }
                }
            }

            is PhoneControl.Hangup -> {
                synchronized(this@CallNotificationProcessor) {
                    when (val state = callState.value) {
                        is CallState.RINGING -> {
                            if (action.cookie.get() == state.cookie) {
                                Logging.d("Rejecting ringing call")
                                state.notification.decline?.send()
                                callState.value = CallState.IDLE
                            }
                        }
                        is CallState.ONGOING -> {
                            if (action.cookie.get() == state.cookie) {
                                Logging.d("Disconnecting call")
                                state.notification.hangUp?.send()
                                callState.value = CallState.IDLE
                            }
                        }
                    }
                }
            }

            else -> {
                Logging.w("Unhandled phone control message: $action")
            }
        }
    }
}