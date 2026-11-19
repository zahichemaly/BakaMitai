package com.zc.bakamitai.feature.bookmarks.domain.usecase

import com.zc.bakamitai.feature.bookmarks.domain.model.ReleaseAlertsStatus
import org.koin.core.annotation.Factory

/**
 * Whether new-episode alerts are posted right now: switched on in Settings and allowed by Android. When not, a
 * release check makes no request.
 */
@Factory
class GetReleaseAlertsEnabledUseCase(private val getStatus: GetReleaseAlertsStatusUseCase) {
    suspend operator fun invoke(): Boolean = getStatus() == ReleaseAlertsStatus.ON
}
