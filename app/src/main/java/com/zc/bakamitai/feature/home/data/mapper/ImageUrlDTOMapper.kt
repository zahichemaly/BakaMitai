package com.zc.bakamitai.feature.home.data.mapper

import com.zc.bakamitai.common.data.network.SUBSPLEASE_BASE_URL

/** SubsPlease's "no cover yet" image (docs/api.md §1), shown as our own placeholder instead. */
private const val PLACEHOLDER_IMAGE_PATH = "/wp-includes/images/image-coming-soon-placeholder.png"

private const val HTTP_SCHEME = "http"

/**
 * Resolves an `image_url` to an absolute cover URL: site-relative paths are prefixed with the SubsPlease base
 * URL, absolute URLs are kept, and a missing value or the placeholder image is `null`.
 */
fun String?.toCoverUrl(): String? {
    val value = this?.trim().orEmpty()
    return when {
        value.isEmpty() || value.endsWith(PLACEHOLDER_IMAGE_PATH) -> null
        value.startsWith(HTTP_SCHEME) -> value
        else -> SUBSPLEASE_BASE_URL.trimEnd('/') + "/" + value.trimStart('/')
    }
}
