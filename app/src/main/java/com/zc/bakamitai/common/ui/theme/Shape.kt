package com.zc.bakamitai.common.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * DESIGN.md radii on the Material 3 shape scale. Pills (badges, buttons, chips, the navigation indicator)
 * use `CircleShape`.
 *
 * - extraSmall 8 dp: small insets, focus frames
 * - small 12 dp: inner thumbnails, progress handles, tooltips
 * - medium 16 dp: default container rounding
 * - large 24 dp: poster cards, episode cards, the floating search bar
 * - extraLarge 32 dp: bottom sheets, full-width dialogs, hero spotlight banners
 */
internal val BakaMitaiShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp),
)
