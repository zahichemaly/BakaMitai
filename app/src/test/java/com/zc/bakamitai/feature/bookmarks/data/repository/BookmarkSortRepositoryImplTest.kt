package com.zc.bakamitai.feature.bookmarks.data.repository

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.zc.bakamitai.common.data.local.FailingPreferencesDataStore
import com.zc.bakamitai.common.domain.model.DomainError
import com.zc.bakamitai.feature.bookmarks.data.local.BookmarkSortLocalDataSource
import com.zc.bakamitai.feature.bookmarks.domain.model.BookmarkSort
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class BookmarkSortRepositoryImplTest {

    @get:Rule
    val tmp = TemporaryFolder()

    @Test
    fun `a write succeeds and is read back`() = runTest {
        val dataStore = PreferenceDataStoreFactory.create(scope = backgroundScope) {
            File(tmp.root, "settings.preferences_pb")
        }
        val repository = BookmarkSortRepositoryImpl(BookmarkSortLocalDataSource(dataStore))

        assertEquals(BookmarkSort.RECENTLY_ADDED, repository.observeSort().first())
        assertEquals(Result.success(Unit), repository.setSort(BookmarkSort.NEXT_AIRING))
        assertEquals(BookmarkSort.NEXT_AIRING, repository.observeSort().first())
    }

    @Test
    fun `a failing write is DomainError Unknown`() = runTest {
        val repository = BookmarkSortRepositoryImpl(BookmarkSortLocalDataSource(FailingPreferencesDataStore()))

        assertIs<DomainError.Unknown>(repository.setSort(BookmarkSort.TITLE).exceptionOrNull())
    }
}
