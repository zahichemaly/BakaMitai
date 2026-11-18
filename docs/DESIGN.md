---
name: Obsidian Signal
colors:
  surface: '#11131c'
  surface-dim: '#11131c'
  surface-bright: '#373943'
  surface-container-lowest: '#0c0e17'
  surface-container-low: '#191b24'
  surface-container: '#1d1f29'
  surface-container-high: '#282933'
  surface-container-highest: '#32343e'
  on-surface: '#e1e1ef'
  on-surface-variant: '#cbc3d7'
  inverse-surface: '#e1e1ef'
  inverse-on-surface: '#2e303a'
  outline: '#958ea0'
  outline-variant: '#494454'
  surface-tint: '#d0bcff'
  primary: '#d0bcff'
  on-primary: '#3c0091'
  primary-container: '#a078ff'
  on-primary-container: '#340080'
  inverse-primary: '#6d3bd7'
  secondary: '#4cd7f6'
  on-secondary: '#003640'
  secondary-container: '#03b5d3'
  on-secondary-container: '#00424e'
  tertiary: '#ffb2b7'
  on-tertiary: '#67001b'
  tertiary-container: '#ff516a'
  on-tertiary-container: '#5b0017'
  error: '#ffb4ab'
  on-error: '#690005'
  error-container: '#93000a'
  on-error-container: '#ffdad6'
  primary-fixed: '#e9ddff'
  primary-fixed-dim: '#d0bcff'
  on-primary-fixed: '#23005c'
  on-primary-fixed-variant: '#5516be'
  secondary-fixed: '#acedff'
  secondary-fixed-dim: '#4cd7f6'
  on-secondary-fixed: '#001f26'
  on-secondary-fixed-variant: '#004e5c'
  tertiary-fixed: '#ffdadb'
  tertiary-fixed-dim: '#ffb2b7'
  on-tertiary-fixed: '#40000d'
  on-tertiary-fixed-variant: '#92002a'
  background: '#11131c'
  on-background: '#e1e1ef'
  surface-variant: '#32343e'
typography:
  display-lg:
    fontFamily: Plus Jakarta Sans
    fontSize: 40px
    fontWeight: '800'
    lineHeight: 48px
    letterSpacing: -0.02em
  display-lg-mobile:
    fontFamily: Plus Jakarta Sans
    fontSize: 32px
    fontWeight: '800'
    lineHeight: 40px
    letterSpacing: -0.02em
  headline-lg:
    fontFamily: Plus Jakarta Sans
    fontSize: 28px
    fontWeight: '700'
    lineHeight: 36px
    letterSpacing: -0.015em
  headline-md:
    fontFamily: Plus Jakarta Sans
    fontSize: 22px
    fontWeight: '700'
    lineHeight: 28px
    letterSpacing: -0.01em
  headline-sm:
    fontFamily: Plus Jakarta Sans
    fontSize: 18px
    fontWeight: '600'
    lineHeight: 24px
  title-md:
    fontFamily: Plus Jakarta Sans
    fontSize: 16px
    fontWeight: '600'
    lineHeight: 22px
  body-lg:
    fontFamily: Plus Jakarta Sans
    fontSize: 16px
    fontWeight: '400'
    lineHeight: 24px
  body-md:
    fontFamily: Plus Jakarta Sans
    fontSize: 14px
    fontWeight: '400'
    lineHeight: 20px
  body-sm:
    fontFamily: Plus Jakarta Sans
    fontSize: 12px
    fontWeight: '400'
    lineHeight: 16px
  label-lg:
    fontFamily: Space Grotesk
    fontSize: 13px
    fontWeight: '700'
    lineHeight: 18px
    letterSpacing: 0.04em
  label-md:
    fontFamily: Space Grotesk
    fontSize: 11px
    fontWeight: '600'
    lineHeight: 16px
    letterSpacing: 0.06em
  label-sm:
    fontFamily: Space Grotesk
    fontSize: 10px
    fontWeight: '500'
    lineHeight: 14px
    letterSpacing: 0.08em
rounded:
  sm: 0.5rem
  DEFAULT: 1rem
  md: 1.5rem
  lg: 2rem
  xl: 3rem
  full: 9999px
spacing:
  gutter: 1rem
  gutter-sm: 0.75rem
  margin: 1rem
  margin-tablet: 1.5rem
  space-xs: 0.25rem
  space-sm: 0.5rem
  space-md: 1rem
  space-lg: 1.5rem
  space-xl: 2rem
---

## Brand & Style

The design system establishes an atmospheric, hyper-focused mobile interface for tracking episodic anime media. Balancing cinematic immersion with programmatic precision, the visual language draws heavily from **Material Design 3 Expressive** merged with **Midnight Neo-Tokyo Cyber-Modernism**.

### Audience & Sentiment
- **Audience:** Otaku cinephiles, seasonal anime trackers, power users tracking simulcasts down to the minute across multiple streaming services.
- **Emotional Response:** High-tech anticipation, nocturnal focus, visual delight, and frictionless navigation. The interface recedes into deep nocturnal obsidian to make episodic visual keys and anime cover art pop with vivid luminescence.

### Design Movement & Directives
- **Atmospheric Nocturnalism:** True-dark canvas architecture utilizing deep slate and tinted indigo undertones rather than desaturated, lifeless grays.
- **M3 Expressiveness:** Bold, pill-shaped interactions, expressive tonal shifts, vibrant active containers, and clear tactile responses.
- **Precision Data Scannability:** Distinct badge geometry (Airing, Countdown, Finished, Delayed) anchored by glowing status pips and legible metadata groupings.

## Colors

The palette is engineered exclusively for high-contrast dark environments, fulfilling WCAG AAA contrast standards against deep midnight obsidian backgrounds.

### Palette Architecture
- **Primary (`#8B5CF6` - Neon Electric Violet):** Key interactive focal points, active state indicators, floating action buttons (FAB), and primary progress arcs.
- **Secondary (`#06B6D4` - Cyan Flare):** Secondary highlights, episode release countdowns, upcoming broadcast indicators, and metadata tags.
- **Tertiary (`#F43F5E` - Crimson Hype):** Live broadcast indicators ("Airing Now"), urgent alerts, dropping today pips, and favorite heart actions.
- **Neutral / Canvas (`#0F111A` - Deep Obsidian Slate):** Base screen background absorbing ocular glare.

### Material 3 Surface Containers
- **Surface (`#0F111A`):** Master screen canvas.
- **Surface Container Lowest (`#131520`):** Recessed media wrappers, progress track backgrounds.
- **Surface Container Low (`#161824`):** Section card groups, episode grid tiles.
- **Surface Container (`#1C1F2E`):** Standard media cards, bottom navigation bar surface.
- **Surface Container High (`#23273A`):** Modals, bottom sheets, filter chips in unselected states.
- **Surface Container Highest (`#2C3149`):** Hover/press overlays, active outline frames, segmented controls.

### Semantic Status Tokens
- **Status Airing:** Secondary Cyan `#06B6D4` with glowing container `rgba(6, 182, 212, 0.15)`.
- **Status Live Now:** Tertiary Crimson `#F43F5E` with container `rgba(244, 63, 94, 0.16)`.
- **Status Completed:** Emerald `#10B981` with container `rgba(16, 185, 129, 0.15)`.
- **On-Surface:** `#F1F3FD` (high-emphasis copy, title text).
- **On-Surface Variant:** `#9CA3AF` (metadata, studio names, broadcast air times).

## Typography

The typographical pairing provides clear narrative division: **Plus Jakarta Sans** governs content titles, synopses, and natural language with geometric clarity, while **Space Grotesk** serves numeric broadcast countdowns, episode tags (`EP 12/24`), season numbers, and tracking statuses.

### Typographic Rules
- **Episodic Data & Badges:** Must always use `label-md` or `label-sm` with Space Grotesk in uppercase to reinforce programmatic precision.
- **Anime Romaji/English Titles:** Rendered via `headline-md` or `title-md` using a `-0.01em` tracking for tight, impactful visual grouping on mobile cards.
- **Relative Countdowns:** ("in 2h 14m") should utilize `label-lg` with tabular numbers.

## Layout & Spacing

A mobile-first fluid layout based on an 8-point spatial grid system (with 4-point micro-steps for tight badge insets and chips).

### Grid Structure
- **Mobile (<600px):** 4-column layout with 16px (`1rem`) outer margins and 12px or 16px gutters. Poster cards flow in 2-column asymmetric or balanced grids.
- **Tablet (600px - 1024px):** 8-column layout with 24px (`1.5rem`) margins and 16px gutters. Poster cards flow in 3 to 4 columns with docked navigation rails instead of bottom bars.

### Content Density
- Vertical rhythm follows strict `space-sm` (8px) between title and tags, `space-md` (16px) between list elements, and `space-xl` (32px) between major category carousels (e.g., "Airing Today", "Up Next on Watchlist").

## Elevation & Depth

In alignment with Material 3 Dark specifications, elevation does not rely on opaque directional black drop shadows, but rather on **tonal container layering** coupled with chromatic ambient glow.

### Elevation Levels
- **Level 0 (Flat):** `Surface` (`#0F111A`). No shadow. Used for base background.
- **Level 1 (Low):** `Surface Container Low` (`#161824`). Standard feed cards, episode list rows. Subtle 1px inner border: `inset 0 1px 0 0 rgba(255, 255, 255, 0.05)`.
- **Level 2 (Active/Floating):** `Surface Container` (`#1C1F2E`). Active card highlights, filter sheets. Shadow: `0 8px 24px -4px rgba(15, 17, 26, 0.8), 0 2px 6px -1px rgba(139, 92, 246, 0.08)`.
- **Level 3 (Overlays):** `Surface Container High` (`#23273A`). Modals, quick-view sheets, floating bottom navigation bar. Shadow: `0 16px 36px -6px rgba(0, 0, 0, 0.7), 0 0 20px 2px rgba(6, 182, 212, 0.06)`.

### Chromatic Glow Accents
Interactive and high-priority components (such as an episode "Watch Now" action or airing countdown marker) feature localized radial box-shadows using `rgba(139, 92, 246, 0.35)` or `rgba(6, 182, 212, 0.35)`.

## Shapes

The design system adopts M3's expressive curved aesthetic (`roundedness: 3`). Prominent rounding softens the tech-forward dark interface, giving it an approachable, premium streaming app feel.

### Component Radii Mapping
- **Pill (Full Rounding / `9999px`):** Status badges ("Airing", "Completed"), bottom navigation item indicators, buttons, filter chips, countdown chips.
- **Extra Large (`rounded-3xl` / 2rem - 32px):** Bottom sheets, full-width modal dialogs, featured hero spotlight banners.
- **Large (`rounded-2xl` / 1.5rem - 24px):** Anime cover poster cards, episode card containers, search floating searchbar.
- **Medium (`rounded-lg` / 0.75rem - 12px):** Inner thumbnails, progress bar handles, tooltip popups.

## Components

### Buttons
- **Filled Primary:** Background `#8B5CF6`, text `#FFFFFF`, full pill shape (`rounded-full`). Padding: 12px 24px. Height: 48px. Active press scales down slightly (`scale(0.98)`).
- **Tonal Secondary:** Background `rgba(6, 182, 212, 0.15)`, text `#06B6D4`, 1px border in `rgba(6, 182, 212, 0.3)`. Full pill shape.
- **Outlined / Ghost:** Surface container backdrop with 1px border `rgba(255, 255, 255, 0.12)`, text `#F1F3FD`.

### Status Badges & Chips
- **Airing Status Badge:** Pill shape, height 24px. Padding: 2px 10px. Space Grotesk `label-sm`. Background `rgba(6, 182, 212, 0.16)`, border `1px solid rgba(6, 182, 212, 0.4)`, text `#06B6D4`. Features an embedded 6px pulsing dot.
- **Live Broadcast Badge:** Background `rgba(244, 63, 94, 0.2)`, border `1px solid rgba(244, 63, 94, 0.5)`, text `#F43F5E`.
- **Filter Chips:** Height 32px, `rounded-full`. Inactive: `Surface Container High` (`#23273A`), text `#9CA3AF`. Active: Background `#8B5CF6`, text `#FFFFFF`.

### Cards (Media & Anime Tracker)
- **Aspect Ratio:** Primary poster cards use a 3:4.2 portrait aspect ratio.
- **Surface:** `Surface Container Low` (`#161824`) with `rounded-2xl` (24px) corners.
- **Image Treatment:** Image features a vertical linear gradient overlay at the bottom (`linear-gradient(180deg, rgba(15,17,26,0) 40%, rgba(22,24,36,0.95) 100%)`) allowing on-surface titles to remain legible over artwork.
- **Card Overlays:** Floating status chip anchored at top-left (`space-sm` offset), floating bookmark toggle at top-right with translucent blur `rgba(15, 17, 26, 0.6)`.

### Bottom Navigation Bar (M3 Floating)
- **Bar Container:** Positioned 16px above the device bottom safe area, `rounded-full`, backdrop blur `16px`, background `rgba(28, 31, 46, 0.88)`, border `1px solid rgba(255, 255, 255, 0.08)`.
- **Active Tab Pill Indicator:** Pill indicator (`64px x 32px`) in `#8B5CF6` with primary icon in `#FFFFFF`. Inactive icons rendered in `#9CA3AF`.

### Input Fields & Search
- **SearchBar Container:** Pill shape, height 52px, background `Surface Container` (`#1C1F2E`), 1px border `rgba(255, 255, 255, 0.08)`.
- **Text:** Plus Jakarta Sans `body-md` in `#F1F3FD`, placeholder `#6B7280`. Leading search icon in `#8B5CF6`.

### Episode Progress Bar
- **Track:** Height 4px or 6px, `rounded-full`, background `Surface Container Highest` (`#2C3149`).
- **Indicator Fill:** High-contrast gradient from `#8B5CF6` to `#06B6D4`.