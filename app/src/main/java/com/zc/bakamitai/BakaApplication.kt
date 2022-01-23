package com.zc.bakamitai

import android.app.Application
import android.content.Context
import com.zc.bakamitai.data.network.repos.ScheduleRepository
import com.zc.bakamitai.di.reposModule
import com.zc.bakamitai.di.servicesModule
import com.zc.bakamitai.di.utilsModule
import com.zc.bakamitai.di.viewModelsModule
import kotlinx.coroutines.*
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import timber.log.Timber
import java.util.*

class BakaApplication : Application() {
    private val scheduleRepository: ScheduleRepository by inject()
    private val applicationScope: CoroutineScope = MainScope()

    companion object {
        private var appContext: Context? = null

        fun getContext(): Context {
            return appContext!!
        }
    }

    override fun onCreate() {
        super.onCreate()
        appContext = this
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        startKoin {
            //androidLogger()
            androidContext(this@BakaApplication)
            modules(servicesModule, reposModule, viewModelsModule, utilsModule)
        }
        applicationScope.launch(Dispatchers.IO) {
            scheduleRepository.fetchSchedules(false)
        }
    }
}
