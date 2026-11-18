package com.zc.bakamitai.feature.schedule.data.repository

import com.zc.bakamitai.common.data.network.appJson
import com.zc.bakamitai.common.data.network.createHttpClient
import com.zc.bakamitai.common.domain.model.DomainError
import com.zc.bakamitai.common.util.FixedClock
import com.zc.bakamitai.common.util.SubsPleaseFixtures
import com.zc.bakamitai.feature.schedule.data.remote.ScheduleService
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpRequestData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

class ScheduleRepositoryImplTest {

    private val newYork = TimeZone.of("America/New_York")
    private val tokyo = TimeZone.of("Asia/Tokyo")
    private val clock = FixedClock(Instant.parse("2026-11-19T17:00:00Z"))
    private val requests = mutableListOf<HttpRequestData>()

    private var body: String = SubsPleaseFixtures.read(SubsPleaseFixtures.SCHEDULE_WEEK)
    private var status: HttpStatusCode = HttpStatusCode.OK

    private val repository = ScheduleRepositoryImpl(
        service = ScheduleService(
            createHttpClient(
                json = appJson,
                userAgent = "test",
                engine = MockEngine { request ->
                    requests += request
                    respond(body, status, headersOf(HttpHeaders.ContentType, "text/html; charset=UTF-8"))
                },
            ),
        ),
        clock = clock,
    )

    private val weeklyRequests get() = requests.filter { it.url.parameters["h"] == null }

    @Test
    fun `the week maps to domain shows and sends the zone id`() = runTest {
        val week = repository.getWeeklySchedule(newYork, forceRefresh = false).getOrThrow()

        assertEquals("America/New_York", requests.single().url.parameters["tz"])
        assertEquals(DayOfWeek.entries.toSet(), week.keys)
        assertEquals(listOf(LocalTime(9, 30)), week.getValue(DayOfWeek.MONDAY).map { it.time })
    }

    @Test
    fun `a second call within an hour is served from the cache`() = runTest {
        val first = repository.getWeeklySchedule(newYork, forceRefresh = false)
        clock.instant += 59.minutes

        val second = repository.getWeeklySchedule(newYork, forceRefresh = false)

        assertEquals(1, weeklyRequests.size)
        assertEquals(first, second)
    }

    @Test
    fun `a call after an hour requests again`() = runTest {
        repository.getWeeklySchedule(newYork, forceRefresh = false)
        clock.instant += 60.minutes

        repository.getWeeklySchedule(newYork, forceRefresh = false)
        repository.getWeeklySchedule(newYork, forceRefresh = false)

        assertEquals(2, weeklyRequests.size)
    }

    @Test
    fun `forceRefresh bypasses the cache and refills it`() = runTest {
        repository.getWeeklySchedule(newYork, forceRefresh = false)

        repository.getWeeklySchedule(newYork, forceRefresh = true)
        repository.getWeeklySchedule(newYork, forceRefresh = false)

        assertEquals(2, weeklyRequests.size)
    }

    @Test
    fun `another zone misses the cache`() = runTest {
        repository.getWeeklySchedule(newYork, forceRefresh = false)

        repository.getWeeklySchedule(tokyo, forceRefresh = false)

        assertEquals(listOf("America/New_York", "Asia/Tokyo"), weeklyRequests.map { it.url.parameters["tz"] })
    }

    @Test
    fun `a failure isn't cached`() = runTest {
        status = HttpStatusCode.InternalServerError
        body = "oops"
        assertEquals(DomainError.Server, repository.getWeeklySchedule(newYork, forceRefresh = false).exceptionOrNull())

        status = HttpStatusCode.OK
        body = SubsPleaseFixtures.read(SubsPleaseFixtures.SCHEDULE_WEEK)
        val retried = repository.getWeeklySchedule(newYork, forceRefresh = false)

        assertEquals(2, weeklyRequests.size)
        assertEquals(DayOfWeek.entries.toSet(), retried.getOrThrow().keys)
    }

    @Test
    fun `concurrent calls share one request`() = runTest {
        val results = List(2) { async { repository.getWeeklySchedule(newYork, forceRefresh = false) } }.awaitAll()

        assertEquals(1, weeklyRequests.size)
        assertEquals(results[0], results[1])
    }

    @Test
    fun `an empty body maps to InvalidData`() = runTest {
        body = ""

        assertEquals(
            DomainError.InvalidData,
            repository.getWeeklySchedule(newYork, forceRefresh = false).exceptionOrNull(),
        )
    }

    @Test
    fun `today's schedule maps aired, sends h=true and is never cached`() = runTest {
        body = SubsPleaseFixtures.read(SubsPleaseFixtures.SCHEDULE_TODAY)

        val first = repository.getTodaySchedule(newYork).getOrThrow()
        repository.getTodaySchedule(newYork)

        assertEquals(2, requests.size)
        assertEquals(listOf("true", "true"), requests.map { it.url.parameters["h"] })
        assertEquals("America/New_York", requests.first().url.parameters["tz"])
        assertEquals(listOf(true, true, false, false), first.map { it.aired })
    }

    @Test
    fun `today's schedule maps a 500 to Server`() = runTest {
        status = HttpStatusCode.InternalServerError
        body = "oops"

        assertEquals(DomainError.Server, repository.getTodaySchedule(newYork).exceptionOrNull())
    }
}
