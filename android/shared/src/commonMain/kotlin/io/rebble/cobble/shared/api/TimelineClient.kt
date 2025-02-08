package io.rebble.cobble.shared.api

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.http.HttpHeaders
import io.rebble.cobble.shared.domain.api.timeline.TimelineTokenResponse
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class TimelineClient(
        val syncBaseUrl: String,
        private val token: String
): KoinComponent {
    private val client: HttpClient by inject()
    private val version = "v1"
    suspend fun getSandboxUserToken(uuid: String): String {
        val res = client.get("$syncBaseUrl/$version/tokens/sandbox/$uuid") {
            headers {
                append(HttpHeaders.Accept, "application/json")
                append(HttpHeaders.Authorization, "Bearer $token")
            }
        }
        if (res.status.value != 200) {
            error("Failed to get sandbox user token: ${res.status}")
        }
        val body: TimelineTokenResponse = res.body()
        return body.token
    }
}