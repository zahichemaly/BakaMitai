package com.zc.bakamitai.common.ui.mapper

import com.zc.bakamitai.common.ui.text.UiText
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

class DateUIModelMapperTest {

    @Test
    fun `a date is the abbreviated English month, the unpadded day and the year`() {
        assertEquals(UiText.StringValue("Nov 17, 2026"), LocalDate(2026, 11, 17).toDateLabel())
        assertEquals(UiText.StringValue("Jan 5, 2027"), LocalDate(2027, 1, 5).toDateLabel())
        assertEquals(UiText.StringValue("Dec 31, 2026"), LocalDate(2026, 12, 31).toDateLabel())
    }
}
