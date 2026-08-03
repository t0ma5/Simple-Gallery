package com.simplemobiletools.gallery.pro.dialogs

import androidx.appcompat.app.AlertDialog
import android.view.View
import com.simplemobiletools.commons.activities.BaseSimpleActivity
import com.simplemobiletools.commons.dialogs.ConfirmationDialog
import com.simplemobiletools.commons.dialogs.FilePickerDialog
import com.simplemobiletools.commons.extensions.*
import com.simplemobiletools.commons.helpers.isRPlus
import com.simplemobiletools.gallery.pro.R
import com.simplemobiletools.gallery.pro.databinding.DialogSaveAsBinding
import java.io.File

class SaveAsDialog(
    val activity: BaseSimpleActivity, val path: String, val appendFilename: Boolean = false,
    val simple: Boolean = false, val cancelCallback: (() -> Unit)? = null,
    val callback: (savePath: String) -> Unit
) {
    init {
        if (simple) {
            // Simple mode (image editor): just Overwrite the original or Save a copy.
            val filename = path.getFilenameFromPath()
            activity.getAlertDialogBuilder()
                .setTitle(String.format(activity.getString(R.string.save_overwrite_confirm), filename))
                .setPositiveButton(R.string.save_copy, null)
                .setNegativeButton(R.string.overwrite, null)
                .setNeutralButton(com.simplemobiletools.commons.R.string.cancel) { _, _ -> cancelCallback?.invoke() }
                .setOnCancelListener { cancelCallback?.invoke() }
                .apply {
                    activity.setupDialogStuff(View(activity), this, com.simplemobiletools.commons.R.string.save_as) { alertDialog ->
                        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                            callback(getCopyPath(path))
                            alertDialog.dismiss()
                        }
                        alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener {
                            callback(path)
                            alertDialog.dismiss()
                        }
                    }
                }
        } else {
        var realPath = path.getParentPath()
        if (activity.isRestrictedWithSAFSdk30(realPath) && !activity.isInDownloadDir(realPath)) {
            realPath = activity.getPicturesDirectoryPath(realPath)
        }

        val binding = DialogSaveAsBinding.inflate(activity.layoutInflater).apply {
            folderValue.setText("${activity.humanizePath(realPath).trimEnd('/')}/")

            val fullName = path.getFilenameFromPath()
            val dotAt = fullName.lastIndexOf(".")
            var name = fullName

            if (dotAt > 0) {
                name = fullName.substring(0, dotAt)
                val extension = fullName.substring(dotAt + 1)
                extensionValue.setText(extension)
            }

            if (appendFilename) {
                name += "_1"
            }

            filenameValue.setText(name)
            folderValue.setOnClickListener {
                activity.hideKeyboard(folderValue)
                FilePickerDialog(activity, realPath, false, false, true, true) {
                    folderValue.setText(activity.humanizePath(it))
                    realPath = it
                }
            }
        }

        activity.getAlertDialogBuilder()
            .setPositiveButton(com.simplemobiletools.commons.R.string.ok, null)
            .setNegativeButton(com.simplemobiletools.commons.R.string.cancel) { dialog, which -> cancelCallback?.invoke() }
            .setOnCancelListener { cancelCallback?.invoke() }
            .apply {
                activity.setupDialogStuff(binding.root, this, com.simplemobiletools.commons.R.string.save_as) { alertDialog ->
                    alertDialog.showKeyboard(binding.filenameValue)
                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                        val filename = binding.filenameValue.value
                        val extension = binding.extensionValue.value

                        if (filename.isEmpty()) {
                            activity.toast(com.simplemobiletools.commons.R.string.filename_cannot_be_empty)
                            return@setOnClickListener
                        }

                        if (extension.isEmpty()) {
                            activity.toast(com.simplemobiletools.commons.R.string.extension_cannot_be_empty)
                            return@setOnClickListener
                        }

                        val newFilename = "$filename.$extension"
                        val newPath = "${realPath.trimEnd('/')}/$newFilename"
                        if (!newFilename.isAValidFilename()) {
                            activity.toast(com.simplemobiletools.commons.R.string.filename_invalid_characters)
                            return@setOnClickListener
                        }

                        if (activity.getDoesFilePathExist(newPath)) {
                            val title = String.format(activity.getString(com.simplemobiletools.commons.R.string.file_already_exists_overwrite), newFilename)
                            ConfirmationDialog(activity, title) {
                                if ((isRPlus() && !isExternalStorageManager())) {
                                    val fileDirItem = arrayListOf(File(newPath).toFileDirItem(activity))
                                    val fileUris = activity.getFileUrisFromFileDirItems(fileDirItem)
                                    activity.updateSDK30Uris(fileUris) { success ->
                                        if (success) {
                                            selectPath(alertDialog, newPath)
                                        }
                                    }
                                } else {
                                    selectPath(alertDialog, newPath)
                                }
                            }
                        } else {
                            selectPath(alertDialog, newPath)
                        }
                    }
                }
            }
        }
    }

    private fun getCopyPath(originalPath: String): String {
        val parent = originalPath.getParentPath()
        val fullName = originalPath.getFilenameFromPath()
        val dotAt = fullName.lastIndexOf(".")
        val base = if (dotAt > 0) fullName.substring(0, dotAt) else fullName
        val extension = if (dotAt > 0) fullName.substring(dotAt) else ""
        var index = 1
        var candidate = "${parent.trimEnd('/')}/${base}_$index$extension"
        while (activity.getDoesFilePathExist(candidate)) {
            index++
            candidate = "${parent.trimEnd('/')}/${base}_$index$extension"
        }
        return candidate
    }

    private fun selectPath(alertDialog: AlertDialog, newPath: String) {
        activity.handleSAFDialogSdk30(newPath) {
            if (!it) {
                return@handleSAFDialogSdk30
            }
            callback(newPath)
            alertDialog.dismiss()
        }
    }
}
