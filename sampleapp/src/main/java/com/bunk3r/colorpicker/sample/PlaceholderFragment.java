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


import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.bunk3r.colorpicker.ColorPickerDialog;
import com.bunk3r.colorpicker.ColorPickerListener;

public class PlaceholderFragment extends Fragment {

    private static final String COLOR_KEY = "THE ONLY COLOR";
    private int mCurrentColor = Color.WHITE;

    public static Fragment newInstance() {
        return new PlaceholderFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_personalization, container, false);

        Button choseColorButton = (Button) rootView.findViewById(R.id.chose_color_button);
        choseColorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showColorPicker(v.getContext());
            }
        });

        return rootView;
    }

    private void showColorPicker(Context context) {
        ColorPickerDialog dialog = new ColorPickerDialog(context);
        dialog.setInitialColor(mCurrentColor);
        dialog.setColorPickerLister(new ColorPickerListener() {
            @Override
            public void onSelectionCancel(String key) {

            }

            @Override
            public void onColorSelected(String key, int color) {
                if (isAdded()) {
                    mCurrentColor = color;
                    View layout = getView();
                    if (layout != null) {
                        layout.setBackgroundColor(mCurrentColor);
                    }
                }
            }
        }, COLOR_KEY);

        dialog.show();
    }
}