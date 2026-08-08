package com.simplemobiletools.gallery.pro.models

// Represents a subfolder shown as a tile inside a folder's media grid
// ("folders inside folders" navigation).
class FolderTile(val directory: Directory) : ThumbnailItem() {
    val path: String get() = directory.path
    val name: String get() = directory.name
}