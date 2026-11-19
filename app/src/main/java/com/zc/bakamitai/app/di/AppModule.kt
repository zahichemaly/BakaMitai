package com.zc.bakamitai.app.di

import com.zc.bakamitai.common.di.CommonModule
import com.zc.bakamitai.feature.bookmarks.di.BookmarksModule
import com.zc.bakamitai.feature.details.di.DetailsModule
import com.zc.bakamitai.feature.home.di.HomeModule
import com.zc.bakamitai.feature.schedule.di.ScheduleModule
import com.zc.bakamitai.feature.settings.di.SettingsModule
import com.zc.bakamitai.feature.shows.di.ShowsModule
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module

/**
 * Every Koin module of the app: [CommonModule] and each feature's module. It also scans `app` itself, for the app
 * root's `AppViewModel`.
 */
@Module(
    includes = [
        CommonModule::class,
        HomeModule::class,
        ScheduleModule::class,
        ShowsModule::class,
        BookmarksModule::class,
        DetailsModule::class,
        SettingsModule::class,
    ],
)
@ComponentScan("com.zc.bakamitai.app")
class AppModule
