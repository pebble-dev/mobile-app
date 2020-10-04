package io.rebble.fossil

import android.content.Intent
import androidx.core.content.ContextCompat
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

        ContextCompat.startForegroundService(this, Intent(this, WatchService::class.java))
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