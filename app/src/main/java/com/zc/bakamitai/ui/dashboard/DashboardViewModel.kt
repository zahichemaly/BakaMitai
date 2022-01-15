package com.zc.bakamitai.ui.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.zc.bakamitai.data.models.EntryResponse
import com.zc.bakamitai.data.network.repos.SubsPleaseRepository
import com.zc.bakamitai.ui.base.BaseViewModel
import retrofit2.Response

class DashboardViewModel(private val subsPleaseRepository: SubsPleaseRepository) : BaseViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is dashboard Fragment"
    }
    val text: LiveData<String> = _text

    private val _latestAnime = MutableLiveData<Response<HashMap<String, EntryResponse>>>()
    val latestAnime: LiveData<Response<HashMap<String, EntryResponse>>>
        get() = _latestAnime

    fun getLatest() {
        performNetworkCall {
            val data = subsPleaseRepository.getLatest()
            if (data.isSuccessful) {
                _latestAnime.postValue(data)
            } else {
                onError(data.message())
            }
        }
    }
}
