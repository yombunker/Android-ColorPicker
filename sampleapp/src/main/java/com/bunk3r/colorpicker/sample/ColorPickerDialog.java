package com.bunk3r.colorpicker.sample;

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

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;

import com.bunk3r.colorpicker.ColorPickerListener;
import com.bunk3r.colorpicker.color.ColorAreaPicker;
import com.bunk3r.colorpicker.color.OnColorChangedListener;
import com.bunk3r.colorpicker.hue.HuePicker;

public class ColorPickerDialog extends Dialog implements OnColorChangedListener {

    private ColorPickerListener mColorPickerListener;
    private String mKey;

    private View mSelectedColorPreview;

    private int mInitialColor;
    private int mSelectedColor;

    public ColorPickerDialog(Context context) {
        super(context);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setBackgroundDrawableResource(R.drawable.transparent);
    }

    public void setColorPickerLister(ColorPickerListener colorPickerLister, String key) {
        mColorPickerListener = colorPickerLister;
        mKey = key;
    }

    public void setInitialColor(int color) {
        mInitialColor = color;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_color_picker);

        HuePicker huePicker = (HuePicker) findViewById(R.id.hue_slider);
        ColorAreaPicker colorAreaPicker = (ColorAreaPicker) findViewById(R.id.color_area_picker);
        Button okButton = (Button) findViewById(R.id.ok_button);
        Button cancelButton = (Button) findViewById(R.id.cancel_button);
        View currentColorPreview = findViewById(R.id.current_color);
        mSelectedColorPreview = findViewById(R.id.selected_color);

        currentColorPreview.setBackgroundColor(mInitialColor);
        mSelectedColorPreview.setBackgroundColor(mInitialColor);

        colorAreaPicker.setOnColorChangedListener(this);
        colorAreaPicker.setHuePicker(huePicker);
        colorAreaPicker.setColor(mInitialColor);

        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mColorPickerListener.onColorSelected(mKey, mSelectedColor);
                dismiss();
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mColorPickerListener.onSelectionCancel(mKey);
                dismiss();
            }
        });
    }

    @Override
    public void onColorChanged(int color) {
        mSelectedColor = color;
        mSelectedColorPreview.setBackgroundColor(mSelectedColor);
    }
}