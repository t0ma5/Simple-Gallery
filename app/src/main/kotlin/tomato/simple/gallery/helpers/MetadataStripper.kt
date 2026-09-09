package tomato.simple.gallery.helpers

import android.content.Context
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import tomato.simple.gallery.extensions.AllNonDimensionExifAttributes
import java.io.File

object MetadataStripper {
    private val GPS_TAGS = AllNonDimensionExifAttributes.filter { it.startsWith("GPS") }

    fun stripGps(context: Context, path: String): Boolean {
        return mutate(context, path) { exif ->
            GPS_TAGS.forEach { exif.setAttribute(it, null) }
        }
    }

    fun stripAll(context: Context, path: String): Boolean {
        return mutate(context, path) { exif ->
            AllNonDimensionExifAttributes.forEach { exif.setAttribute(it, null) }
        }
    }

    private fun mutate(context: Context, path: String, update: (ExifInterface) -> Unit): Boolean {
        return try {
            if (path.startsWith("content://")) {
                context.contentResolver.openFileDescriptor(Uri.parse(path), "rw")?.use { pfd ->
                    val exif = ExifInterface(pfd.fileDescriptor)
                    update(exif)
                    exif.saveAttributes()
                } ?: return false
            } else {
                val file = File(path)
                if (!file.exists() || !file.canWrite()) {
                    return false
                }
                val exif = ExifInterface(file.absolutePath)
                update(exif)
                exif.saveAttributes()
            }
            true
        } catch (_: Exception) {
            false
        }
    }
}
