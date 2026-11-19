package com.zc.bakamitai.common.domain.usecase

import app.cash.turbine.test
import com.zc.bakamitai.common.domain.model.AppSettings
import com.zc.bakamitai.common.domain.model.ThemeMode
import com.zc.bakamitai.common.domain.repository.FakeSettingsRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ObserveSettingsUseCaseTest {

    @Test
    fun `emits the repository's settings and their changes`() = runTest {
        val repository = FakeSettingsRepository()

        ObserveSettingsUseCase(repository)().test {
            assertEquals(AppSettings(), awaitItem())
            repository.settings.value = AppSettings(themeMode = ThemeMode.DARK)
            assertEquals(AppSettings(themeMode = ThemeMode.DARK), awaitItem())
        }
    }
}
