package com.zc.bakamitai.feature.bookmarks.domain.usecase

import com.zc.bakamitai.common.domain.model.Show
import com.zc.bakamitai.feature.bookmarks.domain.model.Bookmark
import com.zc.bakamitai.feature.bookmarks.domain.repository.BookmarkRepository
import org.koin.core.annotation.Factory
import kotlin.time.Clock

/** Saves a show as of now, or removes it when it's saved. Returns whether it's saved afterwards. */
@Factory
class ToggleBookmarkUseCase(private val repository: BookmarkRepository, private val clock: Clock) {

    suspend operator fun invoke(show: Show): Result<Boolean> =
        repository.toggle(Bookmark(show = show, addedAt = clock.now()))
}
