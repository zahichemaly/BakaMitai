package com.zc.bakamitai.feature.home.data.remote

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
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BroadcastServiceTest {

    private val requests = mutableListOf<HttpRequestData>()

    private fun service(fixture: String) = BroadcastService(
        createHttpClient(
            json = appJson,
            userAgent = "test",
            engine = MockEngine { request ->
                requests += request
                // SubsPlease serves its JSON as text/html (docs/api.md §1).
                respond(
                    content = SubsPleaseFixtures.read(fixture),
                    headers = headersOf(HttpHeaders.ContentType, "text/html; charset=UTF-8"),
                )
            },
        ),
    )

    private fun HttpRequestData.assertApiGet(vararg parameters: Pair<String, String>) {
        assertEquals(HttpMethod.Get, method)
        assertEquals("subsplease.org", url.host)
        assertEquals("/api/", url.encodedPath)
        assertEquals(parameters.toMap(), url.parameters.entries().associate { (key, values) -> key to values.single() })
    }

    @Test
    fun `getTodaySchedule requests today's schedule in the zone and decodes it`() = runTest {
        val dto = service(SubsPleaseFixtures.SCHEDULE_TODAY).getTodaySchedule("America/New_York")

        requests.single().assertApiGet("f" to "schedule", "h" to "true", "tz" to "America/New_York")
        assertEquals(4, dto.schedule?.size)
        assertEquals("sasaki-to-pii-chan-s2", dto.schedule?.first()?.page)
    }

    @Test
    fun `getLatestReleases requests the page in the zone and decodes it`() = runTest {
        val dto = service(SubsPleaseFixtures.LATEST_PAGE_1).getLatestReleases("Asia/Tokyo", page = 2)

        requests.single().assertApiGet("f" to "latest", "tz" to "Asia/Tokyo", "p" to "2")
        assertFalse(dto.isLimitReached)
        assertEquals(4, dto.releases.size)
    }

    @Test
    fun `getLatestReleases decodes limit_reached`() = runTest {
        val dto = service(SubsPleaseFixtures.LATEST_LIMIT_REACHED).getLatestReleases("Asia/Tokyo", page = 3)

        assertTrue(dto.isLimitReached)
    }

    @Test
    fun `searchReleases encodes the query and decodes the results`() = runTest {
        val dto = service(SubsPleaseFixtures.SEARCH_SHOW).searchReleases("Europe/London", "Sasaki to Pii-chan")

        val request = requests.single()
        request.assertApiGet("f" to "search", "tz" to "Europe/London", "s" to "Sasaki to Pii-chan")
        assertTrue(request.url.encodedQuery.endsWith("s=Sasaki+to+Pii-chan"), request.url.encodedQuery)
        assertEquals(5, dto.releases.size)
    }

    @Test
    fun `searchReleases sends an empty query as s=`() = runTest {
        val dto = service(SubsPleaseFixtures.SEARCH_NEWEST).searchReleases("America/New_York", "")

        val request = requests.single()
        request.assertApiGet("f" to "search", "tz" to "America/New_York", "s" to "")
        assertTrue(request.url.encodedQuery.endsWith("&s="), request.url.encodedQuery)
        assertEquals(4, dto.releases.size)
    }

    @Test
    fun `searchReleases decodes an empty array`() = runTest {
        val dto = service(SubsPleaseFixtures.SEARCH_NO_RESULTS).searchReleases("America/New_York", "zzz")

        assertTrue(dto.releases.isEmpty())
        assertFalse(dto.isLimitReached)
    }
}
