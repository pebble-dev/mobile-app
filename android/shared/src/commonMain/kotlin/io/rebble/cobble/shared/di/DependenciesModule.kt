package io.rebble.cobble.shared.di

import io.ktor.client.HttpClient
import io.ktor.client.plugins.cache.HttpCache
import io.ktor.client.plugins.cache.storage.CacheStorage
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import io.rebble.cobble.shared.PlatformContext
import io.rebble.cobble.shared.errors.GlobalExceptionHandler
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import org.koin.core.qualifier.named
import org.koin.dsl.module

val dependenciesModule = module {
    factory {
        HttpClient {
            install(HttpCache) {
                publicStorage(makePlatformCacheStorage(get()))
            }
            install(ContentNegotiation) {
                json()
            }
        }
    }

    single { GlobalExceptionHandler() }

    factory<CoroutineDispatcher>(qualifier = named("io_dispatcher")) {
        Dispatchers.IO
    }
}

expect fun makePlatformCacheStorage(platformContext: PlatformContext): CacheStorage