package tomato.simple.gallery.dialogs

import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatEditText
import com.simplemobiletools.commons.activities.BaseSimpleActivity
import com.simplemobiletools.commons.extensions.getAlertDialogBuilder
import com.simplemobiletools.commons.extensions.setupDialogStuff
import com.simplemobiletools.commons.extensions.showKeyboard
import com.simplemobiletools.commons.extensions.value
import tomato.simple.gallery.R

class AddTextDialog(val activity: BaseSimpleActivity, val initialText: String = "", val callback: (text: String) -> Unit) {
    init {
        val editText = AppCompatEditText(activity).apply {
            hint = activity.getString(R.string.add_text)
            setSingleLine(false)
            minLines = 2
            setPadding(48, 32, 48, 32)
            if (initialText.isNotEmpty()) {
                setText(initialText)
                setSelection(initialText.length)
            }
        }
        val titleRes = if (initialText.isEmpty()) R.string.add_text else com.simplemobiletools.commons.R.string.edit

        activity.getAlertDialogBuilder()
            .setPositiveButton(com.simplemobiletools.commons.R.string.ok, null)
            .setNegativeButton(com.simplemobiletools.commons.R.string.cancel, null)
            .apply {
                activity.setupDialogStuff(editText, this, titleRes) { alertDialog ->
                    alertDialog.showKeyboard(editText)
                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                        val text = editText.value.trim()
                        if (text.isEmpty()) {
                            return@setOnClickListener
                        }
                        callback(text)
                        alertDialog.dismiss()
                    }
                }
            }
    }
}
