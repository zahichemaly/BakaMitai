package com.zc.bakamitai.app.di

import android.content.Context
import androidx.work.WorkerParameters
import com.zc.bakamitai.common.domain.usecase.GetTodayDateUseCase
import com.zc.bakamitai.common.domain.util.DefaultDispatcherProvider
import com.zc.bakamitai.common.domain.util.DispatcherProvider
import com.zc.bakamitai.common.domain.util.SystemTimeZoneProvider
import com.zc.bakamitai.common.domain.util.TimeZoneProvider
import com.zc.bakamitai.feature.details.data.repository.ShowDetailsRepositoryImpl
import com.zc.bakamitai.feature.details.domain.repository.ShowDetailsRepository
import com.zc.bakamitai.feature.details.domain.usecase.GetShowEpisodesUseCase
import com.zc.bakamitai.feature.details.domain.usecase.GetShowOverviewUseCase
import com.zc.bakamitai.feature.home.data.repository.BroadcastRepositoryImpl
import com.zc.bakamitai.feature.home.domain.repository.BroadcastRepository
import com.zc.bakamitai.feature.home.domain.usecase.GetTodayBroadcastsUseCase
import com.zc.bakamitai.feature.schedule.data.repository.ScheduleRepositoryImpl
import com.zc.bakamitai.feature.schedule.domain.repository.ScheduleRepository
import com.zc.bakamitai.feature.schedule.domain.usecase.GetWeeklyScheduleUseCase
import com.zc.bakamitai.feature.shows.data.repository.ShowCatalogRepositoryImpl
import com.zc.bakamitai.feature.shows.domain.repository.ShowCatalogRepository
import com.zc.bakamitai.feature.shows.domain.usecase.FilterShowIndexUseCase
import com.zc.bakamitai.feature.shows.domain.usecase.GetShowIndexUseCase
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
        // verify() checks the constructors of the provided types. Context and WorkerParameters come from Android
        // (androidContext, WorkManager's factory); the Ktor types are built by createHttpClient, not by Koin.
        AppModule().module().verify(
            extraTypes = listOf(
                Context::class,
                WorkerParameters::class,
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
            assertIs<ScheduleRepositoryImpl>(koin.get<ScheduleRepository>())
            koin.get<GetWeeklyScheduleUseCase>()
            assertIs<ShowCatalogRepositoryImpl>(koin.get<ShowCatalogRepository>())
            koin.get<GetShowIndexUseCase>()
            koin.get<FilterShowIndexUseCase>()
            assertIs<ShowDetailsRepositoryImpl>(koin.get<ShowDetailsRepository>())
            koin.get<GetShowOverviewUseCase>()
            koin.get<GetShowEpisodesUseCase>()
            // The bookmarks bindings need an Android Context: verify() above covers them.
            koin.get<Json>()
            koin.get<HttpClient>().close()
        } finally {
            koin.close()
        }
    }
}
