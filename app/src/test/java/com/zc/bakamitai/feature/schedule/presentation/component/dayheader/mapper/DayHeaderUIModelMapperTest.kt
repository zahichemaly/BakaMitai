package com.zc.bakamitai.feature.schedule.presentation.component.dayheader.mapper

import com.zc.bakamitai.common.domain.model.ShowTestData.scheduled
import com.zc.bakamitai.common.ui.resource.PluralResource
import com.zc.bakamitai.common.ui.resource.StringResource
import com.zc.bakamitai.common.ui.text.UiText
import com.zc.bakamitai.feature.schedule.domain.model.ScheduleDay
import com.zc.bakamitai.feature.schedule.presentation.component.dayheader.model.StatusFilterChipUIModel
import com.zc.bakamitai.feature.schedule.presentation.model.StatusFilter
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.UtcOffset
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DayHeaderUIModelMapperTest {

    private val thursday = LocalDate(2026, 11, 19)

    private fun today(vararg aired: Boolean?) = ScheduleDay(
        date = thursday,
        shows = aired.mapIndexed { index, value -> scheduled("s$index", LocalTime(8, index), aired = value) },
    )

    @Test
    fun `the title names the day and the subtitle wraps the offset label`() {
        val header = today(null).toDayHeaderUIModel(UtcOffset(2))

        assertEquals(
            UiText.StringResource(StringResource.day_thursday),
            header.title,
        )
        assertEquals(
            UiText.StringResource(
                StringResource.schedule_time_zone,
                listOf(UiText.StringResource(StringResource.utc_offset, listOf("+2"))),
            ),
            header.subtitle,
        )
    }

    @Test
    fun `the count is a plural of the day's shows`() {
        assertEquals(
            UiText.StringPlural(PluralResource.schedule_show_count, 6, listOf(6)),
            today(true, true, true, true, true, false).toDayHeaderUIModel(UtcOffset.ZERO).countLabel,
        )
        assertEquals(
            UiText.StringPlural(PluralResource.schedule_show_count, 1, listOf(1)),
            today(null).toDayHeaderUIModel(UtcOffset.ZERO).countLabel,
        )
    }

    @Test
    fun `a day with statuses has the three filters with their counts`() {
        assertEquals(
            listOf(
                StatusFilterChipUIModel(
                    StatusFilter.ALL,
                    UiText.StringResource(StringResource.schedule_filter_all, listOf(6)),
                ),
                StatusFilterChipUIModel(
                    StatusFilter.AIRED,
                    UiText.StringResource(StringResource.schedule_filter_aired, listOf(5)),
                ),
                StatusFilterChipUIModel(
                    StatusFilter.UPCOMING,
                    UiText.StringResource(StringResource.schedule_filter_upcoming, listOf(1)),
                ),
            ),
            today(true, true, true, true, true, false).toDayHeaderUIModel(UtcOffset.ZERO).filters,
        )
    }

    @Test
    fun `a show without a status counts in All only`() {
        val filters = today(true, false, null).toDayHeaderUIModel(UtcOffset.ZERO).filters

        assertEquals(
            listOf(listOf(3), listOf(1), listOf(1)),
            filters.map { (it.label as UiText.StringResource).args },
        )
    }

    @Test
    fun `a day without statuses has no filters`() {
        assertTrue(today(null, null).toDayHeaderUIModel(UtcOffset.ZERO).filters.isEmpty())
        assertTrue(ScheduleDay(thursday, emptyList()).toDayHeaderUIModel(UtcOffset.ZERO).filters.isEmpty())
    }
}
