package com.zc.bakamitai.feature.shows.presentation

import androidx.compose.runtime.Immutable
import com.zc.bakamitai.common.ui.component.error.model.ErrorUIModel
import com.zc.bakamitai.common.ui.text.UiText
import com.zc.bakamitai.feature.shows.presentation.model.ShowSectionUIModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

/**
 * @property isLoading first load, with nothing to show yet.
 * @property isRefreshing a pull-to-refresh while the list is shown.
 * @property error the full-screen error, set only when there is no content.
 * @property query the search field's text, as typed.
 * @property sections the catalogue's sections, filtered by [query].
 * @property footerLabel "Showing N shows" for the rows listed; non-null once a load succeeded.
 */
@Immutable
data class AllShowsState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: ErrorUIModel? = null,
    val query: String = "",
    val sections: ImmutableList<ShowSectionUIModel> = persistentListOf(),
    val footerLabel: UiText? = null,
) {
    val hasContent: Boolean get() = footerLabel != null

    /** The catalogue is loaded, but nothing matches the query. */
    val showNoResults: Boolean get() = hasContent && sections.isEmpty()
}
