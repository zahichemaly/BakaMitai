package com.zc.bakamitai.feature.bookmarks.domain.usecase

import com.zc.bakamitai.feature.bookmarks.domain.repository.FakeReleaseAlertNotifier
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class GetReleaseAlertsEnabledUseCaseTest {

    private val notifier = FakeReleaseAlertNotifier()
    private val useCase = GetReleaseAlertsEnabledUseCase(notifier)

    @Test
    fun `follows the notifier`() {
        assertTrue(useCase())
        notifier.enabled = false
        assertFalse(useCase())
    }
}
