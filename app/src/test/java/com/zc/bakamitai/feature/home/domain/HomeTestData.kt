package com.zc.bakamitai.feature.home.domain

import com.zc.bakamitai.feature.home.domain.model.Release
import com.zc.bakamitai.feature.home.domain.model.ScheduledShow
import com.zc.bakamitai.feature.home.domain.model.Show
import com.zc.bakamitai.feature.home.domain.model.ShowSlug
import kotlinx.datetime.LocalTime
import kotlin.time.Instant

/** Builders for Home's domain models in tests. */
object HomeTestData {

    fun show(slug: String, title: String = slug, coverUrl: String? = null) =
        Show(slug = ShowSlug(slug), title = title, coverUrl = coverUrl)

    fun scheduled(slug: String, time: LocalTime, aired: Boolean? = false, title: String = slug) =
        ScheduledShow(show = show(slug, title), time = time, aired = aired)

    fun release(slug: String, episode: String, releasedAt: String, title: String = slug) =
        Release(show = show(slug, title), episode = episode, releasedAt = Instant.parse(releasedAt))
}
