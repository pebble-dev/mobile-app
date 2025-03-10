package io.rebble.cobble.shared.di

import org.koin.dsl.module
import io.rebble.cobble.shared.ui.viewmodel.LockerViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named


val viewModelModule = module {
    viewModel {
        LockerViewModel(get(), get(qualifier = named("io_dispatcher")) )
    }
}