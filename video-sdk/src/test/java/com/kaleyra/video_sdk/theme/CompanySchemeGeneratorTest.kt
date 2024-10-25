//package com.kaleyra.video_sdk.theme
//
//import androidx.compose.material3.darkColorScheme
//import androidx.compose.material3.lightColorScheme
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.graphics.toArgb
//import com.kaleyra.material_color_utilities.hct.Hct
//import com.kaleyra.material_color_utilities.scheme.SchemeFidelity
//import com.kaleyra.material_color_utilities.scheme.SchemeMonochrome
//import com.kaleyra.video_common_ui.CompanyUI
//import io.mockk.mockk
//import org.junit.Assert.assertEquals
//import org.junit.Test
//
//class CompanySchemeGeneratorTest {
//
//    @Test
//    fun noSeedColor_generateLightMonochromeCompanyScheme() {
//        val theme = CompanyUI.Theme(
//            defaultStyle = CompanyUI.Theme.DefaultStyle.Day,
//            day = CompanyUI.Theme.Style(
//                colors = mockk<CompanyUI.Theme.Colors>()
//            )
//        )
//        val monochromeScheme = SchemeMonochrome(Hct.fromInt(Color.White.toArgb()), false, .0)
//
//        val generator = CompanySchemeGenerator()
//        val companyScheme = generator.getCompanyScheme(theme, false)
//
//        assertEquals(false, companyScheme.isDark)
//        assertEquals(Color(monochromeScheme.primary), companyScheme.scheme.primary)
//        assertEquals(Color(monochromeScheme.onPrimary), companyScheme.scheme.onPrimary)
//        assertEquals(Color(monochromeScheme.primaryContainer), companyScheme.scheme.primaryContainer)
//        assertEquals(Color(monochromeScheme.onPrimaryContainer), companyScheme.scheme.onPrimaryContainer)
//        assertEquals(Color(monochromeScheme.inversePrimary), companyScheme.scheme.inversePrimary)
//        assertEquals(Color(monochromeScheme.secondary), companyScheme.scheme.secondary)
//        assertEquals(Color(monochromeScheme.onSecondary), companyScheme.scheme.onSecondary)
//        assertEquals(Color(monochromeScheme.secondaryContainer), companyScheme.scheme.secondaryContainer)
//        assertEquals(Color(monochromeScheme.onSecondaryContainer), companyScheme.scheme.onSecondaryContainer)
//        assertEquals(Color(monochromeScheme.tertiary), companyScheme.scheme.tertiary)
//        assertEquals(Color(monochromeScheme.onTertiary), companyScheme.scheme.onTertiary)
//        assertEquals(Color(monochromeScheme.tertiaryContainer), companyScheme.scheme.tertiaryContainer)
//        assertEquals(Color(monochromeScheme.onTertiaryContainer), companyScheme.scheme.onTertiaryContainer)
//        assertEquals(Color(monochromeScheme.background), companyScheme.scheme.background)
//        assertEquals(Color(monochromeScheme.onBackground), companyScheme.scheme.onBackground)
//        assertEquals(Color(monochromeScheme.onBackground), companyScheme.scheme.onBackground)
//        assertEquals(Color(monochromeScheme.surface), companyScheme.scheme.surface)
//        assertEquals(Color(monochromeScheme.onSurface), companyScheme.scheme.onSurface)
//        assertEquals(Color(monochromeScheme.surfaceVariant), companyScheme.scheme.surfaceVariant)
//        assertEquals(Color(monochromeScheme.onSurfaceVariant), companyScheme.scheme.onSurfaceVariant)
//        assertEquals(Color(monochromeScheme.surfaceTint), companyScheme.scheme.surfaceTint)
//        assertEquals(Color(monochromeScheme.inverseSurface), companyScheme.scheme.inverseSurface)
//        assertEquals(Color(monochromeScheme.inverseOnSurface), companyScheme.scheme.inverseOnSurface)
//        assertEquals(Color(monochromeScheme.error), companyScheme.scheme.error)
//        assertEquals(Color(monochromeScheme.onError), companyScheme.scheme.onError)
//        assertEquals(Color(monochromeScheme.errorContainer), companyScheme.scheme.errorContainer)
//        assertEquals(Color(monochromeScheme.onErrorContainer), companyScheme.scheme.onErrorContainer)
//        assertEquals(Color(monochromeScheme.outline), companyScheme.scheme.outline)
//        assertEquals(Color(monochromeScheme.outlineVariant), companyScheme.scheme.outlineVariant)
//        assertEquals(Color(monochromeScheme.scrim), companyScheme.scheme.scrim)
//        assertEquals(Color(monochromeScheme.surfaceBright), companyScheme.scheme.surfaceBright)
//        assertEquals(Color(monochromeScheme.surfaceContainer), companyScheme.scheme.surfaceContainer)
//        assertEquals(Color(monochromeScheme.surfaceContainerHigh), companyScheme.scheme.surfaceContainerHigh)
//        assertEquals(Color(monochromeScheme.surfaceContainerHighest), companyScheme.scheme.surfaceContainerHighest)
//        assertEquals(Color(monochromeScheme.surfaceContainerLow), companyScheme.scheme.surfaceContainerLow)
//        assertEquals(Color(monochromeScheme.surfaceContainerLowest), companyScheme.scheme.surfaceContainerLowest)
//        assertEquals(Color(monochromeScheme.surfaceDim), companyScheme.scheme.surfaceDim)
//    }
//
//    @Test
//    fun noSeedColor_generateDarkMonochromeCompanyScheme() {
//        val theme = CompanyUI.Theme(
//            defaultStyle = CompanyUI.Theme.DefaultStyle.Night,
//            night = CompanyUI.Theme.Style(
//                colors = mockk<CompanyUI.Theme.Colors>()
//            )
//        )
//        val monochromeScheme = SchemeMonochrome(Hct.fromInt(Color.White.toArgb()), true, .0)
//
//        val generator = CompanySchemeGenerator()
//        val companyScheme = generator.getCompanyScheme(theme, false)
//
//        assertEquals(true, companyScheme.isDark)
//        assertEquals(Color(monochromeScheme.primary), companyScheme.scheme.primary)
//        assertEquals(Color(monochromeScheme.onPrimary), companyScheme.scheme.onPrimary)
//        assertEquals(Color(monochromeScheme.primaryContainer), companyScheme.scheme.primaryContainer)
//        assertEquals(Color(monochromeScheme.onPrimaryContainer), companyScheme.scheme.onPrimaryContainer)
//        assertEquals(Color(monochromeScheme.inversePrimary), companyScheme.scheme.inversePrimary)
//        assertEquals(Color(monochromeScheme.secondary), companyScheme.scheme.secondary)
//        assertEquals(Color(monochromeScheme.onSecondary), companyScheme.scheme.onSecondary)
//        assertEquals(Color(monochromeScheme.secondaryContainer), companyScheme.scheme.secondaryContainer)
//        assertEquals(Color(monochromeScheme.onSecondaryContainer), companyScheme.scheme.onSecondaryContainer)
//        assertEquals(Color(monochromeScheme.tertiary), companyScheme.scheme.tertiary)
//        assertEquals(Color(monochromeScheme.onTertiary), companyScheme.scheme.onTertiary)
//        assertEquals(Color(monochromeScheme.tertiaryContainer), companyScheme.scheme.tertiaryContainer)
//        assertEquals(Color(monochromeScheme.onTertiaryContainer), companyScheme.scheme.onTertiaryContainer)
//        assertEquals(Color(monochromeScheme.background), companyScheme.scheme.background)
//        assertEquals(Color(monochromeScheme.onBackground), companyScheme.scheme.onBackground)
//        assertEquals(Color(monochromeScheme.onBackground), companyScheme.scheme.onBackground)
//        assertEquals(Color(monochromeScheme.surface), companyScheme.scheme.surface)
//        assertEquals(Color(monochromeScheme.onSurface), companyScheme.scheme.onSurface)
//        assertEquals(Color(monochromeScheme.surfaceVariant), companyScheme.scheme.surfaceVariant)
//        assertEquals(Color(monochromeScheme.onSurfaceVariant), companyScheme.scheme.onSurfaceVariant)
//        assertEquals(Color(monochromeScheme.surfaceTint), companyScheme.scheme.surfaceTint)
//        assertEquals(Color(monochromeScheme.inverseSurface), companyScheme.scheme.inverseSurface)
//        assertEquals(Color(monochromeScheme.inverseOnSurface), companyScheme.scheme.inverseOnSurface)
//        assertEquals(Color(monochromeScheme.error), companyScheme.scheme.error)
//        assertEquals(Color(monochromeScheme.onError), companyScheme.scheme.onError)
//        assertEquals(Color(monochromeScheme.errorContainer), companyScheme.scheme.errorContainer)
//        assertEquals(Color(monochromeScheme.onErrorContainer), companyScheme.scheme.onErrorContainer)
//        assertEquals(Color(monochromeScheme.outline), companyScheme.scheme.outline)
//        assertEquals(Color(monochromeScheme.outlineVariant), companyScheme.scheme.outlineVariant)
//        assertEquals(Color(monochromeScheme.scrim), companyScheme.scheme.scrim)
//        assertEquals(Color(monochromeScheme.surfaceBright), companyScheme.scheme.surfaceBright)
//        assertEquals(Color(monochromeScheme.surfaceContainer), companyScheme.scheme.surfaceContainer)
//        assertEquals(Color(monochromeScheme.surfaceContainerHigh), companyScheme.scheme.surfaceContainerHigh)
//        assertEquals(Color(monochromeScheme.surfaceContainerHighest), companyScheme.scheme.surfaceContainerHighest)
//        assertEquals(Color(monochromeScheme.surfaceContainerLow), companyScheme.scheme.surfaceContainerLow)
//        assertEquals(Color(monochromeScheme.surfaceContainerLowest), companyScheme.scheme.surfaceContainerLowest)
//        assertEquals(Color(monochromeScheme.surfaceDim), companyScheme.scheme.surfaceDim)
//    }
//
//    @Test
//    fun colorPaletteWithDayDefaultStyle_generateCompanyScheme() {
//        val colorScheme = lightColorScheme()
//        val theme = CompanyUI.Theme(
//            defaultStyle = CompanyUI.Theme.DefaultStyle.Day,
//            day = CompanyUI.Theme.Style(
//                colors = CompanyUI.Theme.Colors.Palette(colorScheme)
//            )
//        )
//
//        val generator = CompanySchemeGenerator()
//        val companyScheme = generator.getCompanyScheme(theme, true)
//
//        assertEquals(false, companyScheme.isDark)
//        assertEquals(colorScheme.primary, companyScheme.scheme.primary)
//        assertEquals(colorScheme.onPrimary, companyScheme.scheme.onPrimary)
//        assertEquals(colorScheme.primaryContainer, companyScheme.scheme.primaryContainer)
//        assertEquals(colorScheme.onPrimaryContainer, companyScheme.scheme.onPrimaryContainer)
//        assertEquals(colorScheme.inversePrimary, companyScheme.scheme.inversePrimary)
//        assertEquals(colorScheme.secondary, companyScheme.scheme.secondary)
//        assertEquals(colorScheme.onSecondary, companyScheme.scheme.onSecondary)
//        assertEquals(colorScheme.secondaryContainer, companyScheme.scheme.secondaryContainer)
//        assertEquals(colorScheme.onSecondaryContainer, companyScheme.scheme.onSecondaryContainer)
//        assertEquals(colorScheme.tertiary, companyScheme.scheme.tertiary)
//        assertEquals(colorScheme.onTertiary, companyScheme.scheme.onTertiary)
//        assertEquals(colorScheme.tertiaryContainer, companyScheme.scheme.tertiaryContainer)
//        assertEquals(colorScheme.onTertiaryContainer, companyScheme.scheme.onTertiaryContainer)
//        assertEquals(colorScheme.background, companyScheme.scheme.background)
//        assertEquals(colorScheme.onBackground, companyScheme.scheme.onBackground)
//        assertEquals(colorScheme.onBackground, companyScheme.scheme.onBackground)
//        assertEquals(colorScheme.surface, companyScheme.scheme.surface)
//        assertEquals(colorScheme.onSurface, companyScheme.scheme.onSurface)
//        assertEquals(colorScheme.surfaceVariant, companyScheme.scheme.surfaceVariant)
//        assertEquals(colorScheme.onSurfaceVariant, companyScheme.scheme.onSurfaceVariant)
//        assertEquals(colorScheme.surfaceTint, companyScheme.scheme.surfaceTint)
//        assertEquals(colorScheme.inverseSurface, companyScheme.scheme.inverseSurface)
//        assertEquals(colorScheme.inverseOnSurface, companyScheme.scheme.inverseOnSurface)
//        assertEquals(colorScheme.error, companyScheme.scheme.error)
//        assertEquals(colorScheme.onError, companyScheme.scheme.onError)
//        assertEquals(colorScheme.errorContainer, companyScheme.scheme.errorContainer)
//        assertEquals(colorScheme.onErrorContainer, companyScheme.scheme.onErrorContainer)
//        assertEquals(colorScheme.outline, companyScheme.scheme.outline)
//        assertEquals(colorScheme.outlineVariant, companyScheme.scheme.outlineVariant)
//        assertEquals(colorScheme.scrim, companyScheme.scheme.scrim)
//        assertEquals(colorScheme.surfaceBright, companyScheme.scheme.surfaceBright)
//        assertEquals(colorScheme.surfaceContainer, companyScheme.scheme.surfaceContainer)
//        assertEquals(colorScheme.surfaceContainerHigh, companyScheme.scheme.surfaceContainerHigh)
//        assertEquals(colorScheme.surfaceContainerHighest, companyScheme.scheme.surfaceContainerHighest)
//        assertEquals(colorScheme.surfaceContainerLow, companyScheme.scheme.surfaceContainerLow)
//        assertEquals(colorScheme.surfaceContainerLowest, companyScheme.scheme.surfaceContainerLowest)
//        assertEquals(colorScheme.surfaceDim, companyScheme.scheme.surfaceDim)
//    }
//
//    @Test
//    fun colorPaletteWithNightDefaultStyle_generateCompanyScheme() {
//        val colorScheme = darkColorScheme()
//        val theme = CompanyUI.Theme(
//            defaultStyle = CompanyUI.Theme.DefaultStyle.Night,
//            night = CompanyUI.Theme.Style(
//                colors = CompanyUI.Theme.Colors.Palette(colorScheme)
//            )
//        )
//
//        val generator = CompanySchemeGenerator()
//        val companyScheme = generator.getCompanyScheme(theme, false)
//
//        assertEquals(true, companyScheme.isDark)
//        assertEquals(colorScheme.primary, companyScheme.scheme.primary)
//        assertEquals(colorScheme.onPrimary, companyScheme.scheme.onPrimary)
//        assertEquals(colorScheme.primaryContainer, companyScheme.scheme.primaryContainer)
//        assertEquals(colorScheme.onPrimaryContainer, companyScheme.scheme.onPrimaryContainer)
//        assertEquals(colorScheme.inversePrimary, companyScheme.scheme.inversePrimary)
//        assertEquals(colorScheme.secondary, companyScheme.scheme.secondary)
//        assertEquals(colorScheme.onSecondary, companyScheme.scheme.onSecondary)
//        assertEquals(colorScheme.secondaryContainer, companyScheme.scheme.secondaryContainer)
//        assertEquals(colorScheme.onSecondaryContainer, companyScheme.scheme.onSecondaryContainer)
//        assertEquals(colorScheme.tertiary, companyScheme.scheme.tertiary)
//        assertEquals(colorScheme.onTertiary, companyScheme.scheme.onTertiary)
//        assertEquals(colorScheme.tertiaryContainer, companyScheme.scheme.tertiaryContainer)
//        assertEquals(colorScheme.onTertiaryContainer, companyScheme.scheme.onTertiaryContainer)
//        assertEquals(colorScheme.background, companyScheme.scheme.background)
//        assertEquals(colorScheme.onBackground, companyScheme.scheme.onBackground)
//        assertEquals(colorScheme.onBackground, companyScheme.scheme.onBackground)
//        assertEquals(colorScheme.surface, companyScheme.scheme.surface)
//        assertEquals(colorScheme.onSurface, companyScheme.scheme.onSurface)
//        assertEquals(colorScheme.surfaceVariant, companyScheme.scheme.surfaceVariant)
//        assertEquals(colorScheme.onSurfaceVariant, companyScheme.scheme.onSurfaceVariant)
//        assertEquals(colorScheme.surfaceTint, companyScheme.scheme.surfaceTint)
//        assertEquals(colorScheme.inverseSurface, companyScheme.scheme.inverseSurface)
//        assertEquals(colorScheme.inverseOnSurface, companyScheme.scheme.inverseOnSurface)
//        assertEquals(colorScheme.error, companyScheme.scheme.error)
//        assertEquals(colorScheme.onError, companyScheme.scheme.onError)
//        assertEquals(colorScheme.errorContainer, companyScheme.scheme.errorContainer)
//        assertEquals(colorScheme.onErrorContainer, companyScheme.scheme.onErrorContainer)
//        assertEquals(colorScheme.outline, companyScheme.scheme.outline)
//        assertEquals(colorScheme.outlineVariant, companyScheme.scheme.outlineVariant)
//        assertEquals(colorScheme.scrim, companyScheme.scheme.scrim)
//        assertEquals(colorScheme.surfaceBright, companyScheme.scheme.surfaceBright)
//        assertEquals(colorScheme.surfaceContainer, companyScheme.scheme.surfaceContainer)
//        assertEquals(colorScheme.surfaceContainerHigh, companyScheme.scheme.surfaceContainerHigh)
//        assertEquals(colorScheme.surfaceContainerHighest, companyScheme.scheme.surfaceContainerHighest)
//        assertEquals(colorScheme.surfaceContainerLow, companyScheme.scheme.surfaceContainerLow)
//        assertEquals(colorScheme.surfaceContainerLowest, companyScheme.scheme.surfaceContainerLowest)
//        assertEquals(colorScheme.surfaceDim, companyScheme.scheme.surfaceDim)
//    }
//
//    @Test
//    fun colorPaletteWithSystemLightTheme_generateLightCompanyScheme() {
//        val dayColorScheme = lightColorScheme()
//        val nightColorScheme = darkColorScheme()
//        val theme = CompanyUI.Theme(
//            defaultStyle = CompanyUI.Theme.DefaultStyle.System,
//            day = CompanyUI.Theme.Style(
//                colors = CompanyUI.Theme.Colors.Palette(dayColorScheme)
//            ),
//            night = CompanyUI.Theme.Style(
//                colors = CompanyUI.Theme.Colors.Palette(nightColorScheme)
//            ),
//        )
//
//        val generator = CompanySchemeGenerator()
//        val companyScheme = generator.getCompanyScheme(theme, false)
//
//        assertEquals(false, companyScheme.isDark)
//        assertEquals(dayColorScheme.primary, companyScheme.scheme.primary)
//        assertEquals(dayColorScheme.onPrimary, companyScheme.scheme.onPrimary)
//        assertEquals(dayColorScheme.primaryContainer, companyScheme.scheme.primaryContainer)
//        assertEquals(dayColorScheme.onPrimaryContainer, companyScheme.scheme.onPrimaryContainer)
//        assertEquals(dayColorScheme.inversePrimary, companyScheme.scheme.inversePrimary)
//        assertEquals(dayColorScheme.secondary, companyScheme.scheme.secondary)
//        assertEquals(dayColorScheme.onSecondary, companyScheme.scheme.onSecondary)
//        assertEquals(dayColorScheme.secondaryContainer, companyScheme.scheme.secondaryContainer)
//        assertEquals(dayColorScheme.onSecondaryContainer, companyScheme.scheme.onSecondaryContainer)
//        assertEquals(dayColorScheme.tertiary, companyScheme.scheme.tertiary)
//        assertEquals(dayColorScheme.onTertiary, companyScheme.scheme.onTertiary)
//        assertEquals(dayColorScheme.tertiaryContainer, companyScheme.scheme.tertiaryContainer)
//        assertEquals(dayColorScheme.onTertiaryContainer, companyScheme.scheme.onTertiaryContainer)
//        assertEquals(dayColorScheme.background, companyScheme.scheme.background)
//        assertEquals(dayColorScheme.onBackground, companyScheme.scheme.onBackground)
//        assertEquals(dayColorScheme.onBackground, companyScheme.scheme.onBackground)
//        assertEquals(dayColorScheme.surface, companyScheme.scheme.surface)
//        assertEquals(dayColorScheme.onSurface, companyScheme.scheme.onSurface)
//        assertEquals(dayColorScheme.surfaceVariant, companyScheme.scheme.surfaceVariant)
//        assertEquals(dayColorScheme.onSurfaceVariant, companyScheme.scheme.onSurfaceVariant)
//        assertEquals(dayColorScheme.surfaceTint, companyScheme.scheme.surfaceTint)
//        assertEquals(dayColorScheme.inverseSurface, companyScheme.scheme.inverseSurface)
//        assertEquals(dayColorScheme.inverseOnSurface, companyScheme.scheme.inverseOnSurface)
//        assertEquals(dayColorScheme.error, companyScheme.scheme.error)
//        assertEquals(dayColorScheme.onError, companyScheme.scheme.onError)
//        assertEquals(dayColorScheme.errorContainer, companyScheme.scheme.errorContainer)
//        assertEquals(dayColorScheme.onErrorContainer, companyScheme.scheme.onErrorContainer)
//        assertEquals(dayColorScheme.outline, companyScheme.scheme.outline)
//        assertEquals(dayColorScheme.outlineVariant, companyScheme.scheme.outlineVariant)
//        assertEquals(dayColorScheme.scrim, companyScheme.scheme.scrim)
//        assertEquals(dayColorScheme.surfaceBright, companyScheme.scheme.surfaceBright)
//        assertEquals(dayColorScheme.surfaceContainer, companyScheme.scheme.surfaceContainer)
//        assertEquals(dayColorScheme.surfaceContainerHigh, companyScheme.scheme.surfaceContainerHigh)
//        assertEquals(dayColorScheme.surfaceContainerHighest, companyScheme.scheme.surfaceContainerHighest)
//        assertEquals(dayColorScheme.surfaceContainerLow, companyScheme.scheme.surfaceContainerLow)
//        assertEquals(dayColorScheme.surfaceContainerLowest, companyScheme.scheme.surfaceContainerLowest)
//        assertEquals(dayColorScheme.surfaceDim, companyScheme.scheme.surfaceDim)
//    }
//
//    @Test
//    fun colorPaletteWithSystemDarkTheme_generateDarkCompanyScheme() {
//        val dayColorScheme = lightColorScheme()
//        val nightColorScheme = darkColorScheme()
//        val theme = CompanyUI.Theme(
//            defaultStyle = CompanyUI.Theme.DefaultStyle.System,
//            day = CompanyUI.Theme.Style(
//                colors = CompanyUI.Theme.Colors.Palette(dayColorScheme)
//            ),
//            night = CompanyUI.Theme.Style(
//                colors = CompanyUI.Theme.Colors.Palette(nightColorScheme)
//            ),
//        )
//
//        val generator = CompanySchemeGenerator()
//        val companyScheme = generator.getCompanyScheme(theme, true)
//
//        assertEquals(true, companyScheme.isDark)
//        assertEquals(nightColorScheme.primary, companyScheme.scheme.primary)
//        assertEquals(nightColorScheme.onPrimary, companyScheme.scheme.onPrimary)
//        assertEquals(nightColorScheme.primaryContainer, companyScheme.scheme.primaryContainer)
//        assertEquals(nightColorScheme.onPrimaryContainer, companyScheme.scheme.onPrimaryContainer)
//        assertEquals(nightColorScheme.inversePrimary, companyScheme.scheme.inversePrimary)
//        assertEquals(nightColorScheme.secondary, companyScheme.scheme.secondary)
//        assertEquals(nightColorScheme.onSecondary, companyScheme.scheme.onSecondary)
//        assertEquals(nightColorScheme.secondaryContainer, companyScheme.scheme.secondaryContainer)
//        assertEquals(nightColorScheme.onSecondaryContainer, companyScheme.scheme.onSecondaryContainer)
//        assertEquals(nightColorScheme.tertiary, companyScheme.scheme.tertiary)
//        assertEquals(nightColorScheme.onTertiary, companyScheme.scheme.onTertiary)
//        assertEquals(nightColorScheme.tertiaryContainer, companyScheme.scheme.tertiaryContainer)
//        assertEquals(nightColorScheme.onTertiaryContainer, companyScheme.scheme.onTertiaryContainer)
//        assertEquals(nightColorScheme.background, companyScheme.scheme.background)
//        assertEquals(nightColorScheme.onBackground, companyScheme.scheme.onBackground)
//        assertEquals(nightColorScheme.onBackground, companyScheme.scheme.onBackground)
//        assertEquals(nightColorScheme.surface, companyScheme.scheme.surface)
//        assertEquals(nightColorScheme.onSurface, companyScheme.scheme.onSurface)
//        assertEquals(nightColorScheme.surfaceVariant, companyScheme.scheme.surfaceVariant)
//        assertEquals(nightColorScheme.onSurfaceVariant, companyScheme.scheme.onSurfaceVariant)
//        assertEquals(nightColorScheme.surfaceTint, companyScheme.scheme.surfaceTint)
//        assertEquals(nightColorScheme.inverseSurface, companyScheme.scheme.inverseSurface)
//        assertEquals(nightColorScheme.inverseOnSurface, companyScheme.scheme.inverseOnSurface)
//        assertEquals(nightColorScheme.error, companyScheme.scheme.error)
//        assertEquals(nightColorScheme.onError, companyScheme.scheme.onError)
//        assertEquals(nightColorScheme.errorContainer, companyScheme.scheme.errorContainer)
//        assertEquals(nightColorScheme.onErrorContainer, companyScheme.scheme.onErrorContainer)
//        assertEquals(nightColorScheme.outline, companyScheme.scheme.outline)
//        assertEquals(nightColorScheme.outlineVariant, companyScheme.scheme.outlineVariant)
//        assertEquals(nightColorScheme.scrim, companyScheme.scheme.scrim)
//        assertEquals(nightColorScheme.surfaceBright, companyScheme.scheme.surfaceBright)
//        assertEquals(nightColorScheme.surfaceContainer, companyScheme.scheme.surfaceContainer)
//        assertEquals(nightColorScheme.surfaceContainerHigh, companyScheme.scheme.surfaceContainerHigh)
//        assertEquals(nightColorScheme.surfaceContainerHighest, companyScheme.scheme.surfaceContainerHighest)
//        assertEquals(nightColorScheme.surfaceContainerLow, companyScheme.scheme.surfaceContainerLow)
//        assertEquals(nightColorScheme.surfaceContainerLowest, companyScheme.scheme.surfaceContainerLowest)
//        assertEquals(nightColorScheme.surfaceDim, companyScheme.scheme.surfaceDim)
//    }
//
//    @Test
//    fun seedColorWithDayDefaultStyle_generateLightCompanyScheme() {
//        val seed = Color.Red
//        val theme = CompanyUI.Theme(
//            defaultStyle = CompanyUI.Theme.DefaultStyle.Day,
//            day = CompanyUI.Theme.Style(
//                colors = CompanyUI.Theme.Colors.Seed(seed.toArgb())
//            )
//        )
//        val fidelityScheme = SchemeFidelity(Hct.fromInt(seed.toArgb()), false, .0)
//        val monochromeScheme = SchemeMonochrome(Hct.fromInt(seed.toArgb()), false, .0)
//
//        val generator = CompanySchemeGenerator()
//        val companyScheme = generator.getCompanyScheme(theme, false)
//
//        assertEquals(false, companyScheme.isDark)
//        assertEquals(seed, companyScheme.scheme.primary)
//        assertEquals(Color(fidelityScheme.onPrimaryContainer), companyScheme.scheme.onPrimary)
//        assertEquals(Color(fidelityScheme.primaryContainer), companyScheme.scheme.primaryContainer)
//        assertEquals(Color(fidelityScheme.onPrimaryContainer), companyScheme.scheme.onPrimaryContainer)
//        assertEquals(Color(fidelityScheme.inversePrimary), companyScheme.scheme.inversePrimary)
//        assertEquals(Color(fidelityScheme.secondary), companyScheme.scheme.secondary)
//        assertEquals(Color(fidelityScheme.onSecondary), companyScheme.scheme.onSecondary)
//        assertEquals(Color(fidelityScheme.secondaryContainer), companyScheme.scheme.secondaryContainer)
//        assertEquals(Color(fidelityScheme.onSecondaryContainer), companyScheme.scheme.onSecondaryContainer)
//        assertEquals(Color(fidelityScheme.tertiary), companyScheme.scheme.tertiary)
//        assertEquals(Color(fidelityScheme.onTertiary), companyScheme.scheme.onTertiary)
//        assertEquals(Color(fidelityScheme.tertiaryContainer), companyScheme.scheme.tertiaryContainer)
//        assertEquals(Color(fidelityScheme.onTertiaryContainer), companyScheme.scheme.onTertiaryContainer)
//        assertEquals(Color(monochromeScheme.background), companyScheme.scheme.background)
//        assertEquals(Color(monochromeScheme.onBackground), companyScheme.scheme.onBackground)
//        assertEquals(Color(monochromeScheme.onBackground), companyScheme.scheme.onBackground)
//        assertEquals(Color(monochromeScheme.surface), companyScheme.scheme.surface)
//        assertEquals(Color(monochromeScheme.onSurface), companyScheme.scheme.onSurface)
//        assertEquals(Color(monochromeScheme.surfaceVariant), companyScheme.scheme.surfaceVariant)
//        assertEquals(Color(monochromeScheme.onSurfaceVariant), companyScheme.scheme.onSurfaceVariant)
//        assertEquals(Color(monochromeScheme.surfaceTint), companyScheme.scheme.surfaceTint)
//        assertEquals(Color(monochromeScheme.inverseSurface), companyScheme.scheme.inverseSurface)
//        assertEquals(Color(monochromeScheme.inverseOnSurface), companyScheme.scheme.inverseOnSurface)
//        assertEquals(Color(monochromeScheme.error), companyScheme.scheme.error)
//        assertEquals(Color(monochromeScheme.onError), companyScheme.scheme.onError)
//        assertEquals(Color(fidelityScheme.errorContainer), companyScheme.scheme.errorContainer)
//        assertEquals(Color(fidelityScheme.onErrorContainer), companyScheme.scheme.onErrorContainer)
//        assertEquals(Color(monochromeScheme.outline), companyScheme.scheme.outline)
//        assertEquals(Color(monochromeScheme.outlineVariant), companyScheme.scheme.outlineVariant)
//        assertEquals(Color(monochromeScheme.scrim), companyScheme.scheme.scrim)
//        assertEquals(Color(monochromeScheme.surfaceBright), companyScheme.scheme.surfaceBright)
//        assertEquals(Color(monochromeScheme.surfaceContainer), companyScheme.scheme.surfaceContainer)
//        assertEquals(Color(monochromeScheme.surfaceContainerHigh), companyScheme.scheme.surfaceContainerHigh)
//        assertEquals(Color(monochromeScheme.surfaceContainerHighest), companyScheme.scheme.surfaceContainerHighest)
//        assertEquals(Color(monochromeScheme.surfaceContainerLow), companyScheme.scheme.surfaceContainerLow)
//        assertEquals(Color(monochromeScheme.surfaceContainerLowest), companyScheme.scheme.surfaceContainerLowest)
//        assertEquals(Color(monochromeScheme.surfaceDim), companyScheme.scheme.surfaceDim)
//    }
//
//    @Test
//    fun seedColorWithNightDefaultStyle_generateDarkCompanyScheme() {
//        val seed = Color.Blue
//        val theme = CompanyUI.Theme(
//            defaultStyle = CompanyUI.Theme.DefaultStyle.Night,
//            night = CompanyUI.Theme.Style(
//                colors = CompanyUI.Theme.Colors.Seed(Color.Blue.toArgb())
//            )
//        )
//        val fidelityScheme = SchemeFidelity(Hct.fromInt(seed.toArgb()), true, .0)
//        val monochromeScheme = SchemeMonochrome(Hct.fromInt(seed.toArgb()), true, .0)
//
//        val generator = CompanySchemeGenerator()
//        val companyScheme = generator.getCompanyScheme(theme, false)
//
//        assertEquals(true, companyScheme.isDark)
//        assertEquals(seed, companyScheme.scheme.primary)
//        assertEquals(Color(fidelityScheme.onPrimaryContainer), companyScheme.scheme.onPrimary)
//        assertEquals(Color(fidelityScheme.primaryContainer), companyScheme.scheme.primaryContainer)
//        assertEquals(Color(fidelityScheme.onPrimaryContainer), companyScheme.scheme.onPrimaryContainer)
//        assertEquals(Color(fidelityScheme.inversePrimary), companyScheme.scheme.inversePrimary)
//        assertEquals(Color(fidelityScheme.secondary), companyScheme.scheme.secondary)
//        assertEquals(Color(fidelityScheme.onSecondary), companyScheme.scheme.onSecondary)
//        assertEquals(Color(fidelityScheme.secondaryContainer), companyScheme.scheme.secondaryContainer)
//        assertEquals(Color(fidelityScheme.onSecondaryContainer), companyScheme.scheme.onSecondaryContainer)
//        assertEquals(Color(fidelityScheme.tertiary), companyScheme.scheme.tertiary)
//        assertEquals(Color(fidelityScheme.onTertiary), companyScheme.scheme.onTertiary)
//        assertEquals(Color(fidelityScheme.tertiaryContainer), companyScheme.scheme.tertiaryContainer)
//        assertEquals(Color(fidelityScheme.onTertiaryContainer), companyScheme.scheme.onTertiaryContainer)
//        assertEquals(Color(monochromeScheme.background), companyScheme.scheme.background)
//        assertEquals(Color(monochromeScheme.onBackground), companyScheme.scheme.onBackground)
//        assertEquals(Color(monochromeScheme.onBackground), companyScheme.scheme.onBackground)
//        assertEquals(Color(monochromeScheme.surface), companyScheme.scheme.surface)
//        assertEquals(Color(monochromeScheme.onSurface), companyScheme.scheme.onSurface)
//        assertEquals(Color(monochromeScheme.surfaceVariant), companyScheme.scheme.surfaceVariant)
//        assertEquals(Color(monochromeScheme.onSurfaceVariant), companyScheme.scheme.onSurfaceVariant)
//        assertEquals(Color(monochromeScheme.surfaceTint), companyScheme.scheme.surfaceTint)
//        assertEquals(Color(monochromeScheme.inverseSurface), companyScheme.scheme.inverseSurface)
//        assertEquals(Color(monochromeScheme.inverseOnSurface), companyScheme.scheme.inverseOnSurface)
//        assertEquals(Color(monochromeScheme.error), companyScheme.scheme.error)
//        assertEquals(Color(monochromeScheme.onError), companyScheme.scheme.onError)
//        assertEquals(Color(fidelityScheme.errorContainer), companyScheme.scheme.errorContainer)
//        assertEquals(Color(fidelityScheme.onErrorContainer), companyScheme.scheme.onErrorContainer)
//        assertEquals(Color(monochromeScheme.outline), companyScheme.scheme.outline)
//        assertEquals(Color(monochromeScheme.outlineVariant), companyScheme.scheme.outlineVariant)
//        assertEquals(Color(monochromeScheme.scrim), companyScheme.scheme.scrim)
//        assertEquals(Color(monochromeScheme.surfaceBright), companyScheme.scheme.surfaceBright)
//        assertEquals(Color(monochromeScheme.surfaceContainer), companyScheme.scheme.surfaceContainer)
//        assertEquals(Color(monochromeScheme.surfaceContainerHigh), companyScheme.scheme.surfaceContainerHigh)
//        assertEquals(Color(monochromeScheme.surfaceContainerHighest), companyScheme.scheme.surfaceContainerHighest)
//        assertEquals(Color(monochromeScheme.surfaceContainerLow), companyScheme.scheme.surfaceContainerLow)
//        assertEquals(Color(monochromeScheme.surfaceContainerLowest), companyScheme.scheme.surfaceContainerLowest)
//        assertEquals(Color(monochromeScheme.surfaceDim), companyScheme.scheme.surfaceDim)
//    }
//
//    @Test
//    fun seedColorWithSystemLightTheme_generateLightCompanyScheme() {
//        val daySeed = Color.Red
//        val nightSeed = Color.Blue
//        val theme = CompanyUI.Theme(
//            defaultStyle = CompanyUI.Theme.DefaultStyle.System,
//            day = CompanyUI.Theme.Style(
//                colors = CompanyUI.Theme.Colors.Seed(daySeed.toArgb())
//            ),
//            night = CompanyUI.Theme.Style(
//                colors = CompanyUI.Theme.Colors.Seed(nightSeed.toArgb())
//            )
//        )
//        val fidelityScheme = SchemeFidelity(Hct.fromInt(daySeed.toArgb()), false, .0)
//        val monochromeScheme = SchemeMonochrome(Hct.fromInt(daySeed.toArgb()), false, .0)
//
//        val generator = CompanySchemeGenerator()
//        val companyScheme = generator.getCompanyScheme(theme, false)
//
//        assertEquals(false, companyScheme.isDark)
//        assertEquals(daySeed, companyScheme.scheme.primary)
//        assertEquals(Color(fidelityScheme.onPrimaryContainer), companyScheme.scheme.onPrimary)
//        assertEquals(Color(fidelityScheme.primaryContainer), companyScheme.scheme.primaryContainer)
//        assertEquals(Color(fidelityScheme.onPrimaryContainer), companyScheme.scheme.onPrimaryContainer)
//        assertEquals(Color(fidelityScheme.inversePrimary), companyScheme.scheme.inversePrimary)
//        assertEquals(Color(fidelityScheme.secondary), companyScheme.scheme.secondary)
//        assertEquals(Color(fidelityScheme.onSecondary), companyScheme.scheme.onSecondary)
//        assertEquals(Color(fidelityScheme.secondaryContainer), companyScheme.scheme.secondaryContainer)
//        assertEquals(Color(fidelityScheme.onSecondaryContainer), companyScheme.scheme.onSecondaryContainer)
//        assertEquals(Color(fidelityScheme.tertiary), companyScheme.scheme.tertiary)
//        assertEquals(Color(fidelityScheme.onTertiary), companyScheme.scheme.onTertiary)
//        assertEquals(Color(fidelityScheme.tertiaryContainer), companyScheme.scheme.tertiaryContainer)
//        assertEquals(Color(fidelityScheme.onTertiaryContainer), companyScheme.scheme.onTertiaryContainer)
//        assertEquals(Color(monochromeScheme.background), companyScheme.scheme.background)
//        assertEquals(Color(monochromeScheme.onBackground), companyScheme.scheme.onBackground)
//        assertEquals(Color(monochromeScheme.onBackground), companyScheme.scheme.onBackground)
//        assertEquals(Color(monochromeScheme.surface), companyScheme.scheme.surface)
//        assertEquals(Color(monochromeScheme.onSurface), companyScheme.scheme.onSurface)
//        assertEquals(Color(monochromeScheme.surfaceVariant), companyScheme.scheme.surfaceVariant)
//        assertEquals(Color(monochromeScheme.onSurfaceVariant), companyScheme.scheme.onSurfaceVariant)
//        assertEquals(Color(monochromeScheme.surfaceTint), companyScheme.scheme.surfaceTint)
//        assertEquals(Color(monochromeScheme.inverseSurface), companyScheme.scheme.inverseSurface)
//        assertEquals(Color(monochromeScheme.inverseOnSurface), companyScheme.scheme.inverseOnSurface)
//        assertEquals(Color(monochromeScheme.error), companyScheme.scheme.error)
//        assertEquals(Color(monochromeScheme.onError), companyScheme.scheme.onError)
//        assertEquals(Color(fidelityScheme.errorContainer), companyScheme.scheme.errorContainer)
//        assertEquals(Color(fidelityScheme.onErrorContainer), companyScheme.scheme.onErrorContainer)
//        assertEquals(Color(monochromeScheme.outline), companyScheme.scheme.outline)
//        assertEquals(Color(monochromeScheme.outlineVariant), companyScheme.scheme.outlineVariant)
//        assertEquals(Color(monochromeScheme.scrim), companyScheme.scheme.scrim)
//        assertEquals(Color(monochromeScheme.surfaceBright), companyScheme.scheme.surfaceBright)
//        assertEquals(Color(monochromeScheme.surfaceContainer), companyScheme.scheme.surfaceContainer)
//        assertEquals(Color(monochromeScheme.surfaceContainerHigh), companyScheme.scheme.surfaceContainerHigh)
//        assertEquals(Color(monochromeScheme.surfaceContainerHighest), companyScheme.scheme.surfaceContainerHighest)
//        assertEquals(Color(monochromeScheme.surfaceContainerLow), companyScheme.scheme.surfaceContainerLow)
//        assertEquals(Color(monochromeScheme.surfaceContainerLowest), companyScheme.scheme.surfaceContainerLowest)
//        assertEquals(Color(monochromeScheme.surfaceDim), companyScheme.scheme.surfaceDim)
//    }
//
//    @Test
//    fun seedColorWithSystemDarkTheme_generateDarkCompanyScheme() {
//        val daySeed = Color.Red
//        val nightSeed = Color.Blue
//        val theme = CompanyUI.Theme(
//            defaultStyle = CompanyUI.Theme.DefaultStyle.System,
//            day = CompanyUI.Theme.Style(
//                colors = CompanyUI.Theme.Colors.Seed(daySeed.toArgb())
//            ),
//            night = CompanyUI.Theme.Style(
//                colors = CompanyUI.Theme.Colors.Seed(nightSeed.toArgb())
//            )
//        )
//        val fidelityScheme = SchemeFidelity(Hct.fromInt(nightSeed.toArgb()), true, .0)
//        val monochromeScheme = SchemeMonochrome(Hct.fromInt(nightSeed.toArgb()), true, .0)
//
//        val generator = CompanySchemeGenerator()
//        val companyScheme = generator.getCompanyScheme(theme, true)
//
//        assertEquals(true, companyScheme.isDark)
//        assertEquals(nightSeed, companyScheme.scheme.primary)
//        assertEquals(Color(fidelityScheme.onPrimaryContainer), companyScheme.scheme.onPrimary)
//        assertEquals(Color(fidelityScheme.primaryContainer), companyScheme.scheme.primaryContainer)
//        assertEquals(Color(fidelityScheme.onPrimaryContainer), companyScheme.scheme.onPrimaryContainer)
//        assertEquals(Color(fidelityScheme.inversePrimary), companyScheme.scheme.inversePrimary)
//        assertEquals(Color(fidelityScheme.secondary), companyScheme.scheme.secondary)
//        assertEquals(Color(fidelityScheme.onSecondary), companyScheme.scheme.onSecondary)
//        assertEquals(Color(fidelityScheme.secondaryContainer), companyScheme.scheme.secondaryContainer)
//        assertEquals(Color(fidelityScheme.onSecondaryContainer), companyScheme.scheme.onSecondaryContainer)
//        assertEquals(Color(fidelityScheme.tertiary), companyScheme.scheme.tertiary)
//        assertEquals(Color(fidelityScheme.onTertiary), companyScheme.scheme.onTertiary)
//        assertEquals(Color(fidelityScheme.tertiaryContainer), companyScheme.scheme.tertiaryContainer)
//        assertEquals(Color(fidelityScheme.onTertiaryContainer), companyScheme.scheme.onTertiaryContainer)
//        assertEquals(Color(monochromeScheme.background), companyScheme.scheme.background)
//        assertEquals(Color(monochromeScheme.onBackground), companyScheme.scheme.onBackground)
//        assertEquals(Color(monochromeScheme.onBackground), companyScheme.scheme.onBackground)
//        assertEquals(Color(monochromeScheme.surface), companyScheme.scheme.surface)
//        assertEquals(Color(monochromeScheme.onSurface), companyScheme.scheme.onSurface)
//        assertEquals(Color(monochromeScheme.surfaceVariant), companyScheme.scheme.surfaceVariant)
//        assertEquals(Color(monochromeScheme.onSurfaceVariant), companyScheme.scheme.onSurfaceVariant)
//        assertEquals(Color(monochromeScheme.surfaceTint), companyScheme.scheme.surfaceTint)
//        assertEquals(Color(monochromeScheme.inverseSurface), companyScheme.scheme.inverseSurface)
//        assertEquals(Color(monochromeScheme.inverseOnSurface), companyScheme.scheme.inverseOnSurface)
//        assertEquals(Color(monochromeScheme.error), companyScheme.scheme.error)
//        assertEquals(Color(monochromeScheme.onError), companyScheme.scheme.onError)
//        assertEquals(Color(fidelityScheme.errorContainer), companyScheme.scheme.errorContainer)
//        assertEquals(Color(fidelityScheme.onErrorContainer), companyScheme.scheme.onErrorContainer)
//        assertEquals(Color(monochromeScheme.outline), companyScheme.scheme.outline)
//        assertEquals(Color(monochromeScheme.outlineVariant), companyScheme.scheme.outlineVariant)
//        assertEquals(Color(monochromeScheme.scrim), companyScheme.scheme.scrim)
//        assertEquals(Color(monochromeScheme.surfaceBright), companyScheme.scheme.surfaceBright)
//        assertEquals(Color(monochromeScheme.surfaceContainer), companyScheme.scheme.surfaceContainer)
//        assertEquals(Color(monochromeScheme.surfaceContainerHigh), companyScheme.scheme.surfaceContainerHigh)
//        assertEquals(Color(monochromeScheme.surfaceContainerHighest), companyScheme.scheme.surfaceContainerHighest)
//        assertEquals(Color(monochromeScheme.surfaceContainerLow), companyScheme.scheme.surfaceContainerLow)
//        assertEquals(Color(monochromeScheme.surfaceContainerLowest), companyScheme.scheme.surfaceContainerLowest)
//        assertEquals(Color(monochromeScheme.surfaceDim), companyScheme.scheme.surfaceDim)
//    }
//}