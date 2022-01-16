package com.zc.bakamitai.ui.base

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import com.zc.bakamitai.listeners.PageListener
import com.zc.bakamitai.ui.details.DetailsActivity
import com.zc.bakamitai.ui.dialog.LoadingDialog

abstract class BaseFragment<VBinding : ViewBinding, VM : BaseViewModel> : Fragment(), PageListener {
    private var _binding: VBinding? = null
    protected val binding: VBinding by lazy { _binding!! }
    protected abstract val viewModel: VM
    protected abstract fun getViewBinding(): VBinding
    protected val loadingDialog: LoadingDialog by lazy { LoadingDialog(requireContext()) }

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

    override fun onPageClicked(page: String) {
        val intent = Intent(requireActivity(), DetailsActivity::class.java)
        intent.putExtra(DetailsActivity.PAGE, page)
        startActivity(intent)
    }
}
