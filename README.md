# Simple Gallery (Plus)

A fast, privacy-focused photo and video gallery for Android — a fork of
[SimpleMobileTools/Simple-Gallery](https://github.com/SimpleMobileTools/Simple-Gallery)
with added security and remote-storage features. No ads.

> ⚠️ **Heavily vibe-coded, not even reviewed — but tested and it looks working.**
> Built largely with AI assistance; no formal code review. Manual testing shows
> the added features work.

## Features (beyond upstream)

- **On-device encryption** — encrypt/decrypt folders locally; encrypted media is
  transparently decrypted on open and purged from cache on exit.
- **FTP / SFTP remote access** — browse and transfer media over network storage;
  thumbnails stream via Glide (`remote://` pipeline).
- **Private gallery** — PIN, pattern, or fingerprint per folder.
- **Tree (folder-in-folder) mode** — nested subfolders under parents.
- Upstream: photo editor, wide format support, recycle-bin recovery, customizable UI.

## Requirements

- Android 6.0 (API 23)+
- Android SDK 36 to build; JDK 17

## Build

```bash
./gradlew assembleFossDebug      # foss flavor -> app/build/outputs/apk/foss/debug
./gradlew assembleRelease        # requires signing config
```

## Known issues / Limitations

- Encryption is local-only (no cloud sync of ciphertext yet).
- Fork drifts from upstream; merging upstream fixes is manual.

## Support

If you value ad-free, privacy-first tooling, donations keep it maintained:

| | |
|---|---|
| PayPal | `paypal.com/ncp/payment/W78F6W4TXZ4CS` |
| Binance | `1011264323` |
| Bybit | `467077834` |
| TRC20 | `TMW5uSDN6sMUBNirMoqY1icpsfa7GhPZfK` |
| BEP20/ERC20 | `0x7a8887c2ac3e596f6170c9e28b44e6b6d025c854` |
| LTC | `LVswXiD6Vd2dejXcgZ7a` |
| TON | `UQAllRezWgHi3LPrSwyvAb4zazIph6j6goU7lMaqcFWFBxVH` |
| BTC | `1rSX6BDN1nqDMyBHqceySkZSs6PHUP23m` |
| SOL | `d8RonhC8oEHssrQjN1Y4UWHnd6MMP33XGCKtfNL4j59` |

## Apology

No screenshots or video demos are included.

## License

GPL-3.0 (inherits upstream SimpleMobileTools license)
