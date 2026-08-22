package com.simplemobiletools.gallery.pro.models

import android.content.Context
import androidx.room.*
import com.bumptech.glide.signature.ObjectKey
import com.simplemobiletools.commons.extensions.*
import com.simplemobiletools.commons.helpers.*
import com.simplemobiletools.commons.models.FileDirItem
import com.simplemobiletools.gallery.pro.helpers.*
import java.io.File
import java.io.Serializable
import java.util.Calendar
import java.util.Locale

@Entity(tableName = "media", indices = [(Index(value = ["full_path"], unique = true))])
data class Medium(
    @PrimaryKey(autoGenerate = true) var id: Long?,
    @ColumnInfo(name = "filename") var name: String,
    @ColumnInfo(name = "full_path") var path: String,
    @ColumnInfo(name = "parent_path") var parentPath: String,
    @ColumnInfo(name = "last_modified") var modified: Long,
    @ColumnInfo(name = "date_taken") var taken: Long,
    @ColumnInfo(name = "size") var size: Long,
    @ColumnInfo(name = "type") var type: Int,
    @ColumnInfo(name = "video_duration") var videoDuration: Int,
    @ColumnInfo(name = "is_favorite") var isFavorite: Boolean,
    @ColumnInfo(name = "deleted_ts") var deletedTS: Long,
    @ColumnInfo(name = "media_store_id") var mediaStoreId: Long
) : Serializable, ThumbnailItem() {

    @Ignore var isDirectory: Boolean = false

    @Ignore var gridPosition: Int = 0

    constructor() : this(null, "", "", "", 0L, 0L, 0L, 0, 0, false, 0L, 0L)

    companion object {
        private const val serialVersionUID = -6553149366975655L
    }

    fun isWebP(): Boolean = name.lowercase().endsWith(".webp")

    fun isGIF(): Boolean = type == TYPE_GIFS

    fun isImage(): Boolean = type == TYPE_IMAGES

    fun isVideo(): Boolean = type == TYPE_VIDEOS

    fun isRaw(): Boolean = type == TYPE_RAWS

    fun isSVG(): Boolean = type == TYPE_SVGS

    fun isPortrait(): Boolean = type == TYPE_PORTRAITS

    fun isApng(): Boolean = name.lowercase().endsWith(".apng")

    fun isHidden(): Boolean = name.startsWith('.')

    fun isHeic(): Boolean = name.lowercase().endsWith(".heic") || name.lowercase().endsWith(".heif")

    fun getBubbleText(sorting: Int, context: Context, dateFormat: String, timeFormat: String): String {
        return when {
            sorting and SORT_BY_NAME != 0 -> name
            sorting and SORT_BY_PATH != 0 -> path
            sorting and SORT_BY_SIZE != 0 -> formatSize(size)
            sorting and SORT_BY_DATE_MODIFIED != 0 -> formatDate(modified, context, dateFormat, timeFormat)
            sorting and SORT_BY_RANDOM != 0 -> name
            else -> formatDate(taken, context)
        }
    }

    fun getGroupingKey(groupBy: Int): String {
        return when {
            groupBy and GROUP_BY_LAST_MODIFIED_DAILY != 0 -> getDayStartTS(modified, false)
            groupBy and GROUP_BY_LAST_MODIFIED_MONTHLY != 0 -> getDayStartTS(modified, true)
            groupBy and GROUP_BY_DATE_TAKEN_DAILY != 0 -> getDayStartTS(taken, false)
            groupBy and GROUP_BY_DATE_TAKEN_MONTHLY != 0 -> getDayStartTS(taken, true)
            groupBy and GROUP_BY_FILE_TYPE != 0 -> type.toString()
            groupBy and GROUP_BY_EXTENSION != 0 -> getFilenameExtension().lowercase()
            groupBy and GROUP_BY_FOLDER != 0 -> parentPath
            else -> ""
        }
    }

    fun getIsInRecycleBin(): Boolean = deletedTS != 0L

    private fun getDayStartTS(ts: Long, resetDays: Boolean): String {
        val calendar = Calendar.getInstance(Locale.ENGLISH).apply {
            timeInMillis = ts
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            if (resetDays) {
                set(Calendar.DAY_OF_MONTH, 1)
            }
        }

        return calendar.timeInMillis.toString()
    }

    fun getSignature(): String {
        val lastModified = if (modified > 1) {
            modified
        } else {
            File(path).lastModified()
        }

        return "$path-$lastModified-$size"
    }

    fun getKey() = ObjectKey(getSignature())

    fun toFileDirItem() = FileDirItem(path, name, false, 0, size)

    private fun formatSize(size: Long): String {
        return when {
            size < 1024 -> "$size B"
            size < 1024 * 1024 -> "${size / 1024} KB"
            size < 1024 * 1024 * 1024 -> "${size / (1024 * 1024)} MB"
            else -> "${size / (1024 * 1024 * 1024)} GB"
        }
    }

    private fun formatDate(ts: Long, context: Context): String {
        val calendar = Calendar.getInstance().apply { timeInMillis = ts }
        return "${calendar.get(Calendar.DAY_OF_MONTH)}/${calendar.get(Calendar.MONTH) + 1}/${calendar.get(Calendar.YEAR)}"
    }

    private fun formatDate(ts: Long, context: Context, dateFormat: String, timeFormat: String): String {
        return formatDate(ts, context)
    }

    private fun getFilenameExtension(): String {
        val lastDot = name.lastIndexOf('.')
        return if (lastDot >= 0) name.substring(lastDot + 1) else ""
    }
}
