package com.zc.bakamitai.feature.bookmarks.domain.model

import com.zc.bakamitai.feature.bookmarks.domain.BookmarksTestData.MONDAY_NOON
import com.zc.bakamitai.feature.bookmarks.domain.BookmarksTestData.mondayNoon
import com.zc.bakamitai.feature.bookmarks.domain.BookmarksTestData.slot
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

class AiringSlotTest {

    private val slot = mondayNoon()
    private val previousMonday = Instant.parse("2026-11-09T12:00:00Z")
    private val nextMonday = Instant.parse("2026-11-23T12:00:00Z")

    @Test
    fun `latest occurrence at, just before and just after the slot`() {
        assertEquals(MONDAY_NOON, slot.latestAtOrBefore(MONDAY_NOON))
        assertEquals(previousMonday, slot.latestAtOrBefore(MONDAY_NOON - 1.seconds))
        assertEquals(MONDAY_NOON, slot.latestAtOrBefore(MONDAY_NOON + 1.seconds))
        assertEquals(MONDAY_NOON, slot.latestAtOrBefore(Instant.parse("2026-11-22T23:59:00Z")))
    }

    @Test
    fun `first occurrence strictly after`() {
        assertEquals(nextMonday, slot.firstAfter(MONDAY_NOON))
        assertEquals(MONDAY_NOON, slot.firstAfter(MONDAY_NOON - 1.seconds))
        assertEquals(MONDAY_NOON, slot.firstAfter(Instant.parse("2026-11-10T00:00:00Z")))
    }

    @Test
    fun `nearest occurrence is unique within three and a half days`() {
        assertEquals(MONDAY_NOON, slot.nearestTo(MONDAY_NOON + 1.hours))
        assertEquals(MONDAY_NOON, slot.nearestTo(MONDAY_NOON - 25.hours))
        assertEquals(MONDAY_NOON, slot.nearestTo(MONDAY_NOON + 3.days))
        assertEquals(nextMonday, slot.nearestTo(MONDAY_NOON + 4.days))
        assertEquals(MONDAY_NOON, slot.nearestTo(MONDAY_NOON + 3.5.days))
    }

    @Test
    fun `a Tokyo slot is a Tokyo wall-clock time`() {
        val tokyo = slot(DayOfWeek.TUESDAY, LocalTime(0, 30), timeZone = TimeZone.of("Asia/Tokyo"))

        // Tuesday 00:30 in Tokyo is Monday 15:30 UTC.
        assertEquals(Instant.parse("2026-11-16T15:30:00Z"), tokyo.firstAfter(MONDAY_NOON))
        assertEquals(Instant.parse("2026-11-09T15:30:00Z"), tokyo.latestAtOrBefore(MONDAY_NOON))
    }

    @Test
    fun `a New York slot keeps its wall-clock time across the end of DST`() {
        val newYork = slot(DayOfWeek.MONDAY, LocalTime(9, 0), timeZone = TimeZone.of("America/New_York"))
        val beforeChange = Instant.parse("2026-10-26T13:00:00Z") // 09:00 EDT
        val afterChange = Instant.parse("2026-11-02T14:00:00Z") // 09:00 EST, after 1 Nov 2026

        assertEquals(afterChange, newYork.firstAfter(beforeChange))
        assertEquals(afterChange, newYork.latestAtOrBefore(Instant.parse("2026-11-03T00:00:00Z")))
        assertEquals(beforeChange, newYork.latestAtOrBefore(Instant.parse("2026-11-02T13:30:00Z")))
        assertEquals(afterChange, newYork.nearestTo(Instant.parse("2026-11-02T14:05:00Z")))
    }

    @Test
    fun `a slot inside a DST gap moves forward by the gap`() {
        // 14 Mar 2027, New York: 02:00 EST jumps to 03:00 EDT, so 02:30 doesn't exist and becomes 03:30 EDT.
        val inGap = slot(DayOfWeek.SUNDAY, LocalTime(2, 30), timeZone = TimeZone.of("America/New_York"))

        assertEquals(Instant.parse("2027-03-14T07:30:00Z"), inGap.firstAfter(Instant.parse("2027-03-13T00:00:00Z")))
        assertEquals(Instant.parse("2027-03-21T06:30:00Z"), inGap.firstAfter(Instant.parse("2027-03-14T07:30:00Z")))
        assertEquals(
            Instant.parse("2027-03-07T07:30:00Z"),
            inGap.latestAtOrBefore(Instant.parse("2027-03-14T07:00:00Z")),
        )
    }
}
