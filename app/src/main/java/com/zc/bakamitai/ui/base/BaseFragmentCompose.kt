package com.zc.bakamitai.ui.base

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.SearchView
import androidx.fragment.app.Fragment
import com.zc.bakamitai.R
import com.zc.bakamitai.data.Constants
import com.zc.bakamitai.listeners.PageListener
import com.zc.bakamitai.ui.details.DetailsActivity

/**
 * Created by Zahi Chemaly on 04/12/2025.
 */
abstract class BaseFragmentCompose : Fragment(), PageListener {
    protected lateinit var searchView: SearchView
    open var isSearchable: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
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
