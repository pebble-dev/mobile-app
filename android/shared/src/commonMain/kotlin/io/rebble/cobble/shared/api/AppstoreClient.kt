package io.rebble.cobble.shared.api

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.*
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import io.rebble.cobble.shared.domain.api.appstore.LockerEntry

class AppstoreClient(
        val baseUrl: String,
        private val token: String,
        engine: HttpClientEngine? = null,
) {
    private val version = "v1"
    private val client = engine?.let { HttpClient(it) {
        install(ContentNegotiation) {
            json()
        }
    }} ?: HttpClient {
        install(ContentNegotiation) {
            json()
        }
    }

    suspend fun getLocker(): List<LockerEntry> {
        val body: Map<String, List<LockerEntry>> = client.get("$baseUrl/$version/locker") {
            headers {
                append(HttpHeaders.Accept, "application/json")
                append(HttpHeaders.Authorization, "Bearer $token")
            }
        }.body()
        return body["applications"] ?: emptyList()
    }

    suspend fun addToLocker(uuid: String) {
        client.put("$baseUrl/$version/locker/$uuid") {
            headers {
                append(HttpHeaders.Accept, "application/json")
                append(HttpHeaders.Authorization, "Bearer $token")
            }
        }
    }

    suspend fun removeFromLocker(uuid: String) {
        client.delete("$baseUrl/$version/locker/$uuid") {
            headers {
                append(HttpHeaders.Accept, "application/json")
                append(HttpHeaders.Authorization, "Bearer $token")
            }
        }
    }
}