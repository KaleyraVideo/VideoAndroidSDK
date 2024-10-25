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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.kaleyra.video_common_ui.theme.Theme
import com.kaleyra.video_common_ui.theme.utils.PaletteExtensions.toDarkColorScheme
import com.kaleyra.video_common_ui.theme.utils.PaletteExtensions.toLightColorScheme

internal val KaleyraPaletteSeed = Color(0xFF2A638A).toArgb()

/**
 * Composable function to generate the Collaboration M3 Theme
 * @param theme Theme input theme
 * @param lightStatusBarIcons Boolean flag indicating if the system bars should be transparent, true if transparent, false otherwise
 * @param content [@androidx.compose.runtime.Composable] Function1<[@kotlin.ParameterName] Boolean, Unit> composable callback with isDarkTheme as input flag
 */
@Composable
fun CollaborationTheme(
    theme: Theme,
    lightStatusBarIcons: Boolean = false,
    content: @Composable (isDarkTheme: Boolean) -> Unit
) {
    val systemUiController = rememberSystemUiController()
    val isSystemDarkTheme = isSystemInDarkTheme()

    val isDarkTheme = remember(theme) {
        when (theme.config.style) {
            Theme.Config.Style.Light -> false
            Theme.Config.Style.Dark -> true
            else -> isSystemDarkTheme
        }
    }

    val palette = theme.palette ?: Theme.Palette(seed = KaleyraPaletteSeed)
    val colorScheme = remember(theme) {
        when {
            isDarkTheme -> palette.toDarkColorScheme()
            else -> palette.toLightColorScheme()
        }
    }

    val typography = (theme.typography ?: kaleyraTypography).typography

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
        MaterialTheme(
            colorScheme = colorScheme,
            typography = typography,
            content = { content(isDarkTheme) }
        )
    }
}

@Immutable
internal data class KaleyraColors(
    val warning: Color = Color(0xFFFFD02B),
    val onWarning: Color = Color.White,
    val negativeContainer: Color = Color(0xFFE11900),
    val onNegativeContainer: Color = Color.White,
    val positiveContainer: Color = Color(0xFF1A7924),
    val onPositiveContainer: Color = Color.White
)

internal val LocalKaleyraColors = compositionLocalOf { KaleyraColors() }

internal fun lightKaleyraColors(
    warning: Color = Color(0xFFFFD02B),
    onWarning: Color = Color.White,
    negativeContainer: Color = Color(0xFFE11900),
    onNegativeContainer: Color = Color.White,
    positiveContainer: Color = Color(0xFF1A7924),
    onPositiveContainer: Color = Color.White
) = KaleyraColors(
    warning = warning,
    onWarning = onWarning,
    negativeContainer = negativeContainer,
    onNegativeContainer = onNegativeContainer,
    positiveContainer = positiveContainer,
    onPositiveContainer = onPositiveContainer
)

internal fun darkKaleyraColors(
    warning: Color = Color(0xFFFFD02B),
    onWarning: Color = Color.White,
    negativeContainer: Color = Color(0xFFAE1300),
    onNegativeContainer: Color = Color.White,
    positiveContainer: Color = Color(0xFF1A7924),
    onPositiveContainer: Color = Color.White
) = KaleyraColors(
    warning = warning,
    onWarning = onWarning,
    negativeContainer = negativeContainer,
    onNegativeContainer = onNegativeContainer,
    positiveContainer = positiveContainer,
    onPositiveContainer = onPositiveContainer
)

internal object KaleyraTheme {

    val colors: KaleyraColors
        @Composable
        @ReadOnlyComposable
        get() = LocalKaleyraColors.current
}

/**
 * Composable function to build the Kaleyra M3 Theme
 * @param content [@androidx.compose.runtime.Composable] Function0<Unit> composable callback called when the content should be rendered
 */
@Composable
fun KaleyraTheme(content: @Composable (isDarkTheme: Boolean) -> Unit) {
    CollaborationTheme(theme = Theme(), content = content)
}

private val TermsDarkColorScheme = darkColorScheme(
    primary = Color.White,
    surface = Color(0xFF0E0E0E),
    onPrimary = Color.Black,
    onSurface = Color.White
)

private val TermsLightColorScheme = lightColorScheme(
    primary = Color.Black,
    surface = Color.White,
    onPrimary = Color.White,
    onSurface = Color.Black
)

/**
 * Composable function to build the Kaleyra Theme
 * @param isDarkTheme Boolean flag indicating if dark theme should be used, true if dark theme should be used, false otherwise
 * @param content [@androidx.compose.runtime.Composable] Function0<Unit> composable callback called when the content should be rendered
 */
@Composable
fun TermsAndConditionsTheme(
    isDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        isDarkTheme -> TermsDarkColorScheme
        else -> TermsLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(),
        content = content
    )
}
