package io.rebble.cobble.di

import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import dagger.Binds
import dagger.Module
import dagger.Provides
import io.rebble.cobble.errors.GlobalExceptionHandler
import kotlinx.coroutines.CoroutineExceptionHandler

@Module(
        subcomponents = [BackgroundFlutterSubcomponent::class]
)
abstract class AppModule {
    @Binds
    abstract fun bindContext(application: Application): Context

    @Binds
    abstract fun bindCoroutineExceptionHandler(
            globalExceptionHandler: GlobalExceptionHandler
    ): CoroutineExceptionHandler

    @Module
    companion object {
        @Provides
        fun providePackageManager(context: Context): PackageManager {
            return context.packageManager
        }
    }
}