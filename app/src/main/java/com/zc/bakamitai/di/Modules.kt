package com.zc.bakamitai.di

import androidx.room.Room
import com.zc.bakamitai.data.network.Api
import com.zc.bakamitai.data.network.repos.BookmarksRepository
import com.zc.bakamitai.data.network.repos.SubsPleaseRepository
import com.zc.bakamitai.data.network.repos.impl.BookmarksRepositoryImpl
import com.zc.bakamitai.data.network.repos.impl.SubsPleaseRepositoryImpl
import com.zc.bakamitai.data.network.services.SubsPleaseService
import com.zc.bakamitai.data.room.AppDatabase
import com.zc.bakamitai.prefs.PreferenceUtil
import com.zc.bakamitai.ui.bookmarks.BookmarksViewModel
import com.zc.bakamitai.ui.details.DetailsViewModel
import com.zc.bakamitai.ui.home.HomeViewModel
import com.zc.bakamitai.ui.schedule.ScheduleViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val servicesModule = module {
    factory<SubsPleaseService> { Api.retrofit.create(SubsPleaseService::class.java) }
}

val reposModule = module {
    factory<SubsPleaseRepository> { SubsPleaseRepositoryImpl(get()) }
    single {
        Room.databaseBuilder(androidApplication(), AppDatabase::class.java, "baka-db")
            .fallbackToDestructiveMigration()
            .build()
    }
    factory { get<AppDatabase>().bookmarkDao() }
    single<BookmarksRepository> { BookmarksRepositoryImpl(get()) }
}

val viewModelsModule = module {
    viewModel { DetailsViewModel(get(), get()) }
    viewModel { HomeViewModel(get(), get()) }
    viewModel { ScheduleViewModel(get(), get()) }
    viewModel { BookmarksViewModel(get()) }
}

val utilsModule = module {
    single { PreferenceUtil(get()) }
}
