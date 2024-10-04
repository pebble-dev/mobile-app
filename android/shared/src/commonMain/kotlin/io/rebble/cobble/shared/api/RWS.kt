package io.rebble.cobble.shared.api

import io.rebble.cobble.shared.domain.state.CurrentToken
import io.rebble.cobble.shared.domain.state.CurrentToken.LoggedOut.tokenOrNull
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.qualifier.named

object RWS: KoinComponent {
    private val domainSuffix = "rebble.io"
    private val token: StateFlow<CurrentToken> by inject(named("currentToken"))
    private val scope = CoroutineScope(Dispatchers.Default)

    private val _appstoreClient = token.map {
        it.tokenOrNull?.let { t -> AppstoreClient("https://appstore-api.$domainSuffix/api", t) }
    }.stateIn(scope, SharingStarted.Eagerly, null)
    private val _authClient = token.map {
        it.tokenOrNull?.let { t -> AuthClient("https://auth.$domainSuffix/api", t) }
    }.stateIn(scope, SharingStarted.Eagerly, null)
    val appstoreClient: AppstoreClient?
        get() = _appstoreClient.value
    val authClient: AuthClient?
        get() = _authClient.value
}