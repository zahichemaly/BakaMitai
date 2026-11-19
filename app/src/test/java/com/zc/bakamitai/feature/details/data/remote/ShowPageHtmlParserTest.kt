package com.zc.bakamitai.feature.details.data.remote

import com.zc.bakamitai.common.util.SubsPleaseFixtures
import com.zc.bakamitai.feature.details.data.dto.ShowPageDTO
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ShowPageHtmlParserTest {

    private val parser = ShowPageHtmlParser()

    private fun page(body: String) = "<html><body>$body</body></html>"

    @Test
    fun `the real page gives its title, both paragraphs, the cover and the sid`() {
        val page = parser.parse(SubsPleaseFixtures.read(SubsPleaseFixtures.SHOW_PAGE))

        assertEquals("Sousou no Frieren", page.title)
        assertEquals(2, page.synopsisParagraphs.size)
        assertTrue(page.synopsisParagraphs[0].startsWith("The demon king has been defeated"))
        assertTrue("The four—mage Frieren" in page.synopsisParagraphs[0])
        assertTrue(page.synopsisParagraphs[1].startsWith("Before his death, Heiter"))
        assertTrue(page.synopsisParagraphs.none { "Synopsis" == it || "One stop shop" in it })
        assertEquals("/wp-content/uploads/2023/09/138006.jpg", page.coverSrc)
        assertEquals("620", page.sid)
    }

    @Test
    fun `entities in the title are decoded`() {
        fun title(raw: String) = parser.parse(page("""<h1 class="entry-title">$raw</h1>""")).title

        assertEquals("A3! Season Autumn & Winter", title("A3! Season Autumn &amp; Winter"))
        assertEquals("BanG Dream! It’s MyGO!!!!!", title("BanG Dream! It&#8217;s MyGO!!!!!"))
        assertEquals("Dark Machine – The Animation", title("Dark Machine &#8211; The Animation"))
    }

    @Test
    fun `the placeholder synopsis is read as text`() {
        val page = parser.parse(
            page("""<div class="series-syn"><h2>Synopsis</h2><p><i>Synopsis will be added later</i></p></div>"""),
        )

        assertEquals(listOf("Synopsis will be added later"), page.synopsisParagraphs)
    }

    @Test
    fun `a synopsis written outside paragraphs is one paragraph, without the heading`() {
        val page = parser.parse(page("""<div class="series-syn"><h2>Synopsis</h2>A short synopsis.</div>"""))

        assertEquals(listOf("A short synopsis."), page.synopsisParagraphs)
    }

    @Test
    fun `blank paragraphs are left out`() {
        val page = parser.parse(page("""<div class="series-syn"><p> </p><p>Text.</p><p></p></div>"""))

        assertEquals(listOf("Text."), page.synopsisParagraphs)
    }

    @Test
    fun `missing elements are null or empty`() {
        assertEquals(
            ShowPageDTO(title = null, synopsisParagraphs = emptyList(), coverSrc = null, sid = null),
            parser.parse(page("")),
        )
    }

    @Test
    fun `blank attributes are null`() {
        val page = parser.parse(
            page(
                """<h1 class="entry-title"> </h1><div id="secondary"><img class="img-responsive" src=""></div>""" +
                    """<table id="show-release-table" sid=""></table>""",
            ),
        )

        assertEquals(null, page.title)
        assertEquals(null, page.coverSrc)
        assertEquals(null, page.sid)
    }
}
