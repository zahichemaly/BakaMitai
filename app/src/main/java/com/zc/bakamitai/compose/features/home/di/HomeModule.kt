package com.zc.bakamitai.compose.features.home.di

import com.zc.bakamitai.compose.core.network.HttpClientProvider
import com.zc.bakamitai.compose.core.network.HttpClientWrapper
import com.zc.bakamitai.compose.features.home.data.remote.ReleaseDataSource
import com.zc.bakamitai.compose.features.home.data.remote.ReleaseDataSourceImpl
import com.zc.bakamitai.compose.features.home.data.repository.ReleaseRepositoryImpl
import com.zc.bakamitai.compose.features.home.domain.repository.ReleaseRepository
import com.zc.bakamitai.compose.features.home.presentation.HomeViewModel
import io.ktor.client.HttpClient
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

//TODO move to core
private val networkModule = module {
    single<HttpClient> { HttpClientProvider.getHttpClient() }
    single<HttpClientWrapper> { HttpClientWrapper(get()) }
}

private val infraModule = module {
    factory<ReleaseDataSource> { ReleaseDataSourceImpl(get()) }
    factory<ReleaseRepository> { ReleaseRepositoryImpl(get()) }
    viewModel { HomeViewModel(get()) }
}



val homeModule = module {
    includes(networkModule)
    includes(infraModule)
}
