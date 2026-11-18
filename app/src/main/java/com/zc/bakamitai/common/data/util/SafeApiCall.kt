package com.zc.bakamitai.common.data.util

import com.zc.bakamitai.common.domain.model.DomainError
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.ResponseException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.ContentConvertException
import kotlinx.coroutines.CancellationException
import kotlinx.io.IOException
import kotlinx.serialization.SerializationException

/**
 * Runs a service call and maps its failure to a [DomainError], the only place that knows Ktor and
 * serialization exceptions. Every repository call to a service goes through it. Cancellation is rethrown;
 * a [DomainError] thrown by [block] (e.g. `InvalidData` from a parser) passes through unchanged.
 */
@Suppress("SwallowedException") // Each catch deliberately turns the exception into a DomainError.
suspend fun <T> safeApiCall(block: suspend () -> T): Result<T> =
    try {
        Result.success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: DomainError) {
        Result.failure(e)
    } catch (e: HttpRequestTimeoutException) {
        Result.failure(DomainError.Timeout)
    } catch (e: ConnectTimeoutException) {
        Result.failure(DomainError.Timeout)
    } catch (e: SocketTimeoutException) {
        Result.failure(DomainError.Timeout)
    } catch (e: ClientRequestException) {
        Result.failure(e.response.status.toDomainError())
    } catch (e: ServerResponseException) {
        Result.failure(DomainError.Server)
    } catch (e: ResponseException) {
        Result.failure(DomainError.Unknown(e))
    } catch (e: ContentConvertException) {
        Result.failure(DomainError.InvalidData)
    } catch (e: SerializationException) {
        Result.failure(DomainError.InvalidData)
    } catch (e: IOException) {
        Result.failure(DomainError.Network)
    }

private fun HttpStatusCode.toDomainError(): DomainError = when (this) {
    HttpStatusCode.Unauthorized, HttpStatusCode.Forbidden -> DomainError.Unauthorized
    HttpStatusCode.NotFound -> DomainError.NotFound
    HttpStatusCode.TooManyRequests -> DomainError.RateLimited
    else -> DomainError.Unknown(IllegalStateException("HTTP $value"))
}
