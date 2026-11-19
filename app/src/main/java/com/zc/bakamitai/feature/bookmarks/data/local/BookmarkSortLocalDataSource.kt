package com.zc.bakamitai.feature.bookmarks.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.zc.bakamitai.common.data.local.enumOrNull
import com.zc.bakamitai.common.data.local.safeData
import com.zc.bakamitai.feature.bookmarks.domain.model.BookmarkSort
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Single

/**
 * The Bookmarks sort, saved by name in the app's Preferences DataStore (key `bookmarks_sort`). A missing or unknown
 * value, or a read failure, is [BookmarkSort.RECENTLY_ADDED]. A write throws `IOException`.
 */
@Single
class BookmarkSortLocalDataSource(private val dataStore: DataStore<Preferences>) {

    val sort: Flow<BookmarkSort> = dataStore.safeData()
        .map { preferences -> preferences.enumOrNull<BookmarkSort>(BOOKMARKS_SORT) ?: BookmarkSort.RECENTLY_ADDED }
        .distinctUntilChanged()

    suspend fun setSort(sort: BookmarkSort) {
        dataStore.edit { it[BOOKMARKS_SORT] = sort.name }
    }

    private companion object {
        val BOOKMARKS_SORT = stringPreferencesKey("bookmarks_sort")
    }
}
