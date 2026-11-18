package com.zc.bakamitai.common.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * DESIGN.md tokens that have no Material 3 color role. Read them with `BakaMitaiTheme.extendedColors`.
 *
 * @property airing "Airing" badge and upcoming-broadcast markers (cyan).
 * @property live "Live now" / airing-today badge (crimson).
 * @property completed "Completed" / finished badge (emerald).
 * @property progressStart start of the episode progress gradient.
 * @property progressEnd end of the episode progress gradient.
 * @property glowPrimary radial glow behind high-priority primary actions ("Watch now").
 * @property glowSecondary radial glow behind countdown and airing markers.
 */
@Immutable
data class ExtendedColorScheme(
    val airing: StatusColors,
    val live: StatusColors,
    val completed: StatusColors,
    val progressStart: Color,
    val progressEnd: Color,
    val glowPrimary: Color,
    val glowSecondary: Color,
)

private const val AIRING_CONTAINER_ALPHA = 0.16f
private const val AIRING_OUTLINE_ALPHA = 0.4f
private const val LIVE_CONTAINER_ALPHA = 0.2f
private const val LIVE_OUTLINE_ALPHA = 0.5f
private const val COMPLETED_CONTAINER_ALPHA = 0.15f
private const val COMPLETED_OUTLINE_ALPHA = 0.4f
private const val GLOW_ALPHA = 0.35f

private fun statusColors(brand: Color, content: Color, containerAlpha: Float, outlineAlpha: Float) = StatusColors(
    content = content,
    container = brand.copy(alpha = containerAlpha),
    outline = brand.copy(alpha = outlineAlpha),
)

private fun extendedColorScheme(airingContent: Color, liveContent: Color, completedContent: Color) =
    ExtendedColorScheme(
        airing = statusColors(BrandCyan, airingContent, AIRING_CONTAINER_ALPHA, AIRING_OUTLINE_ALPHA),
        live = statusColors(BrandCrimson, liveContent, LIVE_CONTAINER_ALPHA, LIVE_OUTLINE_ALPHA),
        completed = statusColors(BrandEmerald, completedContent, COMPLETED_CONTAINER_ALPHA, COMPLETED_OUTLINE_ALPHA),
        progressStart = BrandViolet,
        progressEnd = BrandCyan,
        glowPrimary = BrandViolet.copy(alpha = GLOW_ALPHA),
        glowSecondary = BrandCyan.copy(alpha = GLOW_ALPHA),
    )

/** DESIGN.md values: the brand colors themselves label the badges on the dark canvas. */
internal val DarkExtendedColorScheme = extendedColorScheme(
    airingContent = BrandCyan,
    liveContent = BrandCrimson,
    completedContent = BrandEmerald,
)

/** Same hues at tone 40, so badge labels keep a 6:1 contrast on the light surface. */
internal val LightExtendedColorScheme = extendedColorScheme(
    airingContent = Color(0xFF00687A),
    liveContent = Color(0xFFBC0B3B),
    completedContent = Color(0xFF006C49),
)

internal val LocalExtendedColorScheme = staticCompositionLocalOf { DarkExtendedColorScheme }
