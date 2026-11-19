package com.zc.bakamitai.feature.settings.presentation.component.alertspreference.model

import androidx.compose.runtime.Immutable
import com.zc.bakamitai.common.ui.text.UiText

/**
 * The new-episode alerts switch.
 *
 * @property isChecked switched on in Settings.
 * @property isBlocked switched on, but Android blocks the app's notifications: [supportingText] says so (in the
 * tertiary color) and an "Allow notifications" button is shown.
 */
@Immutable
data class AlertsPreferenceUIState(val isChecked: Boolean, val supportingText: UiText, val isBlocked: Boolean)
