package com.zc.bakamitai.common.ui.mapper

import com.zc.bakamitai.common.ui.text.UiText
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant

class TimeUIModelMapperTest {

    @Test
    fun `times use the 12-hour format`() {
        assertEquals(UiText.StringValue("12:00 AM"), LocalTime(0, 0).toTimeLabel())
        assertEquals(UiText.StringValue("8:05 AM"), LocalTime(8, 5).toTimeLabel())
        assertEquals(UiText.StringValue("12:15 PM"), LocalTime(12, 15).toTimeLabel())
        assertEquals(UiText.StringValue("11:59 PM"), LocalTime(23, 59).toTimeLabel())
    }

    @Test
    fun `an instant is shown in the given zone`() {
        val releasedAt = Instant.parse("2026-11-18T14:33:47Z")

        assertEquals(UiText.StringValue("9:33 AM"), releasedAt.toTimeLabel(TimeZone.of("America/New_York")))
        assertEquals(UiText.StringValue("11:33 PM"), releasedAt.toTimeLabel(TimeZone.of("Asia/Tokyo")))
    }
}
