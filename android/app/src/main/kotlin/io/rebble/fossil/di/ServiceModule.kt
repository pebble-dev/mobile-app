package io.rebble.fossil.di

import androidx.lifecycle.lifecycleScope
import dagger.Module
import dagger.Provides
import io.rebble.fossil.WatchService
import kotlinx.coroutines.CoroutineScope

@Module
class ServiceModule {
    @Provides
    fun provideCoroutineScope(watchService: WatchService): CoroutineScope {
        return watchService.lifecycleScope
    }
}