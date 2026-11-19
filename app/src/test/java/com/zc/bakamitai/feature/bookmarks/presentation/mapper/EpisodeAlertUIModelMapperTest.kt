package com.zc.bakamitai.feature.bookmarks.presentation.mapper

import com.zc.bakamitai.common.domain.model.ShowTestData.show
import com.zc.bakamitai.common.ui.resource.PluralResource
import com.zc.bakamitai.common.ui.resource.StringResource
import com.zc.bakamitai.common.ui.text.UiText
import com.zc.bakamitai.feature.bookmarks.domain.model.EpisodeAlert
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.time.Instant

class EpisodeAlertUIModelMapperTest {

    private val releasedAt = Instant.parse("2026-11-16T11:00:00Z")

    private fun alert(episode: String, count: Int = 1, slug: String = "frieren") =
        EpisodeAlert(show(slug, "Sousou no Frieren"), episode, releasedAt, count)

    @Test
    fun `the text names the episode with the shared episode title`() {
        assertEquals(
            UiText.StringResource(
                StringResource.notification_episode_out,
                listOf(UiText.StringResource(StringResource.episode_label_number, listOf("8"))),
            ),
            alert("8").toEpisodeAlertUIModel().text,
        )
        assertEquals(
            UiText.StringResource(
                StringResource.notification_episode_out,
                listOf(UiText.StringResource(StringResource.episode_label_movie)),
            ),
            alert("Movie").toEpisodeAlertUIModel().text,
        )
        assertEquals(
            UiText.StringResource(StringResource.notification_episode_out, listOf(UiText.StringValue("Egghead SP11"))),
            alert("Egghead SP11").toEpisodeAlertUIModel().text,
        )
    }

    @Test
    fun `title, slug and summary line come from the show`() {
        val model = alert("08").toEpisodeAlertUIModel()

        assertEquals(UiText.StringValue("Sousou no Frieren"), model.title)
        assertEquals("frieren", model.slug)
        assertEquals(
            UiText.StringResource(
                StringResource.notification_summary_line,
                listOf(
                    UiText.StringValue("Sousou no Frieren"),
                    UiText.StringResource(StringResource.episode_label_number, listOf("08")),
                ),
            ),
            model.summaryLine,
        )
    }

    @Test
    fun `the sub-text counts a backlog only`() {
        assertNull(alert("08").toEpisodeAlertUIModel().subText)
        assertEquals(
            UiText.StringPlural(PluralResource.notification_more_new_episodes, 2, listOf(2)),
            alert("08", count = 2).toEpisodeAlertUIModel().subText,
        )
    }

    @Test
    fun `the notification id is stable per show`() {
        assertEquals(
            alert("08").toEpisodeAlertUIModel().notificationId,
            alert("09").toEpisodeAlertUIModel().notificationId,
        )
        assertNotEquals(
            alert("08").toEpisodeAlertUIModel().notificationId,
            alert("08", slug = "dandadan").toEpisodeAlertUIModel().notificationId,
        )
    }
}
