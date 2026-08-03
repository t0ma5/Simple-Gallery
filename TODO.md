# TODO — Gallery+ fork (Simple Gallery Pro 6.28.1)

## Visual parity with official Pro build

- [ ] **Editor look (biggest gap)** — imgly PhotoEditorSDK (PESDK/VESDK) is NOT in the build.
      Official Pro's fancy editor = imgly lib, applied only when gradle task name contains
      "Proprietary" (`gradle/imglysdk.gradle` + `app/build.gradle.kts:150`). The imgly license
      files (`vesdk_android_license`, `pesdk_android_license`) were deleted from git history
      (`4217d5369`, `2e3753216`) → proprietary flavor can't build without them.
      Last build = `Gallery+-396-foss-debug.apk` (foss flavor) → basic home-made editor.
      Options: re-obtain imgly license (per-app, paid) OR accept foss editor look.
- [ ] **Editor filter lib replaced** — official uses `info.androidhive:imagefilters:1.0.7`;
      fork uses `com.github.naveensingh:androidphotofilters` (zomato). Filter thumbnails in the
      editor render differently. Restore official lib if imagefilters still exists on jitpack/maven.
- [ ] **rtl-viewpager swapped to a fork** — local Simple-Commons build uses
      `com.github.naveensingh:rtl-viewpager:2.0.2` instead of official
      `com.github.duolingo:rtl-viewpager:940f12724f`. RTL (Arabic) layout edge cases only.

## Minor

- [ ] `supportSwipeToRefresh="true"` stripped from fastscroller in
      `activity_main.xml` + `activity_media.xml` — check swipe-to-refresh still works.
- [ ] `settingsHolder.setBackgroundColor(getProperBackgroundColor())` added in
      SettingsActivity.kt — verify against official behavior (may override theme bg).

## Build/toolchain drift (no visual impact, but tracked)

- [ ] Kotlin 1.8.22 → 1.9.22, AGP 7.4.0 → 8.6.0, compileSdk 34 → 35, minSdk 23 → 26,
      Room 2.6.0-beta01 → 2.5.2 (downgraded), gradle daemon off / Xmx1536m.
      Nothing visual, but Room downgrade could matter for future DB migrations.
- [ ] simple-commons now resolved from local mavenLocal
      (`com.simplemobiletools.commons:release:5.34.26`, built from ../Simple-Commons @ 9e60e24)
      instead of JitPack `com.github.SimpleMobileTools:Simple-Commons:9e60e24790`.
      Same commit → same resources, but keep the local build reproducible
      (needs `../Simple-Commons` present + published to mavenLocal before building).

## Done

- [x] Settings UI purple/override styles — removed local shadow styles in
      `app/src/main/res/values/styles.xml` + purple `colorPrimary` in `colors.xml`;
      commons' themed styles now apply (official look).
