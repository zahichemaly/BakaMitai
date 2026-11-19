package com.zc.bakamitai.feature.shows.data.repository

import com.zc.bakamitai.common.data.network.appJson
import com.zc.bakamitai.common.data.network.createHttpClient
import com.zc.bakamitai.common.domain.model.DomainError
import com.zc.bakamitai.common.domain.model.ShowSlug
import com.zc.bakamitai.common.domain.util.TestDispatcherProvider
import com.zc.bakamitai.common.util.SubsPleaseFixtures
import com.zc.bakamitai.feature.shows.data.remote.AllShowsHtmlParser
import com.zc.bakamitai.feature.shows.data.remote.ShowsService
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import java.io.IOException
import kotlin.test.Test
import kotlin.test.assertEquals

class ShowCatalogRepositoryImplTest {

    private val dispatcher = StandardTestDispatcher()
    private val fixture = SubsPleaseFixtures.read(SubsPleaseFixtures.SHOWS_PAGE)

    private var requestCount = 0
    private var body: String = fixture
    private var status: HttpStatusCode = HttpStatusCode.OK
    private var failWith: Exception? = null

    private val repository = ShowCatalogRepositoryImpl(
        service = ShowsService(
            createHttpClient(
                json = appJson,
                userAgent = "test",
                engine = MockEngine { _ ->
                    requestCount++
                    failWith?.let { throw it }
                    respond(body, status, headersOf(HttpHeaders.ContentType, "text/html; charset=UTF-8"))
                },
            ),
        ),
        parser = AllShowsHtmlParser(),
        dispatchers = TestDispatcherProvider(dispatcher),
    )

    private fun test(block: suspend TestScope.() -> Unit) = runTest(dispatcher) { block() }

    private fun respondWith(newStatus: HttpStatusCode, newBody: String = "oops") {
        status = newStatus
        body = newBody
    }

    @Test
    fun `the page maps to its shows in page order, duplicates of a slug included`() = test {
        val shows = repository.getAllShows(forceRefresh = false).getOrThrow()

        assertEquals(1, requestCount)
        assertEquals(10, shows.size)
        assertEquals("100-man no Inochi no Ue ni Ore wa Tatte Iru", shows.first().title)
        assertEquals("Monogatari Series - Off & Monster Season", shows.last().title)
        assertEquals(3, shows.count { it.slug == ShowSlug("fruits-basket-2019") })
        assertEquals(listOf(null), shows.map { it.coverUrl }.distinct())
    }

    @Test
    fun `a second call is served from the cache`() = test {
        val first = repository.getAllShows(forceRefresh = false)

        val second = repository.getAllShows(forceRefresh = false)

        assertEquals(1, requestCount)
        assertEquals(first, second)
    }

    @Test
    fun `forceRefresh requests again and refills the cache`() = test {
        repository.getAllShows(forceRefresh = false)

        repository.getAllShows(forceRefresh = true)
        repository.getAllShows(forceRefresh = false)

        assertEquals(2, requestCount)
    }

    @Test
    fun `a failure isn't cached and doesn't clear the previous catalogue`() = test {
        respondWith(HttpStatusCode.InternalServerError)
        assertEquals(DomainError.Server, repository.getAllShows(forceRefresh = false).exceptionOrNull())

        respondWith(HttpStatusCode.OK, fixture)
        val loaded = repository.getAllShows(forceRefresh = false).getOrThrow()

        respondWith(HttpStatusCode.InternalServerError)
        assertEquals(DomainError.Server, repository.getAllShows(forceRefresh = true).exceptionOrNull())

        assertEquals(loaded, repository.getAllShows(forceRefresh = false).getOrThrow())
        assertEquals(3, requestCount)
    }

    @Test
    fun `concurrent calls share one request`() = test {
        val results = List(2) { async { repository.getAllShows(forceRefresh = false) } }.awaitAll()

        assertEquals(1, requestCount)
        assertEquals(results[0], results[1])
    }

    @Test
    fun `a 500 maps to Server`() = test {
        respondWith(HttpStatusCode.InternalServerError)

        assertEquals(DomainError.Server, repository.getAllShows(forceRefresh = false).exceptionOrNull())
    }

    @Test
    fun `a 404 maps to NotFound`() = test {
        respondWith(HttpStatusCode.NotFound)

        assertEquals(DomainError.NotFound, repository.getAllShows(forceRefresh = false).exceptionOrNull())
    }

    @Test
    fun `a connection failure maps to Network`() = test {
        failWith = IOException("reset")

        assertEquals(DomainError.Network, repository.getAllShows(forceRefresh = false).exceptionOrNull())
    }

    @Test
    fun `a page without the list maps to InvalidData`() = test {
        respondWith(HttpStatusCode.OK, "<html></html>")

        assertEquals(DomainError.InvalidData, repository.getAllShows(forceRefresh = false).exceptionOrNull())
    }

    @Test
    fun `a list with only unusable links maps to InvalidData, and isn't cached`() = test {
        respondWith(
            HttpStatusCode.OK,
            """<div class="all-shows"><div class="all-shows-link"><a href="/other/x" title="X">X</a></div></div>""",
        )

        assertEquals(DomainError.InvalidData, repository.getAllShows(forceRefresh = false).exceptionOrNull())

        respondWith(HttpStatusCode.OK, fixture)
        assertEquals(10, repository.getAllShows(forceRefresh = false).getOrThrow().size)
        assertEquals(2, requestCount)
    }
}
