package com.zc.bakamitai

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import com.zc.bakamitai.data.network.repos.ScheduleRepository
import com.zc.bakamitai.di.reposModule
import com.zc.bakamitai.di.servicesModule
import com.zc.bakamitai.di.utilsModule
import com.zc.bakamitai.di.viewModelsModule
import com.zc.bakamitai.utils.PreferenceUtil
import com.zc.bakamitai.utils.Theme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import timber.log.Timber

class BakaApplication : Application() {
    private val scheduleRepository: ScheduleRepository by inject()
    private val applicationScope: CoroutineScope = MainScope()
    private val preferenceUtil: PreferenceUtil by inject()

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
        setTheme()
        applicationScope.launch(Dispatchers.IO) {
            scheduleRepository.fetchSchedules(false)
        }
    }

    private fun setTheme() {
        when (preferenceUtil.getTheme()) {
            Theme.Light -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            Theme.Dark -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            Theme.System -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }
}
