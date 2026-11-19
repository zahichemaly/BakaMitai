package com.zc.bakamitai.feature.settings.presentation.component.segmentedpreference.mapper

import com.zc.bakamitai.common.domain.model.TimeFormat
import com.zc.bakamitai.common.domain.model.WeekStart
import com.zc.bakamitai.common.ui.resource.StringResource
import com.zc.bakamitai.common.ui.text.UiText
import com.zc.bakamitai.feature.settings.presentation.component.segmentedpreference.model.SegmentUIState
import com.zc.bakamitai.feature.settings.presentation.model.TimeFormatOption
import com.zc.bakamitai.feature.settings.presentation.model.WeekStartOption
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

/** "Monday (Default)" and "Sunday", this week start selected. */
fun WeekStart.toWeekStartSegments(): ImmutableList<SegmentUIState<WeekStartOption>> = persistentListOf(
    SegmentUIState(
        value = WeekStartOption.MONDAY,
        label = UiText.StringResource(StringResource.day_monday),
        qualifier = UiText.StringResource(StringResource.settings_option_default),
        isSelected = this == WeekStart.MONDAY,
    ),
    SegmentUIState(
        value = WeekStartOption.SUNDAY,
        label = UiText.StringResource(StringResource.day_sunday),
        qualifier = null,
        isSelected = this == WeekStart.SUNDAY,
    ),
)

/** "12-hour (AM/PM)" and "24-hour", this format selected. */
fun TimeFormat.toTimeFormatSegments(): ImmutableList<SegmentUIState<TimeFormatOption>> = persistentListOf(
    SegmentUIState(
        value = TimeFormatOption.TWELVE_HOUR,
        label = UiText.StringResource(StringResource.settings_time_format_12h),
        qualifier = UiText.StringResource(StringResource.settings_time_format_12h_qualifier),
        isSelected = this == TimeFormat.TWELVE_HOUR,
    ),
    SegmentUIState(
        value = TimeFormatOption.TWENTY_FOUR_HOUR,
        label = UiText.StringResource(StringResource.settings_time_format_24h),
        qualifier = null,
        isSelected = this == TimeFormat.TWENTY_FOUR_HOUR,
    ),
)
