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

import android.support.annotation.ColorInt;
import android.support.annotation.FloatRange;
import android.support.annotation.NonNull;

public interface HuePicker {

    /**
     * Changes the current selected hue
     *
     * @param hue in range of [0, 360)
     */
    void setHue(@FloatRange(from = 0, to = 360, toInclusive = false) float hue);

    /**
     * Changes the current selected hue based on the input color
     *
     * @param color in form of ARGB, where the Alpha channel is optional
     */
    void setColor(@ColorInt int color);

    /**
     * Sets the listener that will be used whenever something changes in the HuePicker
     *
     * @param listener (if null passed, it will stop reporting changes)
     */
    void setOnHueChangedListener(@NonNull OnHueChangedListener listener);

}