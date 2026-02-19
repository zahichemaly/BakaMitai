package com.zc.bakamitai.compose.features.details.di

import com.zc.bakamitai.compose.features.details.data.remote.DetailsDataSource
import com.zc.bakamitai.compose.features.details.data.remote.DetailsDataSourceImpl
import com.zc.bakamitai.compose.features.details.data.remote.EpisodeDataSource
import com.zc.bakamitai.compose.features.details.data.remote.EpisodeDataSourceImpl
import com.zc.bakamitai.compose.features.details.data.repository.ShowDetailsRepositoryImpl
import com.zc.bakamitai.compose.features.details.domain.repository.ShowDetailsRepository
import com.zc.bakamitai.compose.features.details.presentation.DetailsViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val detailsModule = module {
    factory<EpisodeDataSource> { EpisodeDataSourceImpl(get()) }
    factory<DetailsDataSource> { DetailsDataSourceImpl(get()) }
    factory<ShowDetailsRepository> { ShowDetailsRepositoryImpl(get(), get()) }
    viewModel { DetailsViewModel(get()) }
}
