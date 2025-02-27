package io.rebble.cobble.shared.api

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.http.HttpHeaders
import io.rebble.cobble.shared.domain.api.auth.RWSAccount
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class AuthClient(
        val baseUrl: String,
        private val token: String
): KoinComponent {
    private val version = "v1"
    private val client: HttpClient by inject()

    suspend fun getCurrentAccount(): RWSAccount {
        val res = client.get("$baseUrl/$version/me") {
            headers {
                append(HttpHeaders.Accept, "application/json")
                append(HttpHeaders.Authorization, "Bearer $token")
            }
        }

        if (res.status.value != 200) {
            error("Failed to get account: ${res.status}")
        }

        return res.body() ?: error("Failed to deserialize account")
    }
}
