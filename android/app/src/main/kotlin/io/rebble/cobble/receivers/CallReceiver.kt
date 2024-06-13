package io.rebble.cobble.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.ContactsContract
import android.telecom.Call
import android.telephony.TelephonyManager
import io.rebble.cobble.CobbleApplication
import io.rebble.libpebblecommon.packets.PhoneControl
import io.rebble.libpebblecommon.services.PhoneControlService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.actor
import timber.log.Timber
import kotlin.random.Random
import kotlin.random.nextUInt

class CallReceiver: BroadcastReceiver() {
    private var lastCookie: UInt? = null

    sealed class PhoneState {
        data class IncomingCall(val cookie: UInt, val number: String?, val contactName: String?): PhoneState()
        data class OutgoingCall(val cookie: UInt, val number: String?, val contactName: String?): PhoneState()
        data object CallReceived: PhoneState()
        data object CallEnded: PhoneState()
    }

    lateinit var phoneControlService: PhoneControlService

    private val phoneStateChangeActor = GlobalScope.actor<PhoneState> {
        for (state in channel) {
            Timber.d("Phone state changed: $state")
            when (state) {
                is PhoneState.IncomingCall -> {
                    // Incoming call
                    val cookie = state.cookie
                    val incomingNumber = state.number
                    val contactName = state.contactName
                    lastCookie = cookie
                    phoneControlService.send(
                            PhoneControl.IncomingCall(
                                    cookie,
                                    incomingNumber ?: "Unknown",
                                    contactName ?: "",
                            )
                    )
                }
                is PhoneState.OutgoingCall -> {
                    // Outgoing call
                    // Needs implementing when firmware supports it
                }
                is PhoneState.CallReceived -> {
                    // Call received
                    lastCookie?.let {
                        phoneControlService.send(PhoneControl.Start(it))
                    }
                }
                is PhoneState.CallEnded -> {
                    // Call ended
                    lastCookie?.let {
                        phoneControlService.send(PhoneControl.End(it))
                        lastCookie = null
                    }
                }
            }
        }
    }
    override fun onReceive(context: Context?, intent: Intent?) {
        val injectionComponent = (context!!.applicationContext as CobbleApplication).component
        phoneControlService = injectionComponent.createPhoneControlService()

        
        when (intent?.action) {
            TelephonyManager.ACTION_PHONE_STATE_CHANGED -> {
                val state = intent?.getStringExtra(TelephonyManager.EXTRA_STATE)
                val number = intent?.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)
                val contactName = number?.let { getContactName(context, it) }

                when (state) {
                    TelephonyManager.EXTRA_STATE_RINGING -> {
                        phoneStateChangeActor.trySend(PhoneState.IncomingCall(Random.nextUInt(), number, contactName))
                    }
                    TelephonyManager.EXTRA_STATE_OFFHOOK -> {
                        phoneStateChangeActor.trySend(PhoneState.CallReceived)
                    }
                    TelephonyManager.EXTRA_STATE_IDLE -> {
                        phoneStateChangeActor.trySend(PhoneState.CallEnded)
                    }
                }
            }
            Intent.ACTION_NEW_OUTGOING_CALL -> {
                val number = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER)
                val contactName = number?.let { getContactName(context, it) }
                phoneStateChangeActor.trySend(PhoneState.OutgoingCall(Random.nextUInt(), number, contactName))
            }
        }
       
    }

    private fun getContactName(context: Context, number: String): String? {
        val cursor = context.contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                arrayOf(
                        ContactsContract.Contacts.DISPLAY_NAME,
                        ContactsContract.CommonDataKinds.Phone.NUMBER
                ),
                ContactsContract.CommonDataKinds.Phone.NUMBER + " = ?",
                arrayOf(number),
                null
        )
        val name = cursor?.use {
            if (it.moveToFirst()) {
                it.getString(it.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME))
            } else {
                null
            }
        }
        return name
    }
}