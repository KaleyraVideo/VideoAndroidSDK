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

package com.kaleyra.video_sdk.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Typography
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontFamily
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.kaleyra.video_common_ui.CompanyUI.Theme
import com.kaleyra.video_common_ui.KaleyraFontFamily

private val KaleyraLightColorTheme = lightColors(
    primary = kaleyra_theme_light_primary,
    primaryVariant = kaleyra_theme_light_primaryVariant,
    onPrimary = kaleyra_theme_light_onPrimary,
    secondary = kaleyra_theme_light_secondary,
    secondaryVariant = kaleyra_theme_light_secondaryVariant,
    onSecondary = kaleyra_theme_light_onSecondary,
    error = kaleyra_theme_light_error,
    onError = kaleyra_theme_light_onError,
    background = kaleyra_theme_light_background,
    onBackground = kaleyra_theme_light_onBackground,
    surface = kaleyra_theme_light_surface,
    onSurface = kaleyra_theme_light_onSurface
)

private val KaleyraDarkColorTheme = darkColors(
    primary = kaleyra_theme_dark_primary,
    primaryVariant = kaleyra_theme_dark_primaryVariant,
    onPrimary = kaleyra_theme_dark_onPrimary,
    secondary = kaleyra_theme_dark_secondary,
    secondaryVariant = kaleyra_theme_dark_secondaryVariant,
    onSecondary = kaleyra_theme_dark_onSecondary,
    error = kaleyra_theme_dark_error,
    onError = kaleyra_theme_dark_onError,
    background = kaleyra_theme_dark_background,
    onBackground = kaleyra_theme_dark_onBackground,
    surface = kaleyra_theme_dark_surface,
    onSurface = kaleyra_theme_dark_onSurface
)

/**
//DE-COMMENT THIS WHEN THE MATERIAL 3 DEPENDENCY WILL BE ADDED
private val KaleyraM3LightColorTheme = lightColorScheme(
    primary = kaleyra_m3_theme_light_primary,
    onPrimary = kaleyra_m3_theme_light_onPrimary,
    primaryContainer = kaleyra_m3_theme_light_primaryContainer,
    onPrimaryContainer = kaleyra_m3_theme_light_onPrimaryContainer,
    secondary = kaleyra_m3_theme_light_secondary,
    onSecondary = kaleyra_m3_theme_light_onSecondary,
    secondaryContainer = kaleyra_m3_theme_light_secondaryContainer,
    onSecondaryContainer = kaleyra_m3_theme_light_onSecondaryContainer,
    tertiary = kaleyra_m3_theme_light_tertiary,
    onTertiary = kaleyra_m3_theme_light_onTertiary,
    tertiaryContainer = kaleyra_m3_theme_light_tertiaryContainer,
    onTertiaryContainer = kaleyra_m3_theme_light_onTertiaryContainer,
    error = kaleyra_m3_theme_light_error,
    errorContainer = kaleyra_m3_theme_light_errorContainer,
    onError = kaleyra_m3_theme_light_onError,
    onErrorContainer = kaleyra_m3_theme_light_onErrorContainer,
    background = kaleyra_m3_theme_light_background,
    onBackground = kaleyra_m3_theme_light_onBackground,
    surface = kaleyra_m3_theme_light_surface,
    onSurface = kaleyra_m3_theme_light_onSurface,
    surfaceVariant = kaleyra_m3_theme_light_surfaceVariant,
    onSurfaceVariant = kaleyra_m3_theme_light_onSurfaceVariant,
    outline = kaleyra_m3_theme_light_outline,
    inverseOnSurface = kaleyra_m3_theme_light_inverseOnSurface,
    inverseSurface = kaleyra_m3_theme_light_inverseSurface,
    inversePrimary = kaleyra_m3_theme_light_inversePrimary,
    surfaceTint = kaleyra_m3_theme_light_surfaceTint,
    outlineVariant = kaleyra_m3_theme_light_outlineVariant,
    scrim = kaleyra_m3_theme_light_scrim,
)


private val KaleyraM3DarkColorTheme = darkColorScheme(
    primary = kaleyra_m3_theme_dark_primary,
    onPrimary = kaleyra_m3_theme_dark_onPrimary,
    primaryContainer = kaleyra_m3_theme_dark_primaryContainer,
    onPrimaryContainer = kaleyra_m3_theme_dark_onPrimaryContainer,
    secondary = kaleyra_m3_theme_dark_secondary,
    onSecondary = kaleyra_m3_theme_dark_onSecondary,
    secondaryContainer = kaleyra_m3_theme_dark_secondaryContainer,
    onSecondaryContainer = kaleyra_m3_theme_dark_onSecondaryContainer,
    tertiary = kaleyra_m3_theme_dark_tertiary,
    onTertiary = kaleyra_m3_theme_dark_onTertiary,
    tertiaryContainer = kaleyra_m3_theme_dark_tertiaryContainer,
    onTertiaryContainer = kaleyra_m3_theme_dark_onTertiaryContainer,
    error = kaleyra_m3_theme_dark_error,
    errorContainer = kaleyra_m3_theme_dark_errorContainer,
    onError = kaleyra_m3_theme_dark_onError,
    onErrorContainer = kaleyra_m3_theme_dark_onErrorContainer,
    background = kaleyra_m3_theme_dark_background,
    onBackground = kaleyra_m3_theme_dark_onBackground,
    surface = kaleyra_m3_theme_dark_surface,
    onSurface = kaleyra_m3_theme_dark_onSurface,
    surfaceVariant = kaleyra_m3_theme_dark_surfaceVariant,
    onSurfaceVariant = kaleyra_m3_theme_dark_onSurfaceVariant,
    outline = kaleyra_m3_theme_dark_outline,
    inverseOnSurface = kaleyra_m3_theme_dark_inverseOnSurface,
    inverseSurface = kaleyra_m3_theme_dark_inverseSurface,
    inversePrimary = kaleyra_m3_theme_dark_inversePrimary,
    surfaceTint = kaleyra_m3_theme_dark_surfaceTint,
    outlineVariant = kaleyra_m3_theme_dark_outlineVariant,
    scrim = kaleyra_m3_theme_dark_scrim,
)
**/

/**
 * Composable function to generate the Collaboration Theme
 * @param theme Theme input theme
 * @param transparentSystemBars Boolean flag indicating if the system bars should be transparent, true if transparent, false otherwise
 * @param content [@androidx.compose.runtime.Composable] Function1<[@kotlin.ParameterName] Boolean, Unit> composable callback with isDarkTheme as input flag
 */
@Composable
fun CollaborationTheme(
    theme: Theme,
    transparentSystemBars: Boolean = false,
    content: @Composable (isDarkTheme: Boolean) -> Unit
) {
    val systemUiController = rememberSystemUiController()

    val lightColors = theme.day.colors
    val darkColors = theme.night.colors
    val isSystemDarkTheme = isSystemInDarkTheme()
    val isDarkTheme by remember(theme) {
        derivedStateOf {
            when (theme.defaultStyle) {
                Theme.DefaultStyle.Day -> false
                Theme.DefaultStyle.Night -> true
                else -> isSystemDarkTheme
            }
        }
    }

    val colors = when {
        isDarkTheme -> {
            if (darkColors != null) KaleyraDarkColorTheme.copy(secondary = darkColors.secondary, onSecondary = onColorFor(darkColors.secondary))
            else KaleyraDarkColorTheme
        }
        else -> {
            if (lightColors != null) KaleyraLightColorTheme.copy(secondary = lightColors.secondary, onSecondary = onColorFor(lightColors.secondary))
            else KaleyraLightColorTheme
        }
    }

    SideEffect {
        systemUiController.setStatusBarColor(
            color = if (transparentSystemBars) Color.Transparent else colors.primaryVariant,
            darkIcons = !transparentSystemBars && !isDarkTheme,
            transformColorForLightContent = { Color.Black }
        )
        systemUiController.setNavigationBarColor(
            color = if (transparentSystemBars) Color.Transparent else colors.primaryVariant,
            darkIcons = !isDarkTheme,
            navigationBarContrastEnforced = false,
            transformColorForLightContent = { Color.Black }
        )
    }

    MaterialTheme(
        colors = colors,
        typography = Typography(defaultFontFamily = theme.fontFamily),
        content = { content(isDarkTheme) }
    )
}

private fun onColorFor(color: Color) = if (color.luminance() > .5f) Color.Black else Color.White

/**
 * Composable function to build the Kaleyra Theme
 * @param isDarkTheme Boolean flag indicating if dark theme should be used, true if dark theme should be used, false otherwise
 * @param content [@androidx.compose.runtime.Composable] Function0<Unit> composable callback called when the content should be rendered
 */
@Composable
fun KaleyraTheme(
    isDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = when {
        isDarkTheme -> KaleyraDarkColorTheme
        else -> KaleyraLightColorTheme
    }

    MaterialTheme(
        colors = colors,
        typography = Typography(defaultFontFamily = KaleyraFontFamily.default),
        content = content
    )
}

private val TermsDarkColorTheme = darkColors(
    primary = Color.White,
    primaryVariant = Color.White,
    surface = Color(0xFF0E0E0E),
    onPrimary = Color.Black,
    onSurface = Color.White
)

private val TermsLightColorTheme = lightColors(
    primary = Color.Black,
    primaryVariant = Color.Black,
    surface = Color.White,
    onPrimary = Color.White,
    onSurface = Color.Black
)

/**
 * Composable function to build the Kaleyra Theme
 * @param fontFamily FontFamily font family to be used
 * @param isDarkTheme Boolean flag indicating if dark theme should be used, true if dark theme should be used, false otherwise
 * @param content [@androidx.compose.runtime.Composable] Function0<Unit> composable callback called when the content should be rendered
 */
@Composable
fun TermsAndConditionsTheme(
    fontFamily: FontFamily = KaleyraFontFamily.default,
    isDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = when {
        isDarkTheme -> TermsDarkColorTheme
        else -> TermsLightColorTheme
    }

    MaterialTheme(
        colors = colors,
        typography = Typography(defaultFontFamily = fontFamily),
        content = content
    )
}
