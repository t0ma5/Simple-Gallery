# Simple Gallery

<img alt="Logo" src="graphics/icon.png" width="120" />

**Forked from** [SimpleMobileTools/Simple-Gallery](https://github.com/SimpleMobileTools/Simple-Gallery) (GPL-3.0).

This is an independent fork of Simple Gallery Pro 6.28.1. It is not affiliated with Simple Mobile Tools or Fossify.

APKs are published on [GitHub Releases](https://github.com/t0ma5/Simple-Gallery/releases) as `Simple-Gallery_<version>.apk` (for example `Simple-Gallery_6.29.0.apk`). Push a tag `vX.Y.Z` to attach a FOSS APK to a release. Every push, pull request, and manual **Build APK** run also uploads the APK as a workflow artifact.

## Photo editor

This fork ships the **FOSS editor** (crop, rotate, resize, draw, filters, tone sliders, text and emoji stickers). The Play-style IMG.LY PhotoEditor SDK (upstream 10.7.3; latest 10.10.14) is proprietary. Its license files were removed from git.

applicationId and Kotlin packages are `tomato.simple.gallery`, so it can sit next to Play Store Simple Gallery Pro.

## New features ahead of mainstream

- Stack crop, draw, filters, adjust, and text in one edit session (upstream still discards work when you switch tools).
- Adjust brightness, contrast, saturation, and temperature in the FOSS editor.
- Add draggable text and emoji stickers.
- Overwrite the original file when saving edits (upstream 6.28.1 is save-as only).
- Reliable filter save (resource cleanup before applying the filter).
- No donation prompts; no “fake version” block on save.
- Folder tree view (indent nested albums instead of a flat list).
- Temporarily showing hidden folders no longer leaks `Android/data`, `Android/obb`, or app-private trees unless you explicitly include them.
- minSdk 26, targetSdk 34, compileSdk 35.
- 360° Cardboard VR viewer removed (Google VR widgets; panoramas open as normal photos/videos).

## About

Simple Gallery brings you all the photo viewing and editing features you have been missing on your Android in one stylish easy-to-use app. Browse, manage, crop and edit photos or videos faster than ever, recover accidentally deleted files or create hidden galleries for your most precious images and videos. And with advanced file-support and full customization, finally, your gallery works just the way you want.

ADVANCED PHOTO EDITOR  
Turn photo editing into child's play with Simple Gallery's improved file organizer and photo album. Intuitive gestures make it super easy to edit your images on the fly. Crop, flip, rotate and resize pictures or apply stylish filters to make them pop in an instant.

ALL THE FILES YOU NEED  
Simple Gallery supports a huge variety of different file types including JPEG, PNG, MP4, MKV, RAW, SVG, GIF, Panoramic photos, videos and many more, so you enjoy full flexibility in your choice of format. Ever wonder "Can I use this format on my Android"? Now the answer is yes.

MAKE IT YOURS  
Simple Gallery's highly customizable design allows you make the photo app look, feel and work just the way you want it to. From the UI to the function buttons on the bottom toolbar, Simple Gallery gives you the creative freedom you need in a gallery app.

RECOVER DELETED PHOTOS & VIDEOS  
Never worry about accidentally deleting that one precious photo or video you just can't replace. Simple Gallery allows you to quickly recover any deleted photo and videos, meaning on top of being the best media gallery for Android, Simple Gallery doubles as an amazing photo vault app.

PROTECT YOUR PRIVATE PHOTOS, VIDEOS & FILES  
Rest assured your photo album is safe. With Simple Gallery's superior security features you can use a pin, pattern or your device’s fingerprint scanner to limit who can view or edit selected photos and videos or access important files. You can even protect the app itself or place locks on specific functions of the file organizer.

<div style="display:flex;">
<img alt="App image" src="fastlane/metadata/android/en-US/images/phoneScreenshots/1_en-US.jpeg" width="30%">
<img alt="App image" src="fastlane/metadata/android/en-US/images/phoneScreenshots/2_en-US.jpeg" width="30%">
<img alt="App image" src="fastlane/metadata/android/en-US/images/phoneScreenshots/3_en-US.jpeg" width="30%">
</div>
