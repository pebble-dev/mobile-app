package io.rebble.cobble.shared.ui.view.home.watches

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
                    .padding(12.dp)
                    .clickable { viewModel.selectWatch(watch) },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            RebbleIcons.deadWatchGhost80()

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(text = watch.name, fontWeight = FontWeight.Bold)
                Text(
                        text = if (watch.isConnected) "Connected" else "Disconnected",
                        color = if (watch.isConnected) Color.Green else Color.Red
                )
            }
        }
        // Ensure the icon's click event is separate from the row's click event
        Box(
                modifier = Modifier.clickable { viewModel.toggleConnection(watch) }
        ) {
            if (watch.isConnected) RebbleIcons.disconnectFromWatch() else RebbleIcons.connectToWatch()
        }
    }
}