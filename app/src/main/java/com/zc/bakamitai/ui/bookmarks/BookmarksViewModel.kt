package com.zc.bakamitai.ui.bookmarks

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.zc.bakamitai.data.models.Resource
import com.zc.bakamitai.data.network.repos.BookmarksRepository
import com.zc.bakamitai.data.room.entities.Bookmark
import com.zc.bakamitai.extensions.setLoading
import com.zc.bakamitai.extensions.setSuccess
import com.zc.bakamitai.ui.base.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BookmarksViewModel(private val bookmarksRepository: BookmarksRepository) : BaseViewModel() {
    private val _bookmarks = MutableLiveData<Resource<List<Bookmark>>>()
    val bookmarks: LiveData<Resource<List<Bookmark>>>
        get() = _bookmarks

    fun getBookmarks() {
        _bookmarks.setLoading()
        viewModelScope.launch(Dispatchers.IO) {
            _bookmarks.setSuccess(bookmarksRepository.getBookmarks())
        }
    }
}
