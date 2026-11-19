package com.zc.bakamitai.feature.bookmarks.domain.usecase

import com.zc.bakamitai.common.domain.model.AppSettings
import com.zc.bakamitai.common.domain.repository.FakeSettingsRepository
import com.zc.bakamitai.feature.bookmarks.domain.model.ReleaseAlertsStatus
import com.zc.bakamitai.feature.bookmarks.domain.repository.FakeReleaseAlertNotifier
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class GetReleaseAlertsStatusUseCaseTest {

    private val settings = FakeSettingsRepository()
    private val notifier = FakeReleaseAlertNotifier()
    private val useCase = GetReleaseAlertsStatusUseCase(settings, notifier)

    @Test
    fun `switched off is OFF whatever the notifier says`() = runTest {
        settings.settings.value = AppSettings(newEpisodeAlerts = false)

        assertEquals(ReleaseAlertsStatus.OFF, useCase())
        notifier.enabled = false
        assertEquals(ReleaseAlertsStatus.OFF, useCase())
    }

    @Test
    fun `switched on follows the notifier`() = runTest {
        assertEquals(ReleaseAlertsStatus.ON, useCase())
        notifier.enabled = false
        assertEquals(ReleaseAlertsStatus.BLOCKED, useCase())
    }
}
