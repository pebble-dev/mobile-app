package io.rebble.cobble.shared.ui.common

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp

@Composable
fun TextIcon(font: FontFamily, char: Char, contentDescription: String = "Icon", modifier: Modifier = Modifier) {
    BoxWithConstraints(modifier = modifier) {
        val size = with(LocalDensity.current) { constraints.maxWidth.toSp() }
        Text(
                text = char.toString(),
                style = TextStyle(fontFamily = font, fontSize = size),
                modifier = Modifier.semantics {
                    this.contentDescription = contentDescription
                }
        )
    }
}