package com.zc.bakamitai.feature.settings.presentation.component.segmentedpreference.mapper

import com.zc.bakamitai.common.domain.model.TimeFormat
import com.zc.bakamitai.common.domain.model.WeekStart
import com.zc.bakamitai.common.ui.resource.StringResource
import com.zc.bakamitai.common.ui.text.UiText
import com.zc.bakamitai.feature.settings.presentation.component.segmentedpreference.model.SegmentUIState
import com.zc.bakamitai.feature.settings.presentation.model.TimeFormatOption
import com.zc.bakamitai.feature.settings.presentation.model.WeekStartOption
import kotlin.test.Test
import kotlin.test.assertEquals

class SegmentUIModelMapperTest {

    @Test
    fun `week start segments are Monday (Default) then Sunday, the stored one selected`() {
        assertEquals(
            listOf(
                SegmentUIState(
                    value = WeekStartOption.MONDAY,
                    label = UiText.StringResource(StringResource.day_monday),
                    qualifier = UiText.StringResource(StringResource.settings_option_default),
                    isSelected = true,
                ),
                SegmentUIState(
                    value = WeekStartOption.SUNDAY,
                    label = UiText.StringResource(StringResource.day_sunday),
                    qualifier = null,
                    isSelected = false,
                ),
            ),
            WeekStart.MONDAY.toWeekStartSegments(),
        )
        assertEquals(
            listOf(WeekStartOption.SUNDAY),
            WeekStart.SUNDAY.toWeekStartSegments().filter { it.isSelected }.map { it.value },
        )
    }

    @Test
    fun `time format segments are 12-hour (AM-PM) then 24-hour, the stored one selected`() {
        assertEquals(
            listOf(
                SegmentUIState(
                    value = TimeFormatOption.TWELVE_HOUR,
                    label = UiText.StringResource(StringResource.settings_time_format_12h),
                    qualifier = UiText.StringResource(StringResource.settings_time_format_12h_qualifier),
                    isSelected = false,
                ),
                SegmentUIState(
                    value = TimeFormatOption.TWENTY_FOUR_HOUR,
                    label = UiText.StringResource(StringResource.settings_time_format_24h),
                    qualifier = null,
                    isSelected = true,
                ),
            ),
            TimeFormat.TWENTY_FOUR_HOUR.toTimeFormatSegments(),
        )
        assertEquals(
            listOf(TimeFormatOption.TWELVE_HOUR),
            TimeFormat.TWELVE_HOUR.toTimeFormatSegments().filter { it.isSelected }.map { it.value },
        )
    }
}
