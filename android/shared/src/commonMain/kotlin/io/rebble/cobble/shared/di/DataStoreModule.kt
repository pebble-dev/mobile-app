package io.rebble.cobble.shared.di

import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import io.rebble.cobble.shared.datastore.KMPPrefs
import io.rebble.cobble.shared.datastore.createDataStore

val dataStoreModule = module {
    singleOf(::KMPPrefs)
}