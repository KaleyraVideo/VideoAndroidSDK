package com.kaleyra.video_common_ui.theme

import android.net.Uri
import androidx.compose.material3.Typography
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontFamily
import com.kaleyra.video_common_ui.theme.resource.ColorResource
import com.kaleyra.video_common_ui.theme.resource.URIResource
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Test

class ThemeTest {

    @Test
    fun testTypography() {
        val fontFamily = FontFamily.Serif
        val themeTypography = Theme.Typography(fontFamily)

        val typography = Typography().let {
            it.copy(
                displayLarge = it.displayLarge.copy(fontFamily = fontFamily),
                displayMedium = it.displayMedium.copy(fontFamily = fontFamily),
                displaySmall = it.displaySmall.copy(fontFamily = fontFamily),

                headlineLarge = it.headlineLarge.copy(fontFamily = fontFamily),
                headlineMedium = it.headlineMedium.copy(fontFamily = fontFamily),
                headlineSmall = it.headlineSmall.copy(fontFamily = fontFamily),

                titleLarge = it.titleLarge.copy(fontFamily = fontFamily),
                titleMedium = it.titleMedium.copy(fontFamily = fontFamily),
                titleSmall = it.titleSmall.copy(fontFamily = fontFamily),

                bodyLarge = it.bodyLarge.copy(fontFamily = fontFamily),
                bodyMedium = it.bodyMedium.copy(fontFamily = fontFamily),
                bodySmall = it.bodySmall.copy(fontFamily = fontFamily),

                labelLarge = it.labelLarge.copy(fontFamily = fontFamily),
                labelMedium = it.labelMedium.copy(fontFamily = fontFamily),
                labelSmall = it.labelSmall.copy(fontFamily = fontFamily)
            )
        }
        assertEquals(typography, themeTypography.typography)
    }

    @Test
    fun testLogo() {
        val uriResource = URIResource(mockk<Uri>(), mockk<Uri>())

        val logo = Theme.Logo(uriResource)
        assertEquals(uriResource, logo.resource)
        assertEquals(uriResource, logo.compact)
        assertEquals(uriResource, logo.extended)
    }

    @Test
    fun testSeedPalette() {
        val seed = ColorResource(Color.Red.toArgb(), Color.Blue.toArgb())
        val seedPalette = Theme.Palette(seed)

        val colorSchemePalette = Theme.Palette(
            ColorSchemeFactory.createLightColorScheme(seed.light),
            ColorSchemeFactory.createDarkColorScheme(seed.dark)
        )
        assertEquals(colorSchemePalette, seedPalette)
    }

    @Test
    fun testColorSchemePalette() {
        val lightColorScheme = ColorSchemeFactory.createLightColorScheme(Color.Red.toArgb())
        val darkColorScheme = ColorSchemeFactory.createLightColorScheme(Color.Blue.toArgb())
        val colorSchemePalette = Theme.Palette(lightColorScheme, darkColorScheme)

        val expected = Theme.Palette(
            primary = ColorResource(lightColorScheme.primary.toArgb(), darkColorScheme.primary.toArgb()),
            onPrimary = ColorResource(lightColorScheme.onPrimary.toArgb(), darkColorScheme.onPrimary.toArgb()),
            secondary = ColorResource(lightColorScheme.secondary.toArgb(), darkColorScheme.secondary.toArgb()),
            onSecondary = ColorResource(
                lightColorScheme.onSecondary.toArgb(),
                darkColorScheme.onSecondary.toArgb()
            ),
            secondaryContainer = ColorResource(
                lightColorScheme.secondaryContainer.toArgb(),
                darkColorScheme.secondaryContainer.toArgb()
            ),
            onSecondaryContainer = ColorResource(
                lightColorScheme.onSecondaryContainer.toArgb(),
                darkColorScheme.onSecondaryContainer.toArgb()
            ),
            surface = ColorResource(lightColorScheme.surface.toArgb(), darkColorScheme.surface.toArgb()),
            onSurface = ColorResource(lightColorScheme.onSurface.toArgb(), darkColorScheme.onSurface.toArgb()),
            surfaceVariant = ColorResource(
                lightColorScheme.surfaceVariant.toArgb(),
                darkColorScheme.surfaceVariant.toArgb()
            ),
            onSurfaceVariant = ColorResource(
                lightColorScheme.onSurfaceVariant.toArgb(),
                darkColorScheme.onSurfaceVariant.toArgb()
            ),
            surfaceTint = ColorResource(
                lightColorScheme.surfaceTint.toArgb(),
                darkColorScheme.surfaceTint.toArgb()
            ),
            inverseSurface = ColorResource(
                lightColorScheme.inverseSurface.toArgb(),
                darkColorScheme.inverseSurface.toArgb()
            ),
            inverseOnSurface = ColorResource(
                lightColorScheme.inverseOnSurface.toArgb(),
                darkColorScheme.inverseOnSurface.toArgb()
            ),
            error = ColorResource(lightColorScheme.error.toArgb(), darkColorScheme.error.toArgb()),
            onError = ColorResource(lightColorScheme.onError.toArgb(), darkColorScheme.onError.toArgb()),
            outline = ColorResource(lightColorScheme.outline.toArgb(), darkColorScheme.outline.toArgb()),
            outlineVariant = ColorResource(
                lightColorScheme.outlineVariant.toArgb(),
                darkColorScheme.outlineVariant.toArgb()
            ),
            surfaceContainer = ColorResource(
                lightColorScheme.surfaceContainer.toArgb(),
                darkColorScheme.surfaceContainer.toArgb()
            ),
            surfaceContainerHigh = ColorResource(
                lightColorScheme.surfaceContainerHigh.toArgb(),
                darkColorScheme.surfaceContainerHigh.toArgb()
            ),
            surfaceContainerHighest = ColorResource(
                lightColorScheme.surfaceContainerHighest.toArgb(),
                darkColorScheme.surfaceContainerHighest.toArgb()
            ),
            surfaceContainerLow = ColorResource(
                lightColorScheme.surfaceContainerLow.toArgb(),
                darkColorScheme.surfaceContainerLow.toArgb()
            ),
            surfaceContainerLowest = ColorResource(
                lightColorScheme.surfaceContainerLowest.toArgb(),
                darkColorScheme.surfaceContainerLowest.toArgb()
            )
        )
        assertEquals(expected, colorSchemePalette)
    }
}