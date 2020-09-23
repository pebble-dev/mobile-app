package io.rebble.fossil.bridges

import io.flutter.plugin.common.BinaryMessenger
import io.rebble.fossil.pigeons.Pigeons
import io.rebble.libpebblecommon.blobdb.PushNotification
import io.rebble.libpebblecommon.services.notification.NotificationService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class Notifications @Inject constructor(
        binaryMessenger: BinaryMessenger,
        private val notificationService: NotificationService,
        private val coroutineScope: CoroutineScope
) : FlutterBridge, Pigeons.NotificationsControl {
    init {
        Pigeons.NotificationsControl.setup(binaryMessenger, this)
    }

    override fun sendTestNotification() {
        coroutineScope.launch {
            notificationService.send(PushNotification(
                    "Test Notification"

            ))
        }
    }
}