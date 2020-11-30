package io.rebble.cobble

import io.flutter.app.FlutterApplication
import io.rebble.cobble.di.AppComponent
import io.rebble.cobble.di.DaggerAppComponent

class CobbleApplication : FlutterApplication() {
    lateinit var component: AppComponent
        private set

    override fun onCreate() {
        component = DaggerAppComponent.factory().build(this)

        super.onCreate()
    }
}