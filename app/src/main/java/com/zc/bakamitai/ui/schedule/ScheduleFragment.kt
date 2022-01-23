package com.zc.bakamitai.ui.schedule

import android.os.Bundle
import android.view.View
import com.google.android.material.tabs.TabLayoutMediator
import com.zc.bakamitai.data.models.Resource
import com.zc.bakamitai.databinding.FragmentScheduleBinding
import com.zc.bakamitai.ui.base.BaseFragment
import com.zc.bakamitai.utils.PreferenceUtil
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class ScheduleFragment : BaseFragment<FragmentScheduleBinding, ScheduleViewModel>() {
    override val viewModel: ScheduleViewModel by viewModel()
    override fun getViewBinding() = FragmentScheduleBinding.inflate(layoutInflater)
    private lateinit var adapter: SchedulePageAdapter
    private val preferenceUtil: PreferenceUtil by inject()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.getSchedule()
    }

    override fun setupView() {
        adapter = SchedulePageAdapter(this, preferenceUtil)
        binding.viewPager.adapter = adapter
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            val item = adapter.getItem(position)
            item?.let {
                tab.text = it.day
            }
        }.attach()
    }

    override fun manageSubscriptions() {
        viewModel.schedule.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Success -> {
                    binding.loadingView.setSuccess()
                    adapter.addItems(it.data!!)
                }
                is Resource.Error -> binding.loadingView.setError()
                is Resource.Loading -> binding.loadingView.setLoading()
            }
        }
    }
}
