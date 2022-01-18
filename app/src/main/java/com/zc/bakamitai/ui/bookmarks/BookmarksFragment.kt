package com.zc.bakamitai.ui.bookmarks

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import com.zc.bakamitai.databinding.FragmentBookmarksBinding
import com.zc.bakamitai.ui.base.BaseFragment
import org.koin.androidx.viewmodel.ext.android.viewModel

class BookmarksFragment : BaseFragment<FragmentBookmarksBinding, BookmarksViewModel>() {
    override val viewModel: BookmarksViewModel by viewModel()
    override fun getViewBinding() = FragmentBookmarksBinding.inflate(layoutInflater)
    private lateinit var bookmarkAdapter: BookmarkAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.getBookmarks()
    }

    override fun setupView() {
        bookmarkAdapter = BookmarkAdapter(this)
        binding.rvBookmarks.layoutManager = GridLayoutManager(requireContext(), 3)
        binding.rvBookmarks.adapter = bookmarkAdapter
    }

    override fun manageSubscriptions() {
        viewModel.bookmarks.observe(viewLifecycleOwner) {
            if (it.isNotEmpty()) bookmarkAdapter.addItems(it)
        }
        viewModel.deletedBookmark.observe(viewLifecycleOwner) { id ->
            if (!id.isNullOrEmpty()) {
                viewModel.getBookmarks()
            }
        }
    }
}
