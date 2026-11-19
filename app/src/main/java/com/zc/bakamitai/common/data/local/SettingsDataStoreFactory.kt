package com.zc.bakamitai.common.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import java.io.File

/**
 * The app's Preferences DataStore (settings and the Bookmarks sort) in the file [produceFile] returns. A corrupt file
 * is replaced by empty preferences once, so reads fall back to the defaults instead of failing every time.
 */
fun createSettingsDataStore(produceFile: () -> File): DataStore<Preferences> = PreferenceDataStoreFactory.create(
    corruptionHandler = ReplaceFileCorruptionHandler { emptyPreferences() },
    produceFile = produceFile,
)
