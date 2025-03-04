package io.rebble.cobble.shared.ui.view.home.settings

import androidx.compose.foundation.clickable
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.rebble.cobble.shared.ui.common.RebbleIcons

@Composable
internal fun SettingsNavigableListItem(
        icon: @Composable () -> Unit,
        title: String,
        onClick: () -> Unit,
        modifier: Modifier = Modifier
) {
    ListItem(
            modifier = modifier.clickable(onClick = onClick),
            leadingContent = icon,
            headlineContent = { Text(text = title) },
            trailingContent = { RebbleIcons.caretRight() }
    )
}