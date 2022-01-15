package com.zc.bakamitai.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.zc.bakamitai.databinding.FragmentDashboardBinding
import org.koin.android.ext.android.inject
import timber.log.Timber

class DashboardFragment : Fragment() {

    private val dashboardViewModel: DashboardViewModel by inject()
    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textDashboard
        dashboardViewModel.text.observe(viewLifecycleOwner, Observer {
            textView.text = it
        })
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        manageSubscriptions()
        dashboardViewModel.getLatest()
    }

    private fun manageSubscriptions() {
        dashboardViewModel.latestAnime.observe(viewLifecycleOwner) {
            Timber.d(it.body().toString())
        }

        dashboardViewModel.errorMessage.observe(viewLifecycleOwner) {
            Timber.e(it)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
