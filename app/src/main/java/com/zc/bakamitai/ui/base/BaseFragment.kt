package com.zc.bakamitai.ui.base

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import com.zc.bakamitai.R
import com.zc.bakamitai.listeners.PageListener
import com.zc.bakamitai.ui.details.DetailsActivity

abstract class BaseFragment<VBinding : ViewBinding, VM : BaseViewModel> : Fragment(), PageListener {
    private var _binding: VBinding? = null
    protected val binding: VBinding by lazy { _binding!! }
    protected abstract val viewModel: VM
    protected abstract fun getViewBinding(): VBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

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
    open fun refreshData() {}

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onPageClicked(page: String) {
        val intent = Intent(requireActivity(), DetailsActivity::class.java)
        intent.putExtra(DetailsActivity.PAGE, page)
        startActivity(intent)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_refresh -> {
                refreshData()
                true
            }
            R.id.action_search -> {
                //TODO handle search
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
