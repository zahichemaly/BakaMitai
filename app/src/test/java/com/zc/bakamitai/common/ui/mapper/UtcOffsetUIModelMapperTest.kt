package com.zc.bakamitai.common.ui.mapper

import com.zc.bakamitai.common.ui.resource.StringResource
import com.zc.bakamitai.common.ui.text.UiText
import kotlinx.datetime.UtcOffset
import kotlin.test.Test
import kotlin.test.assertEquals

class UtcOffsetUIModelMapperTest {

    private fun label(suffix: String) = UiText.StringResource(StringResource.utc_offset, listOf(suffix))

    @Test
    fun `the offset label shows the sign, hours and any minutes`() {
        assertEquals(label("+9"), UtcOffset(9).toUtcOffsetLabel())
        assertEquals(label("-5"), UtcOffset(-5).toUtcOffsetLabel())
        assertEquals(label("+5:30"), UtcOffset(hours = 5, minutes = 30).toUtcOffsetLabel())
        assertEquals(label("-3:30"), UtcOffset(hours = -3, minutes = -30).toUtcOffsetLabel())
    }

    @Test
    fun `a zero offset has no suffix`() {
        assertEquals(label(""), UtcOffset.ZERO.toUtcOffsetLabel())
    }
}
