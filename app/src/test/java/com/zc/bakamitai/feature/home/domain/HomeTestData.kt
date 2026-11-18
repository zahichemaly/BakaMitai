package com.zc.bakamitai.feature.home.domain

import com.zc.bakamitai.common.domain.model.ShowTestData.show
import com.zc.bakamitai.feature.home.domain.model.Release
import kotlin.time.Instant

/** Builders for Home's domain models in tests. Shows come from `ShowTestData`. */
object HomeTestData {

    fun release(slug: String, episode: String, releasedAt: String, title: String = slug) =
        Release(show = show(slug, title), episode = episode, releasedAt = Instant.parse(releasedAt))
}
