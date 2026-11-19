package com.zc.bakamitai.feature.bookmarks.data.local

import androidx.datastore.core.DataStoreFactory
import com.zc.bakamitai.common.data.network.appJson
import com.zc.bakamitai.feature.bookmarks.data.dto.BookmarkDTO
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BookmarksLocalDataSourceTest {

    @get:Rule
    val folder = TemporaryFolder()

    private val sora = BookmarkDTO("sora-wa-akai-kawa-no-hotori", "Sora wa Akai Kawa no Hotori", null, 1)
    private val frieren = BookmarkDTO("sousou-no-frieren", "Sousou no Frieren", "https://x/y.jpg", 2)

    /** A path that doesn't exist yet: the store creates it on the first write. */
    private val file: File get() = File(folder.root, "datastore/bookmarks.json")

    private fun TestScope.dataSource() = BookmarksLocalDataSource(
        DataStoreFactory.create(
            serializer = BookmarkListSerializer(appJson),
            scope = backgroundScope,
            produceFile = { file },
        ),
    )

    @Test
    fun `starts with no bookmarks`() = runTest {
        assertEquals(emptyList(), dataSource().bookmarks.first())
    }

    @Test
    fun `toggle adds a show, then removes it`() = runTest {
        val dataSource = dataSource()

        assertTrue(dataSource.toggle(sora))
        assertEquals(listOf(sora), dataSource.bookmarks.first())

        assertFalse(dataSource.toggle(sora.copy(addedAtEpochMillis = 99)))
        assertEquals(emptyList(), dataSource.bookmarks.first())
    }

    @Test
    fun `shows are kept in the order they were added, and written to the file`() = runTest {
        val dataSource = dataSource()

        dataSource.toggle(frieren)
        dataSource.toggle(sora)

        assertEquals(listOf(frieren, sora), dataSource.bookmarks.first())
        assertTrue(file.readText().contains("\"sousou-no-frieren\""))
    }

    @Test
    fun `a slug stored twice is read once`() = runTest {
        file.parentFile?.mkdirs()
        file.writeText(
            """{"bookmarks":[""" +
                """{"slug":"sousou-no-frieren","title":"Sousou no Frieren","added_at_epoch_millis":2},""" +
                """{"slug":"sousou-no-frieren","title":"Frieren","added_at_epoch_millis":3}]}""",
        )

        val bookmarks = dataSource().bookmarks.first()

        assertEquals(listOf("Sousou no Frieren"), bookmarks.map { it.title })
    }
}
