package com.zc.bakamitai.compose.core.network

import com.zc.bakamitai.compose.common.UiText
import com.zc.bakamitai.compose.core.domain.exception.ServerException
import io.ktor.client.call.body
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.accept
import io.ktor.client.request.request
import io.ktor.client.request.url
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.contentType
import kotlin.coroutines.cancellation.CancellationException

suspend inline fun <reified T> AppClient.execute(
    method: HttpMethod,
    urlString: String,
    block: HttpRequestBuilder.() -> Unit
): T {
    val builder =
        HttpRequestBuilder().apply {
            url(urlString)
            contentType(ContentType.Application.Json)
            accept(ContentType.Application.Json)
            block()
            this.method = method
        }
    return getResponse(builder = builder)
}

suspend inline fun <reified T> AppClient.getResponse(
    builder: HttpRequestBuilder
): T {
    runCatchingNetwork {
        httpClient.request(builder)
    }.fold(
        onSuccess = {
            if (it.status.value in NetworkResponse.successCodeRange) {
                val successResponse = it.body<T>()
                return successResponse
            } else {
                val errorCode = it.status.value
                //TODO this is temporary
                throw exceptionManager.transform(ServerException(UiText.StringValue(errorCode.toString())))
            }
        },
        onFailure = {
            throw exceptionManager.transform(it)
        }
    )
}

inline fun <T, R> T.runCatchingNetwork(block: T.() -> R): Result<R> {
    return try {
        Result.success(block())
    } catch (e: CancellationException) {
        throw e // Let coroutines handle their own cancellation
    } catch (e: Throwable) {
        Result.failure(e)
    }
}
