package io.rebble.cobble.shared.handlers

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
    init {
        pebbleDevice.negotiationScope.launch {
            val deviceScope = pebbleDevice.connectionScope.filterNotNull().first()
            listenForPKJSLifecycleChanges(deviceScope)
        }
    }

    private fun listenForPKJSLifecycleChanges(scope: CoroutineScope) {
        pebbleDevice.currentActiveApp.filterNotNull().onEach {
            pkjsApp?.stop()
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