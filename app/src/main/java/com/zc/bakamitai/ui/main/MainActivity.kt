package com.zc.bakamitai.ui.main

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.SearchView
import androidx.activity.compose.setContent
import com.zc.bakamitai.R
import com.zc.bakamitai.databinding.ActivityMainBinding
import com.zc.bakamitai.ui.base.BaseActivity
import com.zc.bakamitai.ui.settings.SettingsActivity

class MainActivity : BaseActivity<ActivityMainBinding>() {
    override fun getViewBinding() = ActivityMainBinding.inflate(layoutInflater)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setSupportActionBar(findViewById(R.id.toolbar))
        setContent {
            MainScreen()
        }
    }

//    override fun onCreateOptionsMenu(menu: Menu): Boolean {
//        menuInflater.inflate(R.menu.action_bar_menu, menu)
//        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
//        (menu.findItem(R.id.action_search).actionView as SearchView).apply {
//            setSearchableInfo(searchManager.getSearchableInfo(componentName))
//        }
//        return true
//    }
//
//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        return when (item.itemId) {
//            R.id.action_settings -> {
//                val intent = Intent(this, SettingsActivity::class.java)
//                startActivity(intent)
//                true
//            }
//
//            else -> super.onOptionsItemSelected(item)
//        }
//    }
}
