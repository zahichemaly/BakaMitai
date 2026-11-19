package com.zc.bakamitai.feature.shows.data.remote

import com.zc.bakamitai.common.util.SubsPleaseFixtures
import com.zc.bakamitai.feature.shows.data.dto.ShowLinkDTO
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AllShowsHtmlParserTest {

    private val parser = AllShowsHtmlParser()

    private fun page(links: String) = """<html><body><div class="all-shows">$links</div></body></html>"""

    private fun link(attributes: String, text: String) =
        """<div class="all-shows-link"><a $attributes>$text</a></div>"""

    @Test
    fun `the fixture yields its entries in page order despite the mismatched headers`() {
        val links = parser.parse(SubsPleaseFixtures.read(SubsPleaseFixtures.SHOWS_PAGE))

        assertEquals(
            listOf(
                "100-man no Inochi no Ue ni Ore wa Tatte Iru",
                "86 - Eighty Six",
                "A3! Season Autumn & Winter",
                "Dungeon no Naka no Hito",
                "D_Cide Traumerei the Animation",
                "Fruits Basket (2019)",
                "Fruits Basket (2019) S2",
                "Fruits Basket (2019) S3",
                "mono",
                "Monogatari Series - Off & Monster Season",
            ),
            links.map { it.title },
        )
        assertEquals(links.map { it.title }, links.map { it.text })
        assertEquals("/shows/86-eighty-six", links[1].href)
        val fruitsBasketLinks = links.filter { it.title.orEmpty().startsWith("Fruits Basket") }
        assertEquals(List(3) { "/shows/fruits-basket-2019" }, fruitsBasketLinks.map { it.href })
    }

    @Test
    fun `headers, the jump links and the menu link outside the list are not entries`() {
        val links = parser.parse(SubsPleaseFixtures.read(SubsPleaseFixtures.SHOWS_PAGE))

        assertTrue(links.none { it.text in setOf("1", "8", "A", "D", "F", "M", "#", "Shows") })
        assertTrue(links.none { it.href == "https://subsplease.org/shows/" || it.href.orEmpty().startsWith("#") })
    }

    @Test
    fun `entities are decoded and a raw ampersand is kept`() {
        val links = parser.parse(
            page(
                link("""href="/shows/a" title="Kaguya &amp; Friends"""", "Kaguya &amp; Friends") +
                    link("""href="/shows/b" title="Rock&#8217;n Roll"""", "Rock&#8217;n Roll") +
                    link("""href="/shows/c" title="&quot;Oshi&quot;"""", "&quot;Oshi&quot;") +
                    link("""href="/shows/d" title="Off & Monster"""", "Off & Monster"),
            ),
        )

        assertEquals(
            listOf("Kaguya & Friends", "Rock’n Roll", "\"Oshi\"", "Off & Monster"),
            links.map { it.title },
        )
        assertEquals(links.map { it.title }, links.map { it.text })
    }

    @Test
    fun `a missing title attribute is null and the text is kept`() {
        val links = parser.parse(page(link("""href="/shows/x"""", "Only Text")))

        assertEquals(listOf(ShowLinkDTO(href = "/shows/x", title = null, text = "Only Text")), links)
    }

    @Test
    fun `nested tags are flattened into the text`() {
        val links = parser.parse(page(link("""href="/shows/x"""", "<span>X</span> Y")))

        assertEquals("X Y", links.single().text)
    }

    @Test
    fun `blank attributes and text are null`() {
        val links = parser.parse(page(link("""href=" " title=""""", "  ")))

        assertEquals(listOf(ShowLinkDTO(href = null, title = null, text = null)), links)
    }

    @Test
    fun `a page without the list yields nothing`() {
        assertEquals(emptyList(), parser.parse("""<html><body><a href="/shows/x" title="X">X</a></body></html>"""))
        assertEquals(emptyList(), parser.parse(""))
    }
}
