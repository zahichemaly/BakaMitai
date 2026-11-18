package com.zc.bakamitai.feature.home.presentation.mapper

import com.zc.bakamitai.common.ui.resource.StringResource
import com.zc.bakamitai.common.ui.text.UiText
import com.zc.bakamitai.feature.home.domain.HomeTestData.release
import com.zc.bakamitai.feature.home.domain.HomeTestData.show
import com.zc.bakamitai.feature.home.domain.model.UpcomingBroadcast
import com.zc.bakamitai.feature.home.presentation.model.EpisodeBadgeUIModel
import kotlinx.datetime.LocalTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class EpisodeBadgeUIModelMapperTest {

    private fun badgeOf(episode: String) = release("show", episode, "2026-11-18T14:33:47Z").toEpisodeBadgeUIModel()

    private fun upcoming(episodeNumber: Int?) = UpcomingBroadcast(show("show"), LocalTime(18, 0), episodeNumber)

    @Test
    fun `a numeric episode is a regular badge`() {
        assertEquals(
            EpisodeBadgeUIModel(
                label = UiText.StringResource(StringResource.home_episode, listOf("07")),
                contentDescription = UiText.StringResource(StringResource.cd_episode, listOf("07")),
                isSpecial = false,
            ),
            badgeOf("07"),
        )
    }

    @Test
    fun `a batch range is a special batch badge`() {
        assertEquals(
            EpisodeBadgeUIModel(
                label = UiText.StringResource(StringResource.home_episode_batch, listOf("01-24")),
                contentDescription = UiText.StringResource(StringResource.cd_episode_batch, listOf("01-24")),
                isSpecial = true,
            ),
            badgeOf("01-24"),
        )
    }

    @Test
    fun `a movie, in any case, is a special movie badge`() {
        val expected = EpisodeBadgeUIModel(
            label = UiText.StringResource(StringResource.home_episode_movie),
            contentDescription = UiText.StringResource(StringResource.cd_episode_movie),
            isSpecial = true,
        )

        assertEquals(expected, badgeOf("movie"))
        assertEquals(expected, badgeOf("Movie"))
    }

    @Test
    fun `any other episode is shown as-is, uppercase, as a special badge`() {
        assertEquals(
            EpisodeBadgeUIModel(
                label = UiText.StringValue("OVA"),
                contentDescription = UiText.StringResource(StringResource.cd_episode, listOf("Ova")),
                isSpecial = true,
            ),
            badgeOf("Ova"),
        )
    }

    @Test
    fun `an upcoming episode number is padded to two digits`() {
        assertEquals(
            EpisodeBadgeUIModel(
                label = UiText.StringResource(StringResource.home_episode, listOf("08")),
                contentDescription = UiText.StringResource(StringResource.cd_episode, listOf("08")),
                isSpecial = false,
            ),
            upcoming(8).toEpisodeBadgeUIModel(),
        )
        assertEquals(
            UiText.StringResource(StringResource.home_episode, listOf("1221")),
            upcoming(1221).toEpisodeBadgeUIModel()?.label,
        )
    }

    @Test
    fun `an unknown upcoming episode has no badge`() {
        assertNull(upcoming(null).toEpisodeBadgeUIModel())
    }
}
