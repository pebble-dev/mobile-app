package io.rebble.cobble.shared.domain.timeline

import com.benasher44.uuid.Uuid
import io.rebble.cobble.shared.Logging
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import io.rebble.cobble.shared.database.dao.TimelinePinDao
import io.rebble.libpebblecommon.packets.blobdb.TimelineAction
import io.rebble.libpebblecommon.services.blobdb.TimelineService
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.filter

class TimelineActionManager: KoinComponent {
    private val timelineDao: TimelinePinDao by inject()
    private val timelineService: TimelineService by inject()

    private val actionFlow = callbackFlow {
        timelineService.actionHandler = {
            val deferred = CompletableDeferred<TimelineService.ActionResponse>()
            trySend(Pair(it, deferred))
            deferred.await()
        }
    }.buffer()

    fun actionFlowForApp(appId: Uuid):
            Flow<Pair<TimelineAction.InvokeAction, CompletableDeferred<TimelineService.ActionResponse>>> {
        return actionFlow.filter {
            val (action, _) = it
            val itemId = action.itemID.get()
            val item = timelineDao.get(itemId) ?: run {
                Logging.w("Received action for non-existent item $itemId")
                return@filter false
            }
            item.parentId == appId
        }
    }
}