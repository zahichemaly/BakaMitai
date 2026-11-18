package com.zc.bakamitai.app.di

import android.content.Context
import com.zc.bakamitai.common.domain.util.DefaultDispatcherProvider
import com.zc.bakamitai.common.domain.util.DispatcherProvider
import com.zc.bakamitai.common.domain.util.SystemTimeZoneProvider
import com.zc.bakamitai.common.domain.util.TimeZoneProvider
import com.zc.bakamitai.feature.home.data.repository.BroadcastRepositoryImpl
import com.zc.bakamitai.feature.home.domain.repository.BroadcastRepository
import com.zc.bakamitai.feature.home.domain.usecase.GetTodayBroadcastsUseCase
import com.zc.bakamitai.feature.home.domain.usecase.GetTodayDateUseCase
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngine
import kotlinx.serialization.json.Json
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.plugin.module.dsl.koinApplication
import org.koin.test.verify.verify
import kotlin.test.Test
import kotlin.test.assertIs
import kotlin.test.assertSame
import kotlin.time.Clock

class KoinModulesTest {

    @OptIn(KoinExperimentalAPI::class)
    @Test
    fun `all modules resolve`() {
        // verify() checks the constructors of the provided types. The ones below are built by provider
        // function in CommonModule (createHttpClient), not by Koin.
        AppModule().module().verify(
            extraTypes = listOf(
                Context::class,
                HttpClientEngine::class,
                HttpClientConfig::class,
            ),
        )
    }

    @Test
    fun `common bindings are created`() {
        val koin = koinApplication<AppKoin>().koin
        try {
            assertIs<DefaultDispatcherProvider>(koin.get<DispatcherProvider>())
            assertIs<SystemTimeZoneProvider>(koin.get<TimeZoneProvider>())
            assertSame(Clock.System, koin.get<Clock>())
            assertIs<BroadcastRepositoryImpl>(koin.get<BroadcastRepository>())
            koin.get<GetTodayBroadcastsUseCase>()
            koin.get<GetTodayDateUseCase>()
            koin.get<Json>()
            koin.get<HttpClient>().close()
        } finally {
            koin.close()
        }
    }
}
