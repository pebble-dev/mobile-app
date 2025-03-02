package io.rebble.cobble.shared.domain.state

open class CurrentToken {
    object LoggedOut : CurrentToken()

    data class LoggedIn(val token: String) : CurrentToken()

    val CurrentToken.tokenOrNull: String?
        get() =
            when (this) {
                is LoggedIn -> token
                else -> null
            }
}