package com.zc.bakamitai.feature.settings.domain.repository

import com.zc.bakamitai.feature.settings.domain.model.AppInfo

/** The installed build's information. */
interface AppInfoRepository {
    fun getAppInfo(): AppInfo
}
