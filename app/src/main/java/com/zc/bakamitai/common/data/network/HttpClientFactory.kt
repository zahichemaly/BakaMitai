package com.zc.bakamitai.common.data.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.ANDROID
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/** SubsPlease, the app's only backend (docs/api.md). */
const val SUBSPLEASE_BASE_URL = "https://subsplease.org/"

private const val REQUEST_TIMEOUT_MILLIS = 15_000L
private const val CONNECT_TIMEOUT_MILLIS = 10_000L

/** The one JSON configuration for API DTOs and typed DataStore. */
val appJson = Json {
    ignoreUnknownKeys = true
    coerceInputValues = true
    explicitNulls = false
}

/**
 * The app-wide SubsPlease client (docs/api.md §7). Non-2xx responses throw (`expectSuccess`), which
 * `safeApiCall` maps to `DomainError`. SubsPlease serves JSON as `text/html`, so JSON is negotiated for any
 * content type.
 *
 * @param userAgent sent on every request, e.g. `BakaMitai/1.0 (Android)`.
 * @param engine OkHttp in the app; tests pass a `MockEngine`.
 * @param logLevel `LogLevel.NONE` in release builds.
 */
fun createHttpClient(
    json: Json,
    userAgent: String,
    engine: HttpClientEngine = OkHttp.create(),
    logLevel: LogLevel = LogLevel.NONE,
): HttpClient = HttpClient(engine) {
    expectSuccess = true
    install(ContentNegotiation) {
        json(json, contentType = ContentType.Any)
    }
    install(HttpTimeout) {
        requestTimeoutMillis = REQUEST_TIMEOUT_MILLIS
        connectTimeoutMillis = CONNECT_TIMEOUT_MILLIS
    }
    if (logLevel != LogLevel.NONE) {
        install(Logging) {
            logger = Logger.ANDROID
            level = logLevel
        }
    }
    defaultRequest {
        url(SUBSPLEASE_BASE_URL)
        headers.append(HttpHeaders.UserAgent, userAgent)
    }
}
