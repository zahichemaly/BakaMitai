package com.zc.bakamitai.app.di

import org.koin.core.annotation.KoinApplication

/** Koin entry point, started with `startKoin<AppKoin>` in `AppApplication`. */
@KoinApplication(modules = [AppModule::class])
object AppKoin
