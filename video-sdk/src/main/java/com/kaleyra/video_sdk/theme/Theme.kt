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
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
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
import androidx.compose.ui.graphics.toArgb
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.kaleyra.material_color_utilities.hct.Hct
import com.kaleyra.material_color_utilities.scheme.SchemeFidelity
import com.kaleyra.material_color_utilities.scheme.SchemeMonochrome
import com.kaleyra.video.Company
import com.kaleyra.video_common_ui.CompanyUI.Theme

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

    val seed = if (isDarkTheme) {
        when(darkColors) {
            is Company.Theme.Style.Colors.Seed -> darkColors.color
            else -> kaleyra_m3_seed.toArgb()
        }
    } else {
        when(lightColors) {
            is Company.Theme.Style.Colors.Seed -> lightColors.color
            else -> kaleyra_m3_seed.toArgb()
        }
    }

    val kaleyraColors = when {
        isDarkTheme -> darkKaleyraColors()
        else -> lightKaleyraColors()
    }

    val monochromeScheme = SchemeMonochrome(Hct.fromInt(seed), isDarkTheme, .0)
    val fidelityScheme = SchemeFidelity(Hct.fromInt(seed), isDarkTheme, .0)

    val scheme = ColorScheme(
        primary = Color(fidelityScheme.primary),
        onPrimary = Color(fidelityScheme.onPrimary),
        primaryContainer = Color(fidelityScheme.primaryContainer),
        onPrimaryContainer = Color(fidelityScheme.onPrimaryContainer),
        inversePrimary = Color(fidelityScheme.inversePrimary),
        secondary = Color(fidelityScheme.secondary),
        onSecondary = Color(fidelityScheme.onSecondary),
        secondaryContainer = Color(fidelityScheme.secondaryContainer),
        onSecondaryContainer = Color(fidelityScheme.onSecondaryContainer),
        tertiary = Color(fidelityScheme.tertiary),
        onTertiary = Color(fidelityScheme.onTertiary),
        tertiaryContainer = Color(fidelityScheme.tertiaryContainer),
        onTertiaryContainer = Color(fidelityScheme.onTertiaryContainer),
        background = Color(monochromeScheme.background),
        onBackground = Color(monochromeScheme.onBackground),
        surface = Color(monochromeScheme.surface),
        onSurface = Color(monochromeScheme.onSurface),
        surfaceVariant = Color(monochromeScheme.surfaceVariant),
        onSurfaceVariant = Color(monochromeScheme.onSurfaceVariant),
        surfaceTint = Color(monochromeScheme.surfaceTint),
        inverseSurface = Color(monochromeScheme.inverseSurface),
        inverseOnSurface = Color(monochromeScheme.inverseOnSurface),
        error = Color(fidelityScheme.error),
        onError = Color(fidelityScheme.onError),
        errorContainer = Color(fidelityScheme.errorContainer),
        onErrorContainer = Color(fidelityScheme.onErrorContainer),
        outline = Color(monochromeScheme.outline),
        outlineVariant = Color(monochromeScheme.outlineVariant),
        scrim = Color(monochromeScheme.scrim),
        surfaceBright = Color(monochromeScheme.surfaceBright),
        surfaceContainer = Color(monochromeScheme.surfaceContainer),
        surfaceContainerHigh = Color(monochromeScheme.surfaceContainerHigh),
        surfaceContainerHighest = Color(monochromeScheme.surfaceContainerHighest),
        surfaceContainerLow = Color(monochromeScheme.surfaceContainerLow),
        surfaceContainerLowest = Color(monochromeScheme.surfaceContainerLowest),
        surfaceDim = Color(monochromeScheme.surfaceDim),
    )

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
            colorScheme = scheme,
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
    hangUp: Color = Color(0xFFE11900),
    onHangUp: Color = Color.White,
    answer: Color = Color(0xFF1A7924),
    onAnswer: Color = Color.White
) = KaleyraColors(
    warning = warning,
    onWarning = onWarning,
    negativeContainer = hangUp,
    onNegativeContainer = onHangUp,
    positiveContainer = answer,
    onPositiveContainer = onAnswer
)

internal fun darkKaleyraColors(
    warning: Color = Color(0xFFFFD02B),
    onWarning: Color = Color.White,
    hangUp: Color = Color(0xFFAE1300),
    onHangUp: Color = Color.White,
    answer: Color = Color(0xFF1A7924),
    onAnswer: Color = Color.White
) = KaleyraColors(
    warning = warning,
    onWarning = onWarning,
    negativeContainer = hangUp,
    onNegativeContainer = onHangUp,
    positiveContainer = answer,
    onPositiveContainer = onAnswer
)

// TODO decide if this object will be used for a custom theme, or only some colors, so it's overkill
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
    val theme = Theme(
        day = Theme.Style(colors = Company.Theme.Style.Colors.Seed(color = kaleyra_m3_seed.toArgb())),
        night = Theme.Style(colors = Company.Theme.Style.Colors.Seed(color = kaleyra_m3_seed.toArgb()))
    )
    CollaborationTheme(theme = theme, content = content)
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
        typography = typography,
        content = content
    )
}
