package com.zc.bakamitai.feature.details.data.repository

import com.zc.bakamitai.common.data.network.appJson
import com.zc.bakamitai.common.data.network.createHttpClient
import com.zc.bakamitai.common.domain.model.DomainError
import com.zc.bakamitai.common.domain.model.ShowSlug
import com.zc.bakamitai.common.domain.util.TestDispatcherProvider
import com.zc.bakamitai.common.util.FixedClock
import com.zc.bakamitai.common.util.SubsPleaseFixtures
import com.zc.bakamitai.feature.details.data.remote.ShowDetailsService
import com.zc.bakamitai.feature.details.data.remote.ShowPageHtmlParser
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpRequestData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.TimeZone
import java.io.IOException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

class ShowDetailsRepositoryImplTest {

    private val dispatcher = StandardTestDispatcher()
    private val clock = FixedClock(Instant.parse("2026-11-19T08:00:00Z"))
    private val slug = ShowSlug("sousou-no-frieren")
    private val newYork = TimeZone.of("America/New_York")
    private val tokyo = TimeZone.of("Asia/Tokyo")

    private var pageBody = SubsPleaseFixtures.read(SubsPleaseFixtures.SHOW_PAGE)
    private var pageStatus = HttpStatusCode.OK
    private var episodesBody = SubsPleaseFixtures.read(SubsPleaseFixtures.SHOW_EPISODES)
    private var episodesStatus = HttpStatusCode.OK
    private var failWith: Exception? = null

    private val requests = mutableListOf<HttpRequestData>()
    private val pageRequests get() = requests.count { it.url.encodedPath.startsWith("/shows/") }
    private val episodeRequests get() = requests.count { it.url.encodedPath == "/api/" }

    private val repository = ShowDetailsRepositoryImpl(
        service = ShowDetailsService(
            createHttpClient(
                json = appJson,
                userAgent = "test",
                engine = MockEngine { request ->
                    requests += request
                    failWith?.let { throw it }
                    val isPage = request.url.encodedPath.startsWith("/shows/")
                    respond(
                        content = if (isPage) pageBody else episodesBody,
                        status = if (isPage) pageStatus else episodesStatus,
                        headers = headersOf(HttpHeaders.ContentType, "text/html; charset=UTF-8"),
                    )
                },
            ),
        ),
        parser = ShowPageHtmlParser(),
        dispatchers = TestDispatcherProvider(dispatcher),
        clock = clock,
    )

    private fun test(block: suspend TestScope.() -> Unit) = runTest(dispatcher) { block() }

    private suspend fun overview(forceRefresh: Boolean = false) = repository.getOverview(slug, forceRefresh)

    private suspend fun episodes(zone: TimeZone = newYork, forceRefresh: Boolean = false) =
        repository.getEpisodes(slug, zone, forceRefresh)

    @Test
    fun `the overview is the page's title, cover, synopsis and URL`() = test {
        val overview = overview().getOrThrow()

        assertEquals("Sousou no Frieren", overview.show.title)
        assertEquals(slug, overview.show.slug)
        assertEquals("https://subsplease.org/wp-content/uploads/2023/09/138006.jpg", overview.show.coverUrl)
        assertEquals(2, overview.synopsis?.split("\n\n")?.size)
        assertEquals("https://subsplease.org/shows/sousou-no-frieren/", overview.webUrl)
        assertEquals("/shows/sousou-no-frieren/", requests.single().url.encodedPath)
    }

    @Test
    fun `the overview is cached for 10 minutes`() = test {
        val first = overview()

        clock.instant += 9.minutes + 59.seconds
        assertEquals(first, overview())
        assertEquals(1, pageRequests)

        clock.instant += 1.seconds
        overview()
        assertEquals(2, pageRequests)
    }

    @Test
    fun `forceRefresh fetches the page again`() = test {
        overview()

        overview(forceRefresh = true)

        assertEquals(2, pageRequests)
    }

    @Test
    fun `episodes of a new show need the page, then f show, then nothing within 10 minutes`() = test {
        val first = episodes().getOrThrow()

        assertEquals(1, pageRequests)
        assertEquals(1, episodeRequests)
        assertEquals(listOf("28", "27", "01"), first.episodes.map { it.label })
        assertEquals(listOf("01-28"), first.batches.map { it.label })

        assertEquals(first, episodes().getOrThrow())
        overview()
        assertEquals(2, requests.size)
    }

    @Test
    fun `f show gets the page's sid and the given zone`() = test {
        episodes(zone = tokyo)

        val request = requests.last()
        assertEquals("show", request.url.parameters["f"])
        assertEquals("620", request.url.parameters["sid"])
        assertEquals("Asia/Tokyo", request.url.parameters["tz"])
    }

    @Test
    fun `cached episodes come back with the caller's zone`() = test {
        episodes(zone = newYork)

        assertEquals(tokyo, episodes(zone = tokyo).getOrThrow().timeZone)
        assertEquals(1, episodeRequests)
    }

    @Test
    fun `expired episodes reuse the expired page's sid`() = test {
        episodes()
        clock.instant += 11.minutes

        episodes()

        assertEquals(1, pageRequests)
        assertEquals(2, episodeRequests)
    }

    @Test
    fun `forceRefresh fetches the episodes again with the cached sid`() = test {
        episodes()

        episodes(forceRefresh = true)

        assertEquals(1, pageRequests)
        assertEquals(2, episodeRequests)
    }

    @Test
    fun `a failure isn't cached and keeps the older entry`() = test {
        val loaded = overview().getOrThrow()

        pageStatus = HttpStatusCode.InternalServerError
        assertEquals(DomainError.Server, overview(forceRefresh = true).exceptionOrNull())

        assertEquals(loaded, overview().getOrThrow())
        assertEquals(2, pageRequests)
    }

    @Test
    fun `a failed episodes call isn't cached`() = test {
        episodesStatus = HttpStatusCode.InternalServerError
        assertEquals(DomainError.Server, episodes().exceptionOrNull())

        episodesStatus = HttpStatusCode.OK
        assertEquals(3, episodes().getOrThrow().episodes.size)
        assertEquals(1, pageRequests)
        assertEquals(2, episodeRequests)
    }

    @Test
    fun `an unknown slug's 404 maps to NotFound`() = test {
        pageStatus = HttpStatusCode.NotFound

        assertEquals(DomainError.NotFound, overview().exceptionOrNull())
        assertEquals(DomainError.NotFound, episodes().exceptionOrNull())
        assertEquals(0, episodeRequests)
    }

    @Test
    fun `a 500 maps to Server, a connection failure to Network`() = test {
        pageStatus = HttpStatusCode.InternalServerError
        assertEquals(DomainError.Server, overview().exceptionOrNull())

        failWith = IOException("reset")
        assertEquals(DomainError.Network, overview().exceptionOrNull())
    }

    @Test
    fun `a page without a title or a sid maps to InvalidData`() = test {
        pageBody = """<table id="show-release-table" sid="620"></table>"""
        assertEquals(DomainError.InvalidData, overview().exceptionOrNull())

        pageBody = """<h1 class="entry-title">Sousou no Frieren</h1><table id="show-release-table"></table>"""
        assertEquals(DomainError.InvalidData, overview().exceptionOrNull())
        assertEquals(DomainError.InvalidData, episodes().exceptionOrNull())
        assertEquals(0, episodeRequests)
    }

    @Test
    fun `an empty f show body maps to InvalidData`() = test {
        episodesBody = ""

        assertEquals(DomainError.InvalidData, episodes().exceptionOrNull())
    }

    @Test
    fun `an unknown sid's empty lists are a success with nothing in them`() = test {
        episodesBody = SubsPleaseFixtures.read(SubsPleaseFixtures.SHOW_EPISODES_EMPTY)

        val episodes = episodes().getOrThrow()

        assertEquals(emptyList(), episodes.episodes)
        assertEquals(emptyList(), episodes.batches)
    }
}
