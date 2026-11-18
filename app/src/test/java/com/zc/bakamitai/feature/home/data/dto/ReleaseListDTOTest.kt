package com.zc.bakamitai.feature.home.data.dto

import com.zc.bakamitai.common.data.network.appJson
import com.zc.bakamitai.common.util.SubsPleaseFixtures
import kotlinx.serialization.SerializationException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ReleaseListDTOTest {

    private fun decode(json: String): ReleaseListDTO = appJson.decodeFromString(ReleaseListDTO.serializer(), json)

    @Test
    fun `an object root becomes the list of its values, ignoring downloads and xdcc`() {
        val list = decode(SubsPleaseFixtures.read(SubsPleaseFixtures.LATEST_PAGE_1))

        assertFalse(list.isLimitReached)
        assertEquals(
            listOf("psyren", "tensei-goblin-dakedo-shitsumon-aru", "dark-summoner-to-dekiteiru", "kanata-kara"),
            list.releases.map { it.page },
        )
        assertEquals(
            ReleaseDTO(
                time = "Nov 16th",
                releaseDate = "Mon, 16 Nov 2026 08:32:02 -0500",
                show = "PSYREN",
                episode = "06",
                imageUrl = "/wp-content/uploads/2026/10/160405.jpg",
                page = "psyren",
            ),
            list.releases.first(),
        )
    }

    @Test
    fun `batch and movie episodes are kept as strings`() {
        val list = decode(SubsPleaseFixtures.read(SubsPleaseFixtures.SEARCH_SHOW))

        assertEquals(listOf("07", "06", "01-12", "12", "Movie"), list.releases.map { it.episode })
    }

    @Test
    fun `an empty array root is an empty list`() {
        val list = decode(SubsPleaseFixtures.read(SubsPleaseFixtures.SEARCH_NO_RESULTS))

        assertEquals(ReleaseListDTO(releases = emptyList(), isLimitReached = false), list)
    }

    @Test
    fun `limit_reached is an empty list past the last page`() {
        val list = decode(SubsPleaseFixtures.read(SubsPleaseFixtures.LATEST_LIMIT_REACHED))

        assertTrue(list.isLimitReached)
        assertTrue(list.releases.isEmpty())
    }

    @Test
    fun `missing fields decode to null`() {
        val list = decode("""{"Show - 01":{"show":"Show"}}""")

        assertEquals(
            ReleaseDTO(time = null, releaseDate = null, show = "Show", episode = null, imageUrl = null, page = null),
            list.releases.single(),
        )
    }

    @Test
    fun `a primitive root is rejected`() {
        assertFailsWith<SerializationException> { decode("42") }
    }
}
