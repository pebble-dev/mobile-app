package io.rebble.cobble.shared.ui

import androidx.compose.material.Colors
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle

@Immutable
data class Theme(
        val materialColors: Colors,
) {

    companion object {
        val dark = Theme(
                materialColors = darkColors(
                        primary = Color(0xFFF9A285),
                        background = Color(0xFF333333),
                        surface = Color(0xFF414141),
                )
        )

        val light = Theme(
                materialColors = lightColors(
                        primary = Color(0xFFCD3100),
                        background = Color(0xFFF0F0F0),
                        surface = Color(0xFFFAFAFA),
                )
        )
    }
}

val LocalTheme = staticCompositionLocalOf { Theme.dark }

val AppTheme
    @Composable
    get() = LocalTheme.current