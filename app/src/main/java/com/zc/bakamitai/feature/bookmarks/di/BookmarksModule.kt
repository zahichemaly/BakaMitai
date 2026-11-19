package com.zc.bakamitai.feature.bookmarks.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.dataStoreFile
import com.zc.bakamitai.feature.bookmarks.data.dto.BookmarkListDTO
import com.zc.bakamitai.feature.bookmarks.data.local.BOOKMARKS_DATA_STORE
import com.zc.bakamitai.feature.bookmarks.data.local.BookmarkListSerializer
import kotlinx.serialization.json.Json
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single

private const val BOOKMARKS_FILE = "bookmarks.json"

/**
 * Registers the annotated classes of the Bookmarks feature, and its `DataStore`. The store is provided here,
 * not in `CommonModule` (which can't see a feature's DTO), and named so it can't collide with another
 * `DataStore` binding (Koin keys by class, and the type argument is erased).
 */
@Module
@ComponentScan("com.zc.bakamitai.feature.bookmarks")
class BookmarksModule {

    @Single
    @Named(BOOKMARKS_DATA_STORE)
    fun bookmarksDataStore(context: Context, json: Json): DataStore<BookmarkListDTO> = DataStoreFactory.create(
        serializer = BookmarkListSerializer(json),
        corruptionHandler = ReplaceFileCorruptionHandler { BookmarkListDTO(bookmarks = emptyList()) },
        produceFile = { context.dataStoreFile(BOOKMARKS_FILE) },
    )
}
