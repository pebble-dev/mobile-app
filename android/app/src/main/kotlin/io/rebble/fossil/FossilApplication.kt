package io.rebble.fossil

import android.app.Application
import io.flutter.app.FlutterApplication
import io.rebble.fossil.di.AppComponent
import io.rebble.fossil.di.AppModule
import io.rebble.fossil.di.DaggerAppComponent

class FossilApplication: FlutterApplication() {
    lateinit var component: AppComponent
        private set

    override fun onCreate() {
        component = DaggerAppComponent.factory().build(this)

        super.onCreate()
    }
}