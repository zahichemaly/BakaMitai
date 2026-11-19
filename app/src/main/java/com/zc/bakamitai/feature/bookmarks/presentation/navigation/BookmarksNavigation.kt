package com.zc.bakamitai.feature.bookmarks.presentation.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.zc.bakamitai.feature.bookmarks.presentation.BookmarksRoute

/**
 * Registers Bookmarks, a top-level tab selected through `TopLevelDestination`; nothing else opens it yet, so there
 * is no `navigateToBookmarks()`.
 *
 * @param onOpenDetails opens a show's Details by slug.
 * @param onDiscoverShows opens the Schedule tab.
 * @param onOpenSettings opens Settings: the top bar's gear, and the alerts chip while alerts are on or switched off.
 */
fun EntryProviderScope<NavKey>.bookmarksEntry(
    onOpenDetails: (slug: String) -> Unit,
    onDiscoverShows: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    entry<BookmarksNavKey> {
        BookmarksRoute(
            onOpenDetails = onOpenDetails,
            onDiscoverShows = onDiscoverShows,
            onOpenSettings = onOpenSettings,
        )
    }
}
