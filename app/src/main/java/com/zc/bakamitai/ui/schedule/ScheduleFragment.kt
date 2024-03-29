package com.zc.bakamitai.ui.schedule

import android.os.Bundle
import android.view.View
import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import com.zc.bakamitai.R
import com.zc.bakamitai.data.models.Resource
import com.zc.bakamitai.databinding.FragmentScheduleBinding
import com.zc.bakamitai.ui.base.BaseFragmentWithPrefs
import org.koin.androidx.viewmodel.ext.android.viewModel

class ScheduleFragment : BaseFragmentWithPrefs<FragmentScheduleBinding, ScheduleViewModel>() {
    override val viewModel: ScheduleViewModel by viewModel()
    override fun getViewBinding() = FragmentScheduleBinding.inflate(layoutInflater)
    private lateinit var adapter: SchedulePageAdapter
    private val onPageChangeCallback: ViewPager2.OnPageChangeCallback by lazy {
        object : ViewPager2.OnPageChangeCallback() {
            override fun onPageScrollStateChanged(state: Int) {
                toggleRefreshing(state == ViewPager.SCROLL_STATE_IDLE)
            }
        }
    }

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

    override fun manageListeners() {
        binding.swipeLayout.setOnRefreshListener {
            viewModel.getSchedule()
        }
        binding.viewPager.registerOnPageChangeCallback(onPageChangeCallback)
    }

    override fun refreshData() {
        viewModel.getSchedule()
    }

    override fun checkIfPrefsChanged(key: String): Boolean {
        return key == getString(R.string.key_calendar) || key == getString(R.string.key_time_format)
    }

    override fun updateSettings() {
        adapter.updateSettings()
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
        viewModel.loading.observe(viewLifecycleOwner) {
            binding.swipeLayout.apply {
                if (isRefreshing) isRefreshing = it
            }
        }
    }

    fun toggleRefreshing(enabled: Boolean) {
        binding.swipeLayout.isEnabled = enabled
    }

    override fun onDestroyView() {
        binding.viewPager.unregisterOnPageChangeCallback(onPageChangeCallback)
        super.onDestroyView()
    }
}
