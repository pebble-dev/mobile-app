package io.rebble.cobble.shared.di

import io.rebble.cobble.shared.datastore.SecureStorage
import io.rebble.cobble.shared.domain.state.ConnectionState
import io.rebble.cobble.shared.domain.state.CurrentToken
import io.rebble.cobble.shared.domain.state.CurrentToken.LoggedOut.tokenOrNull
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module

val stateModule = module {
    single(named("connectionState")) {
        MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    } bind StateFlow::class
    factory(named("isConnected")) {
        get<StateFlow<ConnectionState>>(named("connectionState"))
                .map { it is ConnectionState.Connected }
                .stateIn(CoroutineScope(Dispatchers.Default), SharingStarted.WhileSubscribed(), false)
    }

    single(named("currentToken")) {
        val token = get<SecureStorage>().token
        MutableStateFlow(token?.let { CurrentToken.LoggedIn(token) } ?: CurrentToken.LoggedOut)
    } bind StateFlow::class
}