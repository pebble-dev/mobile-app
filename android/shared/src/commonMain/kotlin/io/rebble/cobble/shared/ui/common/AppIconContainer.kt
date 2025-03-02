package io.rebble.cobble.shared.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.isUnspecified
import androidx.compose.ui.unit.dp

@Composable
fun AppIconContainer(
    color: Color = Color.Unspecified,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val background = remember { if (color.isUnspecified) randomColor() else color }
    Box(
        modifier =
            Modifier.clip(
                RoundedCornerShape(8.dp)
            ).background(background).size(48.dp).then(modifier),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

fun randomColor(): Color {
    return listOf(
        Color(0xFF41CBF7),
        Color(0xFF008DFF),
        Color(0xFF00A982),
        Color(0xFFFFFF00),
        Color(0xFF6B1D97)
    ).random()
}