package com.surendramaran.yolov8tflite


import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.os.Handler
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import java.util.Timer
import kotlin.concurrent.timer


class OverlayView(context: Context?, attrs: AttributeSet?) : View(context, attrs) {

    private var results = listOf<BoundingBox>()
    private var boxPaint = Paint()
    private var textBackgroundPaint = Paint()
    private var textPaint = Paint()
    private var lastUpdateTime: Long = 0
    private val updateInterval: Long = 900 // 更新間隔 (ミリ秒)

    private var bounds = Rect()

    init {
        initPaints()
    }

    fun clear() {
        results = listOf()
        textPaint.reset()
        textBackgroundPaint.reset()
        boxPaint.reset()
        invalidate()
        initPaints()
    }

    private fun initPaints() {
        textBackgroundPaint.color = Color.BLACK
        textBackgroundPaint.style = Paint.Style.FILL
        textBackgroundPaint.textSize = 60f

        textPaint.color = Color.WHITE
        textPaint.style = Paint.Style.FILL
        textPaint.textSize = 65f

        boxPaint.color = ContextCompat.getColor(context!!, R.color.bounding_box_color)
        boxPaint.strokeWidth = 10F
        boxPaint.style = Paint.Style.STROKE
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)


        results.forEach {
            val left = it.x1 * width
            val top = it.y1 * height
            val right = it.x2 * width
            val bottom = it.y2 * height

            canvas.drawRect(left, top, right, bottom, boxPaint)
            val s = String.format("%.1f", it.cnf * 100) + "%"
            val drawableText = "${it.clsName} ${s}"
            textBackgroundPaint.getTextBounds(drawableText, 0, drawableText.length, bounds)
            val textWidth = bounds.width()
            val textHeight = bounds.height()
            canvas.drawRect(
                left,
                top,
                left + textWidth + BOUNDING_RECT_TEXT_PADDING,
                top + textHeight + BOUNDING_RECT_TEXT_PADDING,
                textBackgroundPaint
            )
            canvas.drawText(drawableText, left, top + bounds.height(), textPaint)





        }
    }

    fun setResults(boundingBoxes: List<BoundingBox>) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastUpdateTime < updateInterval) {
            return // 前回の更新から十分な時間が経過していない場合はスキップ
        }
        lastUpdateTime = currentTime

        // 更新ロジック（現在のコードをここに記述）
        val updatedResults = boundingBoxes.mapIndexed { index, box ->
            if (index < results.size) {
                val currentBox = results[index]
                if (Math.abs(box.cnf - currentBox.cnf) > 0) {
                    box.copy(cnf = currentBox.cnf)
                } else {
                    currentBox
                }
            } else {
                box
            }
        }

        if (updatedResults != results) {
            results = updatedResults
            invalidate()
        }
    }

    companion object {
        private const val BOUNDING_RECT_TEXT_PADDING = 8
    }
}