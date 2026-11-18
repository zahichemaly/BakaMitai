package com.zc.bakamitai.app.di

import com.zc.bakamitai.common.di.CommonModule
import com.zc.bakamitai.feature.home.di.HomeModule
import org.koin.core.annotation.Module

/** Every Koin module of the app: [CommonModule] and, as they are added, each feature's module. */
@Module(includes = [CommonModule::class, HomeModule::class])
class AppModule
