package com.zc.bakamitai.feature.home.presentation.component.upcomingcard.mapper

import com.zc.bakamitai.common.ui.text.UiText
import com.zc.bakamitai.feature.home.domain.model.UpcomingBroadcast
import com.zc.bakamitai.feature.home.presentation.component.upcomingcard.model.UpcomingCardUIModel
import com.zc.bakamitai.feature.home.presentation.mapper.toEpisodeBadgeUIModel
import com.zc.bakamitai.feature.home.presentation.mapper.toTimeLabel

fun UpcomingBroadcast.toUIModel(): UpcomingCardUIModel = UpcomingCardUIModel(
    key = "${show.slug.value}@$time",
    slug = show.slug.value,
    title = UiText.StringValue(show.title),
    coverUrl = show.coverUrl,
    timeLabel = time.toTimeLabel(),
    episodeBadge = toEpisodeBadgeUIModel(),
)
