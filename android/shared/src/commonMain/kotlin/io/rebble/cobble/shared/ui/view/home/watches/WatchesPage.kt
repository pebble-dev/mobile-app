package io.rebble.cobble.shared.ui.view.home.watches

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import io.rebble.cobble.shared.ui.common.RebbleIcons
import io.rebble.cobble.shared.ui.viewmodel.WatchesListViewModel
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WatchesPage(viewModel: WatchesListViewModel = viewModel{ WatchesListViewModel() }) {
    val selectedWatch = viewModel.selectedWatch.value
    val sheetState = rememberModalBottomSheetState()
    val coroutineScope = rememberCoroutineScope()

    if (selectedWatch != null) {
        ModalBottomSheet(
                onDismissRequest = {
                    coroutineScope.launch {
                        sheetState.hide()  // First, hide the sheet smoothly
                    }.invokeOnCompletion {
                        viewModel.clearSelection()  // Then, reset selected watch
                    }
                },
                sheetState = sheetState
        ) {
            WatchBottomSheetContent(
                    watch = selectedWatch,
                    onToggleConnection = { viewModel.toggleConnection(selectedWatch) },
                    onForgetWatch = { viewModel.forgetWatch(selectedWatch) },
                    onCheckForUpdates = { viewModel.checkForUpdates(selectedWatch) },
                    onDismiss = {
                        coroutineScope.launch {
                            sheetState.hide()
                        }.invokeOnCompletion {
                            viewModel.clearSelection()
                        }
                    }
            )
        }
    }

    Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Top
    ) {
        CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                ),
                title = {
                    Text(
                            "My watches",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                    )
                }
        )
        Row(
                modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(25.dp)
        ) {
            RebbleIcons.disconnectFromWatch()

            Column {
                Text(
                        text = "Nothing connected",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                )
                Text(
                        text = "Background service stopped",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Text(modifier = Modifier
                        .padding(
                        horizontal = 10.dp),
                        text = "Other Watches")
        HorizontalDivider(thickness = 2.dp)

        LazyColumn {
            items(viewModel.watches) { watch ->
                WatchesListItem(watch = watch) {
                    viewModel.selectWatch(watch)
                }
            }
        }
    }
}