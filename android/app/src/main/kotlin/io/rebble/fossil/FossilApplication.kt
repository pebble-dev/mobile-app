package io.rebble.fossil

import io.flutter.app.FlutterApplication
import io.rebble.fossil.di.AppComponent
import io.rebble.fossil.di.DaggerAppComponent
import io.rebble.fossil.log.AppTaggedDebugTree
import io.rebble.fossil.log.FileLoggingTree
import timber.log.Timber
import kotlin.system.exitProcess

class FossilApplication : FlutterApplication() {
    lateinit var component: AppComponent
        private set

    override fun onCreate() {
        component = DaggerAppComponent.factory().build(this)

        super.onCreate()

        initLogging()

        component.initNotificationChannels()
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
        Timber.plant(AppTaggedDebugTree("Fossil"))
        val fileLoggingTree = FileLoggingTree(this, "Fossil")
        Timber.plant(fileLoggingTree)

        Thread.setDefaultUncaughtExceptionHandler { _, e ->
            Timber.e(e, "CRASH")
            fileLoggingTree.waitForLogsToWrite()
            exitProcess(1)
        }
    }
}