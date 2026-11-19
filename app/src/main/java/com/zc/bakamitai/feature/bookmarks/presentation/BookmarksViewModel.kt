package com.zc.bakamitai.feature.bookmarks.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zc.bakamitai.common.domain.model.ShowSlug
import com.zc.bakamitai.common.ui.component.error.mapper.toErrorUIModel
import com.zc.bakamitai.common.ui.resource.StringResource
import com.zc.bakamitai.common.ui.text.UiText
import com.zc.bakamitai.feature.bookmarks.domain.model.Bookmark
import com.zc.bakamitai.feature.bookmarks.domain.model.BookmarkSort
import com.zc.bakamitai.feature.bookmarks.domain.model.BookmarkedShow
import com.zc.bakamitai.feature.bookmarks.domain.usecase.GetReleaseAlertsEnabledUseCase
import com.zc.bakamitai.feature.bookmarks.domain.usecase.ObserveBookmarksUseCase
import com.zc.bakamitai.feature.bookmarks.domain.usecase.RefreshAiringSlotsUseCase
import com.zc.bakamitai.feature.bookmarks.domain.usecase.RemoveBookmarkUseCase
import com.zc.bakamitai.feature.bookmarks.domain.usecase.RestoreBookmarkUseCase
import com.zc.bakamitai.feature.bookmarks.presentation.component.bookmarkcard.mapper.toBookmarkCardUIModels
import com.zc.bakamitai.feature.bookmarks.presentation.component.bookmarksheader.mapper.sortChipUIModels
import com.zc.bakamitai.feature.bookmarks.presentation.component.bookmarksheader.mapper.toAlertsChipUIModel
import com.zc.bakamitai.feature.bookmarks.presentation.component.bookmarksheader.mapper.toBookmarkCountLabel
import com.zc.bakamitai.feature.bookmarks.presentation.model.BookmarkSortOption
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

/**
 * The saved shows in the selected order, from Room, with their airing chips; removal with Undo; the alerts chip.
 * Reducer rules: docs/5-bookmarks.md §6.11.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@KoinViewModel
class BookmarksViewModel(
    observeBookmarks: ObserveBookmarksUseCase,
    private val refreshAiringSlots: RefreshAiringSlotsUseCase,
    private val removeBookmark: RemoveBookmarkUseCase,
    private val restoreBookmark: RestoreBookmarkUseCase,
    private val getAlertsEnabled: GetReleaseAlertsEnabledUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(BookmarksState())

    /** Bumped by Retry: re-reads the bookmarks after a failure. */
    private val retries = MutableStateFlow(0)

    private val bookmarks: Flow<Result<List<BookmarkedShow>>> =
        combine(_state.map { it.selectedSort }.distinctUntilChanged(), retries) { sort, _ -> sort }
            .flatMapLatest { sort ->
                observeBookmarks(sort.toBookmarkSort())
                    .map { Result.success(it) }
                    .catch { emit(Result.failure(it)) }
            }

    val state: StateFlow<BookmarksState> = combine(_state, bookmarks) { state, result -> state.reduce(result) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = BookmarksState(),
        )

    private val _effect = Channel<BookmarksEffect>(Channel.BUFFERED)
    val effect: Flow<BookmarksEffect> = _effect.receiveAsFlow()

    private var slotJob: Job? = null

    /** The last removed bookmark, for Undo. Never exposed. */
    private var lastRemoved: Bookmark? = null

    init {
        onIntent(BookmarksIntent.Load)
    }

    fun onIntent(intent: BookmarksIntent) {
        when (intent) {
            BookmarksIntent.Load -> {
                _state.update { it.copy(header = it.header.copy(sortChips = sortChipUIModels())) }
                onIntent(BookmarksIntent.ScreenStarted)
            }

            BookmarksIntent.ScreenStarted -> {
                checkAlerts()
                refreshSlots(force = false)
            }

            BookmarksIntent.Refresh -> refreshSlots(force = true)

            BookmarksIntent.Retry -> retries.update { it + 1 }

            is BookmarksIntent.SortSelected -> _state.update { it.copy(selectedSort = intent.option) }

            is BookmarksIntent.BookmarkClicked -> sendEffect(BookmarksEffect.OpenDetails(intent.slug))

            is BookmarksIntent.RemoveClicked -> remove(intent.slug)

            is BookmarksIntent.UndoRemoveClicked -> undo(intent.slug)

            BookmarksIntent.DiscoverShowsClicked -> sendEffect(BookmarksEffect.OpenSchedule)

            BookmarksIntent.AlertsChipClicked -> onAlertsChipClicked()

            is BookmarksIntent.AlertsPermissionResult -> {
                checkAlerts()
                if (!intent.granted) sendEffect(BookmarksEffect.ShowAlertsDenied)
            }

            BookmarksIntent.SettingsClicked -> sendEffect(BookmarksEffect.OpenSettings)
        }
    }

    private fun checkAlerts() {
        val alertsChip = getAlertsEnabled().toAlertsChipUIModel()
        _state.update { it.copy(header = it.header.copy(alertsChip = alertsChip)) }
    }

    /**
     * Refreshes the airing slots: silently on screen start (ignored while a refresh runs), or forced by
     * pull-to-refresh, which replaces a silent one and reports a failure.
     */
    private fun refreshSlots(force: Boolean) {
        if (slotJob?.isActive == true) {
            if (!force || _state.value.isRefreshing) return
            slotJob?.cancel()
        }
        if (force) _state.update { it.copy(isRefreshing = true) }
        slotJob = viewModelScope.launch {
            val result = refreshAiringSlots(forceRefresh = force)
            if (force) {
                _state.update { it.copy(isRefreshing = false) }
                result.onFailure { error -> _effect.send(BookmarksEffect.ShowError(error.toErrorUIModel())) }
            }
        }
    }

    private fun remove(slug: String) {
        viewModelScope.launch {
            removeBookmark(ShowSlug(slug))
                .onSuccess { removed ->
                    if (removed != null) {
                        lastRemoved = removed
                        val message = UiText.StringResource(
                            StringResource.bookmarks_removed,
                            listOf(removed.show.title),
                        )
                        _effect.send(BookmarksEffect.ShowRemoved(message, slug))
                    }
                }
                .onFailure { error -> _effect.send(BookmarksEffect.ShowError(error.toErrorUIModel())) }
        }
    }

    /** Restores the last removed bookmark, only when it's [slug]'s (an older snackbar's Undo is ignored). */
    private fun undo(slug: String) {
        val removed = lastRemoved?.takeIf { it.show.slug.value == slug } ?: return
        lastRemoved = null
        viewModelScope.launch {
            restoreBookmark(
                removed,
            ).onFailure { error -> _effect.send(BookmarksEffect.ShowError(error.toErrorUIModel())) }
        }
    }

    private fun onAlertsChipClicked() {
        when (_state.value.header.alertsChip?.isEnabled) {
            true -> sendEffect(BookmarksEffect.OpenNotificationSettings)
            false -> sendEffect(BookmarksEffect.EnableAlerts)
            null -> Unit
        }
    }

    private fun sendEffect(effect: BookmarksEffect) {
        viewModelScope.launch { _effect.send(effect) }
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}

/** The bookmarks read (or the read failure) reduced into the screen state. */
private fun BookmarksState.reduce(result: Result<List<BookmarkedShow>>): BookmarksState = result.fold(
    onSuccess = { shows ->
        copy(
            isLoading = false,
            error = null,
            cards = shows.toBookmarkCardUIModels(),
            header = header.copy(countLabel = shows.size.toBookmarkCountLabel()),
        )
    },
    onFailure = { error -> copy(isLoading = false, error = error.toErrorUIModel()) },
)

private fun BookmarkSortOption.toBookmarkSort(): BookmarkSort = when (this) {
    BookmarkSortOption.RECENTLY_ADDED -> BookmarkSort.RECENTLY_ADDED
    BookmarkSortOption.TITLE -> BookmarkSort.TITLE
    BookmarkSortOption.RELEASE_TIME -> BookmarkSort.NEXT_AIRING
}
