package com.zc.bakamitai.feature.bookmarks.data.local

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.zc.bakamitai.common.data.local.FailingPreferencesDataStore
import com.zc.bakamitai.feature.bookmarks.domain.model.BookmarkSort
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import java.io.File
import java.io.IOException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class BookmarkSortLocalDataSourceTest {

    @get:Rule
    val tmp = TemporaryFolder()

    private fun TestScope.dataStore() =
        PreferenceDataStoreFactory.create(scope = backgroundScope) { File(tmp.root, "settings.preferences_pb") }

    @Test
    fun `nothing saved is recently added`() = runTest {
        assertEquals(BookmarkSort.RECENTLY_ADDED, BookmarkSortLocalDataSource(dataStore()).sort.first())
    }

    @Test
    fun `the sort round-trips`() = runTest {
        val dataSource = BookmarkSortLocalDataSource(dataStore())

        dataSource.setSort(BookmarkSort.TITLE)

        assertEquals(BookmarkSort.TITLE, dataSource.sort.first())
    }

    @Test
    fun `an unknown name is recently added`() = runTest {
        val dataStore = dataStore()
        dataStore.edit { it[stringPreferencesKey("bookmarks_sort")] = "RATING" }

        assertEquals(BookmarkSort.RECENTLY_ADDED, BookmarkSortLocalDataSource(dataStore).sort.first())
    }

    @Test
    fun `a read failure is recently added, and a write failure throws`() = runTest {
        val dataSource = BookmarkSortLocalDataSource(FailingPreferencesDataStore())

        assertEquals(BookmarkSort.RECENTLY_ADDED, dataSource.sort.first())
        assertFailsWith<IOException> { dataSource.setSort(BookmarkSort.TITLE) }
    }
}
