package com.zc.bakamitai.feature.details.data.mapper

import com.zc.bakamitai.common.data.network.SUBSPLEASE_BASE_URL
import com.zc.bakamitai.feature.details.data.dto.DownloadDTO
import com.zc.bakamitai.feature.details.domain.model.Download
import com.zc.bakamitai.feature.details.domain.model.Resolution

private const val HEIGHT_360P = 360
private const val HEIGHT_480P = 480
private const val HEIGHT_540P = 540
private const val HEIGHT_720P = 720
private const val HEIGHT_1080P = 1080

private const val MAGNET_SCHEME = "magnet:"
private const val HTTP_PREFIX = "http://"
private const val HTTPS_PREFIX = "https://"

/** SubsPlease's XDCC search page; the API's `xdcc` value is already URL-encoded (docs/api.md §5). */
private const val XDCC_SEARCH_PATH = "xdcc/?search="

/**
 * The download, or `null` when its resolution is unknown (anything but 360/480/540/720/1080, docs/api.md §3.6)
 * or it has no usable link. A magnet must be a `magnet:` URI and a torrent an http(s) URL; the XDCC value is
 * appended to the search page as-is, never encoded again.
 */
fun DownloadDTO.toDomain(): Download? {
    val height = res?.trim()?.toIntOrNull()
    val resolution = height?.toResolution()
    val magnetUri = magnet?.trim()?.takeIf { it.startsWith(MAGNET_SCHEME, ignoreCase = true) }
    val torrentUrl = torrent?.trim()?.takeIf { it.startsWith(HTTPS_PREFIX) || it.startsWith(HTTP_PREFIX) }
    val xdccUrl = xdcc?.trim()?.ifEmpty { null }?.let { SUBSPLEASE_BASE_URL + XDCC_SEARCH_PATH + it }
    val hasLink = magnetUri != null || torrentUrl != null || xdccUrl != null
    return if (height != null && resolution != null && hasLink) {
        Download(
            resolution = resolution,
            height = height,
            magnetUri = magnetUri,
            torrentUrl = torrentUrl,
            xdccUrl = xdccUrl,
        )
    } else {
        null
    }
}

private fun Int.toResolution(): Resolution? = when (this) {
    HEIGHT_360P, HEIGHT_480P, HEIGHT_540P -> Resolution.SD
    HEIGHT_720P -> Resolution.HD
    HEIGHT_1080P -> Resolution.FHD
    else -> null
}
