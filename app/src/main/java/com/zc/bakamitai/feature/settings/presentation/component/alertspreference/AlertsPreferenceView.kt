package com.zc.bakamitai.feature.settings.presentation.component.alertspreference

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.zc.bakamitai.common.ui.component.skeleton.SkeletonBlock
import com.zc.bakamitai.common.ui.component.skeleton.SkeletonTextLine
import com.zc.bakamitai.common.ui.component.skeleton.SkeletonView
import com.zc.bakamitai.common.ui.resource.StringResource
import com.zc.bakamitai.common.ui.text.UiText
import com.zc.bakamitai.common.ui.theme.BakaMitaiTheme
import com.zc.bakamitai.common.ui.util.AdaptivePreviews
import com.zc.bakamitai.feature.settings.presentation.component.alertspreference.model.AlertsPreferenceUIState

private val SwitchWidth = 52.dp
private val SwitchHeight = 32.dp
private const val SKELETON_DESCRIPTION_FRACTION = 0.8f

/**
 * The new-episode alerts switch: the whole row toggles it (a switch for TalkBack). When Android blocks the app's
 * notifications, the supporting text says so and an "Allow notifications" button follows.
 */
@Composable
fun AlertsPreferenceView(
    state: AlertsPreferenceUIState,
    onCheckedChange: (Boolean) -> Unit,
    onAllowClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .toggleable(value = state.isChecked, role = Role.Switch, onValueChange = onCheckedChange),
            horizontalArrangement = Arrangement.spacedBy(BakaMitaiTheme.spacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                AlertsTitle()
                Text(
                    text = state.supportingText.asString(),
                    color = if (state.isBlocked) colors.tertiary else colors.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            Switch(checked = state.isChecked, onCheckedChange = null)
        }
        if (state.isBlocked) {
            TextButton(onClick = onAllowClick) {
                Text(text = UiText.StringResource(StringResource.settings_alerts_allow).asString())
            }
        }
    }
}

@Composable
private fun AlertsTitle(modifier: Modifier = Modifier) {
    Text(
        text = UiText.StringResource(StringResource.settings_alerts_title).asString(),
        modifier = modifier,
        color = MaterialTheme.colorScheme.onSurface,
        style = MaterialTheme.typography.titleMedium,
    )
}

/** The real title, one line of supporting text and the switch's track. */
@Composable
fun AlertsPreferenceSkeletonView(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(BakaMitaiTheme.spacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            AlertsTitle()
            SkeletonTextLine(
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.fillMaxWidth(SKELETON_DESCRIPTION_FRACTION),
            )
        }
        SkeletonBlock(modifier = Modifier.size(width = SwitchWidth, height = SwitchHeight), shape = CircleShape)
    }
}

/** On, off, and on but blocked by Android. */
private class AlertsPreferencePreviewProvider : PreviewParameterProvider<AlertsPreferenceUIState> {
    private val description = UiText.StringResource(StringResource.settings_alerts_description)

    override val values = sequenceOf(
        AlertsPreferenceUIState(isChecked = true, supportingText = description, isBlocked = false),
        AlertsPreferenceUIState(isChecked = false, supportingText = description, isBlocked = false),
        AlertsPreferenceUIState(
            isChecked = true,
            supportingText = UiText.StringResource(StringResource.settings_alerts_blocked),
            isBlocked = true,
        ),
    )
}

@AdaptivePreviews
@Composable
private fun AlertsPreferenceViewPreview(
    @PreviewParameter(AlertsPreferencePreviewProvider::class) state: AlertsPreferenceUIState,
) {
    BakaMitaiTheme {
        Surface(color = MaterialTheme.colorScheme.surfaceContainerLow) {
            AlertsPreferenceView(
                state = state,
                onCheckedChange = {},
                onAllowClick = {},
                modifier = Modifier.padding(BakaMitaiTheme.spacing.md),
            )
        }
    }
}

@AdaptivePreviews
@Composable
private fun AlertsPreferenceSkeletonViewPreview() {
    BakaMitaiTheme {
        Surface(color = MaterialTheme.colorScheme.surfaceContainerLow) {
            SkeletonView(modifier = Modifier.padding(BakaMitaiTheme.spacing.md)) {
                AlertsPreferenceSkeletonView()
            }
        }
    }
}
