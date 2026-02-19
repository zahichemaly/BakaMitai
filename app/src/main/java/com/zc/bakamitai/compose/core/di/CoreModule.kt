package com.zc.bakamitai.compose.core.di

import com.zc.bakamitai.compose.core.network.HttpClientProvider
import com.zc.bakamitai.compose.core.network.HttpClientWrapper
import io.ktor.client.HttpClient
import org.koin.dsl.module


val coreModule = module {
    single<HttpClient> { HttpClientProvider.getHttpClient() }
    single<HttpClientWrapper> { HttpClientWrapper(get()) }
}
