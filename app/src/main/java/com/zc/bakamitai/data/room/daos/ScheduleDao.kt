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

    @Query("SELECT * FROM schedule WHERE id = :id")
    fun getById(id: String): Schedule?

    @Query("SELECT * FROM schedule s INNER JOIN bookmark b ON s.id = b.page")
    fun getFromBookmarks(): List<Schedule>

    @Query("UPDATE schedule SET isScheduled = isScheduled")
    fun markAllSchedule(isSchedule: Boolean): List<Schedule>

    @Query("UPDATE schedule SET isScheduled = isScheduled WHERE id = id")
    fun markSchedule(id: String, isSchedule: Boolean): List<Schedule>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg schedules: Schedule)
}
