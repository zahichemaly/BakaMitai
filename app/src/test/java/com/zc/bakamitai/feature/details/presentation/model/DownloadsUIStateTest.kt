package com.zc.bakamitai.feature.details.presentation.model

import com.zc.bakamitai.common.ui.component.error.model.ErrorUIModel
import com.zc.bakamitai.common.ui.resource.StringResource
import com.zc.bakamitai.common.ui.text.UiText
import com.zc.bakamitai.feature.details.presentation.component.episoderow.model.EpisodeUIState
import kotlinx.collections.immutable.persistentListOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertSame

class DownloadsUIStateTest {

    private fun row(key: String, isExpanded: Boolean = false) = EpisodeUIState(
        key = key,
        title = UiText.StringValue(key),
        releasedLabel = UiText.StringValue(""),
        resolutions = persistentListOf(),
        isExpanded = isExpanded,
    )

    private val content = DownloadsUIState.Content(
        countLabel = null,
        batches = persistentListOf(row("batch|01-28")),
        episodes = persistentListOf(row("episode|28", isExpanded = true), row("episode|27")),
        limitNote = null,
    )

    private fun DownloadsUIState.expanded(): List<String> {
        val content = assertIs<DownloadsUIState.Content>(this)
        return (content.batches + content.episodes).filter { it.isExpanded }.map { it.key }
    }

    @Test
    fun `toggling flips one episode row`() {
        assertEquals(listOf("episode|28", "episode|27"), content.withEpisodeToggled("episode|27").expanded())
        assertEquals(emptyList(), content.withEpisodeToggled("episode|28").expanded())
    }

    @Test
    fun `toggling flips a batch row`() {
        assertEquals(listOf("batch|01-28", "episode|28"), content.withEpisodeToggled("batch|01-28").expanded())
    }

    @Test
    fun `an unknown key changes nothing`() {
        assertEquals(content, content.withEpisodeToggled("episode|99"))
    }

    @Test
    fun `other states are returned unchanged`() {
        val error = DownloadsUIState.Error(ErrorUIModel(UiText.StringResource(StringResource.error_network), true))

        assertSame(DownloadsUIState.Loading, DownloadsUIState.Loading.withEpisodeToggled("episode|28"))
        assertSame(DownloadsUIState.Empty, DownloadsUIState.Empty.withEpisodeToggled("episode|28"))
        assertSame(error, error.withEpisodeToggled("episode|28"))
    }

    @Test
    fun `expandedKeys lists the expanded rows`() {
        assertEquals(setOf("episode|28"), content.expandedKeys)
        val toggled = assertIs<DownloadsUIState.Content>(content.withEpisodeToggled("batch|01-28"))

        assertEquals(setOf("batch|01-28", "episode|28"), toggled.expandedKeys)
    }
}
