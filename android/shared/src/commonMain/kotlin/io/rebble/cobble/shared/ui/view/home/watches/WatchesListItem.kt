package io.rebble.cobble.shared.ui.view.home.watches

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.rebble.cobble.shared.data.WatchItem
import io.rebble.cobble.shared.ui.common.RebbleIcons
import io.rebble.cobble.shared.ui.viewmodel.WatchesListViewModel

@Composable
fun WatchesListItem(watch: WatchItem, viewModel: WatchesListViewModel) {
    Row(
            modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 24.dp)
                    .clickable { viewModel.selectWatch(watch) },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
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
                    RebbleIcons.deadWatchGhost80()
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
                Text(text = watch.name, fontWeight = FontWeight.Bold)
                Text(
                        text = if (watch.isConnected) "Connected!" else "Disconnected",
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.secondary
                )
            }
        }
        // Ensure the icon's click event is separate from the row's click event
        Box(
                modifier = Modifier.clickable { viewModel.toggleConnection(watch) }
        ) {
            if (watch.isConnected) RebbleIcons.disconnectFromWatch(tint = ButtonDefaults.buttonColors().containerColor)
            else RebbleIcons.connectToWatch(tint = ButtonDefaults.buttonColors().containerColor)
        }
    }
}