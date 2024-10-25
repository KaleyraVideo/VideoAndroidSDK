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
import androidx.annotation.ColorInt
import androidx.compose.ui.text.font.FontFamily
import com.kaleyra.video.Company
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn

/**
 * UI representation of a Company
 * @constructor
 */
@Deprecated("This class is deprecated. It will be removed in a future release")
class CompanyUI(company: Company, coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO)) :
    Company by company {

    override val theme: SharedFlow<Theme> = company.theme.map {
        Theme(
            day = Theme.Style(
                logo = it.day.logo,
                colors = it.day.colors.takeIf { colors -> colors is Company.Theme.Style.Colors.Seed }
                    ?.let { colors ->
                        Theme.Colors.Seed((colors as Company.Theme.Style.Colors.Seed).color)
                    }
            ),
            night = Theme.Style(
                logo = it.night.logo,
                colors = it.night.colors.takeIf { colors -> colors is Company.Theme.Style.Colors.Seed }
                    ?.let { colors ->
                        Theme.Colors.Seed((colors as Company.Theme.Style.Colors.Seed).color)
                    }
            ),
        )
    }.shareIn(coroutineScope, SharingStarted.Eagerly, 1)

    /**
     * Company associated theme
     * @property fontFamily FontFamily the font family to be used on the UI layer
     * @property defaultStyle DefaultStyle the default style (Day/Night/System) to be applied on the UI layer
     * @property day Style Day mode style
     * @property night Style Night mode style
     * @constructor
     */
    @Deprecated(
        message = "This class is deprecated. It will be removed in a future release. Use com.kaleyra.video_common_ui.theme.Theme instead.",
        replaceWith = ReplaceWith("com.kaleyra.video_common_ui.theme.Theme")
    )
    data class Theme(
        val fontFamily: FontFamily = KaleyraFontFamily.default,
        val defaultStyle: DefaultStyle = DefaultStyle.System,
        override val day: Style = Style(),
        override val night: Style = Style(),
    ) : Company.Theme, KaleyraVideo.Theme {

        /**
         * Default Style representation
         */
        @Deprecated("This class is deprecated. It will be removed in a future release")
        sealed class DefaultStyle {

            /**
             * Day style
             */
            @Deprecated("This class is deprecated. It will be removed in a future release")
            data object Day : DefaultStyle()

            /**
             * Night style
             */
            @Deprecated("This class is deprecated. It will be removed in a future release")
            data object Night : DefaultStyle()

            /**
             * Style based on operating system selected style
             */
            @Deprecated("This class is deprecated. It will be removed in a future release")
            data object System : DefaultStyle()
        }

        /**
         * Style representation applied to a day/night/system style
         * @property logo Uri? optional uri of the company logo
         * @property colors Colors? optional colors of the company
         * @constructor
         */
        @Deprecated("This class is deprecated. It will be removed in a future release")
        data class Style(override val logo: Uri? = null, override val colors: Colors? = null) :
            Company.Theme.Style

        /**
         * Colors representations
         */
        @Deprecated("This class is deprecated. It will be removed in a future release")
        sealed class Colors : Company.Theme.Style.Colors {

            /**
             * Theme colors representation from seed color
             * @property color Int seed color used to generate a consistent theme based on this color
             *
             * @constructor
             */
            @Deprecated("This class is deprecated. It will be removed in a future release")
            data class Seed(@ColorInt val color: Int) : Colors()
        }
    }
}

internal class NoOpCompany(
    override val name: SharedFlow<String> = MutableSharedFlow(),
    override val id: SharedFlow<String> = MutableSharedFlow(),
    override val theme: SharedFlow<Company.Theme> = MutableSharedFlow(),
) : Company
