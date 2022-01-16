package com.zc.bakamitai.ui.main

import android.os.Bundle
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.zc.bakamitai.R
import com.zc.bakamitai.databinding.ActivityMainBinding
import com.zc.bakamitai.ui.base.BaseActivity

class MainActivity : BaseActivity<ActivityMainBinding>() {
    override fun getViewBinding(): ActivityMainBinding = ActivityMainBinding.inflate(layoutInflater)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val navView: BottomNavigationView = binding.navView
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment_activity_main) as NavHostFragment
        val navController = navHostFragment.navController
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_schedule
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }
}
