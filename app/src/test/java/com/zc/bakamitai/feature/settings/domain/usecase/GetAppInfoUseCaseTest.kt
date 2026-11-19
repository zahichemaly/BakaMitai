package com.zc.bakamitai.feature.settings.domain.usecase

import com.zc.bakamitai.feature.settings.domain.model.AppInfo
import com.zc.bakamitai.feature.settings.domain.repository.FakeAppInfoRepository
import kotlin.test.Test
import kotlin.test.assertEquals

class GetAppInfoUseCaseTest {

    @Test
    fun `returns the repository's app info`() {
        val appInfo = AppInfo(versionName = "2.1", sourceCodeUrl = "https://example.org/source")

        assertEquals(appInfo, GetAppInfoUseCase(FakeAppInfoRepository(appInfo))())
    }
}
