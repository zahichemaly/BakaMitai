package com.zc.bakamitai.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.zc.bakamitai.data.models.Resource
import com.zc.bakamitai.data.models.dtos.EntryDto
import com.zc.bakamitai.data.network.repos.SubsPleaseRepository
import com.zc.bakamitai.extensions.setError
import com.zc.bakamitai.extensions.setLoading
import com.zc.bakamitai.extensions.setSuccess
import com.zc.bakamitai.ui.base.BaseViewModel

class HomeViewModel(private val subsPleaseRepository: SubsPleaseRepository) : BaseViewModel() {
    private val _text = MutableLiveData<String>().apply {
        value = "This is home alone Fragment"
    }
    val text: LiveData<String> = _text

    private val _latestEntries = MutableLiveData<Resource<List<EntryDto>>>()
    val latestEntries: LiveData<Resource<List<EntryDto>>>
        get() = _latestEntries

    fun getLatest() {
        performNetworkCall {
            _latestEntries.setLoading()
            val response = subsPleaseRepository.getLatest()
            if (response.isSuccessful && response.body() != null) {
                val data = response.body()!!.map { it.value.toEntryDto() }
                _latestEntries.setSuccess(data)
            } else {
                onError(response.message())
                _latestEntries.setError(response)
            }
        }
    }
}
