package tomato.simple.gallery.helpers

import android.net.Uri
import com.simplemobiletools.commons.activities.BaseSimpleActivity
import com.simplemobiletools.commons.extensions.getFileInputStreamSync
import com.simplemobiletools.commons.extensions.getFileOutputStreamSync
import com.simplemobiletools.commons.extensions.getMimeType
import com.simplemobiletools.commons.extensions.isJpg
import java.io.File

data class JpegOptimResult(val ok: Boolean, val bytesSaved: Long)

object JpegOptim {
    init {
        System.loadLibrary("jpegoptim-jni")
    }

    @JvmStatic
    private external fun optimizeNative(path: String, tmpDir: String, quality: Int): Long

    fun isSupported(path: String) = path.isJpg()

    fun optimize(activity: BaseSimpleActivity, path: String, quality: Int): JpegOptimResult {
        if (!isSupported(path)) {
            return JpegOptimResult(false, 0)
        }

        val workDir = File(activity.cacheDir, "jpegoptim").apply { mkdirs() }
        val work = File(workDir, "work-${Thread.currentThread().id}.jpg")
        val tmpDir = workDir.absolutePath + File.separator
        try {
            copySource(activity, path, work)
            val before = work.length()
            if (before <= 0L) {
                return JpegOptimResult(false, 0)
            }

            val saved = optimizeNative(work.absolutePath, tmpDir, quality.coerceAtLeast(-1).coerceAtMost(100))
            if (saved < 0L) {
                return JpegOptimResult(false, 0)
            }
            if (saved == 0L || work.length() >= before) {
                return JpegOptimResult(true, 0)
            }
            if (!writeBack(activity, path, work)) {
                return JpegOptimResult(false, 0)
            }
            return JpegOptimResult(true, saved)
        } catch (_: Exception) {
            return JpegOptimResult(false, 0)
        } finally {
            work.delete()
            workDir.listFiles()?.filter { it.name.endsWith(".tmp") }?.forEach { it.delete() }
        }
    }

    private fun copySource(activity: BaseSimpleActivity, path: String, dest: File) {
        if (path.startsWith("content://")) {
            activity.contentResolver.openInputStream(Uri.parse(path)).use { input ->
                requireNotNull(input)
                dest.outputStream().use { input.copyTo(it) }
            }
        } else {
            activity.getFileInputStreamSync(path)?.use { input ->
                dest.outputStream().use { input.copyTo(it) }
            } ?: error("cannot read $path")
        }
    }

    private fun writeBack(activity: BaseSimpleActivity, path: String, source: File): Boolean {
        val mime = path.getMimeType().ifEmpty { "image/jpeg" }
        val out = activity.getFileOutputStreamSync(path, mime) ?: return false
        source.inputStream().use { input ->
            out.use { input.copyTo(it) }
        }
        return true
    }
}
