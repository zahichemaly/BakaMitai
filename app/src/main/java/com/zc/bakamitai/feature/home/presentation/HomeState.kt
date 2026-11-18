package com.zc.bakamitai.feature.home.presentation

import androidx.compose.runtime.Immutable
import com.zc.bakamitai.common.ui.component.error.model.ErrorUIModel
import com.zc.bakamitai.common.ui.text.UiText
import com.zc.bakamitai.feature.home.presentation.component.releaserow.model.ReleaseRowUIModel
import com.zc.bakamitai.feature.home.presentation.component.todaysummary.model.TodaySummaryUIModel
import com.zc.bakamitai.feature.home.presentation.component.upcomingcard.model.UpcomingCardUIModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

/**
 * @property isLoading first load, with nothing to show yet.
 * @property isRefreshing a reload while content is shown (pull-to-refresh or a new day).
 * @property error the full-screen error, set only when there is no content.
 * @property summary the banner; non-null once a load succeeded.
 * @property upcomingCountLabel the Upcoming section's count pill, "9 shows".
 */
@Immutable
data class HomeState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: ErrorUIModel? = null,
    val summary: TodaySummaryUIModel? = null,
    val upcomingCountLabel: UiText? = null,
    val upcoming: ImmutableList<UpcomingCardUIModel> = persistentListOf(),
    val latest: ImmutableList<ReleaseRowUIModel> = persistentListOf(),
) {
    val hasContent: Boolean get() = summary != null
    val isEmpty: Boolean get() = hasContent && upcoming.isEmpty() && latest.isEmpty()
}
