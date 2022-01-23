package com.zc.bakamitai.data.network.repos

import com.zc.bakamitai.data.room.entities.Bookmark

interface ScheduleRepository {
    suspend fun fetchSchedules(forceFetch: Boolean = false)
    suspend fun setAsNotification(bookmark: Bookmark, isScheduled: Boolean)
}
