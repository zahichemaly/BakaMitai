package com.zc.bakamitai.ui.bookmarks

import androidx.recyclerview.widget.GridLayoutManager
import com.zc.bakamitai.R
import com.zc.bakamitai.data.models.Resource
import com.zc.bakamitai.databinding.FragmentBookmarksBinding
import com.zc.bakamitai.ui.base.BaseFragment
import org.koin.androidx.viewmodel.ext.android.viewModel

class BookmarksFragment : BaseFragment<FragmentBookmarksBinding, BookmarksViewModel>() {
    override val viewModel: BookmarksViewModel by viewModel()
    override fun getViewBinding() = FragmentBookmarksBinding.inflate(layoutInflater)
    private lateinit var bookmarkAdapter: BookmarkAdapter

    override fun setupView() {
        bookmarkAdapter = BookmarkAdapter(this)
        binding.rvBookmarks.layoutManager = GridLayoutManager(requireContext(), 3)
        binding.rvBookmarks.adapter = bookmarkAdapter
    }

    override fun manageSubscriptions() {
        viewModel.bookmarks.observe(viewLifecycleOwner) {
            if (it is Resource.Loading) binding.loadingView.setLoading()
            else {
                val data = it.data ?: listOf()
                if (data.isEmpty()) {
                    binding.loadingView.setMessage(getString(R.string.empty_bookmarks))
                } else {
                    binding.loadingView.setSuccess()
                    bookmarkAdapter.addItems(data)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.getBookmarks()
    }
}
