package io.rebble.cobble.shared.di

import io.rebble.cobble.shared.domain.state.ConnectionState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module

val stateModule = module {
    single(named("connectionState")) {
        MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    } bind StateFlow::class
}