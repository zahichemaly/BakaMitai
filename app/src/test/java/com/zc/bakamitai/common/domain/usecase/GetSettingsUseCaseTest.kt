package com.zc.bakamitai.common.domain.usecase

import com.zc.bakamitai.common.domain.model.AppSettings
import com.zc.bakamitai.common.domain.model.WeekStart
import com.zc.bakamitai.common.domain.repository.FakeSettingsRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class GetSettingsUseCaseTest {

    @Test
    fun `returns the current settings`() = runTest {
        val repository = FakeSettingsRepository(AppSettings(weekStart = WeekStart.SUNDAY))

        assertEquals(AppSettings(weekStart = WeekStart.SUNDAY), GetSettingsUseCase(repository)())
    }
}
