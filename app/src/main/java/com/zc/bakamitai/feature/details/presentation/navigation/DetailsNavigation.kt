package com.zc.bakamitai.feature.details.presentation.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.zc.bakamitai.common.ui.navigation.Navigator
import com.zc.bakamitai.feature.details.presentation.DetailsRoute

/** Opens a show's Details on the current tab (once, however fast the taps). */
fun Navigator.navigateToDetails(slug: String) = navigate(DetailsNavKey(slug))

/** Opens a show's Details over the Home tab's root, whatever tab is selected (a notification tap). */
fun Navigator.navigateToDetailsFromStart(slug: String) = navigateFromStart(DetailsNavKey(slug))

/**
 * Registers Details.
 *
 * @param onNavigateBack leaves Details: the back arrow.
 */
fun EntryProviderScope<NavKey>.detailsEntry(onNavigateBack: () -> Unit) {
    entry<DetailsNavKey> { key ->
        DetailsRoute(slug = key.slug, onNavigateBack = onNavigateBack)
    }
}
