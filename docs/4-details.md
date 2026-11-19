# 4. Details

> **Status:** Implemented · **Design:** [Google Stitch project `5201581584167289517`, screen `c203e52384a648a79e16b7302d501af8` ("Anime Details & Downloads")](https://stitch.google.com/projects/5201581584167289517?node-id=c203e52384a648a79e16b7302d501af8) · **Planned:** 2026-11-19
> **Spec:** [context.md §2](context.md#2-navigation-shell), [§3.5](context.md#35-details), [§4.4](context.md#44-details), [§4.5](context.md#45-formatting-rules-all-screens), [§5](context.md#5-local-persistence), [§6](context.md#6-suggested-feature-breakdown), [§7](context.md#7-open-decisions) · **API:** [api.md §1](api.md#1-basics), [§3.5](api.md#35-show-episodes), [§3.6](api.md#36-resolutions), [§4.2](api.md#42-show-page), [§5](api.md#5-xdcc), [§7](api.md#7-client-guidelines)
> **Depends on:** [1-home.md](1-home.md), [2-schedule.md](2-schedule.md), [3-shows.md](3-shows.md) (all implemented): the navigation shell (`Navigator`, `NavigationState`, `AppEntryProvider`), `Show` / `ShowSlug`, `CoverImageView` (`POSTER_ASPECT_RATIO`), the skeleton pieces, `ErrorView`, `EmptyStateView`, `safeApiCall`, `DispatcherProvider`, `TimeZoneProvider`, the `Clock` provider, `toCoverUrl()`, `toTimeLabel()`, the shared `HttpClient`, and Ksoup behind a `[Name]HtmlParser`.

**Design source.** A Google Stitch project, not Figma or Sketch. Only one screen exists: **`c203e52384a648a79e16b7302d501af8`** in project **`5201581584167289517`**, a 390×1487 dark-mode frame, captured as a render (`screen.jpg`, 743×2832) and a condensed copy of its HTML/Tailwind with Stitch's own prompt. Every "node" below names that screen plus the HTML section inside it: `<header>`, the hero (`relative w-full overflow-hidden`: backdrop, scrim, poster card, identity column, stats chips, genre pills), `#bookmark-btn`, the synopsis card (`#synopsis-paragraph`, `#synopsis-toggle`), the "Episodes & Downloads" section (its header, the episode/batch pill tabs, the 1080p / 720p / 480p resolution cards and their Magnet / Torrent / XDCC source rows) and `#action-toast`. Two renderer artifacts in the PNG: serif fallback fonts, and a **blank row** between the genre pills and the synopsis card, which is the bookmark button (it's in the HTML). Loading, error, partial-load, no-episodes, light and tablet states aren't in the design; they're proposed in [§2.9](#29-states-not-in-the-design-all-proposed).

**How design and spec are reconciled** (same rule as the earlier plans): `context.md` decides **behaviour and data**, the Stitch design decides **visual treatment**. Most of the mock's metadata is invented (rating, studio, genres, season, Japanese title, seeds, file sizes, codecs, IRC commands); anything the data can't back is left out, never invented. Every disagreement is in [§3](#3-design-vs-spec).

---

## 1. Summary

Details is the page of one show. It shows the show's **cover**, **title**, a **bookmark** toggle, a collapsible **synopsis**, and its **downloads**: batches (when the show has any) above a newest-first list of episodes. Each episode row expands to three resolution slots (1080p, 720p, SD), each with **Magnet**, **Torrent** and **XDCC** sources; a resolution the episode doesn't have is shown disabled. The top bar has **back**, **Share** and **Open in web**.

Data comes in two sequential steps (context.md §4.4): the show page HTML (title, synopsis, cover, `sid`), then `f=show&sid=` JSON (episodes and batches). The header renders as soon as the page is parsed while the downloads section is still a skeleton. Both are cached in memory for 10 minutes per show; pull-to-refresh reloads them.

**Entry points:** a tap on any card or row in **Home** (upcoming carousel, latest releases), **Schedule** (grid) and **All Shows** (rows), wired by this plan. Bookmarks will open it too once its screen exists. Details is the app's **first pushed destination**: it gets a back arrow, hides the bottom bar on phones and keeps the rail on larger windows.

**In scope:**
- the `details` feature (`feature/details/`, all four layers)
- the **bookmarks data and domain layers** Details needs (`feature/bookmarks/{data,domain,di}`: typed DataStore, `ObserveIsBookmarkedUseCase`, `ToggleBookmarkUseCase`), with no Bookmarks screen ([§5.3](#53-featurebookmarks-data-domain-di-no-screen-yet))
- navigation: `DetailsNavKey`, the shared `FullScreenNavKey` marker, hiding the bottom bar for it, two small `Navigator` fixes ([§5.7](#57-navigation))
- wiring `onOpenDetails` in Home, Schedule and All Shows, which makes their cards and rows clickable (ripples; chevrons on All Shows rows) ([§5.9](#59-callers-home-schedule-all-shows))
- small moves to `common/` for the second user: the `release_date` parser and the centered content width ([§5.1](#51-shared-code))
- doc updates for the live findings in [§4.2](#42-api-gaps-found-live)

**Out of scope, with hooks left for later plans:**
- **The Bookmarks screen** and its tab: the Bookmarks plan adds `presentation/` on top of this plan's data and domain ([§6](#6-dependencies)).
- **Settings:** times stay 12-hour; Details has no settings action (it isn't a top-level screen).
- **Custom Tabs:** links use a plain `ACTION_VIEW` ([Q4](#10-open-questions)); `androidx.browser` isn't approved.
- **A two-pane layout on expanded windows** ([Q10](#10-open-questions)), **deep links** to `subsplease.org/shows/<slug>/` ([Q21](#10-open-questions)), **"Copy IRC command"** (api.md §5).
- **Design extras without data:** see [§3](#3-design-vs-spec).

---

## 2. Design breakdown

### 2.0 Token mapping

[1-home.md §2.0](1-home.md#20-token-mapping) applies unchanged; **no new theme work is needed**. Extra tokens this screen uses:

| Design (Tailwind/HTML) | Compose |
|---|---|
| `<header>` `bg-surface/80 backdrop-blur-xl`, `h-16` | M3 small `TopAppBar`, `topAppBarColors(containerColor = surface, scrolledContainerColor = surfaceContainer)` (the shared top bar's colors), `pinnedScrollBehavior`. No blur ([§3](#3-design-vs-spec) row 32). |
| Hero `bg-surface-container-lowest` | `colorScheme.surfaceContainerLowest` |
| Backdrop `blur-2xl scale-110 opacity-30` | `CoverImageView` with `Modifier.blur(BackdropBlur = 40.dp)` and `graphicsLayer { alpha = BACKDROP_ALPHA (0.3f); scaleX = scaleY = BACKDROP_SCALE (1.1f) }`, **API 31+ only** ([§2.2](#22-hero)) |
| Scrim `bg-gradient-to-b from-surface/40 via-surface/80 to-surface` | `Brush.verticalGradient(surface.copy(alpha = 0.4f), surface.copy(alpha = 0.8f), surface)` (alphas as `private const`s) |
| Poster `w-28 sm:w-32 aspect-[3/4.2] rounded-DEFAULT` | `PosterWidth` 112 dp compact, 128 dp from medium; `POSTER_ASPECT_RATIO` (already `3f / 4.2f` in `CoverImageView.kt`); `shapes.medium` (16 dp) |
| Title `text-headline-md font-extrabold` | `typography.headlineMedium` (Bold 22 sp) |
| `#bookmark-btn` `h-11 rounded-full`; on `bg-primary text-on-primary`; off `bg-surface-container text-on-surface` | `Button`, 48 dp (DESIGN.md filled button), `CircleShape`; on `primary` / `onPrimary`; off `surfaceContainerHigh` / `onSurface` (one tone above the design so it stays visible on the scrim, All Shows' search-field lesson) |
| Cards `rounded-DEFAULT bg-surface-container-low p-space-md` | `Surface(shape = shapes.medium, color = surfaceContainerLow)`, `spacing.md` padding |
| Section titles `text-headline-sm`, icons `auto_stories` (secondary), `download_for_offline` (primary) | `typography.headlineSmall`; `Icons.Outlined.AutoStories` 20 dp `secondary`, `Icons.Outlined.DownloadForOffline` 20 dp `primary` |
| Synopsis `body-md text-on-surface-variant line-clamp-3` | `bodyMedium` / `onSurfaceVariant`, `maxLines = 3` collapsed |
| "Read more" `label-md text-secondary font-bold` + `expand_more` | `TextButton` with `contentColor = secondary`, `labelLarge` (DESIGN.md label-lg, 13 sp bold: 11 sp is small for a touch label), `Icons.Outlined.ExpandMore` 16 dp, rotated 180° when expanded |
| Resolution pills `1080p FHD` `bg-primary-container text-on-primary-container`; `720p HD` `bg-secondary-container text-on-secondary-container`; `480p SD` `bg-surface-container-highest text-on-surface`, `px-2.5 py-0.5 rounded-full` | the same roles, `CircleShape`, 10 dp × 2 dp padding, `labelMedium` |
| Source actions: Launch `bg-tertiary-container text-on-tertiary-container`; Save `bg-secondary-container text-on-secondary-container`; Copy `bg-surface-container-highest text-primary`, all `rounded-full` | Magnet / Torrent / XDCC `Button`s with these roles; `Icons.Outlined.Bolt`, `Icons.Outlined.FileDownload`, `Icons.Outlined.Terminal` at 18 dp |
| Inner tiles `bg-surface-container` | dropped: resolutions are separated by `HorizontalDivider(color = surfaceContainerHigh)` inside the episode card ([§2.7](#27-resolution-row-and-sources)) |
| `shadow-*`, glows, `animate-pulse` dots | dropped (tonal layering, DESIGN.md "Elevation & Depth") |
| Material Symbols `arrow_back`, `share`, `open_in_new`, `bookmark` | `Icons.AutoMirrored.Filled.ArrowBack`, `Icons.Outlined.Share`, `Icons.AutoMirrored.Outlined.OpenInNew`, `Icons.Filled.Bookmark` / `Icons.Outlined.BookmarkBorder`. Extra: `Icons.Outlined.VideoLibrary` (no episodes). All present in `material-icons-core`/`-extended` 1.7.8 (checked in the Gradle cache). |

Dimensions as private `Dp` constants in their component files: `PosterWidth` / `PosterWidthMedium`, `BackdropBlur`, `BookmarkButtonHeight = 48.dp`, `EpisodeHeaderMinHeight = 56.dp`, `SourceIconSize = 18.dp`, `SectionIconSize = 20.dp`, `PillHorizontalPadding = 10.dp`, `PillVerticalPadding = 2.dp`. Constants: `TITLE_MAX_LINES = 4`, `SYNOPSIS_COLLAPSED_LINES = 3`, `DISABLED_ALPHA = 0.38f`.

### 2.1 Top app bar and navigation

Node: `<header>`. No `<nav>`: Stitch notes this is a pushed screen.

- **Layout:** small `TopAppBar`: navigation icon (back), title, actions (Share, Open in web). It's the screen's own bar, not the shared `TopLevelAppBarView` (that one is for tabs).
- **Title:** empty while the hero is on screen; the **show title** fades in once the hero scrolls under the bar (`val showTitle by remember(listState) { derivedStateOf { listState.firstVisibleItemIndex > 0 } }`, `AnimatedVisibility(fadeIn/fadeOut)`), `titleLarge`, one line, ellipsized. The design's "Anime Details" / "BakaMitai Tracker" is dropped ([§3](#3-design-vs-spec) row 1).
- **Actions:** `IconButton(Icons.Outlined.Share)` → `ShareClicked`; `IconButton(Icons.AutoMirrored.Outlined.OpenInNew)` → `OpenInWebClicked`. Shown only once the header is loaded (both need the title / URL); hidden during the skeleton and the full-screen error. The bookmark is **not** here: it's the hero's button ([§3](#3-design-vs-spec) row 2, [Q2](#10-open-questions)).
- **Navigation suite:**
  - **Compact** (the suite is a `NavigationBar`, which includes phones in landscape): the bar **slides away** while Details is on top (`FullScreenNavKey`, [§5.7](#57-navigation)).
  - **Medium / expanded** (rail): the rail **stays**, with Details' tab still selected. Reselecting that tab pops back to its root ([§5.7](#57-navigation), [Q9](#10-open-questions)).
- **Interactions:** back arrow → `BackClicked` → `NavigateBack` → `navigator.goBack()`; system back and predictive back go through `NavDisplay`'s `onBack` (same function). `NavDisplay`'s default transitions are kept.
- **Theming:** title `onSurface`; icons `onSurfaceVariant`.
- **Accessibility:** `cd_navigate_back` "Navigate back", `cd_details_share` "Share", `cd_details_open_in_web` "Open on SubsPlease"; 48 dp targets (`IconButton`).

### 2.2 Hero

Nodes: the hero container, its backdrop and scrim, the poster card, the identity column.

- **Layout:** the first `LazyColumn` item, full width of the pane. Layers bottom to top: `surfaceContainerLowest`, the backdrop image, the scrim, then the content column with the list's horizontal padding `h` ([§2.9](#29-states-not-in-the-design-all-proposed)), `spacing.lg` top, `spacing.md` bottom, `spacedBy(spacing.md)`:
  1. **Media row** (`Row`, `Alignment.Top`, `spacedBy(spacing.md)`): the poster, then the title (`weight(1f)`, `spacing.xs` top padding).
  2. **Bookmark button**, full width ([§2.3](#23-bookmark-button)).
- **Components:** `component/detailsheader/DetailsHeaderView.kt` (+ `DetailsHeaderSkeletonView`).
  - Poster: `CoverImageView(url, Modifier.width(posterWidth).aspectRatio(POSTER_ASPECT_RATIO).clip(shapes.medium))`. It already shows our placeholder when `url == null`.
  - Backdrop: `CoverImageView(url, Modifier.matchParentSize().graphicsLayer {…}.blur(BackdropBlur))`, drawn only when `url != null` **and** `Build.VERSION.SDK_INT >= Build.VERSION_CODES.S`: `Modifier.blur` is a no-op before API 31, and a sharp image under the scrim is noise ([Q17](#10-open-questions)). Purely decorative (`contentDescription = null` already).
  - Title: `Text(headlineMedium, onSurface, maxLines = TITLE_MAX_LINES, overflow = Ellipsis)`.
- **Content:** `DetailsHeaderUIModel.title` (the page's `h1`, entity-decoded, so "A3! Season Autumn & Winter", "BanG Dream! It’s MyGO!!!!!", "Dark Machine – The Animation"), `coverUrl` (the page's cover, absolute or `null`).
- **Omitted** (no data, [§3](#3-design-vs-spec) rows 5–10): the "FINISHED" badge, "WINTER 2024 • SIMULCAST", the Japanese title, the rating / "28 Eps" / studio chips, the genre pills and "Top Airing #1".
- **States:** content; skeleton (`DetailsHeaderSkeletonView`: no backdrop, a poster-sized `SkeletonBlock(shapes.medium)`, two `SkeletonTextLine(headlineMedium)` at 1.0 / 0.6 width, then `BookmarkButtonSkeletonView`). The header is never in an error state on its own: if the page fails there is no header ([§2.9](#29-states-not-in-the-design-all-proposed)).
- **Interactions:** none besides the bookmark button. The poster isn't zoomable ([Q12](#10-open-questions) covers the extras we skip).
- **Theming:** the scrim fades to `surface`, so the hero blends into the list in light and dark.
- **Accessibility:** the title has `semantics { heading() }`. The poster is decorative (the title names the show).

### 2.3 Bookmark button

Node: `#bookmark-btn` (blank in the render).

- **Layout:** full width of the hero content, 48 dp high, pill.
- **Components:** `component/detailsheader/component/bookmarkbutton/BookmarkButtonView.kt`: `BookmarkButtonView(isBookmarked: Boolean, onClick: () -> Unit, modifier)` + `BookmarkButtonSkeletonView(modifier)` (a full-width 48 dp `CircleShape` block). An M3 `Button` whose colors switch (`ButtonDefaults.buttonColors(...)`); M3 1.4.0 has no stable `ToggleButton` (only its tokens).
  - Bookmarked: `primary` / `onPrimary`, `Icons.Filled.Bookmark`, "Bookmarked".
  - Not bookmarked: `surfaceContainerHigh` / `onSurface`, `Icons.Outlined.BookmarkBorder`, "Bookmark".
  - Icon 20 dp, `spacing.sm` gap, label `labelLarge`, one line.
- **Content:** `DetailsState.isBookmarked`, from the bookmarks store (a `Flow`, so it also updates if another screen changes it later).
- **Interactions:** tap → `BookmarkClicked` → `ToggleBookmarkUseCase(show)` saves `{slug, title, coverUrl, addedAt = clock.now()}` or removes it. No confirmation snackbar ([§3](#3-design-vs-spec) row 3): the button changes. A failed write shows the generic error snackbar.
- **Accessibility:** `Modifier.semantics { stateDescription = …; onClick(label = …, action = null) }` with `cd_details_bookmarked` / `cd_details_not_bookmarked` and `cd_details_add_bookmark` "Add to bookmarks" / `cd_details_remove_bookmark` "Remove from bookmarks" (All Shows' chip pattern for action labels).

### 2.4 Synopsis card

Nodes: the synopsis card, `#synopsis-paragraph`, `#synopsis-toggle`.

- **Layout:** a list item, `spacing.md` below the hero, the list's horizontal padding. Inside (`spacing.md` padding): a header row (icon, `spacing.sm`, "Synopsis"), `spacing.sm`, the text, then the toggle when needed.
- **Components:** `component/synopsiscard/SynopsisCardView.kt`: `SynopsisCardView(synopsis: UiText?, isExpanded: Boolean, onToggle: () -> Unit, modifier)` + `SynopsisCardSkeletonView(modifier)` (the card's shape and color with a `SkeletonTextLine(headlineSmall)` at 0.35, three `bodyMedium` lines at 1.0 / 1.0 / 0.7 and a `labelLarge` line at 0.25). The card uses `Modifier.animateContentSize()`.
- **Content:** the page's synopsis paragraphs, joined with a blank line (`"\n\n"`), entity-decoded, tags stripped. `null` → the card shows `details_synopsis_missing` "No synopsis yet." in `onSurfaceVariant`, with no toggle. SubsPlease's placeholder `<p><i>Synopsis will be added later</i></p>` counts as missing ([§4.2](#42-api-gaps-found-live) D6).
- **States:**
  - Collapsed: `maxLines = 3`, ellipsized. Whether the text overflows is visual: `var hasOverflow by remember(synopsis) { mutableStateOf(false) }` set from `onTextLayout { if (!isExpanded) hasOverflow = it.hasVisualOverflow }`. The toggle shows when `hasOverflow || isExpanded`.
  - Expanded: no line limit, "Read less" with the icon rotated.
  - `isExpanded` is in `DetailsState` (survives rotation); it resets on a new ViewModel.
- **Omitted:** "Source: Official Translation" ([§3](#3-design-vs-spec) row 12).
- **Interactions:** the toggle (`TextButton`) → `SynopsisToggled`.
- **Accessibility:** "Synopsis" is a heading. The toggle reads "Read more" / "Read less" (48 dp `TextButton`). The icon is decorative.

### 2.5 Downloads header and groups

Nodes: the "Episodes & Downloads" header, "Direct Raws", the description line, the episode/batch pill tabs.

- **Section header:** `component/downloadsheader/DownloadsHeaderView.kt`: `DownloadsHeaderView(countLabel: UiText?, modifier)`. A `Row`, `spacing.lg` above it and `spacing.md` below: `Icons.Outlined.DownloadForOffline` (20 dp, `primary`), `spacing.sm`, "Episodes & Downloads" (`headlineSmall`, `heading()`, `weight(1f)`), then `countLabel` ("20 EPISODES", plural `details_episode_count`, `labelSmall`, `secondary`) where the design has "DIRECT RAWS". Static chrome: it renders for real during loading (without the count). No skeleton.
- **Groups:** only when the show has batches. `component/episodegroupheader/EpisodeGroupHeaderView.kt`: `EpisodeGroupHeaderView(label: UiText, modifier)`: `labelMedium`, `onSurfaceVariant`, uppercase resources ("BATCHES", "EPISODES"), `heading()`, `spacing.sm` vertical padding. Order: "BATCHES" then its rows, then "EPISODES" then its rows. Without batches there are no group headers: the section header already says what follows.
- **Episode navigation (decided, [Q1](#10-open-questions)):** the design's horizontal tabs ("Latest (EP 28)", "EP 27", …, "Batch Pack") are **replaced** by the spec's **expandable, newest-first list**. Tabs don't scale: One Piece and Detective Conan return 52 entries ([§4.2](#42-api-gaps-found-live) D1), and finding "EP 1150" in a strip of 52 chips is a scroll hunt, while a one-episode movie gets a lone tab. The list works for both: a movie is one row, already expanded; 52 rows scroll lazily and only expanded rows show links. The design's "Latest" default is kept as the **newest row expanded on first load**.
- **Omitted:** "Direct Raws" and "Verified release groups with multi-audio…" ([§3](#3-design-vs-spec) row 15).

### 2.6 Episode row

Nodes: the pill tabs (replaced) and the resolution cards (their content moves inside the row).

- **Layout:** each episode or batch is one `LazyColumn` item: a card (`Surface(shape = shapes.medium, color = surfaceContainerLow)`), `spacing.sm` between cards.
  - **Header** (always visible, `heightIn(min = EpisodeHeaderMinHeight)`, `spacing.md` horizontal / `spacing.sm` vertical padding, clickable over the card's full width with the card's shape): a `Column(weight(1f))` with the title and the release line, then `Icons.Outlined.ExpandMore` (24 dp, `onSurfaceVariant`), rotated 180° when expanded (`animateFloatAsState`, read in `graphicsLayer { rotationZ = … }`).
  - **Body** (`AnimatedVisibility(isExpanded, enter = expandVertically() + fadeIn(), exit = shrinkVertically() + fadeOut())`): `spacing.md` horizontal and bottom padding, the three resolution rows ([§2.7](#27-resolution-row-and-sources)) separated by `HorizontalDivider(color = surfaceContainerHigh)` with `spacing.sm` around each.
- **Components:** `component/episoderow/EpisodeRowView.kt`: `EpisodeRowView(model: EpisodeUIState, onToggle: () -> Unit, onSourceClick: (DownloadSource, String) -> Unit, modifier)` + `EpisodeRowSkeletonView(modifier)` (the card, a `titleMedium` line at 0.4, a `bodySmall` line at 0.6 and a 24 dp circle block where the chevron is).
- **Content** (`EpisodeUIState`, [§5.6](#56-featuredetailspresentation)):
  - **Title** (`titleMedium`, `onSurface`, up to 2 lines):
    - numeric `"20"` → "Episode 20"
    - revision `"1216v2"` → "Episode 1216 (v2)"
    - `"Movie"` (any case) → "Movie"
    - a batch range `"01-28"` → "Episodes 01–28"
    - anything else as-is: "Egghead SP11", "The Counterfeit Case of Ultra 30"
  - **Release line** (`bodySmall`, `onSurfaceVariant`): `details_episode_released` "%1$s · %2$s" with the date "Nov 17, 2026" and the time "12:38 PM" (the shared 12-hour `toTimeLabel`), in the device zone. `release_date` is shown as reported, an hour early in winter (api.md §3.5).
- **States:** collapsed / expanded; every row starts collapsed, including the newest (user, 2026-11-19). Loading is the section skeleton ([§2.9](#29-states-not-in-the-design-all-proposed)).
- **Interactions:** tapping the header → `EpisodeToggled(key)`. Any number of rows can be open.
- **Accessibility:** the header is one merged node (`Modifier.clickable(onClickLabel = …, role = Role.Button)`) with `stateDescription` `cd_expanded` "Expanded" / `cd_collapsed` "Collapsed" and `onClickLabel` `cd_details_show_downloads` "Show downloads" / `cd_details_hide_downloads` "Hide downloads". The chevron is decorative.

### 2.7 Resolution row and sources

Nodes: the 1080p / 720p / 480p cards, their pills, codec lines, "SubsPlease" labels, and the Magnet / Torrent / XDCC source rows.

- **Layout:** `Column(spacedBy(spacing.sm))`:
  1. a `Row`: the resolution pill, then, when unavailable, `details_resolution_unavailable` "Not released" (`labelSmall`, `onSurfaceVariant`, `spacing.sm` start);
  2. a `FlowRow(horizontalArrangement = spacedBy(spacing.sm), verticalArrangement = spacedBy(spacing.xs))` of the three source buttons at their natural width. At font scale 1.0 they fit on one line of a 360 dp phone (~257 dp in a ~296 dp row); at large font scales the last one wraps instead of truncating.
- **Components:** `component/episoderow/component/resolutionrow/ResolutionRowView.kt`: `ResolutionRowView(model: ResolutionUIModel, episodeTitle: UiText, onSourceClick: (DownloadSource, String) -> Unit, modifier)`. A private `SourceButton` in the same file: M3 `Button(onClick, enabled = url != null, colors = buttonColors(container, content), contentPadding = PaddingValues(horizontal = spacing.sm + spacing.xs, vertical = spacing.xs))` with an 18 dp icon, `spacing.xs`, and a one-line `labelMedium` label. `Button` keeps the 48 dp touch target. No skeleton: the rows only exist inside a loaded, expanded episode.
- **Content:** always **three slots, in this order: 1080p, 720p, SD** (the design's order, [§3](#3-design-vs-spec) row 18):
  - Pill label: "1080p FHD", "720p HD", or the SD's real height ("480p SD", "360p SD", "540p SD"); a missing SD slot reads just "SD", like the site.
  - Pill colors by `ResolutionTone`: FHD `primaryContainer` / `onPrimaryContainer`, HD `secondaryContainer` / `onSecondaryContainer`, SD `surfaceContainerHighest` / `onSurface`. Unavailable: `surfaceContainerHighest` and `onSurface` at `DISABLED_ALPHA`.
  - Buttons: **Magnet** (`tertiaryContainer` / `onTertiaryContainer`, `Bolt`), **Torrent** (`secondaryContainer` / `onSecondaryContainer`, `FileDownload`), **XDCC** (`surfaceContainerHighest` / `primary`, `Terminal`). Each is enabled when its link exists; a missing resolution shows all three **disabled** (spec), with M3's disabled colors.
- **Omitted** ([§3](#3-design-vs-spec) rows 20–24): codec lines, the "SubsPlease" label, seeds and pulse dots, file and payload sizes, `/msg` IRC commands, the "Launch / Save / Copy" wording.
- **Interactions** (the VM turns them into effects, [§2.8](#28-opening-links-sharing-and-snackbars)):
  - Magnet → `OpenTorrentLink(magnetUri)`
  - Torrent → `OpenTorrentLink(torrentUrl)` (the nyaa.si `.torrent` URL)
  - XDCC → `OpenWebLink("https://subsplease.org/xdcc/?search=<xdcc>")`, SubsPlease's XDCC search page (api.md §5), not an IRC command
- **Accessibility:** each button's `contentDescription` is `cd_details_source` "%1$s, %2$s, %3$s" with the source name, the pill label and the episode title ("Magnet, 1080p FHD, Episode 20"), so the nine buttons of an episode are distinguishable. Disabled buttons are announced as disabled; an unavailable slot also reads "Not released".

### 2.8 Opening links, sharing and snackbars

Node: `#action-toast` and the page script.

All of this happens in `DetailsRoute` (it needs a `Context`), in response to effects. Snackbars are launched in a `rememberCoroutineScope()` so a 10-second snackbar never blocks later effects such as `NavigateBack`.

- **Open a link:** a private `Context.openLink(url): Boolean` in `DetailsRoute.kt`: `startActivity(Intent(Intent.ACTION_VIEW, url.toUri()))` inside `try`, returning `false` on `ActivityNotFoundException`. No `<queries>` element is needed (it doesn't query). No chooser: the system already asks when several torrent apps match.
  - `OpenTorrentLink` failure → snackbar `details_no_torrent_app` "No torrent app found", action `details_copy_link` "Copy link", `SnackbarDuration.Long` (context.md §3.5).
  - `OpenWebLink` failure → snackbar `details_no_app_for_link` "No app can open this link", same action.
  - Copy: `LocalClipboard.current.setClipEntry(ClipEntry(ClipData.newPlainText(clipLabel, url)))`. On API 32 and below, follow with `details_link_copied` "Link copied"; API 33+ shows its own clipboard confirmation.
- **Share:** `Intent(ACTION_SEND)` with `type = "text/plain"`, `EXTRA_TEXT` = `details_share_text` "%1$s\n%2$s" (title, then `https://subsplease.org/shows/<slug>/`), `EXTRA_TITLE` = the title, through `Intent.createChooser(send, null)`. The mock's "Link copied" toast is not used.
- **Open in web:** `OpenWebLink(overview.webUrl)` (`https://subsplease.org/shows/<slug>/`), not MyAnimeList.
- **Errors:** `ShowError` → plain snackbar with the error message (refresh failed, bookmark write failed).
- The design's other toasts ("Added to anime bookmarks", "Launched torrent client", "Loaded <tab>", ".torrent File Downloaded") are dropped: the outcome is visible.

### 2.9 States not in the design (all proposed)

| State | Proposal |
|---|---|
| **First load** (`state.isLoading`: no header, no error) | A private `DetailsSkeleton` fills the body under the real top bar (back arrow only), in **one** `SkeletonView` around the shared `DetailsList` helper (`userScrollEnabled = false`): `DetailsHeaderSkeletonView`, `SynopsisCardSkeletonView`, the real `DownloadsHeaderView(countLabel = null)` and 4 `EpisodeRowSkeletonView`s. |
| **Partial load** (header shown, downloads loading) | The header and synopsis are real; under the downloads header, **one** item holds its own `SkeletonView` with 4 `EpisodeRowSkeletonView`s (`best-practices` §5.3, partial loads). |
| **Page error, nothing shown** | `ErrorView(model = state.error, onRetry = { onIntent(Retry) })` under the bar (back arrow only, no actions). No pull-to-refresh. `NotFound` (unknown or deleted slug, a 404, D8) reads "This page isn't available on SubsPlease anymore." with no Retry; a page without a title or `sid` is `InvalidData` ("Something went wrong", Retry). |
| **Downloads error, header shown** | In place of the rows: `ErrorView(model, onRetry = { onIntent(RetryEpisodes) }, Modifier.fillMaxWidth())` as one item. The header stays usable; pull-to-refresh works. |
| **No episodes and no batches** | `EmptyStateView(icon = Icons.Outlined.VideoLibrary, title = details_no_episodes_title "No episodes yet", message = details_no_episodes_message "No episodes available for this show yet.")` (the spec's text). Batches without episodes show the batches only, with no empty message. |
| **Listing capped** (52 entries, D1) | After the last episode, a centered note `details_episode_limit_note` "SubsPlease lists only the latest %d releases." (`bodySmall`, `onSurfaceVariant`, `spacing.md` padding). |
| **Refreshing** | `PullToRefreshBox` around the list once the header exists; the content stays interactive; expanded rows stay expanded. |
| **Refresh failed, content shown** | Content kept; `ShowError` snackbar. |
| **Light theme** | Same roles; `LightColorScheme` resolves them. The scrim fades to the light `surface`. |
| **Medium / expanded** | Rail visible. One column, at most **720 dp** wide and centered, with `h = max(margin, (maxWidth - MaxContentWidth) / 2)` (`margin` = `spacing.margin` compact, `spacing.marginTablet` from medium), the helper shared with All Shows ([§5.1](#51-shared-code)). The hero's backdrop spans the full pane; its content uses `h`. Poster 128 dp from medium. Two panes: [Q10](#10-open-questions). |
| **Rotation / process death** | The ViewModel survives rotation (expanded rows, synopsis state, scroll via `rememberLazyListState`). After process death the back stack restores `DetailsNavKey(slug)` and the screen reloads (the in-memory cache is gone). |

---

## 3. Design vs. spec

| # | Item | Design | docs/context.md | Recommendation |
|---|---|---|---|---|
| 1 | Top bar title | "Anime Details" + "BakaMitai Tracker" | n/a | **The show title, shown as soon as the page loads** (changed from "faded in once the hero scrolls away" at the user's request, 2026-11-19); no subtitle. |
| 2 | Top bar actions | Share, Open in web; bookmark is a full-width hero button | Share, Bookmark, Open in web, all in the top bar (§3.5) | **Follow the design's placement:** Share and Open in web in the bar, bookmark as the hero's button. Behaviour is the spec's (toggle, filled when bookmarked). [Q2](#10-open-questions). |
| 3 | Bookmark feedback | Toast "Added to / Removed from anime bookmarks" | Toggle | **No toast**: the button changes label, icon and color; TalkBack reads the new state. Errors show a snackbar. |
| 4 | Share | `navigator.share` or "Link copied" toast | `ACTION_SEND` with the title and `https://subsplease.org/shows/<slug>/` | **Spec.** |
| 5 | "FINISHED" badge | On the poster | n/a | **Omit.** No status data. |
| 6 | "WINTER 2024 • SIMULCAST" | Overline | n/a | **Omit.** No season data. |
| 7 | Japanese title | Under the title | n/a | **Omit.** The page has one title. |
| 8 | Rating, "28 Eps", studio chips | Stats row | n/a | **Omit.** No rating or studio; the episode count moves to the downloads header (row 15), where it's real. |
| 9 | Genre pills, "Top Airing #1" | Horizontal pills | n/a | **Omit.** No genres. |
| 10 | Blurred backdrop + scrim | Behind the hero | n/a | **Dropped** at the user's request (2026-11-19): the page has the same plain surface as Home, with no backdrop, scrim or tint. |
| 11 | Title length | One line, truncated ("Frieren: Beyond Journ…") | Title | **Up to 4 lines**, ellipsized: it's the only full title on screen. |
| 12 | Synopsis source label | "Source: Official Translation" | n/a | **Omit.** Invented. |
| 13 | Synopsis clamp | 3 lines, "Read more" / "Read less" | "collapsed to a few lines, with an expand/collapse toggle" | **Match.** Toggle only when the text overflows. |
| 14 | Missing synopsis | n/a | Synopsis can be missing (api.md §4.2) | **"No synopsis yet."** in the card. SubsPlease's "Synopsis will be added later" placeholder counts as missing (D6, [Q11](#10-open-questions)). |
| 15 | Downloads header extras | "DIRECT RAWS", "Verified release groups…" | n/a | **Omit both**; the trailing slot shows the real count ("20 EPISODES"). |
| 16 | Episode navigation | Horizontal tabs (Latest, EP 27, EP 26, Batch Pack) with 3 resolution cards for the selected one | Newest-first list; each episode expands to its links | **Spec's expandable list**, newest row expanded by default (the design's "Latest"). Scales from 1 entry (movie) to 52 ([§2.5](#25-downloads-header-and-groups), [Q1](#10-open-questions)). |
| 17 | Batches | A "Batch Pack (01-28)" tab, last | "show them in their own group above the episodes" | **Spec:** a "BATCHES" group above "EPISODES". |
| 18 | Resolution order | 1080p, 720p, 480p (top to bottom) | "SD → 720p → 1080p" (§4.4, the site's left-to-right order) | **Design: 1080p first.** It's visual order, and 1080p is what most people pick ([Q14](#10-open-questions)). context.md §4.4 is updated. |
| 19 | Resolution labels | "1080p FHD", "720p HD", "480p SD" | Grouped by resolution; SD shows its real value (api.md §3.6) | **Keep the design's pills**; SD uses the real height ("360p SD"), a missing SD reads "SD" (the site's behaviour). |
| 20 | Codec lines, "SubsPlease" label | "x265 • 10-bit • FLAC", … | n/a | **Omit.** Invented; SubsPlease is the only release group. |
| 21 | Source rows | One row per source with an icon circle, name, seeds / size / payload and an action pill | 3 sources per resolution | **Three compact buttons per resolution**, no meta (none of it is in the API). Nine full rows per episode would make one episode taller than a phone screen. |
| 22 | Magnet action | "Launch" | Opens `magnet:` with a torrent client; "No torrent app found" snackbar + "Copy link" | **Spec**, labelled "Magnet". |
| 23 | Torrent action | "Save" (".torrent File Downloaded" toast) | Opens the nyaa.si `.torrent` URL | **Spec**, labelled "Torrent"; the browser (or a torrent app) handles the download. |
| 24 | XDCC action | "Copy" an IRC `/msg … xdcc send #1428` command | Opens SubsPlease's XDCC search page (api.md §5) | **Spec**, labelled "XDCC". The IRC command needs `search.php` with a `Referer`; out of scope. |
| 25 | Missing resolution | Not shown | "show it as disabled rather than hiding it, matching the site" | **Spec:** disabled pill and buttons, "Not released". |
| 26 | Empty downloads | Not shown | "No episodes available for this show yet." | **Spec**, in `EmptyStateView`. |
| 27 | Pull-to-refresh | Not shown | Not noted for Details; api.md §7: the cache is "invalidated by pull-to-refresh" | **Add it** (All Shows Q8 precedent): the only way to see a new episode within the 10-minute cache. |
| 28 | Loading, errors, partial load | Not shown | Header shows while downloads load (§4.4) | **Proposed in [§2.9](#29-states-not-in-the-design-all-proposed)**: skeletons, never spinners. |
| 29 | Bottom navigation | None (pushed screen) | Tapping any anime opens Details (§2) | **Hide the bottom bar** on compact; **keep the rail** on medium / expanded ([Q9](#10-open-questions)). |
| 30 | Header from the caller | n/a | "Callers may also pass the title and cover they already have" (§3.5) | **Slug only** (`conventions` §5.1: keys carry ids). The header skeleton lasts one ~0.4 s page fetch, then the 10-minute cache ([Q3](#10-open-questions)). context.md §3.5 is updated. |
| 31 | Tablet | n/a | n/a | **One centered column, max 720 dp** (All Shows); two panes later ([Q10](#10-open-questions)). |
| 32 | Blur, shadows, glows, pulse dots | Throughout | n/a | **Dropped** (DESIGN.md "Elevation & Depth", earlier plans), except the backdrop image blur (row 10). |
| 33 | Poster and card corners | 16 px (`rounded-DEFAULT`) | n/a | **`shapes.medium`** (16 dp), as drawn. |

---

## 4. Data

### 4.1 Endpoints

| Call | Request | Fields used | Quirks to handle |
|---|---|---|---|
| Show page, [api.md §4.2](api.md#42-show-page) | `GET shows/<slug>/` (`https://subsplease.org/shows/<slug>/`). Trailing slash included (without it: a 301, D8). The slug is path-encoded. No `tz`. Body read with `bodyAsText()`. | `h1.entry-title` text; `div.series-syn > p` texts; `#secondary img.img-responsive` `src`; `table#show-release-table` `sid` | Entities in the title (D5); placeholder synopsis (D6); site-relative or placeholder cover (`toCoverUrl()`); 404 → `NotFound` (D8); no title or no numeric `sid` → `InvalidData`. Parse on `DispatcherProvider.default`. |
| Show episodes, [api.md §3.5](api.md#35-show-episodes) | `GET api/?f=show&tz=<device zone>&sid=<sid>` | `episode` and `batch` objects → each value's `release_date`, `episode`, `downloads[]` (`res`, `magnet`, `torrent`, `xdcc`) | `episode` / `batch` are an object **or `[]`** (api.md §3.5; unknown sid → `{"batch":[],"episode":[]}`, D10). `text/html` content type (handled by the shared client). `release_date` RFC 1123 with arbitrary per-show offsets, **parse to `Instant`**, an hour early (D2). Labels: `"1216v2"`, `"Egghead SP11"`, `"Movie"`, `"01-28"` (D3). `res` `360`/`480`/`540` → SD, `720` → HD, `1080` → FHD, others ignored (D4). At most 52 entries (D1). `xdcc` already URL-encoded: append as-is to `https://subsplease.org/xdcc/?search=`. `time` is display-only and not declared. |

### 4.2 API gaps found live

Checked on **2026-11-19, 07:49–08:05 UTC** with `curl` (User-Agent `BakaMitai/1.0 (Android)`, `tz=America/New_York` unless noted), about 25 requests, one second apart: show pages for `sora-wa-akai-kawa-no-hotori`, `sousou-no-frieren`, `one-piece`, `kaguya-sama-wa-kokurasetai-s3`, `a3-season-autumn-winter`, `detective-conan`, `kyouran-reijou-nia-liston`, `dark-machine-the-animation`, `paw-and-palaces`, `bakuten-movie`, `bang-dream-its-mygo` and an unknown slug; `f=show` for their `sid`s (1199, 620, 46, 353, 58, 48, 1274, 1293, 1292, 582, 585, 999999), once with `tz=Asia/Tokyo` and once without `tz`; the site's `/includes/main.js?v=1.0.16.6`; one nyaa.si page to time a release. Step 24 writes these into `api.md`.

- **D1. `f=show` returns at most 52 entries in `episode`, newest first.** One Piece (sid 46) and Detective Conan (sid 48) both return exactly 52 (One Piece: 1180 back to "Egghead SP8", June 2025; Conan: 1220 back to 1172). Older episodes aren't available from any endpoint; the site shows the same 52. Batches come separately in `batch`. The API order was newest-first by `release_date` in every response, but the app sorts anyway (context.md §4.4).
- **D2. `release_date` offsets are per show, not per server, and still ignore `tz`.** Seen: `-0500`/`-0400` (Sora, Frieren, One Piece, Conan…), `+0100`/`+0200` (Nia Liston), `+0800` (Paw and Palaces). `tz=Asia/Tokyo` returned the same `-0500` values. api.md §3.5's "server's zone (+0200 July, +0100 November)" is out of date. **Still one hour early:** Sora ep 20 is `Tue, 17 Nov 2026 12:38:52 -0500` (17:38:52Z) while its nyaa.si upload is 18:38:49Z.
- **D3. Episode labels:** plain numbers (`"07"`, `"1180"`); revisions as separate entries next to the original (`"1216v2"` after `"1216"`, `"1143v2"`); specials (`"Egghead SP11"`); named specials (`"The Counterfeit Case of Ultra 30"`, `"The Gold-Star Answer"`); `"Movie"` as the only entry of a movie (Bakuten!! Movie, sid 582); batch ranges `"01-28"`, `"01-13"`, `"01-12"` in `batch`. JSON keys always equal `"<show> - <episode>"`.
- **D4. Downloads:** every entry in 11 shows had all four keys (`res`, `magnet`, `torrent`, `xdcc`), non-empty, ordered 480 → 720 → 1080. `360` (A3 episodes) and `540` (A3 batch) appear live as the SD value. No entry missed a resolution today; the site still renders missing ones greyed (D12).
- **D5. The page title is entity-encoded, and can differ from the JSON titles:** `A3! Season Autumn &amp; Winter`, `BanG Dream! It&#8217;s MyGO!!!!!` ("It’s", curly; JSON "It's"), `Dark Machine &#8211; The Animation` ("–"; schedule and `/shows/` say "Dark Machine - The Animation"). One `h1` per page. Details shows the page's decoded title.
- **D6. Synopsis:** one to three `<p>` inside `div.series-syn` after `<h2>Synopsis</h2>`, raw UTF-8 punctuation (`—`, `"`), no entities seen. **Placeholder:** `paw-and-palaces` has `<p><i>Synopsis will be added later</i></p>`. No page lacked `div.series-syn`.
- **D7. Cover:** `#secondary img.img-responsive` with a site-relative `src` on every page. **The page can be newer than the schedule:** Nia Liston's `f=schedule` `image_url` is the "coming soon" placeholder, while its page has `/wp-content/uploads/2026/10/159223.jpg`.
- **D8. Status codes:** an unknown slug returns **404** (WordPress "Page not found", ~26 KB HTML) → `DomainError.NotFound`. `shows/<slug>` without the trailing slash returns **301** to `shows/<slug>/`; request the slash form.
- **D9. Sizes and timing:** show pages ~29 KB, `text/html; charset=UTF-8`, `cf-cache-status: DYNAMIC`, ~0.4–0.5 s. `f=show` from 3 KB (one movie) to ~159 KB for 52 entries (~10 KB gzipped), ~0.22 s.
- **D10. Empty shapes:** unknown `sid` → `{"batch":[],"episode":[]}` (confirmed); no `tz` → `200` with a 0-byte body (confirmed, api.md §1). No live show had zero episodes today (every scheduled show had at least one), so the empty case is tested with the unknown-sid shape.
- **D11. Batches without episodes:** the site prints "no episodes available" under the batch table in that case; Details shows the batches and no empty message ([§2.9](#29-states-not-in-the-design-all-proposed)).
- **D12. The site's renderer (`main.js`):** always three slots per entry, SD / 720p / 1080p; unreleased slots greyed (`badge-unrel`); the SD label is the real height when present, "SD" when missing; any `res` other than 360/480/540/720 goes in the 1080p slot (we ignore unknown values per api.md §3.6, [Q20](#10-open-questions)). Batches are titled "<show> — 01-28 (Batch)", movies just "<show>".

### 4.3 Derivation (domain)

`GetShowOverviewUseCase(slug, forceRefresh = false): Result<ShowOverview>`: the repository's overview, unchanged.

`GetShowEpisodesUseCase(slug, forceRefresh = false): Result<ShowEpisodes>`:
1. `zone = timeZoneProvider.current()`.
2. `repository.getEpisodes(slug, zone, forceRefresh)`; failure → that `DomainError`.
3. Sort `episodes` and `batches` by `releasedAt`, newest first (`sortedByDescending`, stable: ties keep API order).
4. Return them with `timeZone = zone` (labels are formatted in it) and `isTruncated` as the repository set it.

In the **data** mappers:
- **Episode:** dropped when the label is blank or `release_date` doesn't parse. Kept when it has no usable downloads (all three slots then show disabled).
- **Download:** `res` → `(Resolution, height)`: 360/480/540 → `SD`, 720 → `HD`, 1080 → `FHD`, anything else (or not a number) → dropped. `magnetUri` only if it starts with `magnet:`; `torrentUrl` only if it's `http(s)`; `xdccUrl = SUBSPLEASE_BASE_URL + "xdcc/?search=" + xdcc` when `xdcc` is non-blank (no re-encoding). Dropped when all three links are missing. Per episode, one download per `Resolution`: the first one in API order (sorting duplicates by height, highest first, for SD).
- **Truncation:** `isTruncated = raw episode entries >= SHOW_EPISODE_LIMIT (52)`, counted before invalid entries are dropped.
- **Overview:** `title` from `h1` (whitespace collapsed, trimmed; blank → `InvalidData`); `coverUrl = src.toCoverUrl()`; `synopsis` = non-blank paragraphs (whitespace collapsed) joined with `"\n\n"`, `null` when none or when the text is the placeholder `"Synopsis will be added later"` (ignoring case and a trailing period); `webUrl = SUBSPLEASE_BASE_URL + "shows/" + slug + "/"` (building SubsPlease URLs stays in `data`, api.md's header note). `sid`: trimmed, digits only, else `InvalidData`; it never leaves `data`.

Presentation (mappers in [§5.6](#56-featuredetailspresentation)) builds the labels, the three slots per episode and the default expansion.

### 4.4 Local data

**Bookmarks**, typed DataStore with a kotlinx.serialization JSON serializer (context.md §5), file `bookmarks.json` (`context.dataStoreFile`):

```json
{ "bookmarks": [ { "slug": "sora-wa-akai-kawa-no-hotori", "title": "Sora wa Akai Kawa no Hotori",
                   "cover_url": "https://subsplease.org/wp-content/uploads/2026/07/158138.jpg",
                   "added_at_epoch_millis": 1795000000000 } ] }
```

- `BookmarkListDTO(bookmarks: List<BookmarkDTO>)`, `BookmarkDTO(slug, title, coverUrl?, addedAtEpochMillis)`: context.md §5's `BookmarkEntity`, named per `conventions` (`[Name]DTO`; there is no `Entity` suffix). Unique by `slug`, insertion order (the Bookmarks screen sorts by `addedAt`). The root is an object so fields can be added later.
- **Serializer** (`BookmarkListSerializer`): `defaultValue = BookmarkListDTO(emptyList())`; a blank file decodes to the default; `SerializationException` / `IllegalArgumentException` → `CorruptionException`, and the DataStore's `ReplaceFileCorruptionHandler` resets to the default.
- **Reads:** `.catch { if (it is IOException) emit(default) else throw it }` (`best-practices` §4.2); duplicates by slug dropped on read.
- **Toggle** is one atomic `updateData { }` (remove the slug if present, else append), so a double tap can't race; it returns whether the slug is now saved.
- Covers are stored as **absolute** URLs (context.md §5), straight from `ShowOverview.show.coverUrl`.

### 4.5 Caching and refresh

[api.md §7](api.md#7-client-guidelines): show page plus episodes, per slug, about **10 minutes** in memory, invalidated by pull-to-refresh.

- `ShowDetailsRepositoryImpl` (`@Single`, process lifetime) keeps two maps behind one `Mutex`: `pages: Map<ShowSlug, CachedPage(overview, sid, fetchedAt)>` and `episodes: Map<ShowSlug, CachedEpisodes(episodes, batches, isTruncated, fetchedAt)>`, with `fetchedAt` from the injected `Clock` and `DETAILS_CACHE_TTL = 10.minutes`. Expired entries are pruned on every write, so the maps stay small.
- `getOverview(slug, forceRefresh)`: a fresh cached page unless `forceRefresh`, else fetch, parse (on `dispatchers.default`), map, cache.
- `getEpisodes(slug, timeZone, forceRefresh)`: fresh cached episodes unless `forceRefresh`. Otherwise it needs the `sid`: the cached page's, **even if expired** (a `sid` never changes), or else a page fetch, which also fills the page cache. Then `f=show`, mapped and cached. The result is built with the caller's `timeZone` on every call, so a zone change applies to cached data too (`f=show` content doesn't depend on `tz`, D2).
- `Mutex` isn't reentrant: both public functions lock once and call a private, unlocked `fetchPage(slug)`.
- Failures aren't cached and don't clear older entries.
- **When Details fetches:** once per ViewModel (`init { onIntent(Load) }`): page, then episodes, both cache-backed. `Retry` → both, cache-backed (only successes are cached). `RetryEpisodes` → episodes only. Pull-to-refresh → both with `forceRefresh = true`.
- **When it doesn't:** rotation, expanding rows, toggling the synopsis or the bookmark, returning to Details from another tab (the ViewModel lives with its entry). Reopening a show within 10 minutes renders without a request. No polling, no foreground refresh.

---

## 5. Architecture

Root: `app/src/main/java/com/zc/bakamitai/`. Test root: `app/src/test/java/com/zc/bakamitai/`.

### 5.1 Shared code

| Path | | Responsibility |
|---|---|---|
| `common/data/mapper/ReleaseDateDTOMapper.kt` | **new** | `fun String.toReleaseInstant(): Instant?`, moved from the private helper in Home's `ReleaseDTOMapper.kt` (RFC 1123 → `Instant`, `null` when unparseable or without an offset). Second user: Details' `EpisodeDTOMapper`. Same pattern as `ImageUrlDTOMapper.kt`'s `toCoverUrl()`. |
| `feature/home/data/mapper/ReleaseDTOMapper.kt` | modify | Imports `toReleaseInstant()`; the private copy is deleted. |
| `common/ui/util/ContentWidth.kt` | **new** | `val MaxContentWidth = 720.dp` and `fun centeredContentPadding(maxWidth: Dp, margin: Dp): Dp = max(margin, (maxWidth - MaxContentWidth) / 2)`, moved from All Shows' private `MaxContentWidth` / `AllShowsLayout.horizontalPadding`. |
| `feature/shows/presentation/AllShowsScreen.kt` | modify | `AllShowsLayout.horizontalPadding` delegates to `centeredContentPadding`; its private `MaxContentWidth` is deleted. All Shows looks the same. |
| `common/ui/navigation/FullScreenNavKey.kt` | **new** | `interface FullScreenNavKey`: a marker for keys whose screen hides the navigation **bar** (compact); the rail stays. Named in the `architecture` / `structure` skills; doesn't exist yet. |
| `common/ui/navigation/Navigator.kt` | modify | `navigate(key)`: (1) a non-top-level key equal to the selected stack's top isn't pushed again (a double tap opens Details once); (2) a top-level key equal to the selected `topLevelRoute` pops that stack to its root (reselecting the tab, reachable from the rail while Details is open). |
| `res/values/strings.xml` | modify | Shared section: `cd_navigate_back` "Navigate back", `cd_expanded` "Expanded", `cd_collapsed` "Collapsed". |

Reused as-is: `Show` / `ShowSlug`, `CoverImageView` / `POSTER_ASPECT_RATIO`, `SkeletonView` / `SkeletonBlock` / `SkeletonTextLine`, `ErrorView` / `toErrorUIModel()`, `EmptyStateView`, `toCoverUrl()`, `SUBSPLEASE_BASE_URL`, `toTimeLabel(timeZone)`, `safeApiCall`, `DispatcherProvider`, `TimeZoneProvider`, `Clock`, `appJson`, `UiText` / `PluralResource`, `ObserveAsEvents`, `@AdaptivePreviews`, `Navigator` / `NavigationState`.

Home's `Release` doesn't gain `downloads` (1-home.md §6 left that open): Details reads its own `f=show` model.

### 5.2 feature/details/data

| Path | | Responsibility |
|---|---|---|
| `data/remote/ShowDetailsService.kt` | new | `@Single class ShowDetailsService(private val client: HttpClient)`. `suspend fun getShowPage(slug: String): String` (`GET shows/<encoded slug>/`, `bodyAsText()`); `suspend fun getShowEpisodes(sid: String, timeZoneId: String): ShowEpisodesDTO` (`GET api/` with `f=show`, `tz`, `sid`). Constants in a `private companion object`, as `BroadcastService`. |
| `data/remote/ShowPageHtmlParser.kt` | new | `@Single class ShowPageHtmlParser { fun parse(html: String): ShowPageDTO }`. Imports Ksoup (the grep in `tech-stack` allows `*HtmlParser.kt` in `data/remote/`). Pure extraction; validation is the mapper's. |
| `data/dto/ShowPageDTO.kt` | new | `data class ShowPageDTO(val title: String?, val synopsisParagraphs: List<String>, val coverSrc: String?, val sid: String?)`, built by the parser, **not `@Serializable`**. Blank values are `null`; blank paragraphs are left out. |
| `data/dto/ShowEpisodesDTO.kt` | new | `@Serializable data class ShowEpisodesDTO(@SerialName("episode") @Serializable(with = KeyedEpisodesSerializer::class) val episodes: List<EpisodeDTO>?, @SerialName("batch") @Serializable(with = KeyedEpisodesSerializer::class) val batches: List<EpisodeDTO>?)` plus, in the same file (Home's `ReleaseListDTOSerializer` precedent), the decode-only `internal object KeyedEpisodesSerializer : KSerializer<List<EpisodeDTO>>`: `JsonObject` → its values decoded as `EpisodeDTO`; `JsonArray` → its elements (empty → empty list); anything else → `SerializationException`. |
| `data/dto/EpisodeDTO.kt` | new | `@Serializable`: `release_date`, `show`, `episode` (`String?`), `downloads: List<DownloadDTO>?`. |
| `data/dto/DownloadDTO.kt` | new | `@Serializable`: `res`, `magnet`, `torrent`, `xdcc`, all `String?`. |
| `data/mapper/ShowPageDTOMapper.kt` | new | `fun ShowPageDTO.toDomain(slug: ShowSlug): ShowOverview?` (`null` without a title) and `fun ShowPageDTO.toSid(): String?` (digits only), rules in [§4.3](#43-derivation-domain). Private `SYNOPSIS_PLACEHOLDER`. |
| `data/mapper/ShowEpisodesDTOMapper.kt` | new | `fun ShowEpisodesDTO.toDomain(timeZone: TimeZone): ShowEpisodes` (`mapNotNull` both lists, `isTruncated`, private `SHOW_EPISODE_LIMIT = 52`). |
| `data/mapper/EpisodeDTOMapper.kt` | new | `fun EpisodeDTO.toDomain(): Episode?` (label trimmed, `releaseDate?.toReleaseInstant()`, downloads mapped and deduplicated per resolution). |
| `data/mapper/DownloadDTOMapper.kt` | new | `fun DownloadDTO.toDomain(): Download?` (resolution table, link validation, XDCC URL). |
| `data/repository/ShowDetailsRepositoryImpl.kt` | new | `@Single`, implements `ShowDetailsRepository`. Depends on `ShowDetailsService`, `ShowPageHtmlParser`, `DispatcherProvider`, `Clock`. The cache in [§4.5](#45-caching-and-refresh); every call inside `safeApiCall`, throwing `DomainError.InvalidData` for a page without a title or `sid` (passes through `safeApiCall`). |

The parser, the one tricky part:

```kotlin
fun parse(html: String): ShowPageDTO {
    val document = Ksoup.parse(html = html)
    val synopsis = document.selectFirst("div.series-syn")
    val paragraphs = synopsis?.select("p")?.map { it.text() }?.filter { it.isNotBlank() }.orEmpty()  // <h2> skipped
    return ShowPageDTO(
        title = document.selectFirst("h1.entry-title")?.text()?.ifBlank { null },          // entities decoded (D5)
        synopsisParagraphs = paragraphs.ifEmpty { listOfNotNull(synopsis?.ownText()?.ifBlank { null }) }, // text outside <p>
        coverSrc = document.selectFirst("#secondary img.img-responsive")?.attr("src")?.ifBlank { null },
        sid = document.selectFirst("table#show-release-table")?.attr("sid")?.ifBlank { null },
    )
}
```

### 5.3 feature/bookmarks: data, domain, di (no screen yet)

context.md §6 puts the toggle in `feature/bookmarks`' domain, used by Details' presentation (`architecture` §1: presentation may import other features' domain use cases). `presentation/` is created by the Bookmarks plan ([Q5](#10-open-questions)).

| Path | | Responsibility |
|---|---|---|
| `feature/bookmarks/data/dto/BookmarkDTO.kt` | new | `@Serializable data class BookmarkDTO(@SerialName("slug") val slug: String, @SerialName("title") val title: String, @SerialName("cover_url") val coverUrl: String?, @SerialName("added_at_epoch_millis") val addedAtEpochMillis: Long)` |
| `feature/bookmarks/data/dto/BookmarkListDTO.kt` | new | `@Serializable data class BookmarkListDTO(@SerialName("bookmarks") val bookmarks: List<BookmarkDTO>)` |
| `feature/bookmarks/data/local/BookmarkListSerializer.kt` | new | `class BookmarkListSerializer(private val json: Json) : Serializer<BookmarkListDTO>` ([§4.4](#44-local-data)). Reads bytes → `decodeToString()` → `json.decodeFromString`; writes `encodeToString(...).encodeToByteArray()`. |
| `feature/bookmarks/data/local/BookmarksLocalDataSource.kt` | new | `@Single class BookmarksLocalDataSource(@Named(BOOKMARKS_DATA_STORE) private val dataStore: DataStore<BookmarkListDTO>)`. `val bookmarks: Flow<List<BookmarkDTO>>` (IOException → empty, `distinctBy { slug }`, `distinctUntilChanged()`); `suspend fun toggle(bookmark: BookmarkDTO): Boolean` (one `updateData`, returns `updated.bookmarks.any { it.slug == bookmark.slug }`). Top-level `const val BOOKMARKS_DATA_STORE = "bookmarks"` in this file. |
| `feature/bookmarks/data/mapper/BookmarkDTOMapper.kt` | new | `fun BookmarkDTO.toDomain(): Bookmark?` (`null` for a blank slug or title; `Instant.fromEpochMilliseconds`) |
| `feature/bookmarks/data/mapper/BookmarkDomainMapper.kt` | new | `fun Bookmark.toDTO(): BookmarkDTO` |
| `feature/bookmarks/data/repository/BookmarkRepositoryImpl.kt` | new | `@Single`, implements `BookmarkRepository`. `observeBookmarks()` = `local.bookmarks.map { it.mapNotNull(BookmarkDTO::toDomain) }`; `toggle(bookmark)` = `Result.success(local.toggle(bookmark.toDTO()))`, catching only `IOException` → `Result.failure(DomainError.Unknown(e))` ([Q7](#10-open-questions)). Cancellation isn't caught. |
| `feature/bookmarks/domain/model/Bookmark.kt` | new | `data class Bookmark(val show: Show, val addedAt: Instant)` (context.md §6 sketch) |
| `feature/bookmarks/domain/repository/BookmarkRepository.kt` | new | `fun observeBookmarks(): Flow<List<Bookmark>>`; `suspend fun toggle(bookmark: Bookmark): Result<Boolean>` (adds it unless its slug is saved, then removes it; returns whether it's now saved). |
| `feature/bookmarks/domain/usecase/ObserveIsBookmarkedUseCase.kt` | new | `@Factory`. `operator fun invoke(slug: ShowSlug): Flow<Boolean>` = `observeBookmarks().map { list -> list.any { it.show.slug == slug } }.distinctUntilChanged()` |
| `feature/bookmarks/domain/usecase/ToggleBookmarkUseCase.kt` | new | `@Factory`, depends on `BookmarkRepository` and `Clock`. `suspend operator fun invoke(show: Show): Result<Boolean>` = `repository.toggle(Bookmark(show, addedAt = clock.now()))` |
| `feature/bookmarks/di/BookmarksModule.kt` | new | `@Module @ComponentScan("com.zc.bakamitai.feature.bookmarks") class BookmarksModule` with `@Single @Named(BOOKMARKS_DATA_STORE) fun bookmarksDataStore(context: Context, json: Json): DataStore<BookmarkListDTO> = DataStoreFactory.create(serializer = BookmarkListSerializer(json), corruptionHandler = ReplaceFileCorruptionHandler { BookmarkListDTO(emptyList()) }, produceFile = { context.dataStoreFile(BOOKMARKS_FILE) })`, `BOOKMARKS_FILE = "bookmarks.json"`. |

Why the provider is here and qualified ([Q6](#10-open-questions)): `CommonModule` can't import a feature's DTO (`structure` §3), and Koin keys definitions by class, so `DataStore<BookmarkListDTO>` and Settings' future `DataStore<Preferences>` would collide without the `@Named` qualifier (`org.koin.core.annotation.Named`).

### 5.4 feature/details/domain (pure Kotlin)

| Path | | Responsibility |
|---|---|---|
| `domain/model/ShowOverview.kt` | new | `data class ShowOverview(val show: Show, val synopsis: String?, val webUrl: String)`: the show page; `show.title` / `coverUrl` are the page's. |
| `domain/model/ShowEpisodes.kt` | new | `data class ShowEpisodes(val episodes: List<Episode>, val batches: List<Episode>, val isTruncated: Boolean, val timeZone: TimeZone)` (`isTruncated`: SubsPlease's 52-entry cap was hit; `timeZone`: the zone to show times in, Home's `TodayBroadcasts` precedent). |
| `domain/model/Episode.kt` | new | `data class Episode(val label: String, val releasedAt: Instant, val downloads: List<Download>)` (`label` as SubsPlease writes it: `"07"`, `"1216v2"`, `"Movie"`, `"01-28"`). |
| `domain/model/Download.kt` | new | `data class Download(val resolution: Resolution, val height: Int, val magnetUri: String?, val torrentUrl: String?, val xdccUrl: String?)` (`height`: 360, 480, 540, 720 or 1080, for the SD label). |
| `domain/model/Resolution.kt` | new | `enum class Resolution { SD, HD, FHD }` |
| `domain/repository/ShowDetailsRepository.kt` | new | `suspend fun getOverview(slug: ShowSlug, forceRefresh: Boolean): Result<ShowOverview>`; `suspend fun getEpisodes(slug: ShowSlug, timeZone: TimeZone, forceRefresh: Boolean): Result<ShowEpisodes>`. Failures are `DomainError`. The `sid` is a data detail and never appears here. |
| `domain/usecase/GetShowOverviewUseCase.kt` | new | `@Factory`. `suspend operator fun invoke(slug: ShowSlug, forceRefresh: Boolean = false): Result<ShowOverview>` (pass-through) |
| `domain/usecase/GetShowEpisodesUseCase.kt` | new | `@Factory`, depends on `ShowDetailsRepository` and `TimeZoneProvider`. `suspend operator fun invoke(slug: ShowSlug, forceRefresh: Boolean = false): Result<ShowEpisodes>` ([§4.3](#43-derivation-domain)). |

### 5.5 feature/details/di

| Path | | Responsibility |
|---|---|---|
| `feature/details/di/DetailsModule.kt` | new | `@Module @ComponentScan("com.zc.bakamitai.feature.details") class DetailsModule` |
| `app/di/AppModule.kt` | modify | `includes = [CommonModule::class, HomeModule::class, ScheduleModule::class, ShowsModule::class, BookmarksModule::class, DetailsModule::class]` |

### 5.6 feature/details/presentation

Component placement follows `structure` §1.1. `DetailsHeaderUIModel` is read by the Screen (top-bar title) and two components (header, synopsis), so it's in `presentation/model/`. `DownloadSource` is used by the Intent and a component.

| Path | | Responsibility |
|---|---|---|
| `presentation/model/DetailsHeaderUIModel.kt` | new | `@Immutable data class DetailsHeaderUIModel(val title: UiText, val coverUrl: String?, val synopsis: UiText?)` |
| `presentation/model/DownloadsUIState.kt` | new | The downloads section ([§5.10](#510-mvi-contract)), with `fun withEpisodeToggled(key: String): DownloadsUIState` (flips one row in `Content`, otherwise `this`). |
| `presentation/model/DownloadSource.kt` | new | `enum class DownloadSource { MAGNET, TORRENT, XDCC }`, a plain value enum like Schedule's `StatusFilter`. |
| `presentation/mapper/DetailsHeaderUIModelMapper.kt` | new | `fun ShowOverview.toHeaderUIModel(): DetailsHeaderUIModel` |
| `presentation/mapper/ShowEpisodesUIModelMapper.kt` | new | `fun ShowEpisodes.toDownloadsUIState(expandedKeys: Set<String>?): DownloadsUIState`: `Empty` when both lists are empty; else `Content` with `countLabel` (plural `details_episode_count` of `episodes.size`, `null` when 0), the batch and episode rows, `limitNote` when `isTruncated`. `expandedKeys == null` (first load) → only the default key (first episode, else first batch) is expanded; otherwise the given keys. |
| `component/episoderow/model/EpisodeUIState.kt` | new | `@Immutable data class EpisodeUIState(val key: String, val title: UiText, val releasedLabel: UiText, val resolutions: ImmutableList<ResolutionUIModel>, val isExpanded: Boolean = false)`; `key` = `"episode|<label>"` / `"batch|<label>"` (unique: the API keys entries by label). |
| `component/episoderow/mapper/EpisodeUIModelMapper.kt` | new | `fun Episode.toEpisodeUIState(isBatch: Boolean, timeZone: TimeZone, isExpanded: Boolean): EpisodeUIState`: title rules ([§2.6](#26-episode-row)); release line from a private `LocalDate.Format { monthName(MonthNames.ENGLISH_ABBREVIATED); char(' '); day(Padding.NONE); chars(", "); year() }` and `releasedAt.toTimeLabel(timeZone)`. |
| `component/episoderow/component/resolutionrow/model/ResolutionUIModel.kt` | new | `@Immutable data class ResolutionUIModel(val label: UiText, val tone: ResolutionTone, val magnetUri: String?, val torrentUrl: String?, val xdccUrl: String?)` with `val isAvailable get() = magnetUri != null \|\| torrentUrl != null \|\| xdccUrl != null`. |
| `component/episoderow/component/resolutionrow/model/ResolutionTone.kt` | new | `enum class ResolutionTone { FHD, HD, SD }` (pill colors) |
| `component/episoderow/component/resolutionrow/mapper/ResolutionUIModelMapper.kt` | new | `fun List<Download>.toResolutionUIModels(): ImmutableList<ResolutionUIModel>`: exactly three, FHD, HD, SD; labels `details_resolution` "%1$dp %2$s" with `details_quality_fhd` / `_hd` / `_sd`; a missing SD slot → `details_quality_sd` alone. |
| `component/detailsheader/DetailsHeaderView.kt` | new | `DetailsHeaderView(model: DetailsHeaderUIModel, isBookmarked: Boolean, onBookmarkClick: () -> Unit, contentPadding: Dp, modifier)` + `DetailsHeaderSkeletonView(contentPadding: Dp, modifier)` ([§2.2](#22-hero)); window width read inside for the poster size. |
| `component/detailsheader/component/bookmarkbutton/BookmarkButtonView.kt` | new | `BookmarkButtonView(isBookmarked, onClick, modifier)` + `BookmarkButtonSkeletonView(modifier)` ([§2.3](#23-bookmark-button)) |
| `component/synopsiscard/SynopsisCardView.kt` | new | `SynopsisCardView(synopsis: UiText?, isExpanded: Boolean, onToggle: () -> Unit, modifier)` + `SynopsisCardSkeletonView(modifier)` ([§2.4](#24-synopsis-card)) |
| `component/downloadsheader/DownloadsHeaderView.kt` | new | `DownloadsHeaderView(countLabel: UiText?, modifier)` ([§2.5](#25-downloads-header-and-groups)) |
| `component/episodegroupheader/EpisodeGroupHeaderView.kt` | new | `EpisodeGroupHeaderView(label: UiText, modifier)` |
| `component/episoderow/EpisodeRowView.kt` | new | `EpisodeRowView(model, onToggle, onSourceClick, modifier)` + `EpisodeRowSkeletonView(modifier)` ([§2.6](#26-episode-row)) |
| `component/episoderow/component/resolutionrow/ResolutionRowView.kt` | new | `ResolutionRowView(model, episodeTitle, onSourceClick, modifier)` and its private `SourceButton` ([§2.7](#27-resolution-row-and-sources)) |
| `presentation/DetailsState.kt`, `DetailsIntent.kt`, `DetailsEffect.kt` | new | [§5.10](#510-mvi-contract) |
| `presentation/DetailsViewModel.kt` | new | `@KoinViewModel class DetailsViewModel(@InjectedParam private val slug: String, private val getShowOverview: GetShowOverviewUseCase, private val getShowEpisodes: GetShowEpisodesUseCase, observeIsBookmarked: ObserveIsBookmarkedUseCase, private val toggleBookmark: ToggleBookmarkUseCase)`. `state = combine(_state, observeIsBookmarked(ShowSlug(slug))) { s, saved -> s.copy(isBookmarked = saved) }.stateIn(viewModelScope, WhileSubscribed(5_000), DetailsState())`. Keeps the last `ShowOverview` private (share, web, bookmark). Reducer rules in §5.10. |
| `presentation/DetailsRoute.kt` | new | `DetailsRoute(slug: String, onNavigateBack: () -> Unit, modifier, viewModel = koinViewModel { parametersOf(slug) })`: `collectAsStateWithLifecycle()`, `ObserveAsEvents`, owns the `SnackbarHostState`, a `rememberCoroutineScope()`, `LocalClipboard`; private `Context.openLink(url): Boolean` and `Context.share(title, text)`; effects per [§2.8](#28-opening-links-sharing-and-snackbars). |
| `presentation/DetailsScreen.kt` | new | `DetailsScreen(state, onIntent, modifier, snackbarHostState = remember { SnackbarHostState() })`. Private: `DetailsTopAppBar`, `DetailsList` (the `LazyColumn` in `BoxWithConstraints`, shared by content and skeleton; it computes `h = centeredContentPadding(maxWidth, margin)` and hands it to the item builders, with **no** horizontal content padding of its own so the hero's backdrop spans the pane; every other item pads itself by `h`; `spacing.xl` bottom content padding), `DetailsContent`, `LazyListScope.downloadsItems(…)`, `DetailsSkeleton`, `EpisodeLimitNote`. `@AdaptivePreviews` with a `PreviewParameterProvider<DetailsState>`. If Detekt's `TooManyFunctions` (11) trips, move `EpisodeLimitNote` to `component/episodelimitnote/`. |

`DetailsScreen` layout:

- `listState = rememberLazyListState()`, `scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()`, `Scaffold(modifier.nestedScroll(scrollBehavior.nestedScrollConnection), topBar = { DetailsTopAppBar(…) }, snackbarHost = …)` with **default** content insets (the earlier screens' lesson; with the bar hidden they include the system navigation bar, checked in step 22).
- Body `when`: `state.header != null` → `PullToRefreshBox(state.isRefreshing, onRefresh = { onIntent(Refresh) })` → `DetailsContent`; `state.error != null` → `ErrorView`; else → `DetailsSkeleton`.
- `DetailsContent` items, each with `key` and `contentType`: `"hero"`, `"synopsis"`, `"downloads-header"`, then by `state.downloads`: `"downloads-loading"` (one `SkeletonView`), `"downloads-error"`, `"downloads-empty"`, or `"group-batches"` + batch rows + `"group-episodes"` (only with batches) + episode rows (`items(…, key = { it.key }, contentType = { "episode" })`) + `"limit-note"`.
- Lambdas adapted once: `remember(onIntent) { { key: String -> onIntent(EpisodeToggled(key)) } }`, `remember(onIntent) { { source: DownloadSource, url: String -> onIntent(SourceClicked(source, url)) } }`, and likewise for bookmark and synopsis.
- Window size (`currentWindowAdaptiveInfo().windowSizeClass`) is read in `DetailsList` and the header for the margin and poster width, never stored in state.

### 5.7 Navigation

| Path | | Responsibility |
|---|---|---|
| `feature/details/presentation/navigation/DetailsNavKey.kt` | new | `@Serializable data class DetailsNavKey(val slug: String) : NavKey, FullScreenNavKey`: the slug only. |
| `feature/details/presentation/navigation/DetailsNavigation.kt` | new | `fun Navigator.navigateToDetails(slug: String) = navigate(DetailsNavKey(slug))` and `fun EntryProviderScope<NavKey>.detailsEntry(onNavigateBack: () -> Unit)` → `entry<DetailsNavKey> { key -> DetailsRoute(slug = key.slug, onNavigateBack = onNavigateBack) }`. |
| `app/navigation/AppEntryProvider.kt` | modify | `fun appEntryProvider(navigator: Navigator)`: `homeEntry(onOpenDetails = navigator::navigateToDetails, onOpenSettings = null)`, the same for `scheduleEntry` and `allShowsEntry`, and `detailsEntry(onNavigateBack = navigator::goBack)`. KDoc: only Settings is still `null`. |
| `app/navigation/AppNavigation.kt` | modify | `remember(navigator) { appEntryProvider(navigator) }`. Hides the bar for full-screen keys (below). |

Hiding the bar (`AppNavigation`):

```kotlin
val layoutType = NavigationSuiteScaffoldDefaults.calculateFromAdaptiveInfo(currentWindowAdaptiveInfo())
val suiteState = rememberNavigationSuiteScaffoldState()
val hideBar by remember(navigationState, layoutType) {
    derivedStateOf {
        layoutType == NavigationSuiteType.NavigationBar && navigationState.visibleKeys.lastOrNull() is FullScreenNavKey
    }
}
LaunchedEffect(hideBar) { if (hideBar) suiteState.hide() else suiteState.show() }
NavigationSuiteScaffold(…, layoutType = layoutType, state = suiteState) { NavDisplay(…) }
```

`calculateFromAdaptiveInfo` returns `NavigationBar` for compact width **or** compact height (phones in both orientations) and `NavigationRail` otherwise, so the rail never hides. The library recommends the state API over `NavigationSuiteType.None` (it animates).

Back and process death: `NavDisplay(onBack = navigator::goBack)` already pops Details; `DetailsNavKey` is serializable, so each tab's back stack restores it. The Navigation 3 `rememberViewModelStoreNavEntryDecorator` gives each Details entry its own ViewModel.

`architecture` §3 names a `Navigator.navigateBack()` in `app/navigation/NavigatorExtensions.kt`; `Navigator.goBack()` already is that function, so no extension is added and the skill text is updated (step 23, [Q16](#10-open-questions)).

### 5.8 Resources

`res/values/strings.xml` (modify), a new `<!-- Details (feature/details) -->` section (shared entries are in [§5.1](#51-shared-code)):

- `cd_details_share` "Share", `cd_details_open_in_web` "Open on SubsPlease"
- `details_bookmark` "Bookmark", `details_bookmarked` "Bookmarked", `cd_details_bookmarked` "Bookmarked", `cd_details_not_bookmarked` "Not bookmarked", `cd_details_add_bookmark` "Add to bookmarks", `cd_details_remove_bookmark` "Remove from bookmarks"
- `details_synopsis_title` "Synopsis", `details_synopsis_read_more` "Read more", `details_synopsis_read_less` "Read less", `details_synopsis_missing` "No synopsis yet."
- `details_section_downloads` "Episodes &amp; Downloads", `details_group_batches` "BATCHES", `details_group_episodes` "EPISODES" (uppercase, DESIGN.md labels)
- `details_episode` "Episode %1$s", `details_episode_revision` "Episode %1$s (v%2$s)", `details_episode_batch` "Episodes %1$s", `details_episode_movie` "Movie"
- `details_episode_released` "%1$s · %2$s" (`%1$s`: "Nov 17, 2026"; `%2$s`: "12:38 PM")
- `cd_details_show_downloads` "Show downloads", `cd_details_hide_downloads` "Hide downloads"
- `details_resolution` "%1$dp %2$s", `details_quality_fhd` "FHD", `details_quality_hd` "HD", `details_quality_sd` "SD", `details_resolution_unavailable` "Not released"
- `details_source_magnet` "Magnet", `details_source_torrent` "Torrent", `details_source_xdcc` "XDCC", `cd_details_source` "%1$s, %2$s, %3$s" (source, resolution, episode)
- `details_no_episodes_title` "No episodes yet", `details_no_episodes_message` "No episodes available for this show yet."
- `details_episode_limit_note` "SubsPlease lists only the latest %1$d releases."
- `details_share_text` "%1$s\n%2$s"
- `details_no_torrent_app` "No torrent app found", `details_no_app_for_link` "No app can open this link", `details_copy_link` "Copy link", `details_link_copied` "Link copied", `details_clip_label` "Link"
- Plural `details_episode_count`: one "%d EPISODE", other "%d EPISODES"

### 5.9 Callers: Home, Schedule, All Shows

Now that Details exists, the nullable `onOpenDetails` / `canOpenShow` path is dead code; it's removed ([Q8](#10-open-questions)). Views keep their nullable `onClick` (still used by their previews).

| Path | | Change |
|---|---|---|
| `feature/home/presentation/navigation/HomeNavigation.kt` | modify | `homeEntry(onOpenDetails: (slug: String) -> Unit, onOpenSettings: (() -> Unit)?)`; KDoc. |
| `feature/home/presentation/HomeRoute.kt` | modify | `onOpenDetails: (slug: String) -> Unit`; `OpenDetails` → `onOpenDetails(effect.slug)`; no `canOpenShow`. |
| `feature/home/presentation/HomeScreen.kt` | modify | Drop `canOpenShow` (and from `HomeContent` / `UpcomingCarousel`); cards and rows always get `onClick`. Preview call updated. |
| `feature/schedule/presentation/navigation/ScheduleNavigation.kt`, `ScheduleRoute.kt`, `ScheduleScreen.kt` | modify | The same. |
| `feature/shows/presentation/navigation/AllShowsNavigation.kt`, `AllShowsRoute.kt`, `AllShowsScreen.kt` | modify | The same; rows now show the ripple and the chevron (3-shows.md §2.5). |

Visible change: Home's upcoming cards and release rows, Schedule's cards and All Shows' rows get ripples (`Card(onClick)` / `clickable`), and open Details.

### 5.10 MVI contract

```kotlin
@Immutable
data class DetailsState(
    val header: DetailsHeaderUIModel? = null,                       // set once the show page is parsed
    val error: ErrorUIModel? = null,                                // the page failed and there is no header
    val isBookmarked: Boolean = false,                              // from ObserveIsBookmarkedUseCase (combined)
    val isSynopsisExpanded: Boolean = false,
    val downloads: DownloadsUIState = DownloadsUIState.Loading,
    val isRefreshing: Boolean = false,
) {
    val isLoading: Boolean get() = header == null && error == null
}

@Immutable
sealed interface DownloadsUIState {
    @Immutable data object Loading : DownloadsUIState
    @Immutable data class Error(val error: ErrorUIModel) : DownloadsUIState
    @Immutable data object Empty : DownloadsUIState
    @Immutable data class Content(
        val countLabel: UiText?,                                    // "20 EPISODES"; null with batches only
        val batches: ImmutableList<EpisodeUIState>,
        val episodes: ImmutableList<EpisodeUIState>,
        val limitNote: UiText?,                                     // SubsPlease's 52-entry cap was hit
    ) : DownloadsUIState
}

sealed interface DetailsIntent {
    data object Load : DetailsIntent                                // from init
    data object Retry : DetailsIntent                               // full-screen ErrorView
    data object RetryEpisodes : DetailsIntent                       // downloads-section ErrorView
    data object Refresh : DetailsIntent                             // pull-to-refresh (bypasses the cache)
    data object BackClicked : DetailsIntent
    data object ShareClicked : DetailsIntent
    data object OpenInWebClicked : DetailsIntent
    data object BookmarkClicked : DetailsIntent
    data object SynopsisToggled : DetailsIntent
    data class EpisodeToggled(val key: String) : DetailsIntent
    data class SourceClicked(val source: DownloadSource, val url: String) : DetailsIntent
}

sealed interface DetailsEffect {
    data object NavigateBack : DetailsEffect
    data class OpenTorrentLink(val url: String) : DetailsEffect      // magnet: or .torrent; failure → "No torrent app found"
    data class OpenWebLink(val url: String) : DetailsEffect          // XDCC page or show page; failure → "No app can open this link"
    data class Share(val title: UiText, val text: UiText) : DetailsEffect
    data class ShowError(val error: ErrorUIModel) : DetailsEffect    // refresh or bookmark write failed
}
```

**Reducer rules:**

- **`Load` / `Retry`:** ignored while `loadJob?.isActive`. `error = null` (the skeleton shows). Then, in one coroutine: `getShowOverview(slug)` → on success keep `overview`, `header = overview.toHeaderUIModel()`; then load episodes (below). On failure: `error = throwable.toErrorUIModel()`, stop.
- **Episodes** (after the page, and for `RetryEpisodes`): `downloads = Loading` unless it's `Content` (a refresh keeps it); `getShowEpisodes(slug, forceRefresh)` → `downloads = episodes.toDownloadsUIState(expandedKeys)` where `expandedKeys` = `null` the first time (default expansion), afterwards the keys expanded in the current `Content` (kept across refresh and retry; a key that no longer exists is dropped). Failure: `Error(…)` if there was no `Content`, else keep it and send `ShowError`.
- **`RetryEpisodes`:** ignored while a load runs or before the header exists; episodes only.
- **`Refresh`:** ignored while a load runs or without a header. `isRefreshing = true`; page with `forceRefresh = true` (failure → `ShowError`, `isRefreshing = false`, stop); new header; episodes with `forceRefresh = true`; `isRefreshing = false`.
- **`BookmarkClicked`:** ignored without `overview`. `toggleBookmark(overview.show)`; on failure `ShowError`. `isBookmarked` follows the store's `Flow`, so there's no optimistic flip to roll back.
- **`SynopsisToggled`:** flip `isSynopsisExpanded`. **`EpisodeToggled(key)`:** `downloads = downloads.withEpisodeToggled(key)`. Both synchronous.
- **`ShareClicked`:** with `overview`, `Share(title = StringValue(title), text = StringResource(details_share_text, listOf(title, overview.webUrl)))`.
- **`OpenInWebClicked`:** with `overview`, `OpenWebLink(overview.webUrl)`.
- **`SourceClicked`:** `MAGNET` / `TORRENT` → `OpenTorrentLink(url)`; `XDCC` → `OpenWebLink(url)`.
- **`BackClicked`:** `NavigateBack`.

Keep the ViewModel under Detekt's 11 functions per class: one private `load(forceRefresh)` for Load / Retry / Refresh (the page step, then a `loadEpisodes(forceRefresh)` step), `onBookmarkClicked()`, `withOverview { }` for Share / Open in web, `sendEffect`.

---

## 6. Dependencies

### Libraries

**No new library.** Everything is in `gradle/libs.versions.toml` and already a dependency of `:app`:

- DataStore 1.3.0 (`androidx.datastore:datastore`, typed): `DataStoreFactory.create(serializer, corruptionHandler, produceFile)`, `Serializer`, `CorruptionException`, `ReplaceFileCorruptionHandler`, `Context.dataStoreFile`. Its default scope uses the library's own IO dispatcher (our code still references no `Dispatchers.*`).
- Ksoup 0.2.6 (`ShowPageHtmlParser` only).
- Compose Material 3 1.4.0 (`TopAppBar`, `Button`, `TextButton`, `PullToRefreshBox`, `HorizontalDivider`), foundation-layout 1.12.1 (`FlowRow`), compose-ui 1.12.1 (`Modifier.blur`, `LocalClipboard`, `ClipEntry`), `material3-adaptive-navigation-suite` 1.4.0 (`NavigationSuiteScaffoldState`, `rememberNavigationSuiteScaffoldState`, `NavigationSuiteType`), `material-icons-extended` 1.7.8, core-ktx (`String.toUri()`), Navigation 3 1.2.1, Koin (`@Named`), Ktor, kotlinx-serialization, kotlinx-datetime, kotlinx-collections-immutable.
- Opt-ins on the smallest declaration: `ExperimentalMaterial3Api` (`TopAppBar`, `PullToRefreshBox`), `ExperimentalLayoutApi` only if `FlowRow` asks for it, `ExperimentalTime` if `Instant` / `Clock` need it.
- **Custom Tabs** would need `androidx.browser`, which isn't approved (context.md §7 decision 2). The plan uses `ACTION_VIEW` ([Q4](#10-open-questions)); choosing Custom Tabs means a `tech-stack` update first (`library-management` §5).

### Earlier plans and shared code

- **Needs (exists):** everything in [§5.1](#51-shared-code)'s "reused" list and the navigation shell.
- **Moves to `common/`:** `toReleaseInstant()` (from Home), `MaxContentWidth` / `centeredContentPadding` (from All Shows); **adds** `FullScreenNavKey`.
- **Changes:** Home, Schedule and All Shows open Details ([§5.9](#59-callers-home-schedule-all-shows)); `Navigator` gains single-top pushes and tab reselection.

### What it unblocks

- **Bookmarks plan** builds on `feature/bookmarks/{data,domain,di}`:
  - adds `ObserveBookmarksUseCase` over `BookmarkRepository.observeBookmarks()` (sorted by `addedAt`, newest first) and, if it offers removal from the grid, `RemoveBookmarkUseCase` (+ a repository `remove(slug)`);
  - adds `feature/bookmarks/presentation/` (grid, empty state, `BookmarksNavKey`, `bookmarksEntry(onOpenDetails = navigator::navigateToDetails)`) and sets `TopLevelDestination.BOOKMARKS.key`;
  - moves Details' "Nov 17, 2026" date format to `common/ui/mapper/` for the "date added" label (context.md §4.5);
  - may pre-fill Details' header from a saved bookmark and refresh a bookmark's stored title / cover when Details loads it ([Q3](#10-open-questions), [Q15](#10-open-questions)).
- **Settings plan:** the 12-hour constant in `toTimeLabel` drives Details' release times too; `Context.openLink` moves to `common/ui/util/` when Settings' "About" needs it.
- **Any later pushed screen** reuses `FullScreenNavKey` and the bar hiding.

---

## 7. Testing

Unit tests mirror the main package under `app/src/test/java/com/zc/bakamitai/`. Tools: JUnit4, `kotlin.test`, `runTest`, Turbine, Ktor `MockEngine` through `createHttpClient(appJson, "test", engine)`, `TestDispatcherProvider`, `FixedClock`, `FixedTimeZoneProvider`, `TemporaryFolder`, hand-written fakes.

### Fixtures

Captured in step 6 from the live site (one request each, one second apart, `tz=America/New_York`) and trimmed; constants added to `SubsPleaseFixtures`.

| File | Source | Must contain |
|---|---|---|
| `show_page.html` | `GET /shows/sousou-no-frieren/` | The real `<title>` and site menu stub, `h1.entry-title` "Sousou no Frieren", `div.series-syn` with its `<h2>` and both `<p>`s (with the raw "—"), `table#show-release-table sid="620"` (empty), `#secondary img.img-responsive src="/wp-content/uploads/2023/09/138006.jpg"`. |
| `show_episodes.json` | `f=show&sid=620` | `batch` with "01-28"; `episode` trimmed to "28", "27", "01" (three entries, all three resolutions each). |
| `show_episodes_specials.json` | `f=show&sid=48` | `episode` trimmed to "1220", "1219", "1218", "1216v2", "1217", "1216", "The Counterfeit Case of Ultra 30" (API order); `batch: []`. |
| `show_episodes_sd.json` | `f=show&sid=58` | `batch` "01-12" with `540`; `episode` "12" and "01" with `360`. Show name "A3! Season Autumn & Winter". |
| `show_episodes_movie.json` | `f=show&sid=582` | The single "Movie" entry. |
| `show_episodes_empty.json` | `f=show&sid=999999` | `{"batch":[],"episode":[]}` |

Inline in tests: entity titles (`A3! Season Autumn &amp; Winter`, `BanG Dream! It&#8217;s MyGO!!!!!`, `Dark Machine &#8211; The Animation`), the placeholder synopsis (`<p><i>Synopsis will be added later</i></p>`), text outside `<p>`, missing cover / title / `sid`, a non-numeric `sid`, a 52-entry episode object built in the test, `res: "2160"`, blank links, `downloads` missing, a bad `release_date`, an `episode` `[]` with a `batch` object, a non-empty array root.

### Test classes

| Class | Covers |
|---|---|
| `common/data/mapper/ReleaseDateDTOMapperTest` | `-0500`, `+0100`, `+0800` → the same instant rules; no offset / garbage / impossible date → `null`. Home's `ReleaseDTOMapperTest` still passes. |
| `common/ui/navigation/NavigatorTest` | **Modify:** pushing the key already on top is a no-op; a different key still pushes; reselecting the selected tab pops its stack to the root; selecting another tab keeps both stacks. |
| `feature/details/data/remote/ShowPageHtmlParserTest` | Fixture → title, two paragraphs, cover `src`, `sid` "620"; the `<h2>Synopsis</h2>` isn't a paragraph. Entities decoded in titles (D5). Placeholder synopsis read as text. Text outside `<p>` → one paragraph. Missing elements → `null` / empty. |
| `feature/details/data/dto/ShowEpisodesDTOTest` | Each fixture decodes through `appJson`; `{}` objects and `[]` arrays (D10); `episode: []` with a `batch` object; a non-empty array; a string root → `SerializationException`. |
| `feature/details/data/mapper/ShowPageDTOMapperTest` | Title trimmed and collapsed; blank → `null`; cover through `toCoverUrl()` (relative, absolute, placeholder); paragraphs joined with `"\n\n"`; placeholder (any case, trailing period) → `null`; `webUrl` "https://subsplease.org/shows/<slug>/"; `toSid()`: " 620 " → "620", "abc" / blank → `null`. |
| `feature/details/data/mapper/ShowEpisodesDTOMapperTest` | Both lists mapped; invalid entries dropped; `isTruncated` at 52 raw entries, not at 51 (D1); the given zone attached. |
| `feature/details/data/mapper/EpisodeDTOMapperTest` | Labels kept as-is ("1216v2", "Egghead SP11", "Movie", "01-28"); blank label or bad date → `null`; no downloads → empty list; one download per resolution. |
| `feature/details/data/mapper/DownloadDTOMapperTest` | 360/480/540 → SD with that height; 720 → HD; 1080 → FHD; "2160", "abc", `null` → `null` (D4, Q20); non-`magnet:` magnet and non-http torrent dropped; XDCC URL = base + `xdcc/?search=` + the value **unchanged** (`%22…%28480p%29%22`); all links missing → `null`. |
| `feature/details/data/remote/ShowDetailsServiceTest` | `GET /shows/sousou-no-frieren/` (trailing slash, no query), body returned as text (UTF-8 "—"); a slug with `_` keeps it; `GET /api/` with `f=show`, `tz`, `sid` decodes `show_episodes.json` served as `text/html`. |
| `feature/details/data/repository/ShowDetailsRepositoryImplTest` | `MockEngine` counting requests, the real parser, `FixedClock`, `TestDispatcherProvider`. Overview from the fixture; second call within 10 min → no request; at 10 min → request; `forceRefresh` → request. `getEpisodes` on a fresh slug → one page + one `f=show` request; again → none; with an **expired** page and expired episodes → only `f=show` (cached `sid`). `tz` sent is the given zone's id. Failure not cached and keeps the old entry. 404 → `NotFound` (D8); 500 → `Server`; `IOException` → `Network`; page without `h1` or `sid` → `InvalidData`; empty `f=show` body → `InvalidData`. |
| `feature/details/domain/repository/FakeShowDetailsRepository.kt` | **Fake.** Settable results, call logs (`forceRefresh`, zone), optional `CompletableDeferred` gates. |
| `feature/details/domain/usecase/GetShowOverviewUseCaseTest` | Pass-through, `forceRefresh` passed, failure propagated. |
| `feature/details/domain/usecase/GetShowEpisodesUseCaseTest` | Sorted newest first (episodes and batches); ties keep input order; zone from `FixedTimeZoneProvider` passed and returned; failure propagated. |
| `feature/bookmarks/data/local/BookmarkListSerializerTest` | Round trip; blank input → default; malformed JSON → `CorruptionException`; unknown keys ignored. |
| `feature/bookmarks/data/local/BookmarksLocalDataSourceTest` | A real `DataStoreFactory.create(…, scope = backgroundScope, produceFile = { File(tmp.root, "bookmarks.json") })` (a path that doesn't exist yet): starts empty; `toggle` adds (→ `true`) then removes (→ `false`); two shows kept in insertion order; a duplicate slug in the file is read once. |
| `feature/bookmarks/data/mapper/BookmarkDTOMapperTest`, `BookmarkDomainMapperTest` | Field mapping, epoch millis ↔ `Instant`, `null` cover; blank slug or title → `null`. |
| `feature/bookmarks/data/repository/BookmarkRepositoryImplTest` | Over the real data source on a temp file: `observeBookmarks()` emits after `toggle`; `toggle` returns the new state. |
| `feature/bookmarks/domain/repository/FakeBookmarkRepository.kt` | **Fake.** `MutableStateFlow<List<Bookmark>>`, settable toggle failure. |
| `feature/bookmarks/domain/usecase/ObserveIsBookmarkedUseCaseTest` | `true` / `false` per slug; no duplicate emissions when other slugs change. |
| `feature/bookmarks/domain/usecase/ToggleBookmarkUseCaseTest` | Saves the show with `addedAt = FixedClock.now()`; returns the new state; failure propagated. |
| `feature/details/presentation/mapper/DetailsHeaderUIModelMapperTest` | Title / cover / synopsis mapped; `null` synopsis stays `null`. |
| `…/component/episoderow/mapper/EpisodeUIModelMapperTest` | "20" → `details_episode`; "1216v2" → `details_episode_revision` ("1216", "2"); "movie" → `details_episode_movie`; batch "01-28" → `details_episode_batch` with "01–28" (en dash); "Egghead SP11" → `StringValue`; release line "Nov 17, 2026" + "12:38 PM" in New York, the date shifting in Tokyo; key prefixes. |
| `…/resolutionrow/mapper/ResolutionUIModelMapperTest` | Always 3 in FHD, HD, SD order; "480p SD" / "360p SD"; missing SD → "SD", unavailable; missing 720 → "720p HD", unavailable; links copied. |
| `feature/details/presentation/mapper/ShowEpisodesUIModelMapperTest` | `Empty` when both lists empty; batches only → `Content` with `countLabel == null`; default expansion = first episode, else first batch; given keys respected; `limitNote` only when truncated. |
| `feature/details/presentation/model/DownloadsUIStateTest` | `withEpisodeToggled` flips one row in batches or episodes; other states unchanged. |
| `feature/details/presentation/DetailsViewModelTest` | See below. |
| `app/navigation/NavKeySerializationTest` | **Modify:** add `DetailsNavKey("sora-wa-akai-kawa-no-hotori")`. |
| `app/di/KoinModulesTest` | **Modify:** `verify()` passes with `BookmarksModule` and `DetailsModule` (add `String::class` to `extraTypes` if the `@InjectedParam` asks for it). The instance test resolves `ShowDetailsRepository` as `ShowDetailsRepositoryImpl`, `GetShowOverviewUseCase`, `GetShowEpisodesUseCase`; it doesn't resolve the bookmarks bindings, which need an Android `Context` (covered by `verify()`). |

`DetailsViewModelTest` (`MainDispatcherRule`, Turbine, real use cases over `FakeShowDetailsRepository` / `FakeBookmarkRepository`, `FixedClock`, `FixedTimeZoneProvider`):
- `init` → skeleton (`isLoading`) → header with `downloads = Loading` (gate the episodes) → `Content` with the first episode expanded.
- Page failure → `error`, no episodes call; `Retry` → content. `NotFound` → non-retryable error.
- Episodes failure → `downloads = Error`, header kept; `RetryEpisodes` → content.
- `Refresh` → `isRefreshing`, both calls with `forceRefresh = true`, expanded keys kept; refresh page failure → content kept, `ShowError`, no episodes call; refresh episodes failure → old content kept, `ShowError`.
- A second `Load` / `Refresh` while loading → ignored (one call each).
- `isBookmarked` follows the fake's flow; `BookmarkClicked` saves the page's show (title, absolute cover, `addedAt`); before the header → no call; failure → `ShowError`.
- `SynopsisToggled` and `EpisodeToggled` update state synchronously.
- Effects: `BackClicked` → `NavigateBack`; `ShareClicked` → `Share` with the title and `https://subsplease.org/shows/<slug>/`; `OpenInWebClicked` → `OpenWebLink(webUrl)`; `SourceClicked(MAGNET | TORRENT)` → `OpenTorrentLink`; `SourceClicked(XDCC)` → `OpenWebLink`.

**Every quirk in [§4](#4-data) has a test:**
- D1 52-entry cap: `ShowEpisodesDTOMapperTest`, `ShowEpisodesUIModelMapperTest` (`limitNote`)
- D2 offsets and the 1-hour shift (shown as-is): `ReleaseDateDTOMapperTest`, `EpisodeUIModelMapperTest`
- D3 labels: `EpisodeDTOMapperTest`, `EpisodeUIModelMapperTest`
- D4 SD heights, unknown `res`: `DownloadDTOMapperTest`, `ResolutionUIModelMapperTest`
- D5 entity titles: `ShowPageHtmlParserTest`
- D6 placeholder synopsis: `ShowPageHtmlParserTest`, `ShowPageDTOMapperTest`
- D7 page cover preferred: the header uses `ShowOverview.show.coverUrl` (`DetailsViewModelTest` bookmark payload)
- D8 404, trailing slash: `ShowDetailsRepositoryImplTest`, `ShowDetailsServiceTest`
- D10 `[]` / `{}` roots, unknown sid: `ShowEpisodesDTOTest`
- D11 batches without episodes: `ShowEpisodesUIModelMapperTest`
- XDCC not re-encoded: `DownloadDTOMapperTest`
- D9 and D12 are documented only.

### Compose previews

`@AdaptivePreviews` (light/dark × compact/expanded) inside `BakaMitaiTheme`, private sample data:

- `DetailsScreen` provider: first load (skeleton); header with downloads loading; content (batches + episodes, newest expanded, one missing SD); episodes only with the limit note; downloads error; no episodes; page error; refreshing; long title and no synopsis.
- `DetailsHeaderView`: bookmarked / not, long title, `null` cover; plus `DetailsHeaderSkeletonView`.
- `BookmarkButtonView`: both states; plus its skeleton.
- `SynopsisCardView`: collapsed long text, expanded, missing; plus `SynopsisCardSkeletonView`.
- `DownloadsHeaderView` with and without a count; `EpisodeGroupHeaderView`.
- `EpisodeRowView`: collapsed, expanded, batch, movie, a long special title; plus `EpisodeRowSkeletonView`.
- `ResolutionRowView`: FHD available, HD available, SD unavailable.

---

## 8. Implementation steps

### Phase 0: shared prerequisites

1. [x] **Shared moves:** `common/data/mapper/ReleaseDateDTOMapper.kt` (Home imports it) + `ReleaseDateDTOMapperTest`; `common/ui/util/ContentWidth.kt` (All Shows uses it); `common/ui/navigation/FullScreenNavKey.kt`; the shared strings `cd_navigate_back`, `cd_expanded`, `cd_collapsed`.
   **Done when:** `./gradlew testDebugUnitTest detekt` pass, and All Shows' list is still centered at 720 dp on an expanded preview.
2. [x] **Navigator:** single-top push and tab reselection ([§5.1](#51-shared-code)) + `NavigatorTest` cases.
   **Done when:** `./gradlew testDebugUnitTest --tests "*NavigatorTest"` passes.

### Phase 1: bookmarks data and domain

3. [x] **DTOs and serializer:** `BookmarkDTO`, `BookmarkListDTO`, `BookmarkListSerializer` + test.
   **Done when:** `BookmarkListSerializerTest` passes.
4. [x] **Data source, mappers, repository, DI:** `BookmarksLocalDataSource` (`BOOKMARKS_DATA_STORE`), both mappers, `BookmarkRepositoryImpl`, `BookmarksModule` with the named DataStore provider, `AppModule` includes it; tests.
   **Done when:** the data source, mapper and repository tests pass, and `KoinModulesTest`'s `verify()` passes.
5. [x] **Domain:** `Bookmark`, `BookmarkRepository`, `ObserveIsBookmarkedUseCase`, `ToggleBookmarkUseCase`, `FakeBookmarkRepository`, tests.
   **Done when:** both use-case tests pass and the `architecture` §1 domain-purity grep prints nothing.

### Phase 2: details, non-UI

6. [x] **Fixtures:** capture and trim the six files in [§7](#7-testing) (at most six requests, one second apart); add their `SubsPleaseFixtures` constants.
   **Done when:** each file has the entries listed in §7.
7. [x] **DTOs:** `ShowPageDTO`, `ShowEpisodesDTO` (+ `KeyedEpisodesSerializer`), `EpisodeDTO`, `DownloadDTO`, `ShowEpisodesDTOTest`.
   **Done when:** `ShowEpisodesDTOTest` passes.
8. [x] **Parser:** `ShowPageHtmlParser` + test.
   **Done when:** the parser tests pass and the `tech-stack` Ksoup grep prints nothing.
9. [x] **Domain models and repository interface:** `ShowOverview`, `ShowEpisodes`, `Episode`, `Download`, `Resolution`, `ShowDetailsRepository`.
   **Done when:** it compiles and the domain-purity grep prints nothing.
10. [x] **Mappers:** `ShowPageDTOMapper`, `ShowEpisodesDTOMapper`, `EpisodeDTOMapper`, `DownloadDTOMapper` + tests.
    **Done when:** the four mapper tests pass.
11. [x] **Service:** `ShowDetailsService` + test.
    **Done when:** the request and decoding assertions pass.
12. [x] **Repository:** `ShowDetailsRepositoryImpl` with the cache + test.
    **Done when:** every repository case in §7 passes.
13. [x] **Use cases:** `GetShowOverviewUseCase`, `GetShowEpisodesUseCase`, `FakeShowDetailsRepository`, tests.
    **Done when:** both use-case tests pass.
14. [x] **DI:** `DetailsModule`, `AppModule`, `KoinModulesTest`.
    **Done when:** `./gradlew testDebugUnitTest --tests "*KoinModulesTest"` passes.
15. [x] **Phase gate:** `./gradlew testDebugUnitTest`, `./gradlew detekt`, `./gradlew assembleDebug`, the `architecture` §1 and `tech-stack` greps.
    **Done when:** all pass with zero Detekt findings.

### Phase 3: UI

16. [x] **Resources:** the strings and plural in [§5.8](#58-resources).
    **Done when:** it builds and the `conventions` §2.5 grep prints nothing new.
17. [x] **Presentation models and mappers:** `DetailsHeaderUIModel`, `DownloadsUIState`, `DownloadSource`, `EpisodeUIState`, `ResolutionUIModel`, `ResolutionTone`, the four mappers; their tests.
    **Done when:** the mapper and `DownloadsUIStateTest` tests pass.
18. [x] **MVI and ViewModel:** `DetailsState`, `DetailsIntent`, `DetailsEffect`, `DetailsViewModel`, `DetailsViewModelTest`.
    **Done when:** every ViewModel case in §7 passes.
19. **Components:** `BookmarkButtonView`, `DetailsHeaderView`, `SynopsisCardView`, `DownloadsHeaderView`, `EpisodeGroupHeaderView`, `ResolutionRowView`, `EpisodeRowView`, each data component with its skeleton, all with `@AdaptivePreviews`.
    **Done when:** the previews render and `./gradlew detekt` is clean.
20. **Screen and Route:** `DetailsScreen` (top bar with the fading title, shared list helper, partial-load skeleton, error / empty / limit note, `PullToRefreshBox`) with its preview provider; `DetailsRoute` (link opening, copy, share, snackbars in a scope).
    **Done when:** all nine preview states render in light and dark, compact and expanded.
21. [x] **Navigation and callers:** `DetailsNavKey`, `DetailsNavigation`, `AppEntryProvider(navigator)`, the bar hiding in `AppNavigation`, `NavKeySerializationTest`; Home, Schedule and All Shows switched to non-null `onOpenDetails` without `canOpenShow` ([§5.9](#59-callers-home-schedule-all-shows)), previews updated.
    **Done when:** `./gradlew testDebugUnitTest` passes and, after `android run --apks=app/build/outputs/apk/debug/app-debug.apk`, tapping a Home card, a Schedule card and an All Shows row each opens Details.
22. **Emulator pass** of [§9](#9-acceptance-criteria): open Sora wa Akai Kawa no Hotori (no batch), Sousou no Frieren (batch), A3! Season Autumn & Winter (360p / 540p, "&"), One Piece (52 entries, specials, limit note), Bakuten!! Movie (one entry), Paw and Palaces (placeholder synopsis); magnet with and without a torrent app (copy link); share; open in web; the bar hiding and the bottom inset of the last row with gesture and 3-button navigation.
    **Done when:** every navigation, downloads and links criterion in §9 is checked. If the last row sits under the system bar after the suite hides, add `Modifier.windowInsetsPadding(WindowInsets.navigationBars.only(WindowInsetsSides.Bottom))` to the list's bottom content padding and note it in the plan.

### Phase 4: docs and verification

23. [x] **Skills** (done 2026-11-19, separately from the implementor): `structure` §7 "Current repo state" (Details built, `feature/bookmarks` without `presentation/` yet, `FullScreenNavKey` exists, the two `common/` moves); `architecture` §3: "`Navigator.goBack()`" instead of "`NavigatorExtensions.kt` adds `Navigator.navigateBack()`"; `structure` §2 row for `common/ui/util/ContentWidth.kt` if wanted.
    **Done when:** the skills describe what was built.
24. [x] **Docs** (done 2026-11-19, separately from the implementor):
    - **`docs/api.md`:** §3.5 gains D1–D4 and D10–D12 (and its "server's zone" sentence becomes "a per-show offset"); §4.2 gains D5–D9 (trailing slash, 404, placeholder synopsis, page cover newer than the schedule's); §7 notes that the `sid` can be reused after the page cache expires.
    - **`docs/context.md`:** §3.5: slug-only input (drop "Callers may also pass the title and cover"), bookmark as a hero button, 1080p-first order, newest episode expanded, empty message only when there are no batches either, the 52-entry note, pull-to-refresh; §4.4: order 1080p → 720p → SD, the `sid` stays in `data`, URLs built in `data`; §5: `BookmarkDTO` / `BookmarkListDTO` in `bookmarks.json`; §7 decision 2: "`ACTION_VIEW` chosen for v1 (Details)".
    - **`CLAUDE.md`** "Current state": Details is the fourth feature and the first pushed screen; cards and rows open it; `feature/bookmarks` has data and domain; the Bookmarks tab stays disabled.
    
    **Done when:** the docs describe the shipped behaviour.
25. **Final verification**, in order:
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
    Then the `architecture` §1 greps, the `structure` §7 checks, the `tech-stack` verification greps (Ksoup included), the `conventions` §2.5 grep, and [§9](#9-acceptance-criteria) on an emulator.

    **Done when:** tests and Detekt pass with zero findings, the `unstable` grep shows nothing for Details' Screen and Views (the known `ObserveAsEvents` line excepted), every grep prints nothing, and every acceptance criterion is checked.

---

## 9. Acceptance criteria

**Navigation:**
- [ ] Tapping an upcoming card or a release row on Home, a card on Schedule, or a row on All Shows opens that show's Details; each has a ripple, and All Shows rows have a chevron.
- [x] A fast double tap opens one Details, not two (one back press returns).
- [x] On a phone (portrait and landscape) the bottom bar slides away on Details and comes back on return. On a tablet the rail stays, with the originating tab selected; tapping that tab returns to its root.
- [ ] The back arrow, system back and predictive back return to the previous screen with its scroll position.
- [x] Switching tabs on a tablet and coming back shows the same Details (no request).

**Header:**
- [x] First open shows a shimmering skeleton (poster, title lines, button, synopsis, real "Episodes & Downloads" title, episode cards). No spinner.
- [x] The header (cover, title, bookmark, synopsis) appears while the downloads area still shimmers.
- [x] Titles are decoded: "A3! Season Autumn & Winter", "BanG Dream! It’s MyGO!!!!!", "Dark Machine – The Animation"; long titles wrap up to 4 lines.
- [ ] The hero sits on the plain page surface, like Home: no backdrop, scrim or tint (user, 2026-11-19).
- [x] Scrolling past the hero fades the show title into the top bar.
- [x] The synopsis shows 3 lines and "Read more" when longer; "Read less" collapses it; short synopses show no toggle; Paw and Palaces shows "No synopsis yet."

**Bookmark:**
- [x] "Bookmark" turns into a filled "Bookmarked" button on tap and back on a second tap; the state survives leaving and reopening the show and restarting the app.
- [ ] TalkBack reads the button's state and "Add to bookmarks" / "Remove from bookmarks".

**Downloads:**
- [ ] Episodes are newest first; every row starts collapsed (user, 2026-11-19); tapping a row expands or collapses it; several can be open.
- [x] Frieren shows "BATCHES" with "Episodes 01–28" above "EPISODES"; shows without batches have no group headers.
- [x] Each expanded row lists 1080p FHD, 720p HD and SD (e.g. "480p SD"; A3's episodes "360p SD", its batch "540p SD"), each with Magnet, Torrent and XDCC.
- [x] One Piece shows 52 entries including "Egghead SP11" and "Episode 1143 (v2)", the header count "52 EPISODES", and the "latest 52 releases" note at the end.
- [x] Bakuten!! Movie shows one expanded "Movie" row.
- [x] Release lines read like "Nov 17, 2026 · 12:38 PM" in the device zone (an hour earlier than the real release in winter, as reported).

**Links:**
- [ ] Magnet opens the installed torrent client; without one, a snackbar "No torrent app found" offers "Copy link", which copies the magnet (Android 12 and below also show "Link copied").
- [x] Torrent opens the nyaa.si `.torrent` URL in the browser (or a torrent app).
- [ ] XDCC opens `https://subsplease.org/xdcc/?search=…` showing that file.
- [x] Share opens the share sheet with the title and `https://subsplease.org/shows/<slug>/`.
- [x] Open in web opens `https://subsplease.org/shows/<slug>/`.

**Errors and refresh:**
- [x] Airplane mode + opening a show → full-screen error with Retry and a back arrow; Retry after reconnecting loads it.
- [x] Pull-to-refresh sends one show-page and one `f=show` request; reopening the same show within 10 minutes sends none.
- [x] Airplane mode + pull-to-refresh → content kept, error snackbar.

**Appearance:**
- [x] Medium and expanded windows: the content column is centered and at most 720 dp wide; the poster is 128 dp.
- [x] Light and dark themes: pills, buttons and the hero scrim are readable.
- [x] Font scale 1.3: source buttons wrap rather than truncate; no clipped pill, title or button text.
- [ ] TalkBack: headings for the title, "Synopsis", "Episodes & Downloads" and the groups; episode rows read their state and "Show downloads"; source buttons read e.g. "Magnet, 1080p FHD, Episode 20"; the skeleton reads "Loading".

---

## 10. Open questions

**Blocking:** none. Every question has a recommendation the implementor can follow. Only [Q4](#10-open-questions) would need the user, and only if they want Custom Tabs (a new library).

| # | Blocks? | Question | Recommendation |
|---|---|---|---|
| Q1 | No | Episode navigation: the design's tabs or the spec's expandable list? | **Expandable list, all rows collapsed on open** (the user changed it from "newest expanded", 2026-11-19) ([§2.5](#25-downloads-header-and-groups)). Tabs fail at 52 entries and are pointless for one; the list handles both. |
| Q2 | No | Bookmark: hero button (design) or top-bar icon (spec)? | **Hero button.** More discoverable and labelled; the bar keeps Share and Open in web. **Alternative:** add a bar icon that appears once the hero scrolls away. |
| Q3 | No | Render the header from the caller's title and cover (context.md §3.5)? | **No: slug only** (`conventions` §5.1). The skeleton covers one ~0.4 s page fetch, then the 10-minute cache makes reopening instant; Coil's memory cache usually has the cover already. **Compliant alternatives for later:** the Bookmarks plan can pre-fill from the stored bookmark (`ObserveBookmarkUseCase(slug)`), which also helps offline; a shared in-memory "show preview" cache filled by click handlers is possible but couples features through a side effect. |
| Q4 | Only if the user wants Custom Tabs | Custom Tabs or the external browser for Open in web / Torrent / XDCC (context.md §7, decision 2)? | **Plain `ACTION_VIEW`** for v1: no new library, and magnet / torrent links must go to other apps anyway. Custom Tabs needs `androidx.browser` approved in `tech-stack` first. |
| Q5 | No | `structure` §1 says every feature has all four subpackages; `feature/bookmarks` gets no `presentation/` yet. OK? | **Yes, temporarily.** The domain must exist for Details now (context.md §6); the Bookmarks plan adds `presentation/`. Step 23 notes it in `structure` §7. **Alternative:** put the bookmark store in `common/` now and move it later (two moves instead of none). |
| Q6 | No | Where is the bookmarks `DataStore` provided? | **`BookmarksModule`, `@Named("bookmarks")`.** `CommonModule` can't import a feature DTO, and Koin keys by class, so it must be qualified before Settings adds `DataStore<Preferences>`. |
| Q7 | No | Which `DomainError` for a failed bookmark write (`IOException`)? | **`DomainError.Unknown(e)`** → "Something went wrong. Try again." `safeApiCall` would say "Network", which is wrong for disk. **Alternative:** a new `DomainError.Storage` with its own message, if storage errors become common. |
| Q8 | No | Keep the nullable `onOpenDetails` / `canOpenShow` hooks in Home, Schedule and All Shows? | **Remove them** now that the target exists (dead branches); Views keep their nullable `onClick`. `onOpenSettings` stays nullable. |
| Q9 | No | Hide the navigation on Details everywhere, or only the bottom bar? | **Only the bottom bar (compact);** keep the rail, as M3 does for detail pages with rails. Reselecting the selected tab pops to its root ([§5.1](#51-shared-code)). |
| Q10 | No | Two panes on expanded windows (`SupportingPaneScaffold`: show info + downloads)? | **Not in v1:** a centered 720 dp column, like All Shows. The screen's one-column design and a single skeleton stay simple. If wanted later: `SupportingPaneScaffold` with show info as the main pane and downloads as the supporting pane, sharing the item builders. |
| Q11 | No | Treat "Synopsis will be added later" as missing? | **Yes** (D6): our own "No synopsis yet." is consistent and localizable; the check lives in the data mapper. |
| Q12 | No | Long-press to copy a link, poster zoom, "Copy IRC command"? | **Not in v1.** The spec offers "Copy link" when opening fails; the IRC command needs `search.php` with a `Referer` (api.md §5). |
| Q13 | No | Mention SubsPlease's 52-entry cap? | **Yes, a one-line note** after the list when 52 entries came back (D1). Otherwise users assume One Piece starts at 1133. |
| Q14 | No | Resolution order: 1080p first (design) or SD first (context.md §4.4)? | **1080p first** (design, the common choice). context.md §4.4 is updated (step 24). |
| Q15 | No | Update a bookmark's stored title / cover when Details loads newer ones (D7: the page cover can replace a placeholder)? | **In the Bookmarks plan**, as `UpdateBookmarkedShowUseCase` called after the overview loads; it's only visible once the Bookmarks grid exists. |
| Q16 | No | `architecture` §3 names `Navigator.navigateBack()`; the code has `Navigator.goBack()`. | **Use `navigator::goBack`**; fix the skill text (step 23). An extension that only calls `goBack()` adds nothing. |
| Q17 | No | Backdrop below Android 12, where `Modifier.blur` does nothing? | **Superseded:** the backdrop was dropped entirely at the user's request (2026-11-19). |
| Q18 | No | How to label revisions ("1216v2") and named specials? | **"Episode 1216 (v2)"**; specials and named entries as SubsPlease writes them. Revisions stay separate rows next to the original (both are real files). |
| Q19 | No | Default expansion after process death? | **Default again** (everything collapsed): expansion lives in the ViewModel, which doesn't survive process death; not worth `SavedStateHandle`. |
| Q20 | No | Unknown `res` values (e.g. "2160")? | **Ignore** (api.md §3.6). The site would put them in the 1080p slot; none exist today (D4). |
| Q21 | No | Deep links (`https://subsplease.org/shows/<slug>/` opening Details)? | **Out of scope.** `DetailsNavKey(slug)` makes it a small follow-up (an intent filter plus a synthetic back stack, `navigation-3` deep-link recipe). |
| Q22 | No | Episodes with no usable downloads? | **Show the row** with all three slots disabled, like the site; it's a real release whose links are missing. |

---

## 11. Implementation notes

- **Changes after implementation (user, 2026-11-19).** Three QoL changes, which override the rows above where they disagree:
  - **All rows start collapsed**, the newest episode and batches included. `toDownloadsUIState(expandedKeys: Set<String> = emptySet())` no longer picks a default; the rows the user expanded are still kept across a refresh or retry.
  - **Plain background:** `DetailsHeaderView` lost the blurred cover backdrop, the scrim and the `surfaceContainerLowest` base, so the page has the same surface as Home.
  - **Title in the top bar:** the show title appears (fading in) as soon as the page is parsed, instead of only once the hero scrolls away. The hero keeps its own large title.
- **Open questions:** all twenty-two use their recommendations, as the requester decided on 2026-11-19. In particular: plain `ACTION_VIEW`, no `androidx.browser` (Q4); `feature/bookmarks` has `data`, `domain` and `di` but no `presentation/` yet, a **temporary exception** to `structure` §1 ("all four subpackages") until the Bookmarks plan adds it (Q5); a failed bookmark write is `DomainError.Unknown` and shows the generic error (Q7); `onOpenDetails` is non-null in Home, Schedule and All Shows and `canOpenShow` is gone (Q8); one centered 720 dp column on wide windows (Q10).
- **Step 1, All Shows width:** `AllShowsLayout.horizontalPadding` now calls `centeredContentPadding(maxWidth, margin)`, the same formula as before (`max(margin, (maxWidth - 720.dp) / 2)`), so the expanded layout can't have changed; the expanded preview wasn't rendered (see Previews below), and the emulator is a phone.
- **Episode cap constant (§4.3, §5.2):** the 52-entry cap is `ShowEpisodes.EPISODE_LIMIT` in the domain model instead of a private `SHOW_EPISODE_LIMIT` in `ShowEpisodesDTOMapper`. The limit note ("…the latest 52 releases.") needs the number in presentation, which can't see `data`; one constant serves both.
- **Cache pruning (§4.5):** each map is pruned when it's written (pages when a page is cached, episodes when episodes are), not both maps on every write. Writing a show's episodes then never drops the page entry whose `sid` it may reuse later.
- **`DownloadsUIState.Content.expandedKeys`:** a computed property the ViewModel reads to keep the expanded rows across a refresh or retry.
- **Episode titles (§2.6):** a decimal label ("12.5") also counts as numeric ("Episode 12.5"); `Episode.toEpisodeKey(isBatch)` is public in `EpisodeUIModelMapper.kt`, so `ShowEpisodesUIModelMapper` picks the default row with the same key format.
- **Bookmark button (§2.3):** `heightIn(min = 48.dp)` rather than a fixed height, so the label never clips at large font scales; the skeleton is a fixed 48 dp pill.
- **Header skeleton (§2.2):** no tonal base either (the plan's "no backdrop"): a background inside a `SkeletonView` would shimmer as a whole block. The hero's `surfaceContainerLowest` appears with the content.
- **Route (§2.8):** `Context.openLink` names its catch parameter `ignored`, the name Detekt's `SwallowedException` allows, so there is no suppression. Snackbars run in `rememberCoroutineScope()`. "Link copied" shows below API 33.
- **ViewModel:** `slug` is a plain `@InjectedParam` constructor parameter wrapped once in `ShowSlug`. `state` is `combine(_state, observeIsBookmarked(…)).stateIn(…)`, so a synchronous reduction (`SynopsisToggled`, `EpisodeToggled`) reaches `state` on the next dispatch; the tests advance the dispatcher before reading it.
- **Screen file:** 10 top-level functions (Detekt allows 11): besides the plan's helpers, `LazyListScope.groupHeader` and `LazyListScope.episodeItems`. `EpisodeLimitNote` stayed private.
- **Fixtures (step 6):** six requests on 2026-11-19, 08:17:56–08:18:01 UTC, one second apart, User-Agent `BakaMitai/1.0 (Android)`, `tz=America/New_York`. The show page request also carried `?tz=America/New_York` (the implementor's rule: every SubsPlease request sends `tz`); WordPress ignores it, and the page matched §4.2. Magnets are trimmed to their `xt` part, like the earlier fixtures. The offsets in this capture differ from D2's examples (Frieren and Conan entries now say `+0000`, A3 `-0500`/`-0400`, Bakuten!! Movie `-0400`), which supports D2: never trust the offset.
- **`docs/context.md` working-tree edit:** while this plan was built, an uncommitted edit of context.md §3.5 says the synopsis is "Hidden when the show has none". This implementation follows §2.4 (the card stays, with "No synopsis yet."). One of the two needs to change.
- **Previews (steps 19–20 aren't ticked):** Android Studio's renderer (`android studio render-compose-preview`) returned a stale "PreviewParameterProvider couldn't be loaded" error for `DetailsStatePreviewProvider`, even when asked for other previews, the class-loader cache the All Shows notes describe. The four preview providers construct their 9, 3, 3 and 5 values (checked reflectively in a throwaway unit test, since deleted); none was looked at rendered.
- **Checked on emulator-5554 (API 37, compact, dark, 3-button navigation):** a Home upcoming card, a Schedule card and an All Shows row (chevrons and ripples) each open Details; a fast double tap opens one Details and one back press returns; the bottom bar slides away and comes back; the last row ends above the system bar; the title fades into the bar after the hero; the partial skeleton (header placeholders, real "Episodes & Downloads", four row skeletons) during the transition; the newest episode expanded, "BATCHES" / "EPISODES" groups on *86 – Eighty Six* (entity-decoded en dash); bookmark toggle; "Read more" / "Read less"; Magnet without a torrent app → "No torrent app found" → "Copy link" copied the magnet (Android's own clipboard confirmation). **Not checked:** tablet / rail and tab reselection, landscape, light theme, font scale 1.3, TalkBack, gesture navigation, Share, Open in web, Torrent and XDCC links, error and empty states, pull-to-refresh request counts, Android 9–12 behaviour, the specific shows listed in step 22. That's the separate QA pass (step 22 isn't ticked).
- **Docs (steps 23–24) are left to the requester:** the implementor edits only the plan file, so the `structure` / `architecture` skill updates, `docs/api.md`, `docs/context.md` and `CLAUDE.md` weren't touched. `docs/api.md` and `docs/context.md` already had uncommitted edits (staged by someone else during this work); they weren't modified.
