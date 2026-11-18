package com.zc.bakamitai.feature.schedule.presentation.component.schedulecard.component.statusbadge.mapper

import com.zc.bakamitai.common.domain.model.ShowTestData.scheduled
import com.zc.bakamitai.common.ui.resource.StringResource
import com.zc.bakamitai.common.ui.text.UiText
import com.zc.bakamitai.feature.schedule.presentation.component.schedulecard.component.statusbadge.model.StatusBadgeUIModel
import kotlinx.datetime.LocalTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class StatusBadgeUIModelMapperTest {

    @Test
    fun `aired is the Aired badge`() {
        assertEquals(
            StatusBadgeUIModel(UiText.StringResource(StringResource.schedule_status_aired), isUpcoming = false),
            scheduled("a", LocalTime(8, 0), aired = true).toStatusBadgeUIModel(),
        )
    }

    @Test
    fun `not aired is the Upcoming badge`() {
        assertEquals(
            StatusBadgeUIModel(UiText.StringResource(StringResource.schedule_status_upcoming), isUpcoming = true),
            scheduled("a", LocalTime(8, 0), aired = false).toStatusBadgeUIModel(),
        )
    }

    @Test
    fun `no status is no badge`() {
        assertNull(scheduled("a", LocalTime(8, 0), aired = null).toStatusBadgeUIModel())
    }
}
