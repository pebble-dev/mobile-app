package io.rebble.cobble.shared.ui.common

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily

@Composable
fun TextIcon(font: FontFamily, char: Char) = Text(
        text = char.toString(),
        style = TextStyle(fontFamily = font)
)