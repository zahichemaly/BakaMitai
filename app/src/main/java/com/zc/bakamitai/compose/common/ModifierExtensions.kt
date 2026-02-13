package com.zc.bakamitai.compose.common

import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

fun Modifier.roundedBackground(color: Color, cornerRadius: Dp = 16.dp): Modifier {
    return this.background(
        color = color,
        shape = RoundedCornerShape(cornerRadius)
    )
}
