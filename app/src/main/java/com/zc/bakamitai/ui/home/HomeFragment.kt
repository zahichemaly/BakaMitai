package com.zc.bakamitai.ui.home

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.zc.bakamitai.data.models.Resource
import com.zc.bakamitai.databinding.FragmentHomeBinding
import com.zc.bakamitai.extensions.hide
import com.zc.bakamitai.extensions.invisible
import com.zc.bakamitai.extensions.show
import com.zc.bakamitai.ui.base.BaseFragment
import org.koin.android.ext.android.inject
import timber.log.Timber

class HomeFragment : BaseFragment<FragmentHomeBinding, HomeViewModel>() {
    override val viewModel: HomeViewModel by inject()
    override fun getViewBinding() = FragmentHomeBinding.inflate(layoutInflater)
    private lateinit var entryAdapter: EntryAdapter
    private lateinit var entryGridAdapter: EntryGridAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.getLatest()
        viewModel.getTodaySchedule()
    }

    override fun setupView() {
        entryAdapter = EntryAdapter()
        binding.rvEntries.layoutManager = LinearLayoutManager(requireContext())
        binding.rvEntries.adapter = entryAdapter

        entryGridAdapter = EntryGridAdapter()
        binding.rvEntriesToday.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.rvEntriesToday.adapter = entryGridAdapter
    }

    override fun manageSubscriptions() {
        viewModel.latestEntries.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Success -> {
                    Timber.d("Finished getting latest entries")
                    binding.pbEntries.hide()
                    binding.rvEntries.show()
                    entryAdapter.addItems(it.data!!)
                }
                is Resource.Error -> {
                    Timber.e("Error getting latest entries: ${it.errorResponse}")
                    binding.pbEntries.invisible()
                }
                is Resource.Loading -> {
                    binding.pbEntries.show()
                }
            }
        }

        viewModel.todayEntries.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Success -> {
                    Timber.d("Finished getting today entries")
                    binding.pbEntriesToday.hide()
                    binding.rvEntriesToday.show()
                    entryGridAdapter.addItems(it.data!!)
                }
                is Resource.Error -> {
                    Timber.e("Error getting today entries: ${it.errorResponse}")
                    binding.pbEntriesToday.invisible()
                }
                is Resource.Loading -> {
                    binding.pbEntriesToday.show()
                }
            }
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) {
            Timber.e(it)
        }
    }
}
