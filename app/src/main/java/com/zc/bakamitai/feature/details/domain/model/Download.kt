package com.zc.bakamitai.feature.details.domain.model

/**
 * One resolution of an episode, with the links SubsPlease has for it (at least one is set).
 *
 * @property height 360, 480, 540, 720 or 1080: SD is labelled with its real height.
 * @property magnetUri a `magnet:` URI, for a torrent client.
 * @property torrentUrl the nyaa.si `.torrent` URL.
 * @property xdccUrl SubsPlease's XDCC search page for this file.
 */
data class Download(
    val resolution: Resolution,
    val height: Int,
    val magnetUri: String?,
    val torrentUrl: String?,
    val xdccUrl: String?,
)
