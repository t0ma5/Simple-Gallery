# Simple Gallery

<img alt="Logo" src="graphics/icon.png" width="120" />

<div style="display:flex; gap:8px; flex-wrap:wrap;">
<img alt="Albums" src="fastlane/metadata/android/en-US/images/phoneScreenshots/1_en-US.jpeg" width="22%">
<img alt="Editor" src="fastlane/metadata/android/en-US/images/phoneScreenshots/2_en-US.jpeg" width="22%">
<img alt="Viewer" src="fastlane/metadata/android/en-US/images/phoneScreenshots/4_en-US.jpeg" width="22%">
<img alt="Lock" src="fastlane/metadata/android/en-US/images/phoneScreenshots/5_en-US.jpeg" width="22%">
</div>

No ads or unnecessary permissions. It is fully open source and provides customizable colors. No internet access required; photos stay on the device.

## History

[Simple-Gallery](https://github.com/SimpleMobileTools/Simple-Gallery) (GPL-3.0) was my favorite FOSS gallery app until the project was sold to a shady Israeli company named ZipoApps in 2023. I forked it to keep it alive, FOSS and updated. I will release new versions as long as I have time and energy. Code contributions on [GitHub](https://github.com/t0ma5/Simple-Gallery) are very welcome :)

This fork is not affiliated with Simple Mobile Tools, Fossify, or ZipoApps.

APKs are published on [GitHub Releases](https://github.com/t0ma5/Simple-Gallery/releases)
- `Simple-Gallery_<version>-FOSS-arm64-v8a.apk` (most phones)
- `Simple-Gallery_<version>-FOSS-armeabi-v7a.apk`
- `Simple-Gallery_<version>-FOSS-x86_64.apk` (emulators)
- `Simple-Gallery_<version>-FOSS-universal.apk` (all included)

## New features in this fork

The Simple Gallery Pro workflow, plus a FOSS editor, AVIF/JXL, motion photos, and 360° panoramas. Still offline, still GPL-3.0.

**Edit**
- **FOSS photo editor** — Crop, rotate, resize, draw, filters, tone sliders, and text/emoji stickers. Tools stack in one session. No proprietary SDK.
- **JPEG optimize** — Native jpegoptim: lossless Huffman or lossy (default 85). EXIF kept; file replaced only if smaller. Batch from the grid.

**Formats**
- **AVIF, JPEG XL, Ultra HDR** — Grid, viewer, and “open with”. Wide-color display with a settings toggle.
- **Motion photos** — Play Google Motion Photos, Samsung Live Photos, and Micro Videos; save the clip as an MP4.
- **360° and video** — OpenGL photosphere viewer (touch, pinch, gyroscope). In-app player with speed, mute, long-press 2×, and exact seek.

**Organize**
- **Stacks** — Optional RAW+JPEG, bursts, and edited copies in the grid, with a strip in the viewer.
- **Folder tree** — Grid, List, or Tree. Show hidden and Show excluded stay on until you turn them off.

**Private**
- **Locked folders** stay hidden in Favorites, Recycle Bin, and widgets until you unlock.
- **Strip metadata** — Remove GPS or all EXIF from photos, or when sharing.

**No nags**
- No donation prompts. Own app ID (`tomato.simple.gallery`) so it can sit next to Simple Gallery Pro.

## Build

Release APKs come from `assembleFossRelease` (local and GitHub Actions). Names:

- `Simple-Gallery_<version>-FOSS-arm64-v8a.apk`
- `Simple-Gallery_<version>-FOSS-armeabi-v7a.apk`
- `Simple-Gallery_<version>-FOSS-x86_64.apk`
- `Simple-Gallery_<version>-FOSS-universal.apk`

| Item | Value |
| --- | --- |
| App version | 6.30 (versionCode 399) |
| minSdk | 26 |
| targetSdk / compileSdk | 36 (Android 16) |
| JVM bytecode | 17 |
| Gradle | 9.1.0 |
| Android Gradle Plugin | 8.13.2 |
| Kotlin / KSP | 2.2.10 |
| Room | 2.8.4 |
| Local JDK | 25 (do not point Gradle at JDK 17) |
| CI JDK | Temurin 17 |
| Commons | `SimpleMobileTools/Simple-Commons` @ `9e60e2479`, patched by `python scripts/patch_commons.py` |

Checkout Simple-Commons next to the app (gitignored `Simple-Commons/`), run `python scripts/patch_commons.py`, then `./gradlew assembleFossRelease`. When that directory exists, Gradle `includeBuild`s it instead of the JitPack AAR. Sign with gitignored `keystore.properties` and `app/keystore.jks` (GitHub secrets `KEYSTORE_BASE64`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`).
