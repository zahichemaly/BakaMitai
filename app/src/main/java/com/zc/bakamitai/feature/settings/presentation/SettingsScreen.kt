package com.zc.bakamitai.feature.settings.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.window.core.layout.WindowSizeClass
import com.zc.bakamitai.common.domain.model.AppSettings
import com.zc.bakamitai.common.domain.model.ThemeMode
import com.zc.bakamitai.common.domain.model.TimeFormat
import com.zc.bakamitai.common.domain.model.WeekStart
import com.zc.bakamitai.common.ui.component.skeleton.SkeletonView
import com.zc.bakamitai.common.ui.resource.StringResource
import com.zc.bakamitai.common.ui.text.UiText
import com.zc.bakamitai.common.ui.theme.BakaMitaiTheme
import com.zc.bakamitai.common.ui.util.AdaptivePreviews
import com.zc.bakamitai.common.ui.util.centeredContentPadding
import com.zc.bakamitai.feature.bookmarks.domain.model.ReleaseAlertsStatus
import com.zc.bakamitai.feature.settings.presentation.component.aboutrow.AboutRowView
import com.zc.bakamitai.feature.settings.presentation.component.alertspreference.AlertsPreferenceSkeletonView
import com.zc.bakamitai.feature.settings.presentation.component.alertspreference.AlertsPreferenceView
import com.zc.bakamitai.feature.settings.presentation.component.alertspreference.mapper.toAlertsPreferenceUIState
import com.zc.bakamitai.feature.settings.presentation.component.pickerdialog.PickerDialogView
import com.zc.bakamitai.feature.settings.presentation.component.pickerpreference.PickerPreferenceSkeletonView
import com.zc.bakamitai.feature.settings.presentation.component.pickerpreference.PickerPreferenceView
import com.zc.bakamitai.feature.settings.presentation.component.settingsfooter.SettingsFooterView
import com.zc.bakamitai.feature.settings.presentation.component.settingssection.SettingsSectionView
import com.zc.bakamitai.feature.settings.presentation.mapper.toThemePicker
import com.zc.bakamitai.feature.settings.presentation.mapper.toTimeFormatPicker
import com.zc.bakamitai.feature.settings.presentation.mapper.toWeekStartPicker
import com.zc.bakamitai.feature.settings.presentation.model.SettingsPicker
import com.zc.bakamitai.feature.settings.presentation.model.ThemeOption
import com.zc.bakamitai.feature.settings.presentation.model.TimeFormatOption
import com.zc.bakamitai.feature.settings.presentation.model.WeekStartOption

/**
 * Settings: the general preferences (first day of week, time format), the theme, the new-episode alerts switch and
 * the About link, then the version. One column, centered on wide windows. The pickers are rows showing their current
 * value; a tap opens its dialog ([SettingsState.openPicker]). While the settings load, each preference
 * card shows its own skeleton; the headers, the About row and the footer are real.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    state: SettingsState,
    onIntent: (SettingsIntent) -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = { SettingsTopAppBar(onIntent = onIntent, scrollBehavior = scrollBehavior) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        // Default content insets: with the navigation bar hidden they include the system navigation bar; on medium
        // and wider windows, the rail's side is left out (the suite consumed it).
    ) { padding ->
        SettingsContent(
            state = state,
            onIntent = onIntent,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        )
        SettingsPickerDialog(state = state, onIntent = onIntent)
    }
}

/** Back and "Settings"; the bar turns `surfaceContainer` once content scrolls under it. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsTopAppBar(
    onIntent: (SettingsIntent) -> Unit,
    scrollBehavior: TopAppBarScrollBehavior,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    TopAppBar(
        title = {
            Text(
                text = UiText.StringResource(StringResource.settings_title).asString(),
                modifier = Modifier.semantics { heading() },
                color = colors.onSurface,
                style = MaterialTheme.typography.titleLarge,
            )
        },
        modifier = modifier,
        navigationIcon = {
            IconButton(onClick = { onIntent(SettingsIntent.BackClicked) }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = UiText.StringResource(StringResource.cd_navigate_back).asString(),
                    tint = colors.onSurfaceVariant,
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = colors.surface,
            scrolledContainerColor = colors.surfaceContainer,
        ),
        scrollBehavior = scrollBehavior,
    )
}

/** The sections and the footer, in a scrolling column at most `MaxContentWidth` wide. */
@Composable
private fun SettingsContent(state: SettingsState, onIntent: (SettingsIntent) -> Unit, modifier: Modifier = Modifier) {
    val spacing = BakaMitaiTheme.spacing
    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
    val margin = if (windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND)) {
        spacing.marginTablet
    } else {
        spacing.margin
    }
    val onAboutClick = remember(onIntent) { { onIntent(SettingsIntent.AboutClicked) } }
    BoxWithConstraints(modifier = modifier) {
        val horizontalPadding = centeredContentPadding(maxWidth = maxWidth, margin = margin)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(start = horizontalPadding, top = spacing.md, end = horizontalPadding, bottom = spacing.xl),
            verticalArrangement = Arrangement.spacedBy(spacing.xl),
        ) {
            GeneralSection(state = state, onIntent = onIntent)
            AppearanceSection(state = state, onIntent = onIntent)
            NotificationsSection(state = state, onIntent = onIntent)
            SettingsSectionView(
                title = UiText.StringResource(StringResource.settings_section_about),
                accentColor = MaterialTheme.colorScheme.outline,
                contentPadding = spacing.xs,
            ) {
                AboutRowView(onClick = onAboutClick)
            }
            state.versionLabel?.let { versionLabel -> SettingsFooterView(versionLabel = versionLabel) }
        }
    }
}

/** First day of week (a calendar) and time format (a clock): picker rows, or their placeholders in one skeleton. */
@Composable
private fun GeneralSection(state: SettingsState, onIntent: (SettingsIntent) -> Unit, modifier: Modifier = Modifier) {
    val onWeekStartClick = remember(onIntent) { { onIntent(SettingsIntent.PickerClicked(SettingsPicker.WEEK_START)) } }
    val onTimeFormatClick = remember(onIntent) {
        { onIntent(SettingsIntent.PickerClicked(SettingsPicker.TIME_FORMAT)) }
    }
    SettingsSectionView(
        title = UiText.StringResource(StringResource.settings_section_general),
        accentColor = MaterialTheme.colorScheme.primary,
        modifier = modifier,
        contentPadding = BakaMitaiTheme.spacing.xs,
    ) {
        val weekStart = state.weekStart
        val timeFormat = state.timeFormat
        if (state.isLoading || weekStart == null || timeFormat == null) {
            SkeletonView {
                PickerRows {
                    PickerPreferenceSkeletonView(icon = Icons.Outlined.CalendarMonth)
                    PickerPreferenceSkeletonView(icon = Icons.Outlined.Schedule)
                }
            }
        } else {
            PickerRows {
                PickerPreferenceView(
                    icon = Icons.Outlined.CalendarMonth,
                    title = weekStart.title,
                    value = weekStart.valueLabel,
                    onClick = onWeekStartClick,
                )
                PickerPreferenceView(
                    icon = Icons.Outlined.Schedule,
                    title = timeFormat.title,
                    value = timeFormat.valueLabel,
                    onClick = onTimeFormatClick,
                )
            }
        }
    }
}

/** The theme's picker row (a palette), or its placeholder in a skeleton. */
@Composable
private fun AppearanceSection(state: SettingsState, onIntent: (SettingsIntent) -> Unit, modifier: Modifier = Modifier) {
    val onThemeClick = remember(onIntent) { { onIntent(SettingsIntent.PickerClicked(SettingsPicker.THEME)) } }
    SettingsSectionView(
        title = UiText.StringResource(StringResource.settings_section_appearance),
        accentColor = MaterialTheme.colorScheme.secondary,
        modifier = modifier,
        contentPadding = BakaMitaiTheme.spacing.xs,
    ) {
        val theme = state.theme
        if (state.isLoading || theme == null) {
            SkeletonView { PickerPreferenceSkeletonView(icon = Icons.Outlined.Palette) }
        } else {
            PickerPreferenceView(
                icon = Icons.Outlined.Palette,
                title = theme.title,
                value = theme.valueLabel,
                onClick = onThemeClick,
            )
        }
    }
}

/** The new-episode alerts switch, or its skeleton. */
@Composable
private fun NotificationsSection(
    state: SettingsState,
    onIntent: (SettingsIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val onAlertsChange = remember(onIntent) { { enabled: Boolean -> onIntent(SettingsIntent.AlertsToggled(enabled)) } }
    val onAllowClick = remember(onIntent) { { onIntent(SettingsIntent.AllowNotificationsClicked) } }
    SettingsSectionView(
        title = UiText.StringResource(StringResource.settings_section_notifications),
        accentColor = MaterialTheme.colorScheme.tertiary,
        modifier = modifier,
        contentPadding = BakaMitaiTheme.spacing.xs,
    ) {
        val alerts = state.alerts
        if (state.isLoading || alerts == null) {
            SkeletonView { AlertsPreferenceSkeletonView() }
        } else {
            AlertsPreferenceView(state = alerts, onCheckedChange = onAlertsChange, onAllowClick = onAllowClick)
        }
    }
}

/** A card's picker rows, stacked as the About card's (`gap-1`). */
@Composable
private fun PickerRows(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(BakaMitaiTheme.spacing.xs)) {
        content()
    }
}

/** The open picker's dialog: a choice saves it and closes the dialog; Cancel, Back or a tap outside closes it. */
@Composable
private fun SettingsPickerDialog(state: SettingsState, onIntent: (SettingsIntent) -> Unit) {
    val onDismiss = remember(onIntent) { { onIntent(SettingsIntent.PickerDismissed) } }
    val onWeekStartSelect = remember(onIntent) {
        { option: WeekStartOption -> onIntent(SettingsIntent.WeekStartSelected(option)) }
    }
    val onTimeFormatSelect = remember(onIntent) {
        { option: TimeFormatOption -> onIntent(SettingsIntent.TimeFormatSelected(option)) }
    }
    val onThemeSelect = remember(onIntent) { { option: ThemeOption -> onIntent(SettingsIntent.ThemeSelected(option)) } }
    when (state.openPicker) {
        SettingsPicker.WEEK_START -> state.weekStart?.let { picker ->
            PickerDialogView(
                title = picker.title,
                options = picker.options,
                onOptionClick = onWeekStartSelect,
                onDismiss = onDismiss,
            )
        }

        SettingsPicker.TIME_FORMAT -> state.timeFormat?.let { picker ->
            PickerDialogView(
                title = picker.title,
                options = picker.options,
                onOptionClick = onTimeFormatSelect,
                onDismiss = onDismiss,
            )
        }

        SettingsPicker.THEME -> state.theme?.let { picker ->
            PickerDialogView(
                title = picker.title,
                options = picker.options,
                onOptionClick = onThemeSelect,
                onDismiss = onDismiss,
            )
        }

        null -> Unit
    }
}

/** Loading; the defaults; Sunday, 24-hour, Dark and alerts off; alerts blocked by Android; the theme dialog open. */
private class SettingsStatePreviewProvider : PreviewParameterProvider<SettingsState> {
    private val loading = SettingsState(
        versionLabel = UiText.StringResource(StringResource.settings_footer_version, listOf("1.0")),
    )

    override val values = sequenceOf(
        loading,
        loaded(AppSettings(), ReleaseAlertsStatus.ON),
        loaded(
            AppSettings(WeekStart.SUNDAY, TimeFormat.TWENTY_FOUR_HOUR, ThemeMode.DARK, newEpisodeAlerts = false),
            ReleaseAlertsStatus.OFF,
        ),
        loaded(AppSettings(), ReleaseAlertsStatus.BLOCKED),
        loaded(AppSettings(), ReleaseAlertsStatus.ON).copy(openPicker = SettingsPicker.THEME),
    )

    private fun loaded(settings: AppSettings, status: ReleaseAlertsStatus) = loading.copy(
        isLoading = false,
        weekStart = settings.weekStart.toWeekStartPicker(),
        timeFormat = settings.timeFormat.toTimeFormatPicker(),
        theme = settings.themeMode.toThemePicker(),
        alerts = settings.toAlertsPreferenceUIState(status),
    )
}

@AdaptivePreviews
@Composable
private fun SettingsScreenPreview(@PreviewParameter(SettingsStatePreviewProvider::class) state: SettingsState) {
    BakaMitaiTheme {
        SettingsScreen(state = state, onIntent = {})
    }
}
