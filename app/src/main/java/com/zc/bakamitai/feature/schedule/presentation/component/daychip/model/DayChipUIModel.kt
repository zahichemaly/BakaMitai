package com.zc.bakamitai.feature.schedule.presentation.component.daychip.model

import androidx.compose.runtime.Immutable
import com.zc.bakamitai.common.ui.text.UiText
import kotlinx.datetime.DayOfWeek

/**
 * A day tab.
 *
 * @property weekdayLabel the short day name, "THU".
 * @property dateLabel this week's day of the month, unpadded, "9".
 * @property contentDescription what TalkBack reads, "Thursday 19, today".
 * @property isToday marks today's tab with a dot.
 */
@Immutable
data class DayChipUIModel(
    val dayOfWeek: DayOfWeek,
    val weekdayLabel: UiText,
    val dateLabel: UiText,
    val contentDescription: UiText,
    val isToday: Boolean,
)
