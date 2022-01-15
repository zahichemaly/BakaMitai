package com.zc.bakamitai.ui.home

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.zc.bakamitai.data.models.Resource
import com.zc.bakamitai.data.models.dtos.EntryDto
import com.zc.bakamitai.databinding.FragmentHomeBinding
import com.zc.bakamitai.ui.base.BaseFragment
import org.koin.android.ext.android.inject
import timber.log.Timber

class HomeFragment : BaseFragment<FragmentHomeBinding, HomeViewModel>() {
    override val viewModel: HomeViewModel by inject()
    override fun getViewBinding() = FragmentHomeBinding.inflate(layoutInflater)
    private lateinit var adapter: EntryAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.getLatest()
    }

    override fun manageSubscriptions() {
        viewModel.latestEntries.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Success -> {
                    Timber.d("Finished getting latest entries")
                    setupAdapter(it.data!!)
                }
                is Resource.Error -> {

                }
                is Resource.Loading -> {

                }
            }
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) {
            Timber.e(it)
        }
    }

    private fun setupAdapter(data: List<EntryDto>) {
        adapter = EntryAdapter(data)
        val layoutManager = LinearLayoutManager(requireContext())
        binding.rvEntries.layoutManager = layoutManager
        binding.rvEntries.adapter = adapter
    }
}
