package com.zc.bakamitai.ui.base

import android.content.SharedPreferences
import android.os.Bundle
import androidx.viewbinding.ViewBinding
import com.zc.bakamitai.utils.PreferenceUtil
import org.koin.android.ext.android.inject
import timber.log.Timber

abstract class BaseFragmentWithPrefs<VBinding : ViewBinding, VM : BaseViewModel> : BaseFragment<VBinding, VM>() {
    private var isPrefsChanged: Boolean = false
    protected val preferenceUtil: PreferenceUtil by inject()
    private val preferenceListener: SharedPreferences.OnSharedPreferenceChangeListener by lazy {
        SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            Timber.d("Preference value with key $key has changed")
            isPrefsChanged = checkIfPrefsChanged(key)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preferenceUtil.sharedPreferences.registerOnSharedPreferenceChangeListener(preferenceListener)
    }

    open fun updateSettings() {}
    open fun checkIfPrefsChanged(key: String): Boolean {
        return false
    }

    override fun onResume() {
        super.onResume()
        if (isPrefsChanged) {
            isPrefsChanged = false
            updateSettings()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        preferenceUtil.sharedPreferences.unregisterOnSharedPreferenceChangeListener(preferenceListener)
    }
}
