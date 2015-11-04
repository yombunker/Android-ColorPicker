package com.bunk3r.colorpicker.color;

/*
 * Copyright (C) 10/2014 - Bunk3r
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.bunk3r.colorpicker.hue.HuePicker;
import com.bunk3r.colorpicker.hue.OnHueChangedListener;
import com.bunk3r.colorpicker.perf.TrackerId;
import com.microsoft.perf.PerfManager;

public class ColorAreaPicker extends View implements ColorPicker, OnHueChangedListener {

    /**
     * Default values for the control
     */
    private static final int NUMBER_OF_GRADIENTS = 256;
    private static final int DEFAULT_SELECTED_COLOR_RADIUS = 5;
    private static final int DEFAULT_WIDTH = 256;
    private static final int DEFAULT_HEIGHT = 256;

    // True if inflated trough XML, false if created programmatically
    private boolean mWasInflated;

    // The ratio between the number of hues and the size of the view
    private float mWidthDensityMultiplier;
    private float mHeightDensityMultiplier;

    // Paint objects used throughout the view
    private Paint mGradientsPaint;
    private Paint mInnerCirclePaint;
    private Paint mOuterCirclePaint;
    private Paint mBitmapPaint;

    // Holds the width of the Slider's bitmap
    private int mInnerCircleWidth;

    // This is the color currently selected
    private int mCurrentColor;

    // The color used as the base for all calculations
    private int mBaseColor;

    // Location of the last selected color
    private int mCurrentX = 0, mCurrentY = 0;
    private boolean mHasMoved = false;

    // The size of the picker area
    private int mPickerWidth = 0, mPickerHeight = 0;

    // Objects needed for caching the colors
    private Matrix mColorBitmapMatrix;
    private Bitmap mColorsBitmap;
    private Canvas mPreRenderingCanvas;

    // Hue picker that will notify if the current hue has changed
    private HuePicker mHuePicker;

    // This object will be notified of any change in the color currently selected
    private OnColorChangedListener mColorChangedListener;

    /**
     * Use this constructor to generate a DEFAULT_SIZE color picker area
     *
     * @param context to be used for inflating and to search for resources
     */
    @SuppressWarnings("unused")
    public ColorAreaPicker(Context context) {
        super(context);

        if (context != null) {
            int screenDensity = (int) Math.ceil(context.getResources().getDisplayMetrics().density);
            mWidthDensityMultiplier = screenDensity;
            mHeightDensityMultiplier = screenDensity;
        }

        init(false);
    }

    @SuppressWarnings("unused")
    public ColorAreaPicker(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ColorAreaPicker(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(true);
    }

    /**
     * Sets all the initial configuration and pre-rendering for the view
     *
     * @param wasInflated if it was or not inflated via XML
     */
    private void init(boolean wasInflated) {
        mWasInflated = wasInflated;

        mInnerCirclePaint = new Paint();
        mOuterCirclePaint = new Paint();
        mGradientsPaint = new Paint();
        mBitmapPaint = new Paint();

        mInnerCircleWidth = DEFAULT_SELECTED_COLOR_RADIUS;
        mInnerCirclePaint.setStyle(Paint.Style.STROKE);
        mInnerCirclePaint.setColor(Color.BLACK);

        mOuterCirclePaint.setStyle(Paint.Style.STROKE);
        mOuterCirclePaint.setColor(Color.WHITE);

        mColorBitmapMatrix = new Matrix();
        mColorsBitmap = Bitmap.createBitmap(256, 256, Bitmap.Config.ARGB_8888);
        mPreRenderingCanvas = new Canvas(mColorsBitmap);
    }

    // Update the main field colors depending on the current selected hue
    private void updateMainColors(int color) {
        if (mBaseColor == color) {
            return;
        }

        mBaseColor = color;

        final int baseRed = Color.red(mBaseColor);
        final int baseGreen = Color.green(mBaseColor);
        final int baseBlue = Color.blue(mBaseColor);

        // draws the NUMBER_OF_GRADIENTS into a bitmap for later use
        int[] colors = new int[2];
        colors[1] = Color.BLACK;
        for (int x = 0; x < 256; ++x) {
            colors[0] = Color.rgb(
                    255 - (255 - baseRed) * x / 255,
                    255 - (255 - baseGreen) * x / 255,
                    255 - (255 - baseBlue) * x / 255);
            mGradientsPaint.setShader(new LinearGradient(0,
                    0,
                    0,
                    256,
                    colors,
                    null,
                    Shader.TileMode.CLAMP));
            mPreRenderingCanvas.drawLine(x, 0, x, 256, mGradientsPaint);
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        // We only modify the configuration if something changes
        if (changed) {
            final int width = right - left;
            final int height = bottom - top;

            mPickerWidth = width;
            mPickerHeight = height;

            mWidthDensityMultiplier = (float) width / NUMBER_OF_GRADIENTS;
            mHeightDensityMultiplier = (float) height / NUMBER_OF_GRADIENTS;
            mColorBitmapMatrix.setScale(mWidthDensityMultiplier, mHeightDensityMultiplier);
            mInnerCirclePaint.setStrokeWidth(mWidthDensityMultiplier);
            mOuterCirclePaint.setStrokeWidth(mWidthDensityMultiplier + 2);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // we use default size if it's inflated through code
        if (!mWasInflated) {
            widthMeasureSpec = (int) (DEFAULT_WIDTH * mWidthDensityMultiplier);
            heightMeasureSpec = (int) (DEFAULT_HEIGHT * mHeightDensityMultiplier);
        }

        setMeasuredDimension(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // Draws the scaled version of the hues
        canvas.drawBitmap(mColorsBitmap, mColorBitmapMatrix, mBitmapPaint);

        canvas.drawCircle(mCurrentX, mCurrentY, mInnerCircleWidth * mWidthDensityMultiplier, mOuterCirclePaint);
        canvas.drawCircle(mCurrentX, mCurrentY, mInnerCircleWidth * mWidthDensityMultiplier, mInnerCirclePaint);
    }

    @Override
    public void setColor(int color) {
        PerfManager.startElapseTime(TrackerId.GENERATE_COLORS_ELAPSE);
        notifyHuePicker(color);

        PerfManager.startElapseTime(TrackerId.GENERATE_MAIN_COLORS_ELAPSE);
        updateMainColors(color);
        PerfManager.stopElapseTime(TrackerId.GENERATE_MAIN_COLORS_ELAPSE);

        updateCurrentColor();

        notifyColor();

        invalidate();
        PerfManager.stopElapseTime(TrackerId.GENERATE_COLORS_ELAPSE);
    }

    @Override
    public void setOnColorChangedListener(OnColorChangedListener listener) {
        mColorChangedListener = listener;
        notifyColor();
    }

    @Override
    public void setHuePicker(HuePicker huePicker) {
        mHuePicker = huePicker;
        mHuePicker.setOnHueChangedListener(this);
    }

    @Override
    public void onHueChanged(int color) {
        setColor(color);
    }

    /*
    Notifies the hue picker when the base color has been change on the color picker
     */
    private void notifyHuePicker(int color) {
        if (mHuePicker != null) {
            mHuePicker.setColor(color);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // If the action is anything different that DOWN or MOVE we ignore the rest of the gesture
        if (event.getAction() != MotionEvent.ACTION_DOWN && event.getAction() != MotionEvent.ACTION_MOVE) {
            PerfManager.stopAverageFPS(TrackerId.COLOR_AREA_FPS);
            return false;
        }

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            PerfManager.startAverageFPS(TrackerId.COLOR_AREA_FPS);
        } else {
            PerfManager.updateAverageFPS(TrackerId.COLOR_AREA_FPS);
        }

        // Transform the coordinates to a position inside the view
        float x = event.getX();
        float y = event.getY();
        mHasMoved = true;

        // Adjust X coordinate
        if (x < 0) {
            x = 0;
        } else if (x >= mPickerWidth) {
            x = mPickerWidth - 1;
        }

        // Adjust Y coordinate
        if (y < 0) {
            y = 0;
        } else if (y >= mPickerHeight) {
            y = mPickerHeight - 1;
        }

        mCurrentX = (int) x;
        mCurrentY = (int) y;

        updateCurrentColor();

        notifyColor();

        // Re-draw the view
        invalidate();

        return true;
    }

    private void updateCurrentColor() {
        if (mHasMoved) {
            final int transX = (int) (mCurrentX / mWidthDensityMultiplier);
            final int transY = (int) (mCurrentY / mHeightDensityMultiplier);
            mCurrentColor = mColorsBitmap.getPixel(transX, transY);
        } else {
            mCurrentColor = mBaseColor;
        }
    }

    /**
     * Notifies the listener if something changed
     */
    private void notifyColor() {
        if (mColorChangedListener != null) {
            mColorChangedListener.onColorChanged(mCurrentColor);
        }
    }

}