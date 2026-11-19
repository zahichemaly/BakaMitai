package com.zc.bakamitai.common.data.local

import androidx.datastore.preferences.core.emptyPreferences
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals

class SettingsDataStoreFactoryTest {

    @get:Rule
    val tmp = TemporaryFolder()

    @Test
    fun `a corrupt file is replaced by empty preferences`() = runTest {
        val file = File(tmp.root, "settings.preferences_pb").apply { writeText("this is not a preferences file") }

        assertEquals(emptyPreferences(), createSettingsDataStore { file }.data.first())
    }
}
