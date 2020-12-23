package io.rebble.cobble.handlers

import io.rebble.cobble.bridges.background.BackgroundTimelineFlutterBridge
import io.rebble.libpebblecommon.packets.blobdb.TimelineAction
import io.rebble.libpebblecommon.services.blobdb.TimelineService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.job
import javax.inject.Inject

class TimelineHandler @Inject constructor(
        private val timelineService: TimelineService,
        coroutineScope: CoroutineScope,
        private val backgroundTimelineFlutterBridge: BackgroundTimelineFlutterBridge
) : PebbleMessageHandler {
    init {
        timelineService.actionHandler = this::handleAction

        coroutineScope.coroutineContext.job.invokeOnCompletion {
            timelineService.actionHandler = null
        }
    }

    private suspend fun handleAction(
            actionRequest: TimelineAction.InvokeAction
    ): TimelineService.ActionResponse {
        return backgroundTimelineFlutterBridge.handleTimelineAction(actionRequest)
    }
}