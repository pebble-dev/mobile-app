package io.rebble.cobble.shared.ui.view.home.locker

import androidx.compose.foundation.layout.height
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.rebble.cobble.shared.Logging
import io.rebble.cobble.shared.database.entity.SyncedLockerEntry

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LockerListItem(entry: SyncedLockerEntry, trailingContent: (@Composable () -> Unit)? = null) {
    ListItem(
            headlineText = { Text(entry.title) },
            supportingText = { Text(entry.developerName) },
            trailingContent = trailingContent
    )
}