package com.zc.bakamitai.feature.shows.presentation.model

import androidx.compose.runtime.Immutable
import com.zc.bakamitai.common.ui.text.UiText
import com.zc.bakamitai.feature.shows.presentation.component.showrow.model.ShowRowUIModel
import kotlinx.collections.immutable.ImmutableList

/**
 * One letter of the list: its sticky header and its rows, as listed (filtered by the query).
 *
 * @property countLabel "14 SHOWS": the rows listed under it.
 */
@Immutable
data class ShowSectionUIModel(
    val letter: IndexLetterUIModel,
    val countLabel: UiText,
    val rows: ImmutableList<ShowRowUIModel>,
) {
    /** The header's lazy-list key, also the chip's. */
    val headerKey: String get() = "header-${letter.letter}"
}
