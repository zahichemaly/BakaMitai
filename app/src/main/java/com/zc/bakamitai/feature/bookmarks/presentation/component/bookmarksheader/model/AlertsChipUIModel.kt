package com.zc.bakamitai.feature.bookmarks.presentation.component.bookmarksheader.model

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.vector.ImageVector
import com.zc.bakamitai.common.ui.text.UiText

/**
 * Whether new-episode alerts are posted: "ALERTS ON" (opens Settings), or "ALERTS OFF", either switched off in
 * Settings (opens Settings) or blocked by Android (turns them on).
 *
 * @property isEnabled alerts are on: switched on and allowed by Android.
 */
@Immutable
data class AlertsChipUIModel(
    val isEnabled: Boolean,
    val label: UiText,
    val icon: ImageVector,
    val contentDescription: UiText,
)
