package io.rebble.cobble.shared.ui.view.home.locker

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.rebble.cobble.shared.ui.viewmodel.LockerItemViewModel
import io.rebble.cobble.shared.ui.viewmodel.LockerViewModel

@Composable
fun LockerWatchfaceList(
    viewModel: LockerViewModel,
    onOpenModalSheet: (LockerItemViewModel) -> Unit
) {
    val searchQuery: String? by viewModel.searchQuery.collectAsState()
    val entriesState: LockerViewModel.LockerEntriesState by viewModel.entriesState.collectAsState()
    val entries =
        ((entriesState as? LockerViewModel.LockerEntriesState.Loaded)?.entries ?: emptyList())
            .filter { it.entry.type == "watchface" }
            .filter {
                searchQuery == null || it.entry.title.contains(searchQuery!!, ignoreCase = true) || it.entry.developerName.contains(searchQuery!!, ignoreCase = true)
            }
            .sortedBy { it.entry.title }
    val connectedState: Boolean by viewModel.watchIsConnected.collectAsState()

    LazyVerticalGrid(
        modifier = Modifier.padding(8.dp),
        columns = GridCells.Fixed(2),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(entries.size) { i ->
            LockerWatchfaceItem(
                entries[i],
                watchConnected = connectedState,
                onOpenModalSheet = onOpenModalSheet
            )
        }
    }
}