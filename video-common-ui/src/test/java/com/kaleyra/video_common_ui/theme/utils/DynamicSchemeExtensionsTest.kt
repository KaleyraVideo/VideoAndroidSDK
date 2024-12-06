package com.kaleyra.video_common_ui.theme.utils

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.kaleyra.material_color_utilities.hct.Hct
import com.kaleyra.material_color_utilities.scheme.SchemeMonochrome
import com.kaleyra.video_common_ui.theme.utils.DynamicSchemeExtensions.toColorScheme
import junit.framework.TestCase.assertEquals
import org.junit.Test

class DynamicSchemeExtensionsTest {

    @Test
    fun testToColorScheme() {
        val dynamicScheme = SchemeMonochrome(Hct.fromInt(Color.White.toArgb()), false, .0)
        val colorScheme = dynamicScheme.toColorScheme()

        val expected = ColorScheme(
            primary = Color(dynamicScheme.primary),
            onPrimary = Color(dynamicScheme.onPrimary),
            primaryContainer = Color(dynamicScheme.primaryContainer),
            onPrimaryContainer = Color(dynamicScheme.onPrimaryContainer),
            inversePrimary = Color(dynamicScheme.inversePrimary),
            secondary = Color(dynamicScheme.secondary),
            onSecondary = Color(dynamicScheme.onSecondary),
            secondaryContainer = Color(dynamicScheme.secondaryContainer),
            onSecondaryContainer = Color(dynamicScheme.onSecondaryContainer),
            tertiary = Color(dynamicScheme.tertiary),
            onTertiary = Color(dynamicScheme.onTertiary),
            tertiaryContainer = Color(dynamicScheme.tertiaryContainer),
            onTertiaryContainer = Color(dynamicScheme.onTertiaryContainer),
            background = Color(dynamicScheme.background),
            onBackground = Color(dynamicScheme.onBackground),
            surface = Color(dynamicScheme.surface),
            onSurface = Color(dynamicScheme.onSurface),
            surfaceVariant = Color(dynamicScheme.surfaceVariant),
            onSurfaceVariant = Color(dynamicScheme.onSurfaceVariant),
            surfaceTint = Color(dynamicScheme.surfaceTint),
            inverseSurface = Color(dynamicScheme.inverseSurface),
            inverseOnSurface = Color(dynamicScheme.inverseOnSurface),
            error = Color(dynamicScheme.error),
            onError = Color(dynamicScheme.onError),
            errorContainer = Color(dynamicScheme.errorContainer),
            onErrorContainer = Color(dynamicScheme.onErrorContainer),
            outline = Color(dynamicScheme.outline),
            outlineVariant = Color(dynamicScheme.outlineVariant),
            scrim = Color(dynamicScheme.scrim),
            surfaceBright = Color(dynamicScheme.surfaceBright),
            surfaceContainer = Color(dynamicScheme.surfaceContainer),
            surfaceContainerHigh = Color(dynamicScheme.surfaceContainerHigh),
            surfaceContainerHighest = Color(dynamicScheme.surfaceContainerHighest),
            surfaceContainerLow = Color(dynamicScheme.surfaceContainerLow),
            surfaceContainerLowest = Color(dynamicScheme.surfaceContainerLowest),
            surfaceDim = Color(dynamicScheme.surfaceDim),
        )
        assertEquals(expected.toString(), colorScheme.toString())
    }
}