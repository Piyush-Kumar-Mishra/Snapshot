package com.example.snapshot.processor

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import com.example.snapshot.model.PersonProfile
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.max
import kotlin.math.min

/**
 * Renders an Instagram Story-style 9:16 (1080x1920) shareable collage bitmap.
 */
@Singleton
class CollageGenerator @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    companion object {
        const val CANVAS_WIDTH = 1080
        const val CANVAS_HEIGHT = 1920
    }

    suspend fun generateCollage(
        persons: List<PersonProfile>,
        videoTitle: String = "Portrait Video Analysis"
    ): Bitmap = withContext(Dispatchers.Default) {
        val bitmap = Bitmap.createBitmap(CANVAS_WIDTH, CANVAS_HEIGHT, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // 1. Draw elegant dark gradient background
        val bgPaint = Paint().apply {
            shader = LinearGradient(
                0f, 0f, 0f, CANVAS_HEIGHT.toFloat(),
                intArrayOf(
                    Color.parseColor("#0F172A"), // Deep slate
                    Color.parseColor("#18182E"), // Indigo-slate
                    Color.parseColor("#090D16")  // Dark midnight
                ),
                floatArrayOf(0f, 0.55f, 1f),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, CANVAS_WIDTH.toFloat(), CANVAS_HEIGHT.toFloat(), bgPaint)

        // 2. Draw Header
        drawHeader(canvas)

        // 3. Draw Person Tiles in dynamic Instagram Story layouts
        val contentTop = 80f
        val contentBottom = CANVAS_HEIGHT - 24f
        val contentLeft = 28f
        val contentRight = CANVAS_WIDTH - 28f
        val contentWidth = contentRight - contentLeft
        val contentHeight = contentBottom - contentTop

        val tileRects = calculateLayout(persons.size, contentLeft, contentTop, contentWidth, contentHeight)

        for (i in persons.indices) {
            if (i >= tileRects.size) break
            drawPersonCard(canvas, tileRects[i], persons[i])
        }

        bitmap
    }

    private fun drawHeader(canvas: Canvas) {
        val brandPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#94A3B8")
            textSize = 24f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            letterSpacing = 0.2f
        }
        canvas.drawText("SNAPSHOT", 32f, 52f, brandPaint)
    }

    private fun calculateLayout(
        count: Int,
        left: Float,
        top: Float,
        width: Float,
        height: Float
    ): List<RectF> {
        val rects = mutableListOf<RectF>()
        val gap = 18f

        when (count) {
            0 -> Unit
            1 -> {
                rects.add(RectF(left, top, left + width, top + height))
            }
            2 -> {
                val cardHeight = (height - gap) / 2f
                rects.add(RectF(left, top, left + width, top + cardHeight))
                rects.add(RectF(left, top + cardHeight + gap, left + width, top + 2 * cardHeight + gap))
            }
            3 -> {
                val topHeight = (height - gap) * 0.52f
                val bottomHeight = height - topHeight - gap
                val bottomWidth = (width - gap) / 2f

                rects.add(RectF(left, top, left + width, top + topHeight))
                rects.add(RectF(left, top + topHeight + gap, left + bottomWidth, top + height))
                rects.add(RectF(left + bottomWidth + gap, top + topHeight + gap, left + width, top + height))
            }
            4 -> {
                val colW = (width - gap) / 2f
                val rowH = (height - gap) / 2f
                for (r in 0..1) {
                    for (c in 0..1) {
                        val cardLeft = left + c * (colW + gap)
                        val cardTop = top + r * (rowH + gap)
                        rects.add(RectF(cardLeft, cardTop, cardLeft + colW, cardTop + rowH))
                    }
                }
            }
            5 -> {
                // Original orientation: 2 on top, 3 on bottom
                val row1Height = (height - gap) * 0.52f
                val row2Height = height - row1Height - gap

                val topColW = (width - gap) / 2f
                rects.add(RectF(left, top, left + topColW, top + row1Height))
                rects.add(RectF(left + topColW + gap, top, left + width, top + row1Height))

                val botColW = (width - 2 * gap) / 3f
                val botTop = top + row1Height + gap
                for (c in 0..2) {
                    val cardLeft = left + c * (botColW + gap)
                    rects.add(RectF(cardLeft, botTop, cardLeft + botColW, botTop + row2Height))
                }
            }
            else -> {
                // 6+ persons: 2 columns grid
                val rows = (count + 1) / 2
                val colW = (width - gap) / 2f
                val rowH = (height - (rows - 1) * gap) / rows.toFloat()
                for (i in 0 until count) {
                    val r = i / 2
                    val c = i % 2
                    val cardLeft = left + c * (colW + gap)
                    val cardTop = top + r * (rowH + gap)
                    rects.add(RectF(cardLeft, cardTop, cardLeft + colW, cardTop + rowH))
                }
            }
        }

        return rects
    }

    private fun drawPersonCard(canvas: Canvas, rect: RectF, person: PersonProfile) {
        val cornerRadius = 24f

        // Draw image clipped inside rounded rectangle
        val clipPath = Path().apply {
            addRoundRect(rect, cornerRadius, cornerRadius, Path.Direction.CW)
        }

        canvas.save()
        canvas.clipPath(clipPath)

        // Draw portrait image centered and scaled to fill the tile
        val shot = person.representativeShot
        val srcRect = calculateCenterCropRect(shot.width, shot.height, rect.width().toInt(), rect.height().toInt())
        canvas.drawBitmap(shot, srcRect, rect, Paint(Paint.FILTER_BITMAP_FLAG))

        canvas.restore()

        // Card border stroke
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#33FFFFFF")
            style = Paint.Style.STROKE
            strokeWidth = 2.5f
        }
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, borderPaint)
    }

    private fun calculateCenterCropRect(srcW: Int, srcH: Int, dstW: Int, dstH: Int): Rect {
        val srcRatio = srcW.toFloat() / srcH
        val dstRatio = dstW.toFloat() / dstH

        var cropW = srcW
        var cropH = srcH
        var cropX = 0
        var cropY = 0

        if (srcRatio > dstRatio) {
            cropW = (srcH * dstRatio).toInt()
            cropX = (srcW - cropW) / 2
        } else {
            cropH = (srcW / dstRatio).toInt()
            // Bias slightly towards top (35%) so hair/forehead is not cut off
            cropY = ((srcH - cropH) * 0.35f).toInt()
        }

        return Rect(cropX, cropY, cropX + cropW, cropY + cropH)
    }
}
