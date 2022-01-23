package com.zc.bakamitai.data.room.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.zc.bakamitai.data.room.entities.Schedule

@Dao
interface ScheduleDao {
    @Query("SELECT * FROM schedule")
    fun getAll(): List<Schedule>

    @Query("SELECT * FROM schedule WHERE page = :page")
    fun getByPage(page: String): Schedule?

    @Query("SELECT * FROM schedule s INNER JOIN bookmark b ON s.page = b.page")
    fun getFromBookmarks(): List<Schedule>

    @Query("UPDATE schedule SET isScheduled = :isScheduled")
    fun markAllSchedule(isScheduled: Boolean)

    @Query("UPDATE schedule SET isScheduled = :isScheduled WHERE page = :page")
    fun markSchedule(page: String, isScheduled: Boolean)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(vararg schedules: Schedule)
}
