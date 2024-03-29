/*
 * Copyright 2023 Kaleyra @ https://www.kaleyra.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kaleyra.app_utilities.utils

import android.content.Context
import android.os.Build
import android.util.DisplayMetrics

object Utils {
    fun dpToPx(context: Context, dp: Float): Int {
        return (dp * (context.resources.displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)).toInt()
    }

    @JvmStatic
    fun capitalize(s: String): String {
        return if (s.isEmpty()) s else s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase()
    }

    fun isGoogleGlassDevice(): Boolean = Build.DEVICE == "glass_v3"
}