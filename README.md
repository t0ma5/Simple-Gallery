# Simple Gallery

<img alt="Logo" src="graphics/icon.png" width="120" />

**Forked from** [SimpleMobileTools/Simple-Gallery](https://github.com/SimpleMobileTools/Simple-Gallery) (GPL-3.0).

This is an independent fork of Simple Gallery Pro 6.28.1. It is not affiliated with Simple Mobile Tools or Fossify.

APKs are published on [GitHub Releases](https://github.com/t0ma5/Simple-Gallery/releases)

## Photo editor

This fork ships the **FOSS editor**: crop, rotate, resize, draw, filters, tone sliders, text and emoji stickers. There is no proprietary photo-editor SDK.

applicationId and Kotlin packages are `tomato.simple.gallery`, so it can sit next to Play Store Simple Gallery Pro.

## New features ahead of mainstream

- JPEG optimize with native jpegoptim: optional lossless Huffman, or lossy max-quality (default 85). Metadata kept; files replaced only if smaller. Batch from the grid or one file from the viewer.
- Motion photos: play Google Motion Photos, Samsung Live Photos, and Micro Videos in the viewer; save the embedded clip as an MP4.
- OpenGL 360° panorama / photosphere viewer (touch, pinch, optional gyroscope) in place of the removed Google VR Cardboard widgets.
- In-app video player: speed, mute, long-press 2×, exact seek/loop. Settings: tap a video to open the in-app player or the system player (new installs default to in-app).
- Stacked FOSS editor: crop, draw, filters, adjust, and text in one session (upstream still discards work when you switch tools). Tone sliders, draggable text and emoji stickers, overwrite original, keep EXIF on edit/resize (including SAF URIs), reliable filter save. Crop 4:3 / 16:9 flip to 3:4 / 9:16 on a second tap; text overlays have a font picker.
- AVIF and JPEG XL (.jxl) in the grid, viewer, and “open with”.
- Ultra HDR (Android 14+ gain maps) and wide-color display, with a settings toggle.
- RAW+JPEG, burst, and edited-copy stacks in the grid, with a strip in the viewer.
- Remove GPS or all EXIF metadata from photos.
- Folder tree view (indent nested albums instead of a flat list; unchecking restores the previous grid/list).
- Temporarily showing hidden folders no longer leaks `Android/data`, `Android/obb`, or app-private trees unless you explicitly include them.
- Edge-to-edge padding on Android 15/16 (viewer, editor, wallpaper, standalone player).
- Copy image to clipboard; sort folders by item count; confirm restore from the recycle bin; keep the screen on for fullscreen photos; reverse landscape; no silent auto-save of gesture rotation.
- No donation prompts; no “fake version” block on save.
- minSdk 26, targetSdk 34, compileSdk 35.

## About

Simple Gallery is a FOSS Android gallery for browsing, organizing, and editing photos and videos. This fork keeps the original album browser, recycle bin, hidden folders, and PIN / pattern / fingerprint locks, and adds formats and tools the Play Store app does not ship.

**Media.** JPEG, PNG, GIF, WebP, AVIF, JPEG XL, RAW, SVG, MP4, MKV, motion photos, and 360° panoramas. Open with, wallpaper, print, and share still work as in upstream.

**Editor.** Built-in FOSS crop, rotate, resize, draw, filters, tone sliders, and text/emoji stickers. Stacked tools in one session; EXIF is kept on save when possible.

**Organize.** Recycle bin with restore, favorites, folder tree, include/exclude, hide, copy/move, batch rename, and JPEG lossless optimize.

**Libraries.** jpegoptim 1.5.6 with MozJPEG (lossless Huffman or lossy max-quality JPEG); Glide (thumbnails, AVIF/WebP/GIF); AndroidX Media3 ExoPlayer (in-app player); Room (media database); Subsampling Scale Image View and GestureViews (zoom); AndroidSVG; APNG/aWebP/AVIF animation; JXL coder; Android GIF Drawable; Apache Sanselan; Android Photo Filters; Android Image Cropper; Simple Commons.

<div style="display:flex;">
<img alt="App image" src="fastlane/metadata/android/en-US/images/phoneScreenshots/1_en-US.jpeg" width="30%">
<img alt="App image" src="fastlane/metadata/android/en-US/images/phoneScreenshots/2_en-US.jpeg" width="30%">
<img alt="App image" src="fastlane/metadata/android/en-US/images/phoneScreenshots/3_en-US.jpeg" width="30%">
</div>
