package com.zc.bakamitai.feature.schedule.presentation.model

import androidx.compose.runtime.Immutable
import com.zc.bakamitai.feature.schedule.presentation.component.dayheader.model.DayHeaderUIModel
import com.zc.bakamitai.feature.schedule.presentation.component.schedulecard.model.ScheduleCardUIModel
import kotlinx.collections.immutable.ImmutableList

/**
 * The selected day, filtered.
 *
 * @property cards the shows that pass the filter, by time.
 * @property empty the message shown instead of the cards; non-null exactly when [cards] is empty.
 */
@Immutable
data class ScheduleDayUIModel(
    val header: DayHeaderUIModel,
    val cards: ImmutableList<ScheduleCardUIModel>,
    val empty: ScheduleEmptyUIModel?,
)
