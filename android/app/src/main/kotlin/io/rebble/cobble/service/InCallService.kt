package io.rebble.cobble.service

import android.content.ContentResolver
import android.os.Build
import android.provider.ContactsContract
import android.provider.ContactsContract.Contacts
import android.telecom.Call
import android.telecom.InCallService
import android.telecom.VideoProfile
import io.rebble.cobble.CobbleApplication
import io.rebble.cobble.bluetooth.ConnectionLooper
import io.rebble.cobble.shared.Logging
import io.rebble.cobble.shared.domain.notifications.CallNotificationProcessor
import io.rebble.cobble.shared.domain.state.ConnectionState
import io.rebble.cobble.shared.domain.state.ConnectionStateManager
import io.rebble.cobble.shared.domain.state.watchOrNull
import io.rebble.libpebblecommon.packets.PhoneControl
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import timber.log.Timber
import kotlin.random.Random

class InCallService : InCallService() {
    private lateinit var coroutineScope: CoroutineScope
    private lateinit var connectionLooper: ConnectionLooper
    private lateinit var contentResolver: ContentResolver
    private lateinit var callNotificationProcessor: CallNotificationProcessor

    private var lastCookie: UInt? = null
    private var lastCall: Call? = null

    override fun onCreate() {
        super.onCreate()
        Timber.d("InCallService created")
        val injectionComponent = (applicationContext as CobbleApplication).component
        connectionLooper = injectionComponent.createConnectionLooper()
        coroutineScope =
            CoroutineScope(
                SupervisorJob() + injectionComponent.createExceptionHandler()
            )
        contentResolver = applicationContext.contentResolver
        listenForPhoneControlMessages()
    }

    private fun listenForPhoneControlMessages() {
        ConnectionStateManager.connectionState.filterIsInstance<ConnectionState.Connected>().onEach {
            val pebbleDevice = it.watch
            val connectionScope = pebbleDevice.connectionScope.filterNotNull().first()
            val phoneControlService = pebbleDevice.phoneControlService
            phoneControlService.receivedMessages.receiveAsFlow().onEach {
                if (connectionLooper.connectionState.value !is ConnectionState.Connected) {
                    Timber.w("Ignoring phone control message because watch is not connected")
                    return@onEach
                }
                when (it) {
                    is PhoneControl.Answer -> {
                        synchronized(this@InCallService) {
                            if (it.cookie.get() == lastCookie) {
                                lastCall?.answer(
                                    VideoProfile.STATE_AUDIO_ONLY
                                ) // Answering from watch probably means a headset or something
                            } else {
                                callNotificationProcessor.handleCallAction(it)
                            }
                        }
                    }

                    is PhoneControl.Hangup -> {
                        synchronized(this@InCallService) {
                            if (it.cookie.get() == lastCookie) {
                                lastCookie = null
                                lastCall?.let { call ->
                                    if (call.state == Call.STATE_RINGING) {
                                        Timber.d("Rejecting ringing call")
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                            call.reject(Call.REJECT_REASON_DECLINED)
                                        } else {
                                            call.reject(false, null)
                                        }
                                    } else {
                                        Timber.d("Disconnecting call")
                                        call.disconnect()
                                    }
                                }
                            } else {
                                callNotificationProcessor.handleCallAction(it)
                            }
                        }
                    }

                    else -> {
                        Timber.w("Unhandled phone control message: $it")
                    }
                }
            }.launchIn(connectionScope)
        }
    }

    override fun onDestroy() {
        Timber.d("InCallService destroyed")
        coroutineScope.cancel()
        super.onDestroy()
    }

    override fun onCallAdded(call: Call) {
        super.onCallAdded(call)
        Timber.d("Call added")
        coroutineScope.launch(Dispatchers.IO) {
            val pebbleDevice = ConnectionStateManager.connectionState.value.watchOrNull
            val phoneControlService =
                pebbleDevice?.phoneControlService ?: run {
                    Logging.w("Phone control service not available (e.g. device disconnected)")
                    return@launch
                }
            synchronized(this@InCallService) {
                if (lastCookie != null) {
                    lastCookie =
                        if (lastCall == null) {
                            null
                        } else {
                            if (lastCall?.state == Call.STATE_DISCONNECTED) {
                                null
                            } else {
                                Timber.w("Ignoring call because there is already a call in progress")
                                return@launch
                            }
                        }
                }
                lastCall = call
            }
            val cookie = Random.nextInt().toUInt() or 0xCAu // Magic number for phone call to differentiate from third-party calls
            synchronized(this@InCallService) {
                lastCookie = cookie
            }
            if (call.state == Call.STATE_RINGING) {
                coroutineScope.launch(Dispatchers.IO) {
                    phoneControlService.send(
                        PhoneControl.IncomingCall(
                            cookie,
                            getPhoneNumber(call),
                            getContactName(call)
                        )
                    )
                }
            }
            if (connectionLooper.connectionState.value is ConnectionState.Connected) {
                withContext(Dispatchers.Main) {
                    call.registerCallback(
                        object : Call.Callback() {
                            override fun onStateChanged(
                                call: Call,
                                state: Int
                            ) {
                                super.onStateChanged(call, state)
                                Timber.d("Call state changed to $state")
                                synchronized(this@InCallService) {
                                    if (lastCookie != cookie) {
                                        Timber.w(
                                            "Ignoring incoming call ring because it's not the last call"
                                        )
                                        call.unregisterCallback(this)
                                        return
                                    }
                                }
                                when (state) {
                                    Call.STATE_ACTIVE -> {
                                        coroutineScope.launch(Dispatchers.IO) {
                                            phoneControlService.send(
                                                PhoneControl.Start(
                                                    cookie
                                                )
                                            )
                                        }
                                    }

                                    Call.STATE_DISCONNECTED -> {
                                        synchronized(this@InCallService) {
                                            if (lastCookie == cookie) {
                                                lastCookie = null
                                            }
                                        }
                                        coroutineScope.launch(Dispatchers.IO) {
                                            phoneControlService.send(
                                                PhoneControl.End(
                                                    cookie
                                                )
                                            )
                                        }
                                        call.unregisterCallback(this)
                                    }
                                }
                            }
                        }
                    )
                }
            }
        }
    }

    private fun getPhoneNumber(call: Call): String {
        return call.details.handle.schemeSpecificPart
    }

    private fun getContactName(call: Call): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            call.details.contactDisplayName ?: call.details.handle.schemeSpecificPart
        } else {
            val cursor =
                contentResolver.query(
                    Contacts.CONTENT_URI,
                    arrayOf(Contacts.DISPLAY_NAME),
                    Contacts.HAS_PHONE_NUMBER + " = 1 AND " + ContactsContract.CommonDataKinds.Phone.NUMBER + " = ?",
                    arrayOf(call.details.handle.schemeSpecificPart),
                    null
                )
            val name =
                cursor?.use {
                    if (it.moveToFirst()) {
                        it.getString(it.getColumnIndexOrThrow(Contacts.DISPLAY_NAME))
                    } else {
                        null
                    }
                }
            return name ?: call.details.handle.schemeSpecificPart
        }
    }

    override fun onCallRemoved(call: Call) {
        super.onCallRemoved(call)
        Timber.d("Call removed")
        coroutineScope.launch(Dispatchers.IO) {
            val pebbleDevice = ConnectionStateManager.connectionState.value.watchOrNull
            val phoneControlService =
                pebbleDevice?.phoneControlService ?: run {
                    Logging.w("Phone control service not available (e.g. device disconnected)")
                    return@launch
                }
            val cookie =
                synchronized(this@InCallService) {
                    val c = lastCookie ?: return@launch
                    lastCookie = null
                    c
                }
            if (connectionLooper.connectionState.value is ConnectionState.Connected) {
                phoneControlService.send(
                    PhoneControl.End(
                        cookie
                    )
                )
            }
        }
    }
}