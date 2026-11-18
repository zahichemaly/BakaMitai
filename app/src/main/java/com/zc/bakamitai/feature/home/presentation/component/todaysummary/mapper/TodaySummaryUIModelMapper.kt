package com.zc.bakamitai.feature.home.presentation.component.todaysummary.mapper

import com.zc.bakamitai.common.ui.resource.PluralResource
import com.zc.bakamitai.common.ui.resource.StringResource
import com.zc.bakamitai.common.ui.text.UiText
import com.zc.bakamitai.feature.home.domain.model.TodayBroadcasts
import com.zc.bakamitai.feature.home.presentation.component.todaysummary.model.TodaySummaryUIModel
import kotlinx.datetime.UtcOffset
import kotlin.math.absoluteValue

private const val SECONDS_PER_MINUTE = 60
private const val MINUTES_PER_HOUR = 60
private const val MINUTE_DIGITS = 2

fun TodayBroadcasts.toTodaySummaryUIModel(): TodaySummaryUIModel = TodaySummaryUIModel(
    utcOffsetLabel = UiText.StringResource(StringResource.home_utc_offset, listOf(utcOffset.toSuffix())),
    upcomingCount = UiText.StringPlural(PluralResource.home_summary_airing, upcoming.size, listOf(upcoming.size)),
    releasedCount = UiText.StringPlural(PluralResource.home_summary_available, released.size, listOf(released.size)),
)

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
