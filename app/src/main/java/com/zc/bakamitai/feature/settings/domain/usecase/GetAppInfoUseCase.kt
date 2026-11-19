package com.zc.bakamitai.feature.settings.domain.usecase

import com.zc.bakamitai.feature.settings.domain.model.AppInfo
import com.zc.bakamitai.feature.settings.domain.repository.AppInfoRepository
import org.koin.core.annotation.Factory

/** The app's version and source code URL, for the About row and the footer. */
@Factory
class GetAppInfoUseCase(private val repository: AppInfoRepository) {
    operator fun invoke(): AppInfo = repository.getAppInfo()
}
