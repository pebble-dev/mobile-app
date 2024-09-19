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
import io.rebble.cobble.shared.ui.nav.Routes
import io.rebble.cobble.shared.ui.viewmodel.LockerItemViewModel
import io.rebble.cobble.shared.ui.viewmodel.LockerViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import org.koin.compose.getKoin

enum class LockerTabs(val label: String, val navRoute: String) {
    Watchfaces("My watch faces", Routes.Home.LOCKER_WATCHFACES),
    Apps("My apps", Routes.Home.LOCKER_APPS),
}

@Composable
fun Locker(page: LockerTabs, lockerDao: LockerDao = getKoin().get(), viewModel: LockerViewModel = viewModel { LockerViewModel(lockerDao) }, onTabChanged: (LockerTabs) -> Unit) {
    val entriesState: LockerViewModel.LockerEntriesState by viewModel.entriesState.collectAsState()
    val modalSheetState by viewModel.modalSheetState.collectAsState()

    Column {
        Surface {
            Row(modifier = Modifier.fillMaxWidth().height(64.dp)) {
                LockerTabs.entries.forEachIndexed { index, it ->
                    NavigationBarItem(
                            selected = page == it,
                            onClick = { onTabChanged(it) },
                            icon = { Text(it.label) },
                    )
                }
            }
        }

        if (entriesState is LockerViewModel.LockerEntriesState.Loaded) {
            when (page) {
                LockerTabs.Apps -> {
                    LockerAppList(viewModel, onOpenModalSheet = { viewModel.openModalSheet(it) })
                }

                LockerTabs.Watchfaces -> {
                    LockerWatchfaceList(viewModel, onOpenModalSheet = { viewModel.openModalSheet(it) })
                }
            }
        } else {
            CircularProgressIndicator(modifier = Modifier.align(CenterHorizontally))
        }
    }
    if (modalSheetState is LockerViewModel.ModalSheetState.Open) {
        val sheetViewModel = (modalSheetState as LockerViewModel.ModalSheetState.Open).viewModel
        LockerItemSheet(onDismissRequest = { viewModel.closeModalSheet() }, viewModel = sheetViewModel)
    }
}