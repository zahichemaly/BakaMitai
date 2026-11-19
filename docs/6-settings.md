# 6. Settings

> **Status:** Implemented (2026-11-19; skills step 21 and docs step 22 left to the caller, previews of steps 15–16 not rendered, step 23 partly checked: see [§11](#11-implementation-notes)) · **Design:** [Google Stitch project `5201581584167289517`, screen `2451d84613ec4d788d2a276d70767c71` ("Settings")](https://stitch.google.com/projects/5201581584167289517?node-id=2451d84613ec4d788d2a276d70767c71) · **Planned:** 2026-11-19
> **Spec:** [context.md §2](context.md#2-navigation-shell), [§3.2](context.md#32-schedule), [§3.4](context.md#34-bookmarks), [§3.6](context.md#36-settings), [§3.7](context.md#37-new-episode-alerts), [§4.2](context.md#42-schedule), [§4.5](context.md#45-formatting-rules-all-screens), [§5](context.md#5-local-persistence), [§6](context.md#6-suggested-feature-breakdown) · **API:** none; [api.md §7](api.md#7-client-guidelines) only for the alerts exception's wording
> **Depends on:** [1-home.md](1-home.md), [2-schedule.md](2-schedule.md), [3-shows.md](3-shows.md), [4-details.md](4-details.md), [5-bookmarks.md](5-bookmarks.md) (all implemented and committed): the navigation shell (`Navigator`, `NavigationState`, `FullScreenNavKey`, `AppEntryProvider`), `TopLevelAppBarView` and its hidden settings action, the shared `toTimeLabel()` mapper, `GetWeeklyScheduleUseCase(weekStart, …)`, the alerts port `ReleaseAlertNotifier.areAlertsEnabled()` / `GetReleaseAlertsEnabledUseCase`, `NotificationPermission.kt`, the skeleton pieces, `centeredContentPadding`, and `datastore-preferences` (already in the catalog and in `:app`).

**Design source.** A Google Stitch project, not Figma or Sketch, captured by the requester (not reopened for this plan): **`2451d84613ec4d788d2a276d70767c71`** in project **`5201581584167289517`**, a 390×1309 dark frame, as a render of the screen's own HTML with the real fonts (`screen.jpg`, 780×2680; the GitHub circle is blank only because the logo's SVG path was shortened in the copy) and a condensed copy of the HTML/Tailwind with Stitch's prompt (`code.html`). Nodes below name that screen plus its HTML sections: `<header>`, the hero banner, `section` "General Preferences" (two segmented toggles), `section` "Appearance" (theme radio list), `section` "Data & Storage" (storage row, Export / Clear buttons), `section` "About & Community" (About row, Check for Updates row), `<footer>`, and the commented toast. It's a **pushed screen** (back arrow, no bottom bar). Loading, error, light, tablet and the alerts switch are **not in the design** ([§2.8](#28-states-not-in-the-design-all-proposed)).

**How design and spec are reconciled** (same rule as the earlier plans): `context.md` and the earlier plans' hooks decide **behaviour and data**; the Stitch design decides **visual treatment**. The mock invents a lot (hero copy, storage size, Export / Clear, Check for Updates, a fake repository URL and version, marketing subtitles). Nothing the app can't back is shown; every disagreement is in [§3](#3-design-vs-spec).

---

## 1. Summary

**Settings** is a pushed screen with four preferences, saved on the device in a Preferences DataStore, plus an About link and the app version:

| Preference | Options (default first) | Effect |
|---|---|---|
| First day of week | Monday, Sunday | Schedule's tab order and the week's dates |
| Time format | 12-hour (AM/PM), 24-hour | **Every** time in the app: Home, Schedule, Bookmarks' airing chips, Details' release lines |
| Theme | System default, Light, Dark | The app's color scheme **and** the system bars' icon colors, applied before the first frame |
| New-episode alerts | On, Off | The global switch the Bookmarks plan deferred here (5-bookmarks.md D5): gates the background check, the Bookmarks alerts chip and Details' first-bookmark permission ask |

Every change shows at once, everywhere: the theme and the time format through the app root, the week start the next time Schedule is on screen.

**Entry points:**
- the **settings gear** on the top app bar of Home, Schedule, All Shows and Bookmarks (hidden until now; `TopLevelAppBarView`)
- the Bookmarks **alerts chip** when alerts are on, or switched off in Settings ([§5.6](#56-other-features))

**In scope:**
- `feature/settings/` (all four layers): the screen, its components, `AppInfo` (version and source URL), the setters
- the settings store in `common/` (the `structure` skill already places `SettingsRepository`, `AppSettings` and `ObserveSettingsUseCase` there): `AppSettings`, `SettingsRepository`, `SettingsLocalDataSource`, the `DataStore<Preferences>` provider
- the app root: `AppViewModel` / `AppState`, `AppRoute` (theme, time format), `MainActivity` (first frame held until the theme is known, system bar styling)
- the time format through `UiText` (a new `UiText.TimeValue`, resolved with a composition local), so every existing time mapper follows the setting unchanged
- hooks: the gear on the four tabs; Schedule's week start; the alerts switch in the worker, the Bookmarks chip and Details; persisting the Bookmarks sort (context.md §3.4: "Not saved until Settings exists"; separable, [Phase 5](#phase-5-persist-the-bookmarks-sort))
- the doc and skill updates

**Out of scope:** the mock's Data & Storage actions (export, clear), Check for Updates, the hero banner ([§3](#3-design-vs-spec), [Q1–Q5](#10-open-questions)); per-app night mode for the system splash ([Q13](#10-open-questions)); localization (strings stay English).

---

## 2. Design breakdown

### 2.0 Token mapping

Spacing and radius tokens come from the screen's Tailwind config: `space-xs` 4, `space-sm` 8, `space-md` 16, `space-lg` 24, `space-xl` 32, `margin` 16, `margin-tablet` 24; `rounded-DEFAULT` 1 rem, `rounded-lg` **2 rem** (this config, not DESIGN.md's 12 px), `rounded-full` pill.

| Design (Tailwind / DESIGN.md) | Compose |
|---|---|
| `bg-surface`, `text-on-surface` | `colorScheme.surface` / `onSurface` (Scaffold default) |
| Header `bg-surface/80 backdrop-blur-xl`, `font-headline-sm` (18 px bold) | `TopAppBar`, title `typography.titleLarge` ("App bar titles", 18 sp), `topAppBarColors(containerColor = surface, scrolledContainerColor = surfaceContainer)` with `pinnedScrollBehavior()`, as Details. No blur. |
| `arrow_back` `text-on-surface-variant` | `Icons.AutoMirrored.Filled.ArrowBack`, tint `onSurfaceVariant` |
| Section header: 18 px icon + `font-label-lg uppercase tracking-wider` in an accent | `Icon(Modifier.size(18.dp), tint = accent)` + `Text(style = labelLarge, color = accent)`, strings stored uppercase (as `bookmarks_header_title`); accents `primary` (General), `secondary` (Appearance), `tertiary` (Notifications), `outline` (About) |
| Card `bg-surface-container-low rounded-lg p-space-md shadow-sm` | `Surface(color = surfaceContainerLow, shape = shapes.extraLarge /* 32 dp */)`, inner padding `spacing.md`; **no shadow** (DESIGN.md: tonal layering) |
| Preference title `font-title-md` / subtitle `font-body-sm text-on-surface-variant` | `titleMedium` / `bodySmall` in `onSurfaceVariant` |
| Segment track `bg-surface-container rounded-full p-1 gap-1`; selected `bg-primary text-on-primary`; unselected `text-on-surface-variant`; `font-label-md`, qualifier `font-body-sm opacity-80` | `SingleChoiceSegmentedButtonRow` on a `surfaceContainer` `CircleShape` track; `SegmentedButton(shape = CircleShape)` with `SegmentedButtonDefaults.colors(activeContainerColor = primary, activeContentColor = onPrimary, inactiveContainerColor = Transparent, inactiveContentColor = onSurfaceVariant, active/inactiveBorderColor = Transparent)`, `icon = {}`; label `labelMedium`, qualifier `bodySmall` at `QUALIFIER_ALPHA = 0.8f` |
| Theme row `p-space-sm rounded-DEFAULT`, `bg-surface-container/50` (unselected) or `bg-surface-container` (selected) | Row in `shapes.medium` (16 dp), `surfaceContainer.copy(alpha = UNSELECTED_ROW_ALPHA /* 0.5f */)` or `surfaceContainer`, padding `spacing.sm` |
| Theme icon circle 36 px `bg-surface-container text-on-surface-variant`; selected `bg-primary/20 text-primary`, filled glyph | `Box(Modifier.size(36.dp).background(color, CircleShape))`, 20 dp icon; selected `primary.copy(alpha = SELECTED_ICON_ALPHA /* 0.2f */)` + `primary` tint + `Icons.Filled.*` |
| Theme title `font-body-md font-medium/semibold`, subtitle `font-body-sm` | `titleSmall` (14 sp semibold, no hardcoded `FontWeight`) / `bodySmall` `onSurfaceVariant` |
| Check circle 24 px `bg-primary` + `check` | `Icon(Icons.Filled.CheckCircle, tint = primary, Modifier.size(24.dp))`, decorative |
| About card `p-1 gap-1`, rows `p-space-md rounded-DEFAULT hover:bg-surface-container` | Card inner padding `spacing.xs`; row `clip(shapes.medium).clickable(…)` + `padding(spacing.md)` |
| About icon circle 40 px `bg-surface-container-high` | `Box(40.dp, surfaceContainerHigh, CircleShape)`, 20 dp icon in `onSurface` |
| URL `font-label-sm text-primary tracking-wide` | `labelSmall`, `primary` |
| `open_in_new` 20 px `text-outline` | `Icons.AutoMirrored.Outlined.OpenInNew`, 20 dp, `outline` |
| Footer `font-label-lg font-bold`, tagline `font-body-sm text-outline max-w-xs` | `labelLarge` (13 sp bold) `onSurface`; `bodySmall` `outline`, `widthIn(max = 320.dp)`, centered |
| Section gap `gap-space-xl`, header → card `gap-space-sm`, page `px-margin pt-space-md pb-10` | `spacedBy(spacing.xl)`, `spacedBy(spacing.sm)`, side margin per window ([§2.2](#22-layout)), top `spacing.md`, bottom `spacing.xl` |

### 2.1 Top app bar and navigation

- **Layout:** `TopAppBar` with the back arrow and "Settings" (`settings_title`). No subtitle, no actions ([§3](#3-design-vs-spec) row 2). `Scaffold(topBar, snackbarHost)`, `Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)`: the bar turns `surfaceContainer` once content scrolls under it.
- **Navigation:** `SettingsNavKey` is a `FullScreenNavKey`: pushed on the **current tab's** stack; the bottom bar slides away on compact windows, the rail stays on medium and wider. Back (arrow or system) pops it. A double tap on the gear opens one Settings (`Navigator.navigate` is single-top).
- **Accessibility:** back button `cd_navigate_back`; the title is the pane's first heading.

### 2.2 Layout

Top to bottom, in a `Column(Modifier.verticalScroll(…))`:
1. **GENERAL PREFERENCES** (`Icons.Outlined.CalendarMonth`, `primary`): *First day of week*, then *Time format* (`spacedBy(spacing.lg)` between them, as `gap-space-lg`).
2. **APPEARANCE** (`Icons.Outlined.Palette`, `secondary`): *Theme*.
3. **NOTIFICATIONS** (`Icons.Outlined.Notifications`, `tertiary`): *New-episode alerts* (replaces "Data & Storage", [§3](#3-design-vs-spec) rows 6–10).
4. **ABOUT** (`Icons.Outlined.Info`, `outline`): *About BakaMitai*.
5. **Footer**: version and disclaimer.

**Adaptive:** a single column at every width. Side margin `spacing.margin` (16 dp) on compact, `spacing.marginTablet` (24 dp) from medium up, and on wide windows the column is centered at `MaxContentWidth` (720 dp) through `centeredContentPadding(maxWidth, margin)` in a `BoxWithConstraints`, as Details and All Shows. Window size is read in the Screen, never stored. Bottom: the Scaffold's content padding (system bars), like `DetailsScreen`; the footer must end above the gesture / 3-button bar once the suite hides (checked in step 23).

### 2.3 General preferences

Node: `section` "General Preferences".

- **Components:** for each preference, a private `PreferenceHeading(title, description)` in the Screen (static text), then `SegmentedPreferenceView(segments, onSegmentClick)` ([§5.3](#53-featuresettings)).
- **Segmented control:** M3 `SingleChoiceSegmentedButtonRow` + two `SegmentedButton`s styled as the design's track ([§2.0](#20-token-mapping)): fully rounded segments (`shape = CircleShape`, not `itemShape`), `space = 4.dp` between them, no check icon, no borders. Target look: a 48 dp track (the segments' minimum touch target) with ~4 dp of track around a 40 dp selected pill; the implementor checks the preview and sets the track's vertical padding to 0 if the segments already reserve 48 dp.
- **Content:**
  - *First day of week* (`settings_week_start_title`, `settings_week_start_description` "Choose the starting day for the release schedule"): **Monday** + qualifier "(Default)" | **Sunday** (reuses `day_monday` / `day_sunday`).
  - *Time format* (`settings_time_format_title`, `settings_time_format_description` "Display release times in 12-hour or 24-hour clock"): **12-hour** + qualifier "(AM/PM)" | **24-hour**.
  - Labels `maxLines = 1`; the qualifier ellipsizes first (`weight(1f, fill = false)`) at large font scales.
- **Interactions:** a tap selects the segment and saves it at once (no confirm, no toast). M3 animates the colors.
- **Accessibility:** the row is a `selectableGroup()`, each segment a `Role.RadioButton` with its selected state (both built into the M3 component). TalkBack: "Monday (Default), selected, radio button, 1 of 2". The heading text above is read first.

### 2.4 Appearance

Node: `section` "Appearance" (the radio list).

- **Components:** `PreferenceHeading` (`settings_theme_title` "Theme", `settings_theme_description` "Choose how BakaMitai looks"), then `ThemeOptionsView(options, onOptionClick)`: a `Column(Modifier.selectableGroup(), spacedBy(spacing.xs))` of three rows.
- **Row:** `Row(Modifier.heightIn(min = ThemeOptionMinHeight /* 52 dp */).clip(shapes.medium).background(…).selectable(selected, role = Role.RadioButton, onClick).padding(spacing.sm), spacedBy(spacing.sm))`: icon circle, a `Column(weight(1f))` with title and description, and the check when selected.
- **Content** ([§3](#3-design-vs-spec) row 5):

  | Option | Title | Description | Icon (unselected / selected) |
  |---|---|---|---|
  | System | "System default" | "Follows your device's dark theme setting" | `Icons.Outlined.Devices` / `Icons.Filled.Devices` |
  | Light | "Light" | "Always use the light theme" | `Icons.Outlined.LightMode` / `Icons.Filled.LightMode` |
  | Dark | "Dark" | "Always use the dark theme" | `Icons.Outlined.DarkMode` / `Icons.Filled.DarkMode` |

  Title `maxLines = 1` + ellipsis, description up to 2 lines.
- **Interactions:** a tap selects and saves; the **whole app recolors in the same frame**, Settings included, and the system bar icons follow ([§5.1](#51-how-the-settings-reach-the-app)).
- **Accessibility:** each row merges its text (`selectable` merges descendants): "System default, Follows your device's dark theme setting, radio button, selected". Icons and the check are decorative (`contentDescription = null`). Rows are ≥ 52 dp.

### 2.5 Notifications: new-episode alerts

**Not in design (proposed).** The switch the Bookmarks plan deferred to Settings (5-bookmarks.md D5, [§2.3](5-bookmarks.md#23-counter-row-and-alerts-chip), Q20).

- **Component:** `AlertsPreferenceView(state, onCheckedChange, onAllowClick)`: a `Row(Modifier.toggleable(value = isChecked, role = Role.Switch, onValueChange = onCheckedChange))` with a `Column(weight(1f))` (title `titleMedium` "New-episode alerts", supporting text `bodySmall`) and a trailing M3 `Switch(checked = isChecked, onCheckedChange = null)` (theme colors). Under it, only when `isBlocked`, a `TextButton(onClick = onAllowClick) { Text("Allow notifications") }`.
- **Content:**

  | State | Switch | Supporting text |
  |---|---|---|
  | On, Android allows notifications | on | "Get a notification when a bookmarked show's new episode is out" (`onSurfaceVariant`) |
  | Off (switched off here) | off | the same text |
  | On, but Android blocks BakaMitai's notifications (permission denied on 13+, app notifications off, or the channel muted) | on | "Notifications are blocked for BakaMitai." in `tertiary` + the **Allow notifications** button |
- **Interactions:**
  - Switch on → save; when Android blocks notifications, the permission dialog opens right away (Android 13+, while Android still shows it), otherwise the app's notification settings. Denied → the row stays in the "blocked" state (no extra snackbar: the row explains it).
  - Switch off → save. Nothing else: the background check keeps its chain and makes no requests while alerts are off ([§5.1](#51-how-the-settings-reach-the-app), [Q10](#10-open-questions)).
  - **Allow notifications** → the same permission dialog / notification settings.
  - Coming back from the system settings re-reads the state (`ScreenStarted`).
- **Accessibility:** the row reads "New-episode alerts, Get a notification…, switch, on". The Allow button is its own focus stop (48 dp `TextButton`).

### 2.6 About

Node: `section` "About & Community", first row. "Check for Updates" is dropped ([§3](#3-design-vs-spec) row 13).

- **Component:** `AboutRowView(urlLabel, onClick)`: a 40 dp circle with `Icons.Outlined.Code` (no GitHub logo in Material Icons, [Q6](#10-open-questions)), title `titleMedium` "About BakaMitai", the URL label `labelSmall` `primary` ("github.com/zahichemaly/BakaMitai", from `AppInfo`), trailing `Icons.AutoMirrored.Outlined.OpenInNew` (`outline`, 20 dp, decorative).
- **Interactions:** tap → opens `https://github.com/zahichemaly/BakaMitai` with `ACTION_VIEW` (context.md §7 decision 2). No app can open it → snackbar "No app can open this link."
- **Accessibility:** `clickable(onClickLabel = "Open in browser", role = Role.Button)` on the merged row: "About BakaMitai, github.com/zahichemaly/BakaMitai, button, double-tap to open in browser".

### 2.7 Footer

- `SettingsFooterView(versionLabel)`: centered `Column(spacedBy(spacing.xs))`, top padding `spacing.md`:
  - "BakaMitai v1.0" (`settings_footer_version` "BakaMitai v%1$s" with `BuildConfig.VERSION_NAME` through `AppInfo`), `labelLarge`, `onSurface`. No "(Build …)", no verified badge.
  - "Unofficial app. Release data from subsplease.org." (`settings_footer_disclaimer`, proposed, [Q7](#10-open-questions)), `bodySmall`, `outline`, `widthIn(max = 320.dp)`, `TextAlign.Center`.

### 2.8 States not in the design (all proposed)

| State | Proposal |
|---|---|
| **Loading** | Until the first DataStore emission (a few ms; the app root already read the same store). The section headers, preference headings, About row and footer are real (static, or synchronous `AppInfo`). Each of the three preference cards wraps its placeholders in **its own `SkeletonView`, inside the card's surface** (a card background inside a `SkeletonView` would shimmer as one block, the Details lesson): `SegmentedPreferenceSkeletonView` ×2 (a 48 dp `CircleShape` `SkeletonBlock`), `ThemeOptionsSkeletonView` (three 52 dp `shapes.medium` blocks, `spacedBy(spacing.xs)`), `AlertsPreferenceSkeletonView` (the real title, one `bodySmall` line at 0.8 width, a 52×32 dp pill for the switch). One `SkeletonView` per section is what `best-practices` §5.3 allows for sections. |
| **Error** | None as a screen state: a read failure falls back to the defaults ([§4.3](#43-local-data)). A **write** failure (disk full) → snackbar with the generic error; the control stays on the stored value. |
| **Empty** | Not applicable. |
| **Refreshing** | Not applicable (local data, no pull-to-refresh). |
| **Light theme** | Same roles with `LightColorScheme`; accents `primary` / `secondary` / `tertiary` / `outline` stay readable on `surfaceContainerLow`. |
| **Medium / expanded** | Rail stays; 24 dp margins; one column centered at 720 dp. |
| **Large font scales** | No fixed heights (`heightIn(min)`); theme descriptions wrap to 2 lines; segment qualifiers ellipsize first. |
| **Theme change while Settings is open** | Instant recolor of everything, system bars included. |

---

## 3. Design vs. spec

| # | Item | Design | docs/context.md | Recommendation |
|---|---|---|---|---|
| 1 | Hero banner | "Customization & Engine / Fine-tune your simulcast schedule & cache", `tune` icon, glow | Not in §3.6; there's no engine or cache setting | **Drop.** It describes nothing the screen does. |
| 2 | App bar | Translucent blurred bar, "Settings" + subtitle "BakaMitai Tracker" | A pushed screen, like Details | **Solid `TopAppBar`**, back arrow + "Settings", pinned scroll color like Details. **No subtitle**: "Tracker" isn't the app's name. |
| 3 | Segmented controls | Custom pill track, filled `primary` selected pill, no check | Options listed, no visual | **M3 `SingleChoiceSegmentedButtonRow`** styled as the track (radio semantics for free). Labels from the design in the spec's casing: "Monday (Default)" / "Sunday", "12-hour (AM/PM)" / "24-hour". |
| 4 | Theme default | "Dark Mode (Obsidian)" selected | Default **System** | **System** (spec). |
| 5 | Theme labels | "System Default / Follows OS dark & light modes", "Light Mode / High contrast daylight canvas", "Dark Mode (Obsidian) / Midnight neo-tokyo palette", "Active" pill | System / Light / Dark | **Plain, accurate copy** ([§2.4](#24-appearance)); the light theme isn't "high contrast" and nothing is "neo-tokyo". **Drop "Active"**: the check, the row tint and TalkBack's "selected" already say it. Keep the design's icons, tints and check. |
| 6 | Data & Storage: storage row | "Local Bookmarks Storage · 8 saved titles • 12 KB used on device", cyan dot | Not in §3.6 | **Drop** ([Q2](#10-open-questions)). The count is real but leads nowhere here; "12 KB" is invented and meaningless to users. |
| 7 | Export (JSON) | Button; toast "Bookmarks JSON exported to storage" | Not in the spec | **Drop** ([Q4](#10-open-questions)): needs the Storage Access Framework, a file format and an import to be useful. |
| 8 | Clear Local Data | Destructive button; browser `confirm()`; toast "Cache cleared successfully" | Not in the spec | **Drop** ([Q3](#10-open-questions)): per-card remove with Undo exists. If it's ever added: an M3 `AlertDialog` ("Remove all 8 bookmarks? This can't be undone.", Cancel / Remove in `error`) and a `requestCheck()` so the alert chain ends. |
| 9 | "DATA & STORAGE" section | Tertiary header, `database` icon | — | **Replaced by "NOTIFICATIONS"** (tertiary, `Icons.Outlined.Notifications`) holding the alerts switch. |
| 10 | New-episode alerts switch | Not in design | Not in §3.6; §3.7 alerts; 5-bookmarks.md D5 defers "a global switch" to Settings | **Add** ([§2.5](#25-notifications-new-episode-alerts)); context.md §3.6 gains the row. Default **on** (today's behaviour). |
| 11 | About URL | "github.com/BakaMitai-App/tracker" (invented) | `https://github.com/zahichemaly/BakaMitai` | **The real URL.** |
| 12 | About icon | GitHub mark (SVG) | — | **`Icons.Outlined.Code`** ([Q6](#10-open-questions)): Material Icons Extended has no GitHub logo, and the app takes icons from it. |
| 13 | Check for Updates | Row, "You are on the latest build", spinning icon, toast | Not in the spec | **Drop**: there's no update service; the claim would be unverifiable. |
| 14 | Footer version | "BakaMitai v1.4.2 (Build 2025.1)" + `verified` icon | "BakaMitai v1.0", version from `versionName` | **"BakaMitai v{versionName}"**. Drop "(Build …)" and the verified badge (it suggests a check that doesn't exist). |
| 15 | Footer tagline | "Fast, sleek, ad-free anime release tracker • Powered by open community data" | — | **Replace** with "Unofficial app. Release data from subsplease.org." ([Q7](#10-open-questions)): "open community data" misdescribes a scraped site; the disclaimer is accurate. |
| 16 | Toasts | "Settings updated", "Schedule starts on …", "Time format switched to …", "Theme updated to …" | — | **None**: every change is visible at once. A snackbar only when a save fails. |
| 17 | Card shadows | `shadow-sm` | DESIGN.md: tonal layering | **No shadow.** |
| 18 | Settings gear | Stitch prompt: "accessed via the gear icon in the top app bar" | §2: a gear on every top-level screen | **Show it** on Home, Schedule, All Shows and Bookmarks (hidden until now). |
| 19 | Loading, light, tablet | Not in design | §3: every screen has loading/error states | Proposed in [§2.8](#28-states-not-in-the-design-all-proposed). |

---

## 4. Data

### 4.1 Endpoints

None. Settings is local. The only API-doc change is wording: [api.md §7](api.md#7-client-guidelines)'s alerts exception says "Only while the user has bookmarks and notifications are on"; it becomes "…and new-episode alerts are on (the Settings switch, and Android lets the app post notifications)". Behaviour is unchanged: with alerts off, a run makes no request ([5-bookmarks.md §5.3](5-bookmarks.md#53-scheduling) step 2).

### 4.2 API gaps

None (nothing remote; no live check needed).

### 4.3 Local data

**One Preferences DataStore** file, `settings` (`files/datastore/settings.preferences_pb`), provided once by `CommonModule` ([§5.2](#52-common)). Enums are stored **by name**; never rename a stored constant (an unknown or missing value reads as the default).

| Key | Type | Values | Default | Owner |
|---|---|---|---|---|
| `week_start` | String | `MONDAY`, `SUNDAY` (`WeekStart`) | `MONDAY` | `common/data/local/SettingsLocalDataSource` |
| `time_format` | String | `TWELVE_HOUR`, `TWENTY_FOUR_HOUR` (`TimeFormat`) | `TWELVE_HOUR` | same |
| `theme_mode` | String | `SYSTEM`, `LIGHT`, `DARK` (`ThemeMode`) | `SYSTEM` | same |
| `new_episode_alerts` | Boolean | — | `true` | same |
| `bookmarks_sort` | String | `RECENTLY_ADDED`, `TITLE`, `NEXT_AIRING` (`BookmarkSort`) | `RECENTLY_ADDED` | `feature/bookmarks/data/local/BookmarkSortLocalDataSource` ([Phase 5](#phase-5-persist-the-bookmarks-sort)) |

Domain shape (`common/domain/model/`):

```kotlin
data class AppSettings(
    val weekStart: WeekStart = WeekStart.MONDAY,
    val timeFormat: TimeFormat = TimeFormat.TWELVE_HOUR,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val newEpisodeAlerts: Boolean = true,
)
enum class WeekStart(val dayOfWeek: DayOfWeek) { MONDAY(DayOfWeek.MONDAY), SUNDAY(DayOfWeek.SUNDAY) }
enum class TimeFormat { TWELVE_HOUR, TWENTY_FOUR_HOUR }
enum class ThemeMode { SYSTEM, LIGHT, DARK }
```

**Failure handling** (`best-practices` §4.2):
- **Reads never fail:** `IOException` (which includes DataStore's `CorruptionException`) → empty preferences → defaults; the DataStore is also built with `ReplaceFileCorruptionHandler { emptyPreferences() }`, so a corrupt file is reset once instead of failing every read. The app root depends on this: its first frame waits for the first emission ([§5.1](#51-how-the-settings-reach-the-app)).
- **Writes:** `IOException` → `Result.failure(DomainError.Unknown(e))` through `safeDataStoreCall` (`common/data/util/`, the DataStore twin of `safeApiCall`); `CancellationException` is never caught.
- **Backup:** the file is under `files/`, which the default Auto Backup rules include, so settings restore with the app's backup. No rule change.

### 4.4 Caching and refresh

- DataStore keeps the file in memory after the first read; every observer gets the same `Flow<AppSettings>` (`distinctUntilChanged`).
- Schedule's reload after a week-start change reuses its 1 h weekly cache ([api.md §7](api.md#7-client-guidelines)) and re-sends only the best-effort today's-schedule request, the same cost as its new-day reload. A time-format or theme change makes **no** request.

---

## 5. Architecture

Root: `app/src/main/java/com/zc/bakamitai/`. Tests: `app/src/test/java/com/zc/bakamitai/` (unit), `app/src/androidTest/java/com/zc/bakamitai/` (instrumented).

### 5.1 How the settings reach the app

**Theme, before the first frame** (context.md §5: "the app root reads it"):
1. `app/AppViewModel` maps `ObserveSettingsUseCase()` to `AppState(isLoaded, darkThemeOverride, use24HourClock)` with `stateIn(WhileSubscribed(5_000), AppState())`.
2. `app/AppRoute` composes **nothing** until `state.isLoaded`. Then `isDarkTheme = state.darkThemeOverride ?: isSystemInDarkTheme()`, `BakaMitaiTheme(darkTheme = isDarkTheme)`, `CompositionLocalProvider(LocalUse24HourClock provides state.use24HourClock) { AppNavigation(…) }`, and `SideEffect { onThemeApplied(isDarkTheme) }`.
3. `app/MainActivity` holds the first draw: an `OnPreDrawListener` on `android.R.id.content` returns `false` until `onThemeApplied` has run once (the documented "keep the splash screen up" pattern, without the `core-splashscreen` library). The starting window stays up for those few milliseconds, and the **first app frame is already in the chosen theme**: no light → dark flash. Recompositions run during the held frames (the listener only cancels drawing), so the `SideEffect` fires before the frame that draws.
4. **System bars** follow the app theme, not the system: `onThemeApplied(isDarkTheme)` (skipped when unchanged) calls `enableEdgeToEdge(statusBarStyle = style, navigationBarStyle = style)` with `style = if (isDarkTheme) SystemBarStyle.dark(TRANSPARENT) else SystemBarStyle.light(TRANSPARENT, TRANSPARENT)` (light icons on dark, dark icons on light), then sets `window.isNavigationBarContrastEnforced = false` again on API 29+ (`enableEdgeToEdge` resets it). The initial `enableEdgeToEdge` in `onCreate` stays, so the window is edge to edge from the first layout.
5. **Always resolves:** reads never fail ([§4.3](#43-local-data)), so `isLoaded` turns true on the first emission (~10–50 ms on a cold start). On rotation or a system dark-mode change the Activity is recreated, `AppViewModel` survives with a loaded value, and the first composition is already loaded.
6. **Limitation:** the starting window (the Android 12+ splash) is drawn by the system before the app runs, in the **system's** mode. With a forced theme opposite to the system's, the splash is in the system's mode and the app's first frame in the chosen one; the app itself never flashes ([Q13](#10-open-questions)). The navigation suite paints `colorScheme.background` behind every entry, so transitions never reveal the system-themed window background.

**Time format (every time shown):**
- `UiText` gains `data class TimeValue(val time: LocalTime) : UiText`. It formats **when it's resolved**: `asString()` reads `LocalUse24HourClock.current` (a `compositionLocalOf { false }` in `common/ui/text/TimeText.kt`, provided by `AppRoute`); `asString(context, use24HourClock = false)` takes it as a parameter outside composition. Formats (context.md §4.5): 12-hour `amPmHour(Padding.NONE)`, `:`, `minute()`, space, `amPmMarker("AM", "PM")` → "8:05 PM"; 24-hour `hour()`, `:`, `minute()` → "20:05".
- `common/ui/mapper/TimeUIModelMapper.kt`: `LocalTime.toTimeLabel()` returns `UiText.TimeValue(this)`; `Instant.toTimeLabel(timeZone)` is unchanged (it converts, then calls the first). **No call site changes**: Home's upcoming cards and release rows, Schedule's card chips, Bookmarks' airing chips ("TODAY 21:30", nested in a string resource) and Details' release lines all follow the setting.
- **Already-open screens** update in the same frame the setting changes: only the `asString()` calls that read the local recompose; no ViewModel observes the time format, no data reloads, and mappers stay pure functions of domain data.
- **Why not a mapper parameter** (the Schedule plan's hint, 2-schedule.md §6 "What it unblocks"): every ViewModel (Home, Schedule, Bookmarks, Details) would have to observe the setting and re-map data it doesn't keep, and text already in `State` couldn't change. Resolving at the last moment is the same contract `UiText` already has with the locale ([Q8](#10-open-questions)).
- **Outside composition:** nothing resolved with `asString(context)` contains a time today (snackbars, the share text, notification texts: "Episode 9 is out"). A future one passes the setting: a Route reads `LocalUse24HourClock.current`. The notification's own timestamp is drawn by System UI in the **system's** 12/24-hour format ([Q17](#10-open-questions)).

**First day of week (Schedule):**
- `ScheduleViewModel` reads `GetSettingsUseCase()` at the start of every load and passes `weekStart.dayOfWeek` to `GetWeeklyScheduleUseCase`, whose `weekStart` parameter becomes **required** (its `DEFAULT_WEEK_START` goes: the default lives in `AppSettings`).
- `ScreenStarted` (already sent by `ScheduleRoute`'s `LifecycleStartEffect`) becomes `reloadIfStale()`: reload when the day changed **or** the stored week start differs from the loaded week's first day. Settings is a pushed screen, so the user always comes back to Schedule through a screen start (Back to the same tab re-composes the entry; another tab composes it on selection). The selected weekday stays selected; its date follows the new week.
- **Why not an observed flow** (the Schedule plan's hint): a week-start change triggers a load (a side effect with a request), which a `combine(…).stateIn(…)` reducer can't express without restructuring Schedule's load into a flow, and collecting forever in `init` is forbidden (`best-practices` §3.4) ([Q9](#10-open-questions)).

**New-episode alerts (the global switch):**
- `AppSettings.newEpisodeAlerts` is the switch (common: written by Settings, read by Bookmarks).
- Bookmarks' domain gains `ReleaseAlertsStatus { ON, OFF, BLOCKED }` (`OFF`: switched off in Settings; `BLOCKED`: on in Settings but Android blocks the app's notifications) and `GetReleaseAlertsStatusUseCase` (switch from `SettingsRepository` + `ReleaseAlertNotifier.areAlertsEnabled()`, which keeps meaning "Android lets us post").
- `GetReleaseAlertsEnabledUseCase` becomes `suspend` and returns `status == ON`. The worker's code doesn't change (`checkReleases(alertsEnabled = getAlertsEnabled())` already runs in `doWork`): with the switch off, a run takes the existing silent path (no request, pending slots resolved, next run scheduled).
- **The chain isn't cancelled when the switch goes off** (5-bookmarks.md §7 "What it unblocks" said "cancel `release-check` when off"): the silent path already makes no request, needs no new scheduler code, and keeps `resolved_through` moving, so turning alerts back on never fires a burst of alerts for episodes released while they were off ([Q10](#10-open-questions)).
- The Bookmarks chip and Details' first-bookmark ask follow the status ([§5.6](#56-other-features)).

### 5.2 common/

| Path | | Responsibility |
|---|---|---|
| `common/domain/model/AppSettings.kt` | **new** | The four preferences with their defaults ([§4.3](#43-local-data)). |
| `common/domain/model/WeekStart.kt` | **new** | `MONDAY`, `SUNDAY`, each with its `dayOfWeek` (kotlinx-datetime). |
| `common/domain/model/TimeFormat.kt` | **new** | `TWELVE_HOUR`, `TWENTY_FOUR_HOUR`. |
| `common/domain/model/ThemeMode.kt` | **new** | `SYSTEM`, `LIGHT`, `DARK`. |
| `common/domain/repository/SettingsRepository.kt` | **new** | `fun observeSettings(): Flow<AppSettings>`; `suspend fun setWeekStart(value: WeekStart): Result<Unit>`, `setTimeFormat(value: TimeFormat)`, `setThemeMode(value: ThemeMode)`, `setNewEpisodeAlerts(enabled: Boolean)` (each `Result<Unit>`). |
| `common/domain/usecase/ObserveSettingsUseCase.kt` | **new** | `@Factory`; `operator fun invoke(): Flow<AppSettings>`. Users: `AppViewModel`, `SettingsViewModel`. |
| `common/domain/usecase/GetSettingsUseCase.kt` | **new** | `@Factory`; `suspend operator fun invoke(): AppSettings = repository.observeSettings().first()`. Users: Schedule, Bookmarks. |
| `common/data/local/SettingsDataStoreFactory.kt` | **new** | `fun createSettingsDataStore(produceFile: () -> File): DataStore<Preferences>` = `PreferenceDataStoreFactory.create(corruptionHandler = ReplaceFileCorruptionHandler { emptyPreferences() }, produceFile = produceFile)` (the "file factory" of `structure` §2), so the corruption behaviour is testable without a `Context`. |
| `common/data/local/PreferencesDataStoreExtensions.kt` | **new** | `fun DataStore<Preferences>.safeData(): Flow<Preferences>` = `data.catch { if (it is IOException) emit(emptyPreferences()) else throw it }`. Used by both preference data sources. |
| `common/data/local/SettingsLocalDataSource.kt` | **new** | `@Single`, takes `DataStore<Preferences>`. `val settings: Flow<AppSettings>` (`safeData().map { it.toAppSettings() }.distinctUntilChanged()`, private key parsing, unknown name → default); `suspend fun setWeekStart(value)`, `setTimeFormat`, `setThemeMode`, `setNewEpisodeAlerts` (`edit {}`; they throw `IOException`). Keys in a private companion: `week_start`, `time_format`, `theme_mode`, `new_episode_alerts`. |
| `common/data/util/SafeDataStoreCall.kt` | **new** | `suspend fun <T> safeDataStoreCall(block: suspend () -> T): Result<T>`: `IOException` → `DomainError.Unknown(e)`; cancellation and other exceptions propagate. |
| `common/data/repository/SettingsRepositoryImpl.kt` | **new** | `@Single`, bound to `SettingsRepository` by the scan. Delegates reads; wraps every write in `safeDataStoreCall`. |
| `common/di/CommonModule.kt` | **modify** | `@Single fun settingsDataStore(context: Context): DataStore<Preferences> = createSettingsDataStore { context.preferencesDataStoreFile(SETTINGS_DATA_STORE) }` with `private const val SETTINGS_DATA_STORE = "settings"`. |
| `common/ui/text/UiText.kt` | **modify** | `@Immutable data class TimeValue(val time: LocalTime) : UiText`; `asString()` → `time.toClockText(LocalUse24HourClock.current)`; `fun asString(context: Context, use24HourClock: Boolean = false)`, passed down to nested `UiText` args. KDoc: times resolve with the user's clock format. |
| `common/ui/text/TimeText.kt` | **new** | `val LocalUse24HourClock = compositionLocalOf { false }` (provided by `AppRoute`; previews get 12-hour); the two `LocalTime.Format`s; `fun LocalTime.toClockText(use24HourClock: Boolean): String`. |
| `common/ui/mapper/TimeUIModelMapper.kt` | **modify** | `LocalTime.toTimeLabel(): UiText = UiText.TimeValue(this)`; the format constant moves to `TimeText.kt`; KDoc. |
| `common/ui/util/OpenLink.kt` | **new** (moved) | `fun Context.openLink(url: String): Boolean` (`ACTION_VIEW`; `false` on `ActivityNotFoundException`, catch parameter named `ignored`), moved from `DetailsRoute` now that Settings uses it too. |
| `common/ui/component/toplevelappbar/TopLevelAppBarView.kt` | **modify** | Remove `showSettingsAction`: the gear is always shown. The preview provider keeps the with / without `bottomContent` samples. |

### 5.3 feature/settings

| Path | | Responsibility |
|---|---|---|
| **domain** | | |
| `feature/settings/domain/model/AppInfo.kt` | **new** | `data class AppInfo(val versionName: String, val sourceCodeUrl: String)` |
| `feature/settings/domain/repository/AppInfoRepository.kt` | **new** | `fun getAppInfo(): AppInfo` |
| `feature/settings/domain/usecase/GetAppInfoUseCase.kt` | **new** | `@Factory`; `operator fun invoke(): AppInfo` |
| `feature/settings/domain/usecase/UpdateWeekStartUseCase.kt` | **new** | `@Factory`; `suspend operator fun invoke(weekStart: WeekStart): Result<Unit>` → `SettingsRepository.setWeekStart` |
| `feature/settings/domain/usecase/UpdateTimeFormatUseCase.kt` | **new** | same for `TimeFormat` |
| `feature/settings/domain/usecase/UpdateThemeModeUseCase.kt` | **new** | same for `ThemeMode` |
| `feature/settings/domain/usecase/UpdateNewEpisodeAlertsUseCase.kt` | **new** | `suspend operator fun invoke(enabled: Boolean): Result<Unit>` → `setNewEpisodeAlerts` (no scheduler call, [§5.1](#51-how-the-settings-reach-the-app)) |
| **data** | | |
| `feature/settings/data/repository/AppInfoRepositoryImpl.kt` | **new** | `@Single`; `AppInfo(versionName = BuildConfig.VERSION_NAME, sourceCodeUrl = SOURCE_CODE_URL)`, `SOURCE_CODE_URL = "https://github.com/zahichemaly/BakaMitai"` (context.md §1). Keeps the ViewModel on use cases and gives `settings/data` its job ([Q14](#10-open-questions)). |
| **di** | | |
| `feature/settings/di/SettingsModule.kt` | **new** | `@Module @ComponentScan("com.zc.bakamitai.feature.settings") class SettingsModule` |
| **presentation: models and mappers** | | |
| `presentation/model/WeekStartOption.kt` | **new** | `enum class WeekStartOption { MONDAY, SUNDAY }` (presentation keys; domain enums never reach a Composable, `architecture` §4) |
| `presentation/model/TimeFormatOption.kt` | **new** | `TWELVE_HOUR`, `TWENTY_FOUR_HOUR` |
| `presentation/model/ThemeOption.kt` | **new** | `SYSTEM`, `LIGHT`, `DARK` |
| `presentation/mapper/AppInfoUIModelMapper.kt` | **new** | `AppInfo.toSourceCodeLabel(): UiText` ("github.com/zahichemaly/BakaMitai": the URL without its scheme), `AppInfo.toVersionLabel(): UiText` (`settings_footer_version`) |
| **presentation: components** | | |
| `component/settingssection/SettingsSectionView.kt` | **new** | `SettingsSectionView(icon: ImageVector, title: UiText, accentColor: Color, modifier, contentPadding: Dp = spacing.md, content: @Composable ColumnScope.() -> Unit)`: header row (`heading()`) + the `surfaceContainerLow` / `extraLarge` card. Static chrome, no skeleton. `@AdaptivePreviews`. |
| `component/segmentedpreference/model/SegmentUIState.kt` | **new** | `@Immutable data class SegmentUIState<T>(val value: T, val label: UiText, val qualifier: UiText?, val isSelected: Boolean)` |
| `component/segmentedpreference/mapper/SegmentUIModelMapper.kt` | **new** | `WeekStart.toWeekStartSegments(): ImmutableList<SegmentUIState<WeekStartOption>>` (Monday + "(Default)", Sunday), `TimeFormat.toTimeFormatSegments(): ImmutableList<SegmentUIState<TimeFormatOption>>` (12-hour + "(AM/PM)", 24-hour) |
| `component/segmentedpreference/SegmentedPreferenceView.kt` | **new** | `fun <T> SegmentedPreferenceView(segments: ImmutableList<SegmentUIState<T>>, onSegmentClick: (T) -> Unit, modifier)` ([§2.3](#23-general-preferences)) + `SegmentedPreferenceSkeletonView(modifier)` (48 dp pill block), `@AdaptivePreviews` (each selection; skeleton in a `SkeletonView`) |
| `component/themeoptions/model/ThemeOptionUIState.kt` | **new** | `@Immutable data class ThemeOptionUIState(val option: ThemeOption, val title: UiText, val description: UiText, val icon: ImageVector, val isSelected: Boolean)` |
| `component/themeoptions/mapper/ThemeOptionUIModelMapper.kt` | **new** | `ThemeMode.toThemeOptionUIStates(): ImmutableList<ThemeOptionUIState>`: three options in order, the selected one with the filled icon |
| `component/themeoptions/ThemeOptionsView.kt` | **new** | `ThemeOptionsView(options, onOptionClick: (ThemeOption) -> Unit, modifier)` ([§2.4](#24-appearance)) + `ThemeOptionsSkeletonView(modifier)`, previews (each selection) |
| `component/alertspreference/model/AlertsPreferenceUIState.kt` | **new** | `@Immutable data class AlertsPreferenceUIState(val isChecked: Boolean, val supportingText: UiText, val isBlocked: Boolean)` (`isBlocked` → `tertiary` text + Allow button) |
| `component/alertspreference/mapper/AlertsPreferenceUIModelMapper.kt` | **new** | `AppSettings.toAlertsPreferenceUIState(status: ReleaseAlertsStatus?)`: `isChecked = newEpisodeAlerts`; `isBlocked = newEpisodeAlerts && status == BLOCKED`; the matching text. `status == null` (not read yet) → not blocked. |
| `component/alertspreference/AlertsPreferenceView.kt` | **new** | `AlertsPreferenceView(state, onCheckedChange: (Boolean) -> Unit, onAllowClick: () -> Unit, modifier)` ([§2.5](#25-notifications-new-episode-alerts)) + `AlertsPreferenceSkeletonView(modifier)`, previews (on / off / blocked) |
| `component/aboutrow/AboutRowView.kt` | **new** | `AboutRowView(urlLabel: UiText, onClick: () -> Unit, modifier)` ([§2.6](#26-about)), previews. No skeleton: its data is synchronous. |
| `component/settingsfooter/SettingsFooterView.kt` | **new** | `SettingsFooterView(versionLabel: UiText, modifier)` ([§2.7](#27-footer)), previews |
| **presentation: screen** | | |
| `presentation/SettingsState.kt`, `SettingsIntent.kt`, `SettingsEffect.kt` | **new** | [§5.9](#59-mvi-contract) |
| `presentation/SettingsViewModel.kt` | **new** | `@KoinViewModel`; `combine(_state, observeSettings(), alertsStatus).stateIn(WhileSubscribed(5_000), _state.value)`; reducer rules in [§5.9](#59-mvi-contract) |
| `presentation/SettingsRoute.kt` | **new** | `SettingsRoute(onNavigateBack: () -> Unit, modifier, viewModel = koinViewModel())`: `collectAsStateWithLifecycle()`; `LifecycleStartEffect(viewModel) { onIntent(ScreenStarted); onStopOrDispose {} }`; `rememberNotificationPermissionRequester { onIntent(NotificationPermissionResult(it)) }`; `ObserveAsEvents`: `NavigateBack` → `onNavigateBack()`, `OpenUrl` → `context.openLink(url)` or snackbar `settings_no_browser`, `RequestNotifications` → `if (context.canAskNotificationPermission()) requestPermission() else context.openAppNotificationSettings()`, `ShowError` → snackbar; snackbars in `rememberCoroutineScope()` (Bookmarks' pattern); owns the `SnackbarHostState`. |
| `presentation/SettingsScreen.kt` | **new** | `SettingsScreen(state, onIntent, modifier, snackbarHostState = remember { SnackbarHostState() })`. Private: `SettingsTopAppBar`, `SettingsContent` (the column, [§2.2](#22-layout)), `PreferenceHeading(title, description)`, `SettingsSections(state, isLoading, …)` (the three preference sections: each card shows its controls, or its `[Name]SkeletonView`s inside its own `SkeletonView` while `state.isLoading`, [§2.8](#28-states-not-in-the-design-all-proposed)); one remembered adapter per callback (`best-practices` §1.5). `@AdaptivePreviews` with a `PreviewParameterProvider<SettingsState>` (loading; defaults; Sunday / 24-hour / Dark / alerts off; alerts blocked). Watch Detekt's `TooManyFunctions` (11 per file): move a helper to a component if it trips. |
| **presentation: navigation** | | |
| `presentation/navigation/SettingsNavKey.kt` | **new** | `@Serializable data object SettingsNavKey : NavKey, FullScreenNavKey` |
| `presentation/navigation/SettingsNavigation.kt` | **new** | `fun Navigator.navigateToSettings() = navigate(SettingsNavKey)`; `fun EntryProviderScope<NavKey>.settingsEntry(onNavigateBack: () -> Unit)` → `entry<SettingsNavKey> { SettingsRoute(onNavigateBack) }` |

### 5.4 app/

| Path | | Responsibility |
|---|---|---|
| `app/AppState.kt` | **new** | `@Immutable data class AppState(val isLoaded: Boolean = false, val darkThemeOverride: Boolean? = null, val use24HourClock: Boolean = false)`; `darkThemeOverride` `null` = follow the system. Primitives only (no domain enums in UI state). |
| `app/AppViewModel.kt` | **new** | `@KoinViewModel class AppViewModel(observeSettings: ObserveSettingsUseCase)`; `val state: StateFlow<AppState>` = `observeSettings().map { it.toAppState() }.stateIn(…)`; private `AppSettings.toAppState()` (SYSTEM → `null`, LIGHT → `false`, DARK → `true`; `TWENTY_FOUR_HOUR` → `true`). No Intent or Effect: it's the app root, not a screen, and has no user actions. |
| `app/AppRoute.kt` | **modify** | `AppRoute(showDeepLinks: Flow<String>, onThemeApplied: (isDarkTheme: Boolean) -> Unit, modifier, viewModel: AppViewModel = koinViewModel())` ([§5.1](#51-how-the-settings-reach-the-app) step 2). KDoc. |
| `app/MainActivity.kt` | **modify** | `holdFirstFrameUntilThemeApplied()` (the `OnPreDrawListener`, added after `setContent`), `private var isThemeApplied`, `private var appliedDarkTheme: Boolean?`, `private fun onThemeApplied(isDarkTheme: Boolean)` (bar styles + contrast, idempotent), passed as `::onThemeApplied`. Update the class KDoc (bars follow the app theme now). |
| `app/di/AppModule.kt` | **modify** | Add `SettingsModule::class` to `includes` and `@ComponentScan("com.zc.bakamitai.app")` for `AppViewModel` (the `structure` tree's "scanned by AppModule"). |
| `app/navigation/AppEntryProvider.kt` | **modify** | `onOpenSettings = navigator::navigateToSettings` for Home, Schedule, All Shows and Bookmarks; `settingsEntry(onNavigateBack = navigator::goBack)`; KDoc ("null until Settings exists" goes). |

### 5.5 Navigation

- One key, `SettingsNavKey` (no arguments), pushed on the selected tab by `Navigator.navigate` (single-top). Opening it from the rail's tab A and switching to tab B keeps it on A's stack; reselecting A pops it (existing `Navigator` behaviour).
- `FullScreenNavKey` hides the bottom bar on compact windows (`AppNavigation`'s `hideBar` already handles any such key).
- Back: `navigator::goBack`. A notification deep link while Settings is open → `navigateFromStart` (unchanged).
- `NavKeySerializationTest` gains `SettingsNavKey`.

### 5.6 Other features

| Feature | Path | | Change |
|---|---|---|---|
| Home | `presentation/HomeRoute.kt`, `HomeScreen.kt`, `navigation/HomeNavigation.kt` | modify | `onOpenSettings: () -> Unit` (non-null); drop `showSettingsAction` (Screen param, preview argument, KDoc). |
| All Shows | `presentation/AllShowsRoute.kt`, `AllShowsScreen.kt`, `navigation/AllShowsNavigation.kt` | modify | same |
| Schedule | `presentation/ScheduleRoute.kt`, `ScheduleScreen.kt`, `navigation/ScheduleNavigation.kt` | modify | same |
| Schedule | `domain/usecase/GetWeeklyScheduleUseCase.kt` | modify | `weekStart: DayOfWeek` required; remove `DEFAULT_WEEK_START`; KDoc. |
| Schedule | `presentation/ScheduleViewModel.kt` | modify | Inject `GetSettingsUseCase`; `load()` reads the week start inside its job; private `loadedWeekStart`; `ScreenStarted` → `reloadIfStale()` ([§5.1](#51-how-the-settings-reach-the-app)). |
| Bookmarks | `domain/model/ReleaseAlertsStatus.kt` | **new** | `enum class ReleaseAlertsStatus { ON, OFF, BLOCKED; companion object { fun of(isSwitchedOn: Boolean, isSystemAllowed: Boolean): ReleaseAlertsStatus } }` (`!isSwitchedOn` → `OFF`, else `!isSystemAllowed` → `BLOCKED`, else `ON`) |
| Bookmarks | `domain/usecase/GetReleaseAlertsStatusUseCase.kt` | **new** | `@Factory`, takes `SettingsRepository` and `ReleaseAlertNotifier`; `suspend operator fun invoke(): ReleaseAlertsStatus` = `of(settings.observeSettings().first().newEpisodeAlerts, notifier.areAlertsEnabled())`. Users: Bookmarks, Settings, Details, `GetReleaseAlertsEnabledUseCase`. |
| Bookmarks | `domain/usecase/GetReleaseAlertsEnabledUseCase.kt` | modify | Takes `GetReleaseAlertsStatusUseCase`; `suspend operator fun invoke(): Boolean = getStatus() == ReleaseAlertsStatus.ON`. The worker's call is unchanged. |
| Bookmarks | `domain/repository/ReleaseAlertNotifier.kt` | modify | KDoc only: `areAlertsEnabled()` = Android lets the app post; the in-app switch is combined by `GetReleaseAlertsStatusUseCase`. |
| Bookmarks | `presentation/component/bookmarksheader/mapper/BookmarksHeaderUIModelMapper.kt` | modify | `ReleaseAlertsStatus.toAlertsChipUIModel()` replaces `Boolean.toAlertsChipUIModel()`: `ON` → "ALERTS ON", `NotificationsActive`, `cd_bookmarks_alerts_on` ("… Opens Settings"); `OFF` → "ALERTS OFF", `NotificationsOff`, `cd_bookmarks_alerts_switched_off`; `BLOCKED` → "ALERTS OFF", `NotificationsOff`, `cd_bookmarks_alerts_off` (unchanged: "… Turns them on"). |
| Bookmarks | `presentation/component/bookmarksheader/model/AlertsChipUIModel.kt` | modify | KDoc for the three states (`isEnabled` stays: `ON` only). |
| Bookmarks | `presentation/BookmarksViewModel.kt` | modify | `GetReleaseAlertsStatusUseCase` replaces `GetReleaseAlertsEnabledUseCase`; `checkAlerts()` launches and keeps a private `alertsStatus` (never exposed); `AlertsChipClicked`: `ON` / `OFF` → `OpenSettings`, `BLOCKED` → `EnableAlerts`, unknown → ignored. |
| Bookmarks | `presentation/BookmarksEffect.kt`, `BookmarksRoute.kt` | modify | Remove `OpenNotificationSettings` (no longer sent); `onOpenSettings` non-null; drop `showSettingsAction`. `ShowAlertsDenied`'s snackbar action still opens the system settings. |
| Bookmarks | `presentation/BookmarksScreen.kt`, `navigation/BookmarksNavigation.kt` | modify | drop `showSettingsAction`; non-null `onOpenSettings`. |
| Details | `presentation/DetailsViewModel.kt` | modify | Inject `GetReleaseAlertsStatusUseCase`; on `toggle.isFirstBookmark`, send `AskForReleaseAlerts` only when the status isn't `OFF` (the user switched alerts off: don't ask for a permission they don't want). |
| Details | `presentation/DetailsRoute.kt` | modify | Use `common/ui/util/OpenLink.kt`; delete the private copy. |

**Phase 5 (Bookmarks sort persistence):**

| Path | | Change |
|---|---|---|
| `feature/bookmarks/data/local/BookmarkSortLocalDataSource.kt` | **new** | `@Single`, the shared `DataStore<Preferences>`; `val sort: Flow<BookmarkSort>` (`safeData()`, key `bookmarks_sort`, unknown → `RECENTLY_ADDED`); `suspend fun setSort(sort: BookmarkSort)` |
| `feature/bookmarks/domain/repository/BookmarkSortRepository.kt` | **new** | `fun observeSort(): Flow<BookmarkSort>`; `suspend fun setSort(sort: BookmarkSort): Result<Unit>` (a separate interface: `BookmarkRepository` and its fakes stay Room-only) |
| `feature/bookmarks/data/repository/BookmarkSortRepositoryImpl.kt` | **new** | `@Single`; writes through `safeDataStoreCall` |
| `feature/bookmarks/domain/usecase/ObserveBookmarkSortUseCase.kt`, `UpdateBookmarkSortUseCase.kt` | **new** | pass-throughs |
| `feature/bookmarks/presentation/BookmarksViewModel.kt` | modify | The sort comes from `ObserveBookmarkSortUseCase` (not `_state.selectedSort`): `combine(observeBookmarkSort(), retries).flatMapLatest { sort -> observeBookmarks(sort)… }`; the reducer sets `selectedSort = sort.toSortOption()` (private, next to `toBookmarkSort()`); `SortSelected(option)` → `updateBookmarkSort(option.toBookmarkSort())`, failure → `ShowError`. |

### 5.7 di

- `CommonModule` provides `DataStore<Preferences>` and scans the new common classes (`SettingsLocalDataSource`, `SettingsRepositoryImpl`, the two use cases).
- `SettingsModule` scans `feature/settings` (use cases, `AppInfoRepositoryImpl`, `SettingsViewModel`).
- `AppModule` includes `SettingsModule` and scans `app` (`AppViewModel`).
- `KoinModulesTest`'s `verify()` covers them (`Context` is already an extra type). The JVM `koinApplication` check can't build anything that needs the DataStore (no `androidContext`); add `koin.get<GetAppInfoUseCase>()` there only.

### 5.8 Resources

New strings (`values/strings.xml`; uppercase ones stored uppercase, DESIGN.md labels):
- `settings_title` "Settings"
- `settings_section_general` "GENERAL PREFERENCES", `settings_section_appearance` "APPEARANCE", `settings_section_notifications` "NOTIFICATIONS", `settings_section_about` "ABOUT"
- `settings_week_start_title` "First day of week", `settings_week_start_description` "Choose the starting day for the release schedule", `settings_option_default` "(Default)" (with the existing `day_monday`, `day_sunday`)
- `settings_time_format_title` "Time format", `settings_time_format_description` "Display release times in 12-hour or 24-hour clock", `settings_time_format_12h` "12-hour", `settings_time_format_12h_qualifier` "(AM/PM)", `settings_time_format_24h` "24-hour"
- `settings_theme_title` "Theme", `settings_theme_description` "Choose how BakaMitai looks", `settings_theme_system` "System default", `settings_theme_system_description` "Follows your device's dark theme setting", `settings_theme_light` "Light", `settings_theme_light_description` "Always use the light theme", `settings_theme_dark` "Dark", `settings_theme_dark_description` "Always use the dark theme"
- `settings_alerts_title` "New-episode alerts", `settings_alerts_description` "Get a notification when a bookmarked show's new episode is out", `settings_alerts_blocked` "Notifications are blocked for BakaMitai.", `settings_alerts_allow` "Allow notifications"
- `settings_about_title` "About BakaMitai", `cd_settings_open_in_browser` "Open in browser", `settings_no_browser` "No app can open this link."
- `settings_footer_version` "BakaMitai v%1$s", `settings_footer_disclaimer` "Unofficial app. Release data from subsplease.org."
- `cd_bookmarks_alerts_switched_off` "New-episode alerts are off. Opens Settings"

Changed:
- `cd_bookmarks_alerts_on` → "New-episode alerts are on. Opens Settings"
- `details_alerts_denied` → "New-episode alerts are off. You can turn them on in Settings."

Reused: `cd_navigate_back`, `cd_settings`, `day_monday`, `day_sunday`, `error_generic` (through `toErrorUIModel()`). No new plurals, drawables or icons outside Material Icons Extended.

### 5.9 MVI contract

**Settings:**

```kotlin
@Immutable
data class SettingsState(
    val isLoading: Boolean = true,                    // until the first settings emission
    val weekStartSegments: ImmutableList<SegmentUIState<WeekStartOption>> = persistentListOf(),
    val timeFormatSegments: ImmutableList<SegmentUIState<TimeFormatOption>> = persistentListOf(),
    val themeOptions: ImmutableList<ThemeOptionUIState> = persistentListOf(),
    val alerts: AlertsPreferenceUIState? = null,
    val sourceCodeLabel: UiText? = null,              // set from AppInfo in the initial _state
    val versionLabel: UiText? = null,
)

sealed interface SettingsIntent {
    data object ScreenStarted : SettingsIntent       // first composition and every return (re-reads the alerts status)
    data object BackClicked : SettingsIntent
    data class WeekStartSelected(val option: WeekStartOption) : SettingsIntent
    data class TimeFormatSelected(val option: TimeFormatOption) : SettingsIntent
    data class ThemeSelected(val option: ThemeOption) : SettingsIntent
    data class AlertsToggled(val enabled: Boolean) : SettingsIntent
    data object AllowNotificationsClicked : SettingsIntent
    data class NotificationPermissionResult(val granted: Boolean) : SettingsIntent
    data object AboutClicked : SettingsIntent
}

sealed interface SettingsEffect {
    data object NavigateBack : SettingsEffect
    data class OpenUrl(val url: String) : SettingsEffect
    data object RequestNotifications : SettingsEffect  // the permission dialog while Android shows it, else the app's notification settings
    data class ShowError(val error: ErrorUIModel) : SettingsEffect
}

@KoinViewModel
class SettingsViewModel(
    observeSettings: ObserveSettingsUseCase,
    getAppInfo: GetAppInfoUseCase,
    private val updateWeekStart: UpdateWeekStartUseCase,
    private val updateTimeFormat: UpdateTimeFormatUseCase,
    private val updateThemeMode: UpdateThemeModeUseCase,
    private val updateNewEpisodeAlerts: UpdateNewEpisodeAlertsUseCase,
    private val getReleaseAlertsStatus: GetReleaseAlertsStatusUseCase,   // feature/bookmarks/domain
) : ViewModel()
```

Private: `appInfo` (for `AboutClicked`), `alertsStatus = MutableStateFlow<ReleaseAlertsStatus?>(null)`, the `…Option.toWeekStart()` / `toTimeFormat()` / `toThemeMode()` conversions (as Bookmarks' `toBookmarkSort()`). No `Load` intent: the settings stream is collected by `stateIn`, and `ScreenStarted` fires on first composition.

**Reducer rules:**
- **state** = `combine(_state, observeSettings(), alertsStatus) { s, settings, status -> s.copy(isLoading = false, weekStartSegments = settings.weekStart.toWeekStartSegments(), timeFormatSegments = …, themeOptions = settings.themeMode.toThemeOptionUIStates(), alerts = settings.toAlertsPreferenceUIState(status)) }`.
- **`ScreenStarted`**, **`NotificationPermissionResult(_)`** → `refreshAlertsStatus()` (`alertsStatus.value = getReleaseAlertsStatus()` in `viewModelScope`). No snackbar on a denial: the row shows "blocked".
- **`WeekStartSelected` / `TimeFormatSelected` / `ThemeSelected`** → the matching update use case; `onFailure` → `ShowError(error.toErrorUIModel())`. No optimistic state: the store echoes the value back within milliseconds, and a failed write leaves the control on the stored value.
- **`AlertsToggled(enabled)`** → `updateNewEpisodeAlerts(enabled)`: failure → `ShowError`; success → `refreshAlertsStatus()`, then if `enabled` and the status is `BLOCKED` → `RequestNotifications`.
- **`AllowNotificationsClicked`** → `RequestNotifications`.
- **`AboutClicked`** → `OpenUrl(appInfo.sourceCodeUrl)`. **`BackClicked`** → `NavigateBack`.

**App root:** `AppViewModel` exposes `state: StateFlow<AppState>` only ([§5.4](#54-app)).

---

## 6. Dependencies

**Libraries:** none new. `androidx.datastore:datastore-preferences` 1.3.0 is already in `libs.versions.toml` and `app/build.gradle.kts` (kept by the Bookmarks plan for Settings); Material 3 already has `SingleChoiceSegmentedButtonRow`, `SegmentedButton` and `Switch`; the icons (`Devices`, `LightMode`, `DarkMode`, `Palette`, `Notifications`, `Code`, `OpenInNew`, `CheckCircle`, `CalendarMonth`, `Info`) are in Material Icons Extended. `androidx.core:core-splashscreen` is **not** needed ([§5.1](#51-how-the-settings-reach-the-app)); no `tech-stack` or `library-management` change.

**Earlier plans and shared code it uses:** `TopLevelAppBarView`'s settings action and every tab's `SettingsClicked` → `OpenSettings` path (plans 1–3, 5); `FullScreenNavKey` and the bar hiding (plan 4); `GetWeeklyScheduleUseCase(weekStart)` (plan 2); `ReleaseAlertNotifier`, `GetReleaseAlertsEnabledUseCase`, `NotificationPermission.kt`, the alerts chip (plan 5); `centeredContentPadding`, the skeleton pieces, `ErrorUIModel`.

**What it unblocks / closes:** the last hidden UI (the gear); the spec's "until Settings exists" defaults (Monday, 12-hour, system theme); 5-bookmarks.md's three Settings hooks (switch, sort, 24-hour chips); context.md §3.4's unsaved sort. Later: a locale-aware date format could ride the same `UiText` mechanism.

---

## 7. Testing

### Fakes (test source set, mirroring the main tree)

- `common/domain/repository/FakeSettingsRepository.kt`: `val settings = MutableStateFlow(AppSettings())`, `var failWrites = false` (→ `Result.failure(DomainError.Unknown(IOException()))`), records writes.
- `common/data/local/FailingPreferencesDataStore.kt`: a hand-written `DataStore<Preferences>` whose `data` throws `IOException` and whose `updateData` throws `IOException`.
- `feature/bookmarks/domain/repository/FakeReleaseAlertNotifier` (exists; `enabled` toggles the system state).
- Phase 5: `feature/bookmarks/domain/repository/FakeBookmarkSortRepository.kt`.

### Unit tests

| Class | Cases |
|---|---|
| `common/data/local/SettingsLocalDataSourceTest` | Real DataStore (`PreferenceDataStoreFactory.create(scope = backgroundScope) { File(tmp.root, "settings.preferences_pb") }`, JUnit `TemporaryFolder`): empty file → `AppSettings()`; each setter round-trips; an unknown stored name (`"FRIDAY"`, `"SEPIA"`) → the default; with `FailingPreferencesDataStore` → `AppSettings()` (read `IOException`), and a write throws `IOException`; no duplicate emission for an unchanged write. |
| `common/data/local/SettingsDataStoreFactoryTest` | A file of garbage bytes → `createSettingsDataStore` reads `emptyPreferences()` (corruption handler). |
| `common/data/util/SafeDataStoreCallTest` | Success; `IOException` → `DomainError.Unknown` with the cause; `CancellationException` rethrown; `IllegalStateException` propagates. |
| `common/data/repository/SettingsRepositoryImplTest` | Reads pass through; a failing write → `Result.failure(DomainError.Unknown)`; a success → `Result.success(Unit)`. |
| `common/domain/usecase/ObserveSettingsUseCaseTest`, `GetSettingsUseCaseTest` | Emits / returns the repository's settings (fake). |
| `common/ui/text/TimeTextTest` | `toClockText`: 00:00 → "12:00 AM" / "00:00"; 08:05 → "8:05 AM" / "08:05"; 12:15 → "12:15 PM" / "12:15"; 23:59 → "11:59 PM" / "23:59". |
| `common/ui/mapper/TimeUIModelMapperTest` | **Update:** `LocalTime(8, 5).toTimeLabel() == UiText.TimeValue(LocalTime(8, 5))`; an instant in New York / Tokyo → `TimeValue(LocalTime(9, 33))` / `TimeValue(LocalTime(23, 33))`. |
| 6 feature mapper / ViewModel tests that assert times | **Update** `StringValue("6:30 PM")` → `TimeValue(LocalTime(18, 30))`: `BookmarkCardUIModelMapperTest`, `EpisodeUIModelMapperTest`, `HomeViewModelTest`, `ReleaseRowUIModelMapperTest`, `UpcomingCardUIModelMapperTest`, `ScheduleCardUIModelMapperTest`. |
| `feature/settings/data/repository/AppInfoRepositoryImplTest` | `versionName == BuildConfig.VERSION_NAME`; the source URL. |
| `feature/settings/domain/usecase/*UseCaseTest` (5) | Each update passes its value and returns the repository's result (success and failure); `GetAppInfoUseCase` returns the repository's `AppInfo`. |
| `…/segmentedpreference/mapper/SegmentUIModelMapperTest` | Order, labels, the "(Default)" / "(AM/PM)" qualifiers, exactly one selected per value. |
| `…/themeoptions/mapper/ThemeOptionUIModelMapperTest` | Three options in order; the selected one has the filled icon. |
| `…/alertspreference/mapper/AlertsPreferenceUIModelMapperTest` | Switch on + `ON` / `null` → checked, not blocked; switch off + any status → unchecked, not blocked; switch on + `BLOCKED` → blocked text. |
| `feature/settings/presentation/mapper/AppInfoUIModelMapperTest` | "github.com/zahichemaly/BakaMitai"; `StringResource(settings_footer_version, listOf("1.0"))`. |
| `feature/settings/presentation/SettingsViewModelTest` | Initial loading → content from the fake's settings; each selection writes the matching domain value; a failing write → `ShowError` and the state keeps the stored value; `ScreenStarted` reads the status (blocked → `alerts.isBlocked`); `AlertsToggled(true)` with `BLOCKED` → `RequestNotifications`, with `ON` → no effect; `AlertsToggled(false)` → no effect; `AllowNotificationsClicked` → `RequestNotifications`; `NotificationPermissionResult` re-reads; `AboutClicked` → `OpenUrl("https://github.com/zahichemaly/BakaMitai")`; `BackClicked` → `NavigateBack`; a settings change from outside (fake emits) updates the state. |
| `app/AppViewModelTest` | `AppState()` (not loaded) → loaded; SYSTEM → `darkThemeOverride == null`, LIGHT → `false`, DARK → `true`; `TWENTY_FOUR_HOUR` → `use24HourClock`; a later change updates the state. |
| `feature/schedule/domain/usecase/GetWeeklyScheduleUseCaseTest` | **Update** to the required `weekStart` (cases already cover Monday and Sunday). |
| `feature/schedule/presentation/ScheduleViewModelTest` | **Update** (fake settings); new: Sunday start → tabs start on Sunday; `ScreenStarted` after the week start changed → reload in the new order, the same weekday stays selected; unchanged week start and day → no reload. |
| `feature/bookmarks/domain/model/ReleaseAlertsStatusTest` | The four combinations of `of(…)`. |
| `feature/bookmarks/domain/usecase/GetReleaseAlertsStatusUseCaseTest` | Switch off → `OFF` (whatever the notifier says); on + notifier off → `BLOCKED`; on + on → `ON`. |
| `feature/bookmarks/domain/usecase/GetReleaseAlertsEnabledUseCaseTest` | **Rewrite:** `true` only for `ON`. |
| `…/bookmarksheader/mapper/BookmarksHeaderUIModelMapperTest` | **Update:** the three chip states and descriptions. |
| `feature/bookmarks/presentation/BookmarksViewModelTest` | **Update:** chip from the status; `AlertsChipClicked` → `OpenSettings` for `ON` and `OFF`, `EnableAlerts` for `BLOCKED`; no `OpenNotificationSettings`. Phase 5: the stored sort is the initial sort; `SortSelected` writes it; a failing write → `ShowError`. |
| `feature/details/presentation/DetailsViewModelTest` | New: first bookmark with the switch off → no `AskForReleaseAlerts`; with `BLOCKED` or `ON` → `AskForReleaseAlerts` (as today). |
| Phase 5: `BookmarkSortLocalDataSourceTest`, `BookmarkSortRepositoryImplTest`, `ObserveBookmarkSortUseCaseTest`, `UpdateBookmarkSortUseCaseTest` | Default, round-trip, unknown name → `RECENTLY_ADDED`, read failure → default, write failure → `DomainError.Unknown`. |
| `app/navigation/NavKeySerializationTest` | Add `SettingsNavKey`. |
| `app/di/KoinModulesTest` | `verify()` with the new modules; `koin.get<GetAppInfoUseCase>()`. |

### Instrumented tests (`connectedDebugAndroidTest`)

- `common/ui/text/UiTextTimeValueTest` (Compose rule, `createComposeRule()`): `UiText.StringResource(bookmarks_airing_today, listOf(UiText.TimeValue(LocalTime(21, 30))))` renders "TODAY 9:30 PM" under `LocalUse24HourClock provides false` and "TODAY 21:30" under `true`; flipping the provided value recomposes the text.
- `feature/bookmarks/data/worker/ReleaseCheckWorkerTest` (**update**): a private in-memory `SettingsRepository` for `GetReleaseAlertsStatusUseCase`; new case: switch off → the run makes no request, posts nothing and schedules the next check.

### Compose previews

- `SettingsScreen`: loading, defaults, Sunday / 24-hour / Dark / alerts off, alerts blocked × `@AdaptivePreviews` (light/dark × compact/expanded).
- `SegmentedPreferenceView` (both selections, both preferences) and its skeleton; `ThemeOptionsView` (each selection) and its skeleton; `AlertsPreferenceView` (on / off / blocked) and its skeleton; `SettingsSectionView`; `AboutRowView`; `SettingsFooterView`.
- `TopLevelAppBarView` (updated provider). Optional: one Home preview inside `CompositionLocalProvider(LocalUse24HourClock provides true)` to see 24-hour cards.
- Compose reports: `SettingsState`, `SegmentUIState`, `ThemeOptionUIState`, `AlertsPreferenceUIState`, `AppState` stable; every new Screen and View skippable.

---

## 8. Implementation steps

### Phase 0: shared prerequisites

1. [x] **Settings domain in `common/`:** `AppSettings`, `WeekStart`, `TimeFormat`, `ThemeMode`, `SettingsRepository`, `ObserveSettingsUseCase`, `GetSettingsUseCase`, `FakeSettingsRepository`, their tests.
   **Done when:** the two use-case tests pass and the `architecture` §1 domain-purity grep prints nothing.
2. [x] **Settings store:** `createSettingsDataStore`, `safeData()`, `safeDataStoreCall`, `SettingsLocalDataSource`, `SettingsRepositoryImpl`, the `CommonModule` provider, `FailingPreferencesDataStore`, their tests.
   **Done when:** `SettingsLocalDataSourceTest`, `SettingsDataStoreFactoryTest`, `SafeDataStoreCallTest`, `SettingsRepositoryImplTest` and `KoinModulesTest` pass.
3. [x] **Time format through `UiText`:** `UiText.TimeValue` + `asString(context, use24HourClock)`, `TimeText.kt`, `toTimeLabel()` → `TimeValue`; `TimeTextTest`; update `TimeUIModelMapperTest` and the six tests that assert times; `UiTextTimeValueTest` (instrumented).
   **Done when:** `./gradlew testDebugUnitTest` passes, and the app (no provider yet → 12-hour) shows the same times as before on Home and Schedule.
4. [x] **`openLink` to `common/ui/util/OpenLink.kt`;** `DetailsRoute` uses it.
   **Done when:** it builds, Detekt is clean, and Details' "Open in web" still opens the browser.

### Phase 1: the app root

5. [x] **`AppState` + `AppViewModel`**, `AppModule`'s `@ComponentScan("com.zc.bakamitai.app")`, `AppViewModelTest`.
   **Done when:** `AppViewModelTest` and `KoinModulesTest` pass.
6. [x] **`AppRoute` and `MainActivity`:** composition gated on `isLoaded`, `BakaMitaiTheme(darkTheme)`, `LocalUse24HourClock`, `onThemeApplied` (first-frame hold, bar styles, contrast off).
   **Done when:** after `android run --apks=app/build/outputs/apk/debug/app-debug.apk`, a cold start shows no blank frame or flicker after the splash, the system bars look as before in both system modes, and rotation keeps the theme. (Forced modes are checked in step 23, once Settings can set them.)

### Phase 2: non-UI for Settings and its hooks

7. [x] **`feature/settings` domain:** `AppInfo`, `AppInfoRepository`, the five use cases + tests.
   **Done when:** the use-case tests pass and the domain-purity grep prints nothing.
8. [x] **`feature/settings` data and di:** `AppInfoRepositoryImpl` (+ test), `SettingsModule`, `AppModule` includes it.
   **Done when:** `AppInfoRepositoryImplTest` and `KoinModulesTest` pass.
9. [x] **Bookmarks alerts status:** `ReleaseAlertsStatus`, `GetReleaseAlertsStatusUseCase`, `GetReleaseAlertsEnabledUseCase` (suspend), `ReleaseAlertNotifier` KDoc; tests; `ReleaseCheckWorkerTest` updated with the switch-off case.
   **Done when:** the bookmarks domain tests pass and `./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.zc.bakamitai.feature.bookmarks.data.worker.ReleaseCheckWorkerTest` passes on an emulator.
10. [x] **Schedule week start:** `GetWeeklyScheduleUseCase` (required `weekStart`), `ScheduleViewModel` (`GetSettingsUseCase`, `reloadIfStale()`), tests.
    **Done when:** `GetWeeklyScheduleUseCaseTest` and `ScheduleViewModelTest` pass.
11. [x] **Phase gate:** `./gradlew testDebugUnitTest`, `./gradlew detekt`, `./gradlew assembleDebug`, the `architecture` §1 greps.
    **Done when:** all pass with zero Detekt findings.

### Phase 3: the Settings screen

12. [x] **Resources** of [§5.8](#58-resources) (new and changed strings).
    **Done when:** it builds and the `conventions` §2.5 grep prints nothing new.
13. [x] **Presentation models and mappers:** the three option enums, `SegmentUIState`, `ThemeOptionUIState`, `AlertsPreferenceUIState`, their mappers, `AppInfoUIModelMapper`; tests.
    **Done when:** the four mapper tests pass.
14. [x] **MVI and ViewModel:** `SettingsState`, `SettingsIntent`, `SettingsEffect`, `SettingsViewModel`, `SettingsViewModelTest`.
    **Done when:** every ViewModel case in §7 passes.
15. [ ] **Components:** `SettingsSectionView`, `SegmentedPreferenceView`, `ThemeOptionsView`, `AlertsPreferenceView` (each data component with its skeleton), `AboutRowView`, `SettingsFooterView`, all with `@AdaptivePreviews`.
    **Done when:** the previews render in light and dark, compact and expanded, the segmented track matches [§2.3](#23-general-preferences) (48 dp, ~4 dp around the selected pill), and `./gradlew detekt` is clean.
16. [ ] **Screen and Route:** `SettingsScreen` (top bar, centered column, per-section skeletons, snackbar host) with its preview provider; `SettingsRoute` (permission requester, `LifecycleStartEffect`, effects).
    **Done when:** all preview states render; Detekt is clean.
17. [x] **Navigation:** `SettingsNavKey`, `SettingsNavigation` (`navigateToSettings`, `settingsEntry`), `AppEntryProvider`, `NavKeySerializationTest`.
    **Done when:** `NavKeySerializationTest` passes.

### Phase 4: hooks in the other features

18. [x] **The gear:** `TopLevelAppBarView` without `showSettingsAction`; Home, Schedule, All Shows and Bookmarks Routes, Screens, entries and previews with a non-null `onOpenSettings`; `AppEntryProvider` passes `navigator::navigateToSettings`.
    **Done when:** it builds, `grep -rn "showSettingsAction\|onOpenSettings = null" app/src` prints nothing, and on the emulator the gear on each tab opens Settings with the bottom bar hidden; Back returns to the same tab and scroll position.
19. [x] **Bookmarks chip and Details ask:** `toAlertsChipUIModel()` from the status, `BookmarksViewModel`, `BookmarksEffect` / `BookmarksRoute` without `OpenNotificationSettings`, `DetailsViewModel`; the changed strings; tests.
    **Done when:** `BookmarksViewModelTest`, `BookmarksHeaderUIModelMapperTest` and `DetailsViewModelTest` pass.

### Phase 5: persist the Bookmarks sort

20. [x] **Sort store:** `BookmarkSortLocalDataSource`, `BookmarkSortRepository` / `Impl`, `ObserveBookmarkSortUseCase`, `UpdateBookmarkSortUseCase`, the `BookmarksViewModel` change; tests. (Separable: skipping this phase leaves everything else intact; then drop the context.md §3.4 edit in step 22.)
    **Done when:** the Phase 5 tests pass, and on the emulator a "Title A–Z" sort survives a force stop and relaunch.

### Phase 6: docs and verification

21. [x] **Skills** (done 2026-11-19 by the main session, from the implementor's proposed edits):
    - `structure` §1 tree: `app/AppState.kt`; `common/ui/text/TimeText.kt`; `common/data/util/SafeDataStoreCall.kt`. §2 rows: `LocalUse24HourClock` and the clock formats `UiText` resolves with → `common/ui/text/TimeText.kt`; `safeDataStoreCall` → `common/data/util/`; the settings DataStore factory and `safeData()` → `common/data/local/`; `Context.openLink` → `common/ui/util/OpenLink.kt`. §7 "Current repo state": Settings built (`feature/settings/`, a pushed `FullScreenNavKey` screen), the settings store and `AppSettings` in `common/`, `AppViewModel`, the gear shown on every tab, `TopLevelAppBarView` has no `showSettingsAction`.
    - `conventions` §2.5: `UiText.TimeValue(LocalTime)` ("times are `TimeValue`, never preformatted strings: they follow the user's clock format"), `asString(context, use24HourClock)`; the `UiText` sketch.
    - `architecture` §3: the app root's `AppViewModel` has one `StateFlow` and no Intent/Effect (not a screen).

    **Done when:** the skills describe what was built.
22. [x] **Docs** (done 2026-11-19, separately from the implementor):
    - `docs/context.md`: §3.2 (tab order follows the setting; drop "until Settings exists"); §3.4 (the sort is saved; the alerts chip opens Settings when alerts are on or switched off); §3.6 (the final table, with *New-episode alerts* On/Off default On, the About URL, the footer "BakaMitai v{versionName}" and disclaimer, no toasts); §3.7 (the global switch: off → no checks' requests, no permission ask); §4.2 ("Until Settings exists…" goes); §4.5 (times follow *Time format* everywhere; the notification timestamp is drawn by the system); §5 (Settings shape with `newEpisodeAlerts`, the `settings` DataStore file and its keys incl. `bookmarks_sort`; the theme applies before the first frame through `AppRoute`/`AppViewModel`).
    - `docs/api.md` §7: the alerts exception's wording ([§4.1](#41-endpoints)).
    - `CLAUDE.md` "Current state": Settings is the sixth feature (plan 6-settings.md); the gear is shown; the week start, time format and theme follow the settings; delete "Until Settings exists, the week starts on Monday and times are 12-hour" and "the Settings gear is hidden"; the Bookmarks destination is no longer described as disabled.

    **Done when:** the docs describe the shipped behaviour.
23. [ ] **Emulator pass** of [§9](#9-acceptance-criteria) on a phone (gesture and 3-button navigation, light and dark system modes, API 33+ for the permission flow) and a tablet-size emulator or resizable window for the rail and the centered column.
    **Done when:** every criterion in §9 is checked.
24. [x] **Final verification**, in order:
    ```bash
    ./gradlew testDebugUnitTest
    ```
    ```bash
    ./gradlew detekt
    ```
    ```bash
    ./gradlew assembleDebug
    ```
    ```bash
    ./gradlew assembleRelease && grep -rn "unstable" app/build/compose_compiler/*-composables.txt
    ```
    ```bash
    ./gradlew connectedDebugAndroidTest
    ```
    Then the `architecture` §1 greps, the `structure` §7 checks, the `tech-stack` verification greps and the `conventions` §2.5 grep.

    **Done when:** tests and Detekt pass with zero findings, the `unstable` grep shows nothing for the new Screen and Views (the known `ObserveAsEvents` line excepted), and every grep prints nothing.

---

## 9. Acceptance criteria

**Navigation**
- [ ] Home, Schedule, All Shows and Bookmarks each show the gear in the top bar ("Settings" for TalkBack); a tap opens Settings over that tab.
- [ ] On a phone, the bottom bar slides away on Settings and comes back on Back; on a tablet the rail stays and the current tab stays selected.
- [ ] Back (arrow or system gesture) returns to the same tab, scroll position and state; a fast double tap on the gear opens one Settings.
- [ ] Settings survives rotation and process death (it's still on top after "Don't keep activities" or a process kill).

**Defaults and persistence**
- [ ] A fresh install shows Monday, 12-hour, System default and alerts on.
- [ ] Every choice survives a force stop and relaunch.

**First day of week**
- [ ] Sunday → Schedule's tabs read SUN … SAT with this week's dates (on Thu 2026-11-19: Sun 15 … Sat 21), today selected; back to Monday → MON 16 … SUN 22. The change shows when returning to Schedule, without a manual refresh.

**Time format**
- [ ] 24-hour → on return, Home's upcoming cards and release rows, Schedule's card chips, Bookmarks' airing chips ("TODAY 21:30", "MON 21:30") and Details' release lines show "21:30"-style times; 12-hour shows "9:30 PM". No reload or network request is needed (Ktor log).

**Theme**
- [ ] Light / Dark / System default recolor the whole app instantly, Settings included.
- [ ] Status-bar and navigation-bar icons are dark on the light theme and light on the dark theme **whatever the system mode** (check Light with the system in dark mode, and Dark with the system in light mode), with gesture and 3-button navigation; the navigation bar still matches the bottom bar (no scrim).
- [ ] Cold start with Dark forced while the system is light (and the reverse): the first app frame after the splash is already in the chosen theme (no flash). The splash itself follows the system (accepted, Q13).
- [ ] System default follows a system dark-mode toggle while the app is open.

**New-episode alerts**
- [ ] Switch off → the Bookmarks chip says "ALERTS OFF" and opens Settings; a forced release check (`cmd jobscheduler run -f …`, as 5-bookmarks.md §13) makes no SubsPlease request and posts nothing; the first bookmark from Details doesn't show the permission dialog.
- [ ] Switch on, with the permission never asked (Android 13+, `pm clear`) → the permission dialog opens; Allow → the row is normal and the chip says "ALERTS ON"; Deny → the row says "Notifications are blocked for BakaMitai." with **Allow notifications**, which shows the dialog again, or the system settings once Android stops showing it.
- [ ] After allowing in system settings and coming back, the row is normal without leaving Settings.
- [ ] "ALERTS ON" on Bookmarks opens Settings.

**About and footer**
- [ ] About BakaMitai opens `https://github.com/zahichemaly/BakaMitai` in the browser (TalkBack: "double-tap to open in browser").
- [ ] The footer says "BakaMitai v1.0" (the build's `versionName`) and the disclaimer.

**Layout, states and accessibility**
- [ ] Light and dark, compact and expanded: one column, 16 / 24 dp margins, centered at 720 dp on wide windows; the footer ends above the system navigation bar.
- [ ] At font scale 2.0 nothing is clipped (descriptions wrap; segment qualifiers ellipsize).
- [ ] TalkBack: section titles are headings; segments and theme rows are radio buttons with their selected state; the alerts row is a switch; decorative icons are silent.
- [ ] No loading spinner anywhere; the skeleton (if seen at all) has the real section headers.

**Bookmarks sort (Phase 5)**
- [ ] The chosen sort is the one shown after a relaunch.

---

## 10. Open questions

**Needs the user:** nothing blocks. No new library, no build change. Every row below has a recommendation the plan already follows; the user may want to confirm Q1–Q3 (dropping the design's Data & Storage actions) and Q7 (the footer disclaimer).

| # | Needs user | Question | Recommendation |
|---|---|---|---|
| Q1 | No | Keep the "Data & Storage" section? | **No; replace it with "Notifications"** (the alerts switch). Its contents aren't in the spec (Q2–Q4). |
| Q2 | No | Show the bookmark count ("8 saved titles")? | **No.** It's real but leads nowhere here; Bookmarks already shows it. The "12 KB" size is dropped either way. |
| Q3 | No | "Clear Local Data"? | **Not in v1** (destructive, not in the spec; per-card remove with Undo exists). If wanted: an M3 `AlertDialog` naming the count ("Remove all 8 bookmarks? This can't be undone."), Cancel / Remove in `error`, a `RemoveAllBookmarksUseCase` that also calls `requestCheck()` so the alert chain ends. |
| Q4 | No | "Export (JSON)"? | **Not in v1**: needs the Storage Access Framework (`ACTION_CREATE_DOCUMENT`), a versioned file format and an import to be useful. A good follow-up plan together with import. |
| Q5 | No | "Check for Updates"? | **Drop.** No update service exists; Play (or the APK source) handles updates. |
| Q6 | No | The GitHub logo on the About row? | **`Icons.Outlined.Code`.** Icons come from Material Icons Extended, which has no brand logos; a GitHub mark would be a trademarked drawable. |
| Q7 | No | The footer tagline? | **Replace** with "Unofficial app. Release data from subsplease.org.": accurate, and fitting for an unofficial client. Dropping the line entirely is the alternative. |
| Q8 | No | How does the time format reach every mapper? | **`UiText.TimeValue` + `LocalUse24HourClock`** ([§5.1](#51-how-the-settings-reach-the-app)): no ViewModel changes, instant updates on open screens. Deviates from 2-schedule.md's hint (a mapper parameter), deliberately. |
| Q9 | No | How does Schedule pick up a new week start? | **Read at each load, re-checked on `ScreenStarted`.** Settings is pushed, so every return passes through a screen start. Deviates from 2-schedule.md's "observed setting". |
| Q10 | No | Cancel the `release-check` work when alerts are switched off? | **No**: the existing silent path already makes no request and keeps `resolved_through` moving, so switching back on can't fire alerts for episodes released while off. Deviates from 5-bookmarks.md §7's hint. |
| Q11 | No | What do the Bookmarks alerts chip taps do now? | **"ALERTS ON" and "ALERTS OFF" (switched off) → Settings**; "ALERTS OFF" because Android blocks notifications → the permission dialog / system settings, as today. Replaces 5-bookmarks.md Q20 ("open the notification settings until Settings has a switch"). |
| Q12 | No | Persist the Bookmarks sort in this plan? | **Yes** (Phase 5): context.md §3.4 ties it to Settings, it reuses this plan's store, and it's separable. |
| Q13 | No | The splash follows the system mode when the theme is forced. | **Accept for v1.** The app's own first frame is correct. Making the starting window follow the app needs per-app night mode (`UiModeManager.setApplicationNightMode`, API 31+), which persists outside the app and triggers configuration changes; worth a separate spike, not this plan. `core-splashscreen` wouldn't change it and isn't approved. |
| Q14 | No | Where does the version come from? | **`BuildConfig.VERSION_NAME` in `AppInfoRepositoryImpl`** (`feature/settings/data`), read through `GetAppInfoUseCase`: the ViewModel stays on use cases, tests can fake it, and `settings/data` has a real job (`structure` §1 requires all four layers). |
| Q15 | No | Theme labels? | **"System default", "Light", "Dark"** with plain descriptions ([§2.4](#24-appearance)); the mock's marketing subtitles and "Obsidian" are dropped. |
| Q16 | No | Toasts on every change? | **None.** The change is visible at once; a snackbar only when a save fails. |
| Q17 | No | The notification's timestamp ignores the app's time format. | **Accept**: System UI draws it in the system's 12/24-hour format; the notification text itself has no time. |
| Q18 | No | Should Details (a pushed screen) get a gear too? | **No.** context.md §2 puts it on top-level screens only. |
| Q19 | No | Option keys in the UI: the domain enums or presentation copies? | **Presentation enums** (`WeekStartOption`, `TimeFormatOption`, `ThemeOption`): `architecture` §4 keeps domain models (enums included) out of Composables; Bookmarks' `BookmarkSortOption` is the precedent. |
| Q20 | No | One `SkeletonView` per preference card instead of one for the screen? | **Yes**: the cards have surfaces, which would shimmer as blocks inside a single `SkeletonView`; one per section is what `best-practices` §5.3 allows. In practice the skeleton shows for a frame or two. |
| Q21 | No | Turning alerts on: also run a check right away? | **No.** The chain never stopped (Q10); the next slot's check runs as scheduled. |

---

## 11. Implementation notes

Implemented on 2026-11-19 (implementor), all phases including Phase 5. Deviations from the plan, with the reason:

- **Open questions:** Q1–Q21 settled with their recommendations (the user asked for the feature end to end). No library was added.
- **`AppRoute(onThemeApply = …)`**, not `onThemeApplied`: the Compose rules' `ParameterNaming` forbids past-tense lambda parameters. `MainActivity`'s handler keeps the name `onThemeApplied`. After every `enableEdgeToEdge(…)` (in `onCreate` and in `onThemeApplied`) navigation-bar contrast enforcement is turned off again on API 29+.
- **`LocalUse24HourClock`** carries a scoped `@Suppress("CompositionLocalAllowlist")` with its reason on the same line, instead of a new entry in `config/detekt/detekt.yml`'s `allowedCompositionLocals` (the implementor doesn't edit `detekt.yml`). Adding it to the allowlist next to `LocalExtendedColorScheme` is the alternative if the project prefers it.
- **`LocalTime.toTimeLabel()` keeps the minute only** (`TimeValue(LocalTime(hour, minute))`): release instants carry seconds, labels show minutes, and the plan's expectations (`TimeValue(LocalTime(9, 33))` for 14:33:47 UTC) need equal labels to compare equal.
- **`Preferences.enumOrNull<E>(key)`** joins `safeData()` in `common/data/local/PreferencesDataStoreExtensions.kt`; both preference data sources use it for "unknown or missing name → default".
- **Schedule:** no `loadedWeekStart` field. `reloadIfStale()` compares the stored week start with the loaded week's first day (`schedule.days.first().dayOfWeek`), the same information without extra state; the read runs in a coroutine because `GetSettingsUseCase` suspends.
- **Notification permission in `SettingsRoute`:** a private `rememberNotificationEnabler` replaces the plan's `canAskNotificationPermission() ? request : openAppNotificationSettings()`. With the plan's version, once Android stops showing the dialog the request is denied at once and nothing visible happens, so §9's "or the system settings once Android stops showing it" couldn't be met. It reads `shouldShowRequestPermissionRationale` before and after each request: false both times with a denial means no dialog was shown, so the app's notification settings open. Checked on the emulator (dialog, denial, then the system settings on the next tap).
- **Bookmarks:** `checkAlerts()` launches a coroutine (the status read suspends) and keeps the status in a private `alertsStatus`; the chip tap reads that instead of `alertsChip.isEnabled`. Previews use the three statuses. `BookmarkSort`'s KDoc now says it's stored by name.
- **Layout details from the design's HTML** (the plan's §2.0 had no value): section headers use `spacedBy(spacing.xs)` and `padding(horizontal = spacing.xs)` (`gap-space-xs px-space-xs`); a preference's heading and control are `spacedBy(spacing.sm)`.
- **Segmented track:** vertical track padding 0, horizontal 4 dp. Measured on the emulator: a 48 dp track (the segments' touch height) with a 40 dp selected pill and ~4 dp of track around it.
- **Skeletons:** each preference card wraps its content in its own `SkeletonView`, with the preference headings rendered for real inside it (as Home renders its section headers inside its skeleton). `ThemeOptionsSkeletonView`'s rows are `max(52 dp, title line + description line + padding)` tall rather than a fixed 52 dp, so they match the real rows (~56 dp) at every font scale.
- **Tests added beyond §7:** `FakeAppInfoRepository`, `SettingsLocalDataSourceTest`'s no-duplicate case written as "the next item is the following write's", `TimeUIModelMapperTest`'s seconds case, two extra `DetailsViewModelTest` cases (blocked → ask, switched off → no ask). Instrumented test names are camelCase, like the existing ones.
- **Steps left:** step 21 (skills `structure`, `conventions`, `architecture`) and step 22 (context.md, api.md §7, CLAUDE.md, `structure` §7 "Current repo state") were not edited by the implementor; the caller applies them. Steps 15–16: the previews weren't rendered (Android Studio had another project open); every component and the screen were checked on the emulator instead, in dark and light.
- **Emulator checks (`emulator-5554`, API 37, 3-button navigation):** the gear on all four tabs opens Settings with the bottom bar hidden, and Back keeps the tab and its scroll position; the segmented track; Light forced with the system dark and Dark forced with the system light (instant recolor, status- and navigation-bar icons follow the app theme, the navigation bar has no scrim and matches the bottom bar); cold starts recorded frame by frame in both forced cases: the system splash follows the system mode and reveals an app frame already in the chosen theme, with no frame of the other theme; System default follows a live system dark-mode toggle; the Activity recreation keeps Settings and the theme; 24-hour times on Home and Schedule; Sunday week start (SUN 15 … SAT 21, Thursday kept selected); the Bookmarks sort and every setting survive a force stop; the alerts chip opens Settings and says ALERTS OFF when switched off; a forced release check with alerts off ran with no SubsPlease request and scheduled the next check; the blocked row, the dialog, and the system settings once Android stops showing it; the row re-reads after coming back from the system settings; About opens the GitHub page; Details' "Open in web" still works; a landscape window (914 dp) centers the column at ~718 dp; fresh-install defaults.
- **Not checked on a device:** gesture navigation, a tablet or resizable emulator (the rail), font scale 2.0, TalkBack, process death with "Don't keep activities".
