package io.rebble.cobble.shared.domain.store

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class PebbleBridge {
    companion object {
        internal val json =
            Json {
                ignoreUnknownKeys = true
                encodeDefaults = true
            }

        internal val className = "PebbleBridge"

        fun searchRequest(
            query: String,
            section: String
        ): String {
            val request = PebbleBridgeSearchRequest(query = query, section = section)
            return createRequest(request)
        }

        fun navigateRequest(url: String): String {
            val request = PebbleBridgeNavigateRequest(url = url)
            return createRequest(request)
        }

        fun refreshRequest(): String {
            val request = PebbleBridgeRefreshRequest()
            return createRequest(request)
        }

        fun lockerResponse(
            callbackId: Int,
            addedToLocker: Boolean
        ): String {
            val response = PebbleBridgeLockerResponse(addedToLocker)
            return createResponse(callbackId, response)
        }

        private inline fun <reified T> createRequest(request: T): String {
            val requestJson = json.encodeToString(request)
            return "$className.handleRequest($requestJson)"
        }

        private inline fun <reified T> createResponse(
            callbackId: Int,
            response: T
        ): String {
            val responseData = PebbleBridgeResponse(callbackId, response)
            val responseJson = json.encodeToString(responseData)
            return "$className.handleResponse($responseJson)"
        }
    }
}

@Serializable
data class PebbleBridgeResponse<T>(
    val callbackId: Int,
    val data: T
)

@Serializable
data class PebbleBridgeLockerResponse(
    @SerialName("added_to_locker")
    val addedToLocker: Boolean
)

@Serializable
data class PebbleBridgeSearchRequest(
    val methodName: String = "search",
    val query: String,
    val section: String
)

@Serializable
data class PebbleBridgeNavigateRequest(
    val methodName: String = "navigate",
    val url: String
)

@Serializable
data class PebbleBridgeRefreshRequest(
    val methodName: String = "refresh"
)