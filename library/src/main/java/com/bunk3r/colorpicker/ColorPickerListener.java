package com.bunk3r.colorpicker;

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

public interface ColorPickerListener {

    /**
     * This method is called if the color selection was
     * canceled by the user
     *
     * @param key the color selection that was cancelled
     */
    void onSelectionCancel(String key);

    /**
     * Called when the user selects the color they want
     *
     * @param key the color selection that was cancelled
     * @param color in form of ARGB, where the Alpha channel is optional
     */
    void onColorSelected(String key, int color);

}