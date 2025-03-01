package io.rebble.cobble.shared.api

import io.rebble.cobble.shared.domain.state.CurrentToken
import io.rebble.cobble.shared.domain.state.CurrentToken.LoggedOut.tokenOrNull
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.qualifier.named

object RWS: KoinComponent {
    private val domainSuffix = "rebble.io"
    private val token: StateFlow<CurrentToken> by inject(named("currentToken"))
    private val scope = CoroutineScope(Dispatchers.Default)

    val currentTokenFlow: StateFlow<CurrentToken> get() = token

    val appstoreClientFlow = token.map {
        it.tokenOrNull?.let { t -> AppstoreClient("https://appstore-api.$domainSuffix/api", t) }
    }.stateIn(scope, SharingStarted.Eagerly, null)
    val authClientFlow = token.map {
        it.tokenOrNull?.let { t -> AuthClient("https://auth.$domainSuffix/api", t) }
    }.stateIn(scope, SharingStarted.Eagerly, null)
    val timelineClientFlow = token.map {
        it.tokenOrNull?.let { t -> TimelineClient("https://timeline-sync.$domainSuffix", t) }
    }.stateIn(scope, SharingStarted.Eagerly, null)
    val appstoreClient: AppstoreClient?
        get() = appstoreClientFlow.value
    val authClient: AuthClient?
        get() = authClientFlow.value
    val timelineClient: TimelineClient?
        get() = timelineClientFlow.value
}
