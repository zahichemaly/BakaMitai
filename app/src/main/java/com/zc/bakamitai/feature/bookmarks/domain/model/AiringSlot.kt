package com.zc.bakamitai.feature.bookmarks.domain.model

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

private const val DAYS_PER_WEEK = 7

/**
 * A show's weekly slot on SubsPlease's weekly schedule, as fetched: [dayOfWeek] at [time] in [timeZone]. Its
 * occurrences are absolute instants, computed day by day in [timeZone], so they stay right across DST changes.
 *
 * @property releaseTitle the weekly schedule's title for the show, the one the release feeds use (it can differ
 * from the show page's title).
 */
data class AiringSlot(
    val dayOfWeek: DayOfWeek,
    val time: LocalTime,
    val timeZone: TimeZone,
    val releaseTitle: String,
) {

    /** The latest occurrence at or before [instant]. */
    fun latestAtOrBefore(instant: Instant): Instant {
        val today = instant.toLocalDateTime(timeZone).date
        val daysBack = (today.dayOfWeek.isoDayNumber - dayOfWeek.isoDayNumber).mod(DAYS_PER_WEEK)
        val date = today.minus(daysBack, DateTimeUnit.DAY)
        val occurrence = occurrenceOn(date)
        return if (occurrence <= instant) occurrence else occurrenceOn(date.minus(1, DateTimeUnit.WEEK))
    }

    /** The first occurrence strictly after [instant]. */
    fun firstAfter(instant: Instant): Instant {
        val today = instant.toLocalDateTime(timeZone).date
        val daysAhead = (dayOfWeek.isoDayNumber - today.dayOfWeek.isoDayNumber).mod(DAYS_PER_WEEK)
        val date = today.plus(daysAhead, DateTimeUnit.DAY)
        val occurrence = occurrenceOn(date)
        return if (occurrence > instant) occurrence else occurrenceOn(date.plus(1, DateTimeUnit.WEEK))
    }

    /** The occurrence nearest to [instant] (unique within ±3.5 days); the earlier one on a tie. */
    fun nearestTo(instant: Instant): Instant {
        val before = latestAtOrBefore(instant)
        val after = firstAfter(instant)
        return if (instant - before <= after - instant) before else after
    }

    /** The occurrence on [date]; a time inside a DST gap moves forward by the gap, like the clocks do. */
    private fun occurrenceOn(date: LocalDate): Instant = LocalDateTime(date, time).toInstant(timeZone)
}
