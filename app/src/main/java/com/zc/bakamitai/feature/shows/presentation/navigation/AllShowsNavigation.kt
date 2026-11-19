package com.zc.bakamitai.feature.shows.presentation.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.zc.bakamitai.feature.shows.presentation.AllShowsRoute

/**
 * Registers All Shows. All Shows is a top-level tab, selected through `TopLevelDestination`; no feature opens it,
 * so there is no `navigateToAllShows()`.
 *
 * @param onOpenDetails opens a show's Details by slug.
 * @param onOpenSettings opens Settings (the top bar's gear).
 */
fun EntryProviderScope<NavKey>.allShowsEntry(onOpenDetails: (slug: String) -> Unit, onOpenSettings: () -> Unit) {
    entry<AllShowsNavKey> {
        AllShowsRoute(onOpenDetails = onOpenDetails, onOpenSettings = onOpenSettings)
    }
}
