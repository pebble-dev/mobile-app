package io.rebble.cobble.service

import android.content.ContentResolver
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.provider.ContactsContract
import android.provider.ContactsContract.Contacts
import android.telecom.Call
import android.telecom.InCallService
import android.telecom.VideoProfile
import io.rebble.cobble.CobbleApplication
import io.rebble.cobble.bluetooth.ConnectionLooper
import io.rebble.cobble.bluetooth.ConnectionState
import io.rebble.libpebblecommon.packets.PhoneControl
import io.rebble.libpebblecommon.services.PhoneControlService
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import timber.log.Timber
import kotlin.random.Random

class InCallService: InCallService() {
    private lateinit var coroutineScope: CoroutineScope
    private lateinit var phoneControlService: PhoneControlService
    private lateinit var connectionLooper: ConnectionLooper
    private lateinit var contentResolver: ContentResolver

    private var lastCookie: UInt? = null
    private var lastCall: Call? = null

    override fun onCreate() {
        super.onCreate()
        Timber.d("InCallService created")
        val injectionComponent = (applicationContext as CobbleApplication).component
        phoneControlService = injectionComponent.createPhoneControlService()
        connectionLooper = injectionComponent.createConnectionLooper()
        coroutineScope = CoroutineScope(
                SupervisorJob() + injectionComponent.createExceptionHandler()
        )
        contentResolver = applicationContext.contentResolver
        listenForPhoneControlMessages()
    }

    private fun listenForPhoneControlMessages() {
        phoneControlService.receivedMessages.receiveAsFlow().onEach {
            if (connectionLooper.connectionState.value !is ConnectionState.Connected) {
                Timber.w("Ignoring phone control message because watch is not connected")
                return@onEach
            }
            when (it) {
                is PhoneControl.Answer -> {
                    synchronized(this@InCallService) {
                        if (it.cookie.get() == lastCookie) {
                            lastCall?.answer(VideoProfile.STATE_AUDIO_ONLY) // Answering from watch probably means a headset or something
                        }
                    }
                }
                is PhoneControl.Hangup -> {
                    synchronized(this@InCallService) {
                        if (it.cookie.get() == lastCookie) {
                            lastCookie = null
                            lastCall?.let { call ->
                                if (call.details.state == Call.STATE_RINGING) {
                                    Timber.d("Rejecting ringing call")
                                    call.reject(Call.REJECT_REASON_DECLINED)
                                } else {
                                    Timber.d("Disconnecting call")
                                    call.disconnect()
                                }
                            }
                        }
                    }
                }
                else -> {
                    Timber.w("Unhandled phone control message: $it")
                }
            }
        }.launchIn(coroutineScope)
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
            synchronized(this@InCallService) {
                if (lastCookie != null) {
                    lastCookie = if (lastCall == null) {
                        null
                    } else {
                        if (lastCall?.details?.state == Call.STATE_DISCONNECTED) {
                            null
                        } else {
                            Timber.w("Ignoring call because there is already a call in progress")
                            return@launch
                        }
                    }
                }
                lastCall = call
            }
            val cookie = Random.nextInt().toUInt()
            synchronized(this@InCallService) {
                lastCookie = cookie
            }
            if (call.details.state == Call.STATE_RINGING) {
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
                    call.registerCallback(object : Call.Callback() {
                        override fun onStateChanged(call: Call, state: Int) {
                            super.onStateChanged(call, state)
                            Timber.d("Call state changed to $state")
                            synchronized(this@InCallService) {
                                if (lastCookie != cookie) {
                                    Timber.w("Ignoring incoming call ring because it's not the last call")
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
                    })
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
            val cursor = contentResolver.query(
                    Contacts.CONTENT_URI,
                    arrayOf(Contacts.DISPLAY_NAME),
                    Contacts.HAS_PHONE_NUMBER + " = 1 AND " + ContactsContract.CommonDataKinds.Phone.NUMBER + " = ?",
                    arrayOf(call.details.handle.schemeSpecificPart),
                    null
            )
            val name = cursor?.use {
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
            val cookie = synchronized(this@InCallService) {
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