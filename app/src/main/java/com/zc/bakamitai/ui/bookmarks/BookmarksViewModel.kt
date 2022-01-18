package com.zc.bakamitai.ui.bookmarks

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.zc.bakamitai.data.network.repos.BookmarksRepository
import com.zc.bakamitai.data.room.entities.Bookmark
import com.zc.bakamitai.ui.base.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BookmarksViewModel(private val bookmarksRepository: BookmarksRepository) : BaseViewModel() {
    private val _bookmarks = MutableLiveData<List<Bookmark>>()
    val bookmarks: LiveData<List<Bookmark>>
        get() = _bookmarks

    val deletedBookmark = bookmarksRepository.getDeletedBookmark()

    fun getBookmarks() {
        viewModelScope.launch(Dispatchers.IO) {
            _bookmarks.postValue(bookmarksRepository.getBookmarks())
        }
    }
}
