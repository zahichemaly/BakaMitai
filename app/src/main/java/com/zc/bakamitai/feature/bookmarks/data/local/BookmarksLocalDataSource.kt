package com.zc.bakamitai.feature.bookmarks.data.local

import androidx.datastore.core.DataStore
import com.zc.bakamitai.feature.bookmarks.data.dto.BookmarkDTO
import com.zc.bakamitai.feature.bookmarks.data.dto.BookmarkListDTO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single
import java.io.IOException

/** The Koin qualifier of the bookmarks `DataStore`, so it never collides with another store's binding. */
const val BOOKMARKS_DATA_STORE = "bookmarks"

/** The saved shows, in `bookmarks.json` (docs/4-details.md §4.4). */
@Single
class BookmarksLocalDataSource(@Named(BOOKMARKS_DATA_STORE) private val dataStore: DataStore<BookmarkListDTO>) {

    /** Every saved show in the order it was added, once per slug. A read error emits no bookmarks. */
    val bookmarks: Flow<List<BookmarkDTO>> = dataStore.data
        .catch { e -> if (e is IOException) emit(BookmarkListDTO(bookmarks = emptyList())) else throw e }
        .map { list -> list.bookmarks.distinctBy { it.slug } }
        .distinctUntilChanged()

    /**
     * Removes [bookmark]'s slug when it's saved, otherwise appends [bookmark], in one atomic write (a double tap
     * can't race). Returns whether the slug is saved afterwards.
     */
    suspend fun toggle(bookmark: BookmarkDTO): Boolean {
        val updated = dataStore.updateData { current ->
            val isSaved = current.bookmarks.any { it.slug == bookmark.slug }
            current.copy(
                bookmarks = if (isSaved) {
                    current.bookmarks.filterNot { it.slug == bookmark.slug }
                } else {
                    current.bookmarks + bookmark
                },
            )
        }
        return updated.bookmarks.any { it.slug == bookmark.slug }
    }
}
