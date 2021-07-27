package io.rebble.cobble.bridges.background

import io.rebble.cobble.bridges.FlutterBridge
import io.rebble.cobble.pigeons.Pigeons
import io.rebble.cobble.util.awaitPigeonMethod
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

        awaitPigeonMethod<Void> { reply ->
            appInstallCallbacks.beginAppInstall(appInstallData, reply)
        }

        return true
    }

    suspend fun deleteApp(uuid: Pigeons.StringWrapper): Boolean {
        val appInstallCallbacks = getAppInstallCallbacks() ?: return false

        awaitPigeonMethod<Void> { reply ->
            appInstallCallbacks.deleteApp(uuid, reply)
        }

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