package com.zc.bakamitai.feature.home.data.mapper

import com.zc.bakamitai.common.data.network.appJson
import com.zc.bakamitai.common.util.SubsPleaseFixtures
import com.zc.bakamitai.feature.home.data.dto.ReleaseDTO
import com.zc.bakamitai.feature.home.data.dto.ReleaseListDTO
import com.zc.bakamitai.feature.home.domain.model.Release
import com.zc.bakamitai.feature.home.domain.model.Show
import com.zc.bakamitai.feature.home.domain.model.ShowSlug
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Instant

class ReleaseDTOMapperTest {

    private val release = ReleaseDTO(
        time = "Nov 15th",
        releaseDate = "Sun, 15 Nov 2026 10:03:53 -0500",
        show = "Kanata kara",
        episode = "07",
        imageUrl = "/wp-content/uploads/2026/10/159816.jpg",
        page = "kanata-kara",
    )

    @Test
    fun `maps a complete release`() {
        assertEquals(
            Release(
                show = Show(
                    slug = ShowSlug("kanata-kara"),
                    title = "Kanata kara",
                    coverUrl = "https://subsplease.org/wp-content/uploads/2026/10/159816.jpg",
                ),
                episode = "07",
                releasedAt = Instant.parse("2026-11-15T15:03:53Z"),
            ),
            release.toDomain(),
        )
    }

    @Test
    fun `release dates with any offset map to the same instant scale`() {
        fun releasedAt(raw: String) = release.copy(releaseDate = raw).toDomain()?.releasedAt

        assertEquals(Instant.parse("2026-11-15T15:03:53Z"), releasedAt("Sun, 15 Nov 2026 10:03:53 -0500"))
        assertEquals(Instant.parse("2026-07-07T17:41:51Z"), releasedAt("Tue, 07 Jul 2026 19:41:51 +0200"))
        assertEquals(Instant.parse("2026-11-18T14:33:47Z"), releasedAt("Wed, 18 Nov 2026 11:33:47 -0300"))
        assertEquals(Instant.parse("2026-11-18T12:03:04Z"), releasedAt("Wed, 18 Nov 2026 12:03:04 +0000"))
    }

    @Test
    fun `an unparseable, invalid or missing release date drops the release`() {
        assertNull(release.copy(releaseDate = "Nov 15th").toDomain())
        assertNull(release.copy(releaseDate = "Mon, 31 Nov 2026 10:03:53 -0500").toDomain())
        assertNull(release.copy(releaseDate = null).toDomain())
    }

    @Test
    fun `batch and movie episodes are kept as-is`() {
        assertEquals("01-24", release.copy(episode = "01-24").toDomain()?.episode)
        assertEquals("Movie", release.copy(episode = "Movie").toDomain()?.episode)
    }

    @Test
    fun `a missing page, show or episode drops the release`() {
        assertNull(release.copy(page = null).toDomain())
        assertNull(release.copy(show = null).toDomain())
        assertNull(release.copy(show = " ").toDomain())
        assertNull(release.copy(episode = null).toDomain())
    }

    @Test
    fun `the placeholder cover maps to null`() {
        val mapped = release.copy(imageUrl = "/wp-includes/images/image-coming-soon-placeholder.png").toDomain()

        assertNull(mapped?.show?.coverUrl)
    }

    @Test
    fun `a list keeps only the valid releases`() {
        val list = ReleaseListDTO(releases = listOf(release, release.copy(page = null)), isLimitReached = false)

        assertEquals(listOf("kanata-kara"), list.toDomain().map { it.show.slug.value })
    }

    @Test
    fun `the real newest response maps every release`() {
        val dto = appJson.decodeFromString<ReleaseListDTO>(SubsPleaseFixtures.read(SubsPleaseFixtures.SEARCH_NEWEST))

        val releases = dto.toDomain()

        assertEquals(listOf("19", "08", "07", "01-24"), releases.map { it.episode })
        assertNull(releases[2].show.coverUrl)
        assertEquals(Instant.parse("2026-11-16T19:03:26Z"), releases[3].releasedAt)
    }
}
