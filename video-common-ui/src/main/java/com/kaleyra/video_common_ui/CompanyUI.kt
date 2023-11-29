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

import android.net.Uri
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import com.kaleyra.video.Company
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

/**
 * UI representation of a Company
 * @constructor
 */
class CompanyUI(company: Company): Company by company {

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
            data object Day: DefaultStyle()

            /**
             * Night syle
             */
            data object Night: DefaultStyle()

            /**
             * Style based on operating system selected style
             */
            data object System: DefaultStyle()
        }

        /**
         * Style representation applied to a day/night/system style
         * @property logo Uri? optional uri of the company logo
         * @property colors Colors? optional colors of the company
         * @constructor
         */
        data class Style(override val logo: Uri? = null, val colors: Colors? = null) : Company.Theme.Style

        /**
         * Colors representation
         * @property secondary Color secondary color
         * @constructor
         */
        data class Colors(val secondary: Color)
    }
}

internal class NoOpCompany(
    override val name: SharedFlow<String> = MutableSharedFlow(),
    override val id: SharedFlow<String> = MutableSharedFlow(),
    override val theme: SharedFlow<Company.Theme> = MutableSharedFlow()
) : Company
