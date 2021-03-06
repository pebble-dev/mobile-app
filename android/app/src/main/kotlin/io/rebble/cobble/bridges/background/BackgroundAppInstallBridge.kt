package io.rebble.cobble.bridges.background

import io.rebble.cobble.bridges.FlutterBridge
import io.rebble.cobble.pigeons.Pigeons
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackgroundAppInstallBridge @Inject constructor(
        private val flutterBackgroundController: FlutterBackgroundController,
) : FlutterBridge {
    private var cachedAppInstallCallbacks: Pigeons.BackgroundAppInstallCallbacks? = null

    suspend fun installAppNow(uri: String, appInfo: Pigeons.PbwAppInfo): Boolean {
        val appInstallCallbacks = getAppInstallCallbacks() ?: return false

        val appInstallData = Pigeons.InstallData().also {
            it.uri = uri
            it.appInfo = appInfo
        }
        appInstallCallbacks.beginAppInstall(appInstallData) {}

        return true
    }

    private suspend fun getAppInstallCallbacks(): Pigeons.BackgroundAppInstallCallbacks? {
        val cachedAppInstallCallbacks = cachedAppInstallCallbacks
        if (cachedAppInstallCallbacks != null) {
            return cachedAppInstallCallbacks
        }

        val flutterEngine = flutterBackgroundController.getBackgroundFlutterEngine() ?: return null
        return Pigeons.BackgroundAppInstallCallbacks(flutterEngine.dartExecutor.binaryMessenger)
                .also { this.cachedAppInstallCallbacks = it }
    }
}