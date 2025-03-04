package io.rebble.cobble.shared.ui.view.home.watches

import android.shared.generated.resources.Res
import android.shared.generated.resources.connected
import android.shared.generated.resources.disconnected
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.rebble.cobble.shared.data.WatchItem
import io.rebble.cobble.shared.ui.common.AppIconContainer
import io.rebble.cobble.shared.ui.common.RebbleIcons
import org.jetbrains.compose.resources.stringResource

@Composable
fun WatchesListItem(watch: WatchItem,
                    onSelectWatch: () -> Unit,
                    onToggleConnection: () -> Unit) {
    Row(
            modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 24.dp)
                    .clickable { onSelectWatch() },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AppIconContainer(color = if (watch.isConnected) {
                                        CONNECTED_BACKGROUND
                                    } else {
                                        MaterialTheme.colorScheme.primaryContainer
                                    },
                            content = { RebbleIcons.deadWatchGhost80() })

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(text = watch.name, fontWeight = FontWeight.Bold)
                Text(
                        text = if (watch.isConnected) {
                                    "${stringResource(Res.string.connected)}!"
                                } else {
                                    stringResource(Res.string.disconnected)
                                },

                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.secondary
                )
            }
        }
        // Ensure the icon's click event is separate from the row's click event
        Box(
                modifier = Modifier.clickable { onToggleConnection() }
        ) {
            if (watch.isConnected){
                RebbleIcons.disconnectFromWatch(tint = ButtonDefaults.buttonColors().containerColor)
            } else {
                RebbleIcons.connectToWatch(tint = ButtonDefaults.buttonColors().containerColor)
            }
        }
    }
}