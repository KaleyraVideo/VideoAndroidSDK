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

package com.kaleyra.video_common_ui

import android.annotation.TargetApi
import android.graphics.Color as AndroidColor
import android.net.Uri
import androidx.annotation.ColorInt
import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red
import com.kaleyra.video.Company
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

/**
 * UI representation of a Company
 * @constructor
 */
class CompanyUI(company: Company) : Company by company {

    /**
     * Company associated theme
     * @property fontFamily FontFamily the font family to be used on the UI layer
     * @property defaultStyle DefaultStyle the default style (Day/Night/System) to be applied on the UI layer
     * @property day Style Day mode style
     * @property night Style Night mode style
     * @constructor
     */
    data class Theme(
        val fontFamily: FontFamily = KaleyraFontFamily.default,
        val defaultStyle: DefaultStyle = DefaultStyle.System,
        override val day: Style = Style(),
        override val night: Style = Style()
    ) : Company.Theme {

        /**
         * Default Style representation
         */
        sealed class DefaultStyle {

            /**
             * Day style
             */
            data object Day : DefaultStyle()

            /**
             * Night syle
             */
            data object Night : DefaultStyle()

            /**
             * Style based on operating system selected style
             */
            data object System : DefaultStyle()
        }

        /**
         * Style representation applied to a day/night/system style
         * @property logo Uri? optional uri of the company logo
         * @property colors Colors? optional colors of the company
         * @constructor
         */
        data class Style(override val logo: Uri? = null, val colors: Colors? = null) : Company.Theme.Style

        /**
         * Colors representations
         */
        sealed class Colors {

            /**
             * Theme colors representation from seed color
             * @property seed Int seed color used to generate a consistent theme based on this color
             *
             * @constructor
             */
            data class Seed(@ColorInt val color: Int) : Colors()


            /**
             * Material 3 color scheme representation
             *
             * @property primary Int
             * @property onPrimary Int
             * @property primaryContainer Int
             * @property onPrimaryContainer Int
             * @property inversePrimary Int
             * @property secondary Int
             * @property onSecondary Int
             * @property secondaryContainer Int
             * @property onSecondaryContainer Int
             * @property tertiary Int
             * @property onTertiary Int
             * @property tertiaryContainer Int
             * @property onTertiaryContainer Int
             * @property background Int
             * @property onBackground Int
             * @property surface Int
             * @property onSurface Int
             * @property surfaceVariant Int
             * @property onSurfaceVariant Int
             * @property surfaceTint Int
             * @property inverseSurface Int
             * @property inverseOnSurface Int
             * @property error Int
             * @property onError Int
             * @property errorContainer Int
             * @property onErrorContainer Int
             * @property outline Int
             * @property outlineVariant Int
             * @property scrim Int
             * @constructor
             */
            data class Scheme(
                @ColorInt val primary: Int,
                @ColorInt val onPrimary: Int,
                @ColorInt val primaryContainer: Int,
                @ColorInt val onPrimaryContainer: Int,
                @ColorInt val inversePrimary: Int,
                @ColorInt val secondary: Int,
                @ColorInt val onSecondary: Int,
                @ColorInt val secondaryContainer: Int,
                @ColorInt val onSecondaryContainer: Int,
                @ColorInt val tertiary: Int,
                @ColorInt val onTertiary: Int,
                @ColorInt val tertiaryContainer: Int,
                @ColorInt val onTertiaryContainer: Int,
                @ColorInt val background: Int,
                @ColorInt val onBackground: Int,
                @ColorInt val surface: Int,
                @ColorInt val onSurface: Int,
                @ColorInt val surfaceVariant: Int,
                @ColorInt val onSurfaceVariant: Int,
                @ColorInt val surfaceTint: Int,
                @ColorInt val inverseSurface: Int,
                @ColorInt val inverseOnSurface: Int,
                @ColorInt val error: Int,
                @ColorInt val onError: Int,
                @ColorInt val errorContainer: Int,
                @ColorInt val onErrorContainer: Int,
                @ColorInt val outline: Int,
                @ColorInt val outlineVariant: Int,
                @ColorInt val scrim: Int,
            ) : Colors()
        }
    }
}

internal class NoOpCompany(
    override val name: SharedFlow<String> = MutableSharedFlow(),
    override val id: SharedFlow<String> = MutableSharedFlow(),
    override val theme: SharedFlow<Company.Theme> = MutableSharedFlow()
) : Company
