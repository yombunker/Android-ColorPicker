package com.bunk3r.colorpicker.hue;

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
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.security.InvalidParameterException;

public class HueBarSlider extends View implements HuePicker {

    /**
     * Default values for the control
     */
    private static final int NUMBER_OF_HUES = 360;
    private static final int NUMBER_OF_HUE_SETS = 6;
    private static final int DEFAULT_SELECTED_HUE_WIDTH = 3;
    private static final int DEFAULT_WIDTH = 256;
    private static final int DEFAULT_HEIGHT = 30;

    // Used for when the hue is set before the first layout
    private boolean mIsHuePending = false;

    // Holds the width of the Slider's bitmap
    private int mSliderWidth;

    // True if inflated trough XML, false if created programmatically
    private boolean mWasInflated;

    // The width of the line that shows what color is currently selected
    private int mSelectedWidth;

    // The ratio between the number of hues and the size of the view
    private float mDensityMultiplier;

    // Paint object used throughout the view
    private Paint mPaint;

    // The Bitmap that holds the original 360 different hues
    private Bitmap mHuesBitmap;

    // The Bitmap that caches the scaled version of the hues
    private Bitmap mHueBarBitmap;


    private OnHueChangedListener mHueChangedListener;
    private float mCurrentHue;

    public HueBarSlider(Context context) {
        super(context);

        if (context != null) {
            mDensityMultiplier = (int) Math.ceil(context.getResources().getDisplayMetrics().density);
        }

        init(false);
    }

    public HueBarSlider(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HueBarSlider(Context context, AttributeSet attrs, int defStyleAttr) {
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
        mSelectedWidth = DEFAULT_SELECTED_HUE_WIDTH;
        mPaint = new Paint();
        preRenderHueBar();
    }

    /**
     * Sets the width of the line that shows the current selected hue
     *
     * @param width a positive value greater than 0 (1, 2, ....)
     */
    public void setCurrentHueWidth(int width) {
        mSelectedWidth = width > 0 ? width : 1;
    }

    /**
     * Calculates the different hues and caches them in a Bitmap
     */
    private void preRenderHueBar() {
        int index = 0;
        final int[] hueBarColors = new int[NUMBER_OF_HUES];
        final float hueIncrement = 255f / (NUMBER_OF_HUES / NUMBER_OF_HUE_SETS);

        for (float i = hueIncrement; i < 256; i += hueIncrement) // red (#f00) - yellow (#ff0)
        {
            hueBarColors[index] = Color.rgb(255, (int) i, 0);
            index++;
        }

        for (float i = hueIncrement; i < 256; i += hueIncrement) // yellow (#ff0) - green (#0f0)
        {
            hueBarColors[index] = Color.rgb(255 - (int) i, 255, 0);
            index++;
        }

        for (float i = hueIncrement; i < 256; i += hueIncrement) // green (#0f0) - cyan (#0ff)
        {
            hueBarColors[index] = Color.rgb(0, 255, (int) i);
            index++;
        }

        for (float i = hueIncrement; i < 256; i += hueIncrement) // cyan (#0ff) - blue (#00f)
        {
            hueBarColors[index] = Color.rgb(0, 255 - (int) i, 255);
            index++;
        }

        for (float i = hueIncrement; i < 256; i += hueIncrement) // blue (#00f) - Pink (#f0f)
        {
            hueBarColors[index] = Color.rgb((int) i, 0, 255);
            index++;
        }

        for (float i = hueIncrement; i < 256; i += hueIncrement) // pink (#f0f) - Red (#f00)
        {
            hueBarColors[index] = Color.rgb(255, 0, 255 - (int) i);
            index++;
        }

        mHuesBitmap = Bitmap.createBitmap(NUMBER_OF_HUES, 1, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(mHuesBitmap);
        preDrawHueSlider(canvas, hueBarColors);
    }

    /**
     * Draws all the hues into the canvas
     *
     * @param canvas where the hues will be drawn
     * @param hues list of colors that will be drawn
     */
    private void preDrawHueSlider(Canvas canvas, int[] hues) {
        final int height = canvas.getHeight();

        // Display all the colors of the hue bar with lines
        // The current selected color will be drawn with a BLACK line
        mPaint.setStrokeWidth(0);
        for (int x = 0; x < hues.length; x++) {
            mPaint.setColor(hues[x]);
            canvas.drawLine(x,
                    0,
                    x,
                    height,
                    mPaint);
        }
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        // We only modify the configuration if something changes
        if (changed) {
            final int width = right - left;
            final int height = bottom - top;

            mDensityMultiplier = (float)width / NUMBER_OF_HUES;
            mHueBarBitmap = Bitmap.createScaledBitmap(mHuesBitmap, width, height, false);
            mSliderWidth = width;

            if (mIsHuePending) {
                mCurrentHue *= mDensityMultiplier;
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // we use default size if it's inflated through code
        if (!mWasInflated) {
            widthMeasureSpec = (int) (DEFAULT_WIDTH * mDensityMultiplier);
            heightMeasureSpec = (int) (DEFAULT_HEIGHT * mDensityMultiplier);
        }

        setMeasuredDimension(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // Draws the scaled version of the hues
        canvas.drawBitmap(mHueBarBitmap, 0, 0, mPaint);

        // Draws the line of the current selected hue
        final int translatedHue = (int) (mCurrentHue);
        mPaint.setColor(Color.BLACK);
        mPaint.setStrokeWidth((int)(mSelectedWidth * mDensityMultiplier));
        canvas.drawLine(translatedHue,
                0,
                translatedHue,
                canvas.getHeight(),
                mPaint);
    }

    /**
     * Returns the hue value for an specific color
     *
     * @param color in form of ARGB (the alpha part is optional)
     * @return the hue value [0 - 360)
     */
    private float getHueFromColor(int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        return hsv[0] * mDensityMultiplier;
    }

    @Override
    public void setHue(float hue) {

        if (hue < 0f || hue >= NUMBER_OF_HUES) {
            throw new InvalidParameterException("The hue has to be between 0 (inclusive) and 360 (exclusive)");
        }

        // If it was inflated and hasn't being layout as far now,
        // we set it as a pending transaction
        if (mDensityMultiplier == 0) {
            mDensityMultiplier = 1;
            mIsHuePending = true;
        }

        mCurrentHue = hue * mDensityMultiplier;
        invalidate();
    }

    @Override
    public void setColor(int color) {
        // If it was inflated and hasn't being layout as far now,
        // we set it as a pending transaction
        if (mDensityMultiplier == 0) {
            mDensityMultiplier = 1;
            mIsHuePending = true;
        }

        mCurrentHue = getHueFromColor(color);
        invalidate();
    }

    @Override
    public void setOnHueChangedListener(OnHueChangedListener listener) {
        mHueChangedListener = listener;

        // We notify immediately to the listener of the current hue
        if (listener != null) {
            listener.onHueChanged(mHuesBitmap.getPixel((int)(mCurrentHue / mDensityMultiplier), 0));
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // If the action is anything different that DOWN or MOVE we ignore the rest of the gesture
        if (event.getAction() != MotionEvent.ACTION_DOWN &&	event.getAction() != MotionEvent.ACTION_MOVE) {
            return false;
        }

        // Transform the coordinates to a position inside the view
        float x = event.getX();
        if (x < 0) {
            x = 0;
        } else if (x >= mSliderWidth) {
            x = mSliderWidth - 1;
        }

        // Update the main field colors
        mCurrentHue = x;
        if (mHueChangedListener != null) {
            mHueChangedListener.onHueChanged(mHueBarBitmap.getPixel((int)x, 0));
        }

        // Re-draw the view
        invalidate();

        return true;
    }
}