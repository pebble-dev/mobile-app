package io.rebble.cobble.shared.ui.view.home.locker

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import io.rebble.cobble.shared.jobs.LockerSyncJob
import io.rebble.cobble.shared.ui.common.RebbleIcons
import io.rebble.cobble.shared.ui.viewmodel.LockerItemViewModel
import io.rebble.cobble.shared.ui.viewmodel.LockerViewModel
import org.koin.compose.getKoin
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LockerAppList(viewModel: LockerViewModel, onOpenModalSheet: (LockerItemViewModel) -> Unit) {
    val lazyListState = rememberLazyListState()
    val koin = getKoin()
    val entriesState by viewModel.entriesState.collectAsState()
    val searchQuery: String? by viewModel.searchQuery.collectAsState()
    val entries = ((entriesState as? LockerViewModel.LockerEntriesState.Loaded)?.entries ?: emptyList())
            .filter { it.entry.type == "watchapp" }
            .filter { searchQuery == null || it.entry.title.contains(searchQuery!!, ignoreCase = true) || it.entry.developerName.contains(searchQuery!!, ignoreCase = true) }
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
                LockerListItem(koin.get(), entries[i], onOpenModalSheet = onOpenModalSheet, dragHandle = {
                    if (searchQuery == null) {
                        IconButton(
                                modifier = Modifier.draggableHandle(),
                                content = { RebbleIcons.dragHandle() },
                                onClick = {}
                        )
                    }
                })
            }
        }
    }
}