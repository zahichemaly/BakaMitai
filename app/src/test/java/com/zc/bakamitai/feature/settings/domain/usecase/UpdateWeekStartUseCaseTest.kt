package com.zc.bakamitai.feature.settings.domain.usecase

import com.zc.bakamitai.common.domain.model.DomainError
import com.zc.bakamitai.common.domain.model.WeekStart
import com.zc.bakamitai.common.domain.repository.FakeSettingsRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class UpdateWeekStartUseCaseTest {

    private val repository = FakeSettingsRepository()
    private val useCase = UpdateWeekStartUseCase(repository)

    @Test
    fun `saves the value`() = runTest {
        assertEquals(Result.success(Unit), useCase(WeekStart.SUNDAY))
        assertEquals(WeekStart.SUNDAY, repository.settings.value.weekStart)
    }

    @Test
    fun `a failed write is returned`() = runTest {
        repository.failWrites = true

        assertIs<DomainError.Unknown>(useCase(WeekStart.SUNDAY).exceptionOrNull())
        assertEquals(listOf<Any>(), repository.writes)
    }
}
