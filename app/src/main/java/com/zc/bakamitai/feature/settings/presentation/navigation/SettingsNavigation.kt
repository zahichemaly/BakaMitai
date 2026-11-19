package com.zc.bakamitai.feature.settings.presentation.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.zc.bakamitai.common.ui.navigation.Navigator
import com.zc.bakamitai.feature.settings.presentation.SettingsRoute

/** Opens Settings on the current tab (once, however fast the taps). */
fun Navigator.navigateToSettings() = navigate(SettingsNavKey)

/**
 * Registers Settings.
 *
 * @param onNavigateBack leaves Settings: the back arrow.
 */
fun EntryProviderScope<NavKey>.settingsEntry(onNavigateBack: () -> Unit) {
    entry<SettingsNavKey> {
        SettingsRoute(onNavigateBack = onNavigateBack)
    }
}
