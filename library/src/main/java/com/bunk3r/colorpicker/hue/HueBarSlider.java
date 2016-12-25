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

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Shader;
import android.support.annotation.ColorInt;
import android.support.annotation.FloatRange;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.bunk3r.colorpicker.utils.DimenUtils;

import java.security.InvalidParameterException;

public class HueBarSlider
        extends View
        implements HuePicker {

    // Default values for the control
    private static final int NUMBER_OF_HUES = 360;
    private static final int DEFAULT_SELECTED_HUE_WIDTH = 5;
    private static final int DEFAULT_WIDTH = 256;
    private static final int DEFAULT_HEIGHT = 30;

    // Used for when the hue is set before the first layout
    private boolean huePending = false;

    // Holds the width of the Slider's bitmap
    private int sliderWidth;

    // True if inflated trough XML, false if created programmatically
    private boolean wasInflated;

    // The width of the line that shows what color is currently selected
    private int selectedWidth;

    // The ratio between the number of hues and the size of the view
    private float densityMultiplier;

    // Paint object used throughout the view
    private Paint paint;

    // The Bitmap that holds the original 360 different hues
    private Bitmap huesBitmap;

    // The Bitmap that caches the scaled version of the hues
    private Rect hueBarRect;

    private OnHueChangedListener hueChangedListener;

    private float currentHue;

    public HueBarSlider(@NonNull Context context) {
        super(context);

        densityMultiplier = (int) Math.ceil(context.getResources().getDisplayMetrics().density);
        init(false);
    }

    public HueBarSlider(@NonNull Context context, @NonNull AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HueBarSlider(@NonNull Context context, @NonNull AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(true);
    }

    /**
     * Sets all the initial configuration and pre-rendering for the view
     *
     * @param wasInflated if it was or not inflated via XML
     */
    private void init(boolean wasInflated) {
        this.wasInflated = wasInflated;
        selectedWidth = DEFAULT_SELECTED_HUE_WIDTH;
        paint = new Paint();
        hueBarRect = new Rect();
        preRenderHueBar();
    }

    /**
     * Sets the width of the line that shows the current selected hue
     *
     * @param width a positive value greater than 0 (1, 2, ....)
     */
    public void setCurrentHueWidth(@IntRange(from = 0) int width) {
        selectedWidth = width > 0 ? width : 1;
    }

    /**
     * Calculates the different hues and caches them in a Bitmap
     */
    private void preRenderHueBar() {
        int[] hueBarColors = {Color.RED,
                              Color.YELLOW,
                              Color.GREEN,
                              Color.CYAN,
                              Color.BLUE,
                              Color.MAGENTA,
                              Color.RED};

        huesBitmap = Bitmap.createBitmap(NUMBER_OF_HUES, 1, Bitmap.Config.ARGB_4444);
        Canvas canvas = new Canvas(huesBitmap);

        paint.setShader(new LinearGradient(0.f,
                                           0.f,
                                           NUMBER_OF_HUES,
                                           0.f,
                                           hueBarColors,
                                           null,
                                           Shader.TileMode.CLAMP));
        canvas.drawLine(0.f, 0.f, NUMBER_OF_HUES, 0.f, paint);
        paint.setShader(null);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        // We only modify the configuration if something changes
        if (changed) {
            final int width = right - left;
            final int height = bottom - top;

            densityMultiplier = (float) width / NUMBER_OF_HUES;
            hueBarRect.set(0, 0, width, height);
            sliderWidth = width;

            if (huePending) {
                currentHue *= densityMultiplier;
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // we use default size if it's inflated through code
        if (!wasInflated) {
            widthMeasureSpec = (int) (DEFAULT_WIDTH * densityMultiplier);
            heightMeasureSpec = (int) (DEFAULT_HEIGHT * densityMultiplier);
        }

        setMeasuredDimension(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // Draws the scaled version of the hues
        canvas.drawBitmap(huesBitmap, null, hueBarRect, paint);

        // Draws the line of the current selected hue
        final int translatedHue = (int) (currentHue);
        paint.setColor(Color.BLACK);
        paint.setStrokeWidth((int) (selectedWidth * densityMultiplier));
        canvas.drawLine(translatedHue,
                        0,
                        translatedHue,
                        canvas.getHeight(),
                        paint);
    }

    /**
     * Returns the hue value for an specific color
     *
     * @param color in form of ARGB (the alpha part is optional)
     * @return the hue value [0 - 360)
     */
    private float getHueFromColor(@ColorInt int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        return hsv[0] * densityMultiplier;
    }

    @Override
    public void setHue(@FloatRange(from = 0, to = NUMBER_OF_HUES, toInclusive = false) float hue) {
        if (hue < 0f || hue >= NUMBER_OF_HUES) {
            throw new InvalidParameterException(
                    "The hue has to be between 0 (inclusive) and 360 (exclusive)");
        }

        // If it was inflated and hasn't being layout as far now,
        // we set it as a pending transaction
        if (densityMultiplier == 0) {
            densityMultiplier = 1;
            huePending = true;
        }

        currentHue = hue * densityMultiplier;
        invalidate();
    }

    @Override
    public void setColor(@ColorInt int color) {
        // If it was inflated and hasn't being layout as far now,
        // we set it as a pending transaction
        if (densityMultiplier == 0) {
            densityMultiplier = 1;
            huePending = true;
        }

        currentHue = getHueFromColor(color);
        invalidate();
    }

    @Override
    public void setOnHueChangedListener(@NonNull OnHueChangedListener listener) {
        hueChangedListener = listener;

        // We notify immediately to the listener of the current hue
        listener.onHueChanged(huesBitmap.getPixel((int) (currentHue / densityMultiplier), 0));
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // If the action is anything different that DOWN or MOVE we ignore the rest of the gesture
        int touchEvent = event.getAction();
        if (touchEvent != MotionEvent.ACTION_DOWN && touchEvent != MotionEvent.ACTION_MOVE) {
            return false;
        }

        // Transform the coordinates to a position inside the view
        float x = DimenUtils.adjustToBounds(event.getX(), 0, sliderWidth - 1);

        // Update the main field colors
        currentHue = x;
        if (hueChangedListener != null) {
            final int transX = (int) (x / densityMultiplier);
            final int currentHue = huesBitmap.getPixel(transX, 0);
            hueChangedListener.onHueChanged(currentHue);
        }

        // Re-draw the view
        invalidate();

        return true;
    }
}