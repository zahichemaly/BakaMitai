package com.zc.bakamitai.common.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.IOException

/** A DataStore whose file can't be read or written: every read and write throws [IOException]. */
class FailingPreferencesDataStore : DataStore<Preferences> {

    override val data: Flow<Preferences> = flow { throw IOException("unreadable") }

    override suspend fun updateData(transform: suspend (t: Preferences) -> Preferences): Preferences =
        throw IOException("unwritable")
}
