package com.simplemobiletools.gallery.pro.dialogs

import android.util.TypedValue
import android.widget.GridLayout
import android.widget.TextView
import com.simplemobiletools.commons.activities.BaseSimpleActivity
import com.simplemobiletools.commons.extensions.getAlertDialogBuilder
import com.simplemobiletools.commons.extensions.setupDialogStuff
import com.simplemobiletools.gallery.pro.R

class AddStickerDialog(val activity: BaseSimpleActivity, val callback: (emoji: String) -> Unit) {
    companion object {
        private val STICKERS = listOf(
            "😀", "😂", "😍", "🤩", "😎", "😭", "🔥", "👍",
            "❤️", "⭐", "🎉", "☀️", "🌈", "✨", "📷", "🌸"
        )
    }

    init {
        var dialog: androidx.appcompat.app.AlertDialog? = null
        val grid = GridLayout(activity).apply {
            columnCount = 4
            setPadding(32, 24, 32, 24)
        }

        STICKERS.forEach { emoji ->
            val cell = TextView(activity).apply {
                text = emoji
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 28f)
                setPadding(28, 20, 28, 20)
                setOnClickListener {
                    callback(emoji)
                    dialog?.dismiss()
                }
            }
            grid.addView(cell)
        }

        activity.getAlertDialogBuilder()
            .setNegativeButton(com.simplemobiletools.commons.R.string.cancel, null)
            .apply {
                activity.setupDialogStuff(grid, this, R.string.add_sticker) { alertDialog ->
                    dialog = alertDialog
                }
            }
    }
}
