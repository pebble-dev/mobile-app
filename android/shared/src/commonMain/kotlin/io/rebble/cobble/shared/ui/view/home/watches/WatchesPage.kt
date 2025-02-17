package io.rebble.cobble.shared.ui.view.home.watches

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WatchesPage(viewModel: WatchesListViewModel = viewModel{ WatchesListViewModel() }) {
    val selectedWatch = viewModel.selectedWatch.value

    if (selectedWatch != null) {

        WatchBottomSheetContent(
                watch = selectedWatch,
                onToggleConnection = { viewModel.toggleConnection(selectedWatch, true) },
                onForgetWatch = { viewModel.forgetWatch(selectedWatch) },
                onCheckForUpdates = { viewModel.checkForUpdates(selectedWatch) },
                clearSelection = { viewModel.clearSelection() }
        )
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
        if (viewModel.getConnectedWatch() == null) {
            Row(
                    modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 24.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(25.dp)
            ) {
                Box(
                        modifier = Modifier
                                .size(60.dp)
                                .background(
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                        shape = RoundedCornerShape(8.dp)
                                ),
                        contentAlignment = Alignment.Center
                ) {
                    RebbleIcons.disconnectFromWatch()
                }

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
        } else {
            WatchesListItem(watch = viewModel.getConnectedWatch()!!, viewModel = viewModel)
        }
        Text(modifier = Modifier
                        .padding(
                        horizontal = 10.dp),
                        text = "Other Watches")
        HorizontalDivider(thickness = 2.dp, color = MaterialTheme.colorScheme.secondary)

        LazyColumn {
            items(viewModel.watches.filter { !it.isConnected }) { watch ->
                WatchesListItem(watch = watch, viewModel = viewModel)
            }
        }
    }
}