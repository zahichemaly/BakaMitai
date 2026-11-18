package com.zc.bakamitai.feature.schedule.presentation.mapper

import com.zc.bakamitai.common.domain.model.ShowTestData.scheduled
import com.zc.bakamitai.common.ui.resource.StringResource
import com.zc.bakamitai.common.ui.text.UiText
import com.zc.bakamitai.feature.schedule.domain.model.ScheduleDay
import com.zc.bakamitai.feature.schedule.presentation.component.dayheader.mapper.toDayHeaderUIModel
import com.zc.bakamitai.feature.schedule.presentation.model.ScheduleEmptyUIModel
import com.zc.bakamitai.feature.schedule.presentation.model.StatusFilter
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.UtcOffset
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ScheduleDayUIModelMapperTest {

    private val offset = UtcOffset(-5)

    /** Thursday, with statuses. */
    private val today = ScheduleDay(
        date = LocalDate(2026, 11, 19),
        shows = listOf(
            scheduled("a", LocalTime(8, 0), aired = true),
            scheduled("b", LocalTime(10, 0), aired = false),
            scheduled("c", LocalTime(12, 0), aired = null),
        ),
    )

    /** Friday, without statuses. */
    private val friday = ScheduleDay(
        date = LocalDate(2026, 11, 20),
        shows = listOf(scheduled("d", LocalTime(9, 0), aired = null), scheduled("e", LocalTime(11, 0), aired = null)),
    )

    private fun ScheduleDay.slugs(filter: StatusFilter) = toUIModel(offset, filter).cards.map { it.slug }

    @Test
    fun `filters narrow a day with statuses`() {
        assertEquals(listOf("a", "b", "c"), today.slugs(StatusFilter.ALL))
        assertEquals(listOf("a"), today.slugs(StatusFilter.AIRED))
        assertEquals(listOf("b"), today.slugs(StatusFilter.UPCOMING))
    }

    @Test
    fun `any filter shows every card on a day without statuses`() {
        StatusFilter.entries.forEach { filter -> assertEquals(listOf("d", "e"), friday.slugs(filter)) }
    }

    @Test
    fun `the header is the day's header and there is no empty message when cards show`() {
        val model = today.toUIModel(offset, StatusFilter.ALL)

        assertEquals(today.toDayHeaderUIModel(offset), model.header)
        assertNull(model.empty)
    }

    @Test
    fun `a day without shows has the day-empty message without reset`() {
        val model = ScheduleDay(LocalDate(2026, 11, 21), shows = emptyList()).toUIModel(offset, StatusFilter.UPCOMING)

        assertTrue(model.cards.isEmpty())
        assertEquals(
            ScheduleEmptyUIModel(
                title = UiText.StringResource(StringResource.schedule_day_empty_title),
                message = UiText.StringResource(
                    StringResource.schedule_day_empty_message,
                    listOf(UiText.StringResource(StringResource.day_saturday)),
                ),
                showResetAction = false,
            ),
            model.empty,
        )
    }

    @Test
    fun `a filter that matches nothing has the filter-empty message with reset`() {
        val allAired = today.copy(shows = listOf(scheduled("a", LocalTime(8, 0), aired = true)))

        val model = allAired.toUIModel(offset, StatusFilter.UPCOMING)

        assertTrue(model.cards.isEmpty())
        assertEquals(
            ScheduleEmptyUIModel(
                title = UiText.StringResource(StringResource.schedule_filter_empty_title),
                message = UiText.StringResource(StringResource.schedule_filter_empty_message),
                showResetAction = true,
            ),
            model.empty,
        )
    }
}
