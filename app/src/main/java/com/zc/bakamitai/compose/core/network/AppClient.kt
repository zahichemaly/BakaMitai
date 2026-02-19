package com.zc.bakamitai.compose.core.network

import com.zc.bakamitai.compose.core.domain.exceptionmanager.ExceptionManager
import io.ktor.client.HttpClient
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.http.HttpMethod

class AppClient(
    val httpClient: HttpClient,
    val exceptionManager: ExceptionManager,
) {

    suspend inline fun <reified T> get(
        urlString: String, block: HttpRequestBuilder.() -> Unit = { }
    ): T {
        return execute(HttpMethod.Get, urlString, block)
    }
}
