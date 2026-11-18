package com.zc.bakamitai.feature.schedule.presentation

import com.zc.bakamitai.feature.schedule.presentation.model.StatusFilter
import kotlinx.datetime.DayOfWeek

sealed interface ScheduleIntent {

    /** The first load, from `init`. */
    data object Load : ScheduleIntent

    /** The full-screen error's Retry button. */
    data object Retry : ScheduleIntent

    /** Pull-to-refresh: bypasses the weekly cache. */
    data object Refresh : ScheduleIntent

    /** The screen reached STARTED: reload if the local date changed since the last load. */
    data object ScreenStarted : ScheduleIntent

    data class DaySelected(val day: DayOfWeek) : ScheduleIntent

    data class FilterSelected(val filter: StatusFilter) : ScheduleIntent

    /** "Reset Filters" in the empty view. */
    data object ResetFilterClicked : ScheduleIntent

    data class ShowClicked(val slug: String) : ScheduleIntent

    data object SettingsClicked : ScheduleIntent
}
