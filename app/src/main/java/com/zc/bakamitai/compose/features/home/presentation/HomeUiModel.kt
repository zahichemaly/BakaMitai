package com.zc.bakamitai.compose.features.home.presentation

import androidx.compose.runtime.Immutable
import com.zc.bakamitai.compose.features.home.domain.model.LatestRelease
import com.zc.bakamitai.compose.features.home.domain.model.TodaySchedule

@Immutable
data class HomeUiModel(
    val todayReleases: TodaySchedule = TodaySchedule(),
    val latestReleases: LatestRelease = emptyMap(),
)
