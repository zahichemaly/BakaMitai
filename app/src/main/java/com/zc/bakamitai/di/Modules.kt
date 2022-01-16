package com.zc.bakamitai.di

import com.zc.bakamitai.data.network.Api
import com.zc.bakamitai.data.network.repos.SubsPleaseRepository
import com.zc.bakamitai.data.network.repos.impl.SubsPleaseRepositoryImpl
import com.zc.bakamitai.data.network.services.SubsPleaseService
import com.zc.bakamitai.ui.details.DetailsViewModel
import com.zc.bakamitai.ui.home.HomeViewModel
import com.zc.bakamitai.ui.schedule.ScheduleViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val servicesModule = module {
    factory<SubsPleaseService> { Api.retrofit.create(SubsPleaseService::class.java) }
}

val reposModule = module {
    factory<SubsPleaseRepository> { SubsPleaseRepositoryImpl(get()) }
}

val viewModelsModule = module {
    viewModel { DetailsViewModel(get()) }
    viewModel { HomeViewModel(get()) }
    viewModel { ScheduleViewModel(get()) }
}
