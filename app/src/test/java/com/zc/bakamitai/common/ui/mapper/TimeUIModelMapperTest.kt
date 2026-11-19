package com.zc.bakamitai.common.ui.mapper

import com.zc.bakamitai.common.ui.text.UiText
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant

class TimeUIModelMapperTest {

    @Test
    fun `a time is a TimeValue, formatted on the user's clock when shown`() {
        assertEquals(UiText.TimeValue(LocalTime(8, 5)), LocalTime(8, 5).toTimeLabel())
        assertEquals(UiText.TimeValue(LocalTime(23, 59)), LocalTime(23, 59).toTimeLabel())
    }

    @Test
    fun `a time is kept to the minute, as it's shown`() {
        assertEquals(UiText.TimeValue(LocalTime(9, 33)), LocalTime(9, 33, 47).toTimeLabel())
    }

    @Test
    fun `an instant is shown in the given zone`() {
        val releasedAt = Instant.parse("2026-11-18T14:33:47Z")

        assertEquals(UiText.TimeValue(LocalTime(9, 33)), releasedAt.toTimeLabel(TimeZone.of("America/New_York")))
        assertEquals(UiText.TimeValue(LocalTime(23, 33)), releasedAt.toTimeLabel(TimeZone.of("Asia/Tokyo")))
    }
}
