package com.zc.bakamitai.compose.core.network

import io.ktor.client.HttpClient
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.http.HttpMethod

class HttpClientWrapper(val httpClient: HttpClient) {

    suspend inline fun <reified T> get(
        urlString: String, block: HttpRequestBuilder.() -> Unit = { }
    ): GenericResponse<T> {
        return execute(HttpMethod.Get, urlString, block)
    }
}
