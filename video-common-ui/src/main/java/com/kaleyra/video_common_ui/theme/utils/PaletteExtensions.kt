package com.kaleyra.video_common_ui.theme.utils

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import com.kaleyra.video_common_ui.theme.ColorSchemeFactory
import com.kaleyra.video_common_ui.theme.Theme

/**
 * Extension functions for converting a [Theme.Palette] to a [ColorScheme].
 *
 * This object provides functions to transform a theme palette,into a color scheme.
 *
 */
object PaletteExtensions {

    /**
     * Converts a [Theme.Palette] to a light [ColorScheme].
     *
     * This function maps the colors from the palette to the corresponding roles
     * in a light color scheme.
     *
     * @receiver The [Theme.Palette] to convert.
     * @return A [ColorScheme] representing the light color scheme derived from the palette.
     */
    fun Theme.Palette.toLightColorScheme(): ColorScheme {
        val schemeMonochrome = ColorSchemeFactory.lightSchemeMonochrome
        return ColorScheme(
            primary = Color(primary.light),
            onPrimary = Color(onPrimary.light),
            primaryContainer = Color(schemeMonochrome.primaryContainer),
            onPrimaryContainer = Color(schemeMonochrome.onPrimaryContainer),
            inversePrimary = Color(schemeMonochrome.inversePrimary),
            secondary = Color(secondary.light),
            onSecondary = Color(onSecondary.light),
            secondaryContainer = Color(secondaryContainer.light),
            onSecondaryContainer = Color(onSecondaryContainer.light),
            tertiary = Color(schemeMonochrome.tertiary),
            onTertiary = Color(schemeMonochrome.onTertiary),
            tertiaryContainer = Color(schemeMonochrome.tertiaryContainer),
            onTertiaryContainer = Color(schemeMonochrome.onTertiaryContainer),
            background = Color(schemeMonochrome.background),onBackground = Color(schemeMonochrome.onBackground),
            surface = Color(surface.light),
            onSurface = Color(onSurface.light),
            surfaceVariant = Color(surfaceVariant.light),
            onSurfaceVariant = Color(onSurfaceVariant.light),
            surfaceTint = Color(surfaceTint.light),
            inverseSurface = Color(inverseSurface.light),
            inverseOnSurface = Color(inverseOnSurface.light),
            error = Color(error.light),
            onError = Color(onError.light),
            errorContainer = Color(schemeMonochrome.errorContainer),
            onErrorContainer = Color(schemeMonochrome.onErrorContainer),
            outline = Color(outline.light),
            outlineVariant = Color(outlineVariant.light),
            scrim = Color(schemeMonochrome.scrim),
            surfaceBright = Color(schemeMonochrome.surfaceBright),
            surfaceContainer = Color(surfaceContainer.light),
            surfaceContainerHigh = Color(surfaceContainerHigh.light),
            surfaceContainerHighest = Color(surfaceContainerHighest.light),
            surfaceContainerLow = Color(surfaceContainerLow.light),
            surfaceContainerLowest = Color(surfaceContainerLowest.light),surfaceDim = Color(schemeMonochrome.surfaceDim),
        )
    }

    /**
     * Converts a [Theme.Palette] to a dark [ColorScheme].
     *
     * This function maps the colors from the palette to the corresponding roles
     * in a dark color scheme.
     *
     * @receiver The [Theme.Palette] to convert.
     * @return A [ColorScheme] representing the dark color scheme derived from the palette.
     */
    fun Theme.Palette.toDarkColorScheme(): ColorScheme {
        val schemeMonochrome = ColorSchemeFactory.darkSchemeMonochrome
        return ColorScheme(
            primary = Color(primary.dark),
            onPrimary = Color(onPrimary.dark),
            primaryContainer = Color(schemeMonochrome.primaryContainer),
            onPrimaryContainer = Color(schemeMonochrome.onPrimaryContainer),
            inversePrimary = Color(schemeMonochrome.inversePrimary),
            secondary = Color(secondary.dark),
            onSecondary = Color(onSecondary.dark),
            secondaryContainer = Color(secondaryContainer.dark),
            onSecondaryContainer = Color(onSecondaryContainer.dark),
            tertiary = Color(schemeMonochrome.tertiary),
            onTertiary = Color(schemeMonochrome.onTertiary),
            tertiaryContainer = Color(schemeMonochrome.tertiaryContainer),
            onTertiaryContainer = Color(schemeMonochrome.onTertiaryContainer),
            background = Color(schemeMonochrome.background),
            onBackground = Color(schemeMonochrome.onBackground),
            surface = Color(surface.dark),
            onSurface = Color(onSurface.dark),
            surfaceVariant = Color(surfaceVariant.dark),
            onSurfaceVariant = Color(onSurfaceVariant.dark),
            surfaceTint = Color(surfaceTint.dark),
            inverseSurface = Color(inverseSurface.dark),
            inverseOnSurface = Color(inverseOnSurface.dark),
            error = Color(error.dark),
            onError = Color(onError.dark),
            errorContainer = Color(schemeMonochrome.errorContainer),
            onErrorContainer = Color(schemeMonochrome.onErrorContainer),
            outline = Color(outline.dark),
            outlineVariant = Color(outlineVariant.dark),
            scrim = Color(schemeMonochrome.scrim),
            surfaceBright = Color(schemeMonochrome.surfaceBright),
            surfaceContainer = Color(surfaceContainer.dark),
            surfaceContainerHigh = Color(surfaceContainerHigh.dark),
            surfaceContainerHighest = Color(surfaceContainerHighest.dark),
            surfaceContainerLow = Color(surfaceContainerLow.dark),
            surfaceContainerLowest = Color(surfaceContainerLowest.dark),
            surfaceDim = Color(schemeMonochrome.surfaceDim),
        )
    }
}