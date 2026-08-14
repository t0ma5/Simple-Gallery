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

## New feature: folder-in-folder tree mode on the main screen (DONE)

- [x] **3-state folders/files toggle** in the top panel (the `show_all` icon): cycles
  `folders grid -> tree mode -> all media`. Tree mode = the main folder grid rendered as a
  file-manager-style hierarchy (folder-in-folder), so it is NOT the default.
  - Tree mode is a **pure in-memory `mTreeFolders` field** (like the native button modes) —
    the `config.treeFolders` pref and `TREE_FOLDERS` const were removed entirely; no startup
    restore.
  - `getTreeDirectories()` builds the tree from the **in-memory directory list** (no DB access on
    the UI thread — the earlier `directoryDB.getAll()` call crashed with Room's main-thread
    exception), nesting children beneath parents. Roots are folders whose direct parent is not
    itself in the list.
  - Tap a parent folder in tree mode expands/collapses it (`mExpandedTreeFolders`);
    tapping a leaf opens it. Indentation via `dirName` left padding.
  - `refreshMenuItems()` swaps the button icon folder<->**tree (`ic_tree_vector`, Material
    account_tree glyph)**; tree hides the column-count toggle.
- [x] **Tree filters** — hidden folders and excluded folders are skipped unless the user is in
  "temporarily show them" mode (`config.temporarilyShowHidden` / `temporarilyShowExcluded`);
  `.` / `..` pseudo-entries never appear; a folder never nests inside itself
  (`childPath != parentPath`).
- [x] **Media strip in tree rows** — expanding a folder that has pictures/videos shows its direct
  media as a horizontal thumbnail strip row right after the subfolders (`Directory.isTreeMediaStrip`
  + new viewType 1 in `DirectoryAdapter`, `directory_item_tree_media.xml`). Media is fetched off
  the UI thread via `mediaDB.getMediaFromPath` into `mTreeMediaCache`; tapping a strip thumbnail
  opens it in the pager. Strip rows are not selectable.
- [x] **In-folder tree toggle removed** — the old `MediaActivity`/`MediaAdapter` tree code,
  `FolderTile.kt`, and the `toggle_tree_mode` menu item were reverted (back to a plain media
  grid). The `ic_tree_vector` icon is reused for the main-screen toggle.
- [x] **Storage-type overlay icons on folder tiles** — `DirectoryAdapter.dirLocation` draws an
  overlay badge on the thumbnail: cloud (`remote://`), SD card (removable `ic_sd_card_filled_vector`),
  USB (OTG). **Local/internal folders get NO icon** (clean tile); `dirLocation.beVisibleIf(loc != 0)`.
- [x] **Encryption restored** — `tryEncryptFolder`/`tryDecryptFolder`, the `cab_encrypt`/`cab_decrypt`
  menu items + visibility rules, `dialog_lock_folder_options.xml`, and the related strings were
  restored after an earlier pass dropped them; lock+encrypt toggle (commons `SecurityDialog`
  `showEncryptToggle`) still present in `lockFolder()`.

## Build/toolchain drift (no visual impact, tracked for awareness)

- [ ] Kotlin 1.8.22 → 1.9.22, AGP 7.4.0 → 8.6.0, compileSdk 34 → 35, minSdk 23 → 26,
      Room 2.6.0-beta01 → 2.5.2 (downgraded), gradle daemon off / Xmx1536m.
      Room downgrade could matter for future DB migrations. Left as-is.
- [ ] simple-commons resolved from local mavenLocal
      (`com.simplemobiletools.commons:release:5.34.26`, built from ../Simple-Commons @ 9e60e24)
      instead of JitPack. Same commit → same resources. Keep ../Simple-Commons present +
      published to mavenLocal before building.

## Confirmed refinement batch (DONE)

- [x] **Remote folders hidden until verified + real name** — `getVerifiedRemoteDirectory()`
  runs a background FTP `connect` + `listFiles` + has-media check; only verified servers are
  injected as folders. Failed/offline servers are cached with a cooldown (`RemoteServerVerify`)
  so the list never stalls on a dead host. Encrypted folders bypass the check.
- [x] **Remote delete = remove-from-list only** — `DirectoryAdapter.deleteFolders` calls
  `config.removeRemoteServer(serverId)` for `remote://` paths (they aren't in
  `includedFolders`), and the CAB hides rename/move/copy/exclude/hide for remote selections.
- [x] **Remote viewer/player ContentID crash fixed** — `ViewPagerActivity.launchViewVideoIntent`
  temp-downloads `remote://` paths to a local file before handing the URI to ExoPlayer.
- [x] **FTP radio hidden (kept for SFTP)** — `dialog_add_remote_server.xml` FTP
  RadioGroup kept in XML via `visibility="gone"`; SFTP re-add keeps its option slot.
- [x] **Encryption toggle above the PIN input** — new app-tinted `MyAppCompatSwitch` in
  commons; `dialog_security.xml` rewritten: switch sits directly above the pattern/PIN box
  (below the tabs), `fillViewport` removed so the dialog wraps content (no forced scroll).
  Published commons `5.34.26` to mavenLocal.
- [x] **Perf: seamless refresh + cached move/copy (verified, no change needed)** — swipe
  refresh renders the cached DB list first (adapter reused via `updateDirs`, not recreated)
  then rechecks in background; `PickDirectoryDialog` already lists from `getCachedDirectories`.

## Refinement fixes (DONE — code audit batch)

- [x] **Tree crash fixed + mode is a pure in-memory switch** — `getTreeDirectories()` never
  calls `directoryDB.getAll()` (that crashed with Room's main-thread exception); it builds the
  tree from the in-memory directory list. Tree mode is an in-memory `mTreeFolders` field — the
  same way the native "show all" / folders modes work — not a shared pref. Button cycles
  folders grid -> tree -> all-media.
- [x] **Remote media3/ContentID error fixed** — the root cause was active-mode FTP:
  `RemoteModelLoader` (thumbnails), `downloadRemoteFileToTemp` (video download) and
  `MediaFetcher.getRemoteFiles` (listing) never called `enterLocalPassiveMode()`, so NAT'd
  servers returned null streams -> no thumbnails, no playback. All three now use passive
  mode; `completePendingCommand`/`disconnect` retained in the download.
- [x] **Remote name fix (was empty)** — `getVerifiedRemoteDirectory` had swapped args:
  `Directory(null, remotePath, displayName, remotePath, ...)` put the URI in the name slot.
  Now `Directory(null, remotePath, "", displayName, ...)` so the chosen server name shows.
  Second layer: `getCachedDirectories` dropped stale `remote://` DB rows before the merge
  (the old name was winning `distinctBy { path }`), and remotes are never persisted again
  (`updateDBDirectory` / `directoryDB.insert` guards in `MainActivity`).
- [x] **Remote connect timeout reduced** — 5000ms -> 3000ms across `RemoteModelLoader`,
  `downloadRemoteFileToTemp`, `MediaFetcher.getRemoteFiles` and `getVerifiedRemoteDirectory`.
- [x] **Unreachable-remote toast removed** — `RemoteModelLoader` no longer toasts on FTP
  failure; unreachable servers are silently ignored.
- [x] **Storage-type icons corrected** — SD card folders get a new dedicated SD-card glyph
  (`ic_sd_card_filled_vector`, not the phone-like stock one); **local/internal folders get no
  storage icon at all** (clean tile); cloud for remote, USB for OTG unchanged.

## Done

- [x] Settings UI purple/override styles — removed local shadow styles in
      `app/src/main/res/values/styles.xml` + purple `colorPrimary` in `colors.xml`;
      commons' themed styles now apply (official look).
