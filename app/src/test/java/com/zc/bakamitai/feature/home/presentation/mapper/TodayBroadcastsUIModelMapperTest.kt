package com.zc.bakamitai.feature.home.presentation.mapper

import com.zc.bakamitai.common.ui.resource.PluralResource
import com.zc.bakamitai.common.ui.text.UiText
import com.zc.bakamitai.feature.home.domain.HomeTestData.show
import com.zc.bakamitai.feature.home.domain.model.TodayBroadcasts
import com.zc.bakamitai.feature.home.domain.model.UpcomingBroadcast
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.UtcOffset
import kotlin.test.Test
import kotlin.test.assertEquals

class TodayBroadcastsUIModelMapperTest {

    @Test
    fun `the upcoming count is a plural of the upcoming list size`() {
        val broadcasts = TodayBroadcasts(
            date = LocalDate(2026, 11, 18),
            timeZone = TimeZone.UTC,
            utcOffset = UtcOffset.ZERO,
            upcoming = listOf(
                UpcomingBroadcast(show("a"), LocalTime(18, 0), episodeNumber = 2),
                UpcomingBroadcast(show("b"), LocalTime(19, 0), episodeNumber = null),
            ),
            released = emptyList(),
        )

        assertEquals(
            UiText.StringPlural(PluralResource.home_section_upcoming_count, 2, listOf(2)),
            broadcasts.toUpcomingCountLabel(),
        )
    }
}
