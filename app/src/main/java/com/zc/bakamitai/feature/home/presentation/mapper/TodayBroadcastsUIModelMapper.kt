package com.zc.bakamitai.feature.home.presentation.mapper

import com.zc.bakamitai.common.ui.resource.PluralResource
import com.zc.bakamitai.common.ui.text.UiText
import com.zc.bakamitai.feature.home.domain.model.TodayBroadcasts

/** The Upcoming section's count pill, "9 shows". */
fun TodayBroadcasts.toUpcomingCountLabel(): UiText =
    UiText.StringPlural(PluralResource.home_section_upcoming_count, upcoming.size, listOf(upcoming.size))
