package com.zc.bakamitai.feature.shows.data.remote

import com.zc.bakamitai.common.data.network.appJson
import com.zc.bakamitai.common.data.network.createHttpClient
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

class ShowsServiceTest {

    private val requests = mutableListOf<HttpRequestData>()

    private fun service(body: ByteArray) = ShowsService(
        createHttpClient(
            json = appJson,
            userAgent = "test",
            engine = MockEngine { request ->
                requests += request
                respond(content = body, headers = headersOf(HttpHeaders.ContentType, "text/html; charset=UTF-8"))
            },
        ),
    )

    @Test
    fun `getAllShowsPage sends GET shows without parameters`() = runTest {
        service("<html></html>".encodeToByteArray()).getAllShowsPage()

        val request = requests.single()
        assertEquals(HttpMethod.Get, request.method)
        assertEquals("subsplease.org", request.url.host)
        assertEquals("/shows/", request.url.encodedPath)
        assertTrue(request.url.parameters.isEmpty())
    }

    @Test
    fun `getAllShowsPage returns the body unchanged, decoded as UTF-8`() = runTest {
        val html = """<div class="all-shows-link"><a title="Rock’n Roll – Ōkami">Rock’n Roll – Ōkami</a></div>"""

        assertEquals(html, service(html.encodeToByteArray()).getAllShowsPage())
    }
}
