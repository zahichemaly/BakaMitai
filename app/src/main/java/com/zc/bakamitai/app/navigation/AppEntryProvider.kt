package com.zc.bakamitai.app.navigation

import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import com.zc.bakamitai.feature.home.presentation.navigation.homeEntry

/**
 * Every feature's entries, and the only place that wires features together. Home's Details and Settings
 * callbacks are `null` until those features exist.
 */
fun appEntryProvider(): (NavKey) -> NavEntry<NavKey> = entryProvider {
    homeEntry(onOpenDetails = null, onOpenSettings = null)
}
