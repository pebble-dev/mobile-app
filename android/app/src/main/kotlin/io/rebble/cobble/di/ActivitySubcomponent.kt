package io.rebble.cobble.di

import dagger.BindsInstance
import dagger.Subcomponent
import io.rebble.cobble.MainActivity
import javax.inject.Scope

@PerActivity
@Subcomponent(
    modules = [
        ActivityModule::class
    ]
)
interface ActivitySubcomponent {
    @Subcomponent.Factory
    interface Factory {
        fun create(
            @BindsInstance mainActivity: MainActivity
        ): ActivitySubcomponent
    }
}

@Scope
annotation class PerActivity