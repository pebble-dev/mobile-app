package io.rebble.fossil.bridges

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import io.flutter.plugin.common.BasicMessageChannel
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.StandardMessageCodec
import io.rebble.fossil.MainActivity
import io.rebble.fossil.pigeons.BooleanWrapper
import io.rebble.fossil.pigeons.toMapExt
import io.rebble.fossil.util.setCoroutineMessageHandler
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import java.util.*
import javax.inject.Inject

class AppLifecycle @Inject constructor(
        binaryMessenger: BinaryMessenger,
        mainActivity: MainActivity,
        coroutineScope: CoroutineScope,
        lifecycle: Lifecycle
) : FlutterBridge {
    private val bootTrigger = CompletableDeferred<Boolean>()

    init {
        mainActivity.bootIntentCallback = {
            bootTrigger.complete(it)
            mainActivity.bootIntentCallback = null
        }

        // Regular pigeon does not support async methods
        // Reply manually until https://github.com/flutter/flutter/issues/60399 is resolved
        // Following code is just a modified copy from Pigeons.java to reply asynchronously
        val channel = BasicMessageChannel(
                binaryMessenger,
                "dev.flutter.pigeon.AppLifecycleControl.waitForBoot",
                StandardMessageCodec()
        )
        channel.setCoroutineMessageHandler(coroutineScope) { _ ->
            val result = bootTrigger.await()

            val wrapped = HashMap<String, HashMap<*, *>>()
            val output = BooleanWrapper(result)
            wrapped["result"] = output.toMapExt()
            wrapped
        }

        lifecycle.addObserver(object : LifecycleEventObserver {
            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                if (event == Lifecycle.Event.ON_DESTROY) {
                    channel.setMessageHandler(null)
                }
            }
        })
    }
}