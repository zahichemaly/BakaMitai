package com.zc.bakamitai.feature.details.data.mapper

import com.zc.bakamitai.common.domain.model.Show
import com.zc.bakamitai.common.domain.model.ShowSlug
import com.zc.bakamitai.feature.details.data.dto.ShowPageDTO
import com.zc.bakamitai.feature.details.domain.model.ShowOverview
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ShowPageDTOMapperTest {

    private val slug = ShowSlug("sousou-no-frieren")

    private val page = ShowPageDTO(
        title = "Sousou no Frieren",
        synopsisParagraphs = listOf("The demon king has been defeated.", "Before his death, Heiter…"),
        coverSrc = "/wp-content/uploads/2023/09/138006.jpg",
        sid = "620",
    )

    @Test
    fun `maps the page`() {
        assertEquals(
            ShowOverview(
                show = Show(
                    slug = slug,
                    title = "Sousou no Frieren",
                    coverUrl = "https://subsplease.org/wp-content/uploads/2023/09/138006.jpg",
                ),
                synopsis = "The demon king has been defeated.\n\nBefore his death, Heiter…",
                webUrl = "https://subsplease.org/shows/sousou-no-frieren/",
            ),
            page.toDomain(slug),
        )
    }

    @Test
    fun `the title is trimmed with whitespace collapsed, and a blank or missing one drops the page`() {
        assertEquals("Sousou no Frieren", page.copy(title = "  Sousou \n no\tFrieren ").toDomain(slug)?.show?.title)
        assertNull(page.copy(title = "  ").toDomain(slug))
        assertNull(page.copy(title = null).toDomain(slug))
    }

    @Test
    fun `the cover goes through the shared image rules`() {
        fun cover(src: String?) = page.copy(coverSrc = src).toDomain(slug)?.show?.coverUrl

        assertEquals("https://subsplease.org/wp-content/uploads/x.jpg", cover("/wp-content/uploads/x.jpg"))
        assertEquals("https://cdn.example/x.jpg", cover("https://cdn.example/x.jpg"))
        assertNull(cover("/wp-includes/images/image-coming-soon-placeholder.png"))
        assertNull(cover(null))
    }

    @Test
    fun `paragraphs have their whitespace collapsed, and blank ones are dropped`() {
        val synopsis = page.copy(synopsisParagraphs = listOf(" One  line\n broken. ", " ", "Two.")).toDomain(slug)

        assertEquals("One line broken.\n\nTwo.", synopsis?.synopsis)
    }

    @Test
    fun `no paragraphs or the placeholder mean no synopsis`() {
        fun synopsis(vararg paragraphs: String) = page.copy(synopsisParagraphs = paragraphs.toList()).toDomain(slug)

        assertNull(synopsis()?.synopsis)
        assertNull(synopsis("Synopsis will be added later")?.synopsis)
        assertNull(synopsis("SYNOPSIS WILL BE ADDED LATER.")?.synopsis)
        assertEquals("Synopsis will be added later, maybe.", synopsis("Synopsis will be added later, maybe.")?.synopsis)
    }

    @Test
    fun `the sid is digits only`() {
        assertEquals("620", page.copy(sid = " 620 ").toSid())
        assertNull(page.copy(sid = "abc").toSid())
        assertNull(page.copy(sid = "62a").toSid())
        assertNull(page.copy(sid = " ").toSid())
        assertNull(page.copy(sid = null).toSid())
    }
}
