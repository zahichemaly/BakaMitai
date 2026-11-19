package com.zc.bakamitai.feature.settings.presentation.navigation

import androidx.navigation3.runtime.NavKey
import com.zc.bakamitai.common.ui.navigation.FullScreenNavKey
import kotlinx.serialization.Serializable

/** Settings, pushed onto the current tab; it hides the bottom navigation bar. */
@Serializable
data object SettingsNavKey :
    NavKey,
    FullScreenNavKey
