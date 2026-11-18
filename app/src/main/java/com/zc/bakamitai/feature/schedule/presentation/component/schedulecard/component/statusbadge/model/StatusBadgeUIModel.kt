package com.zc.bakamitai.feature.schedule.presentation.component.schedulecard.component.statusbadge.model

import androidx.compose.runtime.Immutable
import com.zc.bakamitai.common.ui.text.UiText

/**
 * An Aired / Upcoming badge.
 *
 * @property isUpcoming drawn in the tertiary style with a pulsing dot; Aired is secondary and static.
 */
@Immutable
data class StatusBadgeUIModel(val label: UiText, val isUpcoming: Boolean)
