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
- **FTP / SFTP Access** — Add remote servers and browse/transfer media over network storage (thumbnails streamed via Glide)
- **Remote Thumbnails** — `remote://` media is loaded directly into the gallery's thumbnail pipeline

> **Note on network access:** FTP/SFTP support requires the `INTERNET` permission. The app only connects to servers you explicitly configure; there are no ads and no analytics/trackers.

## Install

[Get it on F-Droid](https://f-droid.org/packages/com.simplemobiletools.gallery.pro)

## Build

Requirements: Android SDK (compileSdk 35), JDK 17+, Gradle 8.7 (wrapper provided).

```bash
./gradlew assembleFossDebug     # or assembleProprietaryDebug for the proprietary flavor
```

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
