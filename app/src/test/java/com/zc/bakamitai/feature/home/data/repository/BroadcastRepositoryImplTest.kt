package com.zc.bakamitai.feature.home.data.repository

import com.zc.bakamitai.common.data.network.appJson
import com.zc.bakamitai.common.data.network.createHttpClient
import com.zc.bakamitai.common.domain.model.DomainError
import com.zc.bakamitai.common.util.SubsPleaseFixtures
import com.zc.bakamitai.feature.home.data.remote.BroadcastService
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpRequestData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BroadcastRepositoryImplTest {

    private val requests = mutableListOf<HttpRequestData>()

    private fun repository(body: String, status: HttpStatusCode = HttpStatusCode.OK) = BroadcastRepositoryImpl(
        BroadcastService(
            createHttpClient(
                json = appJson,
                userAgent = "test",
                engine = MockEngine { request ->
                    requests += request
                    respond(body, status, headersOf(HttpHeaders.ContentType, "text/html; charset=UTF-8"))
                },
            ),
        ),
    )

    @Test
    fun `the schedule maps to domain shows and sends the zone id`() = runTest {
        val result = repository(SubsPleaseFixtures.read(SubsPleaseFixtures.SCHEDULE_TODAY))
            .getTodaySchedule(TimeZone.of("America/New_York"))

        assertEquals("America/New_York", requests.single().url.parameters["tz"])
        assertEquals(
            listOf(LocalTime(8, 0), LocalTime(10, 30), LocalTime(12, 0), LocalTime(12, 15)),
            result.getOrThrow().map { it.time },
        )
    }

    @Test
    fun `latest releases map to domain releases`() = runTest {
        val result = repository(SubsPleaseFixtures.read(SubsPleaseFixtures.LATEST_PAGE_1))
            .getLatestReleases(page = 1, timeZone = TimeZone.of("Asia/Tokyo"))

        assertEquals("Asia/Tokyo", requests.single().url.parameters["tz"])
        assertEquals("1", requests.single().url.parameters["p"])
        assertEquals(listOf("06", "07", "07", "07"), result.getOrThrow().map { it.episode })
    }

    @Test
    fun `limit_reached is an empty success`() = runTest {
        val result = repository(SubsPleaseFixtures.read(SubsPleaseFixtures.LATEST_LIMIT_REACHED))
            .getLatestReleases(page = 3, timeZone = TimeZone.UTC)

        assertEquals(Result.success(emptyList()), result)
    }

    @Test
    fun `no search results is an empty success`() = runTest {
        val result = repository(SubsPleaseFixtures.read(SubsPleaseFixtures.SEARCH_NO_RESULTS))
            .searchReleases("zzz", TimeZone.UTC)

        assertTrue(result.getOrThrow().isEmpty())
    }

    @Test
    fun `a 500 maps to Server`() = runTest {
        val result = repository("oops", HttpStatusCode.InternalServerError).getTodaySchedule(TimeZone.UTC)

        assertEquals(DomainError.Server, result.exceptionOrNull())
    }

    @Test
    fun `an HTML page maps to InvalidData`() = runTest {
        val result = repository("<html><body>Maintenance</body></html>").searchReleases("", TimeZone.UTC)

        assertEquals(DomainError.InvalidData, result.exceptionOrNull())
    }

    @Test
    fun `an empty body maps to InvalidData`() = runTest {
        val repository = repository("")

        assertEquals(DomainError.InvalidData, repository.getLatestReleases(1, TimeZone.UTC).exceptionOrNull())
        assertEquals(DomainError.InvalidData, repository.getTodaySchedule(TimeZone.UTC).exceptionOrNull())
    }
}
