package com.zc.bakamitai.feature.settings.domain.usecase

import com.zc.bakamitai.common.domain.model.DomainError
import com.zc.bakamitai.common.domain.model.TimeFormat
import com.zc.bakamitai.common.domain.repository.FakeSettingsRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class UpdateTimeFormatUseCaseTest {

    private val repository = FakeSettingsRepository()
    private val useCase = UpdateTimeFormatUseCase(repository)

    @Test
    fun `saves the value`() = runTest {
        assertEquals(Result.success(Unit), useCase(TimeFormat.TWENTY_FOUR_HOUR))
        assertEquals(TimeFormat.TWENTY_FOUR_HOUR, repository.settings.value.timeFormat)
    }

    @Test
    fun `a failed write is returned`() = runTest {
        repository.failWrites = true

        assertIs<DomainError.Unknown>(useCase(TimeFormat.TWENTY_FOUR_HOUR).exceptionOrNull())
        assertEquals(listOf<Any>(), repository.writes)
    }
}
