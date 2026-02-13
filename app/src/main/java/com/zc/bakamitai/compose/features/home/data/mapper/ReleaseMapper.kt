package com.zc.bakamitai.compose.features.home.data.mapper

import com.zc.bakamitai.BakaApplication
import com.zc.bakamitai.R
import com.zc.bakamitai.compose.features.home.data.model.DownloadItemDTO
import com.zc.bakamitai.compose.features.home.data.model.LatestReleaseDTO
import com.zc.bakamitai.compose.features.home.data.model.ReleaseDTO
import com.zc.bakamitai.compose.features.home.domain.model.DownloadItem
import com.zc.bakamitai.compose.features.home.domain.model.LatestRelease
import com.zc.bakamitai.compose.features.home.domain.model.Release
import com.zc.bakamitai.extensions.to12HourFormat
import com.zc.bakamitai.extensions.toImageUrl

fun LatestReleaseDTO.toDomain(): LatestRelease {
    return mapValues { values -> values.value.toDomain() }
}

fun ReleaseDTO.toDomain(): Release {
    return Release(
        time = time,
        show = show,
        episode = episode,
        imageUrl = imageUrl?.toImageUrl(),
        releaseDate = releaseDate,
        page = page,
        xdcc = xdcc,
        downloads = downloads.map { it.toDomain() }
    )
}

private fun DownloadItemDTO.toDomain(): DownloadItem {
    return DownloadItem(
        res = res.toIntOrNull() ?: 0,
        magnet = magnet,
        torrent = torrent,
        xdcc = xdcc
    )
}
