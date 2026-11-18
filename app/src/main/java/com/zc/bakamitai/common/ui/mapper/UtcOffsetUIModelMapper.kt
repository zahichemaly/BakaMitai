package com.zc.bakamitai.common.ui.mapper

import com.zc.bakamitai.common.ui.resource.StringResource
import com.zc.bakamitai.common.ui.text.UiText
import kotlinx.datetime.UtcOffset
import kotlin.math.absoluteValue

private const val SECONDS_PER_MINUTE = 60
private const val MINUTES_PER_HOUR = 60
private const val MINUTE_DIGITS = 2

/** The device's offset as shown to the user: "UTC+9", "UTC-5", "UTC+5:30", or "UTC" at zero. */
fun UtcOffset.toUtcOffsetLabel(): UiText = UiText.StringResource(StringResource.utc_offset, listOf(toSuffix()))

/** `""` at zero, otherwise `+9`, `-5` or `+5:30`. */
private fun UtcOffset.toSuffix(): String {
    val totalMinutes = totalSeconds / SECONDS_PER_MINUTE
    val sign = if (totalMinutes < 0) "-" else "+"
    val hours = totalMinutes.absoluteValue / MINUTES_PER_HOUR
    val minutes = totalMinutes.absoluteValue % MINUTES_PER_HOUR
    return when {
        totalMinutes == 0 -> ""
        minutes == 0 -> "$sign$hours"
        else -> "$sign$hours:${minutes.toString().padStart(MINUTE_DIGITS, '0')}"
    }
}
