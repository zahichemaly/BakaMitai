package com.zc.bakamitai.feature.bookmarks.domain.model

/** The orders of the Bookmarks grid. */
enum class BookmarkSort {
    /** Newest bookmark first. */
    RECENTLY_ADDED,

    /** Title A–Z, ignoring case. */
    TITLE,

    /** Next airing soonest first; shows that aren't airing last, by title. */
    NEXT_AIRING,
}
