package com.zc.bakamitai.compose.features.home.presentation

import androidx.compose.runtime.Immutable
import com.zc.bakamitai.compose.core.domain.DateFormattedResult
import com.zc.bakamitai.compose.features.home.domain.model.Release
import com.zc.bakamitai.compose.features.home.domain.model.TodaySchedule

@Immutable
data class HomeUiModel(
    val todayReleases: TodaySchedule = TodaySchedule(),
    val latestReleases: Map<String, ReleaseUiModel> = emptyMap(),
    val isLoading: Boolean = false
) {
    fun hasData(): Boolean {
        return !isLoading && todayReleases.schedule.isNotEmpty() && latestReleases.isNotEmpty()
    }
}

data class ReleaseUiModel(
    val release: Release,
    val formattedDate: DateFormattedResult = DateFormattedResult.None
)

