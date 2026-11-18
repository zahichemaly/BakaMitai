package com.zc.bakamitai.feature.home.presentation.component.upcomingcard.mapper

import com.zc.bakamitai.common.domain.model.ShowTestData.show
import com.zc.bakamitai.common.ui.resource.StringResource
import com.zc.bakamitai.common.ui.text.UiText
import com.zc.bakamitai.feature.home.domain.model.UpcomingBroadcast
import com.zc.bakamitai.feature.home.presentation.component.upcomingcard.model.UpcomingCardUIModel
import com.zc.bakamitai.feature.home.presentation.model.EpisodeBadgeUIModel
import kotlinx.datetime.LocalTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNull

class UpcomingCardUIModelMapperTest {

    private val upcoming = UpcomingBroadcast(
        show = show("dandadan", "Dandadan", coverUrl = "https://subsplease.org/wp-content/uploads/cover.jpg"),
        time = LocalTime(18, 30),
        episodeNumber = 8,
    )

    @Test
    fun `maps a broadcast to a card`() {
        assertEquals(
            UpcomingCardUIModel(
                key = "dandadan@18:30",
                slug = "dandadan",
                title = UiText.StringValue("Dandadan"),
                coverUrl = "https://subsplease.org/wp-content/uploads/cover.jpg",
                timeLabel = UiText.StringValue("6:30 PM"),
                episodeBadge = EpisodeBadgeUIModel(
                    label = UiText.StringResource(StringResource.home_episode, listOf("08")),
                    contentDescription = UiText.StringResource(StringResource.cd_episode, listOf("08")),
                    isSpecial = false,
                ),
            ),
            upcoming.toUIModel(),
        )
    }

    @Test
    fun `the same show at two times has two keys`() {
        assertNotEquals(upcoming.toUIModel().key, upcoming.copy(time = LocalTime(19, 0)).toUIModel().key)
    }

    @Test
    fun `a null cover and an unknown episode pass through`() {
        val card = upcoming.copy(show = show("dandadan", "Dandadan", coverUrl = null), episodeNumber = null).toUIModel()

        assertNull(card.coverUrl)
        assertNull(card.episodeBadge)
    }
}
