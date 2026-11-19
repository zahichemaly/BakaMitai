package com.zc.bakamitai.common.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import java.io.IOException

/**
 * [DataStore.data] that never fails on a read error: an `IOException` (a corrupt or unreadable file) emits empty
 * preferences, so readers get their defaults (best-practices §4.2). Any other exception is rethrown.
 */
fun DataStore<Preferences>.safeData(): Flow<Preferences> = data.catch { exception ->
    if (exception is IOException) emit(emptyPreferences()) else throw exception
}

/** The enum constant stored by name under [key]; `null` when nothing is stored or the name is unknown. */
inline fun <reified E : Enum<E>> Preferences.enumOrNull(key: Preferences.Key<String>): E? =
    this[key]?.let { name -> enumValues<E>().firstOrNull { it.name == name } }
