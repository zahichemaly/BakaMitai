package com.zc.bakamitai.di

import androidx.room.Room
import com.zc.bakamitai.data.network.Api
import com.zc.bakamitai.data.network.repos.BookmarksRepository
import com.zc.bakamitai.data.network.repos.ScheduleRepository
import com.zc.bakamitai.data.network.repos.SubsPleaseRepository
import com.zc.bakamitai.data.network.repos.impl.BookmarksRepositoryImpl
import com.zc.bakamitai.data.network.repos.impl.ScheduleRepositoryImpl
import com.zc.bakamitai.data.network.repos.impl.SubsPleaseRepositoryImpl
import com.zc.bakamitai.data.network.services.SubsPleaseService
import com.zc.bakamitai.data.room.AppDatabase
import com.zc.bakamitai.ui.bookmarks.BookmarksViewModel
import com.zc.bakamitai.ui.details.DetailsViewModel
import com.zc.bakamitai.ui.home.HomeViewModel
import com.zc.bakamitai.ui.library.LibraryViewModel
import com.zc.bakamitai.ui.schedule.ScheduleViewModel
import com.zc.bakamitai.utils.PreferenceUtil
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
    factory { get<AppDatabase>().scheduleDao() }
    single<BookmarksRepository> { BookmarksRepositoryImpl(get()) }
    single<ScheduleRepository> { ScheduleRepositoryImpl(get(), get(), get()) }
}

val viewModelsModule = module {
    viewModel { DetailsViewModel(get(), get(), get()) }
    viewModel { HomeViewModel(get()) }
    viewModel { ScheduleViewModel(get()) }
    viewModel { BookmarksViewModel(get()) }
    viewModel { LibraryViewModel(get()) }
}

val utilsModule = module {
    single { PreferenceUtil(get()) }
}
