package com.zc.bakamitai.ui.schedule

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.zc.bakamitai.data.models.Resource
import com.zc.bakamitai.data.models.dtos.ScheduleDto
import com.zc.bakamitai.data.network.repos.SubsPleaseRepository
import com.zc.bakamitai.extensions.setError
import com.zc.bakamitai.extensions.setLoading
import com.zc.bakamitai.extensions.setSuccess
import com.zc.bakamitai.ui.base.BaseViewModel

class ScheduleViewModel(private val subsPleaseRepository: SubsPleaseRepository) : BaseViewModel() {
    private val _schedule = MutableLiveData<Resource<List<ScheduleDto>>>()
    val schedule: LiveData<Resource<List<ScheduleDto>>>
        get() = _schedule

    fun getSchedule() {
        performNetworkCall {
            _schedule.setLoading()
            val response = subsPleaseRepository.getSchedule()
            if (response.isSuccessful && response.body() != null) {
                val data = response.body()!!.toScheduleDtoList()
                _schedule.setSuccess(data)
            } else {
                onError(response.message())
                _schedule.setError(response)
            }
        }
    }
}
