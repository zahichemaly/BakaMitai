package com.zc.bakamitai.feature.details.data.mapper

import com.zc.bakamitai.feature.details.data.dto.DownloadDTO
import com.zc.bakamitai.feature.details.data.dto.EpisodeDTO
import com.zc.bakamitai.feature.details.domain.model.Resolution
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Instant

class EpisodeDTOMapperTest {

    private fun download(res: String, magnet: String = "magnet:?xt=urn:btih:$res") =
        DownloadDTO(res = res, magnet = magnet, torrent = null, xdcc = null)

    private val episode = EpisodeDTO(
        releaseDate = "Tue, 17 Nov 2026 12:38:52 -0500",
        show = "Sora wa Akai Kawa no Hotori",
        episode = "20",
        downloads = listOf(download("480"), download("720"), download("1080")),
    )

    @Test
    fun `maps the label, the instant and the downloads in response order`() {
        val mapped = episode.toDomain()

        assertEquals("20", mapped?.label)
        assertEquals(Instant.parse("2026-11-17T17:38:52Z"), mapped?.releasedAt)
        assertEquals(listOf(Resolution.SD, Resolution.HD, Resolution.FHD), mapped?.downloads?.map { it.resolution })
    }

    @Test
    fun `labels are kept as SubsPlease writes them`() {
        listOf("1216v2", "Egghead SP11", "Movie", "01-28", "The Counterfeit Case of Ultra 30").forEach { label ->
            assertEquals(label, episode.copy(episode = " $label ").toDomain()?.label)
        }
    }

    @Test
    fun `a blank label or an unparseable date drops the episode`() {
        assertNull(episode.copy(episode = " ").toDomain())
        assertNull(episode.copy(episode = null).toDomain())
        assertNull(episode.copy(releaseDate = "11/17/26").toDomain())
        assertNull(episode.copy(releaseDate = null).toDomain())
    }

    @Test
    fun `an episode without downloads is kept, with none`() {
        assertEquals(emptyList(), episode.copy(downloads = null).toDomain()?.downloads)
        assertEquals(emptyList(), episode.copy(downloads = listOf(download("2160"))).toDomain()?.downloads)
    }

    @Test
    fun `one download per resolution, the highest SD among several`() {
        val mapped = episode.copy(
            downloads = listOf(
                download("360"),
                download("720", magnet = "magnet:?first"),
                download("480"),
                download("720", magnet = "magnet:?second"),
            ),
        ).toDomain()

        assertEquals(listOf(480, 720), mapped?.downloads?.map { it.height })
        assertEquals("magnet:?first", mapped?.downloads?.last()?.magnetUri)
    }
}
