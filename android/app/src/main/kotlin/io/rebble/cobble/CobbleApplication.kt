package io.rebble.cobble

import io.flutter.app.FlutterApplication
import io.rebble.cobble.di.AppComponent
import io.rebble.cobble.di.DaggerAppComponent
import io.rebble.cobble.log.AppTaggedDebugTree
import io.rebble.cobble.log.FileLoggingTree
import timber.log.Timber
import kotlin.system.exitProcess

class CobbleApplication : FlutterApplication() {
    lateinit var component: AppComponent
        private set

    override fun onCreate() {
        component = DaggerAppComponent.factory().build(this)

         super.onCreate()

        initLogging()

        component.initNotificationChannels()
        component.initLibPebbleCommonServices()

        beginConnectingToDefaultWatch()
    }

    private fun beginConnectingToDefaultWatch() {
        component.initServiceLifecycleControl()

        val macAddressOfDefaultPebble = component.createPairedStorage()
                .getMacAddressOfDefaultPebble()
        if (macAddressOfDefaultPebble != null) {
            component.createConnectionLooper().connectToWatch(macAddressOfDefaultPebble)
        }
    }

    private fun initLogging() {
        Timber.plant(AppTaggedDebugTree("Cobble"))
        val fileLoggingTree = FileLoggingTree(this, "Cobble")
        Timber.plant(fileLoggingTree)

        Thread.setDefaultUncaughtExceptionHandler { _, e ->
            Timber.e(e, "CRASH")
            fileLoggingTree.waitForLogsToWrite()
            exitProcess(1)
        }
    }
}
