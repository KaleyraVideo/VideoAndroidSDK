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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontFamily
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.kaleyra.video_common_ui.CompanyUI.Theme
import com.kaleyra.video_common_ui.KaleyraFontFamily
import com.kaleyra.video_common_ui.KaleyraM3FontFamily
import com.kaleyra.video_sdk.extensions.darkColorSchemeFrom
import com.kaleyra.video_sdk.extensions.lightColorSchemeFrom

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

internal val defaultTypography = androidx.compose.material3.Typography()
internal val typography = androidx.compose.material3.Typography(
    displayLarge = defaultTypography.displayLarge.copy(fontFamily = KaleyraM3FontFamily.default),
    displayMedium = defaultTypography.displayMedium.copy(fontFamily = KaleyraM3FontFamily.default),
    displaySmall = defaultTypography.displaySmall.copy(fontFamily = KaleyraM3FontFamily.default),

    headlineLarge = defaultTypography.headlineLarge.copy(fontFamily = KaleyraM3FontFamily.default),
    headlineMedium = defaultTypography.headlineMedium.copy(fontFamily = KaleyraM3FontFamily.default),
    headlineSmall = defaultTypography.headlineSmall.copy(fontFamily = KaleyraM3FontFamily.default),

    titleLarge = defaultTypography.titleLarge.copy(fontFamily = KaleyraM3FontFamily.default),
    titleMedium = defaultTypography.titleMedium.copy(fontFamily = KaleyraM3FontFamily.default),
    titleSmall = defaultTypography.titleSmall.copy(fontFamily = KaleyraM3FontFamily.default),

    bodyLarge = defaultTypography.bodyLarge.copy(fontFamily = KaleyraM3FontFamily.default),
    bodyMedium = defaultTypography.bodyMedium.copy(fontFamily = KaleyraM3FontFamily.default),
    bodySmall = defaultTypography.bodySmall.copy(fontFamily = KaleyraM3FontFamily.default),

    labelLarge = defaultTypography.labelLarge.copy(fontFamily = KaleyraM3FontFamily.default),
    labelMedium = defaultTypography.labelMedium.copy(fontFamily = KaleyraM3FontFamily.default),
    labelSmall = defaultTypography.labelSmall.copy(fontFamily = KaleyraM3FontFamily.default)
)

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

    val darkSeedColor = (darkColors as? Theme.Colors.Seed)?.color ?: kaleyra_m3_seed.toArgb()
    val lightSeedColor = (lightColors as? Theme.Colors.Seed)?.color ?: kaleyra_m3_seed.toArgb()

    val colors = when {
        isDarkTheme -> {
            with(Color(darkSeedColor)) {
                KaleyraDarkColorTheme.copy(secondary = this, onSecondary = onColorFor(this))
            }
        }
        else -> {
            with(Color(lightSeedColor)) {
                KaleyraLightColorTheme.copy(secondary = this, onSecondary = onColorFor(this))
            }
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

/**
 * Composable function to generate the Collaboration M3 Theme
 * @param theme Theme input theme
 * @param lightStatusBarIcons Boolean flag indicating if the system bars should be transparent, true if transparent, false otherwise
 * @param content [@androidx.compose.runtime.Composable] Function1<[@kotlin.ParameterName] Boolean, Unit> composable callback with isDarkTheme as input flag
 */
@Composable
fun CollaborationM3Theme(
    theme: Theme,
    lightStatusBarIcons: Boolean = false,
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
        isDarkTheme -> when(darkColors) {
            is Theme.Colors.Seed -> darkColorSchemeFrom(darkColors.color)
            else -> darkColorSchemeFrom(kaleyra_m3_seed.toArgb())

        }
        else ->  when(lightColors) {
            is Theme.Colors.Seed -> lightColorSchemeFrom(lightColors.color)
            else -> lightColorSchemeFrom(kaleyra_m3_seed.toArgb())

        }
    }
    val kaleyraColors = when {
        isDarkTheme -> darkKaleyraColors()
        else -> lightKaleyraColors()
    }

    SideEffect {
        systemUiController.setStatusBarColor(
            color = Color.Transparent,
            darkIcons = !lightStatusBarIcons && !isDarkTheme
        )
        systemUiController.setNavigationBarColor(
            color = Color.Transparent,
            darkIcons = !isDarkTheme,
            navigationBarContrastEnforced = false
        )
    }

    CompositionLocalProvider(value = LocalKaleyraColors provides kaleyraColors) {
        androidx.compose.material3.MaterialTheme(
            colorScheme = colors,
            typography = typography,
            content = { content(isDarkTheme) }
        )
    }
}


private fun onColorFor(color: Color) = if (color.luminance() > .5f) Color.Black else Color.White

@Immutable
internal data class KaleyraColors(val hangUp: Color = Color(0xFFE11900))

internal val LocalKaleyraColors = compositionLocalOf { KaleyraColors() }

internal fun lightKaleyraColors(
    hangUp: Color = Color(0xFFE11900)
) = KaleyraColors(hangUp = hangUp)

internal fun darkKaleyraColors(
    hangUp: Color = Color(0xFFAE1300)
) = KaleyraColors(hangUp = hangUp)

// TODO decide if this object will be used for a custom theme, or only some colors, so it's overkill
internal object KaleyraTheme {

    val colors: KaleyraColors
        @Composable
        @ReadOnlyComposable
        get() = LocalKaleyraColors.current
}

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

/**
 * Composable function to build the Kaleyra M3 Theme
 * @param content [@androidx.compose.runtime.Composable] Function0<Unit> composable callback called when the content should be rendered
 */
@Composable
fun KaleyraM3Theme(content: @Composable (isDarkTheme: Boolean) -> Unit) {
    val theme = Theme(
        day = Theme.Style(colors = Theme.Colors.Seed(color = kaleyra_m3_seed.toArgb())),
        night = Theme.Style(colors = Theme.Colors.Seed(color = kaleyra_m3_seed.toArgb()))
    )
    CollaborationM3Theme(theme = theme, content = content)
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
