package com.kaleyra.video_common_ui.theme.utils

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.kaleyra.material_color_utilities.hct.Hct
import com.kaleyra.material_color_utilities.scheme.SchemeMonochrome
import com.kaleyra.video_common_ui.theme.Theme
import com.kaleyra.video_common_ui.theme.resource.ColorResource
import com.kaleyra.video_common_ui.theme.utils.PaletteExtensions.toDarkColorScheme
import com.kaleyra.video_common_ui.theme.utils.PaletteExtensions.toLightColorScheme
import org.junit.Assert.assertEquals
import org.junit.Test

class PaletteExtensionsTest {

    @Test
    fun testToLightColorScheme() {
        val seed = Color.Red.toArgb()
        val palette = Theme.Palette(ColorResource(seed))
        val schemeMonochrome = SchemeMonochrome(Hct.fromInt(seed), false, .0)

        val result = palette.toLightColorScheme()
        val expected = ColorScheme(
            primary = Color(palette.primary.light),
            onPrimary = Color(palette.onPrimary.light),
            primaryContainer = Color(schemeMonochrome.primaryContainer),
            onPrimaryContainer = Color(schemeMonochrome.onPrimaryContainer),
            inversePrimary = Color(schemeMonochrome.inversePrimary),
            secondary = Color(palette.secondary.light),
            onSecondary = Color(palette.onSecondary.light),
            secondaryContainer = Color(palette.secondaryContainer.light),
            onSecondaryContainer = Color(palette.onSecondaryContainer.light),
            tertiary = Color(schemeMonochrome.tertiary),
            onTertiary = Color(schemeMonochrome.onTertiary),
            tertiaryContainer = Color(schemeMonochrome.tertiaryContainer),
            onTertiaryContainer = Color(schemeMonochrome.onTertiaryContainer),
            background = Color(schemeMonochrome.background),
            onBackground = Color(schemeMonochrome.onBackground),
            surface = Color(palette.surface.light),
            onSurface = Color(palette.onSurface.light),
            surfaceVariant = Color(palette.surfaceVariant.light),
            onSurfaceVariant = Color(palette.onSurfaceVariant.light),
            surfaceTint = Color(palette.surfaceTint.light),
            inverseSurface = Color(palette.inverseSurface.light),
            inverseOnSurface = Color(palette.inverseOnSurface.light),
            error = Color(palette.error.light),
            onError = Color(palette.onError.light),
            errorContainer = Color(schemeMonochrome.errorContainer),
            onErrorContainer = Color(schemeMonochrome.onErrorContainer),
            outline = Color(palette.outline.light),
            outlineVariant = Color(palette.outlineVariant.light),
            scrim = Color(schemeMonochrome.scrim),
            surfaceBright = Color(schemeMonochrome.surfaceBright),
            surfaceContainer = Color(palette.surfaceContainer.light),
            surfaceContainerHigh = Color(palette.surfaceContainerHigh.light),
            surfaceContainerHighest = Color(palette.surfaceContainerHighest.light),
            surfaceContainerLow = Color(palette.surfaceContainerLow.light),
            surfaceContainerLowest = Color(palette.surfaceContainerLowest.light),
            surfaceDim = Color(schemeMonochrome.surfaceDim),
        )
        assertEquals(expected.toString(), result.toString())
    }

    @Test
    fun testToDarkColorScheme() {
        val seed = Color.Red.toArgb()
        val palette = Theme.Palette(ColorResource(seed))
        val schemeMonochrome = SchemeMonochrome(Hct.fromInt(Color.White.toArgb()), true, .0)

        val result = palette.toDarkColorScheme()
        val expected = ColorScheme(
            primary = Color(palette.primary.dark),
            onPrimary = Color(palette.onPrimary.dark),
            primaryContainer = Color(schemeMonochrome.primaryContainer),
            onPrimaryContainer = Color(schemeMonochrome.onPrimaryContainer),
            inversePrimary = Color(schemeMonochrome.inversePrimary),
            secondary = Color(palette.secondary.dark),
            onSecondary = Color(palette.onSecondary.dark),
            secondaryContainer = Color(palette.secondaryContainer.dark),
            onSecondaryContainer = Color(palette.onSecondaryContainer.dark),
            tertiary = Color(schemeMonochrome.tertiary),
            onTertiary = Color(schemeMonochrome.onTertiary),
            tertiaryContainer = Color(schemeMonochrome.tertiaryContainer),
            onTertiaryContainer = Color(schemeMonochrome.onTertiaryContainer),
            background = Color(schemeMonochrome.background),
            onBackground = Color(schemeMonochrome.onBackground),
            surface = Color(palette.surface.dark),
            onSurface = Color(palette.onSurface.dark),
            surfaceVariant = Color(palette.surfaceVariant.dark),
            onSurfaceVariant = Color(palette.onSurfaceVariant.dark),
            surfaceTint = Color(palette.surfaceTint.dark),
            inverseSurface = Color(palette.inverseSurface.dark),
            inverseOnSurface = Color(palette.inverseOnSurface.dark),
            error = Color(palette.error.dark),
            onError = Color(palette.onError.dark),
            errorContainer = Color(schemeMonochrome.errorContainer),
            onErrorContainer = Color(schemeMonochrome.onErrorContainer),
            outline = Color(palette.outline.dark),
            outlineVariant = Color(palette.outlineVariant.dark),
            scrim = Color(schemeMonochrome.scrim),
            surfaceBright = Color(schemeMonochrome.surfaceBright),
            surfaceContainer = Color(palette.surfaceContainer.dark),
            surfaceContainerHigh = Color(palette.surfaceContainerHigh.dark),
            surfaceContainerHighest = Color(palette.surfaceContainerHighest.dark),
            surfaceContainerLow = Color(palette.surfaceContainerLow.dark),
            surfaceContainerLowest = Color(palette.surfaceContainerLowest.dark),
            surfaceDim = Color(schemeMonochrome.surfaceDim),
        )
        assertEquals(expected.toString(), result.toString())
    }
}