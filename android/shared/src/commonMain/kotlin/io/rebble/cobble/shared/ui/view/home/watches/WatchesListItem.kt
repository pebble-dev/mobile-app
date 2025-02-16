package io.rebble.cobble.shared.ui.view.home.watches

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.rebble.cobble.shared.data.WatchItem

@Composable
fun WatchesListItem(watch: WatchItem, onClick: () -> Unit) {
    Row(
            modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
                    .clickable { onClick() },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                    modifier = Modifier
                            .size(40.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
                    contentAlignment = Alignment.Center
            ) {
//                Icon(
//                        imageVector = Icons.Default.Watch,  // Replace with your actual icon
//                        contentDescription = null,
//                        tint = MaterialTheme.colorScheme.onSurface
//                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(text = watch.name, fontWeight = FontWeight.Bold)
                Text(
                        text = if (watch.isConnected) "Connected" else "Disconnected",
                        color = if (watch.isConnected) Color.Green else Color.Red
                )
            }
        }

//        Icon(
//                imageVector = if (watch.isConnected) Icons.Default.SignalWifi4Bar else Icons.Default.WifiOff,
//                contentDescription = "Connection Status",
//                tint = if (watch.isConnected) Color.Green else Color.Red
//        )
    }
}