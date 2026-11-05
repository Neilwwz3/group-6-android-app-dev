# UI-1 Validation

Date: 2026-11-05 (Asia/Hong_Kong)

## Completed

- Resource XML parses successfully.
- No literal hex colors in res/layout.
- Git whitespace check passed.
- assembleDebug passed using existing dependency versions and installed SDK 37.
- APK installed and MainActivity launched successfully on Small_Phone (720 × 1280). Actual screenshot: [app-running.png](app-running.png). The existing Hello World startup screen is unchanged.
- The emulator reported a suggested 16GB system RAM with 8GB available, but startup and app launch succeeded; the emulator was closed after verification.
- No Java, Manifest, Gradle, app navigation or activity_main.xml changes.
- English sample strings are stored in strings.xml.
- Light-only Material 3 theme; night theme removed.

## Final component preview evidence

The user supplied three final screenshots on November 5, 2026. They are saved unchanged and embedded in `design-system.md` in scroll order:

- [Typography and brand colors](design-sample-01-typography-colors.png)
- [Semantic colors, watch status badges and filters](design-sample-02-status-filters.png)
- [Media card, read-only rating, floating action button and navigation illustration](design-sample-03-media-navigation.png)

The images show typography, the color palette, all four watch status badges, selected and unselected filters, the media card, read-only rating, FAB and four navigation placeholders. They are sample-page preview evidence; `app-running.png` separately documents startup of the unchanged application.

Preview environment: independent local copy with AGP 9.3.0, authorized by the user; the formal project remains on AGP 9.4.1.

## Build environment

The default Maven Central endpoint returned HTTP 403 for several existing dependencies. Verification used an untracked temporary local mirror populated with the same official Maven Central artifacts. No project repository configuration or dependency versions were changed. The successful command used Gradle's --offline --no-configuration-cache options and a temporary init script. A normal fresh build still needs working access to the configured dependency repositories.

## Remaining acceptance evidence

- Check large font and narrow-screen layout in Android Studio.
- Bottom navigation is a style/label illustration only, as expressly requested; no menu directory was added.
- T0 is a separate prerequisite; this UI-1 change does not include a member card.
- GitHub CI and peer review must pass before merging into dev.

## Caption update

The user requested that media cards omit “Read-only rating · Half-star steps”. The caption has been removed from the formal sample layout and local preview copy; rating behavior is documented only in the design guide. User-provided screenshots are preserved and therefore still show the earlier caption.
