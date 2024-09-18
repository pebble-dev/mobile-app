package io.rebble.cobble.shared.ui.view.home.locker

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.IconButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import io.rebble.cobble.shared.database.dao.LockerDao
import io.rebble.cobble.shared.database.entity.SyncedLockerEntryWithPlatforms
import io.rebble.cobble.shared.jobs.LockerSyncJob
import io.rebble.cobble.shared.ui.common.RebbleIcons
import io.rebble.cobble.shared.ui.viewmodel.LockerViewModel
import kotlinx.coroutines.Job
import org.koin.compose.getKoin
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LockerAppList(viewModel: LockerViewModel) {
    val lazyListState = rememberLazyListState()
    val koin = getKoin()
    val entriesState by viewModel.entriesState.collectAsState()
    val entries = ((entriesState as? LockerViewModel.LockerEntriesState.Loaded)?.entries ?: emptyList()).filter { it.entry.type == "watchapp" }
    val reorderableLazyListState = rememberReorderableLazyListState(lazyListState) { from, to ->
        val entry = entries.first { it.entry.id == from.key }
        val nwList = entries.toMutableList()
        nwList.remove(entry)
        nwList.add(to.index, entry)
        viewModel.updateOrder(nwList)
        LockerSyncJob.schedule(koin.get())
    }
    LazyColumn(state = lazyListState, modifier = Modifier.fillMaxSize()) {
        items(entries.size, key = { i -> entries[i].entry.id }) { i ->
            ReorderableItem(state = reorderableLazyListState, key = entries[i].entry.id) { isDragging ->
                LockerListItem(entries[i].entry, trailingContent = {
                    IconButton(
                            modifier = Modifier.draggableHandle(),
                            content = { RebbleIcons.dragHandle() },
                            onClick = {}
                    )
                })
            }
        }
    }
}