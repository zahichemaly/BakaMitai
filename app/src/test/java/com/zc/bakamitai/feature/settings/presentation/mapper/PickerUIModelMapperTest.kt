package com.zc.bakamitai.feature.settings.presentation.mapper

import com.zc.bakamitai.common.domain.model.ThemeMode
import com.zc.bakamitai.common.domain.model.TimeFormat
import com.zc.bakamitai.common.domain.model.WeekStart
import com.zc.bakamitai.common.ui.resource.StringResource
import com.zc.bakamitai.common.ui.text.UiText
import com.zc.bakamitai.feature.settings.presentation.model.PickerOptionUIState
import com.zc.bakamitai.feature.settings.presentation.model.PickerUIState
import com.zc.bakamitai.feature.settings.presentation.model.ThemeOption
import com.zc.bakamitai.feature.settings.presentation.model.TimeFormatOption
import com.zc.bakamitai.feature.settings.presentation.model.WeekStartOption
import kotlinx.collections.immutable.persistentListOf
import kotlin.test.Test
import kotlin.test.assertEquals

class PickerUIModelMapperTest {

    @Test
    fun `week start picker lists Monday then Sunday, the stored one selected and shown on the row`() {
        assertEquals(
            PickerUIState(
                title = UiText.StringResource(StringResource.settings_week_start_title),
                valueLabel = UiText.StringResource(StringResource.day_monday),
                options = persistentListOf(
                    PickerOptionUIState(
                        option = WeekStartOption.MONDAY,
                        label = UiText.StringResource(StringResource.day_monday),
                        description = null,
                        isSelected = true,
                    ),
                    PickerOptionUIState(
                        option = WeekStartOption.SUNDAY,
                        label = UiText.StringResource(StringResource.day_sunday),
                        description = null,
                        isSelected = false,
                    ),
                ),
            ),
            WeekStart.MONDAY.toWeekStartPicker(),
        )

        val sunday = WeekStart.SUNDAY.toWeekStartPicker()
        assertEquals(UiText.StringResource(StringResource.day_sunday), sunday.valueLabel)
        assertEquals(listOf(WeekStartOption.SUNDAY), sunday.options.filter { it.isSelected }.map { it.option })
    }

    @Test
    fun `time format picker lists 12-hour (AM-PM) then 24-hour, the stored one selected and shown on the row`() {
        assertEquals(
            PickerUIState(
                title = UiText.StringResource(StringResource.settings_time_format_title),
                valueLabel = UiText.StringResource(StringResource.settings_time_format_24h),
                options = persistentListOf(
                    PickerOptionUIState(
                        option = TimeFormatOption.TWELVE_HOUR,
                        label = UiText.StringResource(StringResource.settings_time_format_12h),
                        description = null,
                        isSelected = false,
                    ),
                    PickerOptionUIState(
                        option = TimeFormatOption.TWENTY_FOUR_HOUR,
                        label = UiText.StringResource(StringResource.settings_time_format_24h),
                        description = null,
                        isSelected = true,
                    ),
                ),
            ),
            TimeFormat.TWENTY_FOUR_HOUR.toTimeFormatPicker(),
        )

        val twelveHour = TimeFormat.TWELVE_HOUR.toTimeFormatPicker()
        assertEquals(UiText.StringResource(StringResource.settings_time_format_12h), twelveHour.valueLabel)
        assertEquals(
            listOf(TimeFormatOption.TWELVE_HOUR),
            twelveHour.options.filter { it.isSelected }.map { it.option },
        )
    }

    @Test
    fun `theme picker lists System default, Light and Dark with their descriptions`() {
        val picker = ThemeMode.SYSTEM.toThemePicker()

        assertEquals(UiText.StringResource(StringResource.settings_theme_title), picker.title)
        assertEquals(listOf(ThemeOption.SYSTEM, ThemeOption.LIGHT, ThemeOption.DARK), picker.options.map { it.option })
        assertEquals(
            listOf(
                StringResource.settings_theme_system,
                StringResource.settings_theme_light,
                StringResource.settings_theme_dark,
            ).map { UiText.StringResource(it) },
            picker.options.map { it.label },
        )
        assertEquals(
            listOf(
                StringResource.settings_theme_system_description,
                StringResource.settings_theme_light_description,
                StringResource.settings_theme_dark_description,
            ).map { UiText.StringResource(it) },
            picker.options.map { it.description },
        )
    }

    @Test
    fun `theme picker selects the stored mode and shows its label on the row`() {
        val expected = mapOf(
            ThemeMode.SYSTEM to (ThemeOption.SYSTEM to StringResource.settings_theme_system),
            ThemeMode.LIGHT to (ThemeOption.LIGHT to StringResource.settings_theme_light),
            ThemeMode.DARK to (ThemeOption.DARK to StringResource.settings_theme_dark),
        )

        expected.forEach { (mode, values) ->
            val (option, label) = values
            val picker = mode.toThemePicker()
            assertEquals(listOf(option), picker.options.filter { it.isSelected }.map { it.option })
            assertEquals(UiText.StringResource(label), picker.valueLabel)
        }
    }
}
