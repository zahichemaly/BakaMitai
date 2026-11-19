package com.zc.bakamitai.feature.bookmarks.domain.model

/**
 * The result of toggling a bookmark.
 *
 * @property isBookmarked whether the show is saved afterwards.
 * @property isFirstBookmark whether it was just added and is now the only bookmark (the moment to ask for the
 * notification permission).
 */
data class BookmarkToggle(val isBookmarked: Boolean, val isFirstBookmark: Boolean)
