package com.zc.bakamitai.feature.bookmarks.di

import android.content.Context
import androidx.room.Room
import androidx.work.WorkManager
import com.zc.bakamitai.feature.bookmarks.data.local.BOOKMARKS_DATABASE_NAME
import com.zc.bakamitai.feature.bookmarks.data.local.BookmarkDao
import com.zc.bakamitai.feature.bookmarks.data.local.BookmarksDatabase
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

/**
 * Registers the annotated classes of the Bookmarks feature (repository, scheduler, worker, notifier, ViewModel),
 * and the types it doesn't own: the Room database, its DAO and `WorkManager`.
 */
@Module
@ComponentScan("com.zc.bakamitai.feature.bookmarks")
class BookmarksModule {

    @Single
    fun bookmarksDatabase(context: Context): BookmarksDatabase =
        Room.databaseBuilder<BookmarksDatabase>(context, BOOKMARKS_DATABASE_NAME).build()

    @Single
    fun bookmarkDao(database: BookmarksDatabase): BookmarkDao = database.bookmarkDao()

    @Single
    fun workManager(context: Context): WorkManager = WorkManager.getInstance(context)
}
