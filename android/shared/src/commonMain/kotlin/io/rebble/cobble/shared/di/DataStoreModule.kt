package io.rebble.cobble.shared.di

import io.rebble.cobble.shared.datastore.KMPPrefs
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val dataStoreModule = module {
    singleOf(::KMPPrefs)
}