package com.zc.bakamitai.data.room

import androidx.room.Database
import androidx.room.RoomDatabase
import com.zc.bakamitai.data.room.daos.BookmarkDao
import com.zc.bakamitai.data.room.daos.ScheduleDao
import com.zc.bakamitai.data.room.entities.Bookmark
import com.zc.bakamitai.data.room.entities.Schedule

@Database(entities = [Bookmark::class, Schedule::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun bookmarkDao(): BookmarkDao
    abstract fun scheduleDao(): ScheduleDao
}
