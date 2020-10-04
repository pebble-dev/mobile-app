package io.rebble.fossil

import android.content.Intent
import androidx.core.content.ContextCompat
import io.flutter.app.FlutterApplication
import io.rebble.fossil.di.AppComponent
import io.rebble.fossil.di.DaggerAppComponent
import io.rebble.fossil.log.AppTaggedDebugTree
import io.rebble.fossil.log.FileLoggingTree
import timber.log.Timber

class FossilApplication : FlutterApplication() {
    lateinit var component: AppComponent
        private set

    override fun onCreate() {
        component = DaggerAppComponent.factory().build(this)

        super.onCreate()

        Timber.plant(AppTaggedDebugTree("Fossil"))
        Timber.plant(FileLoggingTree(this, "Fossil"))

        ContextCompat.startForegroundService(this, Intent(this, WatchService::class.java))
    }
}