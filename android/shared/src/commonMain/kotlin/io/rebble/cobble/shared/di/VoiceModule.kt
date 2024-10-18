package io.rebble.cobble.shared.di

import io.rebble.cobble.shared.domain.voice.DictationService
import io.rebble.cobble.shared.domain.voice.NullDictationService
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module

val voiceModule = module {
    factoryOf(::NullDictationService) bind DictationService::class
}