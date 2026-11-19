package com.zc.bakamitai.common.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import com.zc.bakamitai.BuildConfig
import com.zc.bakamitai.common.data.local.createSettingsDataStore
import com.zc.bakamitai.common.data.network.appJson
import com.zc.bakamitai.common.data.network.createHttpClient
import io.ktor.client.HttpClient
import io.ktor.client.plugins.logging.LogLevel
import kotlinx.serialization.json.Json
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single
import kotlin.time.Clock

/** The settings DataStore's file: `files/datastore/settings.preferences_pb`. Never rename it. */
private const val SETTINGS_DATA_STORE = "settings"

/**
 * Shared bindings. `@ComponentScan` registers the annotated classes in `common` (including the settings store);
 * the providers below are for types we don't own.
 */
@Module
@ComponentScan("com.zc.bakamitai.common")
class CommonModule {

    @Single
    fun json(): Json = appJson

    @Single
    fun httpClient(json: Json): HttpClient = createHttpClient(
        json = json,
        userAgent = "BakaMitai/${BuildConfig.VERSION_NAME} (Android)",
        logLevel = if (BuildConfig.DEBUG) LogLevel.INFO else LogLevel.NONE,
    )

    /** The current instant for "today" and time conversions; tests inject a fixed clock instead. */
    @Single
    fun clock(): Clock = Clock.System

    /** The one Preferences DataStore: the settings, and Bookmarks' sort (docs/6-settings.md §4.3). */
    @Single
    fun settingsDataStore(context: Context): DataStore<Preferences> =
        createSettingsDataStore { context.preferencesDataStoreFile(SETTINGS_DATA_STORE) }
}
