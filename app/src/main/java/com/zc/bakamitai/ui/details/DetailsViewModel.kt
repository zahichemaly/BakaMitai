package com.zc.bakamitai.ui.details

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.zc.bakamitai.data.models.Resource
import com.zc.bakamitai.data.models.dtos.ShowDetailsDto
import com.zc.bakamitai.data.network.repos.SubsPleaseRepository
import com.zc.bakamitai.extensions.setError
import com.zc.bakamitai.extensions.setLoading
import com.zc.bakamitai.extensions.setSuccess
import com.zc.bakamitai.extensions.toShowDetailsDto
import com.zc.bakamitai.ui.base.BaseViewModel
import timber.log.Timber

class DetailsViewModel(private val subsPleaseRepository: SubsPleaseRepository) : BaseViewModel() {
    private val _show = MutableLiveData<Resource<ShowDetailsDto>>()
    val show: LiveData<Resource<ShowDetailsDto>>
        get() = _show


    fun getShowDetails(page: String) {
        performNetworkCall {
            _show.setLoading()
            val response = subsPleaseRepository.getShowDetails(page)
            if (response.isSuccessful && response.body() != null) {
                Timber.d("Finished getting show details $page")
                val result = response.body()!!.toShowDetailsDto(page)
                _show.setSuccess(result)
                Timber.d(result.toString())
            } else {
                Timber.d("Error getting show details $page")
                onError(response.message())
                _show.setError(response)
            }
        }
    }
}
