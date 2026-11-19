package com.zc.bakamitai.feature.bookmarks.data.repository

import androidx.datastore.core.DataStoreFactory
import app.cash.turbine.test
import com.zc.bakamitai.common.data.network.appJson
import com.zc.bakamitai.common.domain.model.ShowTestData.show
import com.zc.bakamitai.feature.bookmarks.data.local.BookmarkListSerializer
import com.zc.bakamitai.feature.bookmarks.data.local.BookmarksLocalDataSource
import com.zc.bakamitai.feature.bookmarks.domain.model.Bookmark
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant

class BookmarkRepositoryImplTest {

    @get:Rule
    val folder = TemporaryFolder()

    private val bookmark = Bookmark(
        show = show("sousou-no-frieren", "Sousou no Frieren", "https://subsplease.org/a.jpg"),
        addedAt = Instant.fromEpochMilliseconds(1_795_000_000_000),
    )

    private fun TestScope.repository() = BookmarkRepositoryImpl(
        BookmarksLocalDataSource(
            DataStoreFactory.create(
                serializer = BookmarkListSerializer(appJson),
                scope = backgroundScope,
                produceFile = { File(folder.root, "bookmarks.json") },
            ),
        ),
    )

    @Test
    fun `toggle returns the new state, and observeBookmarks follows it`() = runTest {
        val repository = repository()

        repository.observeBookmarks().test {
            assertEquals(emptyList(), awaitItem())

            assertEquals(Result.success(true), repository.toggle(bookmark))
            assertEquals(listOf(bookmark), awaitItem())

            assertEquals(Result.success(false), repository.toggle(bookmark))
            assertEquals(emptyList(), awaitItem())
        }
    }
}
