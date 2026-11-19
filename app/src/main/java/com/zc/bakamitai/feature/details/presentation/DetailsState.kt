package com.zc.bakamitai.feature.details.presentation

import androidx.compose.runtime.Immutable
import com.zc.bakamitai.common.ui.component.error.model.ErrorUIModel
import com.zc.bakamitai.feature.details.presentation.model.DetailsHeaderUIModel
import com.zc.bakamitai.feature.details.presentation.model.DownloadsUIState

/**
 * @property header set once the show page is parsed; the downloads load after it.
 * @property error the page failed and there is no header to show.
 * @property isBookmarked follows the bookmarks store.
 */
@Immutable
data class DetailsState(
    val header: DetailsHeaderUIModel? = null,
    val error: ErrorUIModel? = null,
    val isBookmarked: Boolean = false,
    val isSynopsisExpanded: Boolean = false,
    val downloads: DownloadsUIState = DownloadsUIState.Loading,
    val isRefreshing: Boolean = false,
) {
    /** Nothing to show yet: the whole screen is a skeleton. */
    val isLoading: Boolean get() = header == null && error == null
}
