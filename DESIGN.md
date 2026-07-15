---
version: alpha
name: HandyLog-design-analysis
description: A dark-first, poker-felt mobile design system for a hand-tracking app (Android + iOS, Compose Multiplatform). The canvas is a near-black blue-grey (#14171E) with cards floating one step lighter (#1D212A); a single emerald voltage — HandyLog primary (#0FB67F) — carries every primary action, the FAB, focus rings, switch tracks and the active brand wordmark. A second, strictly rationed accent is gold (#F7C530), reserved almost exclusively for pot and chip amounts — money is the only thing allowed to glow. Felt green (#235C41) anchors the brand mark (a spade in a rounded felt tile) and the poker-table surfaces. Type is Pretendard across the whole scale, leaning on Bold for headers and Medium/Regular for body — a 2px-stepped scale from 8 up to 32. The shape language is soft but tight: an 8px radius (`{rounded.md}`) governs buttons, fields, segmented tabs and the brand tile, dialogs round to 12px (`{rounded.lg}`), and only the FAB and switch thumb go fully round (`{rounded.full}`). Card suits get their own two-color sub-palette — suit-red (#E83030) for hearts/diamonds, near-white for clubs/spades. There is no central spacing or shape token file; values live inline in `core/designsystem` components, but they converge tightly on a 4px grid.

colors:
  primary: "#0FB67F"
  on-primary: "#000000"
  accent: "#22C38D"
  on-accent: "#F2F2F2"
  background: "#14171E"
  card: "#1D212A"
  modal-background: "#191C24"
  muted: "#272B34"
  secondary: "#2F3541"
  on-secondary: "#F2F2F2"
  text-primary: "#F2F2F2"
  text-secondary: "#808897"
  border: "#4A5060"
  input-border: "#414855"
  focus-ring: "#0FB67F"
  felt: "#235C41"
  felt-light: "#2D6B4E"
  gold: "#F7C530"
  gold-muted: "#C39A22"
  split: "#E88A30"
  error: "#DC2828"
  suit-red: "#E83030"
  suit-black: "#F2F2F2"

typography:
  bold32:
    fontFamily: "Pretendard"
    fontSize: 32px
    fontWeight: 700
    lineHeight: 40px
  bold24:
    fontFamily: "Pretendard"
    fontSize: 24px
    fontWeight: 700
    lineHeight: 32px
  bold22:
    fontFamily: "Pretendard"
    fontSize: 22px
    fontWeight: 700
    lineHeight: 30px
  bold20:
    fontFamily: "Pretendard"
    fontSize: 20px
    fontWeight: 700
    lineHeight: 28px
  bold18:
    fontFamily: "Pretendard"
    fontSize: 18px
    fontWeight: 700
    lineHeight: 26px
  bold16:
    fontFamily: "Pretendard"
    fontSize: 16px
    fontWeight: 700
    lineHeight: 24px
  medium16:
    fontFamily: "Pretendard"
    fontSize: 16px
    fontWeight: 500
    lineHeight: 24px
  regular16:
    fontFamily: "Pretendard"
    fontSize: 16px
    fontWeight: 400
    lineHeight: 24px
  medium14:
    fontFamily: "Pretendard"
    fontSize: 14px
    fontWeight: 500
    lineHeight: 20px
    letterSpacing: 0.25px
  regular14:
    fontFamily: "Pretendard"
    fontSize: 14px
    fontWeight: 400
    lineHeight: 20px
  bold12:
    fontFamily: "Pretendard"
    fontSize: 12px
    fontWeight: 700
    lineHeight: 16px
  regular12:
    fontFamily: "Pretendard"
    fontSize: 12px
    fontWeight: 400
    lineHeight: 16px
  bold10:
    fontFamily: "Pretendard"
    fontSize: 10px
    fontWeight: 700
    lineHeight: 14px
  regular10:
    fontFamily: "Pretendard"
    fontSize: 10px
    fontWeight: 400
    lineHeight: 14px
  bold8:
    fontFamily: "Pretendard"
    fontSize: 8px
    fontWeight: 700
    lineHeight: 12px

rounded:
  none: 0px
  xs: 4px
  sm: 6px
  md: 8px
  lg: 12px
  full: 9999px

spacing:
  xxs: 2px
  xs: 4px
  sm: 6px
  base: 8px
  md: 10px
  lg: 12px
  content: 16px
  xl: 20px
  xxl: 24px
  section: 32px

components:
  button-primary:
    backgroundColor: "{colors.primary}"
    textColor: "{colors.on-primary}"
    typography: "{typography.bold18}"
    rounded: "{rounded.md}"
    padding: 12px 4px
    width: fill
  button-primary-disabled:
    backgroundColor: "{colors.secondary}"
    textColor: "{colors.on-secondary}"
    rounded: "{rounded.md}"
  button-sub:
    backgroundColor: "{colors.muted}"
    textColor: "{colors.text-secondary}"
    typography: "{typography.bold18}"
    rounded: "{rounded.md}"
  fab:
    backgroundColor: "{colors.primary}"
    textColor: "{colors.on-primary}"
    rounded: "{rounded.full}"
  text-field:
    backgroundColor: "{colors.muted}"
    textColor: "{colors.text-primary}"
    typography: "{typography.regular14}"
    rounded: "{rounded.md}"
    border: "1px {colors.input-border}"
    borderFocused: "1px {colors.primary}"
    padding: 10px 12px
    cursor: "{colors.primary}"
  section-label:
    textColor: "{colors.text-secondary}"
    typography: "{typography.regular10}"
    paddingBottom: 6px
  top-nav:
    backgroundColor: "{colors.background}"
    textColor: "{colors.text-primary}"
    typography: "{typography.medium16}"
    minHeight: 50px
  top-nav-icon:
    height: 50px
    rounded: "{rounded.xs}"
  top-nav-icon-button:
    backgroundColor: "{colors.primary}"
    textColor: "{colors.text-primary}"
    typography: "{typography.medium14}"
    rounded: "{rounded.sm}"
    padding: 6px 12px
  brand-logo:
    tile-backgroundColor: "{colors.felt}"
    tile-rounded: "{rounded.md}"
    tile-size: 28px
    icon-color: "{colors.primary}"
    wordmark-handy-color: "{colors.primary}"
    wordmark-log-color: "{colors.text-primary}"
    typography: "{typography.bold16}"
  segmented-tab:
    activeBackgroundColor: "{colors.secondary}"
    activeTextColor: "{colors.text-primary}"
    inactiveBackgroundColor: "{colors.muted}"
    inactiveTextColor: "{colors.text-secondary}"
    typography: "{typography.medium14}"
    rounded: "{rounded.md}"
  switch:
    checkedTrackColor: "{colors.primary}"
    uncheckedTrackColor: "{colors.secondary}"
    thumbColor: "{colors.on-primary}"
    labelCheckedColor: "{colors.primary}"
    labelUncheckedColor: "{colors.text-secondary}"
    labelTypography: "{typography.bold12}"
  bottom-sheet:
    backgroundColor: "{colors.card}"
    textColor: "{colors.text-primary}"
    titleTypography: "{typography.bold20}"
    padding: 0px 16px 32px
  dialog:
    backgroundColor: "{colors.modal-background}"
    rounded: "{rounded.lg}"
    padding: 18px
    titleTypography: "{typography.bold22}"
    contentTypography: "{typography.regular16}"
    descriptionTypography: "{typography.regular14}"
    descriptionColor: "{colors.text-secondary}"
    warningTypography: "{typography.bold18}"
  chip-amount:
    textColor: "{colors.gold}"
    typography: "{typography.bold16}"
---

## Overview

HandyLog is a **dark-first, poker-felt mobile design system** for a poker hand-tracking app shipped on Android and iOS via Compose Multiplatform. The base canvas is a near-black blue-grey (`{colors.background}` — #14171E), with content cards floating exactly one step lighter (`{colors.card}` — #1D212A) and modals one step different again (`{colors.modal-background}` — #191C24). Depth is built from these stepped greys plus a single 8px-radius rounding language, not from heavy shadows.

A single brand voltage — **emerald** (`{colors.primary}` — #0FB67F) — carries every primary action: the `{component.button-primary}` fill, the `{component.fab}`, text-field focus borders and cursor, switch tracks, the active brand wordmark, and the spade icon in the logo tile. It is the one color that means "go."

The system rations a **second** accent with unusual discipline: **gold** (`{colors.gold}` — #F7C530) is reserved almost entirely for **pot and chip amounts**. In a poker tracker, money is the signal — so money is the only thing allowed to glow gold. This is the system's strongest opinion, and it mirrors the `formatChips` / `formatBbCount` output path in `core/ui/poker/ChipFormatter.kt`.

A third family, **felt green** (`{colors.felt}` — #235C41 / `{colors.felt-light}` — #2D6B4E), grounds the brand: the logo mark is a spade sitting in a rounded felt tile, and the same green renders poker-table surfaces in record/detail screens.

Type is **Pretendard** end to end (Bold / Medium / Regular faces, loaded in `theme/Typography.kt`), on a tightly-stepped scale that runs from 8px to 32px in even 2px increments — consistent with the project rule that font sizes are even-only. Headers lean Bold; body runs Medium/Regular.

The shape language is **soft but tight**. An 8px radius (`{rounded.md}`) governs the majority of interactive surfaces — buttons, text fields, segmented tabs, the brand tile. Dialogs round to 12px (`{rounded.lg}`), top-bar icon hit-areas to 4px (`{rounded.xs}`), the icon-button chip to 6px (`{rounded.sm}`), and only the FAB and switch thumb go fully circular (`{rounded.full}`).

**Key Characteristics:**
- Dark by default. `LocalDarkTheme` defaults to `true` (`theme/Theme.kt`); a full, separately-tuned light scheme (`LightHandyColorScheme`) exists and is provided when the system is in light mode.
- Single emerald accent: `{colors.primary}` (#0FB67F) carries every "go" action — `{component.button-primary}`, `{component.fab}`, focus border, cursor, switch track, active label.
- Gold is money-only: `{colors.gold}` (#F7C530) is scoped to pot / chip / stack amounts. It almost never appears as decoration.
- Dual semantic sub-palettes the marketplace systems don't have: **card suits** (`{colors.suit-red}` #E83030 for hearts/diamonds, `{colors.suit-black}` near-white for clubs/spades) and **felt** for table surfaces.
- Pretendard on an even-only 2px-stepped scale (8 → 32). `HandyTypography` exposes Bold/Medium/Regular variants of nearly every step.
- Soft, consistent rounding centered on 8px (`{rounded.md}`); essentially no hard corners on interactive elements.
- No centralized spacing/shape token file — values are inline per component but converge on a 4px grid (with 2px micro-steps).
- Theme access is funneled through `HandyTheme.colorScheme` / `HandyTheme.typography`; direct `MaterialTheme` use is forbidden by project rule.

## Colors

Values below are the **dark** scheme (`DarkHandyColorScheme` in `theme/Color.kt`), the app default. The light scheme (`LightHandyColorScheme`) is listed where it diverges.

### Brand & Accent
- **Primary / Emerald** (`{colors.primary}` — #0FB67F; light #0EA875): The single brand voltage. Primary CTA fills, FAB, text-field focus border + cursor, switch checked track, active brand wordmark, logo spade. Text on top of it is near-black (`{colors.on-primary}` — #000000; light #FFFFFF).
- **Accent** (`{colors.accent}` — #22C38D; light #1EAD7D): A slightly brighter emerald for emphasis/highlight states, paired with `{colors.on-accent}` (#F2F2F2).

### Surface (stepped greys)
- **Background** (`{colors.background}` — #14171E; light #F9F9F9): The app floor.
- **Card** (`{colors.card}` — #1D212A; light #FFFFFF): Panels, bottom-sheet container, list cards.
- **Modal Background** (`{colors.modal-background}` — #191C24; light #FFFFFF): Dialog surfaces.
- **Muted** (`{colors.muted}` — #272B34; light #F0F1F4): Inactive/recessed fills — text-field background, inactive segmented tab, sub-button background.
- **Secondary** (`{colors.secondary}` — #2F3541; light #E7E9ED): Secondary element background — disabled button fill, active segmented tab, switch unchecked track. On-color is `{colors.on-secondary}` (#F2F2F2; light #353C49).

### Text
- **Text Primary** (`{colors.text-primary}` — #F2F2F2; light #181C24): Headlines, body, primary labels. Never pure white in dark mode.
- **Text Secondary** (`{colors.text-secondary}` — #808897; light #676E7E): Sub-labels, section labels, placeholders (often at 50% alpha), inactive tab text.

### Borders
- **Border** (`{colors.border}` — #4A5060; light #DCDEE4): General hairlines.
- **Input Border** (`{colors.input-border}` — #414855; light #E1E4E9): Text-field resting outline; flips to `{colors.primary}` on focus.
- **Focus Ring** (`{colors.focus-ring}` — #0FB67F): Same token as primary — focus is expressed as an emerald border, no glow.

### Poker-domain
- **Felt / Felt Light** (`{colors.felt}` — #235C41 / `{colors.felt-light}` — #2D6B4E): Brand tile + poker-table surfaces.
- **Gold / Gold Muted** (`{colors.gold}` — #F7C530 / `{colors.gold-muted}` — #C39A22): Pot/chip/stack amounts (gold), with the muted tone for secondary money text.
- **Split** (`{colors.split}` — #E88A30; light #D97B1E): Orange for split-pot results.
- **Suit Red** (`{colors.suit-red}` — #E83030; light #E51919): Hearts & diamonds.
- **Suit Black** (`{colors.suit-black}` — #F2F2F2 dark / #414857 light): Clubs & spades — near-white on dark so cards read on the felt.

### Semantic
- **Error** (`{colors.error}` — #DC2828, both themes): Delete/warning actions and destructive confirmations.

## Typography

### Font Family
The entire scale runs **Pretendard** (`theme/Typography.kt` → `pretendard`), bundling three static faces — Bold (700), Medium (500), Regular (400). There is no separate display family; the same family carries display, body, navigation, captions and microcopy. `HandyTypography.with(fontFamily)` injects the family at theme-provision time.

A `nonScaledSp` extension is provided so selected styles can opt out of the OS font-scale (used where layout integrity matters more than user text-scaling).

### Hierarchy
`HandyTypography` exposes Bold / Medium / Regular variants at most steps; the table lists the ones with observed roles. The scale is **even-only** (8, 10, 12, 14, 16, 18, 20, 22, 24, 26, 28, 30, 32) per the project rule.

| Token | Size | Weight | Line Height | Use |
|---|---|---|---|---|
| `{typography.bold32}` | 32px | 700 | 40px | Largest display / empty-state headers |
| `{typography.bold24}` | 24px | 700 | 32px | Screen-level headers |
| `{typography.bold22}` | 22px | 700 | 30px | Dialog title (`{component.dialog}`) |
| `{typography.bold20}` | 20px | 700 | 28px | Bottom-sheet title (`{component.bottom-sheet}`) |
| `{typography.bold18}` | 18px | 700 | 26px | Primary button label; dialog warning line |
| `{typography.bold16}` | 16px | 700 | 24px | Brand wordmark ("HandyLog") |
| `{typography.medium16}` | 16px | 500 | 24px | Top-app-bar title (`{component.top-nav}`) |
| `{typography.regular16}` | 16px | 400 | 24px | Dialog body copy |
| `{typography.medium14}` | 14px | 500 | 20px | Segmented tab + icon-button labels (0.25px tracking) |
| `{typography.regular14}` | 14px | 400 | 20px | Text-field input + placeholder; dialog description |
| `{typography.bold12}` | 12px | 700 | 16px | Switch label |
| `{typography.regular12}` | 12px | 400 | 16px | Dense meta lines |
| `{typography.bold10}` / `{typography.regular10}` | 10px | 700 / 400 | 14px | Section label (`{component.section-label}`), micro-tags |
| `{typography.bold8}` | 8px | 700 | 12px | Smallest badge / overlay micro-labels |

### Principles
Headers are **Bold**; the system reaches for weight, not size, to establish hierarchy on small screens. The loudest type is `{typography.bold32}`, reserved for empty states and top-level headers. Money amounts get their hierarchy from **color** (`{colors.gold}`) rather than a larger size — a chip value at `{typography.bold16}` gold reads louder than a 20px grey label.

### Note on Font Substitutes
If Pretendard is unavailable, **Inter** or **Spoqa Han Sans / system Korean** stacks are the closest substitutes; Pretendard's metrics are Inter-adjacent, so the even-step scale transfers with negligible line-height drift.

## Layout

### Spacing System
- **Base unit:** 4px grid, with frequent 2px micro-steps. There is **no central spacing token file** — values are passed inline in each `core/designsystem` component — but they converge on the set below.
- **Tokens (observed):** `{spacing.xxs}` 2px · `{spacing.xs}` 4px · `{spacing.sm}` 6px · `{spacing.base}` 8px · `{spacing.md}` 10px · `{spacing.lg}` 12px · `{spacing.content}` 16px · `{spacing.xl}` 20px · `{spacing.xxl}` 24px · `{spacing.section}` 32px.
- **Screen / content gutter:** `{spacing.content}` (16px) horizontal — the standard page inset (bottom sheet, forms).
- **Button gaps:** `{spacing.base}` (8px) between paired buttons (sheet/dialog footers).
- **Text-field interior:** 12px horizontal / 10px vertical; icon-button interior 12px / 6px.
- **Bottom-sheet bottom inset:** `{spacing.section}` (32px), to clear the gesture area.

### Container & Structure
- **Single-column mobile** layouts throughout; full-width buttons (`{component.button-primary}` calls `fillMaxWidth()`).
- **Top app bar:** `minHeight` 50px (`{component.top-nav}`), with a leading 50px icon hit-area, centered title, and an optional trailing icon-button or custom end content; a `Main` variant swaps the leading slot for the brand logo.
- **Bottom sheet:** the dominant modal surface — title (`{typography.bold20}`) → scrollable content → 0–2 footer buttons laid out in an 8px-gap row, each `weight(1f)`.
- **Dialogs:** centered `Surface` at `{rounded.lg}` over a scrim, content center-aligned.

### Whitespace Philosophy
Mobile-dense, not editorial. Content sits on 16px gutters with 8–12px internal rhythm; the system trusts the stepped-grey surface separation (background → card → modal) and rounding to delineate regions rather than large whitespace bands.

## Elevation

The system is **near-flat**, expressing depth through stepped surface greys and rounded clipping rather than shadow ramps.

- **Surface stepping (primary depth cue):** `{colors.background}` (#14171E) → `{colors.card}` (#1D212A) → `{colors.modal-background}` (#191C24). Each layer up the z-order is a slightly lighter grey.
- **Bottom sheet / dialog:** rendered on `{colors.card}` / `{colors.modal-background}` with the platform `ModalBottomSheet` / `Dialog` scrim beneath — the only "elevation" most surfaces get.
- **FAB:** the one element carrying Material's default floating shadow (`FloatingActionButton`), reinforced by its emerald fill standing off the dark canvas.

There are no progressive elevation tiers. On dark, a lighter surface *is* the elevation.

## Components

### Buttons

**`button-primary`** (`RegularButton`) — Emerald fill (`{colors.primary}`), near-black label (`{colors.on-primary}`), `{typography.bold18}`, `{rounded.md}` (8px), 12px vertical / 4px horizontal padding, `fillMaxWidth`. Includes a built-in multiple-click cutter (`MultipleEventsCutter`) by default. The universal CTA — "Save", "Done", "Next".

**`button-primary-disabled`** — When `enabled = false`, fill flips to `{colors.secondary}` and label to `{colors.on-secondary}`; click handling is removed.

**`button-sub`** — The same `RegularButton` recolored for secondary/destructive footer actions: `{colors.muted}` fill, `{colors.text-secondary}` label (e.g. "Reset", "Delete" in a sheet footer).

**`fab`** (`HandyFab`) — Circular (`{rounded.full}`) emerald FAB, `{colors.on-primary}` icon, default a `plus` glyph. The primary "record / add" entry point.

### Forms

**`text-field`** (`HandyTextField`) — A `BasicTextField` in a `{colors.muted}` rounded box (`{rounded.md}`), 1px `{colors.input-border}` outline that flips to `{colors.primary}` on focus (no glow/ring). 12px/10px padding, `{typography.regular14}` input, emerald cursor. Placeholder is `{colors.text-secondary}` at 50% alpha. Optional leading icon (12px, secondary tint); a clear ("x") button scales in when focused + non-empty. Numeric mode routes through `amountVisualTransformation()` for grouped money input.

**`section-label`** (`HandySectionLabel`) — A tiny field/section caption: `{typography.regular10}`, `{colors.text-secondary}`, 6px bottom padding, optionally wrapping child content. The standard label above inputs.

### Navigation

**`top-nav`** (`HandyTopAppbar`) — `{colors.background}` surface, `minHeight` 50px. Default variant: leading 50px back-icon hit-area (`{rounded.xs}`), centered title (`{typography.medium16}`, `{colors.text-primary}`, ellipsized), optional trailing icon-button / end content / sub-content row. `Main` variant replaces the leading slot with the brand logo.

**`top-nav-icon-button`** — A compact emerald chip (`{colors.primary}`, `{rounded.sm}` 6px, 12px/6px padding) holding a 14px icon and an optional `{typography.medium14}` label — e.g. the "Settings" action on a detail bar.

**`brand-logo`** (`HomeLogo`) — The identity mark: a 28px `{colors.felt}` tile rounded to `{rounded.md}` containing an emerald (`{colors.primary}`) spade glyph, followed by the wordmark — **"Handy"** in emerald and **"Log"** in `{colors.text-primary}`, both `{typography.bold16}`. When a screen title is supplied, the wordmark is replaced by that title in `{typography.medium16}`.

### Selection Controls

**`segmented-tab`** (`HandySegmentedTab`) — A single-choice segmented row built on M3 `SegmentedButton`, base shape `{rounded.md}`. Active segment: `{colors.secondary}` fill / `{colors.text-primary}`; inactive: `{colors.muted}` fill / `{colors.text-secondary}`; labels `{typography.medium14}`. Borders match the fills (no contrasting outline). Used for "Tables / Hands", "Cash / Tournament".

**`switch`** (`HandySwitch`) — M3 `Switch` with a `{colors.primary}` checked track and `{colors.secondary}` unchecked track, `{colors.on-primary}` thumb (custom-drawn circle). An optional leading label flips from `{colors.text-secondary}` to `{colors.primary}` when checked, in `{typography.bold12}`. Minimum interactive size is overridden to 0 for compact placement (e.g. the BB-unit toggle).

### Modals

**`bottom-sheet`** (`HandyBottomSheet`) — The app's primary modal. `ModalBottomSheet` with `skipPartiallyExpanded`, `{colors.card}` container, 16px horizontal / 32px bottom padding. Structure: title (`{typography.bold20}`) → scrollable content → an optional footer row of 1–2 `RegularButton`s (8px gap, each `weight(1f)`); the sub-button uses the `{component.button-sub}` recoloring. A `SheetDragBlocker` nested-scroll connection keeps inner scroll from dragging the sheet.

**`dialog`** (`BaseDialog` / `DefaultDialog` / `ConfirmDialog`) — Centered `Surface` rounded to `{rounded.lg}` (12px) on `{colors.modal-background}`, 18px padding. Center-aligned stack: title (`{typography.bold22}`) → content (`{typography.regular16}`) → optional description (`{typography.regular14}`, `{colors.text-secondary}`) → optional warning (`{typography.bold18}`) → button row. `ButtonDialog` lays out a negative/positive pair via `ModalButton` (`isNegative` selects emphasis); `ConfirmDialog` is a single full-width confirm.

### Domain primitives

**`chip-amount`** — Not a single file but a consistent treatment: pot/chip/stack figures render in `{colors.gold}` (often `{typography.bold16}`), formatted through `formatChips` / `formatBbCount` / `formatAmountOrBb` (`core/ui/poker/ChipFormatter.kt`). Gold + these formatters together are the visual signature of "this number is money."

**Card suits** — Hearts/diamonds in `{colors.suit-red}`, clubs/spades in `{colors.suit-black}` (near-white on dark), rendered on white card plates or the `{colors.felt}` table surface.

> Additional shipped components not detailed above: `NumberSelector`, `CheckBox`, `ToggleGroup`, `PopupMenu`, `SettingsItem`, `Divider`, `HandyIconButton`, `Scaffold`, `ModalButton`. All follow the same token language (8px radius, emerald primary, Pretendard, stepped greys).

## Adaptive Behavior

This is a **mobile app**, not a responsive web surface — there is one column layout that scales with device width rather than breakpoint-driven reflow. The two real adaptive axes are:

| Axis | Behavior |
|---|---|
| **Theme** | Dark by default (`LocalDarkTheme = true`). A fully separate `LightHandyColorScheme` is provided when the OS is in light mode — every token has a tuned light counterpart (see Colors). |
| **Font scale** | Respects OS text scaling by default; specific layout-critical styles opt out via the `nonScaledSp` extension. |
| **Platform** | Single Compose Multiplatform UI shared across Android and iOS; no per-platform visual divergence in the design system. |
| **Localization** | Korean / English / Japanese / Chinese. All copy via `Res.string.*`; layouts are length-tolerant (ellipsized titles, wrapping body). |

### Touch Targets
- Top-bar icon hit-area is 50×50px.
- FAB uses the platform default (56px) circular target.
- Primary buttons are full-width with 12px vertical padding (comfortably above the 48px min with `{typography.bold18}` line box).

## Known Gaps

- **No central token files.** Spacing and corner radii are inline per component, not exported as named tokens; the values in this doc are the *de facto* system inferred from `core/designsystem`. A formal `Spacing`/`Shape` object would make them enforceable.
- **Light scheme under-documented in use.** `LightHandyColorScheme` is fully defined, but most screens are designed and previewed dark-first; light-mode component QA coverage is uncertain.
- **Poker-table / card rendering** (felt surfaces, card plates, action grid) live in `feature/record` and `feature/hand-detail`, not in `core/designsystem`, so their exact geometry isn't tokenized here.
- **Elevation** beyond the FAB's default shadow is effectively undocumented because the system avoids shadows — depth is surface-stepping, which isn't captured as a token.
- **Motion** (the text-field clear-button `ScaleInAnimation`, sheet transitions) exists in `component/Animation.kt` but isn't enumerated as a motion spec.
