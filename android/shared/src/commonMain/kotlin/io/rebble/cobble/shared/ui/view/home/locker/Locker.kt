package io.rebble.cobble.shared.ui.view.home.locker

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import io.rebble.cobble.shared.database.dao.LockerDao
import io.rebble.cobble.shared.database.entity.SyncedLockerEntryWithPlatforms
import io.rebble.cobble.shared.ui.viewmodel.LockerViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import org.koin.compose.getKoin

enum class LockerTabs(val label: String) {
    Apps("Apps"),
    Watchfaces("Watchfaces"),
}

@Composable
fun Locker(lockerDao: LockerDao = getKoin().get(), viewModel: LockerViewModel = viewModel { LockerViewModel(lockerDao) }) {
    val entriesState: LockerViewModel.LockerEntriesState by viewModel.entriesState.collectAsState()
    val tab = remember { mutableStateOf(LockerTabs.Apps) }

    Column {
        Surface {
            Row(modifier = Modifier.fillMaxWidth().height(64.dp)) {
                LockerTabs.entries.forEachIndexed { index, it ->
                    NavigationBarItem(
                            selected = tab.value == it,
                            onClick = { tab.value = it },
                            icon = { Text(it.label) },
                    )
                }
            }
        }

        if (entriesState is LockerViewModel.LockerEntriesState.Loaded) {
            when (tab.value) {
                LockerTabs.Apps -> {
                    LockerAppList(viewModel)
                }

                LockerTabs.Watchfaces -> {
                    LockerWatchfaceList(viewModel)
                }
            }
        } else {
            CircularProgressIndicator(modifier = Modifier.align(CenterHorizontally))
        }
    }
}