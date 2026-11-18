package com.zc.bakamitai.feature.schedule.presentation.component.daychip.mapper

import com.zc.bakamitai.common.ui.resource.StringResource
import com.zc.bakamitai.common.ui.text.UiText
import com.zc.bakamitai.feature.schedule.domain.model.ScheduleDay
import com.zc.bakamitai.feature.schedule.domain.model.WeeklySchedule
import com.zc.bakamitai.feature.schedule.presentation.component.daychip.model.DayChipUIModel
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.UtcOffset
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DayChipUIModelMapperTest {

    private val today = LocalDate(2026, 11, 19)

    @Test
    fun `a day maps to its short name, its unpadded date and a description`() {
        assertEquals(
            DayChipUIModel(
                dayOfWeek = DayOfWeek.MONDAY,
                weekdayLabel = UiText.StringResource(StringResource.day_short_monday),
                dateLabel = UiText.StringValue("16"),
                contentDescription = UiText.StringResource(
                    StringResource.cd_schedule_day,
                    listOf(UiText.StringResource(StringResource.day_monday), UiText.StringValue("16")),
                ),
                isToday = false,
            ),
            ScheduleDay(LocalDate(2026, 11, 16), emptyList()).toDayChipUIModel(today),
        )
    }

    @Test
    fun `the date isn't padded`() {
        assertEquals(
            UiText.StringValue("9"),
            ScheduleDay(LocalDate(2026, 11, 9), emptyList()).toDayChipUIModel(today).dateLabel,
        )
    }

    @Test
    fun `today is marked and described as today`() {
        val chip = ScheduleDay(today, emptyList()).toDayChipUIModel(today)

        assertTrue(chip.isToday)
        assertEquals(
            UiText.StringResource(
                StringResource.cd_schedule_day_today,
                listOf(UiText.StringResource(StringResource.day_thursday), UiText.StringValue("19")),
            ),
            chip.contentDescription,
        )
    }

    @Test
    fun `the week maps to 7 chips in its order`() {
        val first = LocalDate(2026, 11, 15)
        val week = WeeklySchedule(
            today = today,
            timeZone = TimeZone.UTC,
            utcOffset = UtcOffset.ZERO,
            days = List(7) { ScheduleDay(LocalDate.fromEpochDays(first.toEpochDays() + it), emptyList()) },
        )

        val chips = week.toDayChipUIModels()

        assertEquals(
            listOf(
                DayOfWeek.SUNDAY,
                DayOfWeek.MONDAY,
                DayOfWeek.TUESDAY,
                DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY,
                DayOfWeek.FRIDAY,
                DayOfWeek.SATURDAY,
            ),
            chips.map { it.dayOfWeek },
        )
        assertEquals(listOf(false, false, false, false, true, false, false), chips.map { it.isToday })
        assertFalse(chips.first().isToday)
    }
}
