package com.zc.bakamitai.common.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * DESIGN.md spacing: an 8 dp grid with 4 dp micro-steps. Read it as `BakaMitaiTheme.spacing`.
 *
 * Rhythm: [sm] between a title and its tags, [md] between list elements, [xl] between major carousels.
 * Screen edges use [margin] on compact widths and [marginTablet] from medium widths up; grid gaps use
 * [gutter] (or [gutterSmall] for dense grids).
 */
@Immutable
data class Spacing(
    val xs: Dp = 4.dp,
    val sm: Dp = 8.dp,
    val md: Dp = 16.dp,
    val lg: Dp = 24.dp,
    val xl: Dp = 32.dp,
    val gutter: Dp = 16.dp,
    val gutterSmall: Dp = 12.dp,
    val margin: Dp = 16.dp,
    val marginTablet: Dp = 24.dp,
)
