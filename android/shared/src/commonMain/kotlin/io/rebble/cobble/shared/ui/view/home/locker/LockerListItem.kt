package io.rebble.cobble.shared.ui.view.home.locker

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import io.ktor.client.HttpClient
import io.rebble.cobble.shared.database.entity.SyncedLockerEntryWithPlatforms
import io.rebble.cobble.shared.ui.common.RebbleIcons
import io.rebble.cobble.shared.ui.viewmodel.LockerItemViewModel

@Composable
fun LockerListItem(httpClient: HttpClient, entry: SyncedLockerEntryWithPlatforms, trailingContent: (@Composable () -> Unit)? = null) {
    val viewModel = viewModel(key = "locker-${entry.entry.id}") { LockerItemViewModel(httpClient, entry) }
    val imageState: LockerItemViewModel.ImageState by viewModel.imageState.collectAsState()
    ListItem(
            leadingContent = {
                when (imageState) {
                    is LockerItemViewModel.ImageState.Loaded -> {
                        Image(
                                modifier = Modifier.size(40.dp),
                                bitmap = (imageState as LockerItemViewModel.ImageState.Loaded).image,
                                contentDescription = "App icon",
                        )
                    }

                    is LockerItemViewModel.ImageState.Error -> {
                        RebbleIcons.unknownApp(modifier = Modifier.size(40.dp))
                    }

                    is LockerItemViewModel.ImageState.Loading -> {
                        CircularProgressIndicator(modifier = Modifier.size(40.dp))
                    }
                }
            },
            headlineContent = { Text(viewModel.title) },
            supportingContent = { Text(viewModel.developerName) },
            trailingContent = trailingContent
    )
}