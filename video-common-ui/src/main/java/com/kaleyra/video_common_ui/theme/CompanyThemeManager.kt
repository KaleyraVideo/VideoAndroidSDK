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

package com.kaleyra.video_common_ui.theme

import android.net.Uri
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.kaleyra.video.Company
import com.kaleyra.video_common_ui.CompanyUI
import com.kaleyra.video_common_ui.KaleyraVideo
import com.kaleyra.video_common_ui.theme.resource.ColorResource
import com.kaleyra.video_common_ui.theme.resource.URIResource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.mapNotNull

/**
 * Utility functions for the Company Theme
 */
object CompanyThemeManager {

    /**
     * Mapper function to convert Company to a flow containing its CompanyUI.Theme representation
     */
    val Company.combinedTheme: Flow<Theme>
        get() = theme.mapToUI(uiTheme = KaleyraVideo.theme)

    private fun Flow<Company.Theme>.mapToUI(uiTheme: KaleyraVideo.Theme?): Flow<Theme> =
        flow {
            uiTheme?.let { emit(mapToCombinedTheme(it)) }
            mapNotNull { theme ->
                val lightLogo = theme.day.logo ?: Uri.EMPTY
                val darkLogo = theme.night.logo ?: Uri.EMPTY
                val uriResource = URIResource(lightLogo, darkLogo)

                val lightSeed = (theme.day.colors as? Company.Theme.Style.Colors.Seed)?.color ?: Color.Black.toArgb()
                val darkSeed = (theme.night.colors as? Company.Theme.Style.Colors.Seed)?.color ?: Color.White.toArgb()
                val colorResource = ColorResource(lightSeed, darkSeed)

                mapToCombinedTheme(uiTheme, uriResource, colorResource)
            }.collect(this)
        }

    private fun mapToCombinedTheme(
        uiTheme: KaleyraVideo.Theme?,
        remoteURIResource: URIResource = URIResource(Uri.EMPTY, Uri.EMPTY),
        remoteColorResource: ColorResource? = null
    ): Theme {
        return when (uiTheme) {
            is CompanyUI.Theme -> {
                val lightLogo = uiTheme.day.logo ?: remoteURIResource.light
                val darkLogo = uiTheme.night.logo ?: remoteURIResource.dark
                val logo = Theme.Logo(URIResource(lightLogo, darkLogo))

                val lightSeed = (uiTheme.day.colors as? CompanyUI.Theme.Colors.Seed)?.color ?: remoteColorResource?.light ?: Color.Black.toArgb()
                val darkSeed = (uiTheme.night.colors as? CompanyUI.Theme.Colors.Seed)?.color ?: remoteColorResource?.dark ?: Color.White.toArgb()
                val palette = Theme.Palette(ColorResource(lightSeed, darkSeed))

                val typography = Theme.Typography(uiTheme.fontFamily)
                val config = Theme.Config(
                    style = when (uiTheme.defaultStyle) {
                        CompanyUI.Theme.DefaultStyle.System -> Theme.Config.Style.System
                        CompanyUI.Theme.DefaultStyle.Day -> Theme.Config.Style.Light
                        CompanyUI.Theme.DefaultStyle.Night -> Theme.Config.Style.Dark
                    }
                )

                Theme(logo, palette, typography, config)
            }

            is Theme -> {
                Theme(
                    logo = uiTheme.logo ?: Theme.Logo(remoteURIResource),
                    palette = uiTheme.palette ?: remoteColorResource?.let { Theme.Palette(it) } ?: Theme.Palette.monochrome(),
                    typography = uiTheme.typography,
                    config = uiTheme.config
                )
            }

            else -> Theme(Theme.Logo(remoteURIResource), remoteColorResource?.let { Theme.Palette(it) } ?: Theme.Palette.monochrome())
        }
    }
}
