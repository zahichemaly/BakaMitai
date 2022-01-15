package com.zc.bakamitai.ui.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding

abstract class BaseFragment<VBinding : ViewBinding, VM : BaseViewModel> : Fragment() {
    protected var _binding: VBinding? = null
    protected val binding: VBinding by lazy { _binding!! }
    protected abstract val viewModel: VM
    protected abstract fun getViewBinding(): VBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = getViewBinding()
        return _binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView()
        manageSubscriptions()
        manageListeners()
    }

    open fun setupView() {}
    open fun manageSubscriptions() {}
    open fun manageListeners() {}

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
