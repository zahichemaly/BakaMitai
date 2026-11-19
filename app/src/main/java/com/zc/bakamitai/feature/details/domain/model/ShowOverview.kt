package com.zc.bakamitai.feature.details.domain.model

import com.zc.bakamitai.common.domain.model.Show

/**
 * What a show's page says about it.
 *
 * @property show the page's title and cover (which can be newer than the schedule's).
 * @property synopsis paragraphs separated by a blank line, or `null` when the page has none yet.
 * @property webUrl the page on subsplease.org, for sharing and opening in a browser.
 */
data class ShowOverview(val show: Show, val synopsis: String?, val webUrl: String)
