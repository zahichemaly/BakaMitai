package com.zc.bakamitai.feature.home.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zc.bakamitai.common.ui.component.error.mapper.toErrorUIModel
import com.zc.bakamitai.feature.home.domain.model.TodayBroadcasts
import com.zc.bakamitai.feature.home.domain.usecase.GetTodayBroadcastsUseCase
import com.zc.bakamitai.feature.home.domain.usecase.GetTodayDateUseCase
import com.zc.bakamitai.feature.home.presentation.component.releaserow.mapper.toUIModel
import com.zc.bakamitai.feature.home.presentation.component.todaysummary.mapper.toTodaySummaryUIModel
import com.zc.bakamitai.feature.home.presentation.component.upcomingcard.mapper.toUIModel
import com.zc.bakamitai.feature.home.presentation.mapper.toUpcomingCountLabel
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class HomeViewModel(
    private val getTodayBroadcasts: GetTodayBroadcastsUseCase,
    private val getTodayDate: GetTodayDateUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()

    private val _effect = Channel<HomeEffect>(Channel.BUFFERED)
    val effect: Flow<HomeEffect> = _effect.receiveAsFlow()

    private var loadJob: Job? = null

    /** The date of the last successful load, to reload when the app comes back on a new day. */
    private var loadedDate: LocalDate? = null

    init {
        onIntent(HomeIntent.Load)
    }

    fun onIntent(intent: HomeIntent) {
        when (intent) {
            HomeIntent.Load,
            HomeIntent.Retry,
            HomeIntent.Refresh,
            -> load()

            HomeIntent.ScreenStarted -> reloadOnNewDay()

            is HomeIntent.ShowClicked -> sendEffect(HomeEffect.OpenDetails(intent.slug))

            HomeIntent.SettingsClicked -> sendEffect(HomeEffect.OpenSettings)
        }
    }

    /** Ignored while a load is running. With content shown, it refreshes in place; otherwise it loads. */
    private fun load() {
        if (loadJob?.isActive == true) return
        _state.update { it.copy(isLoading = !it.hasContent, isRefreshing = it.hasContent, error = null) }
        loadJob = viewModelScope.launch {
            getTodayBroadcasts()
                .onSuccess { broadcasts ->
                    loadedDate = broadcasts.date
                    _state.update { it.withBroadcasts(broadcasts) }
                }
                .onFailure { error ->
                    val errorModel = error.toErrorUIModel()
                    val hadContent = _state.value.hasContent
                    _state.update { state ->
                        state.copy(
                            isLoading = false,
                            isRefreshing = false,
                            error = if (state.hasContent) null else errorModel,
                        )
                    }
                    if (hadContent) _effect.send(HomeEffect.ShowError(errorModel))
                }
        }
    }

    private fun reloadOnNewDay() {
        val lastDate = loadedDate ?: return
        if (loadJob?.isActive != true && getTodayDate() != lastDate) load()
    }

    private fun HomeState.withBroadcasts(broadcasts: TodayBroadcasts) = copy(
        isLoading = false,
        isRefreshing = false,
        error = null,
        summary = broadcasts.toTodaySummaryUIModel(),
        upcomingCountLabel = broadcasts.toUpcomingCountLabel(),
        upcoming = broadcasts.upcoming.map { it.toUIModel() }.toImmutableList(),
        latest = broadcasts.released.map { it.toUIModel(broadcasts.timeZone) }.toImmutableList(),
    )

    private fun sendEffect(effect: HomeEffect) {
        viewModelScope.launch { _effect.send(effect) }
    }
}
