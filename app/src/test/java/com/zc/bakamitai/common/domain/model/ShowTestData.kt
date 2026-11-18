package com.zc.bakamitai.common.domain.model

import kotlinx.datetime.LocalTime

/** Builders for the shared show models in tests. */
object ShowTestData {

    fun show(slug: String, title: String = slug, coverUrl: String? = null) =
        Show(slug = ShowSlug(slug), title = title, coverUrl = coverUrl)

    fun scheduled(slug: String, time: LocalTime, aired: Boolean? = false, title: String = slug) =
        ScheduledShow(show = show(slug, title), time = time, aired = aired)
}
