package io.rebble.cobble.shared.ui.view.home.locker

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.dp
import io.rebble.cobble.shared.ui.nav.Routes
import io.rebble.cobble.shared.ui.viewmodel.LockerViewModel

import org.koin.compose.viewmodel.koinViewModel

enum class LockerTabs(val label: String, val navRoute: String) {
    Watchfaces("My watch faces", Routes.Home.LOCKER_WATCHFACES),
    Apps("My apps", Routes.Home.LOCKER_APPS),
}

@Composable
fun Locker(page: LockerTabs, viewModel: LockerViewModel = koinViewModel(), onTabChanged: (LockerTabs) -> Unit) {
    val entriesState: LockerViewModel.LockerEntriesState by viewModel.entriesState.collectAsState()
    val modalSheetState by viewModel.modalSheetState.collectAsState()
    val watchIsConnected by viewModel.watchIsConnected.collectAsState()
    val searchQuery: String? by viewModel.searchQuery.collectAsState()
    val focusRequester = remember { FocusRequester() }
    val searchingState = remember { mutableStateOf(false) }
    val (searching, setSearching) = searchingState

    Column {
        Surface {
            Row(modifier = Modifier.fillMaxWidth().height(64.dp)) {
                if (searching) {
                    TextField(
                            value = searchQuery ?: "",
                            onValueChange = { viewModel.searchQuery.value = it },
                            label = { Text("Search") },
                            modifier = Modifier.fillMaxWidth().padding(8.dp)
                                    .focusRequester(focusRequester)
                                    .onGloballyPositioned {
                                        focusRequester.requestFocus()
                                    },
                            singleLine = true,
                            trailingIcon = {
                                IconButton(
                                        onClick = {
                                            viewModel.searchQuery.value = null
                                            setSearching(false)
                                        },
                                        modifier = Modifier.align(CenterVertically),
                                        content = {
                                            Icon(Icons.Default.Close, contentDescription = "Clear search")
                                        },
                                )
                            }
                    )
                } else {
                    LockerTabs.entries.forEachIndexed { index, it ->
                        NavigationBarItem(
                                selected = page == it,
                                onClick = { onTabChanged(it) },
                                icon = { Text(it.label) },
                        )
                    }
                }
            }
        }

        when (entriesState) {
            is LockerViewModel.LockerEntriesState.Loaded -> {
                when (page) {
                    LockerTabs.Apps -> {
                        LockerAppList(viewModel, onOpenModalSheet = { viewModel.openModalSheet(it) })
                    }

                    LockerTabs.Watchfaces -> {
                        LockerWatchfaceList(viewModel, onOpenModalSheet = { viewModel.openModalSheet(it) })
                    }
                }
            }
            is LockerViewModel.LockerEntriesState.Error -> {
                Text("Error loading locker entries")
            }
            is LockerViewModel.LockerEntriesState.Loading -> {
                CircularProgressIndicator(modifier = Modifier.align(CenterHorizontally))
            }
        }
    }
    if (modalSheetState is LockerViewModel.ModalSheetState.Open) {
        val sheetViewModel = (modalSheetState as LockerViewModel.ModalSheetState.Open).viewModel
        LockerItemSheet(onDismissRequest = { viewModel.closeModalSheet() }, watchIsConnected = watchIsConnected, viewModel = sheetViewModel)
    }
}
