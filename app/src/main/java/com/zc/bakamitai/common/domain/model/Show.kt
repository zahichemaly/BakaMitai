package com.zc.bakamitai.common.domain.model

/**
 * A SubsPlease show.
 *
 * @property coverUrl absolute cover URL, or `null` when the show has no cover yet.
 */
data class Show(val slug: ShowSlug, val title: String, val coverUrl: String?)
