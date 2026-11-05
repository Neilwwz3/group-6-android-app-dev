# MovieTracker Design System

UI-1 · English interface · Material Design 3 · light only

## Usage

Use `Theme.Group6androidappdev`. All interface copy belongs in `strings.xml`. Keep Java, navigation, Gradle and Manifest unchanged for UI-1. This design reuses the existing UI foundation blue (#2979FF), pale background (#F7F9FC) and white surfaces. Material 3 is the [component baseline](https://m3.material.io/).

## Colors

| Resource | Value | Use |
| --- | --- | --- |
| `primary` | `#2979FF` | primary |
| `on_primary` | `#FFFFFF` | on primary |
| `primary_container` | `#EAF2FF` | primary container |
| `on_primary_container` | `#174B9C` | on primary container |
| `background` | `#F7F9FC` | background |
| `surface` | `#FFFFFF` | surface |
| `surface_variant` | `#EDF1F7` | surface variant |
| `text_primary` | `#172033` | text primary |
| `text_secondary` | `#526176` | text secondary |
| `text_hint` | `#64748B` | text hint |
| `outline` | `#DCE3ED` | outline |
| `rating` | `#986400` | rating |
| `status_want_text` | `#174B9C` | status want text |
| `status_want_bg` | `#EAF2FF` | status want bg |
| `status_watching_text` | `#805400` | status watching text |
| `status_watching_bg` | `#FFF3D6` | status watching bg |
| `status_watched_text` | `#176245` | status watched text |
| `status_watched_bg` | `#E0F4EA` | status watched bg |
| `status_dropped_text` | `#8A3441` | status dropped text |
| `status_dropped_bg` | `#FCE8EC` | status dropped bg |
| `error` | `#BA1A1A` | error |
| `on_error` | `#FFFFFF` | on error |

Use each `status_*_text` with its matching `status_*_bg`; status labels must remain visible so color is never the only signal. The retained brand blue is used for accents; small selected text uses the darker `on_primary_container`.

## Dimensions

| Resource | Value | Use |
| --- | --- | --- |
| `text_headline` | `32sp` | text headline |
| `text_title` | `24sp` | text title |
| `text_card_title` | `16sp` | text card title |
| `text_body` | `14sp` | text body |
| `text_caption` | `12sp` | text caption |
| `space_4` | `4dp` | space 4 |
| `space_8` | `8dp` | space 8 |
| `space_12` | `12dp` | space 12 |
| `space_16` | `16dp` | space 16 |
| `space_24` | `24dp` | space 24 |
| `corner_card` | `16dp` | corner card |
| `corner_badge` | `8dp` | corner badge |
| `corner_button` | `12dp` | corner button |
| `badge_horizontal_padding` | `12dp` | badge horizontal padding |
| `badge_vertical_padding` | `6dp` | badge vertical padding |
| `poster_width` | `88dp` | poster width |
| `poster_height` | `132dp` | poster height |
| `touch_target` | `48dp` | Minimum interactive target |
| `swatch_width` | `32dp` | Width of a palette swatch |
| `icon_size` | `24dp` | Navigation icon and palette swatch height |
| `card_outline_width` | `1dp` | Card outline thickness |

Use 16dp screen padding and the 4/8/12/16/24dp spacing scale. Cards have 16dp corners, badges 8dp and buttons 12dp. Button shapes can use `ShapeAppearance.Group6.Button`. Use wrap-content for English labels and keep interactive touch targets at least 48dp.

## Styles

| Resource | Use |
| --- | --- | --- |
| `TextAppearance.Group6.Headline` | 32sp large display heading |
| `TextAppearance.Group6.Title` | 24sp page heading |
| `TextAppearance.Group6.CardTitle` | 16sp card or section heading |
| `TextAppearance.Group6.Body` | 14sp body copy |
| `TextAppearance.Group6.Caption` | 12sp supporting copy |
| `Widget.Group6.Card` | Outlined Material 3 card on white surface |
| `Widget.Group6.StatusBadge` | Base badge; use a state variant |
| `Widget.Group6.StatusBadge.Want` | Want to Watch status |
| `Widget.Group6.StatusBadge.Watching` | Watching status |
| `Widget.Group6.StatusBadge.Watched` | Watched status |
| `Widget.Group6.StatusBadge.Dropped` | Dropped status |
| `Widget.Group6.FilterChip` | Checkable filter; checked container and text change together |
| `Widget.Group6.RatingBar.Small` | Read-only five-star rating, half-star steps |
| `Widget.Group6.BottomNavigation` | Primary selected icon/label; gray unselected items |
| `Widget.Group6.Fab` | Primary add action |
| `ShapeAppearance.Group6.Button` | 12dp button shape override |

## Rating and text

Ratings use 0–10 in product data and 0–5 stars in the small RatingBar: set the displayed rating to score / 2. A score of 8/10 displays four stars. Override the rating content description for each item using a formatted string. Do not reuse the sample description for production data. No editable rating behavior is delivered in UI-1.

Do not display the implementation note “Read-only rating · Half-star steps” on media cards or in the component sample. Show only the stars and score. This behavior is documented here for developers.

The interface uses system typography, English labels and no downloaded font. Labels: Want to Watch, Watching, Watched, Dropped. Date examples should use an English locale. The fictional sample movie and placeholder poster are original content.

## Preview and scope

Open `app/src/main/res/layout/design_sample.xml` in Android Studio Split or Design. The preview declares the application theme. It includes all five text levels, palette swatches, four badges, selected/unselected filter chips, a read-only rating, fictional media card and FAB.

The four navigation labels are a visual illustration only. The actual styled BottomNavigationView has no menu and therefore no working items. This follows the user instruction to keep the exact allowed directory scope: no res/menu resources are added. No app navigation or business page is changed.

## Verification

See `validation.md` for build results and remaining manual checks. Android Studio preview screenshots and emulator screenshots must be captured from actual rendering; they must not be replaced with a mockup.

## Final component preview screenshots

User-provided final screenshots captured on November 5, 2026 at 20:36–20:37 (Asia/Hong_Kong). These show successive, overlapping sections of the scrollable `design_sample.xml` page in the authorized local AGP 9.3.0 preview copy. The formal deliverable retains AGP 9.4.1 and the original Gradle configuration. Original screenshot pixels are preserved. These screenshots precede removal of the “Read-only rating · Half-star steps” caption; the current layout omits that caption as requested.

### 1. Typography and brand colors

![Typography and brand colors](design-sample-01-typography-colors.png)

### 2. Semantic colors, watch status badges and filters

![Semantic colors, watch status badges and filters](design-sample-02-status-filters.png)

### 3. Media card, read-only rating, floating action button and navigation illustration

![Media card, read-only rating, floating action button and navigation illustration](design-sample-03-media-navigation.png)

## App runtime evidence

![Actual app runtime screenshot; existing startup page](app-running.png)

This verifies app startup only. It is not a screenshot of the component sample page.

## Component usage examples

Apply text styles through `android:textAppearance`; apply widget styles through `style`. Use `@color/text_hint` for hints, `@color/text_secondary` for supporting information, `@color/outline` for dividers/outlines and `@color/rating` for rating stars. Pair `primary_container` with `on_primary_container` for selected containers. Use `surface_variant` for poster placeholders, `surface` for cards and `background` for the screen.

```xml
<TextView
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:text="@string/status_watching"
    style="@style/Widget.Group6.StatusBadge.Watching" />
```

```xml
<com.google.android.material.chip.Chip
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:text="@string/sample_movie"
    android:checked="true"
    style="@style/Widget.Group6.FilterChip" />
```

For production cards, supply a per-item rating content description in the layout or binding. The reusable rating style deliberately contains no sample-specific accessible label. `AppCompatImageView` is used when `app:srcCompat` and `app:tint` are needed.

## Local preview environment

The installed Android Studio Quail 3 (2026.1.3) rejects this project's AGP 9.4.1 during sync because it supports through 9.3.0. The user authorized an independent local preview copy using AGP 9.3.0. That copy is for visual verification only; the deliverable retains the project's original AGP and Gradle files. Screenshot captions must identify the preview environment.
