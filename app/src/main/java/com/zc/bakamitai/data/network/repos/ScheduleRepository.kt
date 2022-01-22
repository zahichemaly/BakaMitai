package com.zc.bakamitai.data.network.repos

import androidx.lifecycle.LiveData
import com.zc.bakamitai.data.room.entities.Schedule

interface ScheduleRepository {
    suspend fun getSchedules(forceFetch: Boolean = false): LiveData<List<Schedule>>
}
