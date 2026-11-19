package com.zc.bakamitai.feature.details.presentation.component.episoderow.component.resolutionrow.mapper

import com.zc.bakamitai.common.ui.resource.StringResource
import com.zc.bakamitai.common.ui.text.UiText
import com.zc.bakamitai.feature.details.domain.DetailsTestData.download
import com.zc.bakamitai.feature.details.domain.model.Download
import com.zc.bakamitai.feature.details.domain.model.Resolution
import com.zc.bakamitai.feature.details.presentation.component.episoderow.component.resolutionrow.model.ResolutionTone
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ResolutionUIModelMapperTest {

    private fun label(height: Int, quality: Int) =
        UiText.StringResource(StringResource.details_resolution, listOf(height, UiText.StringResource(quality)))

    @Test
    fun `always three slots, 1080p then 720p then SD, whatever the input order`() {
        val slots = listOf(
            download(Resolution.SD, 480),
            download(Resolution.HD, 720),
            download(Resolution.FHD, 1080),
        ).toResolutionUIModels()

        assertEquals(listOf(ResolutionTone.FHD, ResolutionTone.HD, ResolutionTone.SD), slots.map { it.tone })
        assertEquals(
            listOf(
                label(1080, StringResource.details_quality_fhd),
                label(720, StringResource.details_quality_hd),
                label(480, StringResource.details_quality_sd),
            ),
            slots.map { it.label },
        )
        assertTrue(slots.all { it.isAvailable })
    }

    @Test
    fun `SD shows its real height`() {
        val sd = listOf(download(Resolution.SD, 360)).toResolutionUIModels().last()

        assertEquals(label(360, StringResource.details_quality_sd), sd.label)
    }

    @Test
    fun `a missing SD reads just SD and isn't available`() {
        val sd = listOf(download(Resolution.FHD, 1080)).toResolutionUIModels().last()

        assertEquals(UiText.StringResource(StringResource.details_quality_sd), sd.label)
        assertFalse(sd.isAvailable)
        assertNull(sd.magnetUri)
    }

    @Test
    fun `a missing 720p keeps its label and isn't available`() {
        val hd = listOf(download(Resolution.SD, 480)).toResolutionUIModels()[1]

        assertEquals(label(720, StringResource.details_quality_hd), hd.label)
        assertFalse(hd.isAvailable)
    }

    @Test
    fun `no downloads are three unavailable slots`() {
        assertTrue(emptyList<Download>().toResolutionUIModels().none { it.isAvailable })
    }

    @Test
    fun `the links are copied`() {
        val fhd = download(Resolution.FHD, 1080)

        val slot = listOf(fhd).toResolutionUIModels().first()

        assertEquals(fhd.magnetUri, slot.magnetUri)
        assertEquals(fhd.torrentUrl, slot.torrentUrl)
        assertEquals(fhd.xdccUrl, slot.xdccUrl)
    }
}
