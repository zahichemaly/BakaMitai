package com.zc.bakamitai.common.ui.text

import androidx.compose.runtime.compositionLocalOf
import kotlinx.datetime.LocalTime
import kotlinx.datetime.format.Padding
import kotlinx.datetime.format.char

/**
 * Whether times are shown on a 24-hour clock (the *Time format* setting). `AppRoute` provides it around the whole
 * app; previews and tests get the default 12-hour clock. Only `UiText.TimeValue` reads it, so a change recomposes
 * just the texts that show a time.
 */
@Suppress("CompositionLocalAllowlist") // The clock format, read only where UiText resolves (6-settings.md §5.1).
val LocalUse24HourClock = compositionLocalOf { false }

/** "8:05 PM" (context.md §4.5). */
private val TwelveHourFormat = LocalTime.Format {
    amPmHour(Padding.NONE)
    char(':')
    minute()
    char(' ')
    amPmMarker("AM", "PM")
}

/** "20:05". */
private val TwentyFourHourFormat = LocalTime.Format {
    hour()
    char(':')
    minute()
}

/** This time on a 12-hour ("8:05 PM") or a 24-hour ("20:05") clock. */
fun LocalTime.toClockText(use24HourClock: Boolean): String =
    if (use24HourClock) TwentyFourHourFormat.format(this) else TwelveHourFormat.format(this)
