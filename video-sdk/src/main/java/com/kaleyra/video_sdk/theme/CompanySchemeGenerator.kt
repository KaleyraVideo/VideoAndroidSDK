package com.kaleyra.video_sdk.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.kaleyra.material_color_utilities.dynamiccolor.DynamicScheme
import com.kaleyra.material_color_utilities.hct.Hct
import com.kaleyra.material_color_utilities.scheme.SchemeFidelity
import com.kaleyra.material_color_utilities.scheme.SchemeMonochrome
import com.kaleyra.video_common_ui.CompanyUI

@Immutable
internal data class CompanyScheme(val scheme: ColorScheme, val isDark: Boolean)

internal class CompanySchemeGenerator {

    fun getCompanyScheme(theme: CompanyUI.Theme, isSystemDarkTheme: Boolean): CompanyScheme {
        val isDarkThemeEnabled = isDarkThemeEnabled(theme, isSystemDarkTheme)
        return createCompanyScheme(theme, isDarkThemeEnabled)
    }

    private fun createCompanyScheme(theme: CompanyUI.Theme, isDarkTheme: Boolean): CompanyScheme {
        val colors = if (isDarkTheme) theme.night.colors else theme.day.colors
        return when (colors) {
            is CompanyUI.Theme.Colors.Palette -> {
                CompanyScheme(
                    isDark = isDarkTheme,
                    scheme = ColorScheme(
                        primary = Color(colors.primary),
                        onPrimary = Color(colors.onPrimary),
                        primaryContainer = Color(colors.primaryContainer),
                        onPrimaryContainer = Color(colors.onPrimaryContainer),
                        inversePrimary = Color(colors.inversePrimary),
                        secondary = Color(colors.secondary),
                        onSecondary = Color(colors.onSecondary),
                        secondaryContainer = Color(colors.secondaryContainer),
                        onSecondaryContainer = Color(colors.onSecondaryContainer),
                        tertiary = Color(colors.tertiary),
                        onTertiary = Color(colors.onTertiary),
                        tertiaryContainer = Color(colors.tertiaryContainer),
                        onTertiaryContainer = Color(colors.onTertiaryContainer),
                        background = Color(colors.background),
                        onBackground = Color(colors.onBackground),
                        surface = Color(colors.surface),
                        onSurface = Color(colors.onSurface),
                        surfaceVariant = Color(colors.surfaceVariant),
                        onSurfaceVariant = Color(colors.onSurfaceVariant),
                        surfaceTint = Color(colors.surfaceTint),
                        inverseSurface = Color(colors.inverseSurface),
                        inverseOnSurface = Color(colors.inverseOnSurface),
                        error = Color(colors.error),
                        onError = Color(colors.onError),
                        errorContainer = Color(colors.errorContainer),
                        onErrorContainer = Color(colors.onErrorContainer),
                        outline = Color(colors.outline),
                        outlineVariant = Color(colors.outlineVariant),
                        scrim = Color(colors.scrim),
                        surfaceBright = Color(colors.surfaceBright),
                        surfaceContainer = Color(colors.surfaceContainer),
                        surfaceContainerHigh = Color(colors.surfaceContainerHigh),
                        surfaceContainerHighest = Color(colors.surfaceContainerHighest),
                        surfaceContainerLow = Color(colors.surfaceContainerLow),
                        surfaceContainerLowest = Color(colors.surfaceContainerLowest),
                        surfaceDim = Color(colors.surfaceDim),
                    )
                )
            }
            else -> {
                val seed = (colors as? CompanyUI.Theme.Colors.Seed)?.color
                val monochromeScheme = createSchemeMonochrome(isDarkTheme)
                val fidelityScheme = seed?.let { createSchemeFidelity(it, isDarkTheme) }
                CompanyScheme(
                    isDark = isDarkTheme,
                    scheme = ColorScheme(
                        primary = Color(seed ?: monochromeScheme.primary),
                        onPrimary = Color(fidelityScheme?.onPrimaryContainer ?: monochromeScheme.onPrimary),
                        primaryContainer = Color(fidelityScheme?.primaryContainer ?: monochromeScheme.primaryContainer),
                        onPrimaryContainer = Color(fidelityScheme?.onPrimaryContainer ?: monochromeScheme.onPrimaryContainer),
                        inversePrimary = Color(fidelityScheme?.inversePrimary ?: monochromeScheme.inversePrimary),
                        secondary = Color(fidelityScheme?.secondary ?: monochromeScheme.secondary),
                        onSecondary = Color(fidelityScheme?.onSecondary ?: monochromeScheme.onSecondary),
                        secondaryContainer = Color(fidelityScheme?.secondaryContainer ?: monochromeScheme.secondaryContainer),
                        onSecondaryContainer = Color(fidelityScheme?.onSecondaryContainer ?: monochromeScheme.onSecondaryContainer),
                        tertiary = Color(fidelityScheme?.tertiary ?: monochromeScheme.tertiary),
                        onTertiary = Color(fidelityScheme?.onTertiary ?: monochromeScheme.onTertiary),
                        tertiaryContainer = Color(fidelityScheme?.tertiaryContainer ?: monochromeScheme.tertiaryContainer),
                        onTertiaryContainer = Color(fidelityScheme?.onTertiaryContainer ?: monochromeScheme.onTertiaryContainer),
                        background = Color(monochromeScheme.background),
                        onBackground = Color(monochromeScheme.onBackground),
                        surface = Color(monochromeScheme.surface),
                        onSurface = Color(monochromeScheme.onSurface),
                        surfaceVariant = Color(monochromeScheme.surfaceVariant),
                        onSurfaceVariant = Color(monochromeScheme.onSurfaceVariant),
                        surfaceTint = Color(monochromeScheme.surfaceTint),
                        inverseSurface = Color(monochromeScheme.inverseSurface),
                        inverseOnSurface = Color(monochromeScheme.inverseOnSurface),
                        error = Color(fidelityScheme?.error ?: monochromeScheme.error),
                        onError = Color(fidelityScheme?.onError ?: monochromeScheme.onError),
                        errorContainer = Color(fidelityScheme?.errorContainer ?: monochromeScheme.errorContainer),
                        onErrorContainer = Color(fidelityScheme?.onErrorContainer ?: monochromeScheme.onErrorContainer),
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
                )
            }
        }
    }

    private fun isDarkThemeEnabled(theme: CompanyUI.Theme, isSystemDarkTheme: Boolean): Boolean {
        return when (theme.defaultStyle) {
            CompanyUI.Theme.DefaultStyle.Day -> false
            CompanyUI.Theme.DefaultStyle.Night -> true
            else -> isSystemDarkTheme
        }
    }

    private fun createSchemeMonochrome(isDarkTheme: Boolean): DynamicScheme {
        return SchemeMonochrome(Hct.fromInt(Color.White.toArgb()), isDarkTheme, .0)
    }

    private fun createSchemeFidelity(seed: Int, isDarkTheme: Boolean): DynamicScheme {
        return SchemeFidelity(Hct.fromInt(seed), isDarkTheme, .0)
    }
}