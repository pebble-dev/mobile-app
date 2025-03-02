package io.rebble.cobble.shared.ui.view.home.locker

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.rebble.cobble.shared.ui.AppTheme
import io.rebble.cobble.shared.ui.common.AppIconContainer
import io.rebble.cobble.shared.ui.common.RebbleIcons
import io.rebble.cobble.shared.ui.viewmodel.LockerItemViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LockerItemSheet(
    onDismissRequest: () -> Unit,
    watchIsConnected: Boolean,
    viewModel: LockerItemViewModel
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = AppTheme.materialColors.surface
    ) {
        val imageState: LockerItemViewModel.ImageState by viewModel.imageState.collectAsState()
        val supportedState: Boolean by viewModel.supportedState.collectAsState()
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 8.dp)
        ) {
            if (viewModel.entry.entry.type == "watchapp") {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    AppIconContainer {
                        when (imageState) {
                            is LockerItemViewModel.ImageState.Loaded -> {
                                Image(
                                    modifier = Modifier.size(48.dp),
                                    bitmap = (imageState as LockerItemViewModel.ImageState.Loaded).image,
                                    contentDescription = "App icon"
                                )
                            }

                            is LockerItemViewModel.ImageState.Error -> {
                                RebbleIcons.unknownApp()
                            }
                        }
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            "${viewModel.title} v${viewModel.version}",
                            overflow = TextOverflow.Ellipsis,
                            maxLines = 1,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            viewModel.developerName,
                            overflow = TextOverflow.Ellipsis,
                            maxLines = 1,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            } else {
                when (imageState) {
                    is LockerItemViewModel.ImageState.Loaded -> {
                        Image(
                            modifier =
                                Modifier
                                    .height(130.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .align(Alignment.CenterHorizontally),
                            bitmap = (imageState as LockerItemViewModel.ImageState.Loaded).image,
                            contentDescription = "Watchface screenshot",
                            contentScale = ContentScale.FillHeight
                        )
                    }

                    is LockerItemViewModel.ImageState.Error -> {
                        Box(
                            modifier =
                                Modifier.fillMaxWidth().height(
                                    130.dp
                                ).clip(
                                    RoundedCornerShape(6.dp)
                                ).background(
                                    AppTheme.materialColors.surfaceColorAtElevation(
                                        elevation = 1.dp
                                    )
                                )
                        ) {
                            RebbleIcons.unknownApp(
                                modifier = Modifier.size(56.dp).align(Alignment.Center)
                            )
                        }
                    }

                    is LockerItemViewModel.ImageState.Loading -> {
                        Box(
                            modifier =
                                Modifier.fillMaxWidth().height(
                                    130.dp
                                ).clip(
                                    RoundedCornerShape(6.dp)
                                ).background(
                                    AppTheme.materialColors.surfaceColorAtElevation(
                                        elevation = 1.dp
                                    )
                                )
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(56.dp).align(Alignment.Center)
                            )
                        }
                    }
                }
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        viewModel.title,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        viewModel.developerName,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            Row(
                modifier =
                    Modifier.align(
                        Alignment.CenterHorizontally
                    ).padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                IconButton(
                    onClick = { /*TODO*/ },
                    colors =
                        IconButtonDefaults.iconButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                ) {
                    RebbleIcons.rebbleStore()
                }
                TextButton(
                    onClick = { /*TODO*/ },
                    colors =
                        ButtonDefaults.buttonColors(
                            contentColor = MaterialTheme.colorScheme.primary,
                            containerColor = Color.Transparent
                        ),
                    shape = RoundedCornerShape(100)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier =
                            Modifier.wrapContentWidth(
                                unbounded = true
                            ).align(Alignment.CenterVertically)
                    ) {
                        RebbleIcons.heartEmpty()
                        Text(viewModel.hearts.toString(), fontWeight = FontWeight.Bold)
                    }
                }
                IconButton(
                    onClick = { /*TODO*/ },
                    colors =
                        IconButtonDefaults.iconButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                ) {
                    RebbleIcons.share()
                }
            }
            HorizontalDivider(thickness = 2.dp)
            if (viewModel.entry.entry.type == "watchface") {
                val color =
                    if (watchIsConnected && supportedState) {
                        ListItemDefaults.contentColor
                    } else {
                        ListItemDefaults.contentColor.copy(alpha = 0.38f)
                    }
                ListItem(
                    colors =
                        ListItemDefaults.colors(
                            leadingIconColor = color,
                            headlineColor = color,
                            trailingIconColor = color
                        ),
                    modifier =
                        Modifier.clickable(enabled = watchIsConnected && supportedState) {
                            viewModel.applyWatchface()
                        },
                    leadingContent = { RebbleIcons.sendToWatchUnchecked() },
                    headlineContent = { Text("Apply on watch") }
                )
                HorizontalDivider(thickness = 2.dp)
            }
            ListItem(
                leadingContent = { RebbleIcons.permissions() },
                headlineContent = { Text("Manage permissions") },
                trailingContent = { RebbleIcons.caretRight() }
            )
            ListItem(
                leadingContent = { RebbleIcons.settings() },
                headlineContent = { Text("Settings") },
                trailingContent = { RebbleIcons.caretRight() }
            )
            HorizontalDivider(thickness = 2.dp)
            ListItem(
                leadingContent = { RebbleIcons.deleteTrash() },
                headlineContent = { Text("Delete from locker") }
            )
        }
    }
}