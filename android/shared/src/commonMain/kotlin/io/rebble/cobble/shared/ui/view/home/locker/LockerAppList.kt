package io.rebble.cobble.shared.ui.view.home.locker

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.rebble.cobble.shared.database.dao.LockerDao
import io.rebble.cobble.shared.database.entity.SyncedLockerEntryWithPlatforms
import io.rebble.cobble.shared.jobs.LockerSyncJob
import io.rebble.cobble.shared.ui.common.RebbleIcons
import org.koin.compose.getKoin
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LockerAppList(entries: List<SyncedLockerEntryWithPlatforms>) {
    val lazyListState = rememberLazyListState()
    val koin = getKoin()
    val lockerDao: LockerDao = koin.get()
    val reorderableLazyListState = rememberReorderableLazyListState(lazyListState) { from, to ->
        val entry = entries.first { it.entry.id == from.key }
        val nwList = entries.toMutableList()
        nwList.remove(entry)
        nwList.add(to.index, entry)
        nwList.forEachIndexed { i, e ->
            lockerDao.updateOrder(e.entry.id, i+1)
        }
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