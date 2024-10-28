package com.kaleyra.video_common_ui.theme.factory

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.ColorUtils
import com.kaleyra.material_color_utilities.dynamiccolor.DynamicColor
import com.kaleyra.material_color_utilities.hct.Hct
import com.kaleyra.material_color_utilities.scheme.SchemeFidelity
import com.kaleyra.material_color_utilities.scheme.SchemeMonochrome

/**
 * Factory for creating [ColorScheme] instances for light and dark themes.
 *
 * This object provides methods for generating color schemes based on a seed color
 * and using either a light or dark monochrome scheme as a base.
 *
 * It utilizes the Material Design 3 color system principles and the HCT color space
 * to create harmonious and accessible color palettes.
 */
internal object ColorSchemeFactory {

    /**
     * A light monochrome color scheme.
     */
    val lightSchemeMonochrome by lazy {
        createSchemeMonochrome(Color.White.toArgb())
    }

    /**
     * A dark monochrome color scheme.
     */
    val darkSchemeMonochrome by lazy {
        createSchemeMonochrome(Color.White.toArgb(), isDark = true)
    }

    /**
     * Creates a light color scheme based on the provided seed color.
     *
     * @param seed The seed color in ARGB format.
     * @return A [ColorScheme] instance for light theme.
     */
    fun createLightColorScheme(seed: Int): ColorScheme {
        val fidelityScheme = createSchemeFidelity(seed)
        return createColorScheme(seed, fidelityScheme, lightSchemeMonochrome)
    }

    /**
     * Creates a dark color scheme based on the provided seed color.
     *
     * @param seed The seed color in ARGB format.
     * @return A [ColorScheme] instance for dark theme.
     */
    fun createDarkColorScheme(seed: Int): ColorScheme {
        val fidelityScheme = createSchemeFidelity(seed, isDark = true)
        return createColorScheme(seed, fidelityScheme, darkSchemeMonochrome)
    }

    private fun createColorScheme(
        seed: Int,
        fidelityScheme: SchemeFidelity,
        monochromeScheme: SchemeMonochrome,
    ): ColorScheme {
        val fidelitySeed = ColorUtils.setAlphaComponent(seed, 255)
        val hctSeed = Hct.fromInt(fidelitySeed)
        val onFidelitySeed = Hct.from(hctSeed.hue, hctSeed.chroma, DynamicColor.foregroundTone(hctSeed.tone, 7.0)).toInt()
        return ColorScheme(
            primary = Color(fidelitySeed),
            onPrimary = Color(onFidelitySeed),
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
    }

    private fun createSchemeMonochrome(seed: Int, isDark: Boolean = false): SchemeMonochrome {
        return SchemeMonochrome(Hct.fromInt(seed), isDark, .0)
    }

    private fun createSchemeFidelity(seed: Int, isDark: Boolean = false): SchemeFidelity {
        return SchemeFidelity(Hct.fromInt(seed), isDark, .0)
    }
}