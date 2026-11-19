package com.zc.bakamitai.feature.bookmarks.domain.usecase

import com.zc.bakamitai.common.domain.model.ShowSlug
import com.zc.bakamitai.feature.bookmarks.domain.model.AiringSlot
import com.zc.bakamitai.feature.bookmarks.domain.model.ReleaseWatch
import com.zc.bakamitai.feature.bookmarks.domain.repository.BookmarkRepository
import com.zc.bakamitai.feature.schedule.domain.model.WeeklyTemplate
import com.zc.bakamitai.feature.schedule.domain.usecase.GetWeeklyTemplateUseCase
import kotlinx.datetime.DayOfWeek
import org.koin.core.annotation.Factory
import kotlin.time.Clock
import kotlin.time.Instant

/**
 * Refreshes every bookmark's weekly slot from the weekly schedule (through Schedule's cache), when one is missing
 * or older than a day, or when [invoke]'s `forceRefresh`. A slug that isn't on the schedule gets no slot.
 * Returns whether any slot changed.
 */
@Factory
class UpdateAiringSlotsUseCase(
    private val repository: BookmarkRepository,
    private val getWeeklyTemplate: GetWeeklyTemplateUseCase,
    private val clock: Clock,
) {

    suspend operator fun invoke(forceRefresh: Boolean): Result<Boolean> = repository.getReleaseWatches().fold(
        onSuccess = { watches -> refresh(watches, forceRefresh) },
        onFailure = { Result.failure(it) },
    )

    private suspend fun refresh(watches: List<ReleaseWatch>, forceRefresh: Boolean): Result<Boolean> {
        val now = clock.now()
        val isNeeded = watches.isNotEmpty() && (forceRefresh || watches.any { it.isAiringStale(now) })
        return if (isNeeded) {
            getWeeklyTemplate(forceRefresh).fold(
                onSuccess = { template -> store(watches, template, now) },
                onFailure = { Result.failure(it) },
            )
        } else {
            Result.success(false)
        }
    }

    /** Stores each bookmark's slot from [template]; returns whether any changed. */
    private suspend fun store(watches: List<ReleaseWatch>, template: WeeklyTemplate, now: Instant): Result<Boolean> {
        val slotsBySlug = template.slotsBySlug()
        val slots = watches.associate { it.show.slug to slotsBySlug[it.show.slug] }
        val changed = watches.any { it.airing != slots[it.show.slug] }
        return repository.updateAiringSlots(slots, checkedAt = now).map { changed }
    }

    /** Each show's slot; a show listed twice keeps its first, Monday to Sunday. */
    private fun WeeklyTemplate.slotsBySlug(): Map<ShowSlug, AiringSlot> = buildMap {
        DayOfWeek.entries.forEach { day ->
            showsByDay[day].orEmpty().forEach { scheduled ->
                if (scheduled.show.slug !in this) {
                    put(
                        scheduled.show.slug,
                        AiringSlot(
                            dayOfWeek = day,
                            time = scheduled.time,
                            timeZone = timeZone,
                            releaseTitle = scheduled.show.title,
                        ),
                    )
                }
            }
        }
    }
}
