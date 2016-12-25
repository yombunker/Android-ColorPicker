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
import android.graphics.ComposeShader;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Shader;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.bunk3r.colorpicker.hue.HuePicker;
import com.bunk3r.colorpicker.hue.OnHueChangedListener;
import com.bunk3r.colorpicker.utils.DimenUtils;

public class ColorAreaPicker
        extends View
        implements ColorPicker, OnHueChangedListener {

    // Default values for the control
    private static final int NUMBER_OF_GRADIENTS = 256;
    private static final int DEFAULT_SELECTED_COLOR_RADIUS = 5;
    private static final int DEFAULT_WIDTH = 256;
    private static final int DEFAULT_HEIGHT = 256;

    // True if inflated trough XML, false if created programmatically
    private boolean wasInflated;

    // The ratio between the number of hues and the size of the view
    private float widthDensityMultiplier;
    private float heightDensityMultiplier;

    // Paint objects used throughout the view
    private Paint gradientsPaint;
    private Paint innerCirclePaint;
    private Paint outerCirclePaint;
    private Paint bitmapPaint;

    // Holds the width of the Slider's bitmap
    private int innerCircleWidth;

    // This is the color currently selected
    private int currentColor;

    // The color used as the base for all calculations
    private int baseColor;

    // Location of the last selected color
    private int currentX = 0, currentY = 0;

    // The size of the picker area
    private int pickerWidth = 0, pickerHeight = 0;

    // Objects needed for caching the colors
    private Matrix colorBitmapMatrix;
    private Bitmap colorsBitmap;
    private Canvas preRenderingCanvas;
    private Shader baseGradientShader;

    // Hue picker that will notify if the current hue has changed
    private HuePicker huePicker;

    // This object will be notified of any change in the color currently selected
    private OnColorChangedListener colorChangedListener;

    /**
     * Use this constructor to generate a DEFAULT_SIZE color picker area
     *
     * @param context to be used for inflating and to search for resources
     */
    @SuppressWarnings("unused")
    public ColorAreaPicker(@NonNull Context context) {
        super(context);

        int screenDensity = (int) Math.ceil(context.getResources().getDisplayMetrics().density);
        widthDensityMultiplier = screenDensity;
        heightDensityMultiplier = screenDensity;

        init(false);
    }

    public ColorAreaPicker(@NonNull Context context, @NonNull AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ColorAreaPicker(@NonNull Context context, @NonNull AttributeSet attrs, int defStyleAttr) {
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

        innerCirclePaint = new Paint();
        outerCirclePaint = new Paint();
        gradientsPaint = new Paint();
        bitmapPaint = new Paint();

        innerCircleWidth = DEFAULT_SELECTED_COLOR_RADIUS;
        innerCirclePaint.setStyle(Paint.Style.FILL);
        innerCirclePaint.setColor(Color.BLACK);

        outerCirclePaint.setStyle(Paint.Style.STROKE);
        outerCirclePaint.setColor(Color.WHITE);

        baseGradientShader = new LinearGradient(0.f,
                                                0.f,
                                                0.f,
                                                NUMBER_OF_GRADIENTS,
                                                Color.WHITE,
                                                Color.BLACK,
                                                Shader.TileMode.CLAMP);
        colorBitmapMatrix = new Matrix();
        colorsBitmap = Bitmap
                .createBitmap(NUMBER_OF_GRADIENTS, NUMBER_OF_GRADIENTS, Bitmap.Config.ARGB_4444);
        preRenderingCanvas = new Canvas(colorsBitmap);
    }

    // Update the main field colors depending on the current selected hue
    private void updateMainColors(int color) {
        if (baseColor == color) {
            return;
        }

        baseColor = color;

        Shader gradientShader = new LinearGradient(0.f,
                                                   0.f,
                                                   NUMBER_OF_GRADIENTS,
                                                   0.f,
                                                   Color.WHITE,
                                                   baseColor,
                                                   Shader.TileMode.CLAMP);
        ComposeShader shader = new ComposeShader(gradientShader,
                                                 baseGradientShader,
                                                 PorterDuff.Mode.MULTIPLY);
        gradientsPaint.setShader(shader);
        preRenderingCanvas
                .drawRect(0.f, 0.f, NUMBER_OF_GRADIENTS, NUMBER_OF_GRADIENTS, gradientsPaint);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        // We only modify the configuration if something changes
        if (changed) {
            final int width = right - left;
            final int height = bottom - top;

            pickerWidth = width;
            pickerHeight = height;

            widthDensityMultiplier = (float) width / NUMBER_OF_GRADIENTS;
            heightDensityMultiplier = (float) height / NUMBER_OF_GRADIENTS;
            colorBitmapMatrix.setScale(widthDensityMultiplier, heightDensityMultiplier);
            innerCirclePaint.setStrokeWidth(widthDensityMultiplier);
            outerCirclePaint.setStrokeWidth(widthDensityMultiplier + 2);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // we use default size if it's inflated through code
        if (!wasInflated) {
            widthMeasureSpec = (int) (DEFAULT_WIDTH * widthDensityMultiplier);
            heightMeasureSpec = (int) (DEFAULT_HEIGHT * heightDensityMultiplier);
        }

        setMeasuredDimension(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // Draws the scaled version of the hues
        canvas.drawBitmap(colorsBitmap, colorBitmapMatrix, bitmapPaint);

        canvas.drawCircle(currentX,
                          currentY,
                          innerCircleWidth * widthDensityMultiplier,
                          outerCirclePaint);
        canvas.drawCircle(currentX,
                          currentY,
                          innerCircleWidth * widthDensityMultiplier,
                          innerCirclePaint);
    }

    @Override
    public void setColor(@ColorInt int color) {
        notifyHuePicker(color);

        updateMainColors(color);

        updateCurrentColor();

        invalidate();
    }

    @Override
    public void setOnColorChangedListener(@NonNull OnColorChangedListener listener) {
        colorChangedListener = listener;
        notifyColor();
    }

    @Override
    public void setHuePicker(@NonNull HuePicker huePicker) {
        this.huePicker = huePicker;
        this.huePicker.setOnHueChangedListener(this);
    }

    @Override
    public void onHueChanged(@ColorInt int color) {
        setColor(color);
    }

    /*
    Notifies the hue picker when the base color has been change on the color picker
     */
    private void notifyHuePicker(@ColorInt int color) {
        if (huePicker != null) {
            huePicker.setColor(color);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // If the action is anything different that DOWN or MOVE we ignore the rest of the gesture
        int touchEvent = event.getAction();
        if (touchEvent != MotionEvent.ACTION_DOWN && touchEvent != MotionEvent.ACTION_MOVE) {
            return false;
        }

        // Transform the coordinates to a position inside the view
        float x = DimenUtils.adjustToBounds(event.getX(), 0, pickerWidth - 1);
        float y = DimenUtils.adjustToBounds(event.getY(), 0, pickerHeight - 1);

        // No need to update anything if you haven't moved
        if (x != currentX || y != currentY) {
            currentX = (int) x;
            currentY = (int) y;

            updateCurrentColor();

            // Re-draw the view
            invalidate();
        }

        return true;
    }

    private void updateCurrentColor() {
        final int transX = (int) (currentX / widthDensityMultiplier);
        final int transY = (int) (currentY / heightDensityMultiplier);
        currentColor = colorsBitmap.getPixel(transX, transY);

        notifyColor();
    }

    /**
     * Notifies the listener if something changed
     */
    private void notifyColor() {
        if (colorChangedListener != null) {
            colorChangedListener.onColorChanged(currentColor);
        }
    }

}