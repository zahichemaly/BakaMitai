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
import kotlinx.collections.immutable.toImmutableList

/** *First day of week*: Monday, Sunday; this week start selected. */
fun WeekStart.toWeekStartPicker(): PickerUIState<WeekStartOption> = pickerOf(
    title = UiText.StringResource(StringResource.settings_week_start_title),
    options = WeekStartOption.entries,
    selected = when (this) {
        WeekStart.MONDAY -> WeekStartOption.MONDAY
        WeekStart.SUNDAY -> WeekStartOption.SUNDAY
    },
    label = WeekStartOption::toLabel,
)

/** *Time format*: 12-hour (AM/PM), 24-hour; this format selected. */
fun TimeFormat.toTimeFormatPicker(): PickerUIState<TimeFormatOption> = pickerOf(
    title = UiText.StringResource(StringResource.settings_time_format_title),
    options = TimeFormatOption.entries,
    selected = when (this) {
        TimeFormat.TWELVE_HOUR -> TimeFormatOption.TWELVE_HOUR
        TimeFormat.TWENTY_FOUR_HOUR -> TimeFormatOption.TWENTY_FOUR_HOUR
    },
    label = TimeFormatOption::toLabel,
)

/** *Theme*: System default, Light, Dark, each with a description; this mode selected. */
fun ThemeMode.toThemePicker(): PickerUIState<ThemeOption> {
    val selected = when (this) {
        ThemeMode.SYSTEM -> ThemeOption.SYSTEM
        ThemeMode.LIGHT -> ThemeOption.LIGHT
        ThemeMode.DARK -> ThemeOption.DARK
    }
    return pickerOf(
        title = UiText.StringResource(StringResource.settings_theme_title),
        options = ThemeOption.entries,
        selected = selected,
        label = ThemeOption::toLabel,
        description = ThemeOption::toDescription,
    )
}

private fun <T> pickerOf(
    title: UiText,
    options: List<T>,
    selected: T,
    label: (T) -> UiText,
    description: (T) -> UiText? = { null },
): PickerUIState<T> = PickerUIState(
    title = title,
    valueLabel = label(selected),
    options = options
        .map { option ->
            PickerOptionUIState(
                option = option,
                label = label(option),
                description = description(option),
                isSelected = option == selected,
            )
        }
        .toImmutableList(),
)

private fun WeekStartOption.toLabel(): UiText = UiText.StringResource(
    when (this) {
        WeekStartOption.MONDAY -> StringResource.day_monday
        WeekStartOption.SUNDAY -> StringResource.day_sunday
    },
)

private fun TimeFormatOption.toLabel(): UiText = UiText.StringResource(
    when (this) {
        TimeFormatOption.TWELVE_HOUR -> StringResource.settings_time_format_12h
        TimeFormatOption.TWENTY_FOUR_HOUR -> StringResource.settings_time_format_24h
    },
)

private fun ThemeOption.toLabel(): UiText = UiText.StringResource(
    when (this) {
        ThemeOption.SYSTEM -> StringResource.settings_theme_system
        ThemeOption.LIGHT -> StringResource.settings_theme_light
        ThemeOption.DARK -> StringResource.settings_theme_dark
    },
)

private fun ThemeOption.toDescription(): UiText = UiText.StringResource(
    when (this) {
        ThemeOption.SYSTEM -> StringResource.settings_theme_system_description
        ThemeOption.LIGHT -> StringResource.settings_theme_light_description
        ThemeOption.DARK -> StringResource.settings_theme_dark_description
    },
)
