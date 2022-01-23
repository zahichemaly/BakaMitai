package com.zc.bakamitai.data.network.repos.impl

import android.content.Context
import com.zc.bakamitai.data.network.repos.ScheduleRepository
import com.zc.bakamitai.data.network.services.SubsPleaseService
import com.zc.bakamitai.data.room.daos.ScheduleDao
import com.zc.bakamitai.data.room.entities.Bookmark
import com.zc.bakamitai.data.room.entities.Schedule
import com.zc.bakamitai.utils.NotificationHelper
import timber.log.Timber

class ScheduleRepositoryImpl(
    private val context: Context,
    private val scheduleDao: ScheduleDao,
    private val subsPleaseService: SubsPleaseService
) :
    ScheduleRepository {

    private val schedules = mutableSetOf<Schedule>()

    override suspend fun fetchSchedules(forceFetch: Boolean) {
        if (forceFetch) {
            fetchFromServer()
        } else {
            Timber.d("Fetching from db")
            val result = scheduleDao.getAll()
            if (result.isNullOrEmpty()) {
                Timber.d("Data is empty, fetching from server")
                fetchFromServer()
            } else {
                Timber.d("Fetched from db")
                schedules.addAll(result)
            }
        }
    }

    override suspend fun setAsNotification(bookmark: Bookmark, isScheduled: Boolean) {
        val page = bookmark.page
        val scheduledBookmark = scheduleDao.getByPage(page)
        if (scheduledBookmark != null) {
            Timber.d("Setting schedule $page as $isScheduled")
            scheduleDao.markSchedule(page, isScheduled)
            schedules.find { it.page == page }?.isScheduled = isScheduled
            handleNotification(scheduledBookmark, isScheduled)
        } else {
            Timber.d("Bookmark does not have a schedule")
        }
    }

    private suspend fun fetchFromServer() {
        Timber.d("Fetching from server")
        val response = subsPleaseService.getSchedule()
        if (response.isSuccessful && response.body() != null) {
            Timber.d("Fetched from server")
            val entities = response.body()!!.toScheduleEntities()
            scheduleDao.insert(*entities.toTypedArray())
            schedules.addAll(entities)
        }
    }

    private fun handleNotification(schedule: Schedule, isScheduled: Boolean) {
        if (isScheduled) {
            val isAlreadyScheduled = NotificationHelper.isNotificationSet(context, schedule.id)
            if (!isAlreadyScheduled) {
                NotificationHelper.addNotification(context, schedule)
            }
        } else {
            NotificationHelper.removeNotification(context, schedule.id)
        }
    }
}
