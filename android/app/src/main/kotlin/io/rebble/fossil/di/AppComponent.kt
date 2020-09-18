package io.rebble.fossil.di

import android.app.Application
import dagger.BindsInstance
import dagger.Component

@Component(modules = [
    AppModule::class
])
interface AppComponent {
    @Component.Factory
    interface Factory {
        fun build(@BindsInstance application: Application): AppComponent
    }
}