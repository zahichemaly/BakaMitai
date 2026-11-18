package com.zc.bakamitai.common.domain.model

/**
 * Every failure that reaches `domain` and `presentation`. `data` maps exceptions to it in `safeApiCall`;
 * `presentation` maps it to an `ErrorUIModel`. Pure Kotlin: no HTTP or Ktor types.
 */
sealed class DomainError(cause: Throwable? = null) : Exception(cause) {

    /** No connection, DNS failure, connection reset. */
    data object Network : DomainError()

    /** The request or the connection timed out. */
    data object Timeout : DomainError()

    /** The server refused the request (401 / 403). */
    data object Unauthorized : DomainError()

    /** The requested resource doesn't exist (404). */
    data object NotFound : DomainError()

    /** Too many requests (429). */
    data object RateLimited : DomainError()

    /** The server failed (5xx). */
    data object Server : DomainError()

    /** The response couldn't be decoded or parsed (malformed JSON, missing HTML element). */
    data object InvalidData : DomainError()

    /** Anything else, such as an unexpected HTTP status. */
    data class Unknown(override val cause: Throwable) : DomainError(cause)
}
