package io.rebble.cobble.shared.domain.timeline

import com.benasher44.uuid.Uuid
import io.rebble.cobble.shared.Logging
import io.rebble.cobble.shared.database.dao.TimelinePinDao
import io.rebble.cobble.shared.domain.common.PebbleDevice
import io.rebble.cobble.shared.domain.notifications.NotificationActionHandler
import io.rebble.libpebblecommon.packets.blobdb.TimelineAction
import io.rebble.libpebblecommon.services.blobdb.TimelineService
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class TimelineActionManager(private val pebbleDevice: PebbleDevice): KoinComponent {
    private val timelineDao: TimelinePinDao by inject()
    private val timelineService = pebbleDevice.timelineService
    private val scope = CoroutineScope(Dispatchers.Default) //todo: Exception handler

    val actionFlow = callbackFlow {
        timelineService.actionHandler = {
            val deferred = CompletableDeferred<TimelineService.ActionResponse>()
            trySend(Pair(it, deferred))
            deferred.await()
        }
        awaitClose {
            timelineService.actionHandler = null
        }
    }.shareIn(scope, SharingStarted.Lazily).buffer()

    fun actionFlowForApp(appId: Uuid):
            Flow<Pair<TimelineAction.InvokeAction, CompletableDeferred<TimelineService.ActionResponse>>> {
        return actionFlow.filter {
            val (action, _) = it
            val itemId = action.itemID.get()
            val item = timelineDao.get(itemId) ?: run {
                if (!itemId.toString().startsWith(NotificationActionHandler.NOTIFICATION_UUID_PREFIX)) {
                    Logging.w("Received action for non-existent item $itemId")
                }
                return@filter false
            }
            item.parentId == appId
        }
    }
}