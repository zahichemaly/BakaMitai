package com.zc.bakamitai.compose.features.home.di

import com.zc.bakamitai.compose.features.home.data.remote.ReleaseDataSource
import com.zc.bakamitai.compose.features.home.data.remote.ReleaseDataSourceImpl
import com.zc.bakamitai.compose.features.home.data.repository.ReleaseRepositoryImpl
import com.zc.bakamitai.compose.features.home.domain.repository.ReleaseRepository
import com.zc.bakamitai.compose.features.home.presentation.HomeViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val homeModule = module {
    factory<ReleaseDataSource> { ReleaseDataSourceImpl(get()) }
    factory<ReleaseRepository> { ReleaseRepositoryImpl(get()) }
    viewModel { HomeViewModel(get()) }
}
