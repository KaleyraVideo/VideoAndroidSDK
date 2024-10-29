package com.kaleyra.video_common_ui.material

import androidx.compose.ui.graphics.toArgb
import com.kaleyra.material_color_utilities.scheme.Scheme
import com.kaleyra.video_common_ui.utils.SchemeUtils.toColorScheme
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

    private fun getRandomColor() = android.graphics.Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256))
}
