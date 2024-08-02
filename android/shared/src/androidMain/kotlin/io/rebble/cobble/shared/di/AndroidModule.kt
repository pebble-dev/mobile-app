package io.rebble.cobble.shared.di

import android.service.notification.StatusBarNotification
import com.benasher44.uuid.Uuid
import io.rebble.cobble.shared.AndroidPlatformContext
import io.rebble.cobble.shared.PlatformContext
import io.rebble.cobble.shared.datastore.createDataStore
import io.rebble.cobble.shared.domain.calendar.PlatformCalendarActionExecutor
import io.rebble.cobble.shared.domain.notifications.PlatformNotificationActionExecutor
import io.rebble.cobble.shared.domain.notifications.AndroidNotificationActionExecutor
import io.rebble.cobble.shared.domain.calendar.AndroidCalendarActionExecutor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.koin.dsl.module
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.bind

val androidModule = module {
    factory {
        AndroidPlatformContext(androidContext().applicationContext)
    } bind PlatformContext::class
    single { createDataStore(androidContext()) }

    single(named("activeNotifsState")) {
        MutableStateFlow<Map<Uuid, StatusBarNotification>>(emptyMap())
    } bind StateFlow::class
    singleOf<PlatformNotificationActionExecutor>(::AndroidNotificationActionExecutor)
    singleOf<PlatformCalendarActionExecutor>(::AndroidCalendarActionExecutor)
}