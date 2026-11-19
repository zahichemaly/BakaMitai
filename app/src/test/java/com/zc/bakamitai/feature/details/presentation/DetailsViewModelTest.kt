package com.zc.bakamitai.feature.details.presentation

import app.cash.turbine.test
import com.zc.bakamitai.common.domain.model.DomainError
import com.zc.bakamitai.common.domain.model.ShowSlug
import com.zc.bakamitai.common.domain.model.ShowTestData.show
import com.zc.bakamitai.common.domain.util.FixedTimeZoneProvider
import com.zc.bakamitai.common.ui.component.error.model.ErrorUIModel
import com.zc.bakamitai.common.ui.resource.StringResource
import com.zc.bakamitai.common.ui.text.UiText
import com.zc.bakamitai.common.util.FixedClock
import com.zc.bakamitai.common.util.MainDispatcherRule
import com.zc.bakamitai.feature.bookmarks.domain.model.Bookmark
import com.zc.bakamitai.feature.bookmarks.domain.repository.FakeBookmarkRepository
import com.zc.bakamitai.feature.bookmarks.domain.repository.FakeReleaseCheckScheduler
import com.zc.bakamitai.feature.bookmarks.domain.usecase.ObserveIsBookmarkedUseCase
import com.zc.bakamitai.feature.bookmarks.domain.usecase.ToggleBookmarkUseCase
import com.zc.bakamitai.feature.details.domain.DetailsTestData.episode
import com.zc.bakamitai.feature.details.domain.DetailsTestData.episodes
import com.zc.bakamitai.feature.details.domain.DetailsTestData.newYork
import com.zc.bakamitai.feature.details.domain.DetailsTestData.overview
import com.zc.bakamitai.feature.details.domain.repository.FakeShowDetailsRepository
import com.zc.bakamitai.feature.details.domain.repository.FakeShowDetailsRepository.EpisodesCall
import com.zc.bakamitai.feature.details.domain.repository.FakeShowDetailsRepository.OverviewCall
import com.zc.bakamitai.feature.details.domain.usecase.GetShowEpisodesUseCase
import com.zc.bakamitai.feature.details.domain.usecase.GetShowOverviewUseCase
import com.zc.bakamitai.feature.details.presentation.model.DetailsHeaderUIModel
import com.zc.bakamitai.feature.details.presentation.model.DownloadSource
import com.zc.bakamitai.feature.details.presentation.model.DownloadsUIState
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import java.io.IOException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Instant

class DetailsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val slug = "sora-wa-akai-kawa-no-hotori"
    private val now = Instant.parse("2026-11-19T08:00:00Z")
    private val repository = FakeShowDetailsRepository()
    private val bookmarks = FakeBookmarkRepository()

    private val page = overview(slug = slug)
    private val loadedEpisodes = episodes(
        episodes = listOf(
            episode("20", releasedAt = "2026-11-17T17:38:52Z"),
            episode("19", releasedAt = "2026-11-10T17:38:52Z"),
        ),
    )

    private val networkError = ErrorUIModel(UiText.StringResource(StringResource.error_network), isRetryable = true)
    private val genericError = ErrorUIModel(UiText.StringResource(StringResource.error_generic), isRetryable = true)

    private val expectedHeader = DetailsHeaderUIModel(
        title = UiText.StringValue("Sora wa Akai Kawa no Hotori"),
        coverUrl = "https://subsplease.org/wp-content/uploads/2026/07/158138.jpg",
        synopsis = UiText.StringValue("A synopsis."),
    )

    private fun createViewModel() = DetailsViewModel(
        slug = slug,
        getShowOverview = GetShowOverviewUseCase(repository),
        getShowEpisodes = GetShowEpisodesUseCase(repository, FixedTimeZoneProvider(newYork)),
        observeIsBookmarked = ObserveIsBookmarkedUseCase(bookmarks),
        toggleBookmark = ToggleBookmarkUseCase(bookmarks, FakeReleaseCheckScheduler(), FixedClock(now)),
    )

    /** Keeps the `WhileSubscribed` state collected, so `state.value` follows every update. */
    private fun TestScope.subscribed(viewModel: DetailsViewModel): DetailsViewModel = viewModel.also {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { it.state.collect {} }
    }

    /** A view model whose page and episodes are loaded. */
    private fun TestScope.loadedViewModel(): DetailsViewModel {
        repository.overviewResult = Result.success(page)
        repository.episodesResult = Result.success(loadedEpisodes)
        return subscribed(createViewModel()).also { advanceUntilIdle() }
    }

    private fun DetailsState.content() = assertIs<DownloadsUIState.Content>(downloads)

    private fun DetailsState.expandedKeys() = content().expandedKeys

    @Test
    fun `the first load shows the skeleton, then the header over loading downloads, then the episodes`() = runTest {
        repository.overviewResult = Result.success(page)
        repository.episodesResult = Result.success(loadedEpisodes)
        val gate = CompletableDeferred<Unit>()
        repository.episodesGate = gate
        val viewModel = subscribed(createViewModel())

        assertTrue(viewModel.state.value.isLoading)
        assertEquals(DetailsState(), viewModel.state.value)

        runCurrent()
        val partial = viewModel.state.value
        assertEquals(expectedHeader, partial.header)
        assertEquals(DownloadsUIState.Loading, partial.downloads)
        assertFalse(partial.isLoading)

        gate.complete(Unit)
        advanceUntilIdle()
        val loaded = viewModel.state.value
        assertEquals(listOf("episode|20", "episode|19"), loaded.content().episodes.map { it.key })
        assertEquals(emptySet(), loaded.expandedKeys())
        assertNull(loaded.error)
        assertEquals(listOf(OverviewCall(ShowSlug(slug), forceRefresh = false)), repository.overviewCalls)
        assertEquals(listOf(EpisodesCall(ShowSlug(slug), newYork, forceRefresh = false)), repository.episodesCalls)
    }

    @Test
    fun `a failed page shows the error without loading episodes, and Retry loads everything`() = runTest {
        repository.overviewResult = Result.failure(DomainError.Network)
        val viewModel = subscribed(createViewModel())
        advanceUntilIdle()

        assertEquals(DetailsState(error = networkError), viewModel.state.value)
        assertEquals(emptyList(), repository.episodesCalls)

        repository.overviewResult = Result.success(page)
        repository.episodesResult = Result.success(loadedEpisodes)
        val gate = CompletableDeferred<Unit>()
        repository.overviewGate = gate
        viewModel.onIntent(DetailsIntent.Retry)
        runCurrent()
        assertEquals(DetailsState(), viewModel.state.value)

        gate.complete(Unit)
        advanceUntilIdle()

        assertEquals(expectedHeader, viewModel.state.value.header)
        assertNull(viewModel.state.value.error)
        assertEquals(2, viewModel.state.value.content().episodes.size)
    }

    @Test
    fun `a page that no longer exists can't be retried`() = runTest {
        repository.overviewResult = Result.failure(DomainError.NotFound)
        val viewModel = subscribed(createViewModel())
        advanceUntilIdle()

        assertEquals(
            ErrorUIModel(UiText.StringResource(StringResource.error_not_found), isRetryable = false),
            viewModel.state.value.error,
        )
    }

    @Test
    fun `failed episodes keep the header, and RetryEpisodes loads only them`() = runTest {
        repository.overviewResult = Result.success(page)
        repository.episodesResult = Result.failure(DomainError.Network)
        val viewModel = subscribed(createViewModel())
        advanceUntilIdle()

        assertEquals(expectedHeader, viewModel.state.value.header)
        assertEquals(DownloadsUIState.Error(networkError), viewModel.state.value.downloads)

        repository.episodesResult = Result.success(loadedEpisodes)
        val gate = CompletableDeferred<Unit>()
        repository.episodesGate = gate
        viewModel.onIntent(DetailsIntent.RetryEpisodes)
        runCurrent()
        assertEquals(DownloadsUIState.Loading, viewModel.state.value.downloads)

        gate.complete(Unit)
        advanceUntilIdle()

        assertEquals(emptySet(), viewModel.state.value.expandedKeys())
        assertEquals(1, repository.overviewCalls.size)
        assertEquals(2, repository.episodesCalls.size)
    }

    @Test
    fun `RetryEpisodes and Refresh before the header do nothing`() = runTest {
        repository.overviewResult = Result.failure(DomainError.Network)
        val viewModel = subscribed(createViewModel())
        advanceUntilIdle()

        viewModel.onIntent(DetailsIntent.RetryEpisodes)
        viewModel.onIntent(DetailsIntent.Refresh)
        advanceUntilIdle()

        assertEquals(1, repository.overviewCalls.size)
        assertEquals(emptyList(), repository.episodesCalls)
        assertFalse(viewModel.state.value.isRefreshing)
    }

    @Test
    fun `Refresh reloads both bypassing the cache, and keeps the expanded rows`() = runTest {
        val viewModel = loadedViewModel()
        viewModel.onIntent(DetailsIntent.EpisodeToggled("episode|19"))
        runCurrent()
        repository.overviewResult = Result.success(page.copy(synopsis = "New synopsis."))
        repository.episodesResult = Result.success(
            loadedEpisodes.copy(episodes = listOf(episode("21", "2026-11-24T17:38:52Z")) + loadedEpisodes.episodes),
        )
        val gate = CompletableDeferred<Unit>()
        repository.episodesGate = gate

        viewModel.onIntent(DetailsIntent.Refresh)
        runCurrent()
        assertTrue(viewModel.state.value.isRefreshing)
        assertEquals(UiText.StringValue("New synopsis."), viewModel.state.value.header?.synopsis)
        assertEquals(2, viewModel.state.value.content().episodes.size)

        gate.complete(Unit)
        advanceUntilIdle()
        val refreshed = viewModel.state.value
        assertFalse(refreshed.isRefreshing)
        assertEquals(listOf("episode|21", "episode|20", "episode|19"), refreshed.content().episodes.map { it.key })
        assertEquals(setOf("episode|19"), refreshed.expandedKeys())
        assertEquals(listOf(false, true), repository.overviewCalls.map { it.forceRefresh })
        assertEquals(listOf(false, true), repository.episodesCalls.map { it.forceRefresh })
    }

    @Test
    fun `a failed refresh of the page keeps the content, shows an error and skips the episodes`() = runTest {
        val viewModel = loadedViewModel()
        val before = viewModel.state.value

        viewModel.effect.test {
            repository.overviewResult = Result.failure(DomainError.Network)
            viewModel.onIntent(DetailsIntent.Refresh)
            advanceUntilIdle()

            assertEquals(DetailsEffect.ShowError(networkError), awaitItem())
        }
        assertEquals(before, viewModel.state.value)
        assertEquals(1, repository.episodesCalls.size)
    }

    @Test
    fun `a failed refresh of the episodes keeps the old rows and shows an error`() = runTest {
        val viewModel = loadedViewModel()
        val before = viewModel.state.value

        viewModel.effect.test {
            repository.episodesResult = Result.failure(DomainError.Network)
            viewModel.onIntent(DetailsIntent.Refresh)
            advanceUntilIdle()

            assertEquals(DetailsEffect.ShowError(networkError), awaitItem())
        }
        assertEquals(before.downloads, viewModel.state.value.downloads)
        assertFalse(viewModel.state.value.isRefreshing)
    }

    @Test
    fun `a second Load or Refresh while loading is ignored`() = runTest {
        repository.overviewResult = Result.success(page)
        repository.episodesResult = Result.success(loadedEpisodes)
        val gate = CompletableDeferred<Unit>()
        repository.episodesGate = gate
        val viewModel = subscribed(createViewModel())
        runCurrent()

        viewModel.onIntent(DetailsIntent.Load)
        viewModel.onIntent(DetailsIntent.Retry)
        viewModel.onIntent(DetailsIntent.Refresh)
        viewModel.onIntent(DetailsIntent.RetryEpisodes)
        gate.complete(Unit)
        advanceUntilIdle()

        assertEquals(1, repository.overviewCalls.size)
        assertEquals(1, repository.episodesCalls.size)
        assertFalse(viewModel.state.value.isRefreshing)
    }

    @Test
    fun `isBookmarked follows the bookmarks`() = runTest {
        val viewModel = loadedViewModel()
        assertFalse(viewModel.state.value.isBookmarked)

        bookmarks.bookmarks.value = listOf(Bookmark(show(slug), now))
        runCurrent()
        assertTrue(viewModel.state.value.isBookmarked)

        bookmarks.bookmarks.value = emptyList()
        runCurrent()
        assertFalse(viewModel.state.value.isBookmarked)
    }

    @Test
    fun `BookmarkClicked saves the page's show as of now, and a second click removes it`() = runTest {
        val viewModel = loadedViewModel()

        viewModel.onIntent(DetailsIntent.BookmarkClicked)
        advanceUntilIdle()

        val saved = Bookmark(
            show = show(
                slug = slug,
                title = "Sora wa Akai Kawa no Hotori",
                coverUrl = "https://subsplease.org/wp-content/uploads/2026/07/158138.jpg",
            ),
            addedAt = now,
        )
        assertEquals(listOf(saved), bookmarks.toggled)
        assertTrue(viewModel.state.value.isBookmarked)

        viewModel.onIntent(DetailsIntent.BookmarkClicked)
        advanceUntilIdle()
        assertFalse(viewModel.state.value.isBookmarked)
    }

    @Test
    fun `the very first bookmark asks for release alerts`() = runTest {
        val viewModel = loadedViewModel()

        viewModel.effect.test {
            viewModel.onIntent(DetailsIntent.BookmarkClicked)

            assertEquals(DetailsEffect.AskForReleaseAlerts, awaitItem())
        }
    }

    @Test
    fun `a second bookmark or a removal asks for nothing`() = runTest {
        bookmarks.bookmarks.value = listOf(Bookmark(show("dandadan"), now))
        val viewModel = loadedViewModel()

        viewModel.effect.test {
            viewModel.onIntent(DetailsIntent.BookmarkClicked)
            advanceUntilIdle()
            viewModel.onIntent(DetailsIntent.BookmarkClicked)
            advanceUntilIdle()

            expectNoEvents()
        }
        assertEquals(2, bookmarks.toggled.size)
    }

    @Test
    fun `BookmarkClicked before the page is loaded does nothing`() = runTest {
        repository.overviewResult = Result.failure(DomainError.Network)
        val viewModel = subscribed(createViewModel())
        advanceUntilIdle()

        viewModel.onIntent(DetailsIntent.BookmarkClicked)
        advanceUntilIdle()

        assertEquals(emptyList(), bookmarks.toggled)
    }

    @Test
    fun `a failed bookmark write shows the generic error`() = runTest {
        val viewModel = loadedViewModel()
        bookmarks.toggleFailure = DomainError.Unknown(IOException("disk full"))

        viewModel.effect.test {
            viewModel.onIntent(DetailsIntent.BookmarkClicked)

            assertEquals(DetailsEffect.ShowError(genericError), awaitItem())
        }
        assertFalse(viewModel.state.value.isBookmarked)
    }

    @Test
    fun `toggling the synopsis and a row needs no request`() = runTest {
        val viewModel = loadedViewModel()

        viewModel.onIntent(DetailsIntent.SynopsisToggled)
        viewModel.onIntent(DetailsIntent.EpisodeToggled("episode|19"))
        runCurrent()

        assertTrue(viewModel.state.value.isSynopsisExpanded)
        assertEquals(setOf("episode|19"), viewModel.state.value.expandedKeys())

        viewModel.onIntent(DetailsIntent.SynopsisToggled)
        viewModel.onIntent(DetailsIntent.EpisodeToggled("episode|19"))
        runCurrent()

        assertFalse(viewModel.state.value.isSynopsisExpanded)
        assertEquals(emptySet(), viewModel.state.value.expandedKeys())
        assertEquals(1, repository.overviewCalls.size)
        assertEquals(1, repository.episodesCalls.size)
    }

    @Test
    fun `back, share and open in web send their effects`() = runTest {
        val viewModel = loadedViewModel()

        viewModel.effect.test {
            viewModel.onIntent(DetailsIntent.BackClicked)
            assertEquals(DetailsEffect.NavigateBack, awaitItem())

            viewModel.onIntent(DetailsIntent.ShareClicked)
            assertEquals(
                DetailsEffect.Share(
                    title = UiText.StringValue("Sora wa Akai Kawa no Hotori"),
                    text = UiText.StringResource(
                        StringResource.details_share_text,
                        listOf("Sora wa Akai Kawa no Hotori", "https://subsplease.org/shows/$slug/"),
                    ),
                ),
                awaitItem(),
            )

            viewModel.onIntent(DetailsIntent.OpenInWebClicked)
            assertEquals(DetailsEffect.OpenWebLink("https://subsplease.org/shows/$slug/"), awaitItem())
        }
    }

    @Test
    fun `share and open in web need the page`() = runTest {
        repository.overviewResult = Result.failure(DomainError.Network)
        val viewModel = subscribed(createViewModel())
        advanceUntilIdle()

        viewModel.effect.test {
            viewModel.onIntent(DetailsIntent.ShareClicked)
            viewModel.onIntent(DetailsIntent.OpenInWebClicked)
            advanceUntilIdle()

            expectNoEvents()
        }
    }

    @Test
    fun `magnet and torrent open a torrent link, XDCC a web link`() = runTest {
        val viewModel = loadedViewModel()

        viewModel.effect.test {
            viewModel.onIntent(DetailsIntent.SourceClicked(DownloadSource.MAGNET, "magnet:?xt=urn:btih:x"))
            assertEquals(DetailsEffect.OpenTorrentLink("magnet:?xt=urn:btih:x"), awaitItem())

            viewModel.onIntent(DetailsIntent.SourceClicked(DownloadSource.TORRENT, "https://nyaa.si/view/1/torrent"))
            assertEquals(DetailsEffect.OpenTorrentLink("https://nyaa.si/view/1/torrent"), awaitItem())

            viewModel.onIntent(
                DetailsIntent.SourceClicked(DownloadSource.XDCC, "https://subsplease.org/xdcc/?search=x"),
            )
            assertEquals(DetailsEffect.OpenWebLink("https://subsplease.org/xdcc/?search=x"), awaitItem())
        }
    }
}
