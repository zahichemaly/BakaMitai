package com.zc.bakamitai.ui.settings

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.zc.bakamitai.BuildConfig
import com.zc.bakamitai.R
import com.zc.bakamitai.data.Constants
import com.zc.bakamitai.utils.Theme

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, SettingsFragment())
                .commit()
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        private lateinit var versionPrefs: Preference
        private lateinit var aboutPrefs: Preference
        private lateinit var themePrefs: Preference

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
            versionPrefs = findPreference(getString(R.string.key_version))!!
            aboutPrefs = findPreference(getString(R.string.key_about))!!
            themePrefs = findPreference(getString(R.string.key_theme))!!
            versionPrefs.setOnPreferenceClickListener {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                intent.data = Uri.parse("package:" + BuildConfig.APPLICATION_ID)
                startActivity(intent)
                true
            }
            aboutPrefs.setOnPreferenceClickListener {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(Constants.Common.GITHUB_URL))
                startActivity(intent)
                true
            }
            themePrefs.setOnPreferenceChangeListener { _, newValue ->
                when (newValue) {
                    Theme.Light.name -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                    Theme.Dark.name -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                    Theme.System.name -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                }
                true
            }
        }
    }
}
