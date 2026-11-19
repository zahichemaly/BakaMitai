package com.zc.bakamitai.feature.details.data.dto

import com.zc.bakamitai.common.data.network.appJson
import com.zc.bakamitai.common.util.SubsPleaseFixtures
import kotlinx.serialization.SerializationException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class ShowEpisodesDTOTest {

    private fun decode(json: String): ShowEpisodesDTO = appJson.decodeFromString(json)

    private fun fixture(name: String) = decode(SubsPleaseFixtures.read(name))

    @Test
    fun `episodes and batch objects decode to their values, in response order`() {
        val dto = fixture(SubsPleaseFixtures.SHOW_EPISODES)

        assertEquals(listOf("28", "27", "01"), dto.episodes?.map { it.episode })
        assertEquals(listOf("01-28"), dto.batches?.map { it.episode })
        val latest = assertNotNull(dto.episodes).first()
        val download = assertNotNull(latest.downloads).last()
        assertEquals("1080", download.res)
        assertEquals("https://nyaa.si/view/1793092/torrent", download.torrent)
        assertEquals("%22%5BSubsPlease%5D+Sousou+no+Frieren+-+28+%281080p%29%22", download.xdcc)
        assertEquals("Fri, 22 Mar 2024 15:32:44 +0000", latest.releaseDate)
    }

    @Test
    fun `every fixture decodes, empty lists as empty arrays`() {
        assertEquals(7, fixture(SubsPleaseFixtures.SHOW_EPISODES_SPECIALS).episodes?.size)
        assertEquals(emptyList(), fixture(SubsPleaseFixtures.SHOW_EPISODES_SPECIALS).batches)
        assertEquals(listOf("01-12"), fixture(SubsPleaseFixtures.SHOW_EPISODES_SD).batches?.map { it.episode })
        assertEquals(listOf("Movie"), fixture(SubsPleaseFixtures.SHOW_EPISODES_MOVIE).episodes?.map { it.episode })
    }

    @Test
    fun `an unknown sid decodes to two empty lists`() {
        val dto = fixture(SubsPleaseFixtures.SHOW_EPISODES_EMPTY)

        assertEquals(emptyList(), dto.episodes)
        assertEquals(emptyList(), dto.batches)
    }

    @Test
    fun `an episode array next to a batch object`() {
        val dto = decode("""{"episode":[],"batch":{"X - 01-12":{"episode":"01-12","release_date":"r"}}}""")

        assertEquals(emptyList(), dto.episodes)
        assertEquals(listOf("01-12"), dto.batches?.map { it.episode })
        assertNull(dto.batches?.single()?.downloads)
    }

    @Test
    fun `a non-empty array decodes its elements`() {
        val dto = decode("""{"episode":[{"episode":"01"},{"episode":"02"}],"batch":[]}""")

        assertEquals(listOf("01", "02"), dto.episodes?.map { it.episode })
    }

    @Test
    fun `missing or null lists are null`() {
        val dto = decode("""{"episode":null}""")

        assertNull(dto.episodes)
        assertNull(dto.batches)
    }

    @Test
    fun `a list that isn't an object or an array fails`() {
        assertFailsWith<SerializationException> { decode("""{"episode":"none","batch":[]}""") }
    }
}
