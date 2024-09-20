package io.rebble.cobble.bridges.common

import io.rebble.cobble.bridges.FlutterBridge
import io.rebble.cobble.bridges.ui.BridgeLifecycleController
import io.rebble.cobble.pigeons.Pigeons
import io.rebble.cobble.shared.database.dao.CachedPackageInfoDao
import io.rebble.libpebblecommon.packets.blobdb.PushNotification
import io.rebble.libpebblecommon.services.notification.NotificationService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class NotificationsFlutterBridge @Inject constructor(
        bridgeLifecycleController: BridgeLifecycleController,
        private val coroutineScope: CoroutineScope,
        private val cachedPackageInfoDao: CachedPackageInfoDao,
) : FlutterBridge, Pigeons.NotificationsControl {
    init {
        bridgeLifecycleController.setupControl(Pigeons.NotificationsControl::setup, this)
    }

    override fun getNotificationPackages(result: Pigeons.Result<MutableList<Pigeons.NotifyingPackage>>) {
        coroutineScope.launch {
            result.success(
                    cachedPackageInfoDao.getAll().map {
                        Pigeons.NotifyingPackage.Builder()
                                .setPackageId(it.id)
                                .setPackageName(it.name)
                                .build()
                    }.toMutableList()
            )
        }
    }
}