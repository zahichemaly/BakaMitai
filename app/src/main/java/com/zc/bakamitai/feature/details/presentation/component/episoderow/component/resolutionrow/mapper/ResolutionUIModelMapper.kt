package com.zc.bakamitai.feature.details.presentation.component.episoderow.component.resolutionrow.mapper

import com.zc.bakamitai.common.ui.resource.StringResource
import com.zc.bakamitai.common.ui.text.UiText
import com.zc.bakamitai.feature.details.domain.model.Download
import com.zc.bakamitai.feature.details.domain.model.Resolution
import com.zc.bakamitai.feature.details.presentation.component.episoderow.component.resolutionrow.model.ResolutionTone
import com.zc.bakamitai.feature.details.presentation.component.episoderow.component.resolutionrow.model.ResolutionUIModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

private const val HEIGHT_FHD = 1080
private const val HEIGHT_HD = 720

/**
 * Always three slots, 1080p, 720p then SD, as the design orders them; a resolution the episode doesn't have is an
 * unavailable slot (the site shows it greyed out). SD is labelled with its real height ("360p SD"), or just "SD"
 * when it's missing.
 */
fun List<Download>.toResolutionUIModels(): ImmutableList<ResolutionUIModel> {
    val byResolution = associateBy { it.resolution }
    val sd = byResolution[Resolution.SD]
    return persistentListOf(
        byResolution[Resolution.FHD].toSlot(label(HEIGHT_FHD, StringResource.details_quality_fhd), ResolutionTone.FHD),
        byResolution[Resolution.HD].toSlot(label(HEIGHT_HD, StringResource.details_quality_hd), ResolutionTone.HD),
        sd.toSlot(
            label = sd?.let { label(it.height, StringResource.details_quality_sd) }
                ?: UiText.StringResource(StringResource.details_quality_sd),
            tone = ResolutionTone.SD,
        ),
    )
}

private fun label(height: Int, quality: Int): UiText =
    UiText.StringResource(StringResource.details_resolution, listOf(height, UiText.StringResource(quality)))

private fun Download?.toSlot(label: UiText, tone: ResolutionTone) = ResolutionUIModel(
    label = label,
    tone = tone,
    magnetUri = this?.magnetUri,
    torrentUrl = this?.torrentUrl,
    xdccUrl = this?.xdccUrl,
)
