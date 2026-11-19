package com.zc.bakamitai.feature.bookmarks.data.local

import androidx.datastore.core.CorruptionException
import com.zc.bakamitai.common.data.network.appJson
import com.zc.bakamitai.feature.bookmarks.data.dto.BookmarkDTO
import com.zc.bakamitai.feature.bookmarks.data.dto.BookmarkListDTO
import kotlinx.coroutines.test.runTest
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class BookmarkListSerializerTest {

    private val serializer = BookmarkListSerializer(appJson)

    private val list = BookmarkListDTO(
        bookmarks = listOf(
            BookmarkDTO(
                slug = "sora-wa-akai-kawa-no-hotori",
                title = "Sora wa Akai Kawa no Hotori",
                coverUrl = "https://subsplease.org/wp-content/uploads/2026/07/158138.jpg",
                addedAtEpochMillis = 1_795_000_000_000,
            ),
            BookmarkDTO(slug = "one-piece", title = "One Piece", coverUrl = null, addedAtEpochMillis = 1),
        ),
    )

    private suspend fun read(text: String) = serializer.readFrom(ByteArrayInputStream(text.encodeToByteArray()))

    @Test
    fun `a list round-trips`() = runTest {
        val output = ByteArrayOutputStream()
        serializer.writeTo(list, output)

        assertEquals(list, serializer.readFrom(ByteArrayInputStream(output.toByteArray())))
    }

    @Test
    fun `the file uses the documented keys`() = runTest {
        val output = ByteArrayOutputStream()
        serializer.writeTo(list.copy(bookmarks = list.bookmarks.take(1)), output)

        assertEquals(
            """{"bookmarks":[{"slug":"sora-wa-akai-kawa-no-hotori","title":"Sora wa Akai Kawa no Hotori",""" +
                """"cover_url":"https://subsplease.org/wp-content/uploads/2026/07/158138.jpg",""" +
                """"added_at_epoch_millis":1795000000000}]}""",
            output.toByteArray().decodeToString(),
        )
    }

    @Test
    fun `an empty or blank file is no bookmarks`() = runTest {
        assertEquals(BookmarkListDTO(emptyList()), read(""))
        assertEquals(BookmarkListDTO(emptyList()), read("  \n"))
        assertEquals(BookmarkListDTO(emptyList()), serializer.defaultValue)
    }

    @Test
    fun `malformed JSON is a corruption`() = runTest {
        assertFailsWith<CorruptionException> { read("{\"bookmarks\":[") }
        assertFailsWith<CorruptionException> { read("""{"bookmarks":[{"slug":"x"}]}""") }
        assertFailsWith<CorruptionException> { read("[]") }
    }

    @Test
    fun `unknown keys are ignored`() = runTest {
        val decoded = read(
            """{"version":2,"bookmarks":[{"slug":"one-piece","title":"One Piece","added_at_epoch_millis":1,"x":0}]}""",
        )

        assertEquals(BookmarkListDTO(listOf(list.bookmarks[1])), decoded)
    }
}
