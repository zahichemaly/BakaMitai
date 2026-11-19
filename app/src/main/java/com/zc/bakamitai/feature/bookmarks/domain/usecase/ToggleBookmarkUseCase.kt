package com.zc.bakamitai.feature.bookmarks.domain.usecase

import com.zc.bakamitai.common.domain.model.Show
import com.zc.bakamitai.feature.bookmarks.domain.model.Bookmark
import com.zc.bakamitai.feature.bookmarks.domain.model.BookmarkToggle
import com.zc.bakamitai.feature.bookmarks.domain.repository.BookmarkRepository
import com.zc.bakamitai.feature.bookmarks.domain.repository.ReleaseCheckScheduler
import org.koin.core.annotation.Factory
import kotlin.time.Clock

/**
 * Saves a show as of now, or removes it when it's saved, then asks for a release check (the new bookmark needs its
 * slot and its first check; a removal may end the checks).
 */
@Factory
class ToggleBookmarkUseCase(
    private val repository: BookmarkRepository,
    private val scheduler: ReleaseCheckScheduler,
    private val clock: Clock,
) {

    suspend operator fun invoke(show: Show): Result<BookmarkToggle> =
        repository.toggle(Bookmark(show = show, addedAt = clock.now()))
            .onSuccess { scheduler.requestCheck() }
}
