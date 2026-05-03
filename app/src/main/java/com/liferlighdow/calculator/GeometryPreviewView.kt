package com.liferlighdow.calculator

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import kotlin.math.min

class GeometryPreviewView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLUE
        style = Paint.Style.STROKE
        strokeWidth = 8f
    }

    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLUE
        alpha = 40
        style = Paint.Style.FILL
    }

    var shapeType: String = "Circle"
        set(value) {
            field = value
            invalidate()
        }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        val w = width.toFloat()
        val h = height.toFloat()
        val cx = w / 2
        val cy = h / 2
        val size = min(w, h) * 0.4f

        when (shapeType) {
            "Circle" -> {
                canvas.drawCircle(cx, cy, size, fillPaint)
                canvas.drawCircle(cx, cy, size, paint)
            }
            "Square" -> {
                canvas.drawRect(cx - size, cy - size, cx + size, cy + size, fillPaint)
                canvas.drawRect(cx - size, cy - size, cx + size, cy + size, paint)
            }
            "Triangle" -> {
                val path = android.graphics.Path().apply {
                    moveTo(cx, cy - size)
                    lineTo(cx - size, cy + size)
                    lineTo(cx + size, cy + size)
                    close()
                }
                canvas.drawPath(path, fillPaint)
                canvas.drawPath(path, paint)
            }
            "Rectangle" -> {
                canvas.drawRect(cx - size * 1.5f, cy - size, cx + size * 1.5f, cy + size, fillPaint)
                canvas.drawRect(cx - size * 1.5f, cy - size, cx + size * 1.5f, cy + size, paint)
            }
            "Sphere" -> {
                canvas.drawCircle(cx, cy, size, fillPaint)
                canvas.drawCircle(cx, cy, size, paint)
                canvas.drawOval(cx - size, cy - size / 4, cx + size, cy + size / 4, paint)
            }
            "Cylinder" -> {
                canvas.drawRect(cx - size / 2, cy - size, cx + size / 2, cy + size, fillPaint)
                canvas.drawLine(cx - size / 2, cy - size, cx - size / 2, cy + size, paint)
                canvas.drawLine(cx + size / 2, cy - size, cx + size / 2, cy + size, paint)
                canvas.drawOval(cx - size / 2, cy - size - size / 4, cx + size / 2, cy - size + size / 4, paint)
                canvas.drawOval(cx - size / 2, cy + size - size / 4, cx + size / 2, cy + size + size / 4, paint)
            }
        }
    }
}
