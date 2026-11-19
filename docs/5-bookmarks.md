# 5. Bookmarks

> **Status:** Implemented (2026-11-19; docs steps 16–18 left to the user, see [§12](#12-implementation-notes)) · **Design:** [Google Stitch project `5201581584167289517`, screen `8c44723bac0149fb8b6e2e7fbc6bd10b` ("My Bookmarks")](https://stitch.google.com/projects/5201581584167289517?node-id=8c44723bac0149fb8b6e2e7fbc6bd10b) · **Planned:** 2026-11-19
> **Spec:** [context.md §2](context.md#2-navigation-shell), [§3.4](context.md#34-bookmarks), [§4.5](context.md#45-formatting-rules-all-screens), [§5](context.md#5-local-persistence), [§6](context.md#6-suggested-feature-breakdown), [§7](context.md#7-open-decisions) · **API:** [api.md §3.2](api.md#32-weekly-schedule), [§3.3](api.md#33-latest-releases), [§3.4](api.md#34-search-releases), [§3.5](api.md#35-show-episodes), [§7](api.md#7-client-guidelines)
> **Depends on:** [1-home.md](1-home.md) (`BroadcastRepository`, `Release`), [2-schedule.md](2-schedule.md) (`ScheduleRepository` and its 1 h weekly cache, day-name mapper, the card's time chip), [3-shows.md](3-shows.md) (`EmptyStateView`), [4-details.md](4-details.md) (`feature/bookmarks/{data,domain,di}`, `ToggleBookmarkUseCase`, `DetailsNavKey`, the "Nov 17, 2026" date format). All implemented and committed.

**Design source.** A Google Stitch project, not Figma or Sketch, captured by the requester (not reopened for this plan): a render (`screen.jpg`, 743×2895, a 390×1520 dark frame at ~1.9×) and a condensed copy of the HTML/Tailwind with Stitch's own prompt (`code.html`). The render starts at the counter row: the app bar and the sort strip exist only in the HTML. The HTML's header comment says 390×1756 (the brief says 390×1520); the render matches 1520. Nodes below name that screen plus its HTML sections: `<header>`, the sort strip (`.filter-pill`), the counter row (`#bookmarkCount`), the grid `#animeGrid > .bookmark-card` (poster, top-left status badge, `.bookmark-btn`, bottom "added" label + `h3` title, subtitle `p`, tag + rating row), `#emptyState` (hidden), `#toast`, and the bottom navigation. Loading, error, light, tablet and every notification surface are **not in the design** ([§2.8](#28-states-not-in-the-design-all-proposed)).

**How design and spec are reconciled** (same rule as the earlier plans): `context.md` and the user's decisions decide **behaviour and data**; the Stitch design decides **visual treatment**. Most of the mock's card content is invented (status badges, subtitles, tags, ratings, relative dates, "Sync Ready", "Tracking Watchlist", the manage mode, toast wording). Nothing the data can't back is shown; every disagreement is in [§3](#3-design-vs-spec).

**Decided by the user on 2026-11-19** (recorded as decisions, not open questions):

| # | Decision |
|---|---|
| D1 | **Room replaces the bookmarks DataStore.** It brings KSP (for Room's compiler only) next to the Koin compiler plugin. The skill and catalog updates are part of this plan ([§7](#7-dependencies), step 1–2). |
| D2 | **Start fresh.** The app hasn't shipped: the DataStore bookmark code and all `bookmarks.json` handling are deleted; no migration. |
| D3 | **Local notifications, fired when the episode is actually out**: after a bookmarked show's scheduled weekly time, a background check confirms the release on SubsPlease and notifies with the episode number. A delayed episode notifies late, never wrongly. |
| D4 | **WorkManager** (`androidx.work`) schedules the checks. |
| D5 | **Alerts are on for every bookmark.** On Android 13+ the app asks for `POST_NOTIFICATIONS` on the user's **first bookmark**. A global switch comes with the Settings plan (hook in [§6.6](#66-notifications-presentationnotification)). |
| D6 | **Tapping a notification opens that show's Details.** |

---

## 1. Summary

**Bookmarks** is the fourth tab: a grid of the shows the user saved from Details, newest first, with each show's **cover, title, date added** and **next airing** (weekday and time from the weekly schedule, or "Not airing"). The user can sort it (Recently added, Title A–Z, Release time), open a show's Details, and remove a bookmark (with Undo). It works offline: everything it shows is in a Room database on the device; covers come from Coil's cache.

On top of the grid, the app **notifies the user when a bookmarked show's new episode is out**. A single WorkManager job wakes up shortly after each bookmarked show's weekly slot, confirms the release with one request to SubsPlease's newest-releases feed (shared by every bookmark), retries on a fixed ladder for up to 24 h while the episode is late, and posts one notification per show ("Episode 8 is out"). Tapping it opens Details over the Home tab.

**Entry points:** the **Bookmarks** tab (the last disabled destination becomes enabled); a notification tap (→ Details); Details' bookmark button (adds/removes, and asks for the notification permission on the first bookmark).

**In scope:**
- `feature/bookmarks/presentation/` (screen, components, navigation) and the rewrite of its `data/` on Room ([§6.3](#63-featurebookmarksdata))
- release alerts: domain rules, the WorkManager worker and scheduler, the notifier, the deep link ([§5](#5-notification-feasibility), [§6](#6-architecture))
- Details: the permission ask on the first bookmark, the new toggle result ([§6.10](#610-details-changes))
- build: Room 2.8.5, KSP 2.3.14, WorkManager 2.12.0, `koin-androidx-workmanager`, a `bakamitai.android.room` convention plugin ([§6.2](#62-build))
- shared moves to `common/` for second users ([§6.1](#61-shared-code)), two small use cases each in Home and Schedule
- skill and doc updates (Room, KSP, WorkManager, notifications, new folders; api.md §7's polling exception)

**Out of scope, with hooks:**
- **Settings**: the global alerts switch, persisting the sort order, 24-hour times (hooks: `ReleaseAlertNotifier.areAlertsEnabled()`, `BookmarkSortOption`)
- **Exact-time alerts** (exact alarms, foreground services): rejected in [§5.5](#55-android-limits)
- **Alerts for shows that aren't on the weekly schedule** (finished, on break, movies): only opportunistic ([§5.1](#51-timing-source)); **batches** never notify
- Details' pre-fill from a saved bookmark and refreshing a bookmark's stored title/cover (4-details.md Q3/Q15): deferred ([Q14](#11-open-questions))
- Batch remove / manage mode, cover art in notifications ([Q18](#11-open-questions))

---

## 2. Design breakdown

### 2.0 Token mapping

| Design (Tailwind / DESIGN.md) | Compose |
|---|---|
| `bg-surface`, `text-on-surface` | `colorScheme.surface`, `onSurface` (Scaffold default) |
| Card `bg-surface-container-low rounded-2xl overflow-hidden shadow-md` | `Card(colors = cardColors(containerColor = surfaceContainerLow))`, `shapes.large` (24 dp), zero elevation (DESIGN.md: tonal layering, no drop shadows) |
| Poster `aspect-[3/4.2]` | `POSTER_ASPECT_RATIO` + `CoverImageView` (`common/ui/component/coverimage/`) |
| Scrim `bg-gradient-to-t from-surface-container-low via-surface-container-low/20 to-transparent` | `Brush.verticalGradient(0f to Transparent, 0.5f to surfaceContainerLow@0.2, 1f to surfaceContainerLow)` (`SCRIM_MIDDLE_ALPHA = 0.2f`) |
| Sort pill selected `bg-primary text-on-primary`, unselected `bg-surface-container-high text-on-surface-variant`, `font-label-md`, 15 px icon | `FilterChip` with Schedule's status-chip colors (`containerColor = surfaceContainerHigh`, `selectedContainerColor = primary`, labels `onSurfaceVariant` / `onPrimary`), `labelMedium`, 18 dp leading icon |
| Pill icons `schedule`, `sort_by_alpha`, `sensors`, `done_all` | `Icons.Outlined.Schedule`, `Icons.Outlined.SortByAlpha`, `Icons.Outlined.Sensors`; `done_all` dropped with "Completed" ([§3](#3-design-vs-spec) row 6) |
| Counter `font-label-lg text-on-surface` + 6 px `bg-secondary` dot + `font-label-md text-secondary` | `labelLarge` `onSurface`, a 6 dp `secondary` circle, `labelMedium` `secondary` |
| "SYNC READY" `font-label-sm uppercase text-on-surface-variant` + `offline_bolt` | Alerts chip ([§2.3](#23-counter-row-and-alerts-chip)): `labelSmall`, 14 dp `Icons.Outlined.NotificationsActive` (`secondary`) / `Icons.Outlined.NotificationsOff` (`tertiary`) |
| Top-left badge `rounded-full backdrop-blur-md font-label-sm` | `TimeChipView` (moved to `common/`, [§6.1](#61-shared-code)): `surfaceContainerLowest@0.8`, `CircleShape`, `labelSmall` |
| `.bookmark-btn w-8 h-8 rounded-full bg-surface-dim/75 text-primary`, filled `bookmark` 18 px | 32 dp `CircleShape` container `surfaceDim@0.75`, `Icons.Filled.Bookmark` 18 dp tinted `primary`, inside a 48 dp touch target |
| Added label `font-label-sm uppercase` + 11 px `event` / `history` / `calendar_today` | `labelSmall` `onSurfaceVariant`, 12 dp `Icons.Outlined.Event` (absolute dates only, [§3](#3-design-vs-spec) row 2) |
| Title `font-headline-sm font-bold line-clamp-1` | `headlineSmall` `onSurface`, `maxLines = 2`, ellipsis ([§3](#3-design-vs-spec) row 15) |
| Grid `grid-cols-2 gap-space-md px-margin` | `LazyVerticalGrid(GridCells.Fixed(2 / 3 / 4))`, both arrangements `spacedBy(spacing.gutter)`, horizontal padding `spacing.margin` / `marginTablet` from medium |
| `#emptyState` | shared `EmptyStateView` + filled `Button` ([§2.6](#26-empty-state)) |
| `#toast` `bg-surface-container-highest rounded-full` + `check_circle` | M3 `Snackbar` (theme default) with an "Undo" action ([§2.5](#25-remove-and-undo)) |

Dimensions are private `Dp` constants in their component files: `RemoveButtonSize = 32.dp`, `RemoveIconSize = 18.dp`, `AddedIconSize = 12.dp`, `AlertsIconSize = 14.dp`, `CountDotSize = 6.dp`, `SortIconSize = 18.dp`. Constants: `TITLE_MAX_LINES = 2`, `SCRIM_MIDDLE_ALPHA = 0.2f`, `REMOVE_CONTAINER_ALPHA = 0.75f`.

### 2.1 Top app bar and navigation suite

Node: `<header>` and the bottom navigation.

- **Layout:** the shared `TopLevelAppBarView(subtitle = bookmarks_app_bar_subtitle "BOOKMARKS", showSettingsAction = false until Settings exists, onSettingsClick, scrollBehavior = pinnedScrollBehavior())`, as Home, Schedule and All Shows. No `bottomContent`: the sort strip scrolls with the grid, as in the design.
- **Navigation suite:** `TopLevelDestination.BOOKMARKS` gets `key = BookmarksNavKey`; icons stay `Icons.Filled.Bookmark` / `Icons.Outlined.BookmarkBorder`. Bar on compact, rail on medium and up.
- **Accessibility:** the subtitle is not a heading (same as the other tabs).

### 2.2 Sort strip

Node: the `.filter-pill` strip.

- **Layout:** the grid's first full-span item: a horizontally scrolling `LazyRow` (contentPadding = the grid margin, `spacedBy(spacing.xs)`), top padding `spacing.xs`, bottom `spacing.sm`.
- **Components:** `FilterChip(selected, onClick, label, leadingIcon)`, single selection, part of `BookmarksHeaderView` ([§6.5](#65-featurebookmarkspresentation)).
- **Content:** three options (`BookmarkSortOption`): **Recently added** (default; `added_at` newest first), **Title (A–Z)** (case-insensitive), **Release time** (next airing soonest first; shows that aren't airing go last, by title). "Completed" is dropped: the app can't tell a finished show from one on break ([§3](#3-design-vs-spec) row 6).
- **States:** shown whenever there is at least one bookmark; hidden in the empty and error states. The selection lives in the ViewModel (survives tab switches and rotation; resets on process death, [Q10](#11-open-questions)).
- **Interactions:** tap → `SortSelected(option)`; the grid scrolls back to the top (`LaunchedEffect(state.selectedSort) { gridState.scrollToItem(0) }` in the Screen). No "Sorted by…" toast.
- **Accessibility:** `FilterChip` exposes the selected state; labels are the visible text.

### 2.3 Counter row and alerts chip

Node: the counter row (`#bookmarkCount`, "Sync Ready").

- **Layout:** a full-span `Row` (`SpaceBetween`, vertical padding `spacing.xs`): left, `bookmarks_header_title` "SAVED SHOWS" · dot · count; right, the alerts chip.
- **Content:** count = plural `bookmarks_count` ("1 show", "8 shows"). The alerts chip reflects whether notifications can be posted (`ReleaseAlertNotifier.areAlertsEnabled()`: `POST_NOTIFICATIONS` granted on 13+, app notifications on, channel not blocked):
  - **on:** "ALERTS ON", `Icons.Outlined.NotificationsActive`, `secondary`; tap → system app-notification settings (where the user can mute the channel until Settings has a switch).
  - **off:** "ALERTS OFF", `Icons.Outlined.NotificationsOff`, `tertiary`; tap → the enable flow: the permission dialog when Android can still show it (13+), otherwise the app-notification settings.
  - **unknown** (before the first check): hidden.
- **Components:** private `AlertsChip` in `BookmarksHeaderView.kt`: `Surface(onClick, shape = CircleShape, color = Transparent)` with 14 dp icon + `labelSmall`, `Modifier.minimumInteractiveComponentSize()`.
- **Accessibility:** the title row has `heading()`; the chip has `cd_bookmarks_alerts_on` "New-episode alerts are on. Opens notification settings" / `cd_bookmarks_alerts_off` "New-episode alerts are off. Turns them on" and `Role.Button`.

### 2.4 Bookmark card

Node: `#animeGrid > .bookmark-card` (8 cards; Frieren and Dandadan kept in the HTML).

- **Layout:** a poster-only card (the design's under-poster block holds only invented data, [§3](#3-design-vs-spec) rows 3–5). Poster `fillMaxWidth().aspectRatio(POSTER_ASPECT_RATIO)` with, inset `spacing.sm`:
  - **top-start:** the airing chip (`TimeChipView`), replacing the invented status badge
  - **top-end:** the remove button
  - **bottom:** a `Column` (`spacedBy(2.dp)`): the added label, then the title
- **Components:** `component/bookmarkcard/BookmarkCardView.kt`: `Card(onClick)` + `CoverImageView` + scrim `Box` + `TimeChipView` + `FilledIconButton`-styled remove button + texts. `BookmarkCardSkeletonView` in the same file.
- **Content** (`BookmarkCardUIModel`):
  - `title`: the stored title (Details' decoded page title), 2 lines, ellipsis
  - `coverUrl`: the stored absolute URL or `null` (placeholder poster)
  - `addedLabel`: "Added Nov 18, 2026" (`bookmarks_added_on` + the shared `toDateLabel()`, [§6.1](#61-shared-code))
  - `airing`: `TimeChipUIModel`: today → "TODAY 9:30 PM" (`TimeChipTone.UPCOMING`: `Icons.Outlined.Alarm`, `tertiary`); another day → "MON 9:30 PM" (`DEFAULT`: `Icons.Outlined.Schedule`, `primary` icon, `onSurface` text); not on the weekly schedule → "NOT AIRING" (`MUTED`: `Icons.Outlined.EventBusy`, `onSurfaceVariant`). The time is the next occurrence of the stored slot in the **device** zone, formatted by the shared 12-hour `toTimeLabel()`.
- **States:** content; skeleton (`BookmarkCardSkeletonView`: the same `Card` with one poster-sized `SkeletonBlock(RectangleShape)`; the real card has nothing below the poster).
- **Interactions:** tap → `BookmarkClicked(slug)` → Details. Remove button → `RemoveClicked(slug)` ([§2.5](#25-remove-and-undo)). Items use `Modifier.animateItem()` so removals fade and the grid reflows (replaces the design's shrink-and-fade script).
- **Theming:** scrim and colors as [§2.0](#20-token-mapping); identical roles in light (derived scheme).
- **Accessibility:** `Card(onClick)` merges the texts: "TODAY 9:30 PM, Added Nov 18, 2026, Frieren"; its click is labelled `cd_bookmarks_open` "Open details" through `Modifier.semantics { onClick(label = …, action = null) }` (Details' bookmark-button pattern). The remove button is its own node (`cd_bookmarks_remove` "Remove %1$s from bookmarks"); the card also exposes a `CustomAccessibilityAction` with the same label so TalkBack users can remove without moving focus. Cover `contentDescription = null` (title adjacent).

### 2.5 Remove and Undo

Node: `.bookmark-btn` and `#toast`.

- Tap the remove button → the bookmark is deleted right away (the card animates out) → a snackbar `bookmarks_removed` "Removed %1$s" with action `bookmarks_undo` "Undo", `SnackbarDuration.Short`.
- Undo restores the bookmark with its original date (so it returns to its place in "Recently added") and fresh alert tracking ([§5.4](#54-deduplication)). Only the last removal can be undone.
- Removing the last bookmark shows the empty state under the snackbar.
- The design's manage/batch mode (referenced only by its script) is not planned.

### 2.6 Empty state

Node: `#emptyState` (hidden by default).

- `EmptyStateView(icon = Icons.Outlined.BookmarkBorder, title = bookmarks_empty_title "No bookmarks yet", message = bookmarks_empty_message "Bookmark a show from its page to keep it here. You'll get a notification when a new episode is out.", action = { Button(onClick = DiscoverShowsClicked) { Text(bookmarks_empty_action "Discover airing shows") } })`, as a full-span grid item so pull-to-refresh still works.
- The CTA opens the **Schedule** tab (the design's `data-path="schedule"`): only shows on the weekly schedule get alerts, so that's where to pick them ([Q12](#11-open-questions)).
- The design's tonal card around the empty state and the small search badge on the icon are dropped for the shared view ([§3](#3-design-vs-spec) row 14). No sort strip, no counter row.

### 2.7 Notification (not in design, proposed)

- **One notification per show**, id = `slug.hashCode()`: a newer episode of the same show replaces the older notification.
  - small icon `DrawableResource.ic_notification` (new monochrome vector, the Material "bookmark" glyph)
  - title: the show's stored title
  - text: `notification_episode_out` "%1$s is out" with the shared episode title: "Episode 8 is out", "Movie is out", "Egghead SP11 is out"
  - sub-text when a backlog had several new episodes: plural `notification_more_new_episodes` "%d new episodes"
  - `setAutoCancel(true)`, `setGroup(GROUP_NEW_EPISODES)`, content intent → Details ([§5.7](#57-deep-link))
- **Group summary** when 2+ alerts of the group are active: `InboxStyle` with one line per show ("Frieren · Episode 8"), text plural `notification_summary` "%d new episodes", `setGroupSummary(true)`.
- **Channel** `new_episodes`: name `notification_channel_new_episodes` "New episodes", description "When a bookmarked show releases a new episode", `IMPORTANCE_DEFAULT`. Created before the first post (idempotent).
- No cover art in v1 ([Q18](#11-open-questions)).

### 2.8 States not in the design (all proposed)

| State | Proposal |
|---|---|
| **First load** | A private `BookmarksSkeleton` in **one** `SkeletonView`, using the content's grid helper with `userScrollEnabled = false`: `BookmarksHeaderSkeletonView` (three `FilterChipDefaults.Height` pill blocks, a `labelLarge` line at 0.4 width, a 96 dp pill block) and `columns × 3` `BookmarkCardSkeletonView`s. The app bar is real. Room answers in milliseconds, so this mostly shows on cold starts. |
| **Error** | Only a Room read failure (corrupt or full disk): `ErrorView(model = state.error, onRetry = { onIntent(Retry) })`. |
| **Refreshing** | `PullToRefreshBox` over the grid: forces a weekly-schedule fetch to update the airing chips. Failure → snackbar; the grid stays. |
| **Offline** | Everything renders from Room; covers from Coil's cache or the placeholder. The silent slot refresh on screen start fails quietly. |
| **Light theme** | Same roles, `LightColorScheme`. |
| **Medium / expanded** | Rail, 3 / 4 columns, 24 dp margins. |
| **Permission denied** (13+) | The alerts chip says "ALERTS OFF"; bookmarking works. A denial from the chip shows `bookmarks_alerts_denied` "Notifications are off for BakaMitai." with action `bookmarks_alerts_settings` "Settings" → app-notification settings. A denial from Details' first-bookmark ask shows `details_alerts_denied` "New-episode alerts are off. You can turn them on in Bookmarks." |

---

## 3. Design vs. spec

| # | Item | Design | docs/context.md / data | Recommendation |
|---|---|---|---|---|
| 1 | Top-left status badges | "EP 28 Done", "Airing Today", "Season 2 Ready", arc names, "Reze Movie Soon", "EP 10 Rewatch" | Not in the spec; the app has no watch progress or arc data | **Replace with the airing chip** from the weekly schedule (real): "TODAY 9:30 PM" / "MON 9:30 PM" / "NOT AIRING". |
| 2 | Added date | Relative ("Added Today", "2 hrs ago", "2 days ago", "1 week ago") and absolute ("Oct 14, 2024") | "Date added, formatted for the user's locale" (§3.4), "Nov 18, 2026" (§4.5) | **Absolute "Added Nov 18, 2026"**, English month names like Details (kotlinx-datetime has no locale formats; the app is English-only). No relative dates ([Q11](#11-open-questions)). |
| 3 | Subtitles | "Beyond Journey's End", "Ep 7 • Tonight 23:30", "Season 1 • 12 Episodes" | No data | **Dropped**; the airing chip carries the real schedule. |
| 4 | Bottom tags | "Completed", "Simulcast", "12 / 12 Ep", "Finished" | No data | **Dropped.** |
| 5 | Ratings | ★ 9.1 … | No data | **Dropped.** |
| 6 | Sort pills | Recently Added, Title (A-Z), Release Time, Completed | Grid "newest first", no sorting | **Keep three** (default Recently added = the spec's order; Release time from the weekly schedule); **drop Completed**: "not on the weekly schedule" ≠ completed. |
| 7 | Counter row | "Tracking Watchlist • 8 Titles" | — | "SAVED SHOWS • 8 shows" (real count, no "tracking" wording). |
| 8 | "Sync Ready" | Offline-bolt status label | No sync exists | **Replaced by the alerts chip** (real and actionable). |
| 9 | Stitch prompt extras | "Local Storage" pill, "8 Shows Saved Locally • No Sign-in Needed", bottom tip about the browser cache | — | **Omitted**: not in the HTML or the render, and the tip is about a browser. |
| 10 | Manage / batch remove | Referenced by the script, no button | — | **Not planned**; single remove + Undo. |
| 11 | Toasts | 'Removed "Frieren" from vault', "Sorted by …", "Updated bookmarks" | — | **Snackbar "Removed Frieren" + Undo**; no sort toast. |
| 12 | Remove interaction | Instant remove with card shrink/fade | — | Instant remove, `animateItem()`, Undo. No confirmation dialog. |
| 13 | Empty state copy and CTA | "No Local Bookmarks Found", "Bookmark anime across the schedule…", "Discover Airing Shows" → schedule | "Invites the user to bookmark shows from the Details screen" | **Spec-aligned copy** that mentions alerts; **CTA → Schedule tab** ([Q12](#11-open-questions)). |
| 14 | Empty state look | Tonal card, 80 dp circle, search badge on the icon | — | Shared `EmptyStateView` (consistent with Schedule and All Shows). |
| 15 | Title placement | On the poster, 1 line | Schedule's card puts the title under the poster, 2 lines | **Design placement** (over the poster's scrim), **2 lines**: long SubsPlease titles are common. |
| 16 | Card shadow | `shadow-md` | DESIGN.md: tonal layering | No shadow. |
| 17 | Persistence | — | §5: "Everything is stored with Jetpack DataStore… there's no Room/SQL" | **Room (D1)**; §5 and §7 are rewritten (step 17). |
| 18 | Notifications | — | Not in the spec | **User-decided (D3–D6)**; a new context.md section (step 17). |
| 19 | Next airing | — | Not in the spec | **Shown** (real data the alerts need anyway; makes "Release time" sorting possible). |
| 20 | Pull-to-refresh | — | Not listed for Bookmarks | **Added**: refreshes the airing chips (forces the weekly schedule). |

---

## 4. Data

### 4.1 Endpoints

The Bookmarks **screen** reads only Room. Network use is limited to keeping the airing slots fresh and to the background release check ([§5](#5-notification-feasibility)).

| Call | api.md | Used for | Fields used |
|---|---|---|---|
| `f=schedule&tz=<tz>` (weekly) | [§3.2](api.md#32-weekly-schedule) | Each bookmark's weekly slot (weekday + time + the feed's title), through Schedule's repository and its 1 h cache | `page`, `title`, `time`, the weekday key |
| `f=search&tz=<tz>&s=` (empty) | [§3.4](api.md#34-search-releases) | The **sweep**: the 20 newest releases, for every bookmark at once | `page`, `show`, `episode`, `release_date` |
| `f=latest&tz=<tz>&p=1` | [§3.3](api.md#33-latest-releases) | Fallback sweep, only when the empty search fails | same |
| `f=search&tz=<tz>&s=<title>` | [§3.4](api.md#34-search-releases) | Per-show fallback when the sweep's window doesn't reach back to the slot | same, filtered by `page == slug` |

Quirks this feature must handle (each has a test, [§8](#8-testing)):
- **`f=latest` lags by days** (G1): only a fallback here.
- **`release_date` is about 1 h early** and its offset varies per item ([§3.5](api.md#35-show-episodes)): every comparison against the device clock or a slot allows `RELEASE_DATE_TOLERANCE = 1.hours`; comparisons between two releases of the same show don't need it.
- **Episode labels** are strings: numbers (`"07"`), revisions (`"1216v2"`), specials (`"Egghead SP11"`), `"Movie"`, batch ranges (`"01-24"`).
- **`page` may be missing** (dropped by Home's existing mapper); **no results is `[]`** (Home's `ReleaseListDTO` handles it).
- **Titles differ between the show page and the JSON feeds** ([§4.2](api.md#42-show-page): en dash, curly apostrophe): the per-show search uses the **weekly schedule's title** stored with the slot, never the bookmark's page title.
- **Each show appears once per week** in the weekly schedule; if a slug ever appears twice, the first by Monday → Sunday is used.
- `f=show&sid=` isn't used: the `sid` only exists in the show page HTML and Details owns it ([§5.2](#52-confirming-its-out)).

### 4.2 API gaps found live

Checked on **2026-11-19 at 09:57 UTC** with `tz=UTC` (4 requests: empty search, latest p=1, weekly schedule, search "Kyouran Reijou"):

1. **The empty search's window is about 45 h:** its 20 items went from Mon 16 Nov 19:03 to Wed 18 Nov 16:17 UTC (9–10 releases per busy day). A check within 24 h of a release always finds it there.
2. **`f=latest&p=1` was 3 days behind:** its newest item was Mon 16 Nov 13:32 UTC (G1 again). It added nothing to the empty search.
3. **Real releases follow the slot within minutes:** for 37 of the 39 items with a weekly slot (both feeds), `release_date` was 54–59 min **before** the slot, i.e. the release was 1–6 min after it once the 1 h skew is removed. Two exceptions: *Sora wa Akai Kawa no Hotori* episode 20 was +9 min by `release_date` (+69 min if the skew applies to it), and *Gensou Suikoden* episode 07 was −25 min (+35 min real). Gensou Suikoden also released episodes 02 and 07 on the same day (an older episode can be released after a newer one).
4. **The skew started with the US DST change, not the European one:** *Kyouran Reijou Nia Liston* episode 04 (27 Oct, 12:58 UTC) matched its 12:55 slot and episode 05 (3 Nov, 11:59) was 56 min early, so it started between 27 Oct and 3 Nov (US DST ended 1 Nov; EU DST ended 25 Oct, which api.md §3.5 currently names). It will likely disappear when US DST starts (14 Mar 2027); the tolerance-based rules work either way.
5. **Per-show search returns the show's releases newest first** (7 for "Kyouran Reijou", all with the right `page`).
6. The weekly schedule still has 57 shows, once each.

Step 16 records 1–5 in api.md (§3.3 lag observation, §3.4 window size and ordering, §3.5 skew start and the slot-vs-release measurement) and adds the §7 exception ([§5.6](#56-politeness)).

### 4.3 Local data

**Room**, database `bookmarks.db` (`context.getDatabasePath`), version **1**, `exportSchema = true`, schema committed under `app/schemas/com.zc.bakamitai.feature.bookmarks.data.local.BookmarksDatabase/1.json`. One table:

| Column | Type | Meaning |
|---|---|---|
| `slug` | TEXT **PK** | The show (context.md §5: the bookmark key) |
| `title` | TEXT NOT NULL | Details' page title |
| `cover_url` | TEXT NULL | Absolute cover URL |
| `added_at` | INTEGER NOT NULL | Epoch ms; "Recently added" order; Undo restores it |
| `tracking_since` | INTEGER NOT NULL | Epoch ms; alerts consider releases after it (= `added_at`, or the Undo time) |
| `airing_day` | INTEGER NULL | ISO day 1–7 of the weekly slot; `NULL` = not on the weekly schedule |
| `airing_minute` | INTEGER NULL | Minutes after midnight in `airing_zone` |
| `airing_zone` | TEXT NULL | IANA zone the slot was fetched in |
| `airing_title` | TEXT NULL | The weekly schedule's title (used for the per-show search) |
| `airing_checked_at` | INTEGER NULL | When the slot was last refreshed; `NULL` = never |
| `last_seen_episode` | TEXT NULL | Normalized episode key of the newest release seen (`"1216"` for `"1216v2"`) |
| `last_seen_released_at` | INTEGER NULL | Its `release_date` (epoch ms) |
| `resolved_through` | INTEGER NULL | Latest weekly occurrence already handled (released, given up, or before tracking); `NULL` = `tracking_since − 24 h` |

- **Writes** are single statements or one `@Transaction`: toggle (delete the slug or insert it), remove-and-return, restore (insert with the old `added_at`), slot updates, tracking updates. Toggling is atomic, so a double tap can't race.
- **Old data (D2):** `BookmarkDTO`, `BookmarkListDTO`, `BookmarkListSerializer`, `BookmarksLocalDataSource`, `BookmarkDTOMapper`, the named DataStore provider and their tests are deleted. Nothing reads, writes or deletes `files/datastore/bookmarks.json`; on development devices it stays orphaned (reinstall to clean it).
- **No destructive fallback.** A later schema change adds an `AutoMigration` and a `MigrationTestHelper` test (and `room-testing` then).
- **Backup:** `allowBackup="true"` already backs up `databases/`; bookmarks and their tracking state restore with the app. WorkManager's queue may not, so the screen re-ensures the check is scheduled ([§5.3](#53-scheduling)).
- **Errors:** Room throws `SQLiteException`; the repository maps it to `DomainError.Unknown(e)` (the Details plan's Q7 precedent for disk errors).

### 4.4 Caching and refresh

- **Bookmarks:** Room `Flow`, no cache needed; Details' toggle updates the grid instantly and vice versa.
- **Airing slots** (stored in Room, used offline): refreshed when any bookmark's `airing_checked_at` is older than `AIRING_MAX_AGE = 24.hours` or missing:
  - on Bookmarks' screen start (best effort, silent);
  - on pull-to-refresh (forced, bypasses Schedule's 1 h cache);
  - inside a release-check run.
  The fetch goes through Schedule's `ScheduleRepositoryImpl` (`@Single`), so a visit to Schedule within the hour makes it free.
- **Releases:** never cached; each check run fetches once.
- **Next-airing labels** move with time: `ObserveBookmarksUseCase` re-derives them every minute while the screen is subscribed (`WhileSubscribed(5_000)` stops it).

---

## 5. Notification feasibility

### 5.1 Timing source

- **The weekly schedule** gives every airing show one slot per week: weekday + `HH:mm` in the requested zone ([§3.2](api.md#32-weekly-schedule)). Stored with its zone, a slot turns into **absolute instants** (`LocalDateTime(date, time).toInstant(zone)`, DST-safe through kotlinx-datetime). Live data says the real release lands 1–6 min after the slot ([§4.2](#42-api-gaps-found-live) item 3).
- **Today's `aired`** isn't used: it's unverified whether it follows the release or the clock ([§3.1](api.md#31-todays-schedule)), it has no episode number, and it costs a request without confirming anything.
- **Shows not on the weekly schedule** (finished, on break, movies, batches): no slot → no scheduled check. They're still caught **opportunistically**: every sweep covers every bookmark, so a movie or special of a bookmarked show that lands in a sweep triggered by another show notifies. A show that returns to the schedule gets its slot at the next refresh (≤ 24 h, or on screen start).
- **Times that change week to week:** slots refresh at most every 24 h (and on demand). A stale slot only delays: an earlier-than-stored release is found by the next check of the ladder; a later one is found by a later ladder step.
- **Time-zone changes:** nothing to do. Due times are absolute instants; slots carry the zone they were fetched in; the next refresh fetches the new zone's buckets (same instants). WorkManager's own receiver handles `TIMEZONE_CHANGED` / `TIME_SET`.

### 5.2 Confirming "it's out"

| Option | Requests per check | Reach | Reliability | Verdict |
|---|---|---|---|---|
| **A.** Empty search `f=search&s=` | **1 for all bookmarks** | 20 newest ≈ 45 h | Current (no G1 lag, live-checked) | **Primary sweep** |
| A + `f=latest&p=1` (Home's derivation) | 2 | `f=latest` lags days (3 days on 2026-11-19) | Adds nothing while lagging | **Fallback only** when A fails (error or undecodable root) |
| **B.** Per-show `f=search&s=<title>` + `page == slug` | 1 per show | The title's newest ~30 results | Current; titles can match other seasons (slug filter) or differ from the page title (use the schedule title) | **Fallback** for a pending show only when A's oldest item is newer than its slot − 1 h (the window doesn't reach back). Capped at 5 per run. |
| **C.** `f=show&sid=` | 1 + the 29 KB show page for the `sid`; up to 159 KB JSON | 52 episodes | Authoritative | **Rejected**: two heavy requests per show, and the `sid` is Details' data-layer detail |

The 52-entry cap only matters for C. With a 24 h retry ladder and a ~45 h window, B only runs after long offline or deferred periods.

### 5.3 Scheduling

**One shared worker, not one per show.** A single unique work `release-check` (`ReleaseCheckWorker`) runs at the earliest due time among all bookmarks and handles every bookmark in that run; its last step schedules the next run. It coalesces shows released together into one request and keeps one place that reasons about the whole set. Per-show workers would multiply requests and race on the same sweep.

**Per bookmark** (pure domain logic, [§6.4](#64-featurebookmarksdomain)):
- `pendingOccurrence(now)` = the latest slot occurrence `E ≤ now` with `E > resolvedThrough` (`resolvedThrough` defaults to `trackingSince − 24 h`, so a show bookmarked just after its slot is still checked).
- **Retry ladder** from `E`: `+10 min, +30 min, +1 h, +2 h, +4 h, +8 h, +16 h, +24 h` (8 attempts; live data puts the release at `E + 1–6 min`).
- **Due time:** pending → the first ladder time after now (past the ladder after a failed check: now + 1 h); not pending → the next occurrence after `max(now, resolvedThrough)` + 10 min.
- **Give up:** a successful check at or after `E + 24 h` that found nothing resolves `E` silently. The episode notifies later, when any sweep sees it (next week's slot at the latest): late, never wrong.

**Per run** (`CheckBookmarkedReleasesUseCase`, then `ApplyReleaseCheckUseCase`):
1. Read every bookmark's tracking state. None → stop (nothing is rescheduled).
2. If alerts are disabled (the worker passes `areAlertsEnabled()`): no requests at all; resolve every pending occurrence; reschedule (cheap, local).
3. Refresh slots if any is older than 24 h or missing (one weekly-schedule request, cache permitting).
4. If no bookmark is pending: no more requests; reschedule.
5. Sweep (A, fallback latest p=1). On failure: no state changes; next run at the ladder's next step.
6. Per-show fallback (B) for pending shows the window doesn't cover (≤ 5 per run; if more are left, next run in 15 min).
7. Evaluate **every** bookmark against its candidates ([§5.4](#54-deduplication)); pending ones with no new release past `E + 24 h` are resolved.
8. In one `NonCancellable` block: save the tracking updates, then (only if the save succeeded) post the alerts.
9. Next run = the earliest due time, pushed to the latest due time within the next 15 min (coalescing window). `null` → nothing enqueued.

**Enqueue policy** (`ReleaseCheckSchedulerImpl`):

| Call | Policy | Request |
|---|---|---|
| `requestCheck()` (a bookmark added, removed or restored; slots changed) | `ExistingWorkPolicy.REPLACE` | No delay |
| `ensureCheckScheduled()` (Bookmarks screen start) | `KEEP` | No delay: only enqueues when nothing is scheduled (backup restore, lost queue) |
| `scheduleNextCheck(at)` (end of a run, from inside the worker) | `APPEND_OR_REPLACE`: chained after the running work, so it never cancels itself | `setInitialDelay(at − now)` (WorkManager counts it from the moment the chain unblocks) |

Every request: `OneTimeWorkRequestBuilder<ReleaseCheckWorker>()`, `Constraints(requiredNetworkType = NetworkType.CONNECTED)`, tag `release-check`. The worker always returns `Result.success()`: it handles its own retries through the ladder, and the check use case never throws for network or disk failures (they only shape `nextCheckAt`). A cancelled run schedules nothing: a `REPLACE` that lands mid-sweep has already enqueued its replacement, which re-runs from fresh Room data, and a system stop (lost network) makes WorkManager re-run the same work later. The `NonCancellable` save-then-notify block can't be torn. A crash (a bug) ends the chain until the next `ensureCheckScheduled()` or bookmark change.

**Rescheduling events:**

| Event | Handling |
|---|---|
| Bookmark added / removed / restored | The use case calls `requestCheck()`; the run refreshes the new slot and reschedules. The last removal makes the next run end the chain. |
| Weekly schedule changes | Slot refresh (screen start, pull-to-refresh, any run with slots > 24 h old); a changed slot from the screen path calls `requestCheck()`. |
| Time-zone change | Nothing ([§5.1](#51-timing-source)). |
| Reboot | WorkManager persists its queue and reschedules on `BOOT_COMPLETED`. |
| App update | WorkManager keeps the queue. `ReleaseCheckWorker`'s class name and package are part of the persisted data and must never change (or the update must cancel `release-check` by name and `requestCheck()`). |
| Force stop | The OS drops the jobs; WorkManager reschedules on the next app start. |
| Backup restore / lost queue | `ensureCheckScheduled()` on Bookmarks' screen start. |

### 5.4 Deduplication

Per bookmark, Room keeps `last_seen_episode` + `last_seen_released_at` and `resolved_through`. For each candidate release `R` of the show (sweep ∪ fallback), **batches (`^\d+\s*-\s*\d+$`) are ignored**; the others are new when:

```kotlin
val key = EpisodeKey.of(R.episode)                      // trim, drop a trailing "v<digits>": "1216v2" → "1216"
val isNew = if (lastSeen == null) {
    R.releasedAt + RELEASE_DATE_TOLERANCE >= trackingSince   // released after tracking began (release_date may be 1 h early)
} else {
    R.releasedAt > lastSeen.releasedAt && key != lastSeen.key &&
        (key.number == null || lastSeen.key.number == null || key.number > lastSeen.key.number)
}
```

- **Revisions** (`"1216v2"` after `"1216"`) share the key: never notified.
- **An older episode released late** (Gensou Suikoden's 02 after 06) loses the numeric comparison: never notified.
- **Specials and "Movie"** have no number: new when later and different.
- **Several new episodes** (a backlog after a long offline period): **one alert per show** with the newest episode and the count ("Episode 9 is out · 2 new episodes").
- **Baseline:** with no `lastSeen`, releases from before tracking only set `lastSeen` (no alert). After a successful run, `lastSeen` = the newest candidate considered.
- **Occurrence resolution:** an alert (or a baseline) resolves the occurrence nearest to `R.releasedAt + RELEASE_DATE_TOLERANCE` (unique within ±3.5 days), so an early release also settles the upcoming slot.
- **Re-runs:** state is saved before posting, in one `NonCancellable` block; a run after a crash or a `REPLACE` sees the saved `lastSeen` and stays quiet. Notification ids are per slug, so even a duplicate post replaces the same notification.
- **Undo** restores a bookmark with `tracking_since` = now and no `lastSeen`, so releases from while it was removed don't notify.
- **Known false-positive window:** while SubsPlease's `release_date` is correct (no skew), an episode released up to 1 h **before** the user bookmarked the show can notify once. Accepted ([Q15](#11-open-questions)).

### 5.5 Android limits

- **Doze and App Standby:** WorkManager runs through JobScheduler, so an idle device defers the run to a maintenance window (minutes in light Doze, up to hours in deep Doze). Standby buckets add more: *active* is not deferred, *working set* up to about 2 h, *frequent* up to about 8 h, *rare* up to about 24 h, *restricted* (an app unused for a long time) about once a day. Expedited work can't have an initial delay, so it doesn't help.
- **Exact alarms are deliberately not used.** `USE_EXACT_ALARM` is restricted by Play policy to alarm and calendar apps; `SCHEDULE_EXACT_ALARM` is denied by default from Android 14 and needs the user to grant it; and an exact alarm in Doze still has no network to confirm the release. A foreground service for this is not allowed either.
- **OEM battery managers** (some Xiaomi, Huawei, Samsung "sleeping apps" settings) can stop jobs until the app is opened. There's no in-app fix: `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` is Play-restricted for this use. Documented as a limitation.
- **Network constraint** `CONNECTED` (metered allowed; a check is one ~58 KB response). Offline at the slot → the run waits for a connection.
- **Channel** `new_episodes`, `IMPORTANCE_DEFAULT`; the user can mute it in system settings (the alerts chip's "on" tap opens them).
- **Grouping:** one notification per show plus an `InboxStyle` summary when 2+ are active (Android auto-bundles 4+ anyway).
- **`POST_NOTIFICATIONS` (13+):** asked once, when an add makes the bookmark count go from 0 to 1 ([Q6](#11-open-questions)). Denied → bookmarking still works, runs skip their requests, the alerts chip says "ALERTS OFF" and offers the dialog (while Android still shows it) or the system settings. Below 13 the permission is granted at install; `areNotificationsEnabled()` still covers a user who turned the app's notifications off.
- **Background activity starts:** a notification's content `PendingIntent` is allowed to start the activity on every API level.

### 5.6 Politeness

[api.md §7](api.md#7-client-guidelines) says "no polling and no background sync". The narrowest exception, which step 16 writes into api.md §7:

> **Exception: new-episode alerts (Bookmarks plan, 2026-11-19).** Only while the user has bookmarks. Event-driven, never periodic: a check runs 10 minutes after a bookmarked show's weekly slot, and retries 8 times over 24 hours (10 min, 30 min, 1 h, 2 h, 4 h, 8 h, 16 h, 24 h after the slot) only while that episode isn't out. A check makes one `f=search&s=` request for all bookmarks (`f=latest&p=1` only if it fails), plus one `f=search&s=<title>` per late show only when the newest releases no longer reach back to its slot (at most 5 per check). Slots come from `f=schedule` at most once a day. Nothing runs while notifications are off.

**Budget:** per bookmarked show per week, **1 request** when the episode is on time (shared with every show due in the same 15-minute window), at most **8 sweeps + a few per-show searches** when it never comes; plus at most **one weekly-schedule request per day** for all bookmarks together. With 10 bookmarks on time, that's at most 10 + 7 requests a week, about what four or five Home pull-to-refreshes cost (3–4 requests each).

### 5.7 Deep link

- **PendingIntent:** `PendingIntent.getActivity(context, requestCode = notificationId, intent = context.showDetailsIntent(slug), FLAG_IMMUTABLE or FLAG_UPDATE_CURRENT)`. `showDetailsIntent` (`common/ui/navigation/ShowDeepLink.kt`) = `packageManager.getLaunchIntentForPackage(packageName)` (MainActivity, without the feature importing `app`) + extra `EXTRA_SHOW_SLUG` + `FLAG_ACTIVITY_NEW_TASK or FLAG_ACTIVITY_CLEAR_TOP or FLAG_ACTIVITY_SINGLE_TOP`.
- **MainActivity** keeps `private val showDeepLinks = Channel<String>(Channel.BUFFERED)` and a stable `receiveAsFlow()`:
  - `onCreate`: when `savedInstanceState == null`, sends `intent.showSlugOrNull()` (which ignores intents with `FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY`, so reopening from Recents doesn't re-open Details; a restored process keeps its saved back stack instead).
  - `onNewIntent` (the app is already running; `SINGLE_TOP` + `CLEAR_TOP` keep the same activity): `setIntent(intent)`, then sends the slug.
- **AppNavigation** collects it with `ObserveAsEvents(showDeepLinks) { slug -> navigator.navigateToDetailsFromStart(slug) }`.
- **Back stack:** `Navigator.navigateFromStart(key)` selects the **Home** tab (the start route), pops Home's stack to its root, and pushes `DetailsNavKey(slug)`. Back → Home, whose "Latest" lists the new episode; Back again → leaves the app. Other tabs keep their stacks. On a cold start, Home's ViewModel isn't created until the user goes back (only the top entry is composed). [Q5](#11-open-questions) has the Bookmarks-tab alternative.
- Follows the `navigation-3` skill's deep-link recipes (key built from the intent; a synthetic back stack starting at the start route), without URI patterns: the intent is explicit and internal.

### 5.8 Verdict

**Feasible.** One request per show per week in the normal case, all state local, no exact alarms.
- **Expected latency** after a real release: about **10–15 minutes** on a device in use or recently used; **up to an hour or two** when idle in Doze; **up to about a day** for apps in the *rare* or *restricted* standby buckets; on reconnection when offline.
- **Not guaranteed:** timeliness (Doze, standby buckets, OEM killers); alerts for shows that aren't on the weekly schedule (movies, specials, returning shows) are opportunistic; batches never notify; an episode more than 24 h late notifies only when a later sweep sees it; nothing runs after a force stop until the app is opened; SubsPlease can change its API. The only known wrong alert is the ≤ 1 h window in [§5.4](#54-deduplication).

---

## 6. Architecture

Root: `app/src/main/java/com/zc/bakamitai/`. Test roots: `app/src/test/java/com/zc/bakamitai/`, `app/src/androidTest/java/com/zc/bakamitai/`.

**New folder kinds** (added to the skills in step 1, [Q16](#11-open-questions)):
- `feature/[name]/data/local/`: also Room (`[Name]Entity`, `[Name]Dao`, `[Name]Database`).
- `feature/[name]/data/worker/`: WorkManager `[Name]Worker` (`@KoinWorker`) and `[Name]SchedulerImpl`.
- `feature/[name]/domain/repository/`: also **ports** the domain triggers on the platform, `[Name]Scheduler` and `[Name]Notifier` (implemented in `data/worker/` and `presentation/notification/`).
- `feature/[name]/presentation/notification/`: `[Name]NotifierImpl`, which builds and posts system notifications (all text through `UiText`).

The worker is a **background entry point in `data`**: like a ViewModel, it calls its own feature's use cases; it never imports `presentation`. Notifications are user-visible text, so they are `presentation`, reached through the domain `ReleaseAlertNotifier` port that Koin binds.

### 6.1 Shared code

| Path | | Responsibility |
|---|---|---|
| `common/ui/mapper/DateUIModelMapper.kt` | **new** | `fun LocalDate.toDateLabel(): UiText` ("Nov 17, 2026"), moved from Details' private `ReleaseDateFormat` (4-details.md §6 planned this move). |
| `common/ui/mapper/EpisodeLabelUIModelMapper.kt` | **new** | `fun String.toEpisodeTitle(): UiText` ("Episode 20", "Episode 1216 (v2)", "Movie", "Episodes 01–28", else as-is), moved from Details' `EpisodeUIModelMapper.kt` with its regexes. Strings renamed `details_episode*` → `episode_label_*`. Second user: the notification. |
| `common/ui/mapper/DayOfWeekUIModelMapper.kt` | **new (moved)** | `toDayName()`, `toShortDayName()`, moved from `feature/schedule/presentation/mapper/`. |
| `common/ui/component/timechip/TimeChipView.kt` | **new (moved)** | `TimeChipView(model: TimeChipUIModel, modifier)`, generalized from Schedule's private `TimeChip`: tone → icon and colors (`DEFAULT`: `Schedule`, primary icon, onSurface text; `UPCOMING`: `Alarm`, tertiary; `MUTED`: `EventBusy`, onSurfaceVariant), `@AdaptivePreviews` for the three tones. |
| `common/ui/component/timechip/model/TimeChipUIModel.kt`, `model/TimeChipTone.kt` | **new** | `@Immutable data class TimeChipUIModel(val label: UiText, val tone: TimeChipTone)`; `enum class TimeChipTone { DEFAULT, UPCOMING, MUTED }` |
| `feature/schedule/presentation/component/schedulecard/ScheduleCardView.kt` (+ model/mapper) | modify | Uses `TimeChipView`; `ScheduleCardUIModel.timeLabel` becomes `time: TimeChipUIModel` (UPCOMING when the status is upcoming). Looks the same. |
| `feature/details/presentation/component/episoderow/mapper/EpisodeUIModelMapper.kt` | modify | Imports `toDateLabel()` / `toEpisodeTitle()`; private copies deleted. |
| `common/ui/navigation/Navigator.kt` | modify | `fun navigateFromStart(key: NavKey)`: select `startRoute`, pop its stack to the root, push `key` (unless it's a top-level route). |
| `common/ui/navigation/ShowDeepLink.kt` | **new** | `const val EXTRA_SHOW_SLUG = "com.zc.bakamitai.extra.SHOW_SLUG"`, `fun Context.showDetailsIntent(slug: String): Intent`, `fun Intent.showSlugOrNull(): String?` (blank → `null`; launched from history → `null`). |
| `common/ui/util/NotificationPermission.kt` | **new** | `@Composable fun rememberNotificationPermissionRequester(onResult: (granted: Boolean) -> Unit): () -> Unit` (`rememberLauncherForActivityResult(RequestPermission())`), `fun Context.canAskNotificationPermission(): Boolean` (API ≥ 33 and not granted), `fun Context.openAppNotificationSettings()` (`Settings.ACTION_APP_NOTIFICATION_SETTINGS` + `EXTRA_APP_PACKAGE`). Used by Details and Bookmarks. |
| `common/ui/resource/ResourceTypes.kt` | modify | `typealias DrawableResource = com.zc.bakamitai.R.drawable` (the notification's small icon). |
| `res/drawable/ic_notification.xml` | **new** | 24 dp monochrome vector (Material "bookmark" path), white fill. |
| `feature/home/domain/usecase/GetNewestReleasesUseCase.kt` | **new** | `@Factory`. `suspend operator fun invoke(timeZone: TimeZone): Result<List<Release>>` = `searchReleases("")`, falling back to `getLatestReleases(1)` on failure. |
| `feature/home/domain/usecase/SearchShowReleasesUseCase.kt` | **new** | `@Factory`. `suspend operator fun invoke(slug: ShowSlug, query: String, timeZone: TimeZone): Result<List<Release>>`: `searchReleases(query)` filtered by slug, newest first. |
| `feature/schedule/domain/model/WeeklyTemplate.kt` | **new** | `data class WeeklyTemplate(val timeZone: TimeZone, val showsByDay: Map<DayOfWeek, List<ScheduledShow>>)` |
| `feature/schedule/domain/usecase/GetWeeklyTemplateUseCase.kt` | **new** | `@Factory`. `suspend operator fun invoke(forceRefresh: Boolean = false): Result<WeeklyTemplate>` over `ScheduleRepository.getWeeklySchedule(zone, forceRefresh)` (the 1 h cache) with the current zone. |
| `feature/schedule/presentation/navigation/ScheduleNavigation.kt` | modify | `fun Navigator.navigateToSchedule() = navigate(ScheduleNavKey)` |
| `feature/details/presentation/navigation/DetailsNavigation.kt` | modify | `fun Navigator.navigateToDetailsFromStart(slug: String) = navigateFromStart(DetailsNavKey(slug))` |

Reused as-is: `Show` / `ShowSlug`, `Release` (Home's domain), `CoverImageView` / `POSTER_ASPECT_RATIO`, the skeleton pieces, `ErrorView` / `toErrorUIModel()`, `EmptyStateView`, `TopLevelAppBarView`, `toTimeLabel()`, `ObserveAsEvents`, `@AdaptivePreviews`, `TimeZoneProvider`, `Clock`, `DispatcherProvider`.

### 6.2 Build

| Path | | Change |
|---|---|---|
| `gradle/libs.versions.toml` | modify | `[versions]` `ksp = "2.3.14"`, `room = "2.8.5"`, `work = "2.12.0"`. `[libraries]` `androidx-room-runtime`, `androidx-room-compiler` (`version.ref = "room"`), `androidx-work-runtime`, `androidx-work-testing` (`"work"`), `koin-androidx-workmanager` (BOM, no version), build-logic `ksp-gradle-plugin` (`com.google.devtools.ksp:symbol-processing-gradle-plugin`), `room-gradle-plugin` (`androidx.room:room-gradle-plugin`). `[plugins]` `ksp = { id = "com.google.devtools.ksp", version.ref = "ksp" }`, `room = { id = "androidx.room", version.ref = "room" }`, `bakamitai-android-room = { id = "bakamitai.android.room" }`. **Remove** `androidx-datastore` (typed; its only user is gone). Keep `androidx-datastore-preferences` for Settings. |
| `build.gradle.kts` (root) | modify | `alias(libs.plugins.ksp) apply false`, `alias(libs.plugins.room) apply false` |
| `build-logic/convention/build.gradle.kts` | modify | `compileOnly(libs.ksp.gradle.plugin)`, `compileOnly(libs.room.gradle.plugin)`; register `androidRoom` → `AndroidRoomConventionPlugin` |
| `build-logic/convention/src/main/kotlin/AndroidRoomConventionPlugin.kt` | **new** | `bakamitai.android.room`: applies `com.google.devtools.ksp` and `androidx.room`; `room { schemaDirectory("$projectDir/schemas") }`; `ksp { arg("room.generateKotlin", "true") }`; adds `implementation(room-runtime)` and `ksp(room-compiler)` from the catalog. KSP processes **only** Room: Koin keeps its compiler plugin. |
| `app/build.gradle.kts` | modify | `alias(libs.plugins.bakamitai.android.room)`; `implementation(libs.androidx.work.runtime)`, `implementation(libs.koin.androidx.workmanager)`; remove `implementation(libs.androidx.datastore)`; `androidTestImplementation(libs.androidx.work.testing)`, `androidTestImplementation(libs.kotlinx.coroutines.test)`, `androidTestImplementation(libs.turbine)` |
| `app/schemas/…/BookmarksDatabase/1.json` | **new (generated, committed)** | Room schema v1 |
| `app/src/main/AndroidManifest.xml` | modify | `<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />`; the `androidx.startup.InitializationProvider` `<provider tools:node="merge">` with `<meta-data android:name="androidx.work.WorkManagerInitializer" tools:node="remove" />` (Koin's factory initializes WorkManager) |

KSP compatibility: KSP2 versions are independent of Kotlin. **2.3.10** fixed Kotlin 2.4's module names (#2964) and R-class resolution under AGP 9 built-in Kotlin (#2857); 2.3.6 detects built-in Kotlin without `KotlinBaseApiPlugin`; **2.3.14** (13 Nov 2026) fixes 2.3.13's concurrency regression (#3286). Room 2.8.4 + KSP + AGP 9.3 built-in Kotlin is reported working except for `@Parcelize` entities (#3053, open): entities here are plain. **Not build-verified yet**: step 2 is the go/no-go spike. Fallbacks, in order: KSP 2.3.12; `android.disallowKotlinSourceSets=false` in `gradle.properties` if generated sources aren't picked up; report back before trying anything else.

### 6.3 feature/bookmarks/data

| Path | | Responsibility |
|---|---|---|
| `data/dto/BookmarkDTO.kt`, `data/dto/BookmarkListDTO.kt`, `data/local/BookmarkListSerializer.kt`, `data/local/BookmarksLocalDataSource.kt`, `data/mapper/BookmarkDTOMapper.kt` | **delete** | D2 |
| `data/local/BookmarkEntity.kt` | **new** | `@Entity(tableName = "bookmark") data class BookmarkEntity(@PrimaryKey @ColumnInfo("slug") val slug: String, …)`, the columns of [§4.3](#43-local-data), primitives only (no converters). |
| `data/local/BookmarkDao.kt` | **new** | `@Dao abstract class BookmarkDao`, 10 functions (Detekt's `TooManyFunctions` reports at 11): `observeAll(): Flow<List<BookmarkEntity>>` (`ORDER BY added_at DESC`), `suspend getAll()`, `suspend get(slug)`, `suspend insert(entity): Long` (`IGNORE`), `protected suspend deleteRow(slug): Int`, `@Transaction open suspend fun removeAndGet(slug): BookmarkEntity?` (`get` then `deleteRow`), `@Transaction open suspend fun toggle(entity): Boolean` (`removeAndGet(slug) == null` → `insert`; true = now saved), `suspend updateAiring(slug, day, minute, zone, title, checkedAt): Int`, `suspend updateTracking(slug, lastSeenEpisode, lastSeenReleasedAt, resolvedThrough): Int`, `@Transaction open suspend fun updateTrackings(rows)`. The repository derives `isFirstBookmark` from `getAll().size == 1` after an add. Multi-line raw-string SQL (ktlint line length). |
| `data/local/BookmarksDatabase.kt` | **new** | `@Database(entities = [BookmarkEntity::class], version = 1, exportSchema = true) abstract class BookmarksDatabase : RoomDatabase() { abstract fun bookmarkDao(): BookmarkDao }`; `const val BOOKMARKS_DATABASE_NAME = "bookmarks.db"` |
| `data/mapper/BookmarkEntityMapper.kt` | **new** | `fun BookmarkEntity.toBookmark(): Bookmark?` and `fun BookmarkEntity.toReleaseWatch(): ReleaseWatch?` (`null` for a blank slug/title; a slot only when day, minute, zone and title are all set and valid; an unknown zone id → no slot) |
| `data/mapper/BookmarkDomainMapper.kt` | modify | `fun Bookmark.toEntity(trackingSince: Instant): BookmarkEntity` (empty tracking, no slot); `fun ReleaseWatchUpdate.toTrackingRow()` |
| `data/repository/BookmarkRepositoryImpl.kt` | modify | `@Single class BookmarkRepositoryImpl(private val dao: BookmarkDao)`. Every write wrapped in a private `storageCall { }` that maps `SQLiteException` → `DomainError.Unknown(e)` and never catches `CancellationException`; `observeBookmarks()` rethrows `SQLiteException` as `DomainError.Unknown`. |
| `data/worker/ReleaseCheckWorker.kt` | **new** | `@KoinWorker class ReleaseCheckWorker(context: Context, params: WorkerParameters, private val getAlertsEnabled: GetReleaseAlertsEnabledUseCase, private val checkReleases: CheckBookmarkedReleasesUseCase, private val applyReleaseCheck: ApplyReleaseCheckUseCase, private val scheduler: ReleaseCheckScheduler) : CoroutineWorker(context, params)` (6 parameters, under Detekt's constructor threshold of 7). `doWork()`: `val outcome = checkReleases(alertsEnabled = getAlertsEnabled()); withContext(NonCancellable) { applyReleaseCheck(outcome) }; scheduler.scheduleNextCheck(outcome.nextCheckAt); return Result.success()`. No catch-all: the use case doesn't throw for expected failures, and cancellation must propagate ([§5.3](#53-scheduling)). Name and package are frozen. |
| `data/worker/ReleaseCheckSchedulerImpl.kt` | **new** | `@Single class ReleaseCheckSchedulerImpl(private val workManager: WorkManager, private val clock: Clock) : ReleaseCheckScheduler`. Unique name `RELEASE_CHECK_WORK = "release-check"`, policies of [§5.3](#53-scheduling); `scheduleNextCheck(null)` enqueues nothing. |

`BookmarkRepositoryImpl` implements:

```kotlin
interface BookmarkRepository {
    fun observeBookmarks(): Flow<List<Bookmark>>                                  // newest first
    suspend fun toggle(bookmark: Bookmark): Result<BookmarkToggle>               // tracking_since = bookmark.addedAt
    suspend fun remove(slug: ShowSlug): Result<Bookmark?>                        // the removed bookmark, for Undo
    suspend fun restore(bookmark: Bookmark, trackingSince: Instant): Result<Unit>
    suspend fun getReleaseWatches(): Result<List<ReleaseWatch>>
    suspend fun updateAiringSlots(slots: Map<ShowSlug, AiringSlot?>, checkedAt: Instant): Result<Unit>
    suspend fun saveReleaseWatches(updates: List<ReleaseWatchUpdate>): Result<Unit>
}
```

### 6.4 feature/bookmarks/domain

Pure Kotlin + kotlinx-datetime. Constants live in `ReleaseCheckPolicy`.

| Path | | Responsibility |
|---|---|---|
| `domain/model/Bookmark.kt` | modify | `data class Bookmark(val show: Show, val addedAt: Instant, val airing: AiringSlot? = null)` (Details keeps constructing it with two arguments) |
| `domain/model/AiringSlot.kt` | **new** | `data class AiringSlot(val dayOfWeek: DayOfWeek, val time: LocalTime, val timeZone: TimeZone, val releaseTitle: String)` with `latestAtOrBefore(instant)`, `firstAfter(instant)`, `nearestTo(instant): Instant` (DST-safe via `toInstant(timeZone)`) |
| `domain/model/BookmarkSort.kt` | **new** | `enum class BookmarkSort { RECENTLY_ADDED, TITLE, NEXT_AIRING }` |
| `domain/model/BookmarkedShow.kt` | **new** | `data class BookmarkedShow(val bookmark: Bookmark, val nextAiring: LocalDateTime?, val airsToday: Boolean)`: the next occurrence in the device zone |
| `domain/model/BookmarkToggle.kt` | **new** | `data class BookmarkToggle(val isBookmarked: Boolean, val isFirstBookmark: Boolean)` (`isFirstBookmark` = added and now the only one) |
| `domain/model/EpisodeKey.kt` | **new** | `@JvmInline value class EpisodeKey(val value: String)` with `companion fun of(label: String)` (trim, drop `v\d+$`), `val number: Double?`, and `fun String.isBatchRange(): Boolean` |
| `domain/model/SeenEpisode.kt` | **new** | `data class SeenEpisode(val key: EpisodeKey, val releasedAt: Instant)` |
| `domain/model/ReleaseWatch.kt` | **new** | `data class ReleaseWatch(val show: Show, val trackingSince: Instant, val airing: AiringSlot?, val airingCheckedAt: Instant?, val lastSeen: SeenEpisode?, val resolvedThrough: Instant?)` with `pendingOccurrence(now)`, `isNew(release)`, `evaluate(candidates: List<Release>, now): ReleaseWatchResult`, `resolve(occurrence)`, `nextCheckAt(now): Instant?` ([§5.3](#53-scheduling), [§5.4](#54-deduplication)) |
| `domain/model/ReleaseWatchResult.kt` | **new** | `data class ReleaseWatchResult(val watch: ReleaseWatch, val alert: EpisodeAlert?)` (the updated watch) |
| `domain/model/EpisodeAlert.kt` | **new** | `data class EpisodeAlert(val show: Show, val episode: String, val releasedAt: Instant, val newEpisodeCount: Int)` |
| `domain/model/ReleaseWatchUpdate.kt` | **new** | `data class ReleaseWatchUpdate(val slug: ShowSlug, val lastSeen: SeenEpisode?, val resolvedThrough: Instant?)` |
| `domain/model/ReleaseCheckOutcome.kt` | **new** | `data class ReleaseCheckOutcome(val updates: List<ReleaseWatchUpdate>, val alerts: List<EpisodeAlert>, val nextCheckAt: Instant?)` |
| `domain/model/ReleaseCheckPolicy.kt` | **new** | `object ReleaseCheckPolicy`: `RETRY_OFFSETS = listOf(10.minutes, 30.minutes, 1.hours, 2.hours, 4.hours, 8.hours, 16.hours, 24.hours)`, `GIVE_UP_AFTER = 24.hours`, `RELEASE_DATE_TOLERANCE = 1.hours`, `AIRING_MAX_AGE = 24.hours`, `COALESCE_WINDOW = 15.minutes`, `FAILED_CHECK_RETRY = 1.hours`, `MAX_FALLBACK_SEARCHES = 5`, `FALLBACK_CONTINUE_DELAY = 15.minutes`, `LABEL_REFRESH = 1.minutes` |
| `domain/repository/BookmarkRepository.kt` | modify | [§6.3](#63-featurebookmarksdata) |
| `domain/repository/ReleaseCheckScheduler.kt` | **new** | Port: `fun requestCheck()`, `fun ensureCheckScheduled()`, `fun scheduleNextCheck(at: Instant?)` |
| `domain/repository/ReleaseAlertNotifier.kt` | **new** | Port: `fun areAlertsEnabled(): Boolean`, `fun notify(alerts: List<EpisodeAlert>)`. **Settings hook:** the Settings plan ANDs its switch into the check and the Details ask. |
| `domain/usecase/ObserveIsBookmarkedUseCase.kt` | keep | Unchanged (Details) |
| `domain/usecase/ToggleBookmarkUseCase.kt` | modify | `(repository, scheduler, clock)`: `suspend operator fun invoke(show: Show): Result<BookmarkToggle>`; on success `scheduler.requestCheck()` |
| `domain/usecase/ObserveBookmarksUseCase.kt` | **new** | `(repository, clock, timeZoneProvider)`: `operator fun invoke(sort: BookmarkSort): Flow<List<BookmarkedShow>>` = `combine(repository.observeBookmarks(), ticker(LABEL_REFRESH))`, next airing in the current zone, sorted (`RECENTLY_ADDED`: `addedAt` desc; `TITLE`: case-insensitive, then slug; `NEXT_AIRING`: next airing asc, unscheduled last by title), `distinctUntilChanged()` |
| `domain/usecase/RemoveBookmarkUseCase.kt` | **new** | `suspend operator fun invoke(slug: ShowSlug): Result<Bookmark?>`; `requestCheck()` on success |
| `domain/usecase/RestoreBookmarkUseCase.kt` | **new** | `suspend operator fun invoke(bookmark: Bookmark): Result<Unit>` = `restore(bookmark, trackingSince = clock.now())`; `requestCheck()` |
| `domain/usecase/UpdateAiringSlotsUseCase.kt` | **new** | `(repository, getWeeklyTemplate, clock)`: `suspend operator fun invoke(forceRefresh: Boolean): Result<Boolean>` (whether any slot changed). Fresh slots and not forced → `success(false)` without a request. Otherwise: the template's first slot per bookmarked slug (`null` when absent), stored with `checkedAt = now`. |
| `domain/usecase/RefreshAiringSlotsUseCase.kt` | **new** | Screen path: `updateAiringSlots(forceRefresh)`, then `requestCheck()` when changed, else `ensureCheckScheduled()` (also after a failure). Returns the update's failure for the pull-to-refresh snackbar. |
| `domain/usecase/GetReleaseAlertsEnabledUseCase.kt` | **new** | `operator fun invoke(): Boolean = notifier.areAlertsEnabled()` |
| `domain/usecase/CheckBookmarkedReleasesUseCase.kt` | **new** | `(repository, updateAiringSlots, getNewestReleases, searchShowReleases, clock, timeZoneProvider)` (6 parameters, under Detekt's constructor threshold): `suspend operator fun invoke(alertsEnabled: Boolean): ReleaseCheckOutcome`, the run of [§5.3](#53-scheduling) steps 1–7 and 9. Never throws for network or disk failures (they shape `nextCheckAt`); only cancellation propagates. |
| `domain/usecase/ApplyReleaseCheckUseCase.kt` | **new** | `(repository, notifier)`: `suspend operator fun invoke(outcome: ReleaseCheckOutcome)` = `saveReleaseWatches(updates).onSuccess { notifier.notify(alerts) }` |

Cross-feature domain use: bookmarks' domain imports Home's `Release`, `GetNewestReleasesUseCase`, `SearchShowReleasesUseCase` and Schedule's `GetWeeklyTemplateUseCase` (`architecture` §1: other features' domain models and use cases). No cycle: Home and Schedule don't import Bookmarks.

### 6.5 feature/bookmarks/presentation

Component placement follows `structure` §1.1.

| Path | | Responsibility |
|---|---|---|
| `presentation/model/BookmarkSortOption.kt` | **new** | `enum class BookmarkSortOption { RECENTLY_ADDED, TITLE, RELEASE_TIME }`: read by the State, the Intent and the header (like Schedule's `StatusFilter`). The ViewModel maps it to `BookmarkSort` from the intent's value. |
| `component/bookmarkcard/model/BookmarkCardUIModel.kt` | **new** | `@Immutable data class BookmarkCardUIModel(val slug: String, val title: UiText, val coverUrl: String?, val addedLabel: UiText, val airing: TimeChipUIModel, val removeLabel: UiText)` |
| `component/bookmarkcard/mapper/BookmarkCardUIModelMapper.kt` | **new** | `fun BookmarkedShow.toBookmarkCardUIModel(): BookmarkCardUIModel` (labels of [§2.4](#24-bookmark-card)); `fun List<BookmarkedShow>.toBookmarkCardUIModels(): ImmutableList<…>` |
| `component/bookmarkcard/BookmarkCardView.kt` | **new** | `BookmarkCardView(model, onClick: () -> Unit, onRemoveClick: () -> Unit, modifier)` + `BookmarkCardSkeletonView(modifier)`, `@AdaptivePreviews` |
| `component/bookmarksheader/model/SortChipUIModel.kt` | **new** | `@Immutable data class SortChipUIModel(val option: BookmarkSortOption, val label: UiText, val icon: ImageVector)` |
| `component/bookmarksheader/model/AlertsChipUIModel.kt` | **new** | `@Immutable data class AlertsChipUIModel(val isEnabled: Boolean, val label: UiText, val icon: ImageVector, val contentDescription: UiText)` |
| `component/bookmarksheader/mapper/BookmarksHeaderUIModelMapper.kt` | **new** | `fun sortChipUIModels(): ImmutableList<SortChipUIModel>`, `fun Boolean.toAlertsChipUIModel()`, `fun Int.toBookmarkCountLabel(): UiText` |
| `component/bookmarksheader/model/BookmarksHeaderUIModel.kt` | **new** | `@Immutable data class BookmarksHeaderUIModel(val sortChips: ImmutableList<SortChipUIModel> = persistentListOf(), val countLabel: UiText? = null, val alertsChip: AlertsChipUIModel? = null)`: groups the header's data so the View stays under Detekt's parameter threshold |
| `component/bookmarksheader/BookmarksHeaderView.kt` | **new** | `BookmarksHeaderView(model: BookmarksHeaderUIModel, selectedSort: BookmarkSortOption, onSortClick: (BookmarkSortOption) -> Unit, onAlertsClick: () -> Unit, contentPadding: Dp, modifier)` ([§2.2](#22-sort-strip), [§2.3](#23-counter-row-and-alerts-chip)) + `BookmarksHeaderSkeletonView(contentPadding, modifier)`, `@AdaptivePreviews` (alerts on / off / unknown) |
| `presentation/mapper/EpisodeAlertUIModelMapper.kt` | **new** | `fun EpisodeAlert.toEpisodeAlertUIModel(): EpisodeAlertUIModel` |
| `presentation/model/EpisodeAlertUIModel.kt` | **new** | `@Immutable data class EpisodeAlertUIModel(val notificationId: Int, val slug: String, val title: UiText, val text: UiText, val subText: UiText?, val summaryLine: UiText)` |
| `presentation/BookmarksState.kt`, `BookmarksIntent.kt`, `BookmarksEffect.kt` | **new** | [§6.11](#611-mvi-contract) |
| `presentation/BookmarksViewModel.kt` | **new** | `@KoinViewModel class BookmarksViewModel(observeBookmarks: ObserveBookmarksUseCase, private val refreshAiringSlots: RefreshAiringSlotsUseCase, private val removeBookmark: RemoveBookmarkUseCase, private val restoreBookmark: RestoreBookmarkUseCase, private val getAlertsEnabled: GetReleaseAlertsEnabledUseCase)`. Keeps the last removed `Bookmark` private (Undo). |
| `presentation/BookmarksRoute.kt` | **new** | `BookmarksRoute(onOpenDetails: (slug: String) -> Unit, onDiscoverShows: () -> Unit, onOpenSettings: (() -> Unit)?, modifier, viewModel = koinViewModel())`: `collectAsStateWithLifecycle()`, `LifecycleStartEffect(viewModel) { onIntent(ScreenStarted); onStopOrDispose {} }`, `rememberNotificationPermissionRequester { onIntent(AlertsPermissionResult(it)) }`, `ObserveAsEvents` (table below), owns the `SnackbarHostState`. |
| `presentation/BookmarksScreen.kt` | **new** | `BookmarksScreen(state, onIntent, modifier, showSettingsAction: Boolean = true, snackbarHostState = remember { SnackbarHostState() })`. Private: `BookmarksLayout` (margin; columns 2 / 3 / 4 from `isWidthAtLeastBreakpoint`, never in State), `BookmarksGrid` (shared by content and skeleton), `BookmarksContent`, `BookmarksSkeleton`, `BookmarksEmptyView`. `@AdaptivePreviews` with a `PreviewParameterProvider<BookmarksState>`. |
| `presentation/navigation/BookmarksNavKey.kt` | **new** | `@Serializable data object BookmarksNavKey : NavKey` |
| `presentation/navigation/BookmarksNavigation.kt` | **new** | `fun EntryProviderScope<NavKey>.bookmarksEntry(onOpenDetails: (slug: String) -> Unit, onDiscoverShows: () -> Unit, onOpenSettings: (() -> Unit)?)` → `entry<BookmarksNavKey> { BookmarksRoute(…) }`. No `navigateToBookmarks()` (nothing opens the tab yet). |

Route effects:

| Effect | Route action |
|---|---|
| `OpenDetails(slug)` | `onOpenDetails(slug)` |
| `OpenSchedule` | `onDiscoverShows()` |
| `OpenSettings` | `onOpenSettings?.invoke()` |
| `ShowRemoved(message, slug)` | `scope.launch { if (snackbar(message, actionLabel = Undo, Short) == ActionPerformed) onIntent(UndoRemoveClicked(slug)) }` |
| `EnableAlerts` | `if (context.canAskNotificationPermission()) requestPermission() else context.openAppNotificationSettings()` |
| `OpenNotificationSettings` | `context.openAppNotificationSettings()` |
| `ShowAlertsDenied` | snackbar `bookmarks_alerts_denied` + action "Settings" → `openAppNotificationSettings()` |
| `ShowError(error)` | snackbar with the message |

`BookmarksScreen` layout:
- `Scaffold(modifier.nestedScroll(scrollBehavior.nestedScrollConnection), topBar = { TopLevelAppBarView(…) }, snackbarHost = …)`, default content insets (the earlier screens' lesson).
- Body `when`: `state.error != null` → `ErrorView`; `state.isLoading` → `BookmarksSkeleton`; else `PullToRefreshBox(state.isRefreshing, onRefresh = { onIntent(Refresh) })` → `BookmarksContent`.
- `BookmarksContent` = `BookmarksGrid(gridState = rememberLazyGridState())`: either `item(key = "empty", span = full, contentType = "empty")`, or `item(key = "header", span = full, contentType = "header") { BookmarksHeaderView(…) }` + `items(state.cards, key = { it.slug }, contentType = { "card" }) { BookmarkCardView(…, Modifier.animateItem()) }`.
- Lambdas adapted once: `remember(onIntent) { { slug: String -> onIntent(BookmarkClicked(slug)) } }`, likewise remove and sort.

### 6.6 Notifications (presentation/notification)

| Path | | Responsibility |
|---|---|---|
| `presentation/notification/ReleaseAlertNotifierImpl.kt` | **new** | `@Factory class ReleaseAlertNotifierImpl(private val context: Context) : ReleaseAlertNotifier`. `areAlertsEnabled()` = `NotificationManagerCompat.areNotificationsEnabled()` && the channel isn't `IMPORTANCE_NONE` (when it exists) && (API < 33 or `POST_NOTIFICATIONS` granted). `notify(alerts)`: no-op when empty or disabled (the explicit permission check also satisfies lint's `MissingPermission`); ensure the channel; map with `toEpisodeAlertUIModel()`; one `NotificationCompat.Builder` per alert (`asString(context)` for every text, `PendingIntent` of [§5.7](#57-deep-link)); post the summary when 2+ of the group are active (`NotificationManager.activeNotifications`). Constants `CHANNEL_NEW_EPISODES = "new_episodes"`, `GROUP_NEW_EPISODES = "new_episodes"`, `SUMMARY_NOTIFICATION_ID`. |

### 6.7 Navigation and app

| Path | | Change |
|---|---|---|
| `app/navigation/TopLevelDestination.kt` | modify | `BOOKMARKS(key = BookmarksNavKey, …)`; the KDoc no longer mentions disabled tabs (keep the `key: NavKey?` support for future tabs or drop it, implementor's call) |
| `app/navigation/AppEntryProvider.kt` | modify | `bookmarksEntry(onOpenDetails = navigator::navigateToDetails, onDiscoverShows = navigator::navigateToSchedule, onOpenSettings = null)` |
| `app/navigation/AppNavigation.kt` | modify | `AppNavigation(showDeepLinks: Flow<String>, modifier)`; `ObserveAsEvents(showDeepLinks) { navigator.navigateToDetailsFromStart(it) }` |
| `app/AppRoute.kt` | modify | `AppRoute(showDeepLinks: Flow<String>, modifier)` passes it through |
| `app/MainActivity.kt` | modify | The `Channel` + `onCreate` / `onNewIntent` handling of [§5.7](#57-deep-link) |
| `app/AppApplication.kt` | modify | `startKoin<AppKoin> { androidLogger(); androidContext(…); workManagerFactory() }` (`org.koin.androidx.workmanager.koin.workManagerFactory`) |

### 6.8 di

| Path | | Change |
|---|---|---|
| `feature/bookmarks/di/BookmarksModule.kt` | modify | Drop the DataStore provider and `@Named`. `@Single fun bookmarksDatabase(context: Context): BookmarksDatabase = Room.databaseBuilder<BookmarksDatabase>(context, BOOKMARKS_DATABASE_NAME).build()`, `@Single fun bookmarkDao(database: BookmarksDatabase): BookmarkDao`, `@Single fun workManager(context: Context): WorkManager = WorkManager.getInstance(context)`. `@ComponentScan` picks up the repository, scheduler, worker (`@KoinWorker`), notifier and ViewModel. |
| `app/di/AppModule.kt` | keep | Already includes `BookmarksModule` |

### 6.9 Resources

`res/values/strings.xml`, a new `<!-- Bookmarks (feature/bookmarks) -->` section:
- `bookmarks_app_bar_subtitle` "BOOKMARKS"
- `bookmarks_sort_recently_added` "Recently added", `bookmarks_sort_title` "Title (A–Z)", `bookmarks_sort_release_time` "Release time"
- `bookmarks_header_title` "SAVED SHOWS"; plural `bookmarks_count`: one "%d show", other "%d shows"
- `bookmarks_alerts_on` "ALERTS ON", `bookmarks_alerts_off` "ALERTS OFF", `cd_bookmarks_alerts_on`, `cd_bookmarks_alerts_off` ([§2.3](#23-counter-row-and-alerts-chip))
- `bookmarks_added_on` "Added %1$s"; `bookmarks_airing_today` "TODAY %1$s", `bookmarks_airing_day` "%1$s %2$s", `bookmarks_not_airing` "NOT AIRING"
- `cd_bookmarks_open` "Open details", `cd_bookmarks_remove` "Remove %1$s from bookmarks"
- `bookmarks_removed` "Removed %1$s", `bookmarks_undo` "Undo"
- `bookmarks_empty_title`, `bookmarks_empty_message`, `bookmarks_empty_action` ([§2.6](#26-empty-state))
- `bookmarks_alerts_denied` "Notifications are off for BakaMitai.", `bookmarks_alerts_settings` "Settings"
- Notifications: `notification_channel_new_episodes` "New episodes", `notification_channel_new_episodes_description` "When a bookmarked show releases a new episode", `notification_episode_out` "%1$s is out", `notification_summary_line` "%1$s · %2$s"; plurals `notification_more_new_episodes` and `notification_summary` (one "%d new episode", other "%d new episodes")
- Details: `details_alerts_denied` "New-episode alerts are off. You can turn them on in Bookmarks."
- Renamed (moved to common): `details_episode` → `episode_label_number`, `details_episode_revision` → `episode_label_revision`, `details_episode_batch` → `episode_label_batch`, `details_episode_movie` → `episode_label_movie`

### 6.10 Details changes

| Path | | Change |
|---|---|---|
| `feature/details/presentation/DetailsEffect.kt` | modify | `data object AskForReleaseAlerts : DetailsEffect` |
| `feature/details/presentation/DetailsViewModel.kt` | modify | `onBookmarkClicked()`: `toggleBookmark(show).onSuccess { if (it.isFirstBookmark) _effect.send(AskForReleaseAlerts) }.onFailure { ShowError }` |
| `feature/details/presentation/DetailsRoute.kt` | modify | `val askForAlerts = rememberNotificationPermissionRequester { granted -> if (!granted) scope.launch { snackbar(details_alerts_denied) } }`; `AskForReleaseAlerts` → `if (context.canAskNotificationPermission()) askForAlerts()` |

The bookmark button keeps working: `ObserveIsBookmarkedUseCase` now reads Room's `Flow`.

### 6.11 MVI contract

```kotlin
@Immutable
data class BookmarksState(
    val isLoading: Boolean = true,                                   // until the first Room emission
    val error: ErrorUIModel? = null,                                 // Room read failed
    val selectedSort: BookmarkSortOption = BookmarkSortOption.RECENTLY_ADDED,
    val header: BookmarksHeaderUIModel = BookmarksHeaderUIModel(),   // sort chips, "8 shows", alerts chip (null until checked)
    val cards: ImmutableList<BookmarkCardUIModel> = persistentListOf(),
    val isRefreshing: Boolean = false,
) {
    val isEmpty: Boolean get() = !isLoading && error == null && cards.isEmpty()
}

sealed interface BookmarksIntent {
    data object Load : BookmarksIntent                               // from init
    data object ScreenStarted : BookmarksIntent                      // LifecycleStartEffect
    data object Refresh : BookmarksIntent                            // pull-to-refresh
    data object Retry : BookmarksIntent                              // ErrorView
    data class SortSelected(val option: BookmarkSortOption) : BookmarksIntent
    data class BookmarkClicked(val slug: String) : BookmarksIntent
    data class RemoveClicked(val slug: String) : BookmarksIntent
    data class UndoRemoveClicked(val slug: String) : BookmarksIntent
    data object DiscoverShowsClicked : BookmarksIntent
    data object AlertsChipClicked : BookmarksIntent
    data class AlertsPermissionResult(val granted: Boolean) : BookmarksIntent
    data object SettingsClicked : BookmarksIntent
}

sealed interface BookmarksEffect {
    data class OpenDetails(val slug: String) : BookmarksEffect
    data object OpenSchedule : BookmarksEffect
    data object OpenSettings : BookmarksEffect
    data class ShowRemoved(val message: UiText, val slug: String) : BookmarksEffect
    data object EnableAlerts : BookmarksEffect
    data object OpenNotificationSettings : BookmarksEffect
    data object ShowAlertsDenied : BookmarksEffect
    data class ShowError(val error: ErrorUIModel) : BookmarksEffect
}
```

**Reducer rules:**
- **`state`** = `combine(_state, bookmarks) { s, result -> … }.stateIn(viewModelScope, WhileSubscribed(5_000), BookmarksState())`, where `bookmarks` = `combine(_state.map { it.selectedSort }.distinctUntilChanged(), retries) { sort, _ -> sort }.flatMapLatest { observeBookmarks(it.toDomain()).map { Result.success(it) }.catch { emit(Result.failure(it)) } }` (`retries` is a private `MutableStateFlow<Int>`). Success → `isLoading = false, error = null, cards = …, header = s.header.copy(countLabel = …)`; failure → `isLoading = false, error = it.toErrorUIModel()`.
- **`Load`:** `header = header.copy(sortChips = sortChipUIModels())`; then as `ScreenStarted`.
- **`ScreenStarted`:** `header = header.copy(alertsChip = getAlertsEnabled().toAlertsChipUIModel())`; `refreshAiringSlots(forceRefresh = false)` in the background (ignored while one runs; failure silent).
- **`Refresh`:** ignored while refreshing. `isRefreshing = true`; `refreshAiringSlots(forceRefresh = true)`; failure → `ShowError`; `isRefreshing = false`.
- **`Retry`:** `retries.update { it + 1 }`.
- **`SortSelected`:** `selectedSort = option`.
- **`BookmarkClicked`:** `OpenDetails`. **`DiscoverShowsClicked`:** `OpenSchedule`. **`SettingsClicked`:** `OpenSettings`.
- **`RemoveClicked`:** `removeBookmark(slug)` → success with a bookmark → keep it as `lastRemoved`, `ShowRemoved(StringResource(bookmarks_removed, [title]), slug)`; failure → `ShowError`.
- **`UndoRemoveClicked`:** only when `lastRemoved?.show?.slug` matches → `restoreBookmark(it)` (failure → `ShowError`); clear `lastRemoved`.
- **`AlertsChipClicked`:** enabled → `OpenNotificationSettings`; disabled → `EnableAlerts`; unknown → ignored.
- **`AlertsPermissionResult`:** re-read `alertsChip`; not granted → `ShowAlertsDenied`.

The ViewModel stays under Detekt's 11 functions: `onIntent`, `checkAlerts`, `refreshSlots(force)`, `remove`, `undo`, `onAlertsChipClicked`, `sendEffect`, plus the private flow builders.

---

## 7. Dependencies

### Libraries (all need the skill updates of step 1 first, `library-management` §5)

| Library | Coordinates / version | Status |
|---|---|---|
| **Room** | `androidx.room:room-runtime` / `room-compiler` (KSP) **2.8.5** (9 Sep 2026); Gradle plugin `androidx.room` 2.8.5 | **Approved by the user (D1).** Room 2.8.5 rather than Room 3 (`androidx.room3` 3.0.3): WorkManager 2.12.0 already ships Room 2.x (`room-runtime` 2.7.0 at runtime), so the app keeps one SQLite stack, upgraded to 2.8.5 ([Q1](#11-open-questions)). |
| **KSP** | Plugin `com.google.devtools.ksp` **2.3.14** (13 Nov 2026); build-logic `symbol-processing-gradle-plugin` 2.3.14 | **Approved as Room's processor only (D1).** Koin stays on its compiler plugin. Compatibility notes and fallbacks in [§6.2](#62-build); step 2 verifies it. |
| **WorkManager** | `androidx.work:work-runtime` **2.12.0** (23 Sep 2026; minSdk 24; includes `CoroutineWorker`, `work-runtime-ktx` is empty) | **Approved by the user (D4).** Brings `androidx.startup`, `lifecycle-livedata` (never imported; the banned-import grep still passes) and Room 2.7.0 (aligned to 2.8.5). |
| **WorkManager testing** | `androidx.work:work-testing` 2.12.0, `androidTestImplementation` only | Test-only part of the WorkManager approval ([Q4](#11-open-questions)). |
| **Koin WorkManager** | `io.insert-koin:koin-androidx-workmanager` from the existing **koin-bom 4.2.2** | **Needs a one-line approval** ([Q3](#11-open-questions)): it's a separate artifact, not covered by tech-stack's "Koin (Android, Compose)", though it's the same release train. Needed: the Koin compiler plugin 1.2.3 supports `@KoinWorker` (`org.koin.android.annotation.KoinWorker`, already in `koin-annotations`) and fails with `MissingWorkerArtifact` ("'buildWorker' is not on classpath") without it; workers can't take constructor dependencies otherwise (`KoinComponent` is banned). |
| Notifications | `androidx.core` (`NotificationCompat`, `NotificationManagerCompat`), `activity-compose` (`rememberLauncherForActivityResult`) | Already dependencies |
| DataStore (typed) | `androidx.datastore:datastore` | **Removed** (its only user is gone); `datastore-preferences` stays for Settings |

### Earlier plans and shared code

- **Needs (exists):** Details' bookmarks data/domain (rewritten here), `DetailsNavKey`, Home's `BroadcastRepository` and `Release`, Schedule's `ScheduleRepository` + cache, `EmptyStateView`, `TopLevelAppBarView`, `CoverImageView`, skeletons, `ObserveAsEvents`, `Navigator` / `NavigationState`.
- **Moves to `common/`:** `toDateLabel()` and `toEpisodeTitle()` (from Details), `toDayName()` / `toShortDayName()` and the time chip (from Schedule).
- **Adds to other features:** Home's `GetNewestReleasesUseCase` and `SearchShowReleasesUseCase`; Schedule's `WeeklyTemplate`, `GetWeeklyTemplateUseCase` and `navigateToSchedule()`; Details' `navigateToDetailsFromStart()` and the first-bookmark ask.

### What it unblocks

- **Settings plan:** the alerts switch (gate `areAlertsEnabled()` / the check, cancel `release-check` when off, skip Details' ask), persisting `BookmarkSortOption`, 24-hour times in the airing chips (they use the shared `toTimeLabel()`), and showing the Settings gear on Bookmarks too.
- **Any later Room use** (a second table, a second feature) reuses `bakamitai.android.room`.
- **Any later background job** follows `data/worker/` + `@KoinWorker`.

---

## 8. Testing

Unit tests in `app/src/test/…` (JUnit 4, `kotlin.test`, `runTest`, Turbine, hand-written fakes, `FixedClock`, `FixedTimeZoneProvider`, `MainDispatcherRule`). Room and WorkManager need Android, so their tests are instrumented (`app/src/androidTest/…`, `./gradlew connectedDebugAndroidTest`); everything with logic is plain Kotlin and unit-tested.

### Fakes

| Fake | Notes |
|---|---|
| `feature/bookmarks/domain/repository/FakeBookmarkRepository.kt` | **Modify** for the new interface: `MutableStateFlow<List<Bookmark>>`, watches map, settable failures, recorded calls. Used by Details' tests too. |
| `feature/bookmarks/domain/repository/FakeReleaseCheckScheduler.kt` | **New.** Records `requestCheck` / `ensureCheckScheduled` / `scheduleNextCheck(at)` calls. |
| `feature/bookmarks/domain/repository/FakeReleaseAlertNotifier.kt` | **New.** `enabled` flag, posted alerts list. |
| `feature/bookmarks/data/local/FakeBookmarkDao.kt` | **New.** Extends `BookmarkDao` over an in-memory map (its `@Transaction` bodies run as-is); can throw `SQLiteException`. |
| `feature/home/domain/repository/FakeBroadcastRepository.kt`, `feature/schedule/domain/repository/FakeScheduleRepository.kt` | Reused (extended only if needed). |

### Test classes

| Class | Covers |
|---|---|
| `common/ui/mapper/DateUIModelMapperTest`, `EpisodeLabelUIModelMapperTest`, `DayOfWeekUIModelMapperTest` | Moved from Details / Schedule (same cases, new resource names). |
| `common/ui/navigation/NavigatorTest` | **Modify:** `navigateFromStart` selects Home from another tab, pops Home's stack, pushes Details; other tabs' stacks untouched; a top-level key isn't pushed. |
| `feature/home/domain/usecase/GetNewestReleasesUseCaseTest` | Empty search returned; search failure → latest p=1; both fail → failure. |
| `feature/home/domain/usecase/SearchShowReleasesUseCaseTest` | Filters other slugs (other seasons), newest first, `[]` → empty. |
| `feature/schedule/domain/usecase/GetWeeklyTemplateUseCaseTest` | Uses the current zone; `forceRefresh` passed through; failure propagated. |
| `feature/schedule/presentation/component/schedulecard/mapper/ScheduleCardUIModelMapperTest` | **Modify:** `TimeChipUIModel` tone follows the status. |
| `feature/bookmarks/domain/model/EpisodeKeyTest` | `"1216v2"` → `"1216"`, `"07"` → 7.0, `"12.5"`, `"Egghead SP11"` → no number, `"Movie"`; batch ranges detected (`"01-24"`, `"01 - 28"`). |
| `feature/bookmarks/domain/model/AiringSlotTest` | `latestAtOrBefore` / `firstAfter` / `nearestTo` at, just before and just after the slot; `Asia/Tokyo` slot; `America/New_York` slot across the 1 Nov 2026 DST change; a slot inside a DST gap. |
| `feature/bookmarks/domain/model/ReleaseWatchTest` | **Every rule of [§5.3–§5.4](#53-scheduling):** pending occurrence with `resolvedThrough = null` (bookmarked 2 h after the slot → pending; 2 days after → not); ladder times; give-up only after a successful check at `E + 24 h`; `nextCheckAt` for pending, resolved and unscheduled shows; baseline without alert for a release before tracking; release 30 min after tracking with the 1 h-early `release_date` → new; revision not new; older episode released later not new; special and Movie new by time; batch ignored; backlog → one alert with the newest episode and the count; nearest-occurrence resolution for an early and a late release. |
| `feature/bookmarks/domain/usecase/ObserveBookmarksUseCaseTest` | Three sorts (case-insensitive titles; unscheduled last); next airing and `airsToday` in the device zone; labels move after the slot passes (virtual time + ticker); no duplicate emissions. |
| `ToggleBookmarkUseCaseTest` (modify), `RemoveBookmarkUseCaseTest`, `RestoreBookmarkUseCaseTest` | `isFirstBookmark` only for 0 → 1; `requestCheck()` after each successful write, not after a failure; restore keeps `addedAt` and uses now as `trackingSince`. |
| `UpdateAiringSlotsUseCaseTest`, `RefreshAiringSlotsUseCaseTest` | Fresh slots → no request; stale or missing → one template fetch; absent slug → `null` slot; schedule title stored; duplicate slug → first by Monday → Sunday; changed → `requestCheck`, unchanged or failed → `ensureCheckScheduled`; forced refresh passes `forceRefresh` down. |
| `CheckBookmarkedReleasesUseCaseTest` | No bookmarks → no requests, `nextCheckAt = null`; nothing pending → no requests; alerts disabled → no requests, pending resolved; one sweep for several pending shows; search failure → latest p=1; both fail → no updates, ladder next; per-show fallback only when the window doesn't reach back, using the schedule title (not an en-dash page title), capped at 5 with a 15-min continuation; opportunistic alert for an unscheduled bookmark; coalescing within 15 min; stale slots refreshed first. |
| `ApplyReleaseCheckUseCaseTest` | Saves then notifies; a failed save posts nothing. |
| `feature/bookmarks/data/mapper/BookmarkEntityMapperTest`, `BookmarkDomainMapperTest` | Epoch ms ↔ `Instant`; slot only when every airing column is valid (unknown zone, minute out of range → none); blank slug/title → `null`. |
| `feature/bookmarks/data/repository/BookmarkRepositoryImplTest` | Over `FakeBookmarkDao`: toggle result and `isFirstBookmark` (0 → 1 only), remove returns the entity, restore, slot and tracking updates; `SQLiteException` → `DomainError.Unknown`; cancellation not caught. |
| `feature/bookmarks/presentation/BookmarksViewModelTest` | Loading → content; empty; Room failure → error → Retry; sort change re-sorts; remove → `ShowRemoved` → Undo restores the same bookmark (and an Undo for another slug is ignored); refresh success / failure snackbar; alerts chip states and `EnableAlerts` / `OpenNotificationSettings`; denied → `ShowAlertsDenied`; navigation effects. |
| `…/component/bookmarkcard/mapper/BookmarkCardUIModelMapperTest` | "Added Nov 18, 2026"; "TODAY 9:30 PM" (UPCOMING), "MON 9:30 PM" (DEFAULT), "NOT AIRING" (MUTED); remove label with the title. |
| `…/component/bookmarksheader/mapper/BookmarksHeaderUIModelMapperTest` | Count plural; alerts chip on/off. |
| `feature/bookmarks/presentation/mapper/EpisodeAlertUIModelMapperTest` | "Episode 8 is out", "Movie is out", "Egghead SP11 is out", sub-text only when the count > 1, stable id per slug. |
| `feature/details/presentation/DetailsViewModelTest` | **Modify:** first bookmark → `AskForReleaseAlerts`; a second bookmark or a removal → no effect. |
| `app/di/KoinModulesTest` | **Modify:** `verify()` with `WorkerParameters::class` (and `Context::class`) in `extraTypes`; the bookmarks and worker bindings resolve. |
| `app/navigation/NavKeySerializationTest` | **Modify:** add `BookmarksNavKey`. |
| Deleted | `BookmarkListSerializerTest`, `BookmarksLocalDataSourceTest`, `BookmarkDTOMapperTest`. |

### Instrumented tests (`connectedDebugAndroidTest`)

| Class | Covers |
|---|---|
| `feature/bookmarks/data/local/BookmarkDaoTest` | `Room.inMemoryDatabaseBuilder`: insert/toggle/remove/restore, `added_at DESC` order, a double toggle leaves one or zero rows, the `Flow` emits on change, slot and tracking updates. |
| `feature/bookmarks/data/worker/ReleaseCheckSchedulerImplTest` | `WorkManagerTestInitHelper`: `requestCheck` replaces a delayed run; `ensureCheckScheduled` keeps it; `scheduleNextCheck` from a running work is chained (not cancelled) with the right delay and the network constraint. |
| `feature/bookmarks/data/worker/ReleaseCheckWorkerTest` | `TestListenableWorkerBuilder` with a factory over fakes: returns success; passes the alerts flag; saves before notifying; schedules `nextCheckAt`, and nothing when it's `null`. |
| `feature/bookmarks/presentation/notification/ReleaseAlertNotifierImplTest` | Permission granted through `UiAutomation.grantRuntimePermission`: one alert → one notification with the title, text, group and a content intent carrying the slug; two alerts → two notifications + the summary; disabled → nothing posted. |
| `common/ui/navigation/ShowDeepLinkTest` | `showDetailsIntent()` targets MainActivity with the slug and flags; `showSlugOrNull()` ignores blank slugs and `FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY` (`Intent` needs a device). |

### Compose previews

- `BookmarksScreen`: loading, content (2 sorts, alerts on and off), empty, error × `@AdaptivePreviews` (light/dark × compact/expanded).
- `BookmarkCardView`: today, weekday, not airing, long title, no cover; `BookmarkCardSkeletonView` in a `SkeletonView`.
- `BookmarksHeaderView`: alerts on / off / unknown; its skeleton.
- `TimeChipView`: the three tones.

---

## 9. Implementation steps

### Phase 0: approvals, build and shared prerequisites

1. [x] **Skills** (after the user approves this plan; the skills are binding, so they change first):
   - `tech-stack`: §1 Persistence = DataStore (preferences) + **Room** (structured data, KSP for Room's compiler only); add **WorkManager** (background work) and **Koin WorkManager**; §2 layer rows (Room and WorkManager: `data` + `di`; notifications: `presentation/notification/`, `common/ui`, `app`); banned: `SQLiteOpenHelper`/raw SQLite, exact alarms, `JobScheduler`/`AlarmManager`/`Service` for background work, `allowMainThreadQueries`, `fallbackToDestructiveMigration`; §6 a Room section (DAO functions `suspend` or `Flow`, entities primitives only, schema exported); §7 "Koin uses its compiler plugin, not KSP; KSP runs only Room's compiler"; `@KoinWorker` for workers.
   - `library-management`: artifact-map rows for Room, KSP, WorkManager, `work-testing`, `koin-androidx-workmanager`, the two build-logic compile-only plugins; `bakamitai.android.room` in §4 and its current-state note.
   - `structure`: the folders of [§6](#6-architecture) (`data/local/` Room, `data/worker/`, domain ports, `presentation/notification/`), `app/schemas/` in §1.2, §2 rows, §7 current state (Bookmarks complete; the temporary exception is gone).
   - `conventions`: naming rows `[Name]Entity`, `[Name]Dao`, `[Name]Database`, `[Name]EntityMapper.kt`, `[Name]Worker`, `[Name]Scheduler` / `[Name]SchedulerImpl`, `[Name]Notifier` / `[Name]NotifierImpl`.
   - `architecture`: data may use Room and WorkManager; a worker is a `data` entry point that calls its own feature's use cases; notifications are `presentation`, behind a domain port.
   - `best-practices`: Room `SQLiteException` → `DomainError.Unknown` (§4); DAO and worker tests are instrumented (§6).
   **Done when:** the six skills describe Room, KSP, WorkManager and the new folders, and their checks still print nothing on the current code.
2. [x] **Build spike (go/no-go):** catalog, root plugins, `AndroidRoomConventionPlugin`, `app/build.gradle.kts` ([§6.2](#62-build)); `BookmarkEntity`, `BookmarkDao` and `BookmarksDatabase` compiled (old DataStore code still in place).
   **Done when:** `./gradlew assembleDebug testDebugUnitTest detekt` and `./gradlew assembleRelease` pass, `app/schemas/…/1.json` is generated, `KoinModulesTest` passes. If KSP fails, apply [§6.2](#62-build)'s fallbacks in order, then stop and report.
3. [x] **Shared moves:** `toDateLabel()`, `toEpisodeTitle()` (+ string renames), day-name mappers, `TimeChipView` + models; Details and Schedule use them; `DrawableResource` + `ic_notification.xml`; tests moved.
   **Done when:** unit tests pass and Schedule and Details look unchanged (previews).
4. [x] **Navigation and platform helpers:** `Navigator.navigateFromStart`, `navigateToDetailsFromStart`, `navigateToSchedule`, `ShowDeepLink.kt`, `NotificationPermission.kt`; `NavigatorTest`, `ShowDeepLinkTest`.
   **Done when:** the tests pass.
5. [x] **Home and Schedule use cases:** `GetNewestReleasesUseCase`, `SearchShowReleasesUseCase`, `WeeklyTemplate`, `GetWeeklyTemplateUseCase` + tests.
   **Done when:** the tests pass.

### Phase 1: bookmarks on Room

6. [x] **Domain models and ports:** [§6.4](#64-featurebookmarksdomain) models, `ReleaseCheckPolicy`, `ReleaseCheckScheduler`, `ReleaseAlertNotifier`, the new `BookmarkRepository`; `EpisodeKeyTest`, `AiringSlotTest`, `ReleaseWatchTest`; the three fakes.
   **Done when:** the pure tests pass.
7. [x] **Data on Room (D2):** delete the DataStore files and their tests; full `BookmarkDao`; mappers; `BookmarkRepositoryImpl`; `BookmarksModule` providers; `FakeBookmarkDao` + `BookmarkRepositoryImplTest`; `BookmarkDaoTest`.
   **Done when:** unit tests pass, `BookmarkDaoTest` passes on an emulator, and `grep -rn "datastore\|bookmarks.json" app/src/main/java/com/zc/bakamitai/feature/bookmarks` prints nothing.
8. [x] **Bookmark use cases:** `ToggleBookmarkUseCase` (new result + `requestCheck`), `ObserveBookmarksUseCase`, `RemoveBookmarkUseCase`, `RestoreBookmarkUseCase`, `UpdateAiringSlotsUseCase`, `RefreshAiringSlotsUseCase`, `GetReleaseAlertsEnabledUseCase` + tests; `DetailsViewModel` adapted to `BookmarkToggle` (no effect yet).
   **Done when:** tests pass; Details' bookmark button toggles on an emulator and survives an app restart.
9. [x] **Release check:** `CheckBookmarkedReleasesUseCase`, `ApplyReleaseCheckUseCase` + tests.
   **Done when:** both test classes pass, covering every quirk of [§4.1](#41-endpoints).

### Phase 2: background work and notifications

10. [x] **WorkManager:** `ReleaseCheckSchedulerImpl`, `ReleaseCheckWorker`, `workManagerFactory()` in `AppApplication`, the manifest changes, the `WorkManager` provider; `KoinModulesTest`; the two instrumented worker tests.
    **Done when:** they pass, and on an emulator, after bookmarking a scheduled show, `adb shell dumpsys jobscheduler | grep -A3 com.zc.bakamitai` shows one job with a network constraint and a delay ending about 10 min after the show's next slot.
11. [x] **Notifier:** `EpisodeAlert` → `EpisodeAlertUIModel` mapper + test, `ReleaseAlertNotifierImpl`, strings, channel.
    **Done when:** `EpisodeAlertUIModelMapperTest` passes and the instrumented `ReleaseAlertNotifierImplTest` posts "<title> / Episode 8 is out" (one alert) and two notifications plus a summary (two alerts).
12. [x] **Deep link and the permission ask:** `MainActivity`, `AppRoute`, `AppNavigation`; Details' `AskForReleaseAlerts`, Route handling, `details_alerts_denied`; `DetailsViewModelTest`.
    **Done when:** on API 33+ the first bookmark shows the system dialog once, and `adb shell am start -n com.zc.bakamitai/.app.MainActivity --es com.zc.bakamitai.extra.SHOW_SLUG sousou-no-frieren` opens Frieren's Details over Home, both from a killed app and from the app open on another tab.

### Phase 3: the Bookmarks screen

13. [x] **Components:** `BookmarkCardView` + skeleton, `BookmarksHeaderView` + skeleton, their models and mappers + tests, `@AdaptivePreviews`.
    **Done when:** mapper tests pass and the previews render in light and dark.
14. [x] **MVI:** `BookmarksState`, `BookmarksIntent`, `BookmarksEffect`, `BookmarksViewModel` + `BookmarksViewModelTest`.
    **Done when:** the test passes.
15. [x] **Screen, Route and navigation:** `BookmarksScreen` (+ previews), `BookmarksRoute`, `BookmarksNavKey`, `bookmarksEntry`, `TopLevelDestination.BOOKMARKS.key`, `AppEntryProvider`; `NavKeySerializationTest`.
    **Done when:** the tab is enabled and every acceptance criterion under "Screen" in [§10](#10-acceptance-criteria) holds on an emulator.

### Phase 4: docs and verification

16. [x] **api.md** (done 2026-11-19, separately from the implementor): [§4.2](#42-api-gaps-found-live) findings (§3.3, §3.4, §3.5) and the §7 exception ([§5.6](#56-politeness)). **Done when:** api.md says when background requests are allowed and why.
17. [x] **context.md** (done 2026-11-19, separately from the implementor): §3.4 (sort, next airing, remove + Undo, alerts chip, pull-to-refresh, empty CTA), a new §3.7 "New-episode alerts" (D3–D6, [§5.8](#58-verdict)'s limits), §4.6 Bookmarks data mapping, §5 rewritten for Room (table of [§4.3](#43-local-data)), §6 sources, §7 decisions (Room, WorkManager, notifications, 2026-11-19). **Done when:** context.md no longer says "no Room/SQL".
18. [x] **CLAUDE.md** (done 2026-11-19) "Current state": five features, Room + KSP (Room only), WorkManager + `@KoinWorker`, release alerts, every tab enabled. **Done when:** it matches the code.
19. [x] **Plan status:** this file's status → "Implemented", steps ticked, an "Implementation notes" section for deviations.
20. [x] **Static checks** (each must print nothing):
    - `architecture` §1 greps (domain purity; presentation ↛ data)
    - `structure` §7 checks; `tech-stack` §1 verification commands (incl. the Ksoup grep); `library-management` §6 checks
    - `grep -rnE "import androidx\.(room|work)\." app/src/main/java/com/zc/bakamitai/feature/*/presentation app/src/main/java/com/zc/bakamitai/feature/*/domain app/src/main/java/com/zc/bakamitai/common`
    - `conventions` §2.5 resource grep
21. [x] **Build and tests:** `./gradlew detekt`, `./gradlew testDebugUnitTest`, `./gradlew connectedDebugAndroidTest` (emulator), `./gradlew assembleRelease && grep -rn "unstable" app/build/compose_compiler/*-composables.txt` (prints nothing for Screens and Views), then `./gradlew assembleDebug` and `android run --apks=app/build/outputs/apk/debug/app-debug.apk` for the manual checks of [§10](#10-acceptance-criteria). **Done when:** all pass and every box in §10 is ticked or listed as not checked.

---

## 10. Acceptance criteria

**Screen**
- [x] The Bookmarks tab is enabled (bar on phones, rail on tablets) and shows "BakaMitai / BOOKMARKS".
- [x] Bookmarked shows appear newest first, each with its cover (or the placeholder), title (≤ 2 lines), "Added <Mon d, yyyy>" and an airing chip: "TODAY 9:30 PM" in the tertiary style for a show airing later today, "MON 9:30 PM" for another day, "NOT AIRING" for a show missing from the weekly schedule.
- [ ] "Title (A–Z)" and "Release time" re-sort the grid (unscheduled shows last) and scroll it to the top; the choice survives switching tabs and rotating.
- [x] Tapping a card opens its Details; back returns to the same scroll position.
- [x] The remove button deletes the bookmark, the card animates out, "Removed <title>" offers Undo, and Undo puts it back in its original place.
- [x] Toggling the bookmark in Details adds or removes the card immediately (no refresh).
- [x] With no bookmarks: the empty state, and "Discover airing shows" opens the Schedule tab.
- [x] In airplane mode the grid still shows everything; pull-to-refresh shows an error snackbar and keeps the grid.
- [x] 2 / 3 / 4 columns on compact / medium / expanded; light and dark both readable.
- [ ] TalkBack reads each card as one item ("TODAY 9:30 PM, Added …, <title>"), the remove button as "Remove <title> from bookmarks", offers "Remove from bookmarks" as a card action, and reads the alerts chip's state.

**Alerts**
- [x] On Android 13+, the first bookmark of a fresh install shows the notification permission dialog once; later bookmarks don't. Denying shows the Details snackbar and the Bookmarks chip says "ALERTS OFF"; tapping it shows the dialog again (or the system settings once Android stops showing it).
- [ ] With alerts on, bookmark a show from Schedule's today tab that airs within the next hour, lock the device: after its release, a notification "<title> / Episode N is out" arrives (expected within ~15 min of the slot when the device is active; `adb shell dumpsys jobscheduler` shows the pending job).
- [x] Tapping it opens that show's Details over Home; Back → Home; Back → leaves the app. With the app open on another tab, the tap switches to Home with Details on top and the other tab keeps its stack.
- [ ] No second notification for the same episode after another run (`adb shell cmd jobscheduler run -f -n androidx.work.systemjobscheduler com.zc.bakamitai <job id>`), after a `v2` revision, or after removing and undoing the bookmark.
- [x] Two shows released in the same slot produce two notifications under one group summary.
- [x] With notifications disabled in system settings, no SubsPlease requests happen during a run (Ktor log in a debug build) and bookmarks still work.
- [ ] After a reboot, the job is still scheduled (`dumpsys jobscheduler`).
- [x] A fresh install writes `databases/bookmarks.db` and no `files/datastore/bookmarks.json` (`adb shell run-as com.zc.bakamitai ls databases files`).

---

## 11. Open questions

**Needs the user:** Q1 (if you prefer Room 3), Q3 (one-line approval), Q16 (skill changes). The others have a recommendation that the plan already follows.

| # | Needs user | Question | Recommendation |
|---|---|---|---|
| Q1 | **Decided 2026-11-19: Room 2.8.5** (user approved the plan as written) | Room 2.8.5 (`androidx.room`) or Room 3.0.3 (`androidx.room3`, stable since July 2026: KSP-only, coroutine-only, Kotlin codegen, `SQLiteDriver`)? | **Room 2.8.5.** WorkManager already ships Room 2.x, so the app keeps one Room/SQLite stack and the mature `room-testing` tooling. Room 3 would add a second stack and a driver choice for one table. Revisit when WorkManager moves to Room 3. |
| Q2 | No | KSP 2.3.14 or 2.3.12? | **2.3.14** (fixes 2.3.13's regression; Kotlin 2.4 and built-in-Kotlin fixes since 2.3.10). 2.3.12 is the fallback in step 2. |
| Q3 | **Decided 2026-11-19: approved** by the user | Approve `io.insert-koin:koin-androidx-workmanager` (koin-bom 4.2.2)? | **Approve** with WorkManager: same Koin release train; the compiler plugin's `@KoinWorker` requires it; without it workers need `KoinComponent`, which is banned. |
| Q4 | No | `androidx.work:work-testing` for androidTest? | **Yes**, test-only, part of the WorkManager approval. |
| Q5 | No | Which tab hosts Details from a notification? | **Home** (the start route): Back lands where the new episode is listed, and the app exits through Home. **Alternative:** the Bookmarks tab (Back → Bookmarks → Home). |
| Q6 | No | What counts as the "first bookmark" for the permission ask? | **An add that makes the count go from 0 to 1** (no stored flag). Android itself stops showing the dialog after two denials. **Alternative:** once per install (a stored flag). |
| Q7 | No | Sweep with the empty search only, or Home's empty search + `f=latest` p=1? | **Empty search, `f=latest` only on failure**: on 2026-11-19 `f=latest` was 3 days behind and added nothing; one request per check instead of two. |
| Q8 | No | Retry ladder and give-up time? | **10 min, 30 min, 1 h, 2 h, 4 h, 8 h, 16 h, 24 h**, then silent until the next sweep sees it. Tunable in `ReleaseCheckPolicy`. |
| Q9 | No | Notify for releases of bookmarked shows that aren't on the schedule (movies, specials) when a sweep happens to see them? Batches? | **Yes for movies and specials, never for batches** (a batch repeats episodes the user was already told about). |
| Q10 | No | Persist the sort order? | **Not yet**: in-memory until Settings brings a preferences store. |
| Q11 | No | "Added" date: English "Nov 18, 2026" or the device locale? | **English**, like Details (kotlinx-datetime has no locale formats; the UI is English-only). Locale-aware dates when the app is localized. |
| Q12 | No | Empty-state CTA destination? | **Schedule** (the design's target; only scheduled shows get alerts). **Alternative:** All Shows. |
| Q13 | No | Undo after removing from the grid? | **Yes** (M3 pattern for destructive actions; restores the date, restarts tracking). |
| Q14 | No | Details' Q3 (pre-fill the header from the bookmark) and Q15 (refresh a bookmark's stored title/cover when Details loads)? | **Defer both.** The stored values come from Details' page at bookmark time, which already prefers the page cover. |
| Q15 | No | Accept the ≤ 1 h false-positive window when `release_date` has no skew? | **Yes.** Revisit when api.md records the server fix; then `RELEASE_DATE_TOLERANCE` can shrink. |
| Q16 | **Decided 2026-11-19: yes**, as step 1 describes | Add the new folder kinds and naming (`data/local/` Room, `data/worker/`, domain `[Name]Scheduler` / `[Name]Notifier` ports, `presentation/notification/`, `[Name]Entity` / `Dao` / `Database`) to the skills? | **Yes** (step 1). Background work and notifications have no home in the current skills; the alternatives (calling them repositories, or putting text in `data`) break the existing rules. |
| Q17 | No | Remove the typed DataStore artifact? | **Yes**; keep `datastore-preferences` for Settings. |
| Q18 | No | Cover art as the notification's large icon? | **Not in v1.** The notifier is in `presentation`, so Coil could load it later; it adds a fetch to every run. |
| Q19 | No | A daily wake-up just to refresh slots? | **No.** Stale slots only delay alerts; runs and screen opens refresh them. |
| Q20 | No | Should tapping "ALERTS ON" do anything? | **Open the app's notification settings** (mute the channel there until Settings has a switch). |

---

## 12. Implementation notes

Implemented on 2026-11-19 (implementor). Deviations from the plan, with the reason:

- **Go/no-go (step 2): GO** with KSP **2.3.14**, Room **2.8.5**, WorkManager **2.12.0** under AGP 9.5.1 built-in Kotlin 2.4.20. No fallback was needed (neither KSP 2.3.12 nor `android.disallowKotlinSourceSets=false`); the schema is generated at `app/schemas/…/BookmarksDatabase/1.json`.
- **Docs left to the user:** steps 16–18 (api.md §7 exception and §3.3–§3.5 findings, context.md, CLAUDE.md) and the `structure` skill's §7 "Current repo state" paragraph (it still describes the temporary Bookmarks exception) were not edited, at the user's request. All other step-1 skill changes are in, including `library-management`'s current-state note.
- **§10 command:** WorkManager 2.12 schedules its jobs in the JobScheduler namespace `androidx.work.systemjobscheduler`, so forcing a run needs `adb shell cmd jobscheduler run -f -n androidx.work.systemjobscheduler com.zc.bakamitai <job id>`.
- **DAO shape (§6.3):** `updateAiring(slug, day, minute, zone, title, checkedAt)` has 6 parameters (Detekt `LongParameterList`), so slot and tracking writes are partial-entity `@Update(entity = BookmarkEntity::class)` lists: `updateAirings(List<BookmarkAiringRow>)` and `updateTrackings(List<BookmarkTrackingRow>)` (two small classes in `data/local/`). `isFirstBookmark` uses a `count()` query instead of `getAll().size`. The DAO has 10 functions.
- **Worker (§6.3):** it takes a new `ScheduleNextReleaseCheckUseCase` instead of the `ReleaseCheckScheduler` port, so it calls only use cases (the `architecture` rule written in step 1). After the `NonCancellable` save-then-notify it checks `ensureActive()`, so a run cancelled by a `REPLACE` schedules nothing (§5.3).
- **New-episode rule (§5.4), stricter than the pseudo-code:** a release is new only when it's after tracking (with the 1 h tolerance) **and** later than `lastSeen` with another key and not a lower number, also when `lastSeen` is set. Candidates are processed oldest first; `lastSeen` advances only with a release that passes the "later, other key, not lower" comparison (a new one or a silent baseline), not with "the newest candidate considered", so a late older episode can't lower the baseline and make a later revision look new.
- **Per-show fallback (§5.2–§5.3):** a pending show is searched when the newest releases don't reach back to `slot − 1 h` **and** hold nothing new for it. A failed or capped (not yet searched) per-show search never gives the slot up (`evaluate(…, coversPending = false)`).
- **Extra retries:** a failed weekly-schedule refresh while some bookmark's slot was never fetched schedules the next check in 1 h (otherwise a lone new bookmark has no due time and the chain ends); a failed Room read does the same.
- **`BookmarkedShow`** has `addedOn: LocalDate` (device zone), so the card mapper needs no time zone.
- **Undo keeps the slot:** `Bookmark.toEntity()` stores the bookmark's slot when it has one (marked never fetched, so the next check refreshes it), so a restored card doesn't flash "NOT AIRING".
- **Notifications:** per-show alerts use tag `new_episode` and the summary tag `new_episodes_summary` (id 0), so slug-hash ids can't collide with the summary. Posting is asynchronous, so the summary lists this check's alerts from their models plus the earlier ones still shown (their line is kept in the notification extras).
- **Header (§2.2, §6.5):** the sort chips are a horizontally scrolling `Row` inside the grid's margin, like Schedule's filter chips, instead of a `LazyRow` with its own padding; `BookmarksHeaderView` has no `contentPadding` parameter. The grid scrolls to the top only when the sort really changes (the last sort is saved), so coming back from Details keeps the position.
- **Card action label:** the card's custom TalkBack action uses the remove button's label, "Remove <title> from bookmarks" (§2.4), not "Remove from bookmarks" (§10).
- **Snackbars** replace the one shown (only the last removal can be undone).
- **Tests:** JVM tests can't build `SQLiteException` (android.jar stubs), so its mapping is tested in the instrumented `BookmarkRepositoryImplStorageErrorTest`; `FakeBookmarkDao` covers the rest. Instrumented test names are camelCase. `ReleaseAlertNotifierImplTest` runs in name order because its last case blocks the channel, which only a reinstall undoes. `GetReleaseAlertsEnabledUseCaseTest` and `ScheduleNextReleaseCheckUseCaseTest` were added.
- **Checks done differently:** step 12 used the slugs `sasaki-to-pii-chan-s2` and `tetsuryou-meet-with-tetsudou-musume` (known to exist) instead of `sousou-no-frieren`. Step 13's previews weren't rendered (Android Studio wasn't reachable); the components were checked on the emulator in dark and light instead. The on-device alert was produced by rewinding one bookmark's tracking columns in the debug build's database and forcing the job, not by waiting for a real slot.
- **Open questions** were settled as recommended and approved (Q1 Room 2.8.5, Q3 `koin-androidx-workmanager`, Q16 skills, the rest as recommended). The typed DataStore artifact is removed (Q17).
- **Acceptance (§10) not checked on a device:** the rail on tablets and 3 columns at medium width (compact 2 and landscape 4 were seen); "NOT AIRING" on a card (mapper-tested); scroll-to-top and scroll restoration (only two cards); removing from Details (adding was seen); airplane mode; TalkBack; an alert from a real slot release; the `v2` and remove-then-Undo dedup cases and two shows in one slot (unit/instrumented-tested only); no requests with notifications disabled (unit/instrumented-tested only); a reboot.

---

## 13. QA

Tested on 2026-11-19 (tester) on **emulator-5554**, Android 17 (API 37), 1080×2400 at 420 dpi, dark theme, time zone **Asia/Beirut** (UTC+2). Live data was compared with one `f=schedule` and one `f=search&s=` request in that zone. Alert scenarios were set up by rewinding `tracking_since` / `last_seen_*` / `resolved_through` in the debug build's `bookmarks.db` (pulled and pushed with `run-as`), then forcing the job with `cmd jobscheduler run -f -n androidx.work.systemjobscheduler com.zc.bakamitai <job id>`. No crash or ANR from the app in the crash buffer.

| # | Journey | Result |
|---|---|---|
| 1 | Bookmarks → card → Details → "Bookmarked" tapped → Back: the grid shows the empty state at once | PASSED |
| 2 | Remove from the grid → open the show from Schedule: Details says "Bookmark"; bookmark it again → Back → Bookmarks: the card is first again | PASSED |
| 3 | `pm clear` (permission reset) → first bookmark from Details shows the dialog → Don't allow → `details_alerts_denied` snackbar; second bookmark: no dialog; chip "ALERTS OFF" → tap → dialog → Allow → "ALERTS ON" | PASSED |
| 4 | Chips against the live weekly schedule: "TODAY 2:30 PM" / "TODAY 6:00 PM" (tertiary), "SAT 4:00 AM", "FRI 6:00 PM", "WED 3:00 PM" (default), "NOT AIRING" (muted) for *7th Time Loop* bookmarked from All Shows; Release time puts it last, Title (A–Z) first | PASSED |
| 5 | Two shows in one slot (*Sasaki to Pii-chan S2*, *Sekai Saikyou no Majo*, Wed 3:00 PM): one `f=search&s=` request, two "Episode 07 is out" notifications under an InboxStyle summary "2 new episodes"; tapping a child opens its Details | PASSED |
| 6 | Airplane mode during a forced run: both feeds fail (`UnknownHostException`), no crash, nothing saved or posted, next run scheduled; airplane off + forced run → both notifications | PASSED |
| 7 | App notifications off in system settings: chip "ALERTS OFF"; a forced run with an awaited episode made **no** request, resolved it silently and rescheduled; notifications back on → "ALERTS ON" | PASSED |
| 8 | 11 bookmarks: scroll, open Details from mid-grid, Back keeps the scroll position | PASSED |
| 9 | Reselecting the Bookmarks tab scrolls the grid to the top | FAILED: not implemented (no tab does it), not in the spec; open question 2 |
| 10 | Medium (`wm size 1800x2400`): rail, 3 columns; expanded (`2400x1800`): rail, 4 columns; scroll kept across the size change | PASSED |
| 11 | Font scale 1.3: no clipping or overlap | FAILED, fixed (issue 1), PASSED on re-test |
| 12 | Airplane mode: pull-to-refresh shows "Can't reach SubsPlease…" and keeps the grid; a cold start offline shows every card with cached covers | PASSED |
| 13 | Remove *Sasaki to Pii-chan S2* + Undo: original `added_at` kept, `tracking_since` = Undo time, the follow-up run set the baseline silently (no notification) | PASSED |
| 14 | Accessibility tree (`uiautomator dump`): each card one clickable node holding chip, "Added …", title; the remove button its own 48×48 dp node "Remove <title> from bookmarks"; alerts chip 92×48 dp with its on/off description; sort chips 48 dp tall | PASSED (TalkBack itself not run) |
| 15 | Regression: Home, Schedule (shared time chip), All Shows (search list, letters), Details (load, bookmark toggle) | PASSED |

**Issues**

| # | Type | Issue | Status |
|---|---|---|---|
| 1 | UI | At font scale 1.3 the airing chip ran under the remove button ("TODAY 2:30 PM" on a 2-column card; a 360 dp phone at 1.0 with "TODAY 12:30 PM" is just as tight). Cause: the chip was aligned top-start in the poster `Box` with only `spacing.sm` padding, so nothing kept it out of the top-end button, and `TimeChipView`'s one-line label clipped instead of ellipsizing. | **Fixed:** `feature/bookmarks/presentation/component/bookmarkcard/BookmarkCardView.kt` gives the chip an end padding of the button's 48 dp touch target (`RemoveTouchTargetSize`); `common/ui/component/timechip/TimeChipView.kt` ellipsizes the label (Schedule unaffected: its chip never overflows). UI-only, no unit test possible. At 1.3 on this phone the chip now reads "TODAY 2:30…" (TalkBack reads the whole label). |
| 2 | Spec question | Tab reselect scroll-to-top: neither context.md §2 nor this plan asks for it, and Home, Schedule and All Shows don't do it either (reselect only pops Details, 4-details.md §5.7). | Open: an app-wide navigation decision. |
| 3 | Design question | Issue 1's fix truncates the time at large font scales; wrapping the pill to two lines would keep it. | Open. |
| 4 | Observation | While alerts are off, runs make no requests, so `last_seen` doesn't move; once alerts are back on, the first sweep notifies an episode released while they were off (if still among the 20 newest). Matches §5.3 ("late, never wrong"). | By design. |

**Not covered:** an alert from a real slot release (FX Senshi's 2:30 PM check was still pending); the `v2` revision case (unit-tested only); a reboot; light theme, rotation and the deep-link back stack (checked by the implementor); TalkBack running live (custom "Remove" action not exercised); a placeholder cover (every test show had a cover); scroll-to-top after a sort change; the "TODAY" → "THU" chip change after the slot passes.
