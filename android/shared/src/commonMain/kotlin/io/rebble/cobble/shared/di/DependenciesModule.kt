package io.rebble.cobble.shared.di

import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import org.koin.dsl.module

val dependenciesModule = module {
    factory {
        HttpClient {
            install(ContentNegotiation) {
                json()
            }
        }
    }
}