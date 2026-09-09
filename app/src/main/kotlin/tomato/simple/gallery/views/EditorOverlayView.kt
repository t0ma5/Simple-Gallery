package tomato.simple.gallery.views

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.simplemobiletools.commons.extensions.getProperPrimaryColor

class EditorOverlayView(context: Context, attrs: AttributeSet) : FrameLayout(context, attrs) {
    private val backgroundView = ImageView(context).apply {
        scaleType = ImageView.ScaleType.FIT_CENTER
        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
    }
    private val overlays = mutableListOf<TextView>()
    private var selectedOverlay: TextView? = null
    private var backgroundBitmap: Bitmap? = null
    var overlayColor: Int = context.getProperPrimaryColor()
    var overlayTextSizeSp = 28f

    init {
        addView(backgroundView)
        clipChildren = false
        clipToPadding = false
    }

    fun updateBackgroundBitmap(bitmap: Bitmap) {
        backgroundBitmap = bitmap
        backgroundView.setImageBitmap(bitmap)
    }

    fun addLabel(text: String) {
        val tv = TextView(context).apply {
            this.text = text
            setTextColor(overlayColor)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, overlayTextSizeSp)
            setShadowLayer(6f, 1f, 1f, Color.BLACK)
            setPadding(16, 8, 16, 8)
        }
        addView(tv, LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT))
        overlays.add(tv)
        selectedOverlay = tv
        attachDrag(tv)
        post {
            tv.x = ((width - tv.width) / 2f).coerceAtLeast(0f)
            tv.y = ((height - tv.height) / 2f).coerceAtLeast(0f)
        }
    }

    fun undo() {
        val last = overlays.removeLastOrNull() ?: return
        if (selectedOverlay === last) {
            selectedOverlay = overlays.lastOrNull()
        }
        removeView(last)
    }

    fun clearOverlays() {
        overlays.forEach { removeView(it) }
        overlays.clear()
        selectedOverlay = null
    }

    fun hasOverlays() = overlays.isNotEmpty()

    fun applySizeToSelected(percent: Int) {
        val tv = selectedOverlay ?: overlays.lastOrNull() ?: return
        overlayTextSizeSp = (12 + percent * 0.4f).coerceIn(12f, 72f)
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, overlayTextSizeSp)
    }

    fun applyColorToSelected(color: Int) {
        overlayColor = color
        val tv = selectedOverlay ?: overlays.lastOrNull() ?: return
        tv.setTextColor(color)
    }

    fun getBitmap(): Bitmap {
        val src = backgroundBitmap
        val outWidth = (src?.width ?: width).coerceAtLeast(1)
        val outHeight = (src?.height ?: height).coerceAtLeast(1)
        val bitmap = Bitmap.createBitmap(outWidth, outHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        if (width > 0 && height > 0 && (width != outWidth || height != outHeight)) {
            val scale = minOf(outWidth.toFloat() / width, outHeight.toFloat() / height)
            canvas.translate(
                (outWidth - width * scale) / 2f,
                (outHeight - height * scale) / 2f
            )
            canvas.scale(scale, scale)
        }
        draw(canvas)
        return bitmap
    }

    private fun attachDrag(view: View) {
        var dX = 0f
        var dY = 0f
        view.setOnTouchListener { v, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    dX = v.x - event.rawX
                    dY = v.y - event.rawY
                    selectedOverlay = v as? TextView
                    v.bringToFront()
                }
                MotionEvent.ACTION_MOVE -> {
                    val maxX = (width - v.width).toFloat().coerceAtLeast(0f)
                    val maxY = (height - v.height).toFloat().coerceAtLeast(0f)
                    v.x = (event.rawX + dX).coerceIn(0f, maxX)
                    v.y = (event.rawY + dY).coerceIn(0f, maxY)
                }
            }
            true
        }
    }
}
