package io.rebble.cobble.shared.ui.view.home.watches

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.rebble.cobble.shared.data.WatchItem
import io.rebble.cobble.shared.ui.common.RebbleIcons

@Composable
fun WatchBottomSheetContent(watch: WatchItem, onToggleConnection: (WatchItem) -> Unit, onForgetWatch: (WatchItem) -> Unit, onCheckForUpdates: (WatchItem) -> Unit, onDismiss: () -> Unit) {
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
            RebbleIcons.deadWatchGhost80()

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(text = watch.name, fontWeight = FontWeight.Bold)
                Text(
                        text = if (watch.isConnected&&watch.updateAvailable) watch.softwareVersion + " - Update Available!"
                                else if(watch.isConnected) watch.softwareVersion + " - Connected!"
                                else "Disconnected",
                        color = if (watch.isConnected&&watch.updateAvailable) Color.Green
                                else if(!watch.isConnected) Color.Red
                                else MaterialTheme.colorScheme.secondary
                )
            }
        }

        HorizontalDivider(thickness = 2.dp)

        Row(
                modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .clickable { onToggleConnection(watch) },
                verticalAlignment = Alignment.CenterVertically
        ) {
            if (watch.isConnected) RebbleIcons.disconnectFromWatch() else RebbleIcons.connectToWatch()
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                        text = if (watch.isConnected) "Disconnect from watch" else "Connect to watch"
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
                if (watch.updateAvailable) RebbleIcons.applyUpdate() else RebbleIcons.checkForUpdates()
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                            text = if (watch.updateAvailable) "Download Update" else "Check for updates"
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
                RebbleIcons.checkForUpdates()
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                            text = "Check for updates"
                    )
                }
            }
        }

        HorizontalDivider(thickness = 2.dp)

        Row(
                modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .clickable { onForgetWatch(watch) },
                verticalAlignment = Alignment.CenterVertically
        ) {
            RebbleIcons.unpairFromWatch()
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