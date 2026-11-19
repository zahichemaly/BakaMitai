package com.zc.bakamitai.feature.bookmarks.domain.usecase

import com.zc.bakamitai.feature.bookmarks.domain.repository.ReleaseAlertNotifier
import org.koin.core.annotation.Factory

/** Whether new-episode notifications can be posted right now. */
@Factory
class GetReleaseAlertsEnabledUseCase(private val notifier: ReleaseAlertNotifier) {
    operator fun invoke(): Boolean = notifier.areAlertsEnabled()
}
