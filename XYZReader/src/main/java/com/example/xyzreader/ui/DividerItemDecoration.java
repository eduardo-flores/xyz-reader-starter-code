/*
 * Copyright (C) 2014 The Android Open Source Project
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
package com.example.xyzreader.ui;

import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;

public class DividerItemDecoration {
    public static int getHorizontalSpanCount(Context context) {
        if (context == null) return 2;

        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        int spanSize = 400;
        int widthScreen = displayMetrics.widthPixels;
        int nColumns = widthScreen / spanSize;
        if (nColumns < 2) return 2;
        return nColumns;
    }
}