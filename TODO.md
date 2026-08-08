# TODO — Gallery+ fork (Simple Gallery Pro 6.28.1)

## Visual parity with official Pro build

- [ ] **Editor look (biggest gap — BLOCKED)** — imgly PhotoEditorSDK (PESDK/VESDK) is NOT in the build.
      Official Pro's fancy editor = imgly lib, applied only when gradle task name contains
      "Proprietary" (`gradle/imglysdk.gradle` + `app/build.gradle.kts:150`). The imgly license
      files (`vesdk_android_license`, `pesdk_android_license`) were deleted from git history
      (`4217d5369`, `2e3753216`) and are nowhere on disk. Proprietary flavor can't build without
      them. Re-obtaining requires a per-app paid imgly license tied to this app's package id.
      Last build = `Gallery+-396-foss-debug.apk` (foss flavor) → basic home-made editor.
      Until a license is obtained, accept the foss editor look. NOT fixable without the license.
- [x] **Editor filter lib** — FALSE ALARM. Official `info.androidhive:imagefilters:1.0.7` and the
      fork's `com.github.naveensingh:androidphotofilters` both resolve to the same
      `com.zomato.photofilters` package; `EditActivity` imports `com.zomato.photofilters.FilterPack`
      in both. Filters already render identically — no change needed.
- [x] **rtl-viewpager** — local Simple-Commons build uses
      `com.github.naveensingh:rtl-viewpager:2.0.2` instead of official
      `com.github.duolingo:rtl-viewpager:940f12724f`. Same library, forked at a newer tag.
      No RTL behavior change expected. Left as-is (reverting requires rebuilding commons from the
      old duolingo tag — not worth it for no visual gain).

## Minor (FIXED)

- [x] `supportSwipeToRefresh="true"` restored on fastscroller in
      `activity_main.xml` + `activity_media.xml` (was stripped in the fork).
- [x] `settingsHolder.setBackgroundColor(...)` removed from SettingsActivity.kt — official uses
      `updateMaterialActivityViews(..., useTransparentNavigation = true)` for the bg, the extra
      line was a redundant override.

## Remote/encrypted folder plumbing (FIXED)

- [x] **Remote (FTP/SFTP) + encrypted folders vanished from the main folder list** —
      `MainActivity.checkInvalidDirectories` removed them after `getCachedDirectories`
      synthesized them: `remote://...` paths fail `File().exists()` and encrypted dirs
      contain only `.enc` files (no `isMediaFile`), so both landed in `invalidDirs`.
      Now skipped (kept) there.
- [x] **Opening a legacy remote server errored** (login failures) — servers added before
      the keystore migration (commit `7069244c7`) stored the plaintext password in the
      Gson `passwordHash` field; `getRemoteServerPassword(serverId)` only read
      EncryptedSharedPreferences → returned "" → FTP/SFTP auth failure toast.
      Now falls back to the legacy `passwordHash` and migrates it into the keystore once.
- [x] **Search box re-hides eye-icon-revealed folders** in the select-destination picker —
      `PickDirectoryDialog` keeps `allDirectories` synced on every fetch (committed),
      so search filters the hidden-included set. Verified current source keeps that sync.

## New feature: folders inside folders (DONE)

- [x] **Subfolder tiles inside a folder** — opening a folder now lists its direct
      subfolders as tappable tiles at the top of the media grid (`MediaActivity`):
      - New `models/FolderTile.kt` — wraps a `Directory` for the media-grid item model.
      - `MediaAdapter` renders folder tiles (reuses `DirectoryItemGridSquareBinding` /
        `DirectoryItemListBinding`) — new item type `ITEM_FOLDER_TILE`.
      - Tap a folder tile → opens the subfolder (new `MediaActivity` instance), back
        goes up one level. Encrypted subfolders decrypt to the temp folder like the
        main screen does; locked folders keep the auth dialog.
      - Hidden + excluded subfolders are filtered like the main folder list.
      - Bonus fix: a folder containing only subfolders is no longer treated as empty
        (was auto-deleted via `isDirEmpty`).

## Build/toolchain drift (no visual impact, tracked for awareness)

- [ ] Kotlin 1.8.22 → 1.9.22, AGP 7.4.0 → 8.6.0, compileSdk 34 → 35, minSdk 23 → 26,
      Room 2.6.0-beta01 → 2.5.2 (downgraded), gradle daemon off / Xmx1536m.
      Room downgrade could matter for future DB migrations. Left as-is.
- [ ] simple-commons resolved from local mavenLocal
      (`com.simplemobiletools.commons:release:5.34.26`, built from ../Simple-Commons @ 9e60e24)
      instead of JitPack. Same commit → same resources. Keep ../Simple-Commons present +
      published to mavenLocal before building.

## Done

- [x] Settings UI purple/override styles — removed local shadow styles in
      `app/src/main/res/values/styles.xml` + purple `colorPrimary` in `colors.xml`;
      commons' themed styles now apply (official look).
