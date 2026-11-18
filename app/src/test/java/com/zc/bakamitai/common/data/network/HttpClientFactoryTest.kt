package com.zc.bakamitai.common.data.network

import io.ktor.client.call.body
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.get
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class HttpClientFactoryTest {

    @Serializable
    private data class SampleDTO(@SerialName("page") val page: String, @SerialName("episode") val episode: String?)

    private fun client(
        status: HttpStatusCode = HttpStatusCode.OK,
        body: String = "{}",
        onRequest: (HttpRequestData) -> Unit = {},
    ) = createHttpClient(
        json = appJson,
        userAgent = USER_AGENT,
        engine = MockEngine { request ->
            onRequest(request)
            respond(
                content = body,
                status = status,
                headers = headersOf(HttpHeaders.ContentType, "text/html; charset=UTF-8"),
            )
        },
    )

    @Test
    fun `relative paths resolve against SubsPlease and carry the user agent`() = runTest {
        var request: HttpRequestData? = null
        client(onRequest = { request = it }).get("api/?f=schedule")

        assertEquals("https://subsplease.org/api/?f=schedule", request?.url.toString())
        assertEquals(USER_AGENT, request?.headers?.get(HttpHeaders.UserAgent))
    }

    @Test
    fun `decodes JSON served as text html, ignoring unknown keys`() = runTest {
        val dto = client(body = """{"page":"frieren","extra":1}""").get("api/").body<SampleDTO>()

        assertEquals("frieren", dto.page)
        assertNull(dto.episode)
    }

    @Test
    fun `decodes JSON served as application json`() = runTest {
        val engine = MockEngine {
            respond(
                content = """{"page":"frieren","episode":"12"}""",
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
            )
        }
        val dto = createHttpClient(appJson, USER_AGENT, engine).get("api/").body<SampleDTO>()

        assertEquals("12", dto.episode)
    }

    @Test
    fun `non-2xx responses throw`() = runTest {
        assertFailsWith<ClientRequestException> {
            client(status = HttpStatusCode.NotFound).get("shows/missing/")
        }
    }

    private companion object {
        const val USER_AGENT = "BakaMitai/test (Android)"
    }
}
