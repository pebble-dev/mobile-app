package io.rebble.cobble.bridges.common

import android.content.Context
import android.content.Intent
import io.rebble.cobble.bridges.FlutterBridge
import io.rebble.cobble.bridges.ui.BridgeLifecycleController
import io.rebble.cobble.pigeons.Pigeons
import javax.inject.Inject

class PackageDetailsFlutterBridge
    @Inject
    constructor(
        private val context: Context,
        bridgeLifecycleController: BridgeLifecycleController
    ) : FlutterBridge, Pigeons.PackageDetails {
        init {
            bridgeLifecycleController.setupControl(Pigeons.PackageDetails::setup, this)
        }

        override fun getPackageList(): Pigeons.AppEntriesPigeon {
            val mainIntent = Intent(Intent.ACTION_MAIN, null)
            mainIntent.addCategory(Intent.CATEGORY_LAUNCHER)
            val packages = context.packageManager.queryIntentActivities(mainIntent, 0)
            val ret = Pigeons.AppEntriesPigeon()
            val pm = context.packageManager
            ret.appName =
                ArrayList(
                    packages.map {
                        return@map it.loadLabel(pm).toString()
                    }
                )
            ret.packageId =
                ArrayList(
                    packages.map {
                        return@map it.activityInfo.packageName
                    }
                )
            return ret
        }
    }