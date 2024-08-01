package io.rebble.cobble.shared.ui.view

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.text.selection.DisableSelection
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import io.rebble.cobble.shared.ui.LocalTheme
import io.rebble.cobble.shared.ui.Theme
import io.rebble.cobble.shared.ui.view.home.HomeScaffold

@Composable
fun MainView() {
    DisableSelection {
        val theme = if (isSystemInDarkTheme()) Theme.dark else Theme.light

        CompositionLocalProvider(
                LocalTheme provides theme,
        ) {
            MaterialTheme(
                    colors = theme.materialColors
            ) {
                HomeScaffold()
            }
        }
    }
}