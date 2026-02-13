package com.zc.bakamitai.compose.core.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.logging.SIMPLE
import io.ktor.client.request.accept
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import java.util.TimeZone

object HttpClientProvider {
    fun getHttpClient(): HttpClient {
        return HttpClient(OkHttp) {
            install(ContentNegotiation) {
                val jsonConfig = Json {
                    prettyPrint = true
                    ignoreUnknownKeys = true
                    isLenient = true
                    explicitNulls = false
                }
                json(jsonConfig)
                json(jsonConfig, contentType = ContentType.Text.Html)
            }
            install(DefaultRequest) {
                url("https://subsplease.org/")
                url.parameters.append("tz", TimeZone.getDefault().id)
                contentType(ContentType.Application.Json)
                accept(ContentType.Application.Json)
            }
            install(Logging) {
                logger = Logger.SIMPLE
                level = LogLevel.ALL
            }
            install(HttpTimeout) {
                connectTimeoutMillis = 60000
                requestTimeoutMillis = 60000
                socketTimeoutMillis = 60000
            }
        }
    }
}
