package io.rebble.cobble.shared.ui.view.home.watches

import android.shared.generated.resources.*
import android.shared.generated.resources.Res
import android.shared.generated.resources.bg_service_stopped
import android.shared.generated.resources.my_watches
import android.shared.generated.resources.nothing_connected
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
import io.rebble.cobble.shared.ui.common.AppIconContainer
import io.rebble.cobble.shared.ui.common.RebbleIcons
import io.rebble.cobble.shared.ui.viewmodel.WatchesListViewModel
import org.jetbrains.compose.resources.stringResource


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WatchesPage(viewModel: WatchesListViewModel = viewModel{ WatchesListViewModel() }) {
    val selectedWatch by viewModel.selectedWatch

    if (selectedWatch != null) {

        WatchBottomSheetContent(
                watch = selectedWatch!!,
                onToggleConnection = { viewModel.toggleConnection(selectedWatch!!, true) },
                onForgetWatch = { viewModel.forgetWatch(selectedWatch!!) },
                onCheckForUpdates = { viewModel.checkForUpdates(selectedWatch!!) },
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
                            text = stringResource(Res.string.my_watches),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                    )
                }
        )
        val connectedWatch = viewModel.connectedWatch
        if (connectedWatch == null) {
            Row(
                    modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 24.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(25.dp)
            ) {
                AppIconContainer(color = MaterialTheme.colorScheme.primaryContainer,
                                content = { RebbleIcons.disconnectFromWatch() })

                Column {
                    Text(
                            text = stringResource(Res.string.nothing_connected),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold
                    )
                    Text(
                            text = stringResource(Res.string.bg_service_stopped),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            WatchesListItem(watch = connectedWatch,
                            onSelectWatch = { viewModel.selectWatch(connectedWatch) },
                            onToggleConnection = { viewModel.toggleConnection(connectedWatch) })
        }
        Text(modifier = Modifier
                        .padding(horizontal = 10.dp),
                        text = stringResource(Res.string.other_watches))
        HorizontalDivider(thickness = 2.dp, color = MaterialTheme.colorScheme.secondary)

        LazyColumn {
            items(viewModel.disconnectedWatches, key = { it.name }) { watch ->
                WatchesListItem(watch = watch,
                                onSelectWatch = { viewModel.selectWatch(watch) },
                                onToggleConnection = { viewModel.toggleConnection(watch) })
            }
        }
    }
}