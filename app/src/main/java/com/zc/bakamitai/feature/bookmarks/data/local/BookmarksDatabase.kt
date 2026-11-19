package com.zc.bakamitai.feature.bookmarks.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

/** The file name of [BookmarksDatabase], under the app's `databases/` directory. */
const val BOOKMARKS_DATABASE_NAME = "bookmarks.db"

/**
 * The bookmarks and their alert tracking (docs/5-bookmarks.md §4.3). Schema changes bump [Database.version] and
 * add an `AutoMigration`; there is no destructive fallback.
 */
@Database(entities = [BookmarkEntity::class], version = 1, exportSchema = true)
abstract class BookmarksDatabase : RoomDatabase() {
    abstract fun bookmarkDao(): BookmarkDao
}
