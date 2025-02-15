package io.rebble.cobble.shared.ui.view.home.watches

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.rebble.cobble.shared.ui.common.RebbleIcons


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WatchesPage() {
    Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Top
    ) {
        CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                title = {
                    Text(
                            "My Watches",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                    )
                }
        )
        Row(
                modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            RebbleIcons.disconnectFromWatch()

            Column {
                Text(
                        text = "Nothing connected",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                )
                Text(
                        text = "Background service stopped",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Column(
                modifier = Modifier.padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("All watches")
            HorizontalDivider(thickness = 2.dp)
        }
    }
}