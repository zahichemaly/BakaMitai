package com.zc.bakamitai.ui.library

import androidx.recyclerview.widget.LinearLayoutManager
import com.zc.bakamitai.data.models.Resource
import com.zc.bakamitai.databinding.FragmentLibraryBinding
import com.zc.bakamitai.ui.base.BaseFragment
import org.koin.androidx.viewmodel.ext.android.viewModel

class LibraryFragment : BaseFragment<FragmentLibraryBinding, LibraryViewModel>() {
    override val viewModel: LibraryViewModel by viewModel()
    override fun getViewBinding() = FragmentLibraryBinding.inflate(layoutInflater)
    private lateinit var adapter: LibraryAdapter

    override fun setupView() {
        adapter = LibraryAdapter(this)
        binding.rvShows.layoutManager = LinearLayoutManager(requireContext())
        binding.rvShows.adapter = adapter
    }

    override fun manageListeners() {
        binding.swipeLayout.setOnRefreshListener {
            viewModel.getShows()
        }
    }

    override fun refreshData() {
        viewModel.getShows()
    }

    override fun manageSubscriptions() {
        viewModel.shows.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Success -> {
                    binding.swipeLayout.isRefreshing = false
                    val items = it.data ?: listOf()
                    binding.loadingView.setSuccess()
                    adapter.addItems(items)
                }
                is Resource.Error -> binding.loadingView.setError()
                is Resource.Loading -> binding.loadingView.setLoading()
            }
        }
        viewModel.loading.observe(viewLifecycleOwner) {
            binding.swipeLayout.apply {
                if (isRefreshing) isRefreshing = it
            }
        }
    }
}
