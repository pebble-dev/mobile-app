package io.rebble.cobble.shared.di

import io.rebble.cobble.shared.ui.viewmodel.SettingsAccountCardViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module


val viewModelModule = module {
    viewModelOf(::SettingsAccountCardViewModel)
}