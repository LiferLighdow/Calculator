package com.liferlighdow.calculator

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import kotlin.math.*

class CoordinateSystemView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val axisPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.GRAY
        strokeWidth = 2f
        style = Paint.Style.STROKE
    }

    private val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.LTGRAY
        strokeWidth = 1f
        alpha = 100
        style = Paint.Style.STROKE
    }

    private val graphPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.RED
        strokeWidth = 4f
        style = Paint.Style.STROKE
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.DKGRAY
        textSize = 24f
    }

    private var equation: String = ""
    private val points = mutableListOf<PointF>()
    
    // Range of coordinates
    private var minX = -10.0
    private var maxX = 10.0
    private var minY = -10.0
    private var maxY = 10.0

    fun setEquation(eq: String) {
        this.equation = eq
        calculatePoints()
        invalidate()
    }

    private fun calculatePoints() {
        points.clear()
        if (equation.isEmpty()) return

        val step = (maxX - minX) / 200.0
        var x = minX
        while (x <= maxX) {
            try {
                val y = MathEvaluator.evaluate(equation, x)
                if (!y.isNaN() && !y.isInfinite()) {
                    points.add(PointF(x.toFloat(), y.toFloat()))
                }
            } catch (e: Exception) {
                // Skip invalid points
            }
            x += step
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        val w = width.toFloat()
        val h = height.toFloat()
        
        drawGrid(canvas, w, h)
        drawAxes(canvas, w, h)
        drawGraph(canvas, w, h)
    }

    private fun drawAxes(canvas: Canvas, w: Float, h: Float) {
        val centerX = worldToScreenX(0f, w)
        val centerY = worldToScreenY(0f, h)
        
        // Y-axis
        canvas.drawLine(centerX, 0f, centerX, h, axisPaint)
        // X-axis
        canvas.drawLine(0f, centerY, w, centerY, axisPaint)
        
        // Labels
        canvas.drawText("X", w - 30f, centerY - 10f, textPaint)
        canvas.drawText("Y", centerX + 10f, 30f, textPaint)
    }

    private fun drawGrid(canvas: Canvas, w: Float, h: Float) {
        // Draw vertical grid lines
        for (x in minX.toInt()..maxX.toInt()) {
            val screenX = worldToScreenX(x.toFloat(), w)
            canvas.drawLine(screenX, 0f, screenX, h, gridPaint)
            if (x != 0) canvas.drawText(x.toString(), screenX - 10f, worldToScreenY(0f, h) + 25f, textPaint)
        }
        
        // Draw horizontal grid lines
        for (y in minY.toInt()..maxY.toInt()) {
            val screenY = worldToScreenY(y.toFloat(), h)
            canvas.drawLine(0f, screenY, w, screenY, gridPaint)
            if (y != 0) canvas.drawText(y.toString(), worldToScreenX(0f, w) + 10f, screenY + 10f, textPaint)
        }
    }

    private fun drawGraph(canvas: Canvas, w: Float, h: Float) {
        if (points.size < 2) return
        
        val path = Path()
        var first = true
        
        for (p in points) {
            val sx = worldToScreenX(p.x, w)
            val sy = worldToScreenY(p.y, h)
            
            if (sy < 0 || sy > h) {
                first = true
                continue
            }

            if (first) {
                path.moveTo(sx, sy)
                first = false
            } else {
                path.lineTo(sx, sy)
            }
        }
        canvas.drawPath(path, graphPaint)
    }

    private fun worldToScreenX(x: Float, width: Float): Float {
        return (x - minX.toFloat()) / (maxX.toFloat() - minX.toFloat()) * width
    }

    private fun worldToScreenY(y: Float, height: Float): Float {
        return height - (y - minY.toFloat()) / (maxY.toFloat() - minY.toFloat()) * height
    }
}
