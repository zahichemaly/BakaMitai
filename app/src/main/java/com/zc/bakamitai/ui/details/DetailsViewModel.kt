package com.zc.bakamitai.ui.details

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.zc.bakamitai.data.models.Resource
import com.zc.bakamitai.data.models.dtos.ShowDetailsDto
import com.zc.bakamitai.data.network.repos.BookmarksRepository
import com.zc.bakamitai.data.network.repos.SubsPleaseRepository
import com.zc.bakamitai.extensions.setError
import com.zc.bakamitai.extensions.setLoading
import com.zc.bakamitai.extensions.setSuccess
import com.zc.bakamitai.extensions.toShowDetailsDto
import com.zc.bakamitai.ui.base.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

class DetailsViewModel(
    private val subsPleaseRepository: SubsPleaseRepository,
    private val bookmarksRepository: BookmarksRepository
) : BaseViewModel() {
    private val _show = MutableLiveData<Resource<ShowDetailsDto>>()
    val show: LiveData<Resource<ShowDetailsDto>>
        get() = _show

    private val _isBookmarked = MutableLiveData<Boolean>()
    val isBookmarked: LiveData<Boolean>
        get() = _isBookmarked


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

    fun setIsBookmarked(id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _isBookmarked.postValue(bookmarksRepository.isBookmarked(id))
        }
    }

    fun saveOrRemoveBookmark() {
        _isBookmarked.value?.let { bookmarked ->
            if (bookmarked) removeBookmark()
            else saveBookmark()
        }
    }

    private fun saveBookmark() {
        _show.value?.data?.let {
            viewModelScope.launch(Dispatchers.IO) {
                bookmarksRepository.addBookmark(it.toBookmark())
                _isBookmarked.postValue(true)
            }
        }
    }

    private fun removeBookmark() {
        _show.value?.data?.let {
            viewModelScope.launch(Dispatchers.IO) {
                bookmarksRepository.removeBookmark(it.sid!!)
                _isBookmarked.postValue(false)
            }
        }
    }
}
