package com.zc.bakamitai.feature.schedule.data.remote

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

class ScheduleServiceTest {

    private val requests = mutableListOf<HttpRequestData>()

    private fun service(fixture: String) = ScheduleService(
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
    fun `getWeeklySchedule requests the week in the zone, without h, and decodes it`() = runTest {
        val dto = service(SubsPleaseFixtures.SCHEDULE_WEEK).getWeeklySchedule("America/New_York")

        requests.single().assertApiGet("f" to "schedule", "tz" to "America/New_York")
        assertEquals(7, dto.days.size)
        assertEquals("psyren", dto.days.getValue("Monday").single().page)
    }

    @Test
    fun `getTodaySchedule requests today's schedule in the zone and decodes it`() = runTest {
        val dto = service(SubsPleaseFixtures.SCHEDULE_TODAY).getTodaySchedule("Asia/Tokyo")

        requests.single().assertApiGet("f" to "schedule", "h" to "true", "tz" to "Asia/Tokyo")
        assertEquals(4, dto.schedule?.size)
    }
}
