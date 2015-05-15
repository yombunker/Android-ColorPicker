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

import com.bunk3r.colorpicker.hue.HuePicker;

public interface ColorPicker {

    /**
     * Sets the new base color and recalculates the color palette
     *
     * @param color in form of ARGB, where the Alpha channel is optional
     */
    void setColor(int color);

    /**
     * Sets the hue picker that will be use in conjunction with the color picker
     *
     * @param huePicker send null if the hue picker wants to be removed
     */
    void setHuePicker(HuePicker huePicker);

    /**
     * Sets the listener that will be used whenever something changes in the HuePicker
     *
     * @param listener (if null passed, it will stop reporting changes)
     */
    void setOnColorChangedListener(OnColorChangedListener listener);

}