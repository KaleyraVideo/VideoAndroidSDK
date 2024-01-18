package com.kaleyra.video_common_ui.material

import androidx.compose.ui.graphics.toArgb
import com.kaleyra.video_common_ui.CompanyUI
import com.kaleyra.video_common_ui.material.ColorSchemeUtils.toM3ColorScheme
import com.kaleyra.video_common_ui.material.colorutils.scheme.Scheme
import com.kaleyra.video_common_ui.material.colorutils.scheme.SchemeUtils.toColorScheme
import org.junit.Assert
import org.junit.Test
import java.util.Random

class SchemeUtilsTests {

    private val rnd: Random = Random()

    @Test
    fun testSchemeToColorSchemeMapping() {

        val scheme = Scheme().apply {
            primary = getRandomColor()
            onPrimary = getRandomColor()
            primaryContainer = getRandomColor()
            onPrimaryContainer = getRandomColor()
            secondary = getRandomColor()
            onSecondary = getRandomColor()
            secondaryContainer = getRandomColor()
            onSecondaryContainer = getRandomColor()
            tertiary = getRandomColor()
            onTertiary = getRandomColor()
            tertiaryContainer = getRandomColor()
            onTertiaryContainer = getRandomColor()
            error = getRandomColor()
            onError = getRandomColor()
            errorContainer = getRandomColor()
            onErrorContainer = getRandomColor()
            background = getRandomColor()
            onBackground = getRandomColor()
            surface = getRandomColor()
            onSurface = getRandomColor()
            surfaceVariant = getRandomColor()
            onSurfaceVariant = getRandomColor()
            outline = getRandomColor()
            outlineVariant = getRandomColor()
            shadow = getRandomColor()
            scrim = getRandomColor()
            inverseSurface = getRandomColor()
            inverseOnSurface = getRandomColor()
            inversePrimary = getRandomColor()
        }

        val colorScheme = scheme.toColorScheme()

        Assert.assertEquals(scheme.primary, colorScheme.primary.toArgb())
        Assert.assertEquals(scheme.onPrimary, colorScheme.onPrimary.toArgb())
        Assert.assertEquals(scheme.primaryContainer, colorScheme.primaryContainer.toArgb())
        Assert.assertEquals(scheme.onPrimaryContainer, colorScheme.onPrimaryContainer.toArgb())
        Assert.assertEquals(scheme.secondary, colorScheme.secondary.toArgb())
        Assert.assertEquals(scheme.onSecondary, colorScheme.onSecondary.toArgb())
        Assert.assertEquals(scheme.secondaryContainer, colorScheme.secondaryContainer.toArgb())
        Assert.assertEquals(scheme.onSecondaryContainer, colorScheme.onSecondaryContainer.toArgb())
        Assert.assertEquals(scheme.tertiary, colorScheme.tertiary.toArgb())
        Assert.assertEquals(scheme.tertiaryContainer, colorScheme.tertiaryContainer.toArgb())
        Assert.assertEquals(scheme.onTertiaryContainer, colorScheme.onTertiaryContainer.toArgb())
        Assert.assertEquals(scheme.error, colorScheme.error.toArgb())
        Assert.assertEquals(scheme.onError, colorScheme.onError.toArgb())
        Assert.assertEquals(scheme.errorContainer, colorScheme.errorContainer.toArgb())
        Assert.assertEquals(scheme.background, colorScheme.background.toArgb())
        Assert.assertEquals(scheme.onBackground, colorScheme.onBackground.toArgb())
        Assert.assertEquals(scheme.surface, colorScheme.surface.toArgb())
        Assert.assertEquals(scheme.onSurface, colorScheme.onSurface.toArgb())
        Assert.assertEquals(scheme.surfaceVariant, colorScheme.surfaceVariant.toArgb())
        Assert.assertEquals(scheme.onSurfaceVariant, colorScheme.onSurfaceVariant.toArgb())
        Assert.assertEquals(scheme.outline, colorScheme.outline.toArgb())
        Assert.assertEquals(scheme.outlineVariant, colorScheme.outlineVariant.toArgb())
        Assert.assertEquals(scheme.scrim, colorScheme.scrim.toArgb())
        Assert.assertEquals(scheme.inverseSurface, colorScheme.inverseSurface.toArgb())
        Assert.assertEquals(scheme.inverseOnSurface, colorScheme.inverseOnSurface.toArgb())
        Assert.assertEquals(scheme.inversePrimary, colorScheme.inversePrimary.toArgb())
    }

    @Test
    fun testKaleyraThemeColorSchemeToColorSchemeMapping() {
        val kaleyraThemeColorScheme = CompanyUI.Theme.Colors.Scheme(
            primary = getRandomColor(),
            onPrimary = getRandomColor(),
            primaryContainer = getRandomColor(),
            onPrimaryContainer = getRandomColor(),
            inversePrimary = getRandomColor(),
            secondary = getRandomColor(),
            onSecondary = getRandomColor(),
            secondaryContainer = getRandomColor(),
            onSecondaryContainer = getRandomColor(),
            tertiary = getRandomColor(),
            onTertiary = getRandomColor(),
            tertiaryContainer = getRandomColor(),
            onTertiaryContainer = getRandomColor(),
            background = getRandomColor(),
            onBackground = getRandomColor(),
            surface = getRandomColor(),
            onSurface = getRandomColor(),
            surfaceVariant = getRandomColor(),
            onSurfaceVariant = getRandomColor(),
            surfaceTint = getRandomColor(),
            inverseSurface = getRandomColor(),
            inverseOnSurface = getRandomColor(),
            error = getRandomColor(),
            onError = getRandomColor(),
            errorContainer = getRandomColor(),
            onErrorContainer = getRandomColor(),
            outline = getRandomColor(),
            outlineVariant = getRandomColor(),
            scrim = getRandomColor(),
        )

        val material3ColorScheme = kaleyraThemeColorScheme.toM3ColorScheme()

        Assert.assertEquals(kaleyraThemeColorScheme.primary, material3ColorScheme.primary.toArgb())
        Assert.assertEquals(kaleyraThemeColorScheme.onPrimary, material3ColorScheme.onPrimary.toArgb())
        Assert.assertEquals(kaleyraThemeColorScheme.primaryContainer, material3ColorScheme.primaryContainer.toArgb())
        Assert.assertEquals(kaleyraThemeColorScheme.onPrimaryContainer, material3ColorScheme.onPrimaryContainer.toArgb())
        Assert.assertEquals(kaleyraThemeColorScheme.secondary, material3ColorScheme.secondary.toArgb())
        Assert.assertEquals(kaleyraThemeColorScheme.onSecondary, material3ColorScheme.onSecondary.toArgb())
        Assert.assertEquals(kaleyraThemeColorScheme.secondaryContainer, material3ColorScheme.secondaryContainer.toArgb())
        Assert.assertEquals(kaleyraThemeColorScheme.onSecondaryContainer, material3ColorScheme.onSecondaryContainer.toArgb())
        Assert.assertEquals(kaleyraThemeColorScheme.tertiary, material3ColorScheme.tertiary.toArgb())
        Assert.assertEquals(kaleyraThemeColorScheme.tertiaryContainer, material3ColorScheme.tertiaryContainer.toArgb())
        Assert.assertEquals(kaleyraThemeColorScheme.onTertiaryContainer, material3ColorScheme.onTertiaryContainer.toArgb())
        Assert.assertEquals(kaleyraThemeColorScheme.error, material3ColorScheme.error.toArgb())
        Assert.assertEquals(kaleyraThemeColorScheme.onError, material3ColorScheme.onError.toArgb())
        Assert.assertEquals(kaleyraThemeColorScheme.errorContainer, material3ColorScheme.errorContainer.toArgb())
        Assert.assertEquals(kaleyraThemeColorScheme.background, material3ColorScheme.background.toArgb())
        Assert.assertEquals(kaleyraThemeColorScheme.onBackground, material3ColorScheme.onBackground.toArgb())
        Assert.assertEquals(kaleyraThemeColorScheme.surface, material3ColorScheme.surface.toArgb())
        Assert.assertEquals(kaleyraThemeColorScheme.onSurface, material3ColorScheme.onSurface.toArgb())
        Assert.assertEquals(kaleyraThemeColorScheme.surfaceVariant, material3ColorScheme.surfaceVariant.toArgb())
        Assert.assertEquals(kaleyraThemeColorScheme.onSurfaceVariant, material3ColorScheme.onSurfaceVariant.toArgb())
        Assert.assertEquals(kaleyraThemeColorScheme.outline, material3ColorScheme.outline.toArgb())
        Assert.assertEquals(kaleyraThemeColorScheme.outlineVariant, material3ColorScheme.outlineVariant.toArgb())
        Assert.assertEquals(kaleyraThemeColorScheme.scrim, material3ColorScheme.scrim.toArgb())
        Assert.assertEquals(kaleyraThemeColorScheme.inverseSurface, material3ColorScheme.inverseSurface.toArgb())
        Assert.assertEquals(kaleyraThemeColorScheme.inverseOnSurface, material3ColorScheme.inverseOnSurface.toArgb())
        Assert.assertEquals(kaleyraThemeColorScheme.inversePrimary, material3ColorScheme.inversePrimary.toArgb())
    }

    private fun getRandomColor() = android.graphics.Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256))
}
