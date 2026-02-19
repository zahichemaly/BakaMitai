package com.zc.bakamitai.compose.features.details.data.mapper

import com.zc.bakamitai.compose.features.details.data.model.DownloadDTO
import com.zc.bakamitai.compose.features.details.data.model.EpisodeContentDTO
import com.zc.bakamitai.compose.features.details.data.model.EpisodeDTO
import com.zc.bakamitai.compose.features.details.domain.model.Download
import com.zc.bakamitai.compose.features.details.domain.model.Episode
import com.zc.bakamitai.compose.features.details.domain.model.LinkSource


fun EpisodeContentDTO.toDomain(): List<Episode> {
    return this.episode.map { it.value.toDomain() }
}


private fun EpisodeDTO.toDomain(): Episode {
    return Episode(
        episode = episode ?: "",
        show = show ?: "",
        time = time ?: "",
        releaseDate = releaseDate ?: "",
        downloads = downloads.map { it.toDomain() }
    )
}

private fun DownloadDTO.toDomain(): Download {
    val result = mutableMapOf<LinkSource, String>()
    result[LinkSource.XDCC] = this.xdcc
    result[LinkSource.MAGNET] = this.magnet
    result[LinkSource.TORRENT] = this.torrent
    return Download(
        resolution = this.res,
        links = result
    )
}
