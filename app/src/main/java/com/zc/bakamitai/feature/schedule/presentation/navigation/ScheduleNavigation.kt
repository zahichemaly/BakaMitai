package com.zc.bakamitai.feature.schedule.presentation.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.zc.bakamitai.common.ui.navigation.Navigator
import com.zc.bakamitai.feature.schedule.presentation.ScheduleRoute

/** Selects the Schedule tab (Bookmarks' empty state). */
fun Navigator.navigateToSchedule() = navigate(ScheduleNavKey)

/**
 * Registers Schedule, a top-level tab selected through `TopLevelDestination` or [navigateToSchedule].
 *
 * @param onOpenDetails opens a show's Details by slug.
 * @param onOpenSettings opens Settings; `null` until Settings exists.
 */
fun EntryProviderScope<NavKey>.scheduleEntry(onOpenDetails: (slug: String) -> Unit, onOpenSettings: (() -> Unit)?) {
    entry<ScheduleNavKey> {
        ScheduleRoute(onOpenDetails = onOpenDetails, onOpenSettings = onOpenSettings)
    }
}
