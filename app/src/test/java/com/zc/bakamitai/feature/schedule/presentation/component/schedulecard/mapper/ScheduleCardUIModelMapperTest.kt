package com.zc.bakamitai.feature.schedule.presentation.component.schedulecard.mapper

import com.zc.bakamitai.common.domain.model.ShowTestData.scheduled
import com.zc.bakamitai.common.domain.model.ShowTestData.show
import com.zc.bakamitai.common.ui.component.timechip.model.TimeChipTone
import com.zc.bakamitai.common.ui.component.timechip.model.TimeChipUIModel
import com.zc.bakamitai.common.ui.resource.StringResource
import com.zc.bakamitai.common.ui.text.UiText
import com.zc.bakamitai.feature.schedule.presentation.component.schedulecard.component.statusbadge.model.StatusBadgeUIModel
import com.zc.bakamitai.feature.schedule.presentation.component.schedulecard.model.ScheduleCardUIModel
import kotlinx.datetime.LocalTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ScheduleCardUIModelMapperTest {

    @Test
    fun `a show maps to its card with a 12-hour time`() {
        val scheduled = scheduled("bleach", LocalTime(17, 0), aired = true, title = "Bleach")
            .let { it.copy(show = show("bleach", "Bleach", coverUrl = "https://subsplease.org/cover.jpg")) }

        assertEquals(
            ScheduleCardUIModel(
                key = "bleach@17:00",
                slug = "bleach",
                title = UiText.StringValue("Bleach"),
                coverUrl = "https://subsplease.org/cover.jpg",
                time = TimeChipUIModel(UiText.StringValue("5:00 PM"), TimeChipTone.DEFAULT),
                status = StatusBadgeUIModel(
                    UiText.StringResource(StringResource.schedule_status_aired),
                    isUpcoming = false,
                ),
            ),
            scheduled.toScheduleCardUIModel(),
        )
    }

    @Test
    fun `a missing cover and status pass through as null`() {
        val card = scheduled("x", LocalTime(8, 5), aired = null).toScheduleCardUIModel()

        assertNull(card.coverUrl)
        assertNull(card.status)
        assertEquals("x@08:05", card.key)
        assertEquals(TimeChipUIModel(UiText.StringValue("8:05 AM"), TimeChipTone.DEFAULT), card.time)
    }

    @Test
    fun `an upcoming show has the upcoming badge and an upcoming time chip`() {
        val card = scheduled("x", LocalTime(8, 0), aired = false).toScheduleCardUIModel()

        assertEquals(true, card.status?.isUpcoming)
        assertEquals(TimeChipTone.UPCOMING, card.time.tone)
    }
}
