package com.zc.bakamitai.feature.shows.data.mapper

import com.zc.bakamitai.common.domain.model.Show
import com.zc.bakamitai.common.domain.model.ShowSlug
import com.zc.bakamitai.feature.shows.data.dto.ShowLinkDTO
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ShowLinkDTOMapperTest {

    private fun link(href: String?, title: String? = "Title", text: String? = "Text") = ShowLinkDTO(href, title, text)

    private fun slugOf(href: String?) = link(href).toDomain()?.slug

    @Test
    fun `a site-relative href maps to its slug, the title and no cover`() {
        assertEquals(
            Show(slug = ShowSlug("86-eighty-six"), title = "86 - Eighty Six", coverUrl = null),
            link("/shows/86-eighty-six", title = "86 - Eighty Six").toDomain(),
        )
    }

    @Test
    fun `trailing slashes, absolute URLs, queries and fragments are handled`() {
        assertEquals(ShowSlug("x"), slugOf("/shows/x/"))
        assertEquals(ShowSlug("x"), slugOf("https://subsplease.org/shows/x/"))
        assertEquals(ShowSlug("x"), slugOf("http://subsplease.org/shows/x"))
        assertEquals(ShowSlug("x"), slugOf("/shows/x?a=b#c"))
        assertEquals(ShowSlug("x"), slugOf("/shows/x/#c"))
    }

    @Test
    fun `slugs are opaque`() {
        assertEquals(ShowSlug("d_cide-traumerei-the-animation"), slugOf("/shows/d_cide-traumerei-the-animation"))
    }

    @Test
    fun `links that aren't a show page are dropped`() {
        assertNull(slugOf("/shows/"))
        assertNull(slugOf("https://subsplease.org/shows/"))
        assertNull(slugOf("/other/x"))
        assertNull(slugOf("/shows/x/episodes"))
        assertNull(slugOf("#A"))
        assertNull(slugOf(null))
    }

    @Test
    fun `the title is trimmed with its whitespace collapsed`() {
        val show = link("/shows/x", title = "  Fruits \n Basket\t(2019)  S2 ").toDomain()

        assertEquals("Fruits Basket (2019) S2", show?.title)
    }

    @Test
    fun `a blank title falls back to the text`() {
        assertEquals("Text", link("/shows/x", title = "   ").toDomain()?.title)
        assertEquals("Text", link("/shows/x", title = null).toDomain()?.title)
    }

    @Test
    fun `a link without title and text is dropped`() {
        assertNull(link("/shows/x", title = null, text = " ").toDomain())
    }

    @Test
    fun `a list keeps the usable links in order`() {
        val shows = listOf(link("/shows/b", title = "B"), link("/other"), link("/shows/a", title = "A")).toDomain()

        assertEquals(listOf("b", "a"), shows.map { it.slug.value })
    }
}
