package com.zc.bakamitai.common.ui.text

import kotlinx.datetime.LocalTime
import kotlin.test.Test
import kotlin.test.assertEquals

class TimeTextTest {

    @Test
    fun `12-hour clock`() {
        assertEquals("12:00 AM", LocalTime(0, 0).toClockText(use24HourClock = false))
        assertEquals("8:05 AM", LocalTime(8, 5).toClockText(use24HourClock = false))
        assertEquals("12:15 PM", LocalTime(12, 15).toClockText(use24HourClock = false))
        assertEquals("11:59 PM", LocalTime(23, 59).toClockText(use24HourClock = false))
    }

    @Test
    fun `24-hour clock`() {
        assertEquals("00:00", LocalTime(0, 0).toClockText(use24HourClock = true))
        assertEquals("08:05", LocalTime(8, 5).toClockText(use24HourClock = true))
        assertEquals("12:15", LocalTime(12, 15).toClockText(use24HourClock = true))
        assertEquals("23:59", LocalTime(23, 59).toClockText(use24HourClock = true))
    }
}
