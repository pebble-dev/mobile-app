package io.rebble.cobble.shared.ui.view.home.watches

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.rebble.cobble.shared.data.WatchItem

@Composable
fun WatchBottomSheetContent(watch: WatchItem, onToggleConnection: (WatchItem) -> Unit, onForgetWatch: (WatchItem) -> Unit, onCheckForUpdates: (WatchItem) -> Unit, onDismiss: () -> Unit) {
    Column(
            modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
    ) {
        Text(watch.name, style = MaterialTheme.typography.headlineSmall)
        Text(
                text = if (watch.isConnected) "Connected" else "Disconnected",
                color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(12.dp))

        Button(
                onClick = { onToggleConnection(watch) },
                modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (watch.isConnected) "Disconnect" else "Connect to watch")
        }

        OutlinedButton(
                onClick = { onCheckForUpdates(watch) },
                modifier = Modifier.fillMaxWidth()
        ) {
            Text("Check for updates")
        }

        TextButton(
                onClick = { onForgetWatch(watch) },
                modifier = Modifier.fillMaxWidth()
        ) {
            Text("Forget watch", color = Color.Red)
        }
    }
}