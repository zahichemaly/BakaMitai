package com.zc.bakamitai.feature.settings.presentation.component.alertspreference.mapper

import com.zc.bakamitai.common.domain.model.AppSettings
import com.zc.bakamitai.common.ui.resource.StringResource
import com.zc.bakamitai.common.ui.text.UiText
import com.zc.bakamitai.feature.bookmarks.domain.model.ReleaseAlertsStatus
import com.zc.bakamitai.feature.settings.presentation.component.alertspreference.model.AlertsPreferenceUIState
import kotlin.test.Test
import kotlin.test.assertEquals

class AlertsPreferenceUIModelMapperTest {

    private val description = UiText.StringResource(StringResource.settings_alerts_description)
    private val on = AppSettings(newEpisodeAlerts = true)
    private val off = AppSettings(newEpisodeAlerts = false)

    @Test
    fun `switched on and allowed, or not read yet, is checked and not blocked`() {
        val expected = AlertsPreferenceUIState(isChecked = true, supportingText = description, isBlocked = false)

        assertEquals(expected, on.toAlertsPreferenceUIState(ReleaseAlertsStatus.ON))
        assertEquals(expected, on.toAlertsPreferenceUIState(null))
    }

    @Test
    fun `switched off is unchecked and never blocked`() {
        val expected = AlertsPreferenceUIState(isChecked = false, supportingText = description, isBlocked = false)

        ReleaseAlertsStatus.entries.forEach { status -> assertEquals(expected, off.toAlertsPreferenceUIState(status)) }
        assertEquals(expected, off.toAlertsPreferenceUIState(null))
    }

    @Test
    fun `switched on but blocked by Android says so`() {
        assertEquals(
            AlertsPreferenceUIState(
                isChecked = true,
                supportingText = UiText.StringResource(StringResource.settings_alerts_blocked),
                isBlocked = true,
            ),
            on.toAlertsPreferenceUIState(ReleaseAlertsStatus.BLOCKED),
        )
    }
}
