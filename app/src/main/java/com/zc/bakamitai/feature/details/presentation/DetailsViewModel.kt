package com.zc.bakamitai.feature.details.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zc.bakamitai.common.domain.model.ShowSlug
import com.zc.bakamitai.common.ui.component.error.mapper.toErrorUIModel
import com.zc.bakamitai.common.ui.resource.StringResource
import com.zc.bakamitai.common.ui.text.UiText
import com.zc.bakamitai.feature.bookmarks.domain.usecase.ObserveIsBookmarkedUseCase
import com.zc.bakamitai.feature.bookmarks.domain.usecase.ToggleBookmarkUseCase
import com.zc.bakamitai.feature.details.domain.model.ShowOverview
import com.zc.bakamitai.feature.details.domain.usecase.GetShowEpisodesUseCase
import com.zc.bakamitai.feature.details.domain.usecase.GetShowOverviewUseCase
import com.zc.bakamitai.feature.details.presentation.mapper.toDownloadsUIState
import com.zc.bakamitai.feature.details.presentation.mapper.toHeaderUIModel
import com.zc.bakamitai.feature.details.presentation.model.DownloadSource
import com.zc.bakamitai.feature.details.presentation.model.DownloadsUIState
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.KoinViewModel

/**
 * One show: its page (header), then its downloads, both cache-backed; the bookmark follows the bookmarks store.
 * Reducer rules: docs/4-details.md §5.10.
 *
 * @param slug the show, from `DetailsNavKey`.
 */
@KoinViewModel
class DetailsViewModel(
    @InjectedParam slug: String,
    private val getShowOverview: GetShowOverviewUseCase,
    private val getShowEpisodes: GetShowEpisodesUseCase,
    observeIsBookmarked: ObserveIsBookmarkedUseCase,
    private val toggleBookmark: ToggleBookmarkUseCase,
) : ViewModel() {

    private val showSlug = ShowSlug(slug)

    private val _state = MutableStateFlow(DetailsState())
    val state: StateFlow<DetailsState> =
        combine(_state, observeIsBookmarked(showSlug)) { state, isBookmarked ->
            state.copy(isBookmarked = isBookmarked)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = DetailsState(),
        )

    private val _effect = Channel<DetailsEffect>(Channel.BUFFERED)
    val effect: Flow<DetailsEffect> = _effect.receiveAsFlow()

    private var loadJob: Job? = null

    /** The last loaded page, for sharing, opening in the browser and bookmarking. Never exposed. */
    private var overview: ShowOverview? = null

    init {
        onIntent(DetailsIntent.Load)
    }

    fun onIntent(intent: DetailsIntent) {
        when (intent) {
            DetailsIntent.Load,
            DetailsIntent.Retry,
            -> load(forceRefresh = false)

            DetailsIntent.Refresh -> load(forceRefresh = true)

            DetailsIntent.RetryEpisodes -> retryEpisodes()

            DetailsIntent.BackClicked -> sendEffect(DetailsEffect.NavigateBack)

            DetailsIntent.ShareClicked -> withOverview { loaded ->
                val title = loaded.show.title
                DetailsEffect.Share(
                    title = UiText.StringValue(title),
                    text = UiText.StringResource(StringResource.details_share_text, listOf(title, loaded.webUrl)),
                )
            }

            DetailsIntent.OpenInWebClicked -> withOverview { DetailsEffect.OpenWebLink(it.webUrl) }

            DetailsIntent.BookmarkClicked -> onBookmarkClicked()

            DetailsIntent.SynopsisToggled -> _state.update { it.copy(isSynopsisExpanded = !it.isSynopsisExpanded) }

            is DetailsIntent.EpisodeToggled ->
                _state.update { it.copy(downloads = it.downloads.withEpisodeToggled(intent.key)) }

            is DetailsIntent.SourceClicked -> sendEffect(
                when (intent.source) {
                    DownloadSource.MAGNET, DownloadSource.TORRENT -> DetailsEffect.OpenTorrentLink(intent.url)
                    DownloadSource.XDCC -> DetailsEffect.OpenWebLink(intent.url)
                },
            )
        }
    }

    /**
     * The page, then the episodes, in one job; ignored while a load runs. A refresh ([forceRefresh]) needs a
     * header and keeps the content: its failures are snackbars. Otherwise a failed page is the full-screen error.
     */
    private fun load(forceRefresh: Boolean) {
        if (loadJob?.isActive == true || (forceRefresh && _state.value.header == null)) return
        _state.update { if (forceRefresh) it.copy(isRefreshing = true) else it.copy(error = null) }
        loadJob = viewModelScope.launch {
            getShowOverview(showSlug, forceRefresh)
                .onSuccess { loaded ->
                    overview = loaded
                    _state.update { it.copy(header = loaded.toHeaderUIModel(), error = null) }
                    loadEpisodes(forceRefresh)
                }
                .onFailure { error ->
                    val errorModel = error.toErrorUIModel()
                    if (_state.value.header != null) {
                        _effect.send(DetailsEffect.ShowError(errorModel))
                    } else {
                        _state.update { it.copy(error = errorModel) }
                    }
                }
            _state.update { it.copy(isRefreshing = false) }
        }
    }

    /** Only the episodes, once the header is shown; ignored while a load runs. */
    private fun retryEpisodes() {
        if (loadJob?.isActive == true || _state.value.header == null) return
        loadJob = viewModelScope.launch { loadEpisodes(forceRefresh = false) }
    }

    /**
     * Shows the section skeleton unless rows are already shown (a refresh keeps them). The rows expanded before
     * stay expanded; every row starts collapsed. A failure replaces only a missing list: over rows, it's
     * a snackbar.
     */
    private suspend fun loadEpisodes(forceRefresh: Boolean) {
        _state.update { state ->
            if (state.downloads is DownloadsUIState.Content) state else state.copy(downloads = DownloadsUIState.Loading)
        }
        getShowEpisodes(showSlug, forceRefresh)
            .onSuccess { loaded ->
                _state.update { state ->
                    val expandedKeys = (state.downloads as? DownloadsUIState.Content)?.expandedKeys
                    state.copy(downloads = loaded.toDownloadsUIState(expandedKeys.orEmpty()))
                }
            }
            .onFailure { error ->
                val errorModel = error.toErrorUIModel()
                if (_state.value.downloads is DownloadsUIState.Content) {
                    _effect.send(DetailsEffect.ShowError(errorModel))
                } else {
                    _state.update { it.copy(downloads = DownloadsUIState.Error(errorModel)) }
                }
            }
    }

    /** Saves or removes the page's show; [state] follows the store, so there's nothing to roll back. */
    private fun onBookmarkClicked() {
        val show = overview?.show ?: return
        viewModelScope.launch {
            toggleBookmark(show).onFailure { error -> _effect.send(DetailsEffect.ShowError(error.toErrorUIModel())) }
        }
    }

    /** Sends the effect built from the loaded page; nothing before the page is loaded. */
    private fun withOverview(effect: (ShowOverview) -> DetailsEffect) {
        overview?.let { sendEffect(effect(it)) }
    }

    private fun sendEffect(effect: DetailsEffect) {
        viewModelScope.launch { _effect.send(effect) }
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
