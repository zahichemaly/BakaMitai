package com.zc.bakamitai.common.domain.model

/**
 * The user's preferences (context.md §3.6), saved on the device. Every default is the spec's: Monday, 12-hour,
 * the system's theme, and new-episode alerts on.
 *
 * @property newEpisodeAlerts the in-app switch for new-episode alerts. Android may still block the app's
 * notifications; the Bookmarks feature combines both.
 */
data class AppSettings(
    val weekStart: WeekStart = WeekStart.MONDAY,
    val timeFormat: TimeFormat = TimeFormat.TWELVE_HOUR,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val newEpisodeAlerts: Boolean = true,
)
