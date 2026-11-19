package com.zc.bakamitai.feature.shows.presentation

import app.cash.turbine.test
import com.zc.bakamitai.common.domain.model.DomainError
import com.zc.bakamitai.common.domain.model.ShowTestData.show
import com.zc.bakamitai.common.ui.component.error.model.ErrorUIModel
import com.zc.bakamitai.common.ui.resource.PluralResource
import com.zc.bakamitai.common.ui.resource.StringResource
import com.zc.bakamitai.common.ui.text.UiText
import com.zc.bakamitai.common.util.MainDispatcherRule
import com.zc.bakamitai.feature.shows.domain.repository.FakeShowCatalogRepository
import com.zc.bakamitai.feature.shows.domain.usecase.FilterShowIndexUseCase
import com.zc.bakamitai.feature.shows.domain.usecase.GetShowIndexUseCase
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AllShowsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeShowCatalogRepository()

    private val networkError = ErrorUIModel(UiText.StringResource(StringResource.error_network), isRetryable = true)

    private val catalogue = listOf(
        show("detective-conan", "Detective Conan"),
        show("akame-ga-kill", "Akame ga Kill!"),
        show("86-eighty-six", "86 - Eighty Six"),
        show("meitantei-conan", "Meitantei Conan - Hannin no Hanzawa-san"),
        show("blue-lock", "Blue Lock"),
    )

    private fun createViewModel() = AllShowsViewModel(
        getShowIndex = GetShowIndexUseCase(repository),
        filterShowIndex = FilterShowIndexUseCase(),
    )

    private fun footer(count: Int) = UiText.StringPlural(PluralResource.all_shows_footer_count, count, listOf(count))

    private fun AllShowsState.letters() = sections.map { it.letter.letter }

    private fun AllShowsState.slugs() = sections.flatMap { section -> section.rows.map { it.slug } }

    @Test
    fun `the first load shows loading, then the whole catalogue`() = runTest {
        repository.result = Result.success(catalogue)
        val viewModel = createViewModel()

        viewModel.state.test {
            assertEquals(AllShowsState(isLoading = true), awaitItem())
            val loaded = awaitItem()
            assertFalse(loaded.isLoading)
            assertFalse(loaded.isRefreshing)
            assertNull(loaded.error)
            assertEquals("", loaded.query)
            assertEquals(listOf('#', 'A', 'B', 'D', 'M'), loaded.letters())
            assertEquals(
                listOf("86-eighty-six", "akame-ga-kill", "blue-lock", "detective-conan", "meitantei-conan"),
                loaded.slugs(),
            )
            assertEquals(footer(5), loaded.footerLabel)
            assertTrue(loaded.hasContent)
            assertFalse(loaded.showNoResults)
        }
        assertEquals(listOf(false), repository.forceRefreshCalls)
    }

    @Test
    fun `a failed first load shows the error, and Retry loads the catalogue`() = runTest {
        repository.result = Result.failure(DomainError.Network)
        val viewModel = createViewModel()

        viewModel.state.test {
            assertEquals(AllShowsState(isLoading = true), awaitItem())
            assertEquals(AllShowsState(error = networkError), awaitItem())

            repository.result = Result.success(catalogue)
            viewModel.onIntent(AllShowsIntent.Retry)

            assertEquals(AllShowsState(isLoading = true), awaitItem())
            val loaded = awaitItem()
            assertTrue(loaded.hasContent)
            assertNull(loaded.error)
        }
        assertEquals(listOf(false, false), repository.forceRefreshCalls)
    }

    @Test
    fun `a query filters synchronously, and the footer counts the matches`() = runTest {
        repository.result = Result.success(catalogue)
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onIntent(AllShowsIntent.QueryChanged("conan"))

        // No dispatcher advance: the field reads state.query, so the update must be immediate.
        val filtered = viewModel.state.value
        assertEquals("conan", filtered.query)
        assertEquals(listOf('D', 'M'), filtered.letters())
        assertEquals(listOf("detective-conan", "meitantei-conan"), filtered.slugs())
        assertEquals(footer(2), filtered.footerLabel)
    }

    @Test
    fun `a query typed while loading is kept, and filters the list when it arrives`() = runTest {
        repository.result = Result.success(catalogue)
        val gate = CompletableDeferred<Unit>()
        repository.gate = gate
        val viewModel = createViewModel()
        runCurrent()

        viewModel.onIntent(AllShowsIntent.QueryChanged("CONAN"))
        assertEquals(AllShowsState(isLoading = true, query = "CONAN"), viewModel.state.value)

        gate.complete(Unit)
        advanceUntilIdle()

        val loaded = viewModel.state.value
        assertEquals("CONAN", loaded.query)
        assertEquals(listOf("detective-conan", "meitantei-conan"), loaded.slugs())
    }

    @Test
    fun `a query without match shows no results, and clearing restores every section`() = runTest {
        repository.result = Result.success(catalogue)
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onIntent(AllShowsIntent.QueryChanged("zzz"))
        val empty = viewModel.state.value
        assertTrue(empty.showNoResults)
        assertTrue(empty.sections.isEmpty())
        assertEquals(footer(0), empty.footerLabel)

        viewModel.onIntent(AllShowsIntent.QueryCleared)
        val cleared = viewModel.state.value
        assertEquals("", cleared.query)
        assertFalse(cleared.showNoResults)
        assertEquals(listOf('#', 'A', 'B', 'D', 'M'), cleared.letters())
        assertEquals(footer(5), cleared.footerLabel)
    }

    @Test
    fun `a refresh shows the indicator, bypasses the cache and keeps the query`() = runTest {
        repository.result = Result.success(catalogue)
        val viewModel = createViewModel()

        viewModel.state.test {
            skipItems(2)
            viewModel.onIntent(AllShowsIntent.QueryChanged("conan"))
            assertEquals("conan", awaitItem().query)

            repository.result = Result.success(catalogue + show("case-closed", "Case Closed - Conan"))
            viewModel.onIntent(AllShowsIntent.Refresh)
            val refreshing = awaitItem()
            assertTrue(refreshing.isRefreshing)
            assertFalse(refreshing.isLoading)
            assertTrue(refreshing.hasContent)
            val refreshed = awaitItem()
            assertFalse(refreshed.isRefreshing)
            assertEquals("conan", refreshed.query)
            assertEquals(listOf("case-closed", "detective-conan", "meitantei-conan"), refreshed.slugs())
        }
        assertEquals(listOf(false, true), repository.forceRefreshCalls)
    }

    @Test
    fun `a failed refresh keeps the content and shows the error once`() = runTest {
        repository.result = Result.success(catalogue)
        val viewModel = createViewModel()
        advanceUntilIdle()
        val loaded = viewModel.state.value

        viewModel.effect.test {
            repository.result = Result.failure(DomainError.Network)
            viewModel.onIntent(AllShowsIntent.Refresh)

            assertEquals(AllShowsEffect.ShowError(networkError), awaitItem())
        }
        assertEquals(loaded, viewModel.state.value)
    }

    @Test
    fun `a refresh while a load is running is ignored`() = runTest {
        repository.result = Result.success(catalogue)
        val gate = CompletableDeferred<Unit>()
        repository.gate = gate
        val viewModel = createViewModel()
        runCurrent()

        viewModel.onIntent(AllShowsIntent.Refresh)
        viewModel.onIntent(AllShowsIntent.Retry)
        gate.complete(Unit)
        advanceUntilIdle()

        assertEquals(listOf(false), repository.forceRefreshCalls)
        assertTrue(viewModel.state.value.hasContent)
    }

    @Test
    fun `a show click opens its details, and the settings click opens settings`() = runTest {
        val viewModel = createViewModel()

        viewModel.effect.test {
            viewModel.onIntent(AllShowsIntent.ShowClicked("blue-lock"))
            assertEquals(AllShowsEffect.OpenDetails("blue-lock"), awaitItem())

            viewModel.onIntent(AllShowsIntent.SettingsClicked)
            assertEquals(AllShowsEffect.OpenSettings, awaitItem())
        }
    }
}
