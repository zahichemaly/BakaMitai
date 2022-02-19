package com.zc.bakamitai.ui.base

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import com.zc.bakamitai.R
import com.zc.bakamitai.data.Constants
import com.zc.bakamitai.listeners.PageListener
import com.zc.bakamitai.ui.details.DetailsActivity


abstract class BaseFragment<VBinding : ViewBinding, VM : BaseViewModel> : Fragment(), PageListener {
    private var _binding: VBinding? = null
    protected val binding: VBinding get() = _binding!!
    protected abstract val viewModel: VM
    protected abstract fun getViewBinding(): VBinding
    protected lateinit var searchView: SearchView
    open var isSearchable: Boolean = false

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
        intent.putExtra(Constants.Intent.PAGE, page)
        startActivity(intent)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        val searchMenuItem = menu.findItem(R.id.action_search)
        searchMenuItem?.apply {
            isVisible = isSearchable
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        // Associate searchable configuration with the SearchView
        val searchManager = requireActivity().getSystemService(Context.SEARCH_SERVICE) as SearchManager
        searchView = menu.findItem(R.id.action_search).actionView as SearchView
        searchView.setSearchableInfo(searchManager.getSearchableInfo(requireActivity().componentName))
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_refresh -> {
                refreshData()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
