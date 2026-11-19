package com.zc.bakamitai.common.ui.mapper

import com.zc.bakamitai.common.ui.resource.StringResource
import com.zc.bakamitai.common.ui.text.UiText
import kotlin.test.Test
import kotlin.test.assertEquals

class EpisodeLabelUIModelMapperTest {

    @Test
    fun `a number is an episode`() {
        assertEquals(UiText.StringResource(StringResource.episode_label_number, listOf("20")), "20".toEpisodeTitle())
        assertEquals(UiText.StringResource(StringResource.episode_label_number, listOf("07")), "07".toEpisodeTitle())
        assertEquals(
            UiText.StringResource(StringResource.episode_label_number, listOf("12.5")),
            "12.5".toEpisodeTitle(),
        )
    }

    @Test
    fun `a revision is the episode with its version`() {
        assertEquals(
            UiText.StringResource(StringResource.episode_label_revision, listOf("1216", "2")),
            "1216v2".toEpisodeTitle(),
        )
    }

    @Test
    fun `a movie in any case is Movie`() {
        assertEquals(UiText.StringResource(StringResource.episode_label_movie), "Movie".toEpisodeTitle())
        assertEquals(UiText.StringResource(StringResource.episode_label_movie), "movie".toEpisodeTitle())
    }

    @Test
    fun `a batch range uses an en dash`() {
        assertEquals(
            UiText.StringResource(StringResource.episode_label_batch, listOf("01–28")),
            "01-28".toEpisodeTitle(),
        )
        assertEquals(
            UiText.StringResource(StringResource.episode_label_batch, listOf("01–24")),
            "01 - 24".toEpisodeTitle(),
        )
    }

    @Test
    fun `anything else is shown as SubsPlease writes it`() {
        assertEquals(UiText.StringValue("Egghead SP11"), "Egghead SP11".toEpisodeTitle())
        assertEquals(
            UiText.StringValue("The Counterfeit Case of Ultra 30"),
            "The Counterfeit Case of Ultra 30".toEpisodeTitle(),
        )
    }
}
