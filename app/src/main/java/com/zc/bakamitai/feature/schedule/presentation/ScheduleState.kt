package com.zc.bakamitai.feature.schedule.presentation

import androidx.compose.runtime.Immutable
import com.zc.bakamitai.common.ui.component.error.model.ErrorUIModel
import com.zc.bakamitai.feature.schedule.presentation.component.daychip.model.DayChipUIModel
import com.zc.bakamitai.feature.schedule.presentation.model.ScheduleDayUIModel
import com.zc.bakamitai.feature.schedule.presentation.model.StatusFilter
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.datetime.DayOfWeek

/**
 * @property isLoading first load, with nothing to show yet.
 * @property isRefreshing a reload while content is shown (pull-to-refresh or a new day).
 * @property error the full-screen error, set only when there is no content.
 * @property days the day tabs in the week's order; empty until a load succeeds.
 * @property statusFilter the selected filter; it applies only to a day with statuses (today).
 * @property day the selected day, filtered; non-null once a load succeeded.
 */
@Immutable
data class ScheduleState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: ErrorUIModel? = null,
    val days: ImmutableList<DayChipUIModel> = persistentListOf(),
    val selectedDay: DayOfWeek? = null,
    val statusFilter: StatusFilter = StatusFilter.ALL,
    val day: ScheduleDayUIModel? = null,
) {
    val hasContent: Boolean get() = day != null
}
