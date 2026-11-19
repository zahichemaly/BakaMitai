package com.zc.bakamitai.feature.bookmarks.domain.model

/** The orders of the Bookmarks grid. The chosen one is saved by name: never rename a constant. */
enum class BookmarkSort {
    /** Newest bookmark first. */
    RECENTLY_ADDED,

    /** Title A–Z, ignoring case. */
    TITLE,

    /** Next airing soonest first; shows that aren't airing last, by title. */
    NEXT_AIRING,
}
