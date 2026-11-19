package com.zc.bakamitai.feature.settings.di

import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module

/** Registers the annotated classes of the Settings feature (use cases, `AppInfoRepositoryImpl`, the ViewModel). */
@Module
@ComponentScan("com.zc.bakamitai.feature.settings")
class SettingsModule
