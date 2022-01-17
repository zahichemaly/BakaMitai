package com.zc.bakamitai.data.room

import androidx.room.Database
import androidx.room.RoomDatabase
import com.zc.bakamitai.data.room.daos.BookmarkDao
import com.zc.bakamitai.data.room.entities.Bookmark

@Database(entities = [Bookmark::class], version = 3)
abstract class AppDatabase : RoomDatabase() {
    abstract fun bookmarkDao(): BookmarkDao
}
