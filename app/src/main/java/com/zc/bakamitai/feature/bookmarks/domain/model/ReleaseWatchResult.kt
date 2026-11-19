package com.zc.bakamitai.feature.bookmarks.domain.model

/** One bookmark after a check: its updated [watch], and the [alert] to post, if any. */
data class ReleaseWatchResult(val watch: ReleaseWatch, val alert: EpisodeAlert?)
