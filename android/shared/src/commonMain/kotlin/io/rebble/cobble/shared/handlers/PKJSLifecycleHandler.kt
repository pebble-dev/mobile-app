package io.rebble.cobble.shared.handlers

import com.benasher44.uuid.uuidFrom
import io.ktor.client.HttpClient
import io.rebble.cobble.shared.Logging
import io.rebble.cobble.shared.PlatformContext
import io.rebble.cobble.shared.database.dao.LockerDao
import io.rebble.cobble.shared.domain.common.PebbleDevice
import io.rebble.cobble.shared.js.PKJSApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class PKJSLifecycleHandler(
        private val pebbleDevice: PebbleDevice
) : CobbleHandler, KoinComponent {
    private val platformContext: PlatformContext by inject()
    private val httpClient: HttpClient by inject()
    private val lockerDao: LockerDao by inject()
    var pkjsApp: PKJSApp? = null

    companion object {
        private val SYSTEM_APP_UUIDS = setOf(
                uuidFrom("dec0424c-0625-4878-b1f2-147e57e83688"), // Home
                uuidFrom("07e0d9cb-8957-4bf7-9d42-35bf47caadfe"), // Settings
                uuidFrom("1f03293d-47af-4f28-b960-f2b02a6dd757"), // Music
                uuidFrom("b2cae818-10f8-46df-ad2b-98ad2254a3c1"), // Notifications
                uuidFrom("67a32d95-ef69-46d4-a0b9-854cc62f97f9"), // Alarms
                uuidFrom("18e443ce-38fd-47c8-84d5-6d0c775fbe55"), // Watchfaces
                uuidFrom("8f3c8686-31a1-4f5f-91f5-01600c9bdc59"), // TicToc
        )
    }
    init {
        pebbleDevice.negotiationScope.launch {
            val deviceScope = pebbleDevice.connectionScope.filterNotNull().first()
            listenForPKJSLifecycleChanges(deviceScope)
        }
    }

    private fun listenForPKJSLifecycleChanges(scope: CoroutineScope) {
        pebbleDevice.currentActiveApp.filterNotNull().onEach {
            pkjsApp?.stop()
            // Ignore system apps we know won't be in the locker and don't use pkjs
            if (it in SYSTEM_APP_UUIDS) {
                return@onEach
            }
            val appFile = getAppPbwFile(platformContext, it.toString())
            if (!appFile.exists()) {
                Logging.d("Downloading app $it for js")
                downloadPbw(platformContext, httpClient, lockerDao, it.toString())
            }
            if (PKJSApp.isJsApp(platformContext, it)) {
                Logging.d("Switching to PKJS app $it")
                pkjsApp = PKJSApp(it)
                pkjsApp?.start(pebbleDevice)
            } else {
                Logging.v("App $it is not a PKJS app")
            }
        }.catch {
            Logging.e("Error while listening for PKJS lifecycle changes", it)
        }.launchIn(scope)
    }
}