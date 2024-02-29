package com.example.drawingapp

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.net.Uri
import android.provider.MediaStore
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import java.util.LinkedList
import java.util.Queue


class DrawingView(context: Context, attrs: AttributeSet): View(context,attrs) {

    private var mDrawPath: CustomPath? = null
    private var mCanvasBitmap: Bitmap? = null
    private var mDrawPaint: Paint? = null
    private var mCanvasPaint: Paint? = null
    private var mBrushSize: Float = 0.toFloat()
    private var color = Color.BLACK
    private var canvas: Canvas? = null
    private val mPaths = ArrayList<CustomPath>()
    private val mUndoPaths = ArrayList<CustomPath>()
    private val mRedoPaths = ArrayList<CustomPath>()
    private val removedPaths = mutableListOf<CustomPath>()

    init {
        setUpDrawing()
    }

    internal fun onClickUndo() {
        if (mPaths.isNotEmpty()) {
            val removedPath = mPaths.removeLast()
            removedPaths.add(removedPath)
            invalidate()
        }
    }

    internal fun onClickRedo() {
        if (removedPaths.isNotEmpty()) {
            val restoredPath = removedPaths.removeLast()
            mPaths.add(restoredPath)
            invalidate()
        }
    }


    private fun setUpDrawing() {
        mDrawPaint = Paint()
        mDrawPath = CustomPath(color, mBrushSize)
        mDrawPaint!!.color = color
        mDrawPaint!!.style = Paint.Style.STROKE
        mDrawPaint!!.strokeJoin = Paint.Join.ROUND
        mDrawPaint!!.strokeCap = Paint.Cap.ROUND
        mCanvasPaint = Paint(Paint.DITHER_FLAG)
        //  mBrushSize = 20.toFloat()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mCanvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        canvas = Canvas(mCanvasBitmap!!)
    }

    // Change Canvas to Canvas? if fails
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        mCanvasBitmap?.let { bitmap ->
            val viewWidth = width
            val viewHeight = height
            val bitmapWidth = bitmap.width
            val bitmapHeight = bitmap.height

            val scaleFactor =
                Math.min(viewWidth.toFloat() / bitmapWidth, viewHeight.toFloat() / bitmapHeight)
            val scaledWidth = bitmapWidth * scaleFactor
            val scaledHeight = bitmapHeight * scaleFactor

            val left = (viewWidth - scaledWidth) / 2
            val top = (viewHeight - scaledHeight) / 2
            val srcRect = Rect(0, 0, bitmapWidth, bitmapHeight)
            val destRect = Rect(
                left.toInt(),
                top.toInt(),
                (left + scaledWidth).toInt(),
                (top + scaledHeight).toInt()
            )
            canvas.drawBitmap(bitmap, srcRect, destRect, mCanvasPaint)

        }

        for (path in mPaths) {
            mDrawPaint!!.strokeWidth = path.brushThickness
            mDrawPaint!!.color = path.color
            canvas.drawPath(path, mDrawPaint!!)
        }

        if (!mDrawPath!!.isEmpty) {
            mDrawPaint!!.strokeWidth = mDrawPath!!.brushThickness
            mDrawPaint!!.color = mDrawPath!!.color
            canvas.drawPath(mDrawPath!!, mDrawPaint!!)
        }

    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {

        val touchX = event?.x
        val touchY = event?.y

        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                mDrawPath!!.color = color
                mDrawPath!!.brushThickness = mBrushSize
                mDrawPath!!.reset()
                mDrawPath!!.moveTo(touchX!!, touchY!!)
            }

            MotionEvent.ACTION_MOVE -> {
                mDrawPath!!.lineTo(touchX!!, touchY!!)

            }

            MotionEvent.ACTION_UP -> {
                mPaths.add(mDrawPath!!)
                mDrawPath = CustomPath(color, mBrushSize)

            }
        }
        invalidate()
        return true
        //return super.onTouchEvent(event)
    }


    fun setSizeForBrush(newSize: Float) {
        mBrushSize = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            newSize, resources.displayMetrics
        )
        mDrawPaint!!.strokeWidth = mBrushSize
    }


    fun setColor(newColor: String) {
        color = Color.parseColor(newColor)
        mDrawPaint!!.color = color
    }

    internal inner class CustomPath(var color: Int, var brushThickness: Float) : Path() {

    }

    fun setBackgroundBitmap(bitmap: Bitmap) {
        mCanvasBitmap = bitmap
        invalidate()
    }




}


