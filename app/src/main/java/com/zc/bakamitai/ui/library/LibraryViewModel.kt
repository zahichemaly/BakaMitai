package com.zc.bakamitai.ui.library

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.zc.bakamitai.data.models.Resource
import com.zc.bakamitai.data.models.dtos.ShowDto
import com.zc.bakamitai.data.network.repos.SubsPleaseRepository
import com.zc.bakamitai.extensions.setError
import com.zc.bakamitai.extensions.setLoading
import com.zc.bakamitai.extensions.setSuccess
import com.zc.bakamitai.extensions.toShowDtoList
import com.zc.bakamitai.ui.base.BaseViewModel
import timber.log.Timber

class LibraryViewModel(private val subsPleaseRepository: SubsPleaseRepository) : BaseViewModel() {
    private val _shows = MutableLiveData<Resource<List<ShowDto>>>()
    val shows: LiveData<Resource<List<ShowDto>>>
        get() = _shows

    init {
        getShows()
    }

    private fun getShows() {
        Timber.d("Getting all shows")
        _shows.setLoading()
        performNetworkCall {
            val response = subsPleaseRepository.getShows()
            if (response.isSuccessful && response.body() != null) {
                Timber.d("Finished getting all shows")
                val result = response.body()!!.toShowDtoList()
                _shows.setSuccess(result)
            } else {
                Timber.e("Error getting all shows")
                onError(response.message())
                _shows.setError(response)
            }
        }
    }
}
