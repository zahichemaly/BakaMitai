package com.zc.bakamitai.feature.schedule.presentation.component.schedulecard.component.statusbadge.mapper

import com.zc.bakamitai.common.domain.model.ScheduledShow
import com.zc.bakamitai.common.ui.resource.StringResource
import com.zc.bakamitai.common.ui.text.UiText
import com.zc.bakamitai.feature.schedule.presentation.component.schedulecard.component.statusbadge.model.StatusBadgeUIModel

/** Aired, Upcoming, or no badge when the show has no status (every day but today). */
fun ScheduledShow.toStatusBadgeUIModel(): StatusBadgeUIModel? = when (aired) {
    true -> StatusBadgeUIModel(UiText.StringResource(StringResource.schedule_status_aired), isUpcoming = false)
    false -> StatusBadgeUIModel(UiText.StringResource(StringResource.schedule_status_upcoming), isUpcoming = true)
    null -> null
}
