package com.zc.bakamitai.feature.schedule.presentation.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.zc.bakamitai.feature.schedule.presentation.ScheduleRoute

/**
 * Registers Schedule. Schedule is a top-level tab, selected through `TopLevelDestination`; no feature opens it
 * yet, so there is no `navigateToSchedule()`.
 *
 * @param onOpenDetails opens a show by slug; `null` until the Details screen exists.
 * @param onOpenSettings opens Settings; `null` until Settings exists.
 */
fun EntryProviderScope<NavKey>.scheduleEntry(onOpenDetails: ((slug: String) -> Unit)?, onOpenSettings: (() -> Unit)?) {
    entry<ScheduleNavKey> {
        ScheduleRoute(onOpenDetails = onOpenDetails, onOpenSettings = onOpenSettings)
    }
}
