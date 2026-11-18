package com.zc.bakamitai.feature.home.presentation.component.todaysummary.mapper

import com.zc.bakamitai.common.ui.mapper.toUtcOffsetLabel
import com.zc.bakamitai.common.ui.resource.PluralResource
import com.zc.bakamitai.common.ui.text.UiText
import com.zc.bakamitai.feature.home.domain.model.TodayBroadcasts
import com.zc.bakamitai.feature.home.presentation.component.todaysummary.model.TodaySummaryUIModel

fun TodayBroadcasts.toTodaySummaryUIModel(): TodaySummaryUIModel = TodaySummaryUIModel(
    utcOffsetLabel = utcOffset.toUtcOffsetLabel(),
    upcomingCount = UiText.StringPlural(PluralResource.home_summary_airing, upcoming.size, listOf(upcoming.size)),
    releasedCount = UiText.StringPlural(PluralResource.home_summary_available, released.size, listOf(released.size)),
)
