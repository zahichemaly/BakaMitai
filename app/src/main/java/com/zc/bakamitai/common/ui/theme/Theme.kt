package com.zc.bakamitai.common.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable

/**
 * The BakaMitai theme ("Obsidian Signal", docs/DESIGN.md): Material 3 colors, type and shapes, plus the
 * [BakaMitaiTheme] tokens Material has no slot for. The palette is fixed (no dynamic color), so covers and
 * brand accents look the same on every device.
 */
@Composable
fun BakaMitaiTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    CompositionLocalProvider(
        LocalExtendedColorScheme provides if (darkTheme) DarkExtendedColorScheme else LightExtendedColorScheme,
    ) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
            shapes = BakaMitaiShapes,
            typography = BakaMitaiTypography,
            content = content,
        )
    }
}

/** Design tokens beyond `MaterialTheme`: `BakaMitaiTheme.extendedColors.airing`, `BakaMitaiTheme.spacing.md`. */
object BakaMitaiTheme {

    /** Status, gradient and glow colors for the current light/dark mode. */
    val extendedColors: ExtendedColorScheme
        @Composable
        @ReadOnlyComposable
        get() = LocalExtendedColorScheme.current

    /** Spacing scale; the same in every mode. */
    val spacing: Spacing = Spacing()
}
