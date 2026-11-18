package com.zc.bakamitai.feature.schedule.presentation.component.dayheader.model

import androidx.compose.runtime.Immutable
import com.zc.bakamitai.common.ui.text.UiText
import kotlinx.collections.immutable.ImmutableList

/**
 * The selected day's header.
 *
 * @property title "Thursday Schedule".
 * @property subtitle the device's offset, "Times in UTC+2".
 * @property countLabel the day's show count before filtering, "6 Shows Airing".
 * @property filters the status filter chips; empty when the day has no statuses.
 */
@Immutable
data class DayHeaderUIModel(
    val title: UiText,
    val subtitle: UiText,
    val countLabel: UiText,
    val filters: ImmutableList<StatusFilterChipUIModel>,
)
