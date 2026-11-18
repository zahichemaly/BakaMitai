package com.zc.bakamitai.common.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// "Obsidian Signal" palette (docs/DESIGN.md).
//
// The Material 3 tonal palettes are seeded from the brand colors below: every dark role in the DESIGN.md
// front matter is an HCT tone of the violet, cyan or crimson seed, of the M3 error palette, or of an indigo
// neutral (hue 274.5, chroma 9.7) / neutral variant (hue 299.7, chroma 13.2).
//
// DESIGN.md defines the dark scheme only. The light scheme is derived from the same palettes with the standard
// M3 light tones (accents T40, surfaces T87–T100, on-surface T10, outline T50, …). The vibrant containers and
// their on-colors are the same in both schemes, as in M3 content-based schemes, so brand fills don't change
// between modes.

/** Neon Electric Violet: primary seed, progress gradient start, primary glow. */
internal val BrandViolet = Color(0xFF8B5CF6)

/** Cyan Flare: secondary seed, "Airing" status, progress gradient end, secondary glow. */
internal val BrandCyan = Color(0xFF06B6D4)

/** Crimson Hype: tertiary seed, "Live now" status. */
internal val BrandCrimson = Color(0xFFF43F5E)

/** Emerald: "Completed" status. */
internal val BrandEmerald = Color(0xFF10B981)

internal val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFD0BCFF),
    onPrimary = Color(0xFF3C0091),
    primaryContainer = Color(0xFFA078FF),
    onPrimaryContainer = Color(0xFF340080),
    inversePrimary = Color(0xFF6D3BD7),
    secondary = Color(0xFF4CD7F6),
    onSecondary = Color(0xFF003640),
    secondaryContainer = Color(0xFF03B5D3),
    onSecondaryContainer = Color(0xFF00424E),
    tertiary = Color(0xFFFFB2B7),
    onTertiary = Color(0xFF67001B),
    tertiaryContainer = Color(0xFFFF516A),
    onTertiaryContainer = Color(0xFF5B0017),
    background = Color(0xFF11131C),
    onBackground = Color(0xFFE1E1EF),
    surface = Color(0xFF11131C),
    onSurface = Color(0xFFE1E1EF),
    surfaceVariant = Color(0xFF32343E),
    onSurfaceVariant = Color(0xFFCBC3D7),
    surfaceTint = Color(0xFFD0BCFF),
    inverseSurface = Color(0xFFE1E1EF),
    inverseOnSurface = Color(0xFF2E303A),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    outline = Color(0xFF958EA0),
    outlineVariant = Color(0xFF494454),
    surfaceBright = Color(0xFF373943),
    surfaceContainer = Color(0xFF1D1F29),
    surfaceContainerHigh = Color(0xFF282933),
    surfaceContainerHighest = Color(0xFF32343E),
    surfaceContainerLow = Color(0xFF191B24),
    surfaceContainerLowest = Color(0xFF0C0E17),
    surfaceDim = Color(0xFF11131C),
)

internal val LightColorScheme = lightColorScheme(
    primary = Color(0xFF6D3BD7),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFA078FF),
    onPrimaryContainer = Color(0xFF340080),
    inversePrimary = Color(0xFFD0BCFF),
    secondary = Color(0xFF00687A),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFF03B5D3),
    onSecondaryContainer = Color(0xFF00424E),
    tertiary = Color(0xFFBC0B3B),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFFF516A),
    onTertiaryContainer = Color(0xFF5B0017),
    background = Color(0xFFFAF8FF),
    onBackground = Color(0xFF191B24),
    surface = Color(0xFFFAF8FF),
    onSurface = Color(0xFF191B24),
    surfaceVariant = Color(0xFFE1E1EF),
    onSurfaceVariant = Color(0xFF494454),
    surfaceTint = Color(0xFF6D3BD7),
    inverseSurface = Color(0xFF2E303A),
    inverseOnSurface = Color(0xFFF0F0FD),
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF93000A),
    outline = Color(0xFF7B7486),
    outlineVariant = Color(0xFFCBC3D7),
    surfaceBright = Color(0xFFFAF8FF),
    surfaceContainer = Color(0xFFEDEDFA),
    surfaceContainerHigh = Color(0xFFE7E7F5),
    surfaceContainerHighest = Color(0xFFE1E1EF),
    surfaceContainerLow = Color(0xFFF3F2FF),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceDim = Color(0xFFD9D9E6),
)
