package com.zc.bakamitai.feature.details.domain

import com.zc.bakamitai.common.domain.model.ShowTestData.show
import com.zc.bakamitai.feature.details.domain.model.Download
import com.zc.bakamitai.feature.details.domain.model.Episode
import com.zc.bakamitai.feature.details.domain.model.Resolution
import com.zc.bakamitai.feature.details.domain.model.ShowEpisodes
import com.zc.bakamitai.feature.details.domain.model.ShowOverview
import kotlinx.datetime.TimeZone
import kotlin.time.Instant

/** Builders for the Details domain models in tests. */
object DetailsTestData {

    val newYork: TimeZone = TimeZone.of("America/New_York")

    fun overview(
        slug: String = "sora-wa-akai-kawa-no-hotori",
        title: String = "Sora wa Akai Kawa no Hotori",
        coverUrl: String? = "https://subsplease.org/wp-content/uploads/2026/07/158138.jpg",
        synopsis: String? = "A synopsis.",
    ) = ShowOverview(
        show = show(slug = slug, title = title, coverUrl = coverUrl),
        synopsis = synopsis,
        webUrl = "https://subsplease.org/shows/$slug/",
    )

    fun download(resolution: Resolution, height: Int, label: String = "20") = Download(
        resolution = resolution,
        height = height,
        magnetUri = "magnet:?xt=urn:btih:$label-$height",
        torrentUrl = "https://nyaa.si/view/$label$height/torrent",
        xdccUrl = "https://subsplease.org/xdcc/?search=$label+$height",
    )

    fun episode(
        label: String,
        releasedAt: String = "2026-11-17T17:38:52Z",
        downloads: List<Download> = listOf(
            download(Resolution.SD, 480, label),
            download(Resolution.HD, 720, label),
            download(Resolution.FHD, 1080, label),
        ),
    ) = Episode(label = label, releasedAt = Instant.parse(releasedAt), downloads = downloads)

    fun episodes(
        episodes: List<Episode> = emptyList(),
        batches: List<Episode> = emptyList(),
        isTruncated: Boolean = false,
        timeZone: TimeZone = newYork,
    ) = ShowEpisodes(episodes = episodes, batches = batches, isTruncated = isTruncated, timeZone = timeZone)
}
