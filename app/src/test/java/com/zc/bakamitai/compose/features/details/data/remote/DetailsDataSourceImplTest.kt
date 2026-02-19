package com.zc.bakamitai.compose.features.details.data.remote

import com.zc.bakamitai.compose.core.network.HttpClientWrapper
import com.zc.bakamitai.compose.core.network.NetworkResponse
import com.zc.bakamitai.compose.core.network.converters.registerJsoupHtml
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DetailsDataSourceImplTest {

    @Test
    fun `getPage returns success with document when response is 200`() = runTest {
        // Arrange
        val pageSlug = "29-sai-dokushin-chuuken-boukensha-no-nichijou"
        val mockHtml = """
            <html>
                <body>
                    <h1 class="entry-title">29-sai Dokushin Chuuken Boukensha no Nichijou</h1>
                    <div class="series-syn">
                        <p>Synopsis for 29-sai Dokushin Chuuken Boukensha no Nichijou.</p>
                    </div>
                    <img class="img-responsive img-center" src="https://subsplease.org/img/29-sai.jpg">
                    <table sid="12345"></table>
                </body>
            </html>
        """.trimIndent()

        val mockEngine = MockEngine { request ->
            if (request.url.encodedPath == "/page/$pageSlug") {
                respond(
                    content = mockHtml,
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, ContentType.Text.Html.toString())
                )
            } else {
                respond(
                    content = "Not Found",
                    status = HttpStatusCode.NotFound
                )
            }
        }

        val httpClient = HttpClient(mockEngine) {
            install(ContentNegotiation) {
                registerJsoupHtml("https://subsplease.org")
            }
        }
        val httpClientWrapper = HttpClientWrapper(httpClient)
        val dataSource = DetailsDataSourceImpl(httpClientWrapper)

        // Act
        val response = dataSource.getDetails(pageSlug)

        // Assert
        assertTrue(response is NetworkResponse.Success)
        val document = (response as NetworkResponse.Success).data
        assertEquals("29-sai Dokushin Chuuken Boukensha no Nichijou", document.selectFirst("h1.entry-title")?.text())
        assertEquals("12345", document.selectFirst("table[sid]")?.attr("sid"))
    }

    @Test
    fun `getPage returns failure when response is 404`() = runTest {
        // Arrange
        val pageSlug = "unknown-page"
        val mockEngine = MockEngine { _ ->
            respond(
                content = "Not Found",
                status = HttpStatusCode.NotFound
            )
        }

        val httpClient = HttpClient(mockEngine) {
            install(ContentNegotiation) {
                registerJsoupHtml("https://subsplease.org")
            }
        }
        val httpClientWrapper = HttpClientWrapper(httpClient)
        val dataSource = DetailsDataSourceImpl(httpClientWrapper)

        // Act
        val response = dataSource.getDetails(pageSlug)

        // Assert
        assertTrue(response is NetworkResponse.Failure)
        assertEquals(404, (response as NetworkResponse.Failure).error?.code)
    }
}
