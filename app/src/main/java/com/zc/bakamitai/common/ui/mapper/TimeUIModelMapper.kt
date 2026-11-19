package com.zc.bakamitai.common.ui.mapper

import com.zc.bakamitai.common.ui.text.UiText
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

/**
 * A scheduled time, to the minute. It's formatted when it's shown, on the user's clock: "6:30 PM" or "18:30" (the
 * *Time format* setting, through `UiText.TimeValue`).
 */
fun LocalTime.toTimeLabel(): UiText = UiText.TimeValue(LocalTime(hour, minute))

/** A release instant as a local time in [timeZone]: "9:33 AM" or "09:33". */
fun Instant.toTimeLabel(timeZone: TimeZone): UiText = toLocalDateTime(timeZone).time.toTimeLabel()
