package com.zc.bakamitai.feature.settings.data.repository

import com.zc.bakamitai.BuildConfig
import com.zc.bakamitai.feature.settings.domain.model.AppInfo
import com.zc.bakamitai.feature.settings.domain.repository.AppInfoRepository
import org.koin.core.annotation.Single

/** The installed build's version name, and the project's repository (context.md §1). */
@Single
class AppInfoRepositoryImpl : AppInfoRepository {

    override fun getAppInfo(): AppInfo =
        AppInfo(versionName = BuildConfig.VERSION_NAME, sourceCodeUrl = SOURCE_CODE_URL)

    companion object {
        const val SOURCE_CODE_URL = "https://github.com/zahichemaly/BakaMitai"
    }
}
