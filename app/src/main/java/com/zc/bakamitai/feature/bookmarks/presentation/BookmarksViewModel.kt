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
import com.zc.bakamitai.feature.bookmarks.domain.model.ReleaseAlertsStatus
import com.zc.bakamitai.feature.bookmarks.domain.usecase.GetReleaseAlertsStatusUseCase
import com.zc.bakamitai.feature.bookmarks.domain.usecase.ObserveBookmarkSortUseCase
import com.zc.bakamitai.feature.bookmarks.domain.usecase.ObserveBookmarksUseCase
import com.zc.bakamitai.feature.bookmarks.domain.usecase.RefreshAiringSlotsUseCase
import com.zc.bakamitai.feature.bookmarks.domain.usecase.RemoveBookmarkUseCase
import com.zc.bakamitai.feature.bookmarks.domain.usecase.RestoreBookmarkUseCase
import com.zc.bakamitai.feature.bookmarks.domain.usecase.UpdateBookmarkSortUseCase
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
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

/**
 * The saved shows in the saved order, from Room, with their airing chips; removal with Undo; the alerts chip.
 * Reducer rules: docs/5-bookmarks.md §6.11, with the sort and the alerts status of docs/6-settings.md §5.6.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@KoinViewModel
class BookmarksViewModel(
    observeBookmarks: ObserveBookmarksUseCase,
    observeBookmarkSort: ObserveBookmarkSortUseCase,
    private val updateBookmarkSort: UpdateBookmarkSortUseCase,
    private val refreshAiringSlots: RefreshAiringSlotsUseCase,
    private val removeBookmark: RemoveBookmarkUseCase,
    private val restoreBookmark: RestoreBookmarkUseCase,
    private val getAlertsStatus: GetReleaseAlertsStatusUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(BookmarksState())

    /** Bumped by Retry: re-reads the bookmarks after a failure. */
    private val retries = MutableStateFlow(0)

    /** The saved sort, with the bookmarks in that order (or the read failure). */
    private val bookmarks: Flow<Pair<BookmarkSort, Result<List<BookmarkedShow>>>> =
        combine(observeBookmarkSort(), retries) { sort, _ -> sort }
            .flatMapLatest { sort ->
                observeBookmarks(sort)
                    .map { sort to Result.success(it) }
                    .catch { emit(sort to Result.failure(it)) }
            }

    val state: StateFlow<BookmarksState> =
        combine(_state, bookmarks) { state, (sort, result) -> state.reduce(sort, result) }
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

    /** The last alerts status read, for the chip's tap; `null` until read. Never exposed. */
    private var alertsStatus: ReleaseAlertsStatus? = null

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

            is BookmarksIntent.SortSelected -> selectSort(intent.option)

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
        viewModelScope.launch {
            val status = getAlertsStatus()
            alertsStatus = status
            _state.update { it.copy(header = it.header.copy(alertsChip = status.toAlertsChipUIModel())) }
        }
    }

    /** Saves the sort; the grid follows the store. A failed write keeps the saved order. */
    private fun selectSort(option: BookmarkSortOption) {
        viewModelScope.launch {
            updateBookmarkSort(option.toBookmarkSort())
                .onFailure { error -> _effect.send(BookmarksEffect.ShowError(error.toErrorUIModel())) }
        }
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

    /** On, or switched off in Settings: opens Settings. Blocked by Android: turns them on. */
    private fun onAlertsChipClicked() {
        when (alertsStatus) {
            ReleaseAlertsStatus.ON, ReleaseAlertsStatus.OFF -> sendEffect(BookmarksEffect.OpenSettings)
            ReleaseAlertsStatus.BLOCKED -> sendEffect(BookmarksEffect.EnableAlerts)
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

/** The saved sort and the bookmarks read (or the read failure) reduced into the screen state. */
private fun BookmarksState.reduce(sort: BookmarkSort, result: Result<List<BookmarkedShow>>): BookmarksState {
    val sorted = copy(isLoading = false, selectedSort = sort.toSortOption())
    return result.fold(
        onSuccess = { shows ->
            sorted.copy(
                error = null,
                cards = shows.toBookmarkCardUIModels(),
                header = header.copy(countLabel = shows.size.toBookmarkCountLabel()),
            )
        },
        onFailure = { error -> sorted.copy(error = error.toErrorUIModel()) },
    )
}

private fun BookmarkSort.toSortOption(): BookmarkSortOption = when (this) {
    BookmarkSort.RECENTLY_ADDED -> BookmarkSortOption.RECENTLY_ADDED
    BookmarkSort.TITLE -> BookmarkSortOption.TITLE
    BookmarkSort.NEXT_AIRING -> BookmarkSortOption.RELEASE_TIME
}

private fun BookmarkSortOption.toBookmarkSort(): BookmarkSort = when (this) {
    BookmarkSortOption.RECENTLY_ADDED -> BookmarkSort.RECENTLY_ADDED
    BookmarkSortOption.TITLE -> BookmarkSort.TITLE
    BookmarkSortOption.RELEASE_TIME -> BookmarkSort.NEXT_AIRING
}
