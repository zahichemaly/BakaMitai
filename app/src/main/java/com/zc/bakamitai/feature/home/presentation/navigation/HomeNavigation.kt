package com.zc.bakamitai.feature.home.presentation.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.zc.bakamitai.feature.home.presentation.HomeRoute

/**
 * Registers Home. Home is a top-level tab, selected through `TopLevelDestination`, so there is no
 * `navigateToHome()`.
 *
 * @param onOpenDetails opens a show's Details by slug.
 * @param onOpenSettings opens Settings; `null` until Settings exists.
 */
fun EntryProviderScope<NavKey>.homeEntry(onOpenDetails: (slug: String) -> Unit, onOpenSettings: (() -> Unit)?) {
    entry<HomeNavKey> {
        HomeRoute(onOpenDetails = onOpenDetails, onOpenSettings = onOpenSettings)
    }
}
