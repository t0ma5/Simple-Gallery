package tomato.simple.gallery.dialogs

import androidx.appcompat.app.AlertDialog
import com.simplemobiletools.commons.activities.BaseSimpleActivity
import com.simplemobiletools.commons.extensions.*
import com.simplemobiletools.commons.helpers.ensureBackgroundThread
import tomato.simple.gallery.R
import tomato.simple.gallery.databinding.DialogOptimizeJpegsBinding
import tomato.simple.gallery.extensions.config
import tomato.simple.gallery.extensions.ensureWriteAccess
import tomato.simple.gallery.extensions.rescanPathsAndUpdateLastModified
import tomato.simple.gallery.helpers.JpegOptim
import java.io.File

class OptimizeJpegsDialog(
    private val activity: BaseSimpleActivity,
    private val imagePaths: List<String>,
    private val callback: () -> Unit
) {
    private var dialog: AlertDialog? = null
    private val binding = DialogOptimizeJpegsBinding.inflate(activity.layoutInflater)
    private val progressView = binding.optimizeProgress

    init {
        val config = activity.config
        progressView.apply {
            max = imagePaths.size
            setIndicatorColor(activity.getProperPrimaryColor())
        }

        binding.optimizeQuality.setColors(activity.getProperTextColor(), activity.getProperPrimaryColor(), activity.getProperBackgroundColor())
        binding.optimizeLossless.isChecked = config.jpegOptimLossless
        binding.optimizeQuality.progress = config.jpegOptimQuality.coerceIn(50, 100)
        binding.optimizeLossless.setOnCheckedChangeListener { _, _ -> refreshQualityUi() }
        binding.optimizeQuality.onSeekBarChangeListener {
            refreshQualityUi()
        }
        refreshQualityUi()

        activity.getAlertDialogBuilder()
            .setPositiveButton(com.simplemobiletools.commons.R.string.ok, null)
            .setNegativeButton(com.simplemobiletools.commons.R.string.cancel, null)
            .apply {
                activity.setupDialogStuff(binding.root, this, R.string.optimize_jpegs) { alertDialog ->
                    dialog = alertDialog
                    val positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
                    val negativeButton = alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                    positiveButton.setOnClickListener {
                        alertDialog.setCanceledOnTouchOutside(false)
                        arrayOf(
                            binding.optimizeMessage,
                            binding.optimizeLossless,
                            binding.optimizeQualityLabel,
                            binding.optimizeQuality,
                            positiveButton,
                            negativeButton
                        ).forEach {
                            it.isEnabled = false
                            it.alpha = 0.6f
                        }
                        optimizeImages()
                    }
                }
            }
    }

    private fun refreshQualityUi() {
        val lossless = binding.optimizeLossless.isChecked
        binding.optimizeQuality.isEnabled = !lossless
        binding.optimizeQuality.alpha = if (lossless) 0.4f else 1f
        binding.optimizeQualityLabel.text = if (lossless) {
            activity.getString(R.string.optimize_jpegs_lossless_hint)
        } else {
            activity.getString(R.string.optimize_jpegs_quality, binding.optimizeQuality.progress)
        }
    }

    private fun optimizeImages() {
        val lossless = binding.optimizeLossless.isChecked
        val qualitySetting = binding.optimizeQuality.progress.coerceIn(50, 100)
        activity.config.jpegOptimLossless = lossless
        activity.config.jpegOptimQuality = qualitySetting
        val nativeQuality = if (lossless) -1 else qualitySetting

        progressView.show()
        val parentPath = imagePaths.first().getParentPath()
        val pathsToRescan = arrayListOf<String>()
        val pathLastModifiedMap = mutableMapOf<String, Long>()
        var savedTotal = 0L
        var optimizedCount = 0
        var failureCount = 0

        activity.ensureWriteAccess(parentPath) {
            ensureBackgroundThread {
                for (i in imagePaths.indices) {
                    val path = imagePaths[i]
                    val lastModified = File(path).lastModified()
                    val result = JpegOptim.optimize(activity, path, nativeQuality)
                    if (!result.ok) {
                        failureCount++
                    } else if (result.bytesSaved > 0L) {
                        optimizedCount++
                        savedTotal += result.bytesSaved
                        pathsToRescan.add(path)
                        pathLastModifiedMap[path] = lastModified
                    }
                    activity.runOnUiThread {
                        progressView.progress = i + 1
                    }
                }

                activity.runOnUiThread {
                    val message = when {
                        failureCount > 0 && optimizedCount == 0 -> activity.getString(R.string.jpeg_optimize_failed)
                        optimizedCount == 0 -> activity.getString(R.string.jpegs_already_optimized)
                        else -> activity.getString(
                            R.string.jpegs_optimized,
                            optimizedCount,
                            savedTotal.formatSize()
                        )
                    }
                    activity.toast(message)
                }

                if (pathsToRescan.isEmpty()) {
                    activity.runOnUiThread {
                        dialog?.dismiss()
                        callback.invoke()
                    }
                } else {
                    activity.rescanPathsAndUpdateLastModified(pathsToRescan, pathLastModifiedMap) {
                        activity.runOnUiThread {
                            dialog?.dismiss()
                            callback.invoke()
                        }
                    }
                }
            }
        }
    }
}
