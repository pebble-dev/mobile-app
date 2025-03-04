package io.rebble.cobble.shared.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.rebble.cobble.shared.api.RWS
import io.rebble.cobble.shared.domain.api.auth.RWSAccount
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SettingsAccountCardViewModel(rws: RWS) : ViewModel() {
    sealed interface State {
        data object Loading : State
        data object SignedOut : State
        data object UntrustedBootUrl : State
        data class SignedIn(
                val accountName: String,
                val weatherVoiceSubscriptionStatus: String,
                val timelineSyncInterval: String,
        ) : State
    }

    sealed interface Action {
        data object SignIn: Action
        data object SignOut : Action
        data object ManageAccount : Action
        data object Reset : Action
        data object CopyUrl : Action
    }

    private val authClient = rws.authClient
    private val _state = MutableStateFlow<State>(State.Loading)

    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            if (authClient == null) {
                _state.update { State.SignedOut }
                return@launch
            }

            try {
                val user = authClient.getCurrentAccount()
                _state.update { user.toState() }
            } catch (e: IllegalStateException) {
                _state.update {
                    State.UntrustedBootUrl
                }
            }
        }
    }

    fun onAction(action: Action) {
        println(action)
//        when (action) {
//            Action.CopyUrl -> TODO()
//            Action.ManageAccount -> TODO()
//            Action.Reset -> TODO()
//            Action.SignOut -> TODO()
//            Action.SignIn -> TODO()
//        }
    }

    private fun RWSAccount.toState(): State.SignedIn = State.SignedIn(
            accountName = name,
            weatherVoiceSubscriptionStatus = if (isSubscribed) "Subscribed!" else "Not Subscribed",
            timelineSyncInterval = if (hasTimeline) "Every $timelineTtl minutes" else "Every 2 hours"
    )
}