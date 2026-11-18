package com.zc.bakamitai.common.di

import com.zc.bakamitai.BuildConfig
import com.zc.bakamitai.common.data.network.appJson
import com.zc.bakamitai.common.data.network.createHttpClient
import io.ktor.client.HttpClient
import io.ktor.client.plugins.logging.LogLevel
import kotlinx.serialization.json.Json
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

/**
 * Shared bindings. `@ComponentScan` registers the annotated classes in `common`; the providers below are for
 * types we don't own.
 */
@Module
@ComponentScan("com.zc.bakamitai.common")
class CommonModule {

    @Single
    fun json(): Json = appJson

    @Single
    fun httpClient(json: Json): HttpClient = createHttpClient(
        json = json,
        userAgent = "BakaMitai/${BuildConfig.VERSION_NAME} (Android)",
        logLevel = if (BuildConfig.DEBUG) LogLevel.INFO else LogLevel.NONE,
    )
}
