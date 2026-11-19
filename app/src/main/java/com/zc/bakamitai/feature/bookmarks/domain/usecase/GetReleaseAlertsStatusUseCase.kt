package com.zc.bakamitai.feature.bookmarks.domain.usecase

import com.zc.bakamitai.common.domain.repository.SettingsRepository
import com.zc.bakamitai.feature.bookmarks.domain.model.ReleaseAlertsStatus
import com.zc.bakamitai.feature.bookmarks.domain.repository.ReleaseAlertNotifier
import kotlinx.coroutines.flow.first
import org.koin.core.annotation.Factory

/** The alerts status right now: the Settings switch, then whether Android lets the app post notifications. */
@Factory
class GetReleaseAlertsStatusUseCase(
    private val settingsRepository: SettingsRepository,
    private val notifier: ReleaseAlertNotifier,
) {
    suspend operator fun invoke(): ReleaseAlertsStatus = ReleaseAlertsStatus.of(
        isSwitchedOn = settingsRepository.observeSettings().first().newEpisodeAlerts,
        isSystemAllowed = notifier.areAlertsEnabled(),
    )
}
