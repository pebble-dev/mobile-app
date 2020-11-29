package io.rebble.cobble.di

import android.app.Application
import android.content.Context
import dagger.Binds
import dagger.Module
import io.rebble.cobble.errors.GlobalExceptionHandler
import kotlinx.coroutines.CoroutineExceptionHandler

@Module
abstract class AppModule {
    @Binds
    abstract fun bindContext(application: Application): Context

    @Binds
    abstract fun bindCoroutineExceptionHandler(
            globalExceptionHandler: GlobalExceptionHandler
    ): CoroutineExceptionHandler

    @Module
    companion object {
    }
}