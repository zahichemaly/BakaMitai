package com.zc.bakamitai.common.ui.mapper

import com.zc.bakamitai.common.ui.text.UiText
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format.Padding
import kotlinx.datetime.format.char
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

/** The spec's default 12-hour format, "8:00 AM" (context.md §4.5), until the Time format setting exists. */
private val TwelveHourFormat = LocalTime.Format {
    amPmHour(Padding.NONE)
    char(':')
    minute()
    char(' ')
    amPmMarker("AM", "PM")
}

/** A scheduled time, "6:30 PM". */
fun LocalTime.toTimeLabel(): UiText = UiText.StringValue(TwelveHourFormat.format(this))

/** A release instant as a local time in [timeZone], "9:33 AM". */
fun Instant.toTimeLabel(timeZone: TimeZone): UiText = toLocalDateTime(timeZone).time.toTimeLabel()
