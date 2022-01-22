package com.zc.bakamitai.data.network.repos.impl

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.zc.bakamitai.data.network.repos.ScheduleRepository
import com.zc.bakamitai.data.network.services.SubsPleaseService
import com.zc.bakamitai.data.room.daos.ScheduleDao
import com.zc.bakamitai.data.room.entities.Schedule

class ScheduleRepositoryImpl(
    private val scheduleDao: ScheduleDao,
    private val subsPleaseService: SubsPleaseService
) :
    ScheduleRepository {
    private val _schedules = MutableLiveData<List<Schedule>>()

    override suspend fun getSchedules(forceFetch: Boolean): LiveData<List<Schedule>> {
        if (forceFetch) {
            fetchFromServer()
        } else {
            fetchFromDb()
            if (_schedules.value.isNullOrEmpty()) {
                fetchFromServer()
            }
        }
        return _schedules
    }

    private suspend fun fetchFromServer() {
        val response = subsPleaseService.getSchedule()
        if (response.isSuccessful && response.body() != null) {
            val entities = response.body()!!.toScheduleEntities()
            scheduleDao.insert(*entities.toTypedArray())
            _schedules.postValue(entities)
        }
    }

    private fun fetchFromDb() {
        _schedules.postValue(scheduleDao.getAll())
    }
}
