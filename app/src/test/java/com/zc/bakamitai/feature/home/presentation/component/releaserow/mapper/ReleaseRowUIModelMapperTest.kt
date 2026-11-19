package com.zc.bakamitai.feature.home.presentation.component.releaserow.mapper

import com.zc.bakamitai.common.ui.resource.StringResource
import com.zc.bakamitai.common.ui.text.UiText
import com.zc.bakamitai.feature.home.domain.HomeTestData.release
import com.zc.bakamitai.feature.home.presentation.component.releaserow.model.ReleaseRowUIModel
import com.zc.bakamitai.feature.home.presentation.model.EpisodeBadgeUIModel
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNull

class ReleaseRowUIModelMapperTest {

    private val newYork = TimeZone.of("America/New_York")
    private val release = release("tensei", "08", "2026-11-18T14:33:47Z", title = "Tensei Shitara Ken Deshita S2")

    @Test
    fun `maps a release to a row in the given zone`() {
        assertEquals(
            ReleaseRowUIModel(
                key = "tensei#08",
                slug = "tensei",
                title = UiText.StringValue("Tensei Shitara Ken Deshita S2"),
                coverUrl = null,
                timeLabel = UiText.TimeValue(LocalTime(9, 33)),
                episodeBadge = EpisodeBadgeUIModel(
                    label = UiText.StringResource(StringResource.home_episode, listOf("08")),
                    contentDescription = UiText.StringResource(StringResource.cd_episode, listOf("08")),
                    isSpecial = false,
                ),
            ),
            release.toUIModel(newYork),
        )
    }

    @Test
    fun `the time label follows the zone`() {
        assertEquals(UiText.TimeValue(LocalTime(23, 33)), release.toUIModel(TimeZone.of("Asia/Tokyo")).timeLabel)
    }

    @Test
    fun `two episodes of the same show have two keys`() {
        assertNotEquals(release.toUIModel(newYork).key, release.copy(episode = "09").toUIModel(newYork).key)
    }

    @Test
    fun `a null cover passes through`() {
        assertNull(release.toUIModel(newYork).coverUrl)
    }
}
