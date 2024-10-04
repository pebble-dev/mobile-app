package io.rebble.cobble.shared.api

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.*
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import io.rebble.cobble.shared.domain.api.appstore.LockerEntry
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class AppstoreClient(
        val baseUrl: String,
        private val token: String
): KoinComponent {
    private val version = "v1"
    private val client: HttpClient by inject()

    suspend fun getLocker(): List<LockerEntry> {
        val res = client.get("$baseUrl/$version/locker") {
            headers {
                append(HttpHeaders.Accept, "application/json")
                append(HttpHeaders.Authorization, "Bearer $token")
            }
        }

        if (res.status.value != 200) {
            error("Failed to get locker: ${res.status}")
        }

        val body: Map<String, List<LockerEntry>> = res.body() ?: emptyMap()
        return body["applications"] ?: emptyList()
    }

    suspend fun addToLocker(uuid: String) {
        val res = client.put("$baseUrl/$version/locker/$uuid") {
            headers {
                append(HttpHeaders.Accept, "application/json")
                append(HttpHeaders.Authorization, "Bearer $token")
            }
        }

        if (res.status.value != 200) {
            error("Failed to add to locker: ${res.status}")
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