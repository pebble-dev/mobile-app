package io.rebble.cobble.shared.ui.view.home.settings

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier


@Composable
fun Settings(
        onNavigate: (String) -> Unit,
        modifier: Modifier = Modifier
) {

    Text(text = "Settings!")
}