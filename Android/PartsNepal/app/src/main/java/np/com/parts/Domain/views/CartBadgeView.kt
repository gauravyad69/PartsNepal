package np.com.parts.Domain.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import np.com.parts.R

class CartBadgeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = context.getColor(R.color.status_sending)
        textSize = context.resources.getDimensionPixelSize(R.dimen.badge_text_size).toFloat()
        textAlign = Paint.Align.CENTER
    }

    private var count: Int = 0
    private val bounds = Rect()

    fun setCount(value: Int) {
        count = value
        visibility = if (count > 0) VISIBLE else GONE
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (count > 0) {
            val text = if (count > 99) "99+" else count.toString()
            paint.getTextBounds(text, 0, text.length, bounds)
            
            // Draw circle background
            canvas.drawCircle(width / 2f, height / 2f, width / 2f, paint)
            
            // Draw text
            paint.color = Color.WHITE
            canvas.drawText(
                text,
                width / 2f,
                height / 2f + bounds.height() / 2f,
                paint
            )
        }
    }
} 