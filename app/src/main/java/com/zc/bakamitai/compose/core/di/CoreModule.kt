package com.zc.bakamitai.compose.core.di

import com.zc.bakamitai.compose.core.domain.exceptionmanager.ExceptionManager
import com.zc.bakamitai.compose.core.domain.exceptionmanager.ExceptionManagerImpl
import com.zc.bakamitai.compose.core.network.AppClient
import com.zc.bakamitai.compose.core.network.HttpClientProvider
import io.ktor.client.HttpClient
import org.koin.dsl.module


val coreModule = module {
    single<HttpClient> { HttpClientProvider.getHttpClient() }
    single<AppClient> { AppClient(get(), get()) }
    single<ExceptionManager> { ExceptionManagerImpl() }
}
