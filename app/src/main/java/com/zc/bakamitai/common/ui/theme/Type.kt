package com.zc.bakamitai.common.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.zc.bakamitai.common.ui.resource.FontResource

// Plus Jakarta Sans: titles, synopses, natural language. Space Grotesk: episode tags, countdowns, badges
// (always uppercase in badges, with tabular numbers). Both are bundled variable fonts (res/font); each
// weight below is an instance of the font's `wght` axis. Licenses: assets/licenses/.

/** Plus Jakarta Sans, the content family. */
val PlusJakartaSans = FontFamily(
    listOf(FontWeight.Normal, FontWeight.SemiBold, FontWeight.Bold, FontWeight.ExtraBold).map { weight ->
        Font(
            resId = FontResource.plus_jakarta_sans,
            weight = weight,
            variationSettings = FontVariation.Settings(FontVariation.weight(weight.weight)),
        )
    },
)

/** Space Grotesk, the data and label family. */
val SpaceGrotesk = FontFamily(
    listOf(FontWeight.Medium, FontWeight.SemiBold, FontWeight.Bold).map { weight ->
        Font(
            resId = FontResource.space_grotesk,
            weight = weight,
            variationSettings = FontVariation.Settings(FontVariation.weight(weight.weight)),
        )
    },
)

/** Tabular figures, so countdowns and episode numbers don't jitter. */
private const val TABULAR_NUMBERS = "tnum"

private val Defaults = Typography()

/**
 * DESIGN.md type scale mapped onto the Material 3 roles. Roles the design doesn't name (display-sm,
 * title-lg, title-sm) are interpolated from their neighbors so the scale stays ordered.
 */
internal val BakaMitaiTypography = Typography(
    // display-lg
    displayLarge = Defaults.displayLarge.copy(
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 40.sp,
        lineHeight = 48.sp,
        letterSpacing = (-0.02).em,
    ),
    // display-lg-mobile
    displayMedium = Defaults.displayMedium.copy(
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = (-0.02).em,
    ),
    displaySmall = Defaults.displaySmall.copy(
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = (-0.02).em,
    ),
    // headline-lg
    headlineLarge = Defaults.headlineLarge.copy(
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = (-0.015).em,
    ),
    // headline-md: anime titles on hero and detail headers
    headlineMedium = Defaults.headlineMedium.copy(
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = (-0.01).em,
    ),
    // headline-sm
    headlineSmall = Defaults.headlineSmall.copy(
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp,
    ),
    // App bar titles
    titleLarge = Defaults.titleLarge.copy(
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        letterSpacing = (-0.01).em,
    ),
    // title-md: anime titles on cards (with the -0.01em title tracking)
    titleMedium = Defaults.titleMedium.copy(
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 22.sp,
        letterSpacing = (-0.01).em,
    ),
    titleSmall = Defaults.titleSmall.copy(
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = (-0.01).em,
    ),
    // body-lg
    bodyLarge = Defaults.bodyLarge.copy(
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp,
    ),
    // body-md
    bodyMedium = Defaults.bodyMedium.copy(
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.sp,
    ),
    // body-sm
    bodySmall = Defaults.bodySmall.copy(
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.sp,
    ),
    // label-lg: relative countdowns ("in 2h 14m"), buttons
    labelLarge = Defaults.labelLarge.copy(
        fontFamily = SpaceGrotesk,
        fontWeight = FontWeight.Bold,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.04.em,
        fontFeatureSettings = TABULAR_NUMBERS,
    ),
    // label-md: episode tags ("EP 12/24"), badges
    labelMedium = Defaults.labelMedium.copy(
        fontFamily = SpaceGrotesk,
        fontWeight = FontWeight.SemiBold,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.06.em,
        fontFeatureSettings = TABULAR_NUMBERS,
    ),
    // label-sm: status badges
    labelSmall = Defaults.labelSmall.copy(
        fontFamily = SpaceGrotesk,
        fontWeight = FontWeight.Medium,
        fontSize = 10.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.08.em,
        fontFeatureSettings = TABULAR_NUMBERS,
    ),
)
