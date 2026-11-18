package com.zc.bakamitai.feature.home.presentation.component.todaysummary.mapper

import com.zc.bakamitai.common.domain.model.ShowTestData.show
import com.zc.bakamitai.common.ui.resource.PluralResource
import com.zc.bakamitai.common.ui.resource.StringResource
import com.zc.bakamitai.common.ui.text.UiText
import com.zc.bakamitai.feature.home.domain.HomeTestData.release
import com.zc.bakamitai.feature.home.domain.model.TodayBroadcasts
import com.zc.bakamitai.feature.home.domain.model.UpcomingBroadcast
import com.zc.bakamitai.feature.home.presentation.component.todaysummary.model.TodaySummaryUIModel
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.UtcOffset
import kotlin.test.Test
import kotlin.test.assertEquals

class TodaySummaryUIModelMapperTest {

    private fun broadcasts(offset: UtcOffset, upcoming: Int = 0, released: Int = 0) = TodayBroadcasts(
        date = LocalDate(2026, 11, 18),
        timeZone = TimeZone.UTC,
        utcOffset = offset,
        upcoming = List(upcoming) { UpcomingBroadcast(show("u$it"), LocalTime(18, 0), episodeNumber = null) },
        released = List(released) { release("r$it", "01", "2026-11-18T14:33:47Z") },
    )

    @Test
    fun `the counts are plurals of the section sizes and the offset is the shared label`() {
        assertEquals(
            TodaySummaryUIModel(
                utcOffsetLabel = UiText.StringResource(StringResource.utc_offset, listOf("-5")),
                upcomingCount = UiText.StringPlural(PluralResource.home_summary_airing, 4, listOf(4)),
                releasedCount = UiText.StringPlural(PluralResource.home_summary_available, 5, listOf(5)),
            ),
            broadcasts(UtcOffset(-5), upcoming = 4, released = 5).toTodaySummaryUIModel(),
        )
    }
}
