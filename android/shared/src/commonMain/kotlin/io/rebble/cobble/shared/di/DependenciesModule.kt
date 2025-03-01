package io.rebble.cobble.shared.di

import io.ktor.client.HttpClient
import io.ktor.client.plugins.cache.HttpCache
import io.ktor.client.plugins.cache.storage.CacheStorage
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import io.rebble.cobble.shared.PlatformContext
import io.rebble.cobble.shared.errors.GlobalExceptionHandler
import org.koin.dsl.module

val dependenciesModule =
    module {
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
    }

expect fun makePlatformCacheStorage(platformContext: PlatformContext): CacheStorage