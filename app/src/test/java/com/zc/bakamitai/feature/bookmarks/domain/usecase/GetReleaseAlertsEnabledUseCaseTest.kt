package com.zc.bakamitai.feature.bookmarks.domain.usecase

import com.zc.bakamitai.common.domain.model.AppSettings
import com.zc.bakamitai.common.domain.repository.FakeSettingsRepository
import com.zc.bakamitai.feature.bookmarks.domain.repository.FakeReleaseAlertNotifier
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class GetReleaseAlertsEnabledUseCaseTest {

    private val settings = FakeSettingsRepository()
    private val notifier = FakeReleaseAlertNotifier()
    private val useCase = GetReleaseAlertsEnabledUseCase(GetReleaseAlertsStatusUseCase(settings, notifier))

    @Test
    fun `enabled only when switched on and allowed by Android`() = runTest {
        assertTrue(useCase())

        notifier.enabled = false
        assertFalse(useCase())

        notifier.enabled = true
        settings.settings.value = AppSettings(newEpisodeAlerts = false)
        assertFalse(useCase())
    }
}
