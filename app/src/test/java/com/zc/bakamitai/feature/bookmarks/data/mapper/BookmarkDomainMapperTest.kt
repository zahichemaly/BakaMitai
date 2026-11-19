package com.zc.bakamitai.feature.bookmarks.data.mapper

import com.zc.bakamitai.common.domain.model.ShowTestData.show
import com.zc.bakamitai.feature.bookmarks.data.dto.BookmarkDTO
import com.zc.bakamitai.feature.bookmarks.domain.model.Bookmark
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant

class BookmarkDomainMapperTest {

    @Test
    fun `maps every field, the instant to epoch millis`() {
        val bookmark = Bookmark(
            show = show("sousou-no-frieren", "Sousou no Frieren", "https://subsplease.org/a.jpg"),
            addedAt = Instant.parse("2026-11-19T08:00:00Z"),
        )

        assertEquals(
            BookmarkDTO(
                slug = "sousou-no-frieren",
                title = "Sousou no Frieren",
                coverUrl = "https://subsplease.org/a.jpg",
                addedAtEpochMillis = Instant.parse("2026-11-19T08:00:00Z").toEpochMilliseconds(),
            ),
            bookmark.toDTO(),
        )
    }

    @Test
    fun `a null cover stays null, and the mapping round-trips`() {
        val bookmark = Bookmark(show = show("one-piece", "One Piece"), addedAt = Instant.fromEpochMilliseconds(42))

        assertEquals(null, bookmark.toDTO().coverUrl)
        assertEquals(bookmark, bookmark.toDTO().toDomain())
    }
}
