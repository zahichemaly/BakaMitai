package com.zc.bakamitai.feature.schedule.presentation.component.schedulecard.model

import androidx.compose.runtime.Immutable
import com.zc.bakamitai.common.ui.text.UiText
import com.zc.bakamitai.feature.schedule.presentation.component.schedulecard.component.statusbadge.model.StatusBadgeUIModel

/**
 * A show on the selected day.
 *
 * @property key unique within a day: the slug and the scheduled time.
 * @property slug the show to open.
 * @property coverUrl absolute cover URL, or `null` for the placeholder.
 * @property timeLabel the scheduled time in the device's zone, "5:00 PM".
 * @property status Aired / Upcoming, only on today's cards; `null` otherwise.
 */
@Immutable
data class ScheduleCardUIModel(
    val key: String,
    val slug: String,
    val title: UiText,
    val coverUrl: String?,
    val timeLabel: UiText,
    val status: StatusBadgeUIModel?,
)
