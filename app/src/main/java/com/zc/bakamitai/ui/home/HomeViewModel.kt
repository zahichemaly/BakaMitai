package com.zc.bakamitai.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.zc.bakamitai.data.models.Resource
import com.zc.bakamitai.data.models.dtos.EntryDto
import com.zc.bakamitai.data.network.repos.SubsPleaseRepository
import com.zc.bakamitai.extensions.combineLoading
import com.zc.bakamitai.extensions.setError
import com.zc.bakamitai.extensions.setLoading
import com.zc.bakamitai.extensions.setSuccess
import com.zc.bakamitai.ui.base.BaseViewModel
import timber.log.Timber

class HomeViewModel(private val subsPleaseRepository: SubsPleaseRepository) :
    BaseViewModel() {
    private val _latestEntries = MutableLiveData<Resource<List<EntryDto>>>()
    val latestEntries: LiveData<Resource<List<EntryDto>>>
        get() = _latestEntries

    private val _todayEntries = MutableLiveData<Resource<List<EntryDto>>>()
    val todayEntries: LiveData<Resource<List<EntryDto>>>
        get() = _todayEntries

    val loadingAll: MediatorLiveData<Boolean> = _latestEntries.combineLoading(_todayEntries)

    fun refreshLatest() {
        getLatest()
        getTodaySchedule()
    }

    private fun getLatest() {
        performNetworkCall {
            _latestEntries.setLoading()
            val response = subsPleaseRepository.getLatest()
            if (response.isSuccessful && response.body() != null) {
                Timber.d("Finished getting latest entries")
                val data = response.body()!!.map { it.value.toEntryDto() }
                val ordered = data.sortedByDescending { it.getDateTime() }
                _latestEntries.setSuccess(ordered)
            } else {
                Timber.e("Error getting latest entries")
                onError(response.message())
                _latestEntries.setError(response)
            }
        }
    }

    private fun getTodaySchedule() {
        performNetworkCall {
            _todayEntries.setLoading()
            val response = subsPleaseRepository.getTodaySchedule()
            if (response.isSuccessful && response.body() != null) {
                Timber.d("Finished getting today entries")
                val data = response.body()!!.schedule.map { it.toEntryDto() }
                val ordered = data.sortedByDescending { it.getDateTime() }
                _todayEntries.setSuccess(ordered)
            } else {
                Timber.e("Error getting today entries")
                onError(response.message())
                _todayEntries.setError(response)
            }
        }
    }
}
