package com.zc.bakamitai.feature.details.data.mapper

import com.zc.bakamitai.common.data.network.appJson
import com.zc.bakamitai.common.util.SubsPleaseFixtures
import com.zc.bakamitai.feature.details.data.dto.EpisodeDTO
import com.zc.bakamitai.feature.details.data.dto.ShowEpisodesDTO
import kotlinx.datetime.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ShowEpisodesDTOMapperTest {

    private val zone = TimeZone.of("America/New_York")

    private fun episode(label: String?, releaseDate: String? = "Tue, 17 Nov 2026 12:38:52 -0500") =
        EpisodeDTO(releaseDate = releaseDate, show = "One Piece", episode = label, downloads = null)

    @Test
    fun `maps both lists of the real response, with the given zone`() {
        val dto = appJson.decodeFromString<ShowEpisodesDTO>(SubsPleaseFixtures.read(SubsPleaseFixtures.SHOW_EPISODES))

        val episodes = dto.toDomain(zone)

        assertEquals(listOf("28", "27", "01"), episodes.episodes.map { it.label })
        assertEquals(listOf("01-28"), episodes.batches.map { it.label })
        assertTrue(episodes.episodes.all { it.downloads.size == 3 })
        assertEquals(zone, episodes.timeZone)
        assertFalse(episodes.isTruncated)
    }

    @Test
    fun `revisions and specials of the real response are kept in response order`() {
        val dto = appJson.decodeFromString<ShowEpisodesDTO>(
            SubsPleaseFixtures.read(SubsPleaseFixtures.SHOW_EPISODES_SPECIALS),
        )

        assertEquals(
            listOf("1220", "1219", "1218", "1216v2", "1217", "1216", "The Counterfeit Case of Ultra 30"),
            dto.toDomain(zone).episodes.map { it.label },
        )
    }

    @Test
    fun `unusable entries are dropped, missing lists are empty`() {
        val dto = ShowEpisodesDTO(
            episodes = listOf(episode("02"), episode(null), episode("01", releaseDate = "bad")),
            batches = null,
        )

        val episodes = dto.toDomain(zone)

        assertEquals(listOf("02"), episodes.episodes.map { it.label })
        assertEquals(emptyList(), episodes.batches)
    }

    @Test
    fun `52 raw entries mean the list is truncated, 51 don't, invalid ones included`() {
        val full = List(52) { index -> episode(if (index == 0) null else "${1180 - index}") }

        assertTrue(ShowEpisodesDTO(episodes = full, batches = emptyList()).toDomain(zone).isTruncated)
        assertFalse(ShowEpisodesDTO(episodes = full.drop(1), batches = emptyList()).toDomain(zone).isTruncated)
    }
}
