package com.zc.bakamitai.common.data.util

import com.zc.bakamitai.common.data.network.appJson
import com.zc.bakamitai.common.data.network.createHttpClient
import com.zc.bakamitai.common.domain.model.DomainError
import io.ktor.client.call.body
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.request.get
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.io.IOException
import java.net.UnknownHostException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs

class SafeApiCallTest {

    @Serializable
    private data class SampleDTO(@SerialName("page") val page: String)

    private suspend fun call(status: HttpStatusCode, body: String = """{"page":"frieren"}"""): Result<SampleDTO> {
        val client = createHttpClient(
            json = appJson,
            userAgent = "test",
            engine = MockEngine {
                respond(body, status, headersOf(HttpHeaders.ContentType, "text/html; charset=UTF-8"))
            },
        )
        return safeApiCall { client.get("api/").body<SampleDTO>() }
    }

    @Test
    fun `success is wrapped`() = runTest {
        assertEquals(Result.success(SampleDTO("frieren")), call(HttpStatusCode.OK))
    }

    @Test
    fun `404 maps to NotFound`() = runTest {
        assertEquals(DomainError.NotFound, call(HttpStatusCode.NotFound).exceptionOrNull())
    }

    @Test
    fun `401 and 403 map to Unauthorized`() = runTest {
        assertEquals(DomainError.Unauthorized, call(HttpStatusCode.Unauthorized).exceptionOrNull())
        assertEquals(DomainError.Unauthorized, call(HttpStatusCode.Forbidden).exceptionOrNull())
    }

    @Test
    fun `429 maps to RateLimited`() = runTest {
        assertEquals(DomainError.RateLimited, call(HttpStatusCode.TooManyRequests).exceptionOrNull())
    }

    @Test
    fun `5xx maps to Server`() = runTest {
        assertEquals(DomainError.Server, call(HttpStatusCode.InternalServerError).exceptionOrNull())
        assertEquals(DomainError.Server, call(HttpStatusCode.ServiceUnavailable).exceptionOrNull())
    }

    @Test
    fun `other client errors map to Unknown`() = runTest {
        assertIs<DomainError.Unknown>(call(HttpStatusCode.Conflict).exceptionOrNull())
    }

    @Test
    fun `malformed JSON maps to InvalidData`() = runTest {
        assertEquals(DomainError.InvalidData, call(HttpStatusCode.OK, body = "<html>").exceptionOrNull())
    }

    @Test
    fun `missing required field maps to InvalidData`() = runTest {
        assertEquals(DomainError.InvalidData, call(HttpStatusCode.OK, body = "{}").exceptionOrNull())
    }

    @Test
    fun `timeout maps to Timeout`() = runTest {
        val result = safeApiCall<Unit> { throw HttpRequestTimeoutException("https://subsplease.org/api/", 15_000) }

        assertEquals(DomainError.Timeout, result.exceptionOrNull())
    }

    @Test
    fun `IO failures map to Network`() = runTest {
        assertEquals(DomainError.Network, safeApiCall<Unit> { throw IOException("reset") }.exceptionOrNull())
        assertEquals(DomainError.Network, safeApiCall<Unit> { throw UnknownHostException() }.exceptionOrNull())
    }

    @Test
    fun `a DomainError thrown by the block passes through`() = runTest {
        assertEquals(DomainError.InvalidData, safeApiCall<Unit> { throw DomainError.InvalidData }.exceptionOrNull())
    }

    @Test
    fun `cancellation is rethrown`() = runTest {
        assertFailsWith<CancellationException> {
            safeApiCall<Unit> { throw CancellationException("cancelled") }
        }
    }
}
