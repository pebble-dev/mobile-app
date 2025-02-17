package io.rebble.cobble.shared.ui.view.home.watches

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.rebble.cobble.shared.data.WatchItem
import io.rebble.cobble.shared.ui.common.RebbleIcons
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WatchBottomSheetContent(watch: WatchItem, onToggleConnection: (WatchItem) -> Unit,
                            onForgetWatch: (WatchItem) -> Unit, onCheckForUpdates: (WatchItem) -> Unit,
                            clearSelection: () -> Unit) {
    val sheetState = rememberModalBottomSheetState()
    val coroutineScope = rememberCoroutineScope()

    ModalBottomSheet(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            onDismissRequest = {
                coroutineScope.launch {
                    sheetState.hide()  // First, hide the sheet smoothly
                }.invokeOnCompletion {
                    clearSelection()  // Then, reset selected watch
                }
            },
            sheetState = sheetState
    ){
        Column(
                modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
        ) {
            Row(
                    modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
            ) {
                if (watch.isConnected) {
                    Box(
                            modifier = Modifier
                                    .size(60.dp)
                                    .background(
                                            color = Color(121,249,205),
                                            shape = RoundedCornerShape(8.dp)
                                    ),
                            contentAlignment = Alignment.Center
                    ) {
                        RebbleIcons.deadWatchGhost80() //TODO Switch with watch icon
                    }
                } else {
                    Box(
                            modifier = Modifier
                                    .size(60.dp)
                                    .background(
                                            color = MaterialTheme.colorScheme.primaryContainer,
                                            shape = RoundedCornerShape(8.dp)
                                    ),
                            contentAlignment = Alignment.Center
                    ) {
                        RebbleIcons.deadWatchGhost80()
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(text = watch.name, fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyLarge)
                    Text(
                            text = if (watch.isConnected&&watch.updateAvailable) watch.softwareVersion + " - Update Available!"
                                    else if(watch.isConnected) watch.softwareVersion + " - Connected!"
                                    else "Disconnected",
                            fontWeight = FontWeight.SemiBold,
                            color = if (watch.isConnected&&watch.updateAvailable) Color(0, 108, 81)
                                    else MaterialTheme.colorScheme.secondary
                    )
                }
            }

            HorizontalDivider(thickness = 2.dp, color = MaterialTheme.colorScheme.secondary)

            Row(
                    modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .clickable { onToggleConnection(watch) },
                    verticalAlignment = Alignment.CenterVertically
            ) {
                if (watch.isConnected) RebbleIcons.disconnectFromWatch(tint = MaterialTheme.colorScheme.secondary)
                else RebbleIcons.connectToWatch(tint = MaterialTheme.colorScheme.secondary)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                            text = if (watch.isConnected) "Disconnect from watch" else "Connect to watch",
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
            if (watch.isConnected){
                Row(
                        modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                                .clickable { onCheckForUpdates(watch) },
                        verticalAlignment = Alignment.CenterVertically
                ) {
                    if (watch.updateAvailable) RebbleIcons.applyUpdate(tint = Color(0, 108, 81))
                    else RebbleIcons.checkForUpdates(tint = MaterialTheme.colorScheme.secondary)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                                text = if (watch.updateAvailable) "Download Update" else "Check for updates",
                                fontWeight = FontWeight.SemiBold,
                                color = if (watch.updateAvailable) Color(0, 108, 81)
                                        else MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            } else {
                Row(
                        modifier = Modifier
                                .fillMaxWidth()
                                .alpha(0.7f)
                                .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                ) {
                    RebbleIcons.checkForUpdates(tint = MaterialTheme.colorScheme.secondary)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                                text = "Check for updates",
                                color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }

            HorizontalDivider(thickness = 2.dp, color = MaterialTheme.colorScheme.secondary)

            Row(
                    modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .clickable { onForgetWatch(watch) },
                    verticalAlignment = Alignment.CenterVertically
            ) {
                RebbleIcons.unpairFromWatch(tint = Color.Red)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                            text = "Forget Watch",
                            color = Color.Red
                    )
                }
            }
        }
    }
}