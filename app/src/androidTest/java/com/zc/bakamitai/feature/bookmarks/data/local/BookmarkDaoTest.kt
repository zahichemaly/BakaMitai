package com.zc.bakamitai.feature.bookmarks.data.local

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import app.cash.turbine.test
import kotlinx.coroutines.async
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

@RunWith(AndroidJUnit4::class)
class BookmarkDaoTest {

    private lateinit var database: BookmarksDatabase
    private lateinit var dao: BookmarkDao

    private fun entity(slug: String, addedAt: Long) =
        BookmarkEntity(slug = slug, title = "Title $slug", coverUrl = null, addedAt = addedAt, trackingSince = addedAt)

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder<BookmarksDatabase>(context).build()
        dao = database.bookmarkDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertIgnoresASlugThatIsSavedAlready() = runTest {
        assertTrue(dao.insert(entity("frieren", 1)) > 0)
        assertEquals(-1, dao.insert(entity("frieren", 2)))
        assertEquals(1L, dao.get("frieren")?.addedAt)
        assertEquals(1, dao.count())
    }

    @Test
    fun observeAllIsNewestFirstAndEmitsOnEveryChange() = runTest {
        dao.observeAll().test {
            assertEquals(emptyList(), awaitItem())

            dao.insert(entity("old", 1))
            assertEquals(listOf("old"), awaitItem().map { it.slug })

            dao.insert(entity("new", 2))
            assertEquals(listOf("new", "old"), awaitItem().map { it.slug })

            dao.removeAndGet("new")
            assertEquals(listOf("old"), awaitItem().map { it.slug })
        }
    }

    @Test
    fun toggleAddsThenRemoves() = runTest {
        assertTrue(dao.toggle(entity("frieren", 1)))
        assertEquals(listOf("frieren"), dao.getAll().map { it.slug })

        assertEquals(false, dao.toggle(entity("frieren", 2)))
        assertEquals(emptyList(), dao.getAll())
    }

    @Test
    fun twoTogglesAtOnceLeaveTheTableConsistent() = runTest {
        val first = async { dao.toggle(entity("frieren", 1)) }
        val second = async { dao.toggle(entity("frieren", 2)) }

        assertEquals(setOf(true, false), setOf(first.await(), second.await()))
        assertEquals(0, dao.count())
    }

    @Test
    fun removeAndGetReturnsTheRemovedRowOrNull() = runTest {
        dao.insert(entity("frieren", 1))

        assertEquals("frieren", dao.removeAndGet("frieren")?.slug)
        assertNull(dao.removeAndGet("frieren"))
        assertEquals(0, dao.count())
    }

    @Test
    fun slotAndTrackingUpdatesChangeOnlyTheirColumnsAndSkipMissingSlugs() = runTest {
        dao.insert(entity("frieren", 1))

        val airings = dao.updateAirings(
            listOf(
                BookmarkAiringRow("frieren", 1, 720, "Asia/Tokyo", "Frieren", 100),
                BookmarkAiringRow("gone", null, null, null, null, 100),
            ),
        )
        val trackings = dao.updateTrackings(listOf(BookmarkTrackingRow("frieren", "08", 50, 60)))

        assertEquals(1, airings)
        assertEquals(1, trackings)
        assertEquals(
            entity("frieren", 1).copy(
                airingDay = 1,
                airingMinute = 720,
                airingZone = "Asia/Tokyo",
                airingTitle = "Frieren",
                airingCheckedAt = 100,
                lastSeenEpisode = "08",
                lastSeenReleasedAt = 50,
                resolvedThrough = 60,
            ),
            dao.get("frieren"),
        )
    }
}
