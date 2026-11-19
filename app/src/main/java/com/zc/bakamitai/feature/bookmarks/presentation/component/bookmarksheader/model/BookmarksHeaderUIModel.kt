package com.zc.bakamitai.feature.bookmarks.presentation.component.bookmarksheader.model

import androidx.compose.runtime.Immutable
import com.zc.bakamitai.common.ui.text.UiText
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

/**
 * The grid's header: the sort chips, then "SAVED SHOWS · 8 shows" and the alerts chip.
 *
 * @property countLabel "8 shows"; `null` before the bookmarks load.
 * @property alertsChip `null` until the alerts were checked (the chip is hidden).
 */
@Immutable
data class BookmarksHeaderUIModel(
    val sortChips: ImmutableList<SortChipUIModel> = persistentListOf(),
    val countLabel: UiText? = null,
    val alertsChip: AlertsChipUIModel? = null,
)
