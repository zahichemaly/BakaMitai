package com.zc.bakamitai.app

import androidx.compose.runtime.Immutable

/**
 * The app-wide settings the root applies: the theme and the clock format.
 *
 * @property isLoaded the settings have been read once; until then the root draws nothing (the first frame is held).
 * @property darkThemeOverride `true` / `false` for a forced dark / light theme, `null` to follow the system.
 * @property use24HourClock times are shown on a 24-hour clock.
 */
@Immutable
data class AppState(
    val isLoaded: Boolean = false,
    val darkThemeOverride: Boolean? = null,
    val use24HourClock: Boolean = false,
)
