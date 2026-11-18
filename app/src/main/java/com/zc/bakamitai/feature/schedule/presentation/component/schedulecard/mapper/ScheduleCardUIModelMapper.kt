package com.zc.bakamitai.feature.schedule.presentation.component.schedulecard.mapper

import com.zc.bakamitai.common.domain.model.ScheduledShow
import com.zc.bakamitai.common.ui.mapper.toTimeLabel
import com.zc.bakamitai.common.ui.text.UiText
import com.zc.bakamitai.feature.schedule.presentation.component.schedulecard.component.statusbadge.mapper.toStatusBadgeUIModel
import com.zc.bakamitai.feature.schedule.presentation.component.schedulecard.model.ScheduleCardUIModel

fun ScheduledShow.toScheduleCardUIModel(): ScheduleCardUIModel = ScheduleCardUIModel(
    key = "${show.slug.value}@$time",
    slug = show.slug.value,
    title = UiText.StringValue(show.title),
    coverUrl = show.coverUrl,
    timeLabel = time.toTimeLabel(),
    status = toStatusBadgeUIModel(),
)
