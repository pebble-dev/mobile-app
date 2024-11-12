package io.rebble.cobble.shared.di

import android.os.Build
import android.os.Build.VERSION_CODES
import android.service.notification.StatusBarNotification
import com.benasher44.uuid.Uuid
import io.rebble.cobble.shared.AndroidPlatformContext
import io.rebble.cobble.shared.PlatformContext
import io.rebble.cobble.shared.datastore.FlutterPreferences
import io.rebble.cobble.shared.datastore.createDataStore
import io.rebble.cobble.shared.domain.calendar.AndroidCalendarActionExecutor
import io.rebble.cobble.shared.domain.calendar.PlatformCalendarActionExecutor
import io.rebble.cobble.shared.domain.common.PebbleDevice
import io.rebble.cobble.shared.domain.notifications.AndroidNotificationActionExecutor
import io.rebble.cobble.shared.domain.notifications.CallNotificationProcessor
import io.rebble.cobble.shared.domain.notifications.NotificationProcessor
import io.rebble.cobble.shared.domain.notifications.PlatformNotificationActionExecutor
import io.rebble.cobble.shared.domain.voice.DictationService
import io.rebble.cobble.shared.domain.voice.NullDictationService
import io.rebble.cobble.shared.domain.voice.speechrecognizer.SpeechRecognizerDictationService
import io.rebble.cobble.shared.handlers.*
import io.rebble.cobble.shared.handlers.music.MusicHandler
import io.rebble.cobble.shared.jobs.AndroidJobScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module

val androidModule = module {
    factory {
        AndroidPlatformContext(androidContext().applicationContext)
    } bind PlatformContext::class
    single { createDataStore(androidContext()) }
    factory {
        androidContext().packageManager
    }

    single(named("activeNotifsState")) {
        MutableStateFlow<Map<Uuid, StatusBarNotification>>(emptyMap())
    } bind StateFlow::class
    single { AndroidJobScheduler() }
    single { FlutterPreferences() }
    singleOf<PlatformNotificationActionExecutor>(::AndroidNotificationActionExecutor)
    factory<PlatformCalendarActionExecutor> { params ->
        AndroidCalendarActionExecutor(params.get())
    }

    factory<Set<CobbleHandler>>(named("deviceHandlers")) { params ->
        val pebbleDevice: PebbleDevice = params.get()
        setOf(
                AppRunStateHandler(pebbleDevice),
                AppInstallHandler(pebbleDevice),
                CalendarActionHandler(pebbleDevice),
                CalendarHandler(pebbleDevice),
                MusicHandler(pebbleDevice),
                PKJSLifecycleHandler(pebbleDevice),
                AppMessageHandler(pebbleDevice),
                VoiceSessionHandler(pebbleDevice),
                AudioStreamHandler(pebbleDevice),
                AppLogHandler(pebbleDevice)
        )
    }

    factory<Set<CobbleHandler>>(named("negotiationDeviceHandlers")) { params ->
        val pebbleDevice: PebbleDevice = params.get()
        setOf(
                SystemHandler(pebbleDevice)
        )
    }

    singleOf(::NotificationProcessor)
    singleOf(::CallNotificationProcessor)
    singleOf(::AndroidPlatformAppMessageIPC) bind PlatformAppMessageIPC::class
    if (Build.VERSION.SDK_INT >= VERSION_CODES.TIRAMISU) {
        factoryOf(::SpeechRecognizerDictationService) bind DictationService::class
    } else {
        factoryOf(::NullDictationService) bind DictationService::class
    }
}