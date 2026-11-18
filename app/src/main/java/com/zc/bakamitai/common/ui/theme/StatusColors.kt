package com.zc.bakamitai.common.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/**
 * Colors of one broadcast status badge (DESIGN.md "Status Badges & Chips"): [content] for the label and the
 * status pip, [container] for the translucent fill and [outline] for the 1 dp border.
 */
@Immutable
data class StatusColors(val content: Color, val container: Color, val outline: Color)
