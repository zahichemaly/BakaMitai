package com.zc.bakamitai.feature.settings.domain.usecase

import com.zc.bakamitai.common.domain.model.DomainError
import com.zc.bakamitai.common.domain.repository.FakeSettingsRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class UpdateNewEpisodeAlertsUseCaseTest {

    private val repository = FakeSettingsRepository()
    private val useCase = UpdateNewEpisodeAlertsUseCase(repository)

    @Test
    fun `saves the value`() = runTest {
        assertEquals(Result.success(Unit), useCase(false))
        assertEquals(false, repository.settings.value.newEpisodeAlerts)
    }

    @Test
    fun `a failed write is returned`() = runTest {
        repository.failWrites = true

        assertIs<DomainError.Unknown>(useCase(false).exceptionOrNull())
        assertEquals(listOf<Any>(), repository.writes)
    }
}
