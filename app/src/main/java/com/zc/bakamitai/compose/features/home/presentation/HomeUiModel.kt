package com.zc.bakamitai.compose.features.home.presentation

import androidx.compose.runtime.Immutable
import com.zc.bakamitai.compose.core.domain.DateFormattedResult
import com.zc.bakamitai.compose.features.home.domain.model.LatestRelease
import com.zc.bakamitai.compose.features.home.domain.model.Release
import com.zc.bakamitai.compose.features.home.domain.model.TodaySchedule

@Immutable
data class HomeUiModel(
    val todayReleases: TodaySchedule = TodaySchedule(),
    val latestReleases: Map<String, ReleaseUiModel> = emptyMap(),
    val isLoading: Boolean = false
)

data class ReleaseUiModel(
    val release: Release,
    val formattedDate: DateFormattedResult = DateFormattedResult.None
)

data class TodayScheduleUiModel(
    val todaySchedule: TodaySchedule,
)
