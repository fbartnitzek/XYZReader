package com.example.xyzreader.ui;

import android.text.format.DateUtils;

/**
 * Copyright 2016.  Frank Bartnitzek
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

public class Utilities {

    public static String TRANSITION_NAME_IMAGE_VIEW = "transition_name_image_view_";

    public static String formatTimeSpan(long startMillis, long endMillis) {
        return  DateUtils.getRelativeTimeSpanString(
                    startMillis, endMillis, DateUtils.HOUR_IN_MILLIS,
                    DateUtils.FORMAT_ABBREV_ALL).toString();
    }

}
