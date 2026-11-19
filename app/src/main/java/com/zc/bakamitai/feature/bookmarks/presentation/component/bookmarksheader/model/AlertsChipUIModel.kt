package com.zc.bakamitai.feature.bookmarks.presentation.component.bookmarksheader.model

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.vector.ImageVector
import com.zc.bakamitai.common.ui.text.UiText

/**
 * Whether new-episode alerts can be posted: "ALERTS ON" (opens the notification settings) or "ALERTS OFF" (turns
 * them on).
 */
@Immutable
data class AlertsChipUIModel(
    val isEnabled: Boolean,
    val label: UiText,
    val icon: ImageVector,
    val contentDescription: UiText,
)
