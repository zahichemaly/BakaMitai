package com.zc.bakamitai.feature.settings.domain.usecase

import com.zc.bakamitai.common.domain.model.DomainError
import com.zc.bakamitai.common.domain.model.ThemeMode
import com.zc.bakamitai.common.domain.repository.FakeSettingsRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class UpdateThemeModeUseCaseTest {

    private val repository = FakeSettingsRepository()
    private val useCase = UpdateThemeModeUseCase(repository)

    @Test
    fun `saves the value`() = runTest {
        assertEquals(Result.success(Unit), useCase(ThemeMode.DARK))
        assertEquals(ThemeMode.DARK, repository.settings.value.themeMode)
    }

    @Test
    fun `a failed write is returned`() = runTest {
        repository.failWrites = true

        assertIs<DomainError.Unknown>(useCase(ThemeMode.DARK).exceptionOrNull())
        assertEquals(listOf<Any>(), repository.writes)
    }
}
