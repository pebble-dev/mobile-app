package io.rebble.cobble.di

import androidx.lifecycle.Lifecycle
import dagger.Module
import dagger.Provides
import io.rebble.cobble.MainActivity
import kotlinx.coroutines.CoroutineScope

@Module
class ActivityModule {
    @Provides
    fun provideLifecycle(mainActivity: MainActivity): Lifecycle {
        return mainActivity.lifecycle
    }

    @Provides
    fun provideCoroutineScope(mainActivity: MainActivity): CoroutineScope {
        return mainActivity.coroutineScope
    }
}