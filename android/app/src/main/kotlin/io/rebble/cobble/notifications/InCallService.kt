package io.rebble.cobble.notifications

import android.content.ContentResolver
import android.os.Build
import android.provider.ContactsContract
import android.provider.ContactsContract.Contacts
import android.telecom.Call
import android.telecom.InCallService
import io.rebble.cobble.CobbleApplication
import io.rebble.cobble.bluetooth.ConnectionLooper
import io.rebble.cobble.bluetooth.ConnectionState
import io.rebble.libpebblecommon.packets.PhoneControl
import io.rebble.libpebblecommon.services.PhoneControlService
import io.rebble.libpebblecommon.services.notification.NotificationService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
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
        Timber.d("InCallService created")
        val injectionComponent = (applicationContext as CobbleApplication).component
        phoneControlService = injectionComponent.createPhoneControlService()
        connectionLooper = injectionComponent.createConnectionLooper()
        coroutineScope = CoroutineScope(
                SupervisorJob() + injectionComponent.createExceptionHandler()
        )
        contentResolver = applicationContext.contentResolver
        super.onCreate()
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
            val cookie = Random.nextInt().toUInt()
            synchronized(this@InCallService) {
                lastCookie = cookie
            }
            if (connectionLooper.connectionState.value is ConnectionState.Connected) {
                phoneControlService.send(
                        PhoneControl.IncomingCall(
                                cookie,
                                getPhoneNumber(call),
                                getContactName(call)
                        )
                )
                call.registerCallback(object : Call.Callback() {
                    override fun onStateChanged(call: Call, state: Int) {
                        super.onStateChanged(call, state)
                        Timber.d("Call state changed to $state")
                        if (state == Call.STATE_DISCONNECTED) {
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
                })
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