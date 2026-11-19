package com.zc.bakamitai.feature.details.presentation.component.episoderow.mapper

import com.zc.bakamitai.common.ui.resource.StringResource
import com.zc.bakamitai.common.ui.text.UiText
import com.zc.bakamitai.feature.details.domain.DetailsTestData.episode
import com.zc.bakamitai.feature.details.domain.DetailsTestData.newYork
import kotlinx.datetime.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class EpisodeUIModelMapperTest {

    private fun title(label: String, isBatch: Boolean = false) =
        episode(label).toEpisodeUIState(isBatch = isBatch, timeZone = newYork, isExpanded = false).title

    @Test
    fun `the title is the shared episode title`() {
        assertEquals(UiText.StringResource(StringResource.episode_label_number, listOf("20")), title("20"))
        assertEquals(UiText.StringValue("Egghead SP11"), title("Egghead SP11"))
    }

    @Test
    fun `the release line is the date and the 12-hour time in the given zone`() {
        val released = episode("20", releasedAt = "2026-11-17T17:38:52Z")

        assertEquals(
            UiText.StringResource(
                StringResource.details_episode_released,
                listOf(UiText.StringValue("Nov 17, 2026"), UiText.StringValue("12:38 PM")),
            ),
            released.toEpisodeUIState(isBatch = false, timeZone = newYork, isExpanded = false).releasedLabel,
        )
        assertEquals(
            UiText.StringResource(
                StringResource.details_episode_released,
                listOf(UiText.StringValue("Nov 18, 2026"), UiText.StringValue("2:38 AM")),
            ),
            released.toEpisodeUIState(isBatch = false, timeZone = TimeZone.of("Asia/Tokyo"), isExpanded = false)
                .releasedLabel,
        )
    }

    @Test
    fun `keys are prefixed by list, and the expansion and three slots are set`() {
        val row = episode("01-28").toEpisodeUIState(isBatch = true, timeZone = newYork, isExpanded = true)

        assertEquals("batch|01-28", row.key)
        assertEquals("episode|28", episode("28").toEpisodeKey(isBatch = false))
        assertTrue(row.isExpanded)
        assertEquals(3, row.resolutions.size)
        assertFalse(episode("01").toEpisodeUIState(isBatch = false, timeZone = newYork, isExpanded = false).isExpanded)
    }
}
