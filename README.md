# Simple Gallery

[![GitHub Release](https://img.shields.io/github/v/release/SimpleMobileTools/Simple-Gallery)](https://github.com/SimpleMobileTools/Simple-Gallery/releases)
[![GitHub Stars](https://img.shields.io/github/stars/SimpleMobileTools/Simple-Gallery?style=social)](https://github.com/SimpleMobileTools/Simple-Gallery/stargazers)
[![GitHub Downloads](https://img.shields.io/github/downloads/SimpleMobileTools/Simple-Gallery/total)](https://github.com/SimpleMobileTools/Simple-Gallery/releases)

**Status:** Active development 🟢 — A fork of [SimpleMobileTools/Simple-Gallery](https://github.com/SimpleMobileTools/Simple-Gallery) with added security and remote-storage features.

A fast, privacy-focused photo and video gallery app for Android. Browse, manage, and edit your media with full support for common and advanced file types. Built on the lightweight SimpleMobileTools codebase, this fork adds on-device file encryption, FTP/SFTP remote browsing, and related security hardening.

## Features

- **Photo Editor** — Crop, flip, rotate, resize, draw, and apply filters
- **Wide Format Support** — JPEG, PNG, MP4, MKV, RAW, SVG, GIF, panoramic photos, and more
- **Customizable UI** — Adjust the layout and toolbar to your preferences
- **Deleted File Recovery** — Recover accidentally deleted photos and videos from the Recycle Bin
- **Private Gallery** — PIN, pattern, or fingerprint protection for selected folders
- **On-device Encryption** — Encrypt/decrypt folders locally; encrypted media is transparently decrypted when opened and securely purged from the cache on exit
- **FTP Remote Access** — Add FTP servers and browse/transfer media over network storage (thumbnails streamed via Glide)
- **Remote Thumbnails** — `remote://` media is loaded directly into the gallery's thumbnail pipeline
- **Tree (Folder-in-Folder) Mode** — A third folder view that nests subfolders under their parents; tap a parent to expand/collapse, tap a leaf to open its media. Can be enabled by default in Settings
- **Launch Lock** — If app password/fingerprint protection is enabled, the lock prompt appears once at launch, not every time you switch between folders / tree / images modes

### How remote folders work
- Add an FTP server from the folders screen (menu → *Add FTP server*).
- A remote server only appears in the folder list **after** it has been verified reachable and contains media — it is never shown with a blank name or a stale cached file count.
- Opening remote photos/videos downloads the file to a temp file and serves it through the media3 player (`FileDataSource`); duration, resolution and the properties dialog probe that temp file. The folder-scanner error toast on an unreachable server has been removed.
- **FTP only** (SFTP was removed). Requires the `INTERNET` permission; the app only connects to servers you configure. No ads, no analytics/trackers.

## Install

[Get it on F-Droid](https://f-droid.org/packages/com.simplemobiletools.gallery.pro)

## Build

Requirements: Android SDK (compileSdk 35), JDK 17+, Gradle 8.7 (wrapper provided).

```bash
./gradlew assembleFossDebug     # recommended; builds the FOSS flavor (no proprietary plugins)
```

> The `proprietary` flavor applies an additional editor SDK Gradle plugin that is not available in every build environment. The remote-folder / tree-mode / encryption code lives in the shared `pro` source set and is exercised by the FOSS flavor. If the proprietary plugin is unavailable, build the FOSS flavor above.

## Support

If you find this project useful, consider supporting its development.

IBAN: `SK4083300000002000965231`

| Method | Address / ID |
|---|---|
| PayPal | `paypal.com/ncp/payment/W78F6W4TXZ4CS` |
| Binance | `1011264323` |
| Bybit | `467077834` |
| TRC20 | `TMW5uSDN6sMUBNirMoqY1icpsfa7GhPZfK` |
| BEP20/ERC20 | `0x7a8887c2ac3e596f6170c9e28b44e6b6d025c854` |
| LTC | `LVswXiD6Vd2dejXvGbZLa1R8jkvg748F4q` |
| TON | `UQAllRezWgHi3LPrSwyvAb4zazIph6j6goU7lMaqcFWFBxVH` |
| BTC | `1rSX6BDN1nqDMyBHqceySkZSs6PHUP23m` |
| SOL | `d8RonhC8oEHssrQjN1Y4UWHnd6MMP33XGCKtfNL4j59` |
