package com.zc.bakamitai.feature.details.presentation.mapper

import com.zc.bakamitai.common.ui.resource.PluralResource
import com.zc.bakamitai.common.ui.resource.StringResource
import com.zc.bakamitai.common.ui.text.UiText
import com.zc.bakamitai.feature.details.domain.DetailsTestData.episode
import com.zc.bakamitai.feature.details.domain.DetailsTestData.episodes
import com.zc.bakamitai.feature.details.presentation.model.DownloadsUIState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

class ShowEpisodesUIModelMapperTest {

    private val loaded = episodes(
        episodes = listOf(episode("28"), episode("27"), episode("01")),
        batches = listOf(episode("01-28")),
    )

    private fun DownloadsUIState.content() = assertIs<DownloadsUIState.Content>(this)

    private fun DownloadsUIState.Content.expanded() = (batches + episodes).filter { it.isExpanded }.map { it.key }

    @Test
    fun `no episodes and no batches is Empty`() {
        assertEquals(DownloadsUIState.Empty, episodes().toDownloadsUIState())
    }

    @Test
    fun `rows keep their order, with the episode count`() {
        val content = loaded.toDownloadsUIState().content()

        assertEquals(listOf("batch|01-28"), content.batches.map { it.key })
        assertEquals(listOf("episode|28", "episode|27", "episode|01"), content.episodes.map { it.key })
        assertEquals(UiText.StringPlural(PluralResource.details_episode_count, 3, listOf(3)), content.countLabel)
        assertNull(content.limitNote)
    }

    @Test
    fun `the first load expands nothing, not even the newest episode`() {
        assertEquals(emptyList(), loaded.toDownloadsUIState().content().expanded())
    }

    @Test
    fun `with batches only, the first load expands nothing and has no count`() {
        val content = episodes(batches = listOf(episode("13-24"), episode("01-12")))
            .toDownloadsUIState()
            .content()

        assertEquals(emptyList(), content.expanded())
        assertNull(content.countLabel)
        assertEquals(emptyList(), content.episodes)
    }

    @Test
    fun `given keys are expanded instead, and unknown ones ignored`() {
        val content = loaded.toDownloadsUIState(expandedKeys = setOf("episode|27", "batch|01-28", "episode|99"))
            .content()

        assertEquals(listOf("batch|01-28", "episode|27"), content.expanded())
        assertEquals(emptyList(), loaded.toDownloadsUIState(expandedKeys = emptySet()).content().expanded())
    }

    @Test
    fun `the limit note shows only when SubsPlease's cap was hit`() {
        val content = loaded.copy(isTruncated = true).toDownloadsUIState().content()

        assertEquals(
            UiText.StringResource(StringResource.details_episode_limit_note, listOf(52)),
            content.limitNote,
        )
    }
}
