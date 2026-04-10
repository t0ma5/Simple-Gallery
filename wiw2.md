# Simple Gallery Pro - Architecture & Implementation Guide

This document outlines the current architecture of Simple Gallery Pro and provides a guide for implementing the features requested in `WIW.md`.

## 1. Current Architecture Overview

### Core Components
- **Activities & Fragments**: `MainActivity` (folder listing), `MediaActivity` (media listing), `ViewPagerActivity` (media viewing).
- **Media Fetching**: `MediaFetcher` is the heart of media discovery. It scans MediaStore, local file system, and OTG.
- **Data Model**: `Medium` (file), `Directory` (folder).
- **Caching**: Room database is used to cache `Directory` and `Medium` objects for performance.
- **Base Logic**: Inherits heavily from `Simple-Commons` (`BaseSimpleActivity`, `Config`, etc.).
- **Storage Access**: Uses a mix of direct `File` API, MediaStore, and SAF (Storage Access Framework) for Android 11+ compatibility.

### Folder Locking (Current)
- Currently implemented as "Protection" in `Simple-Commons`.
- It's an access-level lock (password/pattern) before opening a folder or performing operations.
- It does **not** encrypt files on disk.

---

## 2. Feature: FTP/SFTP Support

### Architectural Changes
- **Abstract File System**: Currently, the app assumes local paths (or OTG). To support FTP/SFTP, we need an abstraction layer for file operations.
- **RemoteMediaFetcher**: Create a specialized fetcher for remote sources.
- **Protocol Integration**: Add a library like `JSch` (SFTP) or `Apache Commons Net` (FTP).

### Implementation Steps
1. **Model Update**: Extend `Directory` and `Medium` to handle remote URIs/Paths.
2. **Config Update**: Store remote server credentials securely (consider using Android Keystore).
3. **MediaFetcher Integration**: Update `getFoldersToScan` and `getFilesFrom` to include remote sources when configured.
4. **Thumbnail Handling**: Remote files will need a local caching mechanism for thumbnails to avoid high latency.

---

## 3. Feature: Folder Locking with Encryption

### Architectural Changes
- **Encryption Engine**: Integrate a library like `Conceal` (by Facebook) or `Jetpack Security` (AES-GCM).
- **State Management**: A way to track which folders are currently "encrypted/locked".

### Implementation Steps
1. **Toggle Encryption**:
   - Add a "Toggle Encryption" option in the folder long-press menu.
   - On "Encrypt": Iterate through all files, encrypt content, rename with a suffix (e.g., `.enc`), and delete original.
   - On "Decrypt": Reverse the process.
2. **Handle Locked Folders**:
   - In `MediaFetcher`, if a folder is encrypted, it should still show the folder but with a "locked" placeholder thumbnail.
   - When opening, prompt for the password, decrypt files to a temporary cache directory, and show them.
3. **File Operations**:
   - `Move/Copy In`: Automatically encrypt the incoming file.
   - `Move/Copy Out`: Automatically decrypt the file being moved out.

---

## 4. Feature: UI Refinement - Eye Icon Filter

### Current Behavior
- The "eye" icon toggles `temporarilyShowHidden`.
- It currently shows both hidden (files/folders starting with `.`) and "restricted" folders (Android data/system folders).

### Proposed Change
1. **Filter Logic**: In `MainActivity.setupAdapter`, update the filtering logic.
2. **New State**: Add `temporarilyShowHiddenOnly` to `Config`.
3. **UI Update**: Modify `refreshMenuItems` and the click listener for the eye icon to respect the new filter:
   - If toggled: Show folders where `isHidden` is true, but `isRestricted` is false.

---

## 5. Security Mandates
- **Credential Storage**: Never store FTP/SFTP passwords in plain text in `SharedPreferences`. Use `Android Keystore`.
- **Encryption Keys**: Use a key derived from the user's folder password (PBKDF2) or a randomly generated key stored in `Keystore` protected by the app's master password.
