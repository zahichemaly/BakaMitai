package com.zc.bakamitai.feature.schedule.presentation.model

import androidx.compose.runtime.Immutable
import com.zc.bakamitai.common.ui.text.UiText

/**
 * The message shown instead of the cards: the day has no shows, or the filter matches none of them.
 *
 * @property showResetAction whether "Reset Filters" is offered: only when a filter hides every show.
 */
@Immutable
data class ScheduleEmptyUIModel(val title: UiText, val message: UiText, val showResetAction: Boolean)
