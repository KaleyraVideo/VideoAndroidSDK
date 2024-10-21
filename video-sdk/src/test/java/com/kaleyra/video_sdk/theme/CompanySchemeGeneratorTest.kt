package com.kaleyra.video_sdk.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.kaleyra.material_color_utilities.hct.Hct
import com.kaleyra.material_color_utilities.scheme.SchemeFidelity
import com.kaleyra.material_color_utilities.scheme.SchemeMonochrome
import com.kaleyra.video_common_ui.CompanyUI
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Test

class CompanySchemeGeneratorTest {

    @Test
    fun noSeedColor_generateLightSchemeMonochrome() {
        val theme = CompanyUI.Theme(
            defaultStyle = CompanyUI.Theme.DefaultStyle.Day,
            day = CompanyUI.Theme.Style(
                colors = mockk<CompanyUI.Theme.Colors>()
            )
        )
        val monochromeScheme = SchemeMonochrome(Hct.fromInt(Color.White.toArgb()), false, .0)

        val generator = CompanySchemeGenerator()
        val companyScheme = generator.getColorScheme(theme, false)

        assertEquals(false, companyScheme.isDark)
        assertEquals(Color(monochromeScheme.primary), companyScheme.scheme.primary)
        assertEquals(Color(monochromeScheme.onPrimary), companyScheme.scheme.onPrimary)
        assertEquals(Color(monochromeScheme.primaryContainer), companyScheme.scheme.primaryContainer)
        assertEquals(Color(monochromeScheme.onPrimaryContainer), companyScheme.scheme.onPrimaryContainer)
        assertEquals(Color(monochromeScheme.inversePrimary), companyScheme.scheme.inversePrimary)
        assertEquals(Color(monochromeScheme.secondary), companyScheme.scheme.secondary)
        assertEquals(Color(monochromeScheme.onSecondary), companyScheme.scheme.onSecondary)
        assertEquals(Color(monochromeScheme.secondaryContainer), companyScheme.scheme.secondaryContainer)
        assertEquals(Color(monochromeScheme.onSecondaryContainer), companyScheme.scheme.onSecondaryContainer)
        assertEquals(Color(monochromeScheme.tertiary), companyScheme.scheme.tertiary)
        assertEquals(Color(monochromeScheme.onTertiary), companyScheme.scheme.onTertiary)
        assertEquals(Color(monochromeScheme.tertiaryContainer), companyScheme.scheme.tertiaryContainer)
        assertEquals(Color(monochromeScheme.onTertiaryContainer), companyScheme.scheme.onTertiaryContainer)
        assertEquals(Color(monochromeScheme.background), companyScheme.scheme.background)
        assertEquals(Color(monochromeScheme.onBackground), companyScheme.scheme.onBackground)
        assertEquals(Color(monochromeScheme.onBackground), companyScheme.scheme.onBackground)
        assertEquals(Color(monochromeScheme.surface), companyScheme.scheme.surface)
        assertEquals(Color(monochromeScheme.onSurface), companyScheme.scheme.onSurface)
        assertEquals(Color(monochromeScheme.surfaceVariant), companyScheme.scheme.surfaceVariant)
        assertEquals(Color(monochromeScheme.onSurfaceVariant), companyScheme.scheme.onSurfaceVariant)
        assertEquals(Color(monochromeScheme.surfaceTint), companyScheme.scheme.surfaceTint)
        assertEquals(Color(monochromeScheme.inverseSurface), companyScheme.scheme.inverseSurface)
        assertEquals(Color(monochromeScheme.inverseOnSurface), companyScheme.scheme.inverseOnSurface)
        assertEquals(Color(monochromeScheme.error), companyScheme.scheme.error)
        assertEquals(Color(monochromeScheme.onError), companyScheme.scheme.onError)
        assertEquals(Color(monochromeScheme.errorContainer), companyScheme.scheme.errorContainer)
        assertEquals(Color(monochromeScheme.onErrorContainer), companyScheme.scheme.onErrorContainer)
        assertEquals(Color(monochromeScheme.outline), companyScheme.scheme.outline)
        assertEquals(Color(monochromeScheme.outlineVariant), companyScheme.scheme.outlineVariant)
        assertEquals(Color(monochromeScheme.scrim), companyScheme.scheme.scrim)
        assertEquals(Color(monochromeScheme.surfaceBright), companyScheme.scheme.surfaceBright)
        assertEquals(Color(monochromeScheme.surfaceContainer), companyScheme.scheme.surfaceContainer)
        assertEquals(Color(monochromeScheme.surfaceContainerHigh), companyScheme.scheme.surfaceContainerHigh)
        assertEquals(Color(monochromeScheme.surfaceContainerHighest), companyScheme.scheme.surfaceContainerHighest)
        assertEquals(Color(monochromeScheme.surfaceContainerLow), companyScheme.scheme.surfaceContainerLow)
        assertEquals(Color(monochromeScheme.surfaceContainerLowest), companyScheme.scheme.surfaceContainerLowest)
        assertEquals(Color(monochromeScheme.surfaceDim), companyScheme.scheme.surfaceDim)
    }

    @Test
    fun noSeedColor_generateDarkSchemeMonochrome() {
        val theme = CompanyUI.Theme(
            defaultStyle = CompanyUI.Theme.DefaultStyle.Night,
            night = CompanyUI.Theme.Style(
                colors = mockk<CompanyUI.Theme.Colors>()
            )
        )
        val monochromeScheme = SchemeMonochrome(Hct.fromInt(Color.White.toArgb()), true, .0)

        val generator = CompanySchemeGenerator()
        val companyScheme = generator.getColorScheme(theme, false)

        assertEquals(true, companyScheme.isDark)
        assertEquals(Color(monochromeScheme.primary), companyScheme.scheme.primary)
        assertEquals(Color(monochromeScheme.onPrimary), companyScheme.scheme.onPrimary)
        assertEquals(Color(monochromeScheme.primaryContainer), companyScheme.scheme.primaryContainer)
        assertEquals(Color(monochromeScheme.onPrimaryContainer), companyScheme.scheme.onPrimaryContainer)
        assertEquals(Color(monochromeScheme.inversePrimary), companyScheme.scheme.inversePrimary)
        assertEquals(Color(monochromeScheme.secondary), companyScheme.scheme.secondary)
        assertEquals(Color(monochromeScheme.onSecondary), companyScheme.scheme.onSecondary)
        assertEquals(Color(monochromeScheme.secondaryContainer), companyScheme.scheme.secondaryContainer)
        assertEquals(Color(monochromeScheme.onSecondaryContainer), companyScheme.scheme.onSecondaryContainer)
        assertEquals(Color(monochromeScheme.tertiary), companyScheme.scheme.tertiary)
        assertEquals(Color(monochromeScheme.onTertiary), companyScheme.scheme.onTertiary)
        assertEquals(Color(monochromeScheme.tertiaryContainer), companyScheme.scheme.tertiaryContainer)
        assertEquals(Color(monochromeScheme.onTertiaryContainer), companyScheme.scheme.onTertiaryContainer)
        assertEquals(Color(monochromeScheme.background), companyScheme.scheme.background)
        assertEquals(Color(monochromeScheme.onBackground), companyScheme.scheme.onBackground)
        assertEquals(Color(monochromeScheme.onBackground), companyScheme.scheme.onBackground)
        assertEquals(Color(monochromeScheme.surface), companyScheme.scheme.surface)
        assertEquals(Color(monochromeScheme.onSurface), companyScheme.scheme.onSurface)
        assertEquals(Color(monochromeScheme.surfaceVariant), companyScheme.scheme.surfaceVariant)
        assertEquals(Color(monochromeScheme.onSurfaceVariant), companyScheme.scheme.onSurfaceVariant)
        assertEquals(Color(monochromeScheme.surfaceTint), companyScheme.scheme.surfaceTint)
        assertEquals(Color(monochromeScheme.inverseSurface), companyScheme.scheme.inverseSurface)
        assertEquals(Color(monochromeScheme.inverseOnSurface), companyScheme.scheme.inverseOnSurface)
        assertEquals(Color(monochromeScheme.error), companyScheme.scheme.error)
        assertEquals(Color(monochromeScheme.onError), companyScheme.scheme.onError)
        assertEquals(Color(monochromeScheme.errorContainer), companyScheme.scheme.errorContainer)
        assertEquals(Color(monochromeScheme.onErrorContainer), companyScheme.scheme.onErrorContainer)
        assertEquals(Color(monochromeScheme.outline), companyScheme.scheme.outline)
        assertEquals(Color(monochromeScheme.outlineVariant), companyScheme.scheme.outlineVariant)
        assertEquals(Color(monochromeScheme.scrim), companyScheme.scheme.scrim)
        assertEquals(Color(monochromeScheme.surfaceBright), companyScheme.scheme.surfaceBright)
        assertEquals(Color(monochromeScheme.surfaceContainer), companyScheme.scheme.surfaceContainer)
        assertEquals(Color(monochromeScheme.surfaceContainerHigh), companyScheme.scheme.surfaceContainerHigh)
        assertEquals(Color(monochromeScheme.surfaceContainerHighest), companyScheme.scheme.surfaceContainerHighest)
        assertEquals(Color(monochromeScheme.surfaceContainerLow), companyScheme.scheme.surfaceContainerLow)
        assertEquals(Color(monochromeScheme.surfaceContainerLowest), companyScheme.scheme.surfaceContainerLowest)
        assertEquals(Color(monochromeScheme.surfaceDim), companyScheme.scheme.surfaceDim)
    }

    @Test
    fun seedColorWithDayDefaultStyle_generateLightSchemeFidelity() {
        val seed = Color.Red
        val theme = CompanyUI.Theme(
            defaultStyle = CompanyUI.Theme.DefaultStyle.Day,
            day = CompanyUI.Theme.Style(
                colors = CompanyUI.Theme.Colors.Seed(seed.toArgb())
            )
        )
        val fidelityScheme = SchemeFidelity(Hct.fromInt(seed.toArgb()), false, .0)
        val monochromeScheme = SchemeMonochrome(Hct.fromInt(seed.toArgb()), false, .0)

        val generator = CompanySchemeGenerator()
        val companyScheme = generator.getColorScheme(theme, false)

        assertEquals(false, companyScheme.isDark)
        assertEquals(seed, companyScheme.scheme.primary)
        assertEquals(Color(fidelityScheme.onPrimaryContainer), companyScheme.scheme.onPrimary)
        assertEquals(Color(fidelityScheme.primaryContainer), companyScheme.scheme.primaryContainer)
        assertEquals(Color(fidelityScheme.onPrimaryContainer), companyScheme.scheme.onPrimaryContainer)
        assertEquals(Color(fidelityScheme.inversePrimary), companyScheme.scheme.inversePrimary)
        assertEquals(Color(fidelityScheme.secondary), companyScheme.scheme.secondary)
        assertEquals(Color(fidelityScheme.onSecondary), companyScheme.scheme.onSecondary)
        assertEquals(Color(fidelityScheme.secondaryContainer), companyScheme.scheme.secondaryContainer)
        assertEquals(Color(fidelityScheme.onSecondaryContainer), companyScheme.scheme.onSecondaryContainer)
        assertEquals(Color(fidelityScheme.tertiary), companyScheme.scheme.tertiary)
        assertEquals(Color(fidelityScheme.onTertiary), companyScheme.scheme.onTertiary)
        assertEquals(Color(fidelityScheme.tertiaryContainer), companyScheme.scheme.tertiaryContainer)
        assertEquals(Color(fidelityScheme.onTertiaryContainer), companyScheme.scheme.onTertiaryContainer)
        assertEquals(Color(monochromeScheme.background), companyScheme.scheme.background)
        assertEquals(Color(monochromeScheme.onBackground), companyScheme.scheme.onBackground)
        assertEquals(Color(monochromeScheme.onBackground), companyScheme.scheme.onBackground)
        assertEquals(Color(monochromeScheme.surface), companyScheme.scheme.surface)
        assertEquals(Color(monochromeScheme.onSurface), companyScheme.scheme.onSurface)
        assertEquals(Color(monochromeScheme.surfaceVariant), companyScheme.scheme.surfaceVariant)
        assertEquals(Color(monochromeScheme.onSurfaceVariant), companyScheme.scheme.onSurfaceVariant)
        assertEquals(Color(monochromeScheme.surfaceTint), companyScheme.scheme.surfaceTint)
        assertEquals(Color(monochromeScheme.inverseSurface), companyScheme.scheme.inverseSurface)
        assertEquals(Color(monochromeScheme.inverseOnSurface), companyScheme.scheme.inverseOnSurface)
        assertEquals(Color(monochromeScheme.error), companyScheme.scheme.error)
        assertEquals(Color(monochromeScheme.onError), companyScheme.scheme.onError)
        assertEquals(Color(fidelityScheme.errorContainer), companyScheme.scheme.errorContainer)
        assertEquals(Color(fidelityScheme.onErrorContainer), companyScheme.scheme.onErrorContainer)
        assertEquals(Color(monochromeScheme.outline), companyScheme.scheme.outline)
        assertEquals(Color(monochromeScheme.outlineVariant), companyScheme.scheme.outlineVariant)
        assertEquals(Color(monochromeScheme.scrim), companyScheme.scheme.scrim)
        assertEquals(Color(monochromeScheme.surfaceBright), companyScheme.scheme.surfaceBright)
        assertEquals(Color(monochromeScheme.surfaceContainer), companyScheme.scheme.surfaceContainer)
        assertEquals(Color(monochromeScheme.surfaceContainerHigh), companyScheme.scheme.surfaceContainerHigh)
        assertEquals(Color(monochromeScheme.surfaceContainerHighest), companyScheme.scheme.surfaceContainerHighest)
        assertEquals(Color(monochromeScheme.surfaceContainerLow), companyScheme.scheme.surfaceContainerLow)
        assertEquals(Color(monochromeScheme.surfaceContainerLowest), companyScheme.scheme.surfaceContainerLowest)
        assertEquals(Color(monochromeScheme.surfaceDim), companyScheme.scheme.surfaceDim)
    }

    @Test
    fun seedColorWithNightDefaultStyle_generateDarkSchemeFidelity() {
        val seed = Color.Blue
        val theme = CompanyUI.Theme(
            defaultStyle = CompanyUI.Theme.DefaultStyle.Night,
            night = CompanyUI.Theme.Style(
                colors = CompanyUI.Theme.Colors.Seed(Color.Blue.toArgb())
            )
        )
        val fidelityScheme = SchemeFidelity(Hct.fromInt(seed.toArgb()), true, .0)
        val monochromeScheme = SchemeMonochrome(Hct.fromInt(seed.toArgb()), true, .0)

        val generator = CompanySchemeGenerator()
        val companyScheme = generator.getColorScheme(theme, false)

        assertEquals(true, companyScheme.isDark)
        assertEquals(seed, companyScheme.scheme.primary)
        assertEquals(Color(fidelityScheme.onPrimaryContainer), companyScheme.scheme.onPrimary)
        assertEquals(Color(fidelityScheme.primaryContainer), companyScheme.scheme.primaryContainer)
        assertEquals(Color(fidelityScheme.onPrimaryContainer), companyScheme.scheme.onPrimaryContainer)
        assertEquals(Color(fidelityScheme.inversePrimary), companyScheme.scheme.inversePrimary)
        assertEquals(Color(fidelityScheme.secondary), companyScheme.scheme.secondary)
        assertEquals(Color(fidelityScheme.onSecondary), companyScheme.scheme.onSecondary)
        assertEquals(Color(fidelityScheme.secondaryContainer), companyScheme.scheme.secondaryContainer)
        assertEquals(Color(fidelityScheme.onSecondaryContainer), companyScheme.scheme.onSecondaryContainer)
        assertEquals(Color(fidelityScheme.tertiary), companyScheme.scheme.tertiary)
        assertEquals(Color(fidelityScheme.onTertiary), companyScheme.scheme.onTertiary)
        assertEquals(Color(fidelityScheme.tertiaryContainer), companyScheme.scheme.tertiaryContainer)
        assertEquals(Color(fidelityScheme.onTertiaryContainer), companyScheme.scheme.onTertiaryContainer)
        assertEquals(Color(monochromeScheme.background), companyScheme.scheme.background)
        assertEquals(Color(monochromeScheme.onBackground), companyScheme.scheme.onBackground)
        assertEquals(Color(monochromeScheme.onBackground), companyScheme.scheme.onBackground)
        assertEquals(Color(monochromeScheme.surface), companyScheme.scheme.surface)
        assertEquals(Color(monochromeScheme.onSurface), companyScheme.scheme.onSurface)
        assertEquals(Color(monochromeScheme.surfaceVariant), companyScheme.scheme.surfaceVariant)
        assertEquals(Color(monochromeScheme.onSurfaceVariant), companyScheme.scheme.onSurfaceVariant)
        assertEquals(Color(monochromeScheme.surfaceTint), companyScheme.scheme.surfaceTint)
        assertEquals(Color(monochromeScheme.inverseSurface), companyScheme.scheme.inverseSurface)
        assertEquals(Color(monochromeScheme.inverseOnSurface), companyScheme.scheme.inverseOnSurface)
        assertEquals(Color(monochromeScheme.error), companyScheme.scheme.error)
        assertEquals(Color(monochromeScheme.onError), companyScheme.scheme.onError)
        assertEquals(Color(fidelityScheme.errorContainer), companyScheme.scheme.errorContainer)
        assertEquals(Color(fidelityScheme.onErrorContainer), companyScheme.scheme.onErrorContainer)
        assertEquals(Color(monochromeScheme.outline), companyScheme.scheme.outline)
        assertEquals(Color(monochromeScheme.outlineVariant), companyScheme.scheme.outlineVariant)
        assertEquals(Color(monochromeScheme.scrim), companyScheme.scheme.scrim)
        assertEquals(Color(monochromeScheme.surfaceBright), companyScheme.scheme.surfaceBright)
        assertEquals(Color(monochromeScheme.surfaceContainer), companyScheme.scheme.surfaceContainer)
        assertEquals(Color(monochromeScheme.surfaceContainerHigh), companyScheme.scheme.surfaceContainerHigh)
        assertEquals(Color(monochromeScheme.surfaceContainerHighest), companyScheme.scheme.surfaceContainerHighest)
        assertEquals(Color(monochromeScheme.surfaceContainerLow), companyScheme.scheme.surfaceContainerLow)
        assertEquals(Color(monochromeScheme.surfaceContainerLowest), companyScheme.scheme.surfaceContainerLowest)
        assertEquals(Color(monochromeScheme.surfaceDim), companyScheme.scheme.surfaceDim)
    }

    @Test
    fun seedColorWithSystemLightTheme_generateLightSchemeFidelity() {
        val daySeed = Color.Red
        val nightSeed = Color.Blue
        val theme = CompanyUI.Theme(
            defaultStyle = CompanyUI.Theme.DefaultStyle.System,
            day = CompanyUI.Theme.Style(
                colors = CompanyUI.Theme.Colors.Seed(daySeed.toArgb())
            ),
            night = CompanyUI.Theme.Style(
                colors = CompanyUI.Theme.Colors.Seed(nightSeed.toArgb())
            )
        )
        val fidelityScheme = SchemeFidelity(Hct.fromInt(daySeed.toArgb()), false, .0)
        val monochromeScheme = SchemeMonochrome(Hct.fromInt(daySeed.toArgb()), false, .0)

        val generator = CompanySchemeGenerator()
        val companyScheme = generator.getColorScheme(theme, false)

        assertEquals(false, companyScheme.isDark)
        assertEquals(daySeed, companyScheme.scheme.primary)
        assertEquals(Color(fidelityScheme.onPrimaryContainer), companyScheme.scheme.onPrimary)
        assertEquals(Color(fidelityScheme.primaryContainer), companyScheme.scheme.primaryContainer)
        assertEquals(Color(fidelityScheme.onPrimaryContainer), companyScheme.scheme.onPrimaryContainer)
        assertEquals(Color(fidelityScheme.inversePrimary), companyScheme.scheme.inversePrimary)
        assertEquals(Color(fidelityScheme.secondary), companyScheme.scheme.secondary)
        assertEquals(Color(fidelityScheme.onSecondary), companyScheme.scheme.onSecondary)
        assertEquals(Color(fidelityScheme.secondaryContainer), companyScheme.scheme.secondaryContainer)
        assertEquals(Color(fidelityScheme.onSecondaryContainer), companyScheme.scheme.onSecondaryContainer)
        assertEquals(Color(fidelityScheme.tertiary), companyScheme.scheme.tertiary)
        assertEquals(Color(fidelityScheme.onTertiary), companyScheme.scheme.onTertiary)
        assertEquals(Color(fidelityScheme.tertiaryContainer), companyScheme.scheme.tertiaryContainer)
        assertEquals(Color(fidelityScheme.onTertiaryContainer), companyScheme.scheme.onTertiaryContainer)
        assertEquals(Color(monochromeScheme.background), companyScheme.scheme.background)
        assertEquals(Color(monochromeScheme.onBackground), companyScheme.scheme.onBackground)
        assertEquals(Color(monochromeScheme.onBackground), companyScheme.scheme.onBackground)
        assertEquals(Color(monochromeScheme.surface), companyScheme.scheme.surface)
        assertEquals(Color(monochromeScheme.onSurface), companyScheme.scheme.onSurface)
        assertEquals(Color(monochromeScheme.surfaceVariant), companyScheme.scheme.surfaceVariant)
        assertEquals(Color(monochromeScheme.onSurfaceVariant), companyScheme.scheme.onSurfaceVariant)
        assertEquals(Color(monochromeScheme.surfaceTint), companyScheme.scheme.surfaceTint)
        assertEquals(Color(monochromeScheme.inverseSurface), companyScheme.scheme.inverseSurface)
        assertEquals(Color(monochromeScheme.inverseOnSurface), companyScheme.scheme.inverseOnSurface)
        assertEquals(Color(monochromeScheme.error), companyScheme.scheme.error)
        assertEquals(Color(monochromeScheme.onError), companyScheme.scheme.onError)
        assertEquals(Color(fidelityScheme.errorContainer), companyScheme.scheme.errorContainer)
        assertEquals(Color(fidelityScheme.onErrorContainer), companyScheme.scheme.onErrorContainer)
        assertEquals(Color(monochromeScheme.outline), companyScheme.scheme.outline)
        assertEquals(Color(monochromeScheme.outlineVariant), companyScheme.scheme.outlineVariant)
        assertEquals(Color(monochromeScheme.scrim), companyScheme.scheme.scrim)
        assertEquals(Color(monochromeScheme.surfaceBright), companyScheme.scheme.surfaceBright)
        assertEquals(Color(monochromeScheme.surfaceContainer), companyScheme.scheme.surfaceContainer)
        assertEquals(Color(monochromeScheme.surfaceContainerHigh), companyScheme.scheme.surfaceContainerHigh)
        assertEquals(Color(monochromeScheme.surfaceContainerHighest), companyScheme.scheme.surfaceContainerHighest)
        assertEquals(Color(monochromeScheme.surfaceContainerLow), companyScheme.scheme.surfaceContainerLow)
        assertEquals(Color(monochromeScheme.surfaceContainerLowest), companyScheme.scheme.surfaceContainerLowest)
        assertEquals(Color(monochromeScheme.surfaceDim), companyScheme.scheme.surfaceDim)
    }

    @Test
    fun seedColorWithSystemDarkTheme_generateDarkSchemeFidelity() {
        val daySeed = Color.Red
        val nightSeed = Color.Blue
        val theme = CompanyUI.Theme(
            defaultStyle = CompanyUI.Theme.DefaultStyle.System,
            day = CompanyUI.Theme.Style(
                colors = CompanyUI.Theme.Colors.Seed(daySeed.toArgb())
            ),
            night = CompanyUI.Theme.Style(
                colors = CompanyUI.Theme.Colors.Seed(nightSeed.toArgb())
            )
        )
        val fidelityScheme = SchemeFidelity(Hct.fromInt(daySeed.toArgb()), false, .0)
        val monochromeScheme = SchemeMonochrome(Hct.fromInt(daySeed.toArgb()), false, .0)

        val generator = CompanySchemeGenerator()
        val companyScheme = generator.getColorScheme(theme, false)

        assertEquals(false, companyScheme.isDark)
        assertEquals(daySeed, companyScheme.scheme.primary)
        assertEquals(Color(fidelityScheme.onPrimaryContainer), companyScheme.scheme.onPrimary)
        assertEquals(Color(fidelityScheme.primaryContainer), companyScheme.scheme.primaryContainer)
        assertEquals(Color(fidelityScheme.onPrimaryContainer), companyScheme.scheme.onPrimaryContainer)
        assertEquals(Color(fidelityScheme.inversePrimary), companyScheme.scheme.inversePrimary)
        assertEquals(Color(fidelityScheme.secondary), companyScheme.scheme.secondary)
        assertEquals(Color(fidelityScheme.onSecondary), companyScheme.scheme.onSecondary)
        assertEquals(Color(fidelityScheme.secondaryContainer), companyScheme.scheme.secondaryContainer)
        assertEquals(Color(fidelityScheme.onSecondaryContainer), companyScheme.scheme.onSecondaryContainer)
        assertEquals(Color(fidelityScheme.tertiary), companyScheme.scheme.tertiary)
        assertEquals(Color(fidelityScheme.onTertiary), companyScheme.scheme.onTertiary)
        assertEquals(Color(fidelityScheme.tertiaryContainer), companyScheme.scheme.tertiaryContainer)
        assertEquals(Color(fidelityScheme.onTertiaryContainer), companyScheme.scheme.onTertiaryContainer)
        assertEquals(Color(monochromeScheme.background), companyScheme.scheme.background)
        assertEquals(Color(monochromeScheme.onBackground), companyScheme.scheme.onBackground)
        assertEquals(Color(monochromeScheme.onBackground), companyScheme.scheme.onBackground)
        assertEquals(Color(monochromeScheme.surface), companyScheme.scheme.surface)
        assertEquals(Color(monochromeScheme.onSurface), companyScheme.scheme.onSurface)
        assertEquals(Color(monochromeScheme.surfaceVariant), companyScheme.scheme.surfaceVariant)
        assertEquals(Color(monochromeScheme.onSurfaceVariant), companyScheme.scheme.onSurfaceVariant)
        assertEquals(Color(monochromeScheme.surfaceTint), companyScheme.scheme.surfaceTint)
        assertEquals(Color(monochromeScheme.inverseSurface), companyScheme.scheme.inverseSurface)
        assertEquals(Color(monochromeScheme.inverseOnSurface), companyScheme.scheme.inverseOnSurface)
        assertEquals(Color(monochromeScheme.error), companyScheme.scheme.error)
        assertEquals(Color(monochromeScheme.onError), companyScheme.scheme.onError)
        assertEquals(Color(fidelityScheme.errorContainer), companyScheme.scheme.errorContainer)
        assertEquals(Color(fidelityScheme.onErrorContainer), companyScheme.scheme.onErrorContainer)
        assertEquals(Color(monochromeScheme.outline), companyScheme.scheme.outline)
        assertEquals(Color(monochromeScheme.outlineVariant), companyScheme.scheme.outlineVariant)
        assertEquals(Color(monochromeScheme.scrim), companyScheme.scheme.scrim)
        assertEquals(Color(monochromeScheme.surfaceBright), companyScheme.scheme.surfaceBright)
        assertEquals(Color(monochromeScheme.surfaceContainer), companyScheme.scheme.surfaceContainer)
        assertEquals(Color(monochromeScheme.surfaceContainerHigh), companyScheme.scheme.surfaceContainerHigh)
        assertEquals(Color(monochromeScheme.surfaceContainerHighest), companyScheme.scheme.surfaceContainerHighest)
        assertEquals(Color(monochromeScheme.surfaceContainerLow), companyScheme.scheme.surfaceContainerLow)
        assertEquals(Color(monochromeScheme.surfaceContainerLowest), companyScheme.scheme.surfaceContainerLowest)
        assertEquals(Color(monochromeScheme.surfaceDim), companyScheme.scheme.surfaceDim)
    }
}