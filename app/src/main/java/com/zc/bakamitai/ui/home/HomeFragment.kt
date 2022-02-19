package com.zc.bakamitai.ui.home

import androidx.recyclerview.widget.LinearLayoutManager
import com.zc.bakamitai.R
import com.zc.bakamitai.data.models.Resource
import com.zc.bakamitai.databinding.FragmentHomeBinding
import com.zc.bakamitai.extensions.hide
import com.zc.bakamitai.extensions.show
import com.zc.bakamitai.listeners.PageListener
import com.zc.bakamitai.ui.base.BaseFragmentWithPrefs
import org.koin.androidx.viewmodel.ext.android.viewModel

class HomeFragment : BaseFragmentWithPrefs<FragmentHomeBinding, HomeViewModel>(), PageListener {
    override val viewModel: HomeViewModel by viewModel()
    override fun getViewBinding() = FragmentHomeBinding.inflate(layoutInflater)
    private lateinit var entryAdapter: EntryAdapter
    private lateinit var entryGridAdapter: EntryGridAdapter

    override fun setupView() {
        entryAdapter = EntryAdapter(this, preferenceUtil)
        binding.rvEntries.layoutManager = LinearLayoutManager(requireContext())
        binding.rvEntries.adapter = entryAdapter

        entryGridAdapter = EntryGridAdapter(this, preferenceUtil)
        binding.rvEntriesToday.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.rvEntriesToday.adapter = entryGridAdapter
    }

    override fun manageListeners() {
        binding.swipeLayout.setOnRefreshListener {
            viewModel.refreshLatest()
        }
    }

    override fun refreshData() {
        viewModel.refreshLatest()
    }

    override fun checkIfPrefsChanged(key: String): Boolean {
        return key == getString(R.string.key_time_format)
    }

    override fun updateSettings() {
        entryAdapter.updateSettings()
        entryGridAdapter.updateSettings()
    }

    override fun manageSubscriptions() {
        viewModel.latestEntries.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Success -> {
                    binding.lvEntries.setSuccess()
                    binding.rvEntries.show()
                    entryAdapter.addItems(it.data!!)
                }
                is Resource.Error -> binding.lvEntries.setError()
                is Resource.Loading -> {
                    binding.rvEntries.hide()
                    binding.lvEntries.setLoading()
                }
            }
        }

        viewModel.todayEntries.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Success -> {
                    binding.lvToday.setSuccess()
                    binding.rvEntriesToday.show()
                    entryGridAdapter.addItems(it.data!!)
                }
                is Resource.Error -> binding.lvToday.setError()
                is Resource.Loading -> {
                    binding.rvEntriesToday.hide()
                    binding.lvToday.setLoading()
                }
            }
        }
        viewModel.loadingAll.observe(viewLifecycleOwner) {
            binding.swipeLayout.apply {
                if (isRefreshing) isRefreshing = it
            }
        }
    }
}
