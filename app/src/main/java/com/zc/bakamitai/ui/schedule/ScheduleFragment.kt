package com.zc.bakamitai.ui.schedule

import android.os.Bundle
import android.view.View
import com.google.android.material.tabs.TabLayoutMediator
import com.zc.bakamitai.data.models.Resource
import com.zc.bakamitai.databinding.FragmentScheduleBinding
import com.zc.bakamitai.ui.base.BaseFragment
import org.koin.android.ext.android.inject
import timber.log.Timber

class ScheduleFragment : BaseFragment<FragmentScheduleBinding, ScheduleViewModel>() {
    override val viewModel: ScheduleViewModel by inject()
    override fun getViewBinding() = FragmentScheduleBinding.inflate(layoutInflater)
    private lateinit var adapter: SchedulePageAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.getSchedule()
    }

    override fun setupView() {
        adapter = SchedulePageAdapter()
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
                    Timber.d("Finished getting schedule")
                    loadingDialog.hide()
                    adapter.addItems(it.data!!)
                }
                is Resource.Error -> {
                    Timber.e("Error getting schedule")
                    loadingDialog.hide()
                }
                is Resource.Loading -> {
                    loadingDialog.show()
                }
            }
        }
    }
}
