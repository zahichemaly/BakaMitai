package com.zc.bakamitai.feature.details.data.remote

import com.zc.bakamitai.common.data.network.appJson
import com.zc.bakamitai.common.data.network.createHttpClient
import com.zc.bakamitai.common.util.SubsPleaseFixtures
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpRequestData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ShowDetailsServiceTest {

    private val requests = mutableListOf<HttpRequestData>()

    private fun service(body: String) = ShowDetailsService(
        createHttpClient(
            json = appJson,
            userAgent = "test",
            engine = MockEngine { request ->
                requests += request
                // SubsPlease serves both the pages and the API as text/html.
                respond(
                    content = body.encodeToByteArray(),
                    headers = headersOf(HttpHeaders.ContentType, "text/html; charset=UTF-8"),
                )
            },
        ),
    )

    @Test
    fun `getShowPage sends GET shows slug with the trailing slash and no parameters`() = runTest {
        service("<html></html>").getShowPage("sousou-no-frieren")

        val request = requests.single()
        assertEquals(HttpMethod.Get, request.method)
        assertEquals("subsplease.org", request.url.host)
        assertEquals("/shows/sousou-no-frieren/", request.url.encodedPath)
        assertTrue(request.url.parameters.isEmpty())
    }

    @Test
    fun `getShowPage keeps an underscore in the slug`() = runTest {
        service("<html></html>").getShowPage("d_cide-traumerei-the-animation")

        assertEquals("/shows/d_cide-traumerei-the-animation/", requests.single().url.encodedPath)
    }

    @Test
    fun `getShowPage returns the body as UTF-8 text`() = runTest {
        val html = SubsPleaseFixtures.read(SubsPleaseFixtures.SHOW_PAGE)

        val page = service(html).getShowPage("sousou-no-frieren")

        assertEquals(html, page)
        assertTrue("The four—mage Frieren" in page)
    }

    @Test
    fun `getShowEpisodes sends f show with tz and sid, and decodes a text-html body`() = runTest {
        val episodes = service(SubsPleaseFixtures.read(SubsPleaseFixtures.SHOW_EPISODES))
            .getShowEpisodes(sid = "620", timeZoneId = "America/New_York")

        val request = requests.single()
        assertEquals(HttpMethod.Get, request.method)
        assertEquals("/api/", request.url.encodedPath)
        assertEquals("show", request.url.parameters["f"])
        assertEquals("America/New_York", request.url.parameters["tz"])
        assertEquals("620", request.url.parameters["sid"])
        assertEquals(listOf("28", "27", "01"), episodes.episodes?.map { it.episode })
        assertEquals(listOf("01-28"), episodes.batches?.map { it.episode })
    }
}
