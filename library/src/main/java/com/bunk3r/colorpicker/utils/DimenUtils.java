package com.bunk3r.colorpicker.utils;

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

import java.security.InvalidParameterException;

public final class DimenUtils {

    public static int adjustToBounds(int value, int lower, int upper) {
        if (lower > upper) {
            throw new InvalidParameterException("Lower bound can't be higher than the upper bound");
        }
        value = value < lower ? lower : value;
        value = value > upper ? upper : value;
        return value;
    }

    public static float adjustToBounds(float value, float lower, float upper) {
        if (lower > upper) {
            throw new InvalidParameterException("Lower bound can't be higher than the upper bound");
        }
        value = value < lower ? lower : value;
        value = value > upper ? upper : value;
        return value;
    }

}