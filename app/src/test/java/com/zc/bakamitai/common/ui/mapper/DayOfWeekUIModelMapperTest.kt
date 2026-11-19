package com.zc.bakamitai.common.ui.mapper

import com.zc.bakamitai.common.ui.resource.StringResource
import com.zc.bakamitai.common.ui.text.UiText
import kotlinx.datetime.DayOfWeek
import kotlin.test.Test
import kotlin.test.assertEquals

class DayOfWeekUIModelMapperTest {

    @Test
    fun `every day has its full name`() {
        assertEquals(
            listOf(
                StringResource.day_monday,
                StringResource.day_tuesday,
                StringResource.day_wednesday,
                StringResource.day_thursday,
                StringResource.day_friday,
                StringResource.day_saturday,
                StringResource.day_sunday,
            ).map { UiText.StringResource(it) },
            DayOfWeek.entries.map { it.toDayName() },
        )
    }

    @Test
    fun `every day has its short name`() {
        assertEquals(
            listOf(
                StringResource.day_short_monday,
                StringResource.day_short_tuesday,
                StringResource.day_short_wednesday,
                StringResource.day_short_thursday,
                StringResource.day_short_friday,
                StringResource.day_short_saturday,
                StringResource.day_short_sunday,
            ).map { UiText.StringResource(it) },
            DayOfWeek.entries.map { it.toShortDayName() },
        )
    }
}
