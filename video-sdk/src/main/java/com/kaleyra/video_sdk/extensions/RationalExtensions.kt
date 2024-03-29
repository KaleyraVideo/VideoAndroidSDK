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

package com.kaleyra.video_sdk.extensions

import android.util.Rational

internal object RationalExtensions {

    private val MIN_PIP_RATIONAL = Rational(9, 18)

    private val MAX_PIP_RATIONAL = Rational(18, 9)

    fun Rational.coerceRationalForPip() = coerceIn(MIN_PIP_RATIONAL, MAX_PIP_RATIONAL)
}
