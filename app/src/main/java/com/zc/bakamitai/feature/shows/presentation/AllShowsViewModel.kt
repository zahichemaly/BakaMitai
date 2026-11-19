package com.zc.bakamitai.feature.shows.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zc.bakamitai.common.ui.component.error.mapper.toErrorUIModel
import com.zc.bakamitai.feature.shows.domain.model.ShowIndex
import com.zc.bakamitai.feature.shows.domain.usecase.FilterShowIndexUseCase
import com.zc.bakamitai.feature.shows.domain.usecase.GetShowIndexUseCase
import com.zc.bakamitai.feature.shows.presentation.mapper.toFooterLabel
import com.zc.bakamitai.feature.shows.presentation.mapper.toSectionUIModels
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class AllShowsViewModel(
    private val getShowIndex: GetShowIndexUseCase,
    private val filterShowIndex: FilterShowIndexUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(AllShowsState())
    val state: StateFlow<AllShowsState> = _state.asStateFlow()

    private val _effect = Channel<AllShowsEffect>(Channel.BUFFERED)
    val effect: Flow<AllShowsEffect> = _effect.receiveAsFlow()

    private var loadJob: Job? = null

    /** The last successful load, re-filtered on every query change. Never exposed. */
    private var index: ShowIndex? = null

    init {
        onIntent(AllShowsIntent.Load)
    }

    fun onIntent(intent: AllShowsIntent) {
        when (intent) {
            AllShowsIntent.Load,
            AllShowsIntent.Retry,
            -> load(forceRefresh = false)

            AllShowsIntent.Refresh -> load(forceRefresh = true)

            is AllShowsIntent.QueryChanged -> changeQuery(intent.query)

            AllShowsIntent.QueryCleared -> changeQuery("")

            is AllShowsIntent.ShowClicked -> sendEffect(AllShowsEffect.OpenDetails(intent.slug))

            AllShowsIntent.SettingsClicked -> sendEffect(AllShowsEffect.OpenSettings)
        }
    }

    /** Ignored while a load is running. With content shown, it refreshes in place; otherwise it loads. */
    private fun load(forceRefresh: Boolean) {
        if (loadJob?.isActive == true) return
        _state.update { it.copy(isLoading = !it.hasContent, isRefreshing = it.hasContent, error = null) }
        loadJob = viewModelScope.launch {
            getShowIndex(forceRefresh = forceRefresh)
                .onSuccess { loaded ->
                    index = loaded
                    _state.update { state ->
                        state.copy(isLoading = false, isRefreshing = false, error = null).withIndex(loaded)
                    }
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
                    if (hadContent) _effect.send(AllShowsEffect.ShowError(errorModel))
                }
        }
    }

    /**
     * Synchronous, so the search field (which shows `state.query`) never lags or moves its cursor. Before the first
     * load only the query is kept; it filters the list when the list arrives.
     */
    private fun changeQuery(query: String) {
        val loaded = index
        _state.update { state ->
            val queried = state.copy(query = query)
            if (loaded != null) queried.withIndex(loaded) else queried
        }
    }

    /** Reduces [index] filtered by the current query. */
    private fun AllShowsState.withIndex(index: ShowIndex): AllShowsState {
        val filtered = filterShowIndex(index, query)
        return copy(sections = filtered.toSectionUIModels(), footerLabel = filtered.toFooterLabel())
    }

    private fun sendEffect(effect: AllShowsEffect) {
        viewModelScope.launch { _effect.send(effect) }
    }
}
