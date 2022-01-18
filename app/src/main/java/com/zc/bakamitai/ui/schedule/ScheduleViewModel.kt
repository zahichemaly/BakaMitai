package com.zc.bakamitai.ui.schedule

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.zc.bakamitai.data.models.Resource
import com.zc.bakamitai.data.models.dtos.ScheduleDto
import com.zc.bakamitai.data.network.repos.SubsPleaseRepository
import com.zc.bakamitai.extensions.setError
import com.zc.bakamitai.extensions.setLoading
import com.zc.bakamitai.extensions.setSuccess
import com.zc.bakamitai.prefs.FirstDayOfWeek
import com.zc.bakamitai.prefs.PreferenceUtil
import com.zc.bakamitai.prefs.TimeFormat
import com.zc.bakamitai.ui.base.BaseViewModel
import timber.log.Timber

class ScheduleViewModel(
    private val subsPleaseRepository: SubsPleaseRepository,
    preferenceUtil: PreferenceUtil
) :
    BaseViewModel() {
    private val _schedule = MutableLiveData<Resource<List<ScheduleDto>>>()
    val schedule: LiveData<Resource<List<ScheduleDto>>>
        get() = _schedule

    private val is12HourFormat = preferenceUtil.getTimeFormat() == TimeFormat.TF_12
    private val startsMonday = preferenceUtil.getFirstDayOfWeek() == FirstDayOfWeek.Monday

    fun getSchedule() {
        performNetworkCall {
            _schedule.setLoading()
            val response = subsPleaseRepository.getSchedule()
            if (response.isSuccessful && response.body() != null) {
                Timber.d("Finished getting schedule")
                val data = response.body()!!.toScheduleDtoList(is12HourFormat, startsMonday)
                _schedule.setSuccess(data)
            } else {
                Timber.e("Error getting schedule")
                onError(response.message())
                _schedule.setError(response)
            }
        }
    }
}
