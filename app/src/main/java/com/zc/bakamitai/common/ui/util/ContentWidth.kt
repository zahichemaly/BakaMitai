package com.zc.bakamitai.common.ui.util

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max

/** Single-column content (a list, a search field) is at most this wide, centered, on wide windows. */
val MaxContentWidth = 720.dp

/**
 * The side padding that centers at most [MaxContentWidth] of content in a pane [maxWidth] wide, and never less
 * than [margin] (the screen's side margin for its window size).
 */
fun centeredContentPadding(maxWidth: Dp, margin: Dp): Dp = max(margin, (maxWidth - MaxContentWidth) / 2)
