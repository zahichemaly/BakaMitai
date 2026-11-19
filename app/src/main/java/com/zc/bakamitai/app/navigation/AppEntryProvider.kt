package com.zc.bakamitai.app.navigation

import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import com.zc.bakamitai.common.ui.navigation.Navigator
import com.zc.bakamitai.feature.bookmarks.presentation.navigation.bookmarksEntry
import com.zc.bakamitai.feature.details.presentation.navigation.detailsEntry
import com.zc.bakamitai.feature.details.presentation.navigation.navigateToDetails
import com.zc.bakamitai.feature.home.presentation.navigation.homeEntry
import com.zc.bakamitai.feature.schedule.presentation.navigation.navigateToSchedule
import com.zc.bakamitai.feature.schedule.presentation.navigation.scheduleEntry
import com.zc.bakamitai.feature.shows.presentation.navigation.allShowsEntry

/**
 * Every feature's entries, and the only place that wires features together, with [navigator]'s helpers. The
 * Settings callbacks are `null` until Settings exists.
 */
fun appEntryProvider(navigator: Navigator): (NavKey) -> NavEntry<NavKey> = entryProvider {
    homeEntry(onOpenDetails = navigator::navigateToDetails, onOpenSettings = null)
    scheduleEntry(onOpenDetails = navigator::navigateToDetails, onOpenSettings = null)
    allShowsEntry(onOpenDetails = navigator::navigateToDetails, onOpenSettings = null)
    bookmarksEntry(
        onOpenDetails = navigator::navigateToDetails,
        onDiscoverShows = navigator::navigateToSchedule,
        onOpenSettings = null,
    )
    detailsEntry(onNavigateBack = navigator::goBack)
}
