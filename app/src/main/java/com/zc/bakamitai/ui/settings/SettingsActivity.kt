package com.zc.bakamitai.ui.settings

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.zc.bakamitai.BuildConfig
import com.zc.bakamitai.R

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

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
            versionPrefs = findPreference(getString(R.string.key_version))!!
            aboutPrefs = findPreference(getString(R.string.key_about))!!
            versionPrefs.setOnPreferenceClickListener {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                intent.data = Uri.parse("package:" + BuildConfig.APPLICATION_ID)
                startActivity(intent)
                true
            }
            aboutPrefs.setOnPreferenceClickListener {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/zahichemaly"))
                startActivity(intent)
                true
            }
        }
    }
}
