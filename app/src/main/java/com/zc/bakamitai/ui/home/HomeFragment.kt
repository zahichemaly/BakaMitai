package com.zc.bakamitai.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.zc.bakamitai.data.models.Resource
import com.zc.bakamitai.data.models.dtos.EntryDto
import com.zc.bakamitai.databinding.FragmentHomeBinding
import org.koin.android.ext.android.inject
import timber.log.Timber

class HomeFragment : Fragment() {
    private val viewModel: HomeViewModel by inject()
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: EntryAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return _binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        manageSubscriptions()
        viewModel.getLatest()
    }

    private fun manageSubscriptions() {
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupAdapter(data: List<EntryDto>) {
        adapter = EntryAdapter(data)
        val layoutManager = LinearLayoutManager(requireContext())
        binding.rvEntries.layoutManager = layoutManager
        binding.rvEntries.adapter = adapter
    }
}
