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
import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.toArgb
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
    data class Theme(
        val fontFamily: FontFamily = KaleyraFontFamily.default,
        val defaultStyle: DefaultStyle = DefaultStyle.System,
        override val day: Style = Style(),
        override val night: Style = Style(),
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
             * Night style
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
        data class Style(override val logo: Uri? = null, override val colors: Colors? = null) :
            Company.Theme.Style

        /**
         * Colors representations
         */
        sealed class Colors : Company.Theme.Style.Colors {

            /**
             * Theme colors representation from seed color
             * @property color Int seed color used to generate a consistent theme based on this color
             *
             * @constructor
             */
            data class Seed(@ColorInt val color: Int) : Colors()

            /**
             * Theme colors representation using material colors
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
             * @property surfaceBright Int
             * @property surfaceContainer Int
             * @property surfaceContainerHigh Int
             * @property surfaceContainerHighest Int
             * @property surfaceContainerLow Int
             * @property surfaceContainerLowest Int
             * @property surfaceDim Int
             * @constructor
             */
            data class Palette(
                val primary: Int,
                val onPrimary: Int,
                val primaryContainer: Int,
                val onPrimaryContainer: Int,
                val inversePrimary: Int,
                val secondary: Int,
                val onSecondary: Int,
                val secondaryContainer: Int,
                val onSecondaryContainer: Int,
                val tertiary: Int,
                val onTertiary: Int,
                val tertiaryContainer: Int,
                val onTertiaryContainer: Int,
                val background: Int,
                val onBackground: Int,
                val surface: Int,
                val onSurface: Int,
                val surfaceVariant: Int,
                val onSurfaceVariant: Int,
                val surfaceTint: Int,
                val inverseSurface: Int,
                val inverseOnSurface: Int,
                val error: Int,
                val onError: Int,
                val errorContainer: Int,
                val onErrorContainer: Int,
                val outline: Int,
                val outlineVariant: Int,
                val scrim: Int,
                val surfaceBright: Int,
                val surfaceContainer: Int,
                val surfaceContainerHigh: Int,
                val surfaceContainerHighest: Int,
                val surfaceContainerLow: Int,
                val surfaceContainerLowest: Int,
                val surfaceDim: Int,
            ) : Colors() {
                constructor(colorScheme: ColorScheme) : this(
                    colorScheme.primary.toArgb(),
                    colorScheme.onPrimary.toArgb(),
                    colorScheme.primaryContainer.toArgb(),
                    colorScheme.onPrimaryContainer.toArgb(),
                    colorScheme.inversePrimary.toArgb(),
                    colorScheme.secondary.toArgb(),
                    colorScheme.onSecondary.toArgb(),
                    colorScheme.secondaryContainer.toArgb(),
                    colorScheme.onSecondaryContainer.toArgb(),
                    colorScheme.tertiary.toArgb(),
                    colorScheme.onTertiary.toArgb(),
                    colorScheme.tertiaryContainer.toArgb(),
                    colorScheme.onTertiaryContainer.toArgb(),
                    colorScheme.background.toArgb(),
                    colorScheme.onBackground.toArgb(),
                    colorScheme.surface.toArgb(),
                    colorScheme.onSurface.toArgb(),
                    colorScheme.surfaceVariant.toArgb(),
                    colorScheme.onSurfaceVariant.toArgb(),
                    colorScheme.surfaceTint.toArgb(),
                    colorScheme.inverseSurface.toArgb(),
                    colorScheme.inverseOnSurface.toArgb(),
                    colorScheme.error.toArgb(),
                    colorScheme.onError.toArgb(),
                    colorScheme.errorContainer.toArgb(),
                    colorScheme.onErrorContainer.toArgb(),
                    colorScheme.outline.toArgb(),
                    colorScheme.outlineVariant.toArgb(),
                    colorScheme.scrim.toArgb(),
                    colorScheme.surfaceBright.toArgb(),
                    colorScheme.surfaceContainer.toArgb(),
                    colorScheme.surfaceContainerHigh.toArgb(),
                    colorScheme.surfaceContainerHighest.toArgb(),
                    colorScheme.surfaceContainerLow.toArgb(),
                    colorScheme.surfaceContainerLowest.toArgb(),
                    colorScheme.surfaceDim.toArgb()
                )
            }
        }
    }
}

internal class NoOpCompany(
    override val name: SharedFlow<String> = MutableSharedFlow(),
    override val id: SharedFlow<String> = MutableSharedFlow(),
    override val theme: SharedFlow<Company.Theme> = MutableSharedFlow(),
) : Company
