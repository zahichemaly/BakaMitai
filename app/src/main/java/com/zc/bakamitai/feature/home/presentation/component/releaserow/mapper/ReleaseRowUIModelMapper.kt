package com.zc.bakamitai.feature.home.presentation.component.releaserow.mapper

import com.zc.bakamitai.common.ui.text.UiText
import com.zc.bakamitai.feature.home.domain.model.Release
import com.zc.bakamitai.feature.home.presentation.component.releaserow.model.ReleaseRowUIModel
import com.zc.bakamitai.feature.home.presentation.mapper.toEpisodeBadgeUIModel
import com.zc.bakamitai.feature.home.presentation.mapper.toTimeLabel
import kotlinx.datetime.TimeZone

/** [timeZone] is the zone "today" was computed in, so the time matches the section's date. */
fun Release.toUIModel(timeZone: TimeZone): ReleaseRowUIModel = ReleaseRowUIModel(
    key = "${show.slug.value}#$episode",
    slug = show.slug.value,
    title = UiText.StringValue(show.title),
    coverUrl = show.coverUrl,
    timeLabel = releasedAt.toTimeLabel(timeZone),
    episodeBadge = toEpisodeBadgeUIModel(),
)
