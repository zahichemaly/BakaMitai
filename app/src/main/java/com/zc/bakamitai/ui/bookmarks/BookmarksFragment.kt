package com.zc.bakamitai.ui.bookmarks

import com.zc.bakamitai.databinding.FragmentBookmarksBinding
import com.zc.bakamitai.ui.base.BaseFragment
import org.koin.android.ext.android.inject

class BookmarksFragment : BaseFragment<FragmentBookmarksBinding, BookmarksViewModel>() {
    override val viewModel: BookmarksViewModel by inject()
    override fun getViewBinding() = FragmentBookmarksBinding.inflate(layoutInflater)


}
