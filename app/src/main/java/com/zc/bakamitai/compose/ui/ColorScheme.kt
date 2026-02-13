package com.zc.bakamitai.compose.ui

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

val LightColorScheme = lightColorScheme(
    primary = ColorPrimary,
    background = Color.White,
    surface = Color.White,
    onPrimary = Color.Black,
    onBackground = Color.Black,
    onSurface = Color.Black
)

val DarkColorScheme = darkColorScheme(
    primary = ColorPrimary,
    background = ColorGreyLight,
    surface = ColorGreyLight,
    onPrimary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White
)
