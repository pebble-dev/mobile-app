package io.rebble.cobble.shared.ui.view.home.locker

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import io.rebble.cobble.shared.database.entity.SyncedLockerEntryWithPlatforms
import io.rebble.cobble.shared.ui.AppTheme
import io.rebble.cobble.shared.ui.common.RebbleIcons
import io.rebble.cobble.shared.ui.viewmodel.LockerItemViewModel
import org.koin.compose.getKoin

@Composable
fun LockerWatchfaceItem(entry: SyncedLockerEntryWithPlatforms, watchConnected: Boolean, onOpenModalSheet: (LockerItemViewModel) -> Unit) {
    val koin = getKoin()
    val viewModel: LockerItemViewModel = viewModel(key = "locker-watchface-${entry.entry.id}") { LockerItemViewModel(koin.get(), entry) }
    val imageState: LockerItemViewModel.ImageState by viewModel.imageState.collectAsState()
    val supportedState: Boolean by viewModel.supportedState.collectAsState()
    Surface(tonalElevation = 1.dp, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
        Row(Modifier.padding(8.dp)) {
            Column(modifier = Modifier.fillMaxSize().padding(8.dp).weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                when (imageState) {
                    is LockerItemViewModel.ImageState.Loaded -> {
                        Image(
                                modifier = Modifier
                                        .height(130.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .align(Alignment.CenterHorizontally),
                                bitmap = (imageState as LockerItemViewModel.ImageState.Loaded).image,
                                contentDescription = "Watchface screenshot",
                                contentScale = ContentScale.FillHeight
                        )
                    }

                    is LockerItemViewModel.ImageState.Error -> {
                        Box(modifier = Modifier.fillMaxWidth().height(130.dp).clip(RoundedCornerShape(6.dp)).background(AppTheme.materialColors.surfaceColorAtElevation(elevation = 1.dp))) {
                            RebbleIcons.unknownApp(modifier = Modifier.size(56.dp).align(Alignment.Center))
                        }
                    }

                    is LockerItemViewModel.ImageState.Loading -> {
                        Box(modifier = Modifier.fillMaxWidth().height(130.dp).clip(RoundedCornerShape(6.dp)).background(AppTheme.materialColors.surfaceColorAtElevation(elevation = 1.dp))) {
                            CircularProgressIndicator(modifier = Modifier.size(56.dp).align(Alignment.Center))
                        }
                    }
                }
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(viewModel.title, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth(), overflow = TextOverflow.Ellipsis, maxLines = 1, style = MaterialTheme.typography.bodyLarge)
                    Text(viewModel.developerName, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth(), overflow = TextOverflow.Ellipsis, maxLines = 1, style = MaterialTheme.typography.bodySmall)
                }
            }
            Column(
                    modifier = Modifier.fillMaxHeight(),
                    verticalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = {viewModel.applyWatchface()}, enabled = watchConnected && supportedState, colors = IconButtonDefaults.iconButtonColors(contentColor = AppTheme.materialColors.primary)) {
                    RebbleIcons.sendToWatchUnchecked()
                }
                IconButton(onClick = {}, colors = IconButtonDefaults.iconButtonColors(contentColor = AppTheme.materialColors.primary)) {
                    RebbleIcons.settings()
                }
                IconButton(onClick = {onOpenModalSheet(viewModel)}, colors = IconButtonDefaults.iconButtonColors(contentColor = AppTheme.materialColors.primary)) {
                    RebbleIcons.menuVertical()
                }
            }
        }
    }
}