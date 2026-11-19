package com.zc.bakamitai.feature.bookmarks.data.mapper

import com.zc.bakamitai.common.domain.model.ShowTestData.show
import com.zc.bakamitai.feature.bookmarks.data.dto.BookmarkDTO
import com.zc.bakamitai.feature.bookmarks.domain.model.Bookmark
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Instant

class BookmarkDTOMapperTest {

    private val dto = BookmarkDTO(
        slug = "sora-wa-akai-kawa-no-hotori",
        title = "Sora wa Akai Kawa no Hotori",
        coverUrl = "https://subsplease.org/wp-content/uploads/2026/07/158138.jpg",
        addedAtEpochMillis = 1_795_000_000_000,
    )

    @Test
    fun `maps every field, epoch millis to an instant`() {
        assertEquals(
            Bookmark(
                show = show(
                    slug = "sora-wa-akai-kawa-no-hotori",
                    title = "Sora wa Akai Kawa no Hotori",
                    coverUrl = "https://subsplease.org/wp-content/uploads/2026/07/158138.jpg",
                ),
                addedAt = Instant.fromEpochMilliseconds(1_795_000_000_000),
            ),
            dto.toDomain(),
        )
    }

    @Test
    fun `a null cover stays null`() {
        assertNull(dto.copy(coverUrl = null).toDomain()?.show?.coverUrl)
    }

    @Test
    fun `a blank slug or title is dropped`() {
        assertNull(dto.copy(slug = " ").toDomain())
        assertNull(dto.copy(title = "").toDomain())
    }
}
