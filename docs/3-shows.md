# 3. All Shows

> **Status:** Implemented · **Design:** [Google Stitch project `5201581584167289517`, screen `c5865d74b6d2449ea3c1fdf68bf4af21` ("All Shows Index")](https://stitch.google.com/projects/5201581584167289517?node-id=c5865d74b6d2449ea3c1fdf68bf4af21) · **Planned:** 2026-11-19
> **Spec:** [context.md §2](context.md#2-navigation-shell), [§3.3](context.md#33-all-shows), [§4.3](context.md#43-all-shows), [§6](context.md#6-suggested-feature-breakdown), [§7](context.md#7-open-decisions) · **API:** [api.md §4.1](api.md#41-all-shows-list), [§7](api.md#7-client-guidelines)
> **Depends on:** [1-home.md](1-home.md) and [2-schedule.md](2-schedule.md) (both implemented): the navigation shell, `TopLevelAppBarView`, `Show` / `ShowSlug`, the skeleton pieces, `ErrorView`, `safeApiCall`, `DispatcherProvider`, the shared `HttpClient`.

**Design source.** The design is a Google Stitch project, not Figma or Sketch. Only one screen exists: **`c5865d74b6d2449ea3c1fdf68bf4af21`** in project **`5201581584167289517`**, a 390×1876 dark-mode frame. It was captured as a render (`screen.jpg`, 743×3573) and a condensed copy of its HTML/Tailwind, whose header lists every sample row and Stitch's own prompt. Every "node" below names that screen plus the HTML section inside it: `<header>`, the sticky control deck (`#directory-search`, `#search-clear-btn`, the voice button, `#alpha-chips`), `section.directory-section` (its sticky letter header, the rows card, `.anime-item`, the divider), `#no-results-state`, the stats footer, `#alpha-rail` and `<nav>`. Loading, error, light-mode and tablet states aren't in the design; they're proposed in [§2.8](#28-states-not-in-the-design-all-proposed).

**How design and spec are reconciled** (same rule as Home and Schedule): `context.md` decides **behaviour and data**, the Stitch design decides **visual treatment**. Anything the data can't back is left out, never invented. Every disagreement is in [§3](#3-design-vs-spec).

**Decided (2026-11-19, by the user): the HTML parser is Ksoup** (`com.fleeksoft.ksoup:ksoup`), approved for the `data` layer only. This settles [context.md §7](context.md#7-open-decisions) open decision 1. All parsing still sits behind one class, `AllShowsHtmlParser`, so the parser stays swappable ([§6](#6-dependencies), step 1).

---

## 1. Summary

All Shows is an A–Z catalogue of **every show on SubsPlease** (1,263 titles on 2026-11-18). Each row shows only the title. Rows are sorted case-insensitively and grouped under **sticky letter headers** ("#" for titles starting with a digit or symbol, then "A" … "Z"), each with the number of shows in it. A **search field** pinned under the top bar filters the list locally as the user types (case-insensitive substring). A pinned row of **letter chips** jumps to a section and highlights the section currently at the top of the list. A footer shows how many shows are listed.

The catalogue is scraped from the `/shows/` HTML page once per process and kept in memory; pull-to-refresh fetches it again.

**Entry point:** the **All Shows** tab of the `NavigationSuiteScaffold`, enabled by this plan.

**In scope:**
- the `shows` feature (`feature/shows/`, all four layers)
- **adding Ksoup** to the catalog and the skills ([§6](#6-dependencies))
- a shared `EmptyStateView` in `common/ui/component/emptystate/`, extracted from Schedule's private empty view, which this screen's "No anime found" also needs ([§5.1](#51-shared-code))
- enabling the All Shows tab (`TopLevelDestination`, `AppEntryProvider`)
- doc and skill updates for the decision and the live findings in [§4.2](#42-api-gaps-found-live)

**Out of scope, with hooks left for later plans:**
- **Details.** Rows aren't clickable and have no chevron until the Details plan passes `onOpenDetails` ([Q7](#10-open-questions)).
- **Settings.** The gear stays hidden.
- **Design extras without data or spec:** voice search, the right-edge A–Z rail, "Index letter" captions, "Updated hourly" ([§3](#3-design-vs-spec)).

---

## 2. Design breakdown

### 2.0 Token mapping

The mapping in [1-home.md §2.0](1-home.md#20-token-mapping) applies unchanged; **no new theme work is needed**. Extra tokens this screen uses:

| Design (Tailwind/HTML) | Compose |
|---|---|
| `bg-surface-container` search pill, `focus-within:bg-surface-container-high` | `colorScheme.surfaceContainer`, `surfaceContainerHigh` while the field is focused |
| `text-primary` search icon, `placeholder:text-outline` | `colorScheme.primary`, `colorScheme.outline` |
| Active chip `bg-primary text-on-primary`; inactive `bg-surface-container-high text-on-surface-variant` | `primary` / `onPrimary`; `surfaceContainerHigh` / `onSurfaceVariant` (Schedule's filter-chip colors) |
| Letter badge `bg-primary/15 text-primary` / `bg-secondary-container/20 text-secondary` | `primary.copy(alpha = 0.15f)` / `primary`; `secondaryContainer.copy(alpha = 0.2f)` / `secondary` (alphas as named `private const`s) |
| Count pill `bg-surface-container-highest text-secondary` | `surfaceContainerHighest` / `secondary` |
| Rows card `bg-surface-container-low rounded-2xl p-1` | `surfaceContainerLow`, `MaterialTheme.shapes.large` (24 dp, DESIGN.md `rounded-2xl`), 4 dp (`spacing.xs`) inner padding |
| Divider `h-px bg-surface-container mx-space-md` | `HorizontalDivider(thickness = DividerDefaults.Thickness, color = surfaceContainer)`, inset `spacing.md` |
| Row title `font-title-md font-semibold` | `typography.titleMedium` (SemiBold 16 sp) |
| Letter `font-headline-sm font-bold` | `typography.titleLarge` (Bold 18 sp; DESIGN.md has no bold 18 sp headline) |
| Count / chip / footer `label-sm` / `label-md` | `typography.labelSmall` / `labelMedium` |
| `gap-space-lg` between sections, `gap-space-xs` header → card | `spacing.lg`, `spacing.xs` |
| Material Symbols `search`, `close`, `search_off`, `chevron_right`, `mic` | `Icons.Outlined.Search`, `Icons.Outlined.Close`, `Icons.Outlined.SearchOff`, `Icons.AutoMirrored.Outlined.KeyboardArrowRight` (mirrors in RTL); `mic` is omitted. All present in `material-icons-core`/`-extended` 1.7.8 (checked in the Gradle cache). |

Dimensions as private `Dp` constants in their component files: `LetterBadgeSize = 32.dp`, `RowVerticalPadding = 14.dp` (design `py-3.5`), `LetterChipMinWidth = 40.dp`, `MaxContentWidth = 720.dp` (proposed, §2.8).

### 2.1 Top app bar, search deck and navigation suite

Nodes: `<header>`, the sticky control deck, `<nav>`.

- **Layout:** the shared top bar ("BakaMitai" / "ALL SHOWS", settings action), and under it, pinned in the bar's `bottomContent` slot (the design's `sticky top-0` deck):
  1. the search field (§2.2), `spacing.xs` above and `spacing.sm` below
  2. the letter chip row (§2.3), only when the list has sections
  
  The deck uses the list's horizontal content padding (§2.8) so the field, the chips and the rows line up on every width.
- **Components:** `common/ui/component/toplevelappbar/TopLevelAppBarView` with `subtitle = all_shows_app_bar_subtitle` and `bottomContent = { SearchDeck(…) }`. Navigation: the unchanged `NavigationSuiteScaffold`; the **All Shows** item becomes enabled.
- **States:** the deck is shown during loading and with content; it's hidden in the full-screen error state (nothing to search). The settings action stays hidden until Settings exists.
- **Interactions:** `TopAppBarDefaults.pinnedScrollBehavior(appBarState)`, with `appBarState = key(state.query) { rememberTopAppBarState() }`: a new query starts the list at the top, so the bar tint starts over too (Schedule's per-day pattern). System back returns to Home (`Navigator.goBack()`).
- **Theming:** the bar's existing `surface` → `surfaceContainer` tint covers the deck. The design's `backdrop-blur-xl` and shadows are dropped ([§3](#3-design-vs-spec) row 14).
- **Accessibility:** unchanged from Home and Schedule.

### 2.2 Search field

Nodes: `#directory-search`, `#search-clear-btn`, the voice button.

- **Layout:** full width of the deck, pill shape, 56 dp high (`SearchBarDefaults.InputFieldHeight`, the M3 size; the design's 52 dp is not kept, [§3](#3-design-vs-spec) row 13).
- **Components:** `feature/shows/presentation/component/searchfield/ShowSearchFieldView.kt`: a `Surface(shape = CircleShape, color = …)` holding `SearchBarDefaults.InputField(query, onQueryChange, onSearch, expanded = false, onExpandedChange = {}, …)` (M3 1.4.0, `@OptIn(ExperimentalMaterial3Api::class)` on the function). `onSearch` hides the keyboard (`LocalSoftwareKeyboardController`).
  - leading: `Icons.Outlined.Search` in `primary`
  - trailing: when `query.isNotEmpty()`, an `IconButton` with `Icons.Outlined.Close` (`onSurfaceVariant`) → `onClearClick`
  - placeholder: `all_shows_search_placeholder` "Search anime title…" in `outline`
  - text `onSurface`; `singleLine`; IME action Search
- **Content:** `query: String` from `AllShowsState`. Typing sends `AllShowsIntent.QueryChanged(text)`; the ViewModel updates the state synchronously, so the cursor never jumps.
- **States:** container `surfaceContainer`, `surfaceContainerHigh` while focused (the field's `interactionSource.collectIsFocusedAsState()`, animated with `animateColorAsState`). Enabled while loading too: a query typed before the list arrives is applied when it does.
- **Interactions:** the clear button sends `QueryCleared` and keeps focus in the field (design script). Starting to drag the list clears focus, which hides the keyboard (proposed): `LaunchedEffect(listState) { snapshotFlow { listState.isScrollInProgress }.filter { it }.collect { focusManager.clearFocus() } }` in the Screen.
- **Omitted:** the voice (mic) button ([§3](#3-design-vs-spec) row 6, [Q3](#10-open-questions)). The 1 px border and `shadow-md` are dropped.
- **Accessibility:** `InputField` announces itself as a search field and reads the placeholder as its hint. Clear button: `cd_all_shows_clear_search` "Clear search", 48 dp target (`IconButton`).

### 2.3 Letter chips (quick-jump), and the dropped A–Z rail

Nodes: `#alpha-chips` (kept), `#alpha-rail` (dropped).

**Decision: chips only.** The rail is dropped ([§3](#3-design-vs-spec) row 4, [Q2](#10-open-questions)):
- Its 26 letters are 10 px text with 2 px padding (~14 dp each). The M3 / `best-practices` §5.2 minimum is 48 dp, and 26 × 48 dp can't fit a phone's height.
- It lists letters with no shows, which would do nothing (and every letter A–Z has shows today, [§4.2](#42-api-gaps-found-live) A6, so it duplicates the chips exactly).
- It overlaps the rows and reserves 36 dp of width (`pr-9`) on a 360 dp phone, where 243 titles already need two lines.
- A scrubbing rail (drag to scrub) is custom gesture and accessibility work for no new capability. TalkBack users get heading navigation over the sticky headers instead.

Chip row:
- **Layout:** `LazyRow`, `contentPadding = PaddingValues(horizontal = h)` (the list's padding), `horizontalArrangement = spacedBy(spacing.xs)`, `Modifier.selectableGroup()`. Private `LetterChipRow` in `AllShowsScreen.kt`, shared with the skeleton (`userScrollEnabled = false`), as Schedule's `DayStripRow` is.
- **Components:** `component/letterchip/LetterChipView.kt`: M3 `FilterChip(selected, onClick, label, shape = CircleShape, border = null)` with Schedule's colors (selected `primary` / `onPrimary`, not selected `surfaceContainerHigh` / `onSurfaceVariant`), `Modifier.widthIn(min = LetterChipMinWidth)`, centered label in `labelMedium`. Plus `LetterChipSkeletonView` (a `FilterChipDefaults.Height` pill block inside a `minimumInteractiveComponentSize()` box, as Schedule's filter-chip skeleton).
- **Content:** one chip per section **currently in the list**: all 27 ("#", A–Z) unfiltered, only the letters with matches while searching. Label = the letter (`IndexLetterUIModel.label`).
- **States:**
  - The **active** chip is the section at the top of the list, not the last tapped chip: `val activeSection by remember(headerIndices, listState) { derivedStateOf { headerIndices.indexOfLast { it <= listState.firstVisibleItemIndex } } }` (reads scroll state late, `best-practices` §1.4). `headerIndices` is `remember(state.sections)` of each header's item index (header + rows, cumulated).
  - When `activeSection` changes, the row scrolls to keep it visible: `LaunchedEffect(activeSection) { chipRowState.animateScrollToItem((activeSection - 1).coerceAtLeast(0)) }`.
  - Hidden when there are no sections (no results).
- **Interactions:** tapping a chip scrolls the list: `scope.launch { listState.animateScrollToItem(headerIndices[i]) }`. This is UI-only scroll state handled in the Screen, like Schedule's strip auto-scroll; it changes nothing in `State`, so it's not an Intent ([Q9](#10-open-questions)). The design's `active:scale-95` is dropped; M3's ripple remains.
- **Accessibility:** 48 dp targets (the chip's minimum interactive size). Each chip has `Modifier.semantics { onClick(label = "Jump to A") { … } }` via `cd_all_shows_jump_to` with the letter's description, so "#" reads "Jump to Numbers and symbols". `FilterChip` announces the active one as selected.

### 2.4 Letter header (sticky)

Node: the `sticky top-28` header inside each `section.directory-section`.

- **Layout:** a `Row` 40 dp high (32 dp badge + `spacing.xs` vertical padding), `verticalAlignment = CenterVertically`: the letter badge, then `Spacer(Modifier.weight(1f))`, then the count pill. It sticks at the top of the list (`LazyListScope.stickyHeader`) under the pinned deck. `spacing.xs` between it and the first row.
- **Components:** `component/letterheader/LetterHeaderView.kt` (+ `LetterHeaderSkeletonView`: a 32 dp circle block and a pill block of the count's height).
  - Badge: `Box(Modifier.size(LetterBadgeSize).background(badgeColor, CircleShape), contentAlignment = Center)` with `Text(letter.label, style = titleLarge, color = accent)`.
  - Count pill: `Surface(shape = CircleShape, color = surfaceContainerHighest, contentColor = secondary)`, padding 10 dp × 2 dp (DESIGN.md status badge), `Text(labelSmall)`.
- **Content:** `letter: IndexLetterUIModel` ("#", "A" …) and `countLabel` (plural `all_shows_section_count`: "1 SHOW", "14 SHOWS", stored uppercase per DESIGN.md labels). The count is the section's rows **as listed**, so it follows the search.
- **States:** accent by **letter parity**, so it doesn't change while filtering: A, C, E … → primary badge; B, D, F … → secondary badge; "#" → secondary (it precedes primary A). The design alternates by section position, which would flip colors as sections disappear ([§3](#3-design-vs-spec) row 11). The badge glow is dropped.
- **Theming:** opaque `colorScheme.surface` background (the design's `bg-surface/95` without blur), so rows scroll under it cleanly.
- **Omitted:** the "Index letter" caption ([§3](#3-design-vs-spec) row 2).
- **Accessibility:** `Modifier.semantics(mergeDescendants = true) { heading() }`; the badge text carries `contentDescription = letter.description`, so TalkBack reads "A, 14 SHOWS" / "Numbers and symbols, 9 SHOWS", and heading navigation jumps between sections.

### 2.5 Show row and section card

Nodes: the rows card (`bg-surface-container-low rounded-2xl p-1`), `.anime-item`, the divider.

- **Layout:** each row is its own `LazyColumn` item; together a section's rows draw one rounded card. The row's position decides its slice of the card (`ShowRowPosition`):
  - `FIRST`: top corners of `shapes.large` (`shapes.large.copy(bottomStart = ZeroCornerSize, bottomEnd = ZeroCornerSize)`), `spacing.xs` top inner padding
  - `MIDDLE`: no corners
  - `LAST`: bottom corners, `spacing.xs` bottom inner padding, then `spacing.lg` outer bottom padding (the gap to the next section, outside the background)
  - `SINGLE`: all corners, both inner paddings, the outer gap
  
  Inside: `spacing.xs` horizontal inner padding, then the title with `spacing.md` horizontal and `RowVerticalPadding` vertical padding. A divider is drawn above every row except `FIRST` / `SINGLE`.
- **Components:** `component/showrow/ShowRowView.kt`: `ShowRowView(model, position, onClick: (() -> Unit)?, modifier)` + `ShowRowSkeletonView(position, modifier, titleWidthFraction)` (the same card slice, one `SkeletonTextLine(titleMedium)`). When `onClick != null`: the inner row is `clickable(onClick)` clipped to `shapes.small`, and a trailing `Icons.AutoMirrored.Outlined.KeyboardArrowRight` (20 dp, `outline`) is shown. When `null`: no ripple, no chevron.
- **Content:** `ShowRowUIModel(key, slug, title)`; the title in `titleMedium` / `onSurface`, **`maxLines = 2`**, ellipsized ([§3](#3-design-vs-spec) row 9: 243 of 1,263 titles are longer than one line on a 360 dp phone).
- **States:** content only (loading is the skeleton). The design's hover color changes are dropped.
- **Interactions:** none until Details exists; then a tap sends `AllShowsIntent.ShowClicked(slug)`.
- **Accessibility:** one text node per row. Height ≥ 52 dp. When clickable, `onClickLabel` is not needed (the row's text is the label).

### 2.6 Footer

Node: the stats footer banner.

- **Layout:** the list's last item, `spacing.md` padding inside, `spacing.xl` below (the list's bottom content padding), full width of the content.
- **Components:** private `CatalogFooter` in `AllShowsScreen.kt`: `Surface(shape = shapes.large, color = surfaceContainerLow)` with a centered `Text(labelMedium, onSurfaceVariant)`.
- **Content:** `footerLabel`: plural `all_shows_footer_count` "Showing %d shows", the number of rows listed (1,263 unfiltered; the match count while searching). "• Updated hourly" and the pulsing dot are dropped: the catalogue is fetched once per process ([api.md §7](api.md#7-client-guidelines)), so neither is true ([§3](#3-design-vs-spec) row 10, [Q4](#10-open-questions)).
- **States:** shown whenever the list has rows; replaced by §2.7 when nothing matches.

### 2.7 No results

Node: `#no-results-state` (hidden by default).

- **Layout:** a list item under the deck (so pull-to-refresh still works and the field stays), `spacing.xl` vertical padding, centered.
- **Components:** the new shared `common/ui/component/emptystate/EmptyStateView` ([§5.1](#51-shared-code)): a 64 dp `surfaceContainerHighest` circle with `Icons.Outlined.SearchOff` (32 dp, `secondary`), a `headlineSmall` title with `heading()`, a centered `bodyMedium` / `onSurfaceVariant` message limited to 320 dp. No action.
- **Content:** `all_shows_no_results_title` "No anime found", `all_shows_no_results_message` "Try adjusting your spelling or searching by an alternate English or Romaji title." (the design's copy). The design's `outline` icon and message colors become `secondary` / `onSurfaceVariant`, matching Schedule's empty view and its contrast.
- **States:** shown when content is loaded and the filtered list is empty (`state.showNoResults`). The chip row and the footer are hidden then.

### 2.8 States not in the design (all proposed)

| State | Proposal |
|---|---|
| **First load** | A private `AllShowsSkeleton` fills the content area under the bar (`best-practices` §5.3), in **one** `SkeletonView`: a non-scrolling `LetterChipRow` of 10 `LetterChipSkeletonView`s (where the real chips will appear), then the shared list helper (`ShowsList`, `userScrollEnabled = false`) with 2 sections, each a `LetterHeaderSkeletonView` and 6 `ShowRowSkeletonView`s (`FIRST`, 4 × `MIDDLE`, `LAST`) with title widths from a private list (0.7, 0.45, 0.85, 0.6, 0.5, 0.75). The top bar and the search field are real. |
| **Error, no content** | `ErrorView(model = state.error, onRetry = { onIntent(AllShowsIntent.Retry) })` replaces the list; the deck is hidden. An empty or unrecognisable page is `DomainError.InvalidData` → "Something went wrong" with Retry ([§4.1](#41-endpoint)). No pull-to-refresh. |
| **Refreshing** | `PullToRefreshBox` indicator over the list; the list, search and chips stay interactive. The query is kept. |
| **Refresh failed, content present** | Keep the content; snackbar with the error (`AllShowsEffect.ShowError`). The cached catalogue stays. |
| **Empty catalogue** | Can't be shown: a page without shows is a parsing error (the error row above), because SubsPlease always lists shows. |
| **Keyboard open** | The list container gets `Modifier.consumeWindowInsets(padding).imePadding()` so the last rows and the no-results view scroll above the keyboard (the app is edge-to-edge with `adjustResize`). First text input in the app: check on an emulator. |
| **Light theme** | Same color roles; `LightColorScheme` resolves them. |
| **Medium / expanded** | Rail navigation. One column, its horizontal padding `h = max(margin, (maxWidth - MaxContentWidth) / 2)` with `margin` = `spacing.margin` (compact) or `spacing.marginTablet` (from medium), so content is at most 720 dp wide and centered while the whole width still scrolls. The deck uses the same `h` (each measures its own width with `BoxWithConstraints`; both span the same pane). Chips still scroll horizontally (27 × 48 dp > 720 dp). |
| **Rotation / tab switch** | `listState = key(state.query) { rememberLazyListState() }`: rotation keeps the scroll position; a new query starts at the top. The ViewModel (and its catalogue) survives tab switches. |

---

## 3. Design vs. spec

| # | Item | Design | docs/context.md | Recommendation |
|---|---|---|---|---|
| 1 | Sticky letter headers | Letter badge + "Index letter" caption + "14 SHOWS" pill, sticky under the deck | "grouped under sticky letter headers ("A", "B", …)" | **Keep** the sticky badge and the count pill (real, follows the search). |
| 2 | "Index letter" caption | On every header | n/a | **Omit.** The same two words 27 times carry no information and lengthen TalkBack's reading. |
| 3 | "#" section | Sample shows A–D only | Digits and symbols under "#", before "A" (§3.3, §4.3) | **Add "#"** first (9 titles today, all starting with a digit). TalkBack reads it "Numbers and symbols". |
| 4 | Quick-jump | Horizontal chips **and** a right-edge A–Z rail | Not in spec | **Chips only** ([§2.3](#23-letter-chips-quick-jump-and-the-dropped-az-rail), [Q2](#10-open-questions)): the rail's ~14 dp letters break the 48 dp minimum and duplicate the chips. |
| 5 | Chip set and active chip | Only letters with shows; active = last tapped | n/a | **Letters of the sections currently listed** (all 27 unfiltered, a subset while searching); **active = section at the top of the list**, updated while scrolling. |
| 6 | Voice search (mic) | In the search pill | Not in spec | **Omit** ([Q3](#10-open-questions)). The keyboard's own voice input covers it without Activity-result plumbing. |
| 7 | Search | Case-insensitive substring, clear button, "No anime found" | Local, case-insensitive substring (§3.3, §4.3) | **Match.** Trimmed query, title only. Design's empty state kept. |
| 8 | Row chevrons, ripple | On every row | Rows open Details (§2), which isn't built | **No chevron, no ripple** until Details exists, as Home and Schedule ([Q7](#10-open-questions)). The chevron comes with the click. |
| 9 | Title lines | One line, truncated | "only the name" | **Up to 2 lines**, ellipsized: 243 of 1,263 titles (19 %) are longer than one line on a 360 dp phone, and nothing else shows the full title yet. |
| 10 | Footer | "Showing 482 anime series • Updated hourly" + pulsing dot | Fetch once, cache for the process (api.md §7) | **"Showing 1263 shows"**, the live row count (match count while searching). Drop "Updated hourly" and the pulse: neither is true ([Q4](#10-open-questions)). |
| 11 | Badge colors | Alternate primary / secondary by section position | n/a | **Keep the alternation, by letter parity** (A primary, B secondary, …; "#" secondary), so colors don't change while filtering. |
| 12 | Rows card + dividers | Each section's rows in one `surface-container-low` rounded card, 1 px dividers | n/a | **Keep** (`shapes.large`, divider in `surfaceContainer`). |
| 13 | Search field size | 52 dp pill, 1 px border, `shadow-md` | n/a | **M3 `SearchBarDefaults.InputField`, 56 dp**, pill, no border or shadow (tonal layers, as the other plans). |
| 14 | Blur, shadows, glows, `scale` press effects | Throughout | n/a | **Dropped**, per DESIGN.md "Elevation & Depth" and the earlier plans. |
| 15 | Pull-to-refresh | Not shown | Not noted for All Shows; api.md §7: the cache is "invalidated by pull-to-refresh" | **Add it** ([Q8](#10-open-questions)): it's the only way to see a new season's shows without restarting the app. |
| 16 | Settings gear | Visible | Opens Settings (not built) | **Hidden** until Settings exists. |
| 17 | Bottom navigation | All Shows active | 4 tabs | **Enable All Shows.** Bookmarks stays disabled. |
| 18 | Duplicate slugs | n/a | `List<Show(slug, title)>` | **Keep every title** as its own row (12 slugs have 2–3 titles, e.g. "Fruits Basket (2019)" / "S2" / "S3"); drop exact (slug, title) duplicates only ([Q5](#10-open-questions)). |
| 19 | Tablet | n/a | n/a | **One centered column, max 720 dp**, 24 dp margins from medium width. |
| 20 | Loading / error / refresh / light / keyboard | Not in design | Required (§3) | **Proposed in [§2.8](#28-states-not-in-the-design-all-proposed).** |

---

## 4. Data

### 4.1 Endpoint

| Call | Request | Fields used | Quirks to handle |
|---|---|---|---|
| All shows list, [api.md §4.1](api.md#41-all-shows-list) | `GET shows/` (`https://subsplease.org/shows/`), **no query parameters, no `tz`** (the page doesn't depend on the zone) | each `div.all-shows div.all-shows-link > a`: `href`, `title` attribute, link text | Mismatched `<h3>…</h4>` headers (A3). Entities decoded, raw `&` kept (A5). Slug from `/shows/<slug>` with or without a trailing `/`, absolute or relative (A4); slugs are opaque (A7). Several titles per slug (A1). Ignore the page's own headers and order (A8). A page with no usable link is `DomainError.InvalidData`. |

- The body is read as text with `bodyAsText()` (UTF-8 from `Content-Type`), not through content negotiation.
- Parsing ~200 KiB of HTML is CPU work: the repository runs the parser on `DispatcherProvider.default` (`best-practices` §3.2).
- The service and the parser stay separate: `ShowsService` returns the HTML `String`; `AllShowsHtmlParser` is the **only** class that imports Ksoup.

### 4.2 API gaps found live

Checked on **2026-11-18, 23:08 UTC** with two `curl` requests to `https://subsplease.org/shows/` (User-Agent `BakaMitai/1.0 (Android)`). The docs step ([§8](#8-implementation-steps) step 20) writes these into `api.md` §4.1.

- **A1. 1,263 entries, 1,249 distinct slugs.** 12 slugs appear under 2 or 3 titles: seasons, specials and alternate names that share one show page (`fruits-basket-2019`: "Fruits Basket (2019)", "… S2", "… S3"; `tatoeba-last-dungeon-…`: "Last Dungeon" and the full title; `lord-of-mysteries`: 3 titles; also `detective-conan`, `youjo-senki`, `paripi-koumei`, `d4dj-all-mix`, …). No title appears twice. So a slug alone can't key a row; `slug|title` can.
- **A2. Size and headers.** 206,929 bytes (~202 KiB; api.md's "about 200 KB" holds), **41,374 bytes gzipped** on the wire (OkHttp negotiates gzip). `content-type: text/html; charset=UTF-8`, `cf-cache-status: DYNAMIC`, `cache-control: max-age=3`. `200` with the app's User-Agent, no Cloudflare challenge, ~0.3–0.5 s.
- **A3. Mismatched headers confirmed**, 32 of them: `<h3 class="alphanum-category" id="X">X</h4>` with ids `1 2 3 4 7 8` and `A`…`Z`. The links sit flat inside the single `div.all-shows` (no per-letter wrapper), so the header markup doesn't affect `div.all-shows-link > a`. The site menu also links to `https://subsplease.org/shows/` outside `div.all-shows`: scope the selector to that container.
- **A4. `href` is site-relative `/shows/<slug>` with no trailing slash** on every entry, and the `title` attribute equals the link text on every entry.
- **A5. No entity references in any title today, and two titles contain a raw, unescaped `&`** ("A3! Season Autumn & Winter", "Monogatari Series - Off & Monster Season"). Every title is ASCII. Decoding must still handle `&amp;`, `&#8217;`, `&quot;`, and leave a bare `&` alone; Ksoup does both.
- **A6. First characters.** 9 titles start with a digit ("100-man no Inochi…", "16bit Sensation…", "2.43…", "2.5-jigen no Ririsa", "29-sai…", "30-sai…", "4-nin wa…", "7th Time Loop", "86 - Eighty Six"); none with a symbol or a non-ASCII letter. One starts lowercase ("mono") and belongs under M. **Every letter A–Z has at least one show** (Q and X have 1; S 143, K 139, T 131), so the unfiltered chip row always has all 27 entries.
- **A7. Slugs aren't strictly `[a-z0-9-]`:** `d_cide-traumerei-the-animation` contains `_`. Treat slugs as opaque strings.
- **A8. The page's order isn't case-insensitive alphabetical** ("D_Cide Traumerei…" comes after "Dungeon no Naka no Hito"), which confirms context.md §4.3: sort in the app.
- Longest title: 88 characters; 243 titles exceed 35 characters (§3 row 9).

### 4.3 Derivation (domain)

`GetShowIndexUseCase(forceRefresh: Boolean = false): Result<ShowIndex>`:
1. `shows = repository.getAllShows(forceRefresh)` (failure → that `DomainError`).
2. `distinctBy { it.slug to it.title }` (exact duplicates only, A1).
3. Sort: `compareBy<Show, String>(String.CASE_INSENSITIVE_ORDER) { it.title }.thenBy { it.title }.thenBy { it.slug.value }` (stable for titles that differ only in case).
4. Group by `sectionLetter(title)`: `title.firstOrNull()?.uppercaseChar()?.takeIf { it in 'A'..'Z' } ?: ShowIndexSection.OTHER` (`'#'`). Titles are trimmed by the DTO mapper, so the first character is significant. Sections in `Char` order, which puts `'#'` (0x23) before `'A'`; empty letters get no section.
5. Return `ShowIndex(sections)`.

`FilterShowIndexUseCase(index: ShowIndex, query: String): ShowIndex` (pure, non-suspending):
- `q = query.trim()`; blank → `index` unchanged.
- Otherwise keep shows with `title.contains(q, ignoreCase = true)`, drop sections left empty, keep order.

### 4.4 Local data

None. No DataStore.

### 4.5 Caching and refresh

[api.md §7](api.md#7-client-guidelines): the all shows list is cached in memory **for the process lifetime**, invalidated by pull-to-refresh.

- `ShowCatalogRepositoryImpl` (`@Single`, so it lives for the process) keeps the last successful `List<Show>` behind a `Mutex`:
  ```kotlin
  override suspend fun getAllShows(forceRefresh: Boolean): Result<List<Show>> = mutex.withLock {
      val cached = cache?.takeUnless { forceRefresh }
      if (cached != null) return@withLock Result.success(cached)
      safeApiCall {
          val html = service.getAllShowsPage()
          withContext(dispatchers.default) { parser.parse(html).toDomain() }
              .ifEmpty { throw DomainError.InvalidData }      // markup changed: a parsing error, not a crash
      }.onSuccess { cache = it }
  }
  ```
  Failures aren't cached and don't clear the old cache. Concurrent callers wait for the lock and share one request.
- **When All Shows fetches:** once per `AllShowsViewModel` (`init { onIntent(Load) }`), using the cache; `Retry`, using the cache (it holds only successes); pull-to-refresh, with `forceRefresh = true`.
- **When it doesn't:** tab switches, rotation, typing (filtering is local). No new-day or foreground reload: the catalogue doesn't depend on the date or zone. No polling.

---

## 5. Architecture

Root: `app/src/main/java/com/zc/bakamitai/`. Test root: `app/src/test/java/com/zc/bakamitai/`.

### 5.1 Shared code

| Path | | Responsibility |
|---|---|---|
| `common/ui/component/emptystate/EmptyStateView.kt` | **new** | `EmptyStateView(icon: ImageVector, title: UiText, message: UiText, modifier: Modifier = Modifier, action: (@Composable () -> Unit)? = null)`: Schedule's empty-view visuals unchanged (64 dp `surfaceContainerHighest` circle, 32 dp `secondary` icon, `headlineSmall` title with `heading()`, `bodyMedium` / `onSurfaceVariant` message ≤ 320 dp, `spacing.md` gaps, `spacing.xl` vertical padding, optional action under it). `@AdaptivePreviews` with and without an action. |
| `feature/schedule/presentation/ScheduleScreen.kt` | modify | `ScheduleEmptyView` calls `EmptyStateView(icon = Icons.Outlined.CalendarViewDay, title = model.title, message = model.message, action = if (model.showResetAction) { { Button(…) { … } } } else null)`. Drop the now-unused private constants. Schedule looks the same. |
| `common/domain/model/Show.kt` | modify | KDoc only: `coverUrl` is also `null` when the source has no cover (the All Shows catalogue). All Shows reuses `Show` with `coverUrl = null` ([Q15](#10-open-questions)). |

Already there and reused: `TopLevelAppBarView`, `ErrorView` / `toErrorUIModel()`, `SkeletonView` / `SkeletonBlock` / `SkeletonTextLine` / `textLineHeight`, `Show` / `ShowSlug`, `DispatcherProvider`, `safeApiCall` (passes a thrown `DomainError` through), `createHttpClient` / `appJson`, `UiText`, `PluralResource`, `ObserveAsEvents`, `@AdaptivePreviews`.

### 5.2 feature/shows/data

| Path | | Responsibility |
|---|---|---|
| `feature/shows/data/remote/ShowsService.kt` | new | `@Single class ShowsService(private val client: HttpClient)`. `suspend fun getAllShowsPage(): String = client.get(SHOWS_PATH).bodyAsText()` with `SHOWS_PATH = "shows/"`. No parsing, no mapping. |
| `feature/shows/data/remote/AllShowsHtmlParser.kt` | new | `@Single class AllShowsHtmlParser { fun parse(html: String): List<ShowLinkDTO> }`. **The only file that imports `com.fleeksoft.ksoup.*`.** Pure extraction in page order; entity decoding is Ksoup's. Returns an empty list when nothing matches (the repository decides that's an error). |
| `feature/shows/data/dto/ShowLinkDTO.kt` | new | `data class ShowLinkDTO(val href: String?, val title: String?, val text: String?)`: one `a` element as found. **Not `@Serializable`**: the parser builds it from HTML ([Q10](#10-open-questions)). Blank values are `null`. |
| `feature/shows/data/mapper/ShowLinkDTOMapper.kt` | new | `fun ShowLinkDTO.toDomain(): Show?` and `fun List<ShowLinkDTO>.toDomain(): List<Show>` (`mapNotNull`). Slug: `href` matched by `^(?:https?://[^/]+)?/shows/([^/?#]+)/?(?:[?#].*)?$` (group 1), else `null`. Title: `title`, else `text`, with whitespace collapsed (`\s+` → one space) and trimmed; blank → `null`. `coverUrl = null`. |
| `feature/shows/data/repository/ShowCatalogRepositoryImpl.kt` | new | `@Single`, implements `ShowCatalogRepository`. Depends on `ShowsService`, `AllShowsHtmlParser`, `DispatcherProvider`. The process-lifetime cache in [§4.5](#45-caching-and-refresh). |

The parser, the one tricky detail:

```kotlin
fun parse(html: String): List<ShowLinkDTO> =
    Ksoup.parse(html = html)
        .select("div.all-shows div.all-shows-link > a")          // scoped: the menu links to /shows/ too (A3)
        .map { link ->
            ShowLinkDTO(
                href = link.attr("href").ifBlank { null },
                title = link.attr("title").ifBlank { null },     // entity-decoded by Ksoup (A5)
                text = link.text().ifBlank { null },
            )
        }
```

### 5.3 feature/shows/domain (pure Kotlin)

| Path | | Responsibility |
|---|---|---|
| `domain/model/ShowIndexSection.kt` | new | `data class ShowIndexSection(val letter: Char, val shows: List<Show>)` with `companion object { const val OTHER = '#' }`. `letter` is `'A'..'Z'` or `OTHER`. |
| `domain/model/ShowIndex.kt` | new | `data class ShowIndex(val sections: List<ShowIndexSection>)` with `val showCount: Int get() = sections.sumOf { it.shows.size }`. |
| `domain/repository/ShowCatalogRepository.kt` | new | `suspend fun getAllShows(forceRefresh: Boolean): Result<List<Show>>`. Failures are `DomainError`; a page without shows is `InvalidData`. |
| `domain/usecase/GetShowIndexUseCase.kt` | new | `@Factory`. `suspend operator fun invoke(forceRefresh: Boolean = false): Result<ShowIndex>` implements [§4.3](#43-derivation-domain) steps 1–5. |
| `domain/usecase/FilterShowIndexUseCase.kt` | new | `@Factory`. `operator fun invoke(index: ShowIndex, query: String): ShowIndex` implements the filter in §4.3. No dependencies. |

### 5.4 feature/shows/presentation

Component placement follows `structure` §1.1. The letter model is rendered by two components (header and chip), so it sits in `presentation/model/`.

| Path | | Responsibility |
|---|---|---|
| `presentation/model/IndexLetterUIModel.kt` | new | `@Immutable data class IndexLetterUIModel(val letter: Char, val label: UiText, val description: UiText, val isPrimaryAccent: Boolean)` |
| `presentation/model/ShowSectionUIModel.kt` | new | `@Immutable data class ShowSectionUIModel(val letter: IndexLetterUIModel, val countLabel: UiText, val rows: ImmutableList<ShowRowUIModel>)` with `val headerKey: String get() = "header-${letter.letter}"`. |
| `presentation/mapper/IndexLetterUIModelMapper.kt` | new | `fun ShowIndexSection.toIndexLetterUIModel(): IndexLetterUIModel`. Letters: `label = description = UiText.StringValue("A")`, `isPrimaryAccent = (letter - 'A') % 2 == 0`. `OTHER`: `label = UiText.StringValue("#")`, `description = cd_all_shows_other_section`, `isPrimaryAccent = false`. |
| `presentation/mapper/ShowIndexUIModelMapper.kt` | new | `fun ShowIndex.toSectionUIModels(): ImmutableList<ShowSectionUIModel>` (count = plural `all_shows_section_count` of `shows.size`) and `fun ShowIndex.toFooterLabel(): UiText` (plural `all_shows_footer_count` of `showCount`). |
| `component/showrow/model/ShowRowUIModel.kt` | new | `@Immutable data class ShowRowUIModel(val key: String, val slug: String, val title: UiText)` |
| `component/showrow/model/ShowRowPosition.kt` | new | `enum class ShowRowPosition { FIRST, MIDDLE, LAST, SINGLE }` with `companion fun of(index: Int, size: Int)`. A plain value enum (no suffix), like Schedule's `StatusFilter`. |
| `component/showrow/mapper/ShowRowUIModelMapper.kt` | new | `fun Show.toShowRowUIModel(): ShowRowUIModel`, `key = "${slug.value}|$title"` (unique, A1) |
| `component/showrow/ShowRowView.kt` | new | `ShowRowView(model, position, onClick: (() -> Unit)?, modifier)` + `ShowRowSkeletonView(position, modifier, titleWidthFraction = …)` (§2.5), with `@AdaptivePreviews` |
| `component/letterheader/LetterHeaderView.kt` | new | `LetterHeaderView(letter: IndexLetterUIModel, countLabel: UiText, modifier)` + `LetterHeaderSkeletonView(modifier)` (§2.4), with `@AdaptivePreviews` |
| `component/letterchip/LetterChipView.kt` | new | `LetterChipView(letter: IndexLetterUIModel, selected: Boolean, onClick: () -> Unit, modifier)` + `LetterChipSkeletonView(modifier)` (§2.3), with `@AdaptivePreviews` |
| `component/searchfield/ShowSearchFieldView.kt` | new | `ShowSearchFieldView(query: String, onQueryChange: (String) -> Unit, onClearClick: () -> Unit, modifier)` (§2.2), with `@AdaptivePreviews` (empty, with text). No skeleton: it's chrome, real during loading. |
| `presentation/AllShowsState.kt` | new | MVI state (§5.8) |
| `presentation/AllShowsIntent.kt` | new | Intents (§5.8) |
| `presentation/AllShowsEffect.kt` | new | Effects (§5.8) |
| `presentation/AllShowsViewModel.kt` | new | `@KoinViewModel class AllShowsViewModel(getShowIndex: GetShowIndexUseCase, filterShowIndex: FilterShowIndexUseCase)`. Keeps `loadJob` and the last `ShowIndex` private (re-filtered on every query change, never exposed). Maps with the mappers above, reduces with `_state.update`. |
| `presentation/AllShowsRoute.kt` | new | `AllShowsRoute(onOpenDetails: ((slug: String) -> Unit)?, onOpenSettings: (() -> Unit)?, modifier, viewModel = koinViewModel())`. Schedule's Route without the `LifecycleStartEffect`: `collectAsStateWithLifecycle()`, `ObserveAsEvents` (`OpenDetails` → `onOpenDetails?.invoke(slug)`, `OpenSettings` → `onOpenSettings?.invoke()`, `ShowError` → snackbar), owns the `SnackbarHostState`, passes `canOpenShow` and `showSettingsAction`. |
| `presentation/AllShowsScreen.kt` | new | `AllShowsScreen(state, onIntent, modifier, canOpenShow: Boolean = true, showSettingsAction: Boolean = true, snackbarHostState = remember { SnackbarHostState() })`. Private: `AllShowsLayout` (window-size values), `SearchDeck`, `LetterChipRow`, `ShowsList` (the `LazyColumn` shared by content and skeleton), `AllShowsContent`, `AllShowsSkeleton`, `CatalogFooter`. `@AdaptivePreviews` with a `PreviewParameterProvider<AllShowsState>`. If Detekt's `TooManyFunctions` (11 per file) trips, move `CatalogFooter` to `component/catalogfooter/` (Home's precedent with `SectionHeaderView`). |

`AllShowsScreen` layout:

- `listState = key(state.query) { rememberLazyListState() }`, `appBarState = key(state.query) { rememberTopAppBarState() }`, `scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(appBarState)`.
- `headerIndices = remember(state.sections) { … }` (item index of each section header), `activeSection` via `derivedStateOf` (§2.3), `scope = rememberCoroutineScope()` for chip jumps.
- `Scaffold(modifier.nestedScroll(scrollBehavior.nestedScrollConnection), topBar = { TopLevelAppBarView(subtitle = all_shows_app_bar_subtitle, …, bottomContent = if (state.error == null) { { SearchDeck(…) } } else null) }, snackbarHost = …)`, keeping Scaffold's **default** content insets (Home QA issue 1).
- Body `when`:
  - `state.hasContent` → `PullToRefreshBox(state.isRefreshing, onRefresh = { onIntent(Refresh) })` → `AllShowsContent`
  - `state.error != null` → `ErrorView`
  - else → `AllShowsSkeleton`
- `AllShowsContent` = `ShowsList(layout, listState)` with, per section: `stickyHeader(key = section.headerKey, contentType = "header") { LetterHeaderView(…) }` then `itemsIndexed(section.rows, key = { _, row -> row.key }, contentType = { _, _ -> "row" }) { i, row -> ShowRowView(row, ShowRowPosition.of(i, section.rows.size), onClick = …) }`; then `item(key = "footer", contentType = "footer") { CatalogFooter(…) }`, or, when `state.showNoResults`, only `item(key = "no-results", contentType = "empty") { EmptyStateView(…) }`.
- `ShowsList(layout, listState, modifier, userScrollEnabled = true, content)`: `LazyColumn(contentPadding = PaddingValues(start = h, end = h, top = spacing.xs, bottom = spacing.xl))` inside `BoxWithConstraints` for `h` (§2.8).
- `AllShowsLayout(windowSizeClass)`: `margin` (`margin` / `marginTablet` from `WIDTH_DP_MEDIUM_LOWER_BOUND`), with `fun horizontalPadding(maxWidth: Dp): Dp = max(margin, (maxWidth - MaxContentWidth) / 2)`. Never stored in state.
- Click lambdas are adapted once: `remember(onIntent) { { slug: String -> onIntent(AllShowsIntent.ShowClicked(slug)) } }`; rows get `onClick = if (canOpenShow) { { onShowClick(row.slug) } } else null`. `onQueryChange = remember(onIntent) { { q: String -> onIntent(AllShowsIntent.QueryChanged(q)) } }`.

### 5.5 Navigation

| Path | | Responsibility |
|---|---|---|
| `feature/shows/presentation/navigation/AllShowsNavKey.kt` | new | `@Serializable data object AllShowsNavKey : NavKey`, a plain top-level page. |
| `feature/shows/presentation/navigation/AllShowsNavigation.kt` | new | `fun EntryProviderScope<NavKey>.allShowsEntry(onOpenDetails: ((slug: String) -> Unit)?, onOpenSettings: (() -> Unit)?)` → `entry<AllShowsNavKey> { AllShowsRoute(onOpenDetails, onOpenSettings) }`. No `Navigator.navigateToAllShows()`: no feature opens it. |
| `app/navigation/TopLevelDestination.kt` | modify | `ALL_SHOWS(key = AllShowsNavKey, …)`. Icons and label unchanged. |
| `app/navigation/AppEntryProvider.kt` | modify | Add `allShowsEntry(onOpenDetails = null, onOpenSettings = null)`; update the KDoc (Home's, Schedule's and All Shows' callbacks are `null` until Details and Settings exist). |

`AppNavigation` needs no change: `topLevelRoutes` comes from the destinations' keys, and `NavigationState` gives the new key its own back stack and ViewModel store.

### 5.6 di

| Path | | Responsibility |
|---|---|---|
| `feature/shows/di/ShowsModule.kt` | new | `@Module @ComponentScan("com.zc.bakamitai.feature.shows") class ShowsModule` |
| `app/di/AppModule.kt` | modify | `@Module(includes = [CommonModule::class, HomeModule::class, ScheduleModule::class, ShowsModule::class])` |

### 5.7 Resources

`res/values/strings.xml` (modify), a new `<!-- All Shows (feature/shows) -->` section:

- `all_shows_app_bar_subtitle` "ALL SHOWS"
- `all_shows_search_placeholder` "Search anime title…"
- `cd_all_shows_clear_search` "Clear search"
- `cd_all_shows_other_section` "Numbers and symbols"
- `cd_all_shows_jump_to` "Jump to %1$s" (`%1$s`: the letter's description, a `UiText` argument)
- `all_shows_no_results_title` "No anime found", `all_shows_no_results_message` "Try adjusting your spelling or searching by an alternate English or Romaji title."
- Plural `all_shows_section_count`: one "%d SHOW", other "%d SHOWS" (uppercase, DESIGN.md labels)
- Plural `all_shows_footer_count`: one "Showing %d show", other "Showing %d shows"

### 5.8 MVI contract

```kotlin
@Immutable
data class AllShowsState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: ErrorUIModel? = null,                                   // full-screen error (no content yet)
    val query: String = "",                                            // the search field's text, as typed
    val sections: ImmutableList<ShowSectionUIModel> = persistentListOf(), // filtered by query
    val footerLabel: UiText? = null,                                   // non-null once a load succeeded
) {
    val hasContent: Boolean get() = footerLabel != null
    val showNoResults: Boolean get() = hasContent && sections.isEmpty()
}

sealed interface AllShowsIntent {
    data object Load : AllShowsIntent                                  // from init
    data object Retry : AllShowsIntent                                 // ErrorView button
    data object Refresh : AllShowsIntent                               // pull-to-refresh (bypasses the cache)
    data class QueryChanged(val query: String) : AllShowsIntent
    data object QueryCleared : AllShowsIntent
    data class ShowClicked(val slug: String) : AllShowsIntent
    data object SettingsClicked : AllShowsIntent
}

sealed interface AllShowsEffect {
    data class OpenDetails(val slug: String) : AllShowsEffect
    data object OpenSettings : AllShowsEffect
    data class ShowError(val error: ErrorUIModel) : AllShowsEffect     // refresh failed while content is shown
}
```

**Reducer rules:**

- **`Load` / `Retry` / `Refresh`:** ignored while `loadJob?.isActive`. Otherwise `isLoading = !hasContent`, `isRefreshing = hasContent`, `error = null`, then `getShowIndex(forceRefresh = intent is Refresh)`.
- **Success:** keep the `ShowIndex` privately; flags off; then `withIndex(index)`: `filtered = filterShowIndex(index, query)`, `sections = filtered.toSectionUIModels()`, `footerLabel = filtered.toFooterLabel()`. The query is kept.
- **Failure without content:** `error = throwable.toErrorUIModel()`. **With content:** flags off, then `ShowError`.
- **`QueryChanged(q)`:** **synchronously** `_state.update { it.copy(query = q).withIndexIfLoaded() }` (no coroutine, no debounce: the text field reads `state.query`). Before the first load only `query` changes.
- **`QueryCleared`:** the same with `""`.
- **`ShowClicked` / `SettingsClicked`:** send the matching effect.

Filtering 1,263 titles and mapping the matches costs about a millisecond; if profiling shows jank on low-end devices, cache the mapped `ShowRowUIModel`s by key in the ViewModel rather than debouncing (which would desync the field).

---

## 6. Dependencies

### Libraries

**New: Ksoup, decided by the user on 2026-11-19** (context.md §7 open decision 1; data layer only).

- **Artifact:** `com.fleeksoft.ksoup:ksoup`, **0.2.6**, the latest stable release (published 2026-02-19 on Maven Central). It's Kotlin Multiplatform: Gradle module metadata resolves it to `ksoup-android` (an AAR, ~640 KB) for `:app` and its unit tests. Transitive: `com.fleeksoft.io:io-core`, `com.fleeksoft.charset:charset` (0.0.8), `co.touchlab:stately-concurrency`, `kotlin-stdlib` 2.3.10 (our 2.4.20 wins). License MIT.
- **Not needed:** `ksoup-network` (fetching; we use our Ktor client), `ksoup-kotlinx` / `ksoup-okio` / `ksoup-korlibs` (I/O adapters for files and streams; we parse a `String`), `ksoup-lite` (stuck at 0.2.0).
- **API used:** `com.fleeksoft.ksoup.Ksoup.parse(html)`, `Document.select(css)`, `Element.attr(name)`, `Element.text()`.
- **R8:** the release build's `optimization.packageScope` covers only `androidx.**`, `kotlin.**` and `kotlinx.**`, so Ksoup isn't optimized and needs no keep rules. It adds roughly its AAR size to the APK ([Q14](#10-open-questions)).
- **Changes, in `library-management` §5 order** (skills first, step 1):
  1. `tech-stack` §1 approved table: row "HTML parsing | Ksoup (`com.fleeksoft.ksoup:ksoup`), scraped SubsPlease pages only, inside a `data/remote/[Name]HtmlParser`".
  2. `tech-stack` §2 layer table: Ksoup → `domain` —, `data` ✅ (`[Name]HtmlParser` only), `presentation` —, `common/ui` —, `di` —, app —.
  3. `tech-stack` banned table: "jsoup (Java), regex / `indexOf` HTML scraping, `Html.fromHtml` for parsing" → "Ksoup in a `[Name]HtmlParser`"; and a verification command that must print nothing: `grep -rln "import com.fleeksoft.ksoup" app/src/main | grep -v "/data/remote/[A-Za-z]*HtmlParser.kt$"`.
  4. `library-management` §2 artifact map: "HTML parsing (Ksoup) | `libs.ksoup` | `com.fleeksoft.ksoup:ksoup` (KMP; resolves to `ksoup-android`)".
  5. `gradle/libs.versions.toml`: `[versions] ksoup = "0.2.6"`; `[libraries] ksoup = { group = "com.fleeksoft.ksoup", name = "ksoup", version.ref = "ksoup" }`.
  6. `app/build.gradle.kts`: `implementation(libs.ksoup)` in the "Networking, images, rich text, persistence" group (rename the comment to include HTML parsing).
- **Already present:** Compose Material 3 1.4.0 (`SearchBarDefaults.InputField`, `FilterChip`, `PullToRefreshBox`, `HorizontalDivider`), Foundation 1.12.1 (`LazyListScope.stickyHeader`), `material-icons-extended` 1.7.8, kotlinx-coroutines (`Mutex`), kotlinx-collections-immutable, Ktor (`bodyAsText`), Koin, Navigation 3. Opt-ins (`ExperimentalMaterial3Api` for `TopAppBar`, `PullToRefreshBox`, `SearchBarDefaults.InputField`) go on the smallest declaration.

### Earlier plans and shared code

- **Needs (exists):** everything in [§5.1](#51-shared-code)'s "already there" list, and the navigation shell.
- **Creates:** `EmptyStateView` (Schedule switches to it).

### What it unblocks

- **Details plan:** passes `onOpenDetails = navigator::navigateToDetails` to `allShowsEntry` too; rows become clickable with a chevron. On expanded widths it may put All Shows and Details side by side with the list-detail scene strategy.
- **Settings plan:** wires `onOpenSettings` for this screen as well.
- **Details plan (parsing):** reuses the Ksoup approval and the `[Name]HtmlParser` placement for the show page ([api.md §4.2](api.md#42-show-page)).

---

## 7. Testing

Unit tests mirror the main package under `app/src/test/java/com/zc/bakamitai/`. Tools: JUnit4, `kotlin.test`, `runTest`, Turbine, Ktor `MockEngine` through `createHttpClient(appJson, "test", engine)`, `TestDispatcherProvider`, hand-written fakes.

### Fixtures

| File | Source | Must contain |
|---|---|---|
| `shows_page.html` (new) | A fresh `GET /shows/` (step 3), trimmed | The real `<head>` stub and the site menu's `https://subsplease.org/shows/` link (outside the list); `div.all-shows` with the real mismatched headers `1`, `8`, `A`, `D`, `F`, `M` (`<h3 …>X</h4>`) and these real entries: "100-man no Inochi no Ue ni Ore wa Tatte Iru", "86 - Eighty Six", "A3! Season Autumn & Winter" (raw `&`), "D_Cide Traumerei the Animation", "Dungeon no Naka no Hito", "Fruits Basket (2019)" / "… S2" / "… S3" (one slug), "mono", "Monogatari Series - Off & Monster Season". Add `SubsPleaseFixtures.SHOWS_PAGE`. |

Edge cases (entity references, trailing slashes, absolute `href`s, missing `title`, nested tags in the link, a page without `div.all-shows`) are inline HTML strings in the tests, not fixtures.

### Test classes

| Class | Covers |
|---|---|
| `feature/shows/data/remote/AllShowsHtmlParserTest` | The fixture yields its entries in page order, one per `div.all-shows-link`, despite the mismatched `</h4>` (A3); header texts ("A", "8") never appear as entries; the menu link outside `div.all-shows` is ignored. `title="Kaguya &amp; Friends"` → "Kaguya & Friends"; `&#8217;` → "’"; `&quot;` decoded; raw `&` kept (A5). Missing `title` → `title = null`, `text` set. `<a …><span>X</span> Y</a>` → text "X Y". No container → empty list. |
| `feature/shows/data/mapper/ShowLinkDTOMapperTest` | `/shows/86-eighty-six` → `ShowSlug("86-eighty-six")`; trailing `/`; absolute `https://subsplease.org/shows/x/`; `?a=b#c` stripped; `d_cide-…` kept (A7); `/shows/`, `/other/x`, `null` → dropped. Title trimmed and whitespace collapsed; blank title falls back to text; both blank → dropped. `coverUrl == null`. |
| `feature/shows/data/remote/ShowsServiceTest` | Sends `GET /shows/` to `subsplease.org` with no query parameters; returns the body unchanged, decoded as UTF-8 from `text/html; charset=UTF-8` (inline body with "’"). |
| `feature/shows/data/repository/ShowCatalogRepositoryImplTest` | `MockEngine` counting requests, the real parser, `TestDispatcherProvider`. Fixture → shows in page order (14 duplicates are the use case's job). Second call → no request; `forceRefresh` → request; a failure isn't cached and keeps the previous cache (fail, then succeed, then a forced failure still leaves the next unforced call served from cache); two concurrent calls → one request. 500 → `Server`; 404 → `NotFound`; `IOException` → `Network`; `<html></html>` → `InvalidData`; only unusable links → `InvalidData`. |
| `feature/shows/domain/repository/FakeShowCatalogRepository.kt` | **Fake.** Settable `Result`, a log of `forceRefresh` values, an optional `gate: CompletableDeferred<Unit>`. |
| `feature/shows/domain/usecase/GetShowIndexUseCaseTest` | Sorted case-insensitively ("mono" before "Monogatari…" under M, A6); "D_Cide…" placed by our comparator, not the page's (A8); "#" first with the digit titles ("100-man…", "16bit…", "2.43…", "86 - …" in that order); a symbol ("[Oshi no Ko]") and a non-ASCII initial ("Ōkami") → "#"; sections only for letters with shows, in order; exact (slug, title) duplicates removed, same slug with other titles kept (A1); `forceRefresh` passed through; failure → that `DomainError`. |
| `feature/shows/domain/usecase/FilterShowIndexUseCaseTest` | Blank and whitespace queries → same index; `"  conan "` trimmed; `"CONAN"` finds "Detective Conan" (ignore case); mid-word match ("ainsaw"); sections emptied by the filter are dropped; no match → no sections; order kept. |
| `feature/shows/presentation/mapper/IndexLetterUIModelMapperTest` | A → primary, B → secondary, Z → secondary; "#" → label "#", description `cd_all_shows_other_section`, secondary. |
| `feature/shows/presentation/mapper/ShowIndexUIModelMapperTest` | One section model per section with header key `header-A`; count plural with the section size; footer plural with `showCount`. |
| `…/component/showrow/mapper/ShowRowUIModelMapperTest` | Key `slug|title`, unique for one slug with two titles; title `UiText.StringValue`. |
| `…/component/showrow/model/ShowRowPositionTest` | `of(0, 1)` → `SINGLE`; `of(0, 3)` → `FIRST`; `of(1, 3)` → `MIDDLE`; `of(2, 3)` → `LAST`. |
| `feature/shows/presentation/AllShowsViewModelTest` | See below. |
| `common/ui/component/emptystate/` | No unit test (a stateless View); covered by previews and Schedule's unchanged screenshots. |
| `app/navigation/NavKeySerializationTest` | **Modify:** add `AllShowsNavKey` to the sample list (the top-level-key test requires it). |
| `app/di/KoinModulesTest` | **Modify:** `verify()` passes with `ShowsModule`; resolves `ShowCatalogRepository` as `ShowCatalogRepositoryImpl`, `GetShowIndexUseCase`, `FilterShowIndexUseCase`. |

`AllShowsViewModelTest` (`MainDispatcherRule`, Turbine, real use cases over `FakeShowCatalogRepository`):
- `init` → `isLoading` → content: sections, footer "Showing N shows", `query == ""`, `forceRefresh = false`.
- Failure → `error`; `Retry` → content.
- `QueryChanged("conan")` updates `query` and `sections` **before any dispatcher advance** (synchronous), and the footer counts the matches.
- `QueryChanged` while loading → only `query`; when the load succeeds, sections are already filtered.
- A query with no match → `showNoResults`; `QueryCleared` → all sections, `query == ""`.
- `Refresh` → `isRefreshing` → content, query kept, `forceRefresh = true` logged.
- `Refresh` failure with content → content kept, flags off, `ShowError`.
- A second `Refresh` while loading → ignored (one call).
- `ShowClicked("x")` → `OpenDetails("x")`; `SettingsClicked` → `OpenSettings`.

**Every quirk in [§4](#4-data) has a test:**
- A1 several titles per slug: GetShowIndexUseCaseTest, ShowRowUIModelMapperTest (keys)
- A3 mismatched headers, menu link outside the list: AllShowsHtmlParserTest (fixture)
- A4 `href` forms: ShowLinkDTOMapperTest
- A5 entities and raw `&`: AllShowsHtmlParserTest
- A6 digits, lowercase and non-ASCII initials: GetShowIndexUseCaseTest
- A7 `_` in slugs: ShowLinkDTOMapperTest
- A8 page order not trusted: GetShowIndexUseCaseTest
- empty or unrecognisable page → `InvalidData`; UTF-8 body: RepositoryImplTest, ServiceTest
- process-lifetime cache, forced refresh, failure not cached: RepositoryImplTest
- A2 (size, headers) is documented only.

### Compose previews

`@AdaptivePreviews` (light/dark × compact/expanded) inside `BakaMitaiTheme`, private sample data:

- `AllShowsScreen` provider: loading; content (sections "#", A, B with a long 2-line title and a one-row section); searching with results ("conan"); no results; refreshing; error.
- `ShowRowView`: each `ShowRowPosition`, a long title, clickable and not; plus `ShowRowSkeletonView`.
- `LetterHeaderView`: primary letter, secondary letter, "#"; plus `LetterHeaderSkeletonView`.
- `LetterChipView`: selected, not selected, "#"; plus `LetterChipSkeletonView`.
- `ShowSearchFieldView`: empty (placeholder), with text (clear button).
- `EmptyStateView`: with and without an action.

---

## 8. Implementation steps

### Phase 0: shared prerequisites

1. [x] **Ksoup** (decided 2026-11-19): the `tech-stack` §1, §2 and banned-table rows plus the verification grep, the `library-management` §2 row, then the catalog entry and `implementation(libs.ksoup)` ([§6](#6-dependencies)).
   **Done when:** `./gradlew assembleDebug` passes and `./gradlew :app:dependencies --configuration debugRuntimeClasspath | grep ksoup` shows `com.fleeksoft.ksoup:ksoup:0.2.6 -> ksoup-android`.
2. [x] **`EmptyStateView`** in `common/ui/component/emptystate/`, with previews; `ScheduleEmptyView` uses it; `Show` KDoc.
   **Done when:** `./gradlew detekt testDebugUnitTest` pass and the Schedule filter-empty and day-empty previews look as before.

### Phase 1: non-UI

3. [x] **Fixture:** trim `shows_page.html` from a fresh `GET /shows/` (one request) per §7; add `SubsPleaseFixtures.SHOWS_PAGE`.
   **Done when:** the fixture has every entry listed in §7 and at least one `<h3 …></h4>` header.
4. [x] **DTO and parser:** `ShowLinkDTO`, `AllShowsHtmlParser`, `AllShowsHtmlParserTest`.
   **Done when:** the parser tests pass and the Ksoup import grep from step 1 prints nothing.
5. [x] **Mapper:** `ShowLinkDTOMapper` + test.
   **Done when:** `ShowLinkDTOMapperTest` passes.
6. [x] **Service:** `ShowsService` + `ShowsServiceTest`.
   **Done when:** the request and UTF-8 assertions pass.
7. [x] **Domain:** `ShowIndexSection`, `ShowIndex`, `ShowCatalogRepository`.
   **Done when:** it compiles and the `architecture` §1 domain-purity grep prints nothing.
8. [x] **Repository:** `ShowCatalogRepositoryImpl` with the cache + `ShowCatalogRepositoryImplTest`.
   **Done when:** every repository case in §7 passes.
9. [x] **Use cases:** `GetShowIndexUseCase`, `FilterShowIndexUseCase`, `FakeShowCatalogRepository`, both tests.
   **Done when:** every use-case case in §7 passes.
10. [x] **DI:** `ShowsModule`, `AppModule`, `KoinModulesTest`.
    **Done when:** `./gradlew testDebugUnitTest --tests "*KoinModulesTest"` passes.
11. [x] **Phase 1 gate:** `./gradlew testDebugUnitTest`, `./gradlew detekt`, `./gradlew assembleDebug`, the `tech-stack` verification greps and the `architecture` §1 greps.
    **Done when:** all pass with zero Detekt findings.

### Phase 2: UI

12. [x] **Resources:** the strings and plurals in [§5.7](#57-resources).
    **Done when:** it builds and the `conventions` §2.5 grep prints nothing new.
13. [x] **Presentation models and mappers:** `IndexLetterUIModel`, `ShowSectionUIModel`, `ShowRowUIModel`, `ShowRowPosition`, the three mappers, and their tests.
    **Done when:** the mapper and position tests in §7 pass.
14. [x] **MVI and ViewModel:** `AllShowsState`, `AllShowsIntent`, `AllShowsEffect`, `AllShowsViewModel`, `AllShowsViewModelTest`.
    **Done when:** every ViewModel case in §7 passes.
15. **Components:** `ShowRowView`, `LetterHeaderView`, `LetterChipView` (each with its skeleton) and `ShowSearchFieldView`, with `@AdaptivePreviews`.
    **Done when:** the previews render and `./gradlew detekt` is clean.
16. **Screen and Route:** `AllShowsScreen` (deck in the top-bar slot, sticky headers, chip jump and active chip, list helper shared with the skeleton, footer, no-results, `PullToRefreshBox`, IME padding, snackbar host) with its preview provider, and `AllShowsRoute`.
    **Done when:** all six preview states render in light and dark, compact and expanded.
17. [x] **Navigation:** `AllShowsNavKey`, `AllShowsNavigation`, `TopLevelDestination.ALL_SHOWS.key`, `AppEntryProvider`, `NavKeySerializationTest`.
    **Done when:** `android run --apks=app/build/outputs/apk/debug/app-debug.apk` shows an enabled All Shows tab that loads the catalogue.
18. [x] **Emulator pass** of [§9](#9-acceptance-criteria)'s search, chip and keyboard items (first text input in the app).
    **Done when:** the last rows and the no-results view stay above the keyboard, and typing never moves the cursor.

### Phase 3: docs and verification

19. **Skills** (if not done with step 1): `structure` §2 placement row "HTML parser for a scraped page → `feature/[name]/data/remote/[Name]HtmlParser.kt`" and the `dto/` note that a DTO built by a parser isn't `@Serializable`; `conventions` naming cheat sheet row "HTML parser | `data/remote` | `[Name]HtmlParser` | `AllShowsHtmlParser`"; `structure` §7 "Current repo state" (All Shows built and enabled, `EmptyStateView` in `common/`).
    **Done when:** the skills describe what was built.
20. [x] **Docs** (done 2026-11-19, separately from the implementor):
    - **`docs/context.md`:** §7 open decision 1 → "**Decided (2026-11-19):** Ksoup (`com.fleeksoft.ksoup:ksoup`), `data` layer only, behind a `[Name]HtmlParser`"; §3.3 (letter chips that follow the scroll, per-section counts, the footer count, up to 2-line titles, pull-to-refresh, no-results state); §4.3 (dedupe exact pairs only, an empty page is `InvalidData`, parse off the main thread); §6 (`EmptyStateView` in `common/`).
    - **`docs/api.md`:** §4.1 gains A1–A8 ([§4.2](#42-api-gaps-found-live)) and the "no `tz`" note; §7's "HTML parsing needs a library that isn't approved yet" → Ksoup, approved 2026-11-19.
    - **`CLAUDE.md`** "Current state": All Shows is the third feature and enabled; only Bookmarks stays disabled; Ksoup is in the catalog.
    
    **Done when:** the docs describe the shipped behaviour.
21. **Final verification**, in order:
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
    Then the `architecture` §1 greps, the `structure` §7 checks, the `tech-stack` verification greps (including the new Ksoup one), the `conventions` §2.5 grep, and [§9](#9-acceptance-criteria) on an emulator.

    **Done when:** tests and Detekt pass with zero findings, the `unstable` grep shows nothing for All Shows' Screens and Views (the known `ObserveAsEvents` line excepted), every grep prints nothing, and every acceptance criterion is checked.

---

## 9. Acceptance criteria

**Navigation:**
- [ ] The All Shows tab is enabled; Bookmarks stays disabled. It shows "BakaMitai" / "ALL SHOWS", no settings icon.
- [ ] Back from All Shows returns to Home.
- [ ] Switching tabs and coming back keeps the list, the query and the scroll position, with no request (Logcat `Ktor` lines in a debug build).

**Loading and errors:**
- [ ] First open shows a shimmering skeleton (chip row, letter headers, row cards) under a real search field, then the list. No spinner.
- [ ] Airplane mode + cold start → error with **Retry**, no search field; after reconnecting, Retry loads.
- [ ] Airplane mode + pull-to-refresh with the list shown → list kept, error snackbar.

**List:**
- [ ] The footer says "Showing N shows", with N = the number of `div.all-shows-link` entries on `/shows/` (1,263 on 2026-11-18, no exact duplicates today).
- [ ] Sections run "#", A … Z; "#" holds the digit titles ("100-man no Inochi…" … "86 - Eighty Six"); "mono" is under M; rows are alphabetical ignoring case.
- [ ] Each header shows its letter badge (A primary, B secondary, alternating) and "N SHOWS", and sticks under the search deck while its rows scroll.
- [ ] Titles show in full up to 2 lines; "A3! Season Autumn & Winter" shows a plain "&"; "Fruits Basket (2019)", "… S2" and "… S3" are three rows.
- [ ] Tapping a row does nothing and shows no ripple or chevron.

**Search:**
- [ ] Typing "conan" filters as you type to the titles containing it, any case; section counts and the footer follow; the list starts at the top.
- [ ] A query with no match shows "No anime found" and hides the chips and footer; the clear button empties the field, restores the list and keeps the keyboard open.
- [ ] Dragging the list hides the keyboard; with the keyboard open, the last rows can be scrolled above it.

**Chips:**
- [ ] Unfiltered, the row has 27 chips ("#", A–Z); while searching, only the letters with matches.
- [ ] Tapping a chip scrolls its header to the top; the highlighted chip follows the section at the top while scrolling, and the row keeps it visible.

**Behaviour:**
- [ ] Pull-to-refresh shows the indicator and sends one `GET /shows/`; a later tab switch or rotation sends none.
- [ ] Rotation keeps the query, the list position and the active chip, with no request.

**Appearance:**
- [ ] Medium and expanded windows: rail navigation, the list and the deck centered and at most 720 dp wide, 24 dp margins on narrower medium windows.
- [ ] Light and dark themes: badges, chips and the search field are readable.
- [ ] Font scale 1.3: no clipped chip, badge, count or search text.
- [ ] TalkBack: the search field announces as search with its hint; headers are headings read "A, 14 SHOWS" / "Numbers and symbols, 9 SHOWS"; chips read their letter, "selected" for the active one, and "Jump to …" as the action; the skeleton reads "Loading".

---

## 10. Open questions

**Blocking:** none. The one blocking question, the HTML parser, was **decided by the user on 2026-11-19: Ksoup** ([§6](#6-dependencies)). Every question below can use its recommendation.

| # | Blocks? | Question | Recommendation |
|---|---|---|---|
| Q1 | **Decided 2026-11-19** | HTML parser: Ksoup or narrowly scoped regex (context.md §7, decision 1)? | **Ksoup 0.2.6**, `data` layer only, behind `AllShowsHtmlParser`. Swapping it later changes only that class and its test. |
| Q2 | No | Quick-jump: chips, the A–Z rail, or both? | **Chips only** ([§2.3](#23-letter-chips-quick-jump-and-the-dropped-az-rail)): 48 dp targets, no overlap with long titles, M3-native, and with all 26 letters populated they cover the same jumps. **Alternative:** a drag-to-scrub rail on expanded widths only, where there's room; revisit with user feedback. |
| Q3 | No | Voice search (mic)? | **Omit.** Not in the spec; needs `RecognizerIntent` through an Activity-result launcher and a speech service on the device; the keyboard's mic already dictates into the field. |
| Q4 | No | Footer "Showing 482 anime series • Updated hourly"? | **Keep a live count, drop "Updated hourly" and the pulse.** The list is cached for the process (api.md §7). **Alternative:** drop the footer. |
| Q5 | No | 12 slugs are listed under 2–3 titles (A1). One row per title, or one per show? | **One row per title**, all opening the same show: each title is a way to find it ("Last Dungeon"). Exact duplicates are removed. **Alternative:** one row per slug, keeping the shortest title (fewer rows, but "Fruits Basket (2019) S3" disappears from search). |
| Q6 | No | One-line titles (design) or two? | **Two lines.** 19 % of titles don't fit one line on a phone, and no other screen shows the full title yet. |
| Q7 | No | Row taps and the gear before Details and Settings exist? | **Non-clickable rows without chevrons, hidden gear**, as Home and Schedule. The intent → effect → nullable-lambda path is built and tested now. |
| Q8 | No | Pull-to-refresh on All Shows (not in context.md §3.3)? | **Yes.** api.md §7 already expects it to invalidate the cache, and it's the only way to see new shows without restarting the app. |
| Q9 | No | Chip taps scroll the list inside the Screen instead of going through an Intent (`conventions` §5.2 "every interaction calls onIntent"). OK? | **Yes, UI-local.** It changes only `LazyListState` (visual scroll state, like Schedule's strip auto-scroll and `best-practices` §5.1's "purely visual" `remember`); nothing in `State` changes, and an Intent → Effect → Route round trip would need the list state hoisted to the Route. **Alternative:** `LetterClicked(letter)` → `ScrollToSection` effect, if reviewers want the rule literal. |
| Q10 | No | Where do HTML parsers and their DTOs live? `structure` has no `parser/` folder, and `dto/` says "@Serializable". | **`data/remote/[Name]HtmlParser.kt`** (it decodes the remote wire format, the HTML counterpart of a JSON serializer), returning plain `[Name]DTO`s in `dto/` without `@Serializable`. Step 19 records this in `structure` §2 and `conventions`, so Details' show-page parser follows it. |
| Q11 | No | Titles starting with an accented letter ("Ōkami")? | **"#", per the spec** ("anything else → #"). None exist today (every title is ASCII, A5). Folding diacritics would need `java.text.Normalizer`. |
| Q12 | No | Extract `EmptyStateView` to `common/` now (and touch Schedule)? | **Yes** (`structure` §3: a second feature needs it). Same visuals; Schedule keeps its texts and button. **Alternative:** a private copy in `AllShowsScreen.kt`. |
| Q13 | No | Should search also ignore punctuation ("dr stone" → "Dr. STONE") or match slugs? | **No, plain case-insensitive title substring** per the spec. Revisit if users miss shows. |
| Q14 | No | Ksoup isn't in R8's `packageScope`, so it ships unshrunk (~0.6 MB AAR). Add `com.fleeksoft.**` to the scope? | **Not now.** Measure the release APK first; adding the scope needs a release-build smoke test of All Shows. |
| Q15 | No | Reuse `common` `Show` with `coverUrl = null`, or a cover-less `CatalogShow`? | **Reuse `Show`** (context.md §4.3 says `List<Show(slug, title)>`), with a KDoc note. A second model would duplicate `slug` + `title` for one nullable field. |
| Q16 | No | Keep the design's no-results copy ("…alternate English or Romaji title")? | **Yes.** SubsPlease lists mostly Romaji titles and some English ones ("Detective Conan", "Lord of Mysteries"), so the hint is accurate. |

---

## 11. Implementation notes

- **Open questions:** all use their recommendations, as the requester decided: chips only, no rail (Q2); no mic (Q3); a live footer count without "Updated hourly" (Q4); one row per title (Q5); 2-line titles (Q6); non-clickable rows and a hidden gear (Q7); pull-to-refresh (Q8); chip taps scroll inside the Screen (Q9); the parser in `data/remote/`, its DTO without `@Serializable` (Q10); non-ASCII initials under "#" (Q11); a shared `EmptyStateView` (Q12); plain title substring search (Q13); reuse `Show` with `coverUrl = null` (Q15); the design's no-results copy (Q16).
- **Q14, release size:** the release APK went from 5,281,340 to 5,788,426 bytes (+507,086, +9.6 %). `classes.dex` grew by 502,227 bytes compressed (1,224,264 uncompressed), almost all of it Ksoup and its `io-core` / `charset` dependencies, which R8's `packageScope` (`androidx.**`, `kotlin.**`, `kotlinx.**`) leaves unshrunk; `resources.arsc` grew by 14,304 bytes. Adding `com.fleeksoft.**` to the scope is a follow-up that needs a release-build smoke test of All Shows.
- **Search field colors (§2.0, §2.2):** the field is `surfaceContainerHigh`, `surfaceContainerHighest` while focused, instead of `surfaceContainer` / `surfaceContainerHigh`. On the emulator the pill vanished into the shared top bar as soon as the bar tinted to `surfaceContainer`. The colors go through `SearchBarDefaults.inputFieldColors`, whose container already animates with focus, so there is no wrapping `Surface` or `animateColorAsState`.
- **Keyboard insets (§2.8):** `consumeWindowInsets(padding).imePadding()` was replaced by a private `AboveKeyboard` layout in `AllShowsScreen.kt` that pads only by the part of the keyboard covering the content. On compact widths `imePadding()` padded about 80 dp too much (the content already ends above the navigation bar, but the IME inset is measured from the window bottom), which left a dead band above the keyboard that couldn't be scrolled. The keyboard height is read in the layout phase only.
- **Hiding the keyboard (§2.2):** the Screen clears focus on the list's `DragInteraction.Start`, not on `isScrollInProgress`, so only a user drag hides the keyboard.
- **Chip row auto-scroll (§2.3):** keyed on the sections and the active section, not the active section alone. Seen on the emulator: after clearing a query the row kept its scroll position by key, and the active "#" chip stayed out of view.
- **Header indices (§5.4):** computed in the private `LetterChips` (their only user) instead of the Screen, which keeps `AllShowsScreen` under Detekt's 60-line `LongMethod`. `CatalogFooter` stayed private: the file has 10 top-level functions.
- **Letter chips:** `FilterChip` lays its label out start-aligned in the chip's minimum width, so the label has a minimum width (`40 dp` minus the chip's 32 dp padding) and centered text. Beyond §2.3's "Jump to …" action label, the label's content description is the letter's description, so "#" reads "Numbers and symbols".
- **Letter header (§2.4):** the badge is `max(32 dp, titleLarge line height)`, as Schedule's day chip, so the letter isn't clipped at large font scales; the skeleton uses the same size. The `xs` gap to the card sits inside the header's opaque background, so rows never show through it.
- **Fixture:** `shows_page.html` comes from one `GET /shows/` on 2026-11-18, 23:24 UTC. It keeps the `<head>` stub, the site menu's `/shows/` link, the page's own "#/A/B" jump links (also outside the list), 6 mismatched headers and the 10 entries §7 lists. The live page matched §4.2: 1,263 entries, 206,929 bytes, `text/html; charset=UTF-8`, `<h3 …>X</h4>` headers.
- **Step 2:** Schedule's filter-empty view was checked on the emulator: its crop is byte-identical to a screenshot taken before the change (00:47 the same night). The day-empty view is the same call without an action and wasn't compared separately.
- **Steps 15–16 aren't ticked:** previews were rendered through Android Studio for `LetterHeaderView`, `LetterHeaderSkeletonView`, `LetterChipView`, `LetterChipSkeletonView`, `ShowSearchFieldView` and `EmptyStateView` (the tool returns the first variant only). `AllShowsScreenPreview` and `ShowRowView`'s previews didn't render: Studio's preview class loader cached a `ClassNotFoundException` for `AllShowsState` / `ShowRowPosition` from a render attempted while Gradle was rewriting classes, and only an IDE-initiated build clears it. The preview provider itself constructs its 6 states (checked reflectively in a throwaway unit test, since deleted).
- **Checked on emulator-5554 (compact, dark):** the content and sticky headers; a chip jump and the active chip following the scroll; search, clear (keyboard stays open) and no results; the list reaching the keyboard's top; cursor stability while inserting mid-text; the skeleton (caught during a cold start); one `GET /shows/` per pull-to-refresh; no request and the scroll position kept across a tab switch; the footer "Showing 1263 shows". **Not checked:** the error state, light theme on a device, medium / expanded widths, font scale 1.3, TalkBack, rotation. That's the separate QA pass.
- **Docs (steps 19–20):** with step 1, the `tech-stack` (§1, §2, banned table, Ksoup grep), `library-management` §2, `structure` (tree, §2 parser and DTO rows) and `conventions` (cheat-sheet row, parser DTO rule) skills were updated. `structure` §7 "Current repo state", `docs/api.md`, `docs/context.md` and `CLAUDE.md` are left to the requester, because the implementor edits only the plan. `docs/api.md` and `docs/context.md` already had uncommitted edits when this work started; they weren't touched.
- **Compiler warnings:** `currentWindowAdaptiveInfo()` is deprecated in favour of a V2; All Shows uses it as Home and Schedule do, because the `tech-stack` skill prescribes it.
