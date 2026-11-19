package com.zc.bakamitai.feature.schedule.presentation.component.schedulecard.mapper

import com.zc.bakamitai.common.domain.model.ScheduledShow
import com.zc.bakamitai.common.ui.component.timechip.model.TimeChipTone
import com.zc.bakamitai.common.ui.component.timechip.model.TimeChipUIModel
import com.zc.bakamitai.common.ui.mapper.toTimeLabel
import com.zc.bakamitai.common.ui.text.UiText
import com.zc.bakamitai.feature.schedule.presentation.component.schedulecard.component.statusbadge.mapper.toStatusBadgeUIModel
import com.zc.bakamitai.feature.schedule.presentation.component.schedulecard.model.ScheduleCardUIModel

fun ScheduledShow.toScheduleCardUIModel(): ScheduleCardUIModel {
    val status = toStatusBadgeUIModel()
    return ScheduleCardUIModel(
        key = "${show.slug.value}@$time",
        slug = show.slug.value,
        title = UiText.StringValue(show.title),
        coverUrl = show.coverUrl,
        time = TimeChipUIModel(
            label = time.toTimeLabel(),
            tone = if (status?.isUpcoming == true) TimeChipTone.UPCOMING else TimeChipTone.DEFAULT,
        ),
        status = status,
    )
}
