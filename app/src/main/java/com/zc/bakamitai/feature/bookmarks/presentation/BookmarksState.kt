package com.zc.bakamitai.feature.bookmarks.presentation

import androidx.compose.runtime.Immutable
import com.zc.bakamitai.common.ui.component.error.model.ErrorUIModel
import com.zc.bakamitai.feature.bookmarks.presentation.component.bookmarkcard.model.BookmarkCardUIModel
import com.zc.bakamitai.feature.bookmarks.presentation.component.bookmarksheader.model.BookmarksHeaderUIModel
import com.zc.bakamitai.feature.bookmarks.presentation.model.BookmarkSortOption
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

/**
 * The Bookmarks screen (docs/5-bookmarks.md §6.11).
 *
 * @property isLoading until the bookmarks are first read.
 * @property error the bookmarks couldn't be read (a disk failure).
 * @property isRefreshing a pull-to-refresh of the airing slots is running.
 */
@Immutable
data class BookmarksState(
    val isLoading: Boolean = true,
    val error: ErrorUIModel? = null,
    val selectedSort: BookmarkSortOption = BookmarkSortOption.RECENTLY_ADDED,
    val header: BookmarksHeaderUIModel = BookmarksHeaderUIModel(),
    val cards: ImmutableList<BookmarkCardUIModel> = persistentListOf(),
    val isRefreshing: Boolean = false,
) {
    val isEmpty: Boolean get() = !isLoading && error == null && cards.isEmpty()
}
