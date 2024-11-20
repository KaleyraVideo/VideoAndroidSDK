/*
 * Copyright 2023 Kaleyra @ https://www.kaleyra.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kaleyra.video_common_ui.theme

import android.net.Uri
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontFamily
import com.kaleyra.video.Company
import com.kaleyra.video_common_ui.CompanyUI
import com.kaleyra.video_common_ui.CompanyUI.Theme.Style
import com.kaleyra.video_common_ui.KaleyraVideo
import com.kaleyra.video_common_ui.theme.CompanyThemeManager.combinedTheme
import com.kaleyra.video_common_ui.theme.resource.ColorResource
import com.kaleyra.video_common_ui.theme.resource.URIResource
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class CompanyThemeManagerTest {

    private val company = mockk<Company>()
    private val theme = mockk<Company.Theme>()

    private val remoteLightLogo = mockk<Uri>()
    private val remoteDarkLogo = mockk<Uri>()

    private val remoteLightColors =  Company.Theme.Style.Colors.Seed(Color.Red.toArgb())
    private val remoteDarkColors =  Company.Theme.Style.Colors.Seed(Color.Blue.toArgb())

    private val remoteLightStyle = object : Company.Theme.Style {
        override val logo: Uri = remoteLightLogo
        override val colors: Company.Theme.Style.Colors = remoteLightColors
    }

    private val remoteDarkStyle = object : Company.Theme.Style {
        override val logo: Uri = remoteDarkLogo
        override val colors: Company.Theme.Style.Colors = remoteDarkColors
    }

    @Before
    fun setUp() {
        mockkObject(KaleyraVideo)
        every { company.theme } returns MutableStateFlow(theme)
        every { theme.day } returns remoteLightStyle
        every { theme.night } returns remoteDarkStyle
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `remote company details`() = runTest {
        every { KaleyraVideo.theme } returns null
        val expected = Theme(
            logo = Theme.Logo(URIResource(remoteLightLogo, remoteDarkLogo)),
            palette = Theme.Palette(ColorResource(remoteLightColors.color, remoteDarkColors.color)),
        )
        assertEquals(expected, company.combinedTheme.first())
    }

    @Test
    fun `set legacy local company theme`() = runTest {
        val lightLogo = mockk<Uri>()
        val darkLogo = mockk<Uri>()
        val companyUITheme = CompanyUI.Theme(
            fontFamily = FontFamily.Serif,
            defaultStyle = CompanyUI.Theme.DefaultStyle.Day,
            day = Style(
                logo = lightLogo,
                colors = CompanyUI.Theme.Colors.Seed(color = Color.Red.toArgb())
            ),
            night = Style(
                logo = darkLogo,
                colors = CompanyUI.Theme.Colors.Seed(color = Color.Yellow.toArgb())
            )
        )
        every { KaleyraVideo.theme } returns companyUITheme

        val expected = Theme(
            logo = Theme.Logo(URIResource(lightLogo, darkLogo)),
            palette = Theme.Palette(ColorResource(Color.Red.toArgb(), Color.Yellow.toArgb())),
            typography = Theme.Typography(FontFamily.Serif),
            config = Theme.Config(Theme.Config.Style.Light)
        )
        assertEquals(expected, company.combinedTheme.first())
    }

    @Test
    fun `set local company theme`() = runTest {
        val lightLogo = mockk<Uri>()
        val darkLogo = mockk<Uri>()
        val companyUITheme = Theme(
            logo = Theme.Logo(URIResource(lightLogo, darkLogo)),
            palette = Theme.Palette(ColorResource(Color.Red.toArgb(), Color.Yellow.toArgb())),
            typography = Theme.Typography(FontFamily.Serif),
            config = Theme.Config(Theme.Config.Style.Light)
        )
        every { KaleyraVideo.theme } returns companyUITheme

        assertEquals(companyUITheme, company.combinedTheme.first())
    }

    @Test
    fun `legacy theme uses remote light logo if local light logo is not defined`() = runTest {
        val darkLogo = mockk<Uri>()
        val companyUITheme = CompanyUI.Theme(
            fontFamily = FontFamily.Cursive,
            defaultStyle = CompanyUI.Theme.DefaultStyle.Night,
            day = Style(
                logo = null,
                colors = CompanyUI.Theme.Colors.Seed(color = Color.Blue.toArgb())
            ),
            night = Style(
                logo = darkLogo,
                colors = CompanyUI.Theme.Colors.Seed(color = Color.Black.toArgb())
            )
        )
        every { KaleyraVideo.theme } returns companyUITheme
        val expected = Theme(
            logo = Theme.Logo(URIResource(remoteLightLogo, darkLogo)),
            palette = Theme.Palette(ColorResource(Color.Blue.toArgb(), Color.Black.toArgb())),
            typography = Theme.Typography(FontFamily.Cursive),
            config = Theme.Config(Theme.Config.Style.Dark)
        )
        assertEquals(expected, company.combinedTheme.drop(1).first())
    }

    @Test
    fun `legacy theme uses remote dark logo if local dark logo is not defined`() = runTest {
        val lightLogo = mockk<Uri>()
        val companyUITheme = CompanyUI.Theme(
            fontFamily = FontFamily.Cursive,
            defaultStyle = CompanyUI.Theme.DefaultStyle.Night,
            day = Style(
                logo = lightLogo,
                colors = CompanyUI.Theme.Colors.Seed(color = Color.Blue.toArgb())
            ),
            night = Style(
                logo = null,
                colors = CompanyUI.Theme.Colors.Seed(color = Color.Black.toArgb())
            )
        )
        every { KaleyraVideo.theme } returns companyUITheme
        val expected = Theme(
            logo = Theme.Logo(URIResource(lightLogo, remoteDarkLogo)),
            palette = Theme.Palette(ColorResource(Color.Blue.toArgb(), Color.Black.toArgb())),
            typography = Theme.Typography(FontFamily.Cursive),
            config = Theme.Config(Theme.Config.Style.Dark)
        )
        assertEquals(expected, company.combinedTheme.drop(1).first())
    }

    @Test
    fun `legacy theme uses remote light colors if local light colors are not defined`() = runTest {
        val lightLogo = mockk<Uri>()
        val darkLogo = mockk<Uri>()
        val companyUITheme = CompanyUI.Theme(
            fontFamily = FontFamily.SansSerif,
            defaultStyle = CompanyUI.Theme.DefaultStyle.System,
            day = Style(logo = lightLogo, colors = null),
            night = Style(logo = darkLogo, colors = CompanyUI.Theme.Colors.Seed(color = Color.Black.toArgb()))
        )
        every { KaleyraVideo.theme } returns companyUITheme
        val expected = Theme(
            logo = Theme.Logo(URIResource(lightLogo, darkLogo)),
            palette = Theme.Palette(ColorResource(remoteLightColors.color, Color.Black.toArgb())),
            typography = Theme.Typography(FontFamily.SansSerif),
            config = Theme.Config(Theme.Config.Style.System)
        )
        assertEquals(expected, company.combinedTheme.drop(1).first())
    }

    @Test
    fun `legacy theme uses remote dark colors if local dark colors are not defined`() = runTest {
        val lightLogo = mockk<Uri>()
        val darkLogo = mockk<Uri>()
        val companyUITheme = CompanyUI.Theme(
            fontFamily = FontFamily.SansSerif,
            defaultStyle = CompanyUI.Theme.DefaultStyle.System,
            day = Style(logo = lightLogo, colors = CompanyUI.Theme.Colors.Seed(color = Color.Black.toArgb())),
            night = Style(logo = darkLogo, colors = null)
        )
        every { KaleyraVideo.theme } returns companyUITheme
        val expected = Theme(
            logo = Theme.Logo(URIResource(lightLogo, darkLogo)),
            palette = Theme.Palette(ColorResource(Color.Black.toArgb(), remoteDarkColors.color)),
            typography = Theme.Typography(FontFamily.SansSerif),
            config = Theme.Config(Theme.Config.Style.System)
        )
        assertEquals(expected, company.combinedTheme.drop(1).first())
    }

    @Test
    fun `legacy theme uses kaleyra light colors if local and remote light colors are not defined`() = runTest {
        val lightLogo = mockk<Uri>()
        val darkLogo = mockk<Uri>()
        val companyUITheme = CompanyUI.Theme(
            fontFamily = FontFamily.SansSerif,
            defaultStyle = CompanyUI.Theme.DefaultStyle.System,
            day = Style(logo = lightLogo, colors = null),
            night = Style(logo = darkLogo, colors = CompanyUI.Theme.Colors.Seed(color = Color.Black.toArgb()))
        )
        every { KaleyraVideo.theme } returns companyUITheme
        every { company.theme } returns MutableStateFlow(mockk<Company.Theme>(relaxed = true))
        val expected = Theme(
            logo = Theme.Logo(URIResource(lightLogo, darkLogo)),
            palette = Theme.Palette(ColorResource(KaleyraPaletteSeed, Color.Black.toArgb())),
            typography = Theme.Typography(FontFamily.SansSerif),
            config = Theme.Config(Theme.Config.Style.System)
        )
        assertEquals(expected, company.combinedTheme.drop(1).first())
    }

    @Test
    fun `legacy theme uses kaleyra dark colors if local and remote dark colors are not defined`() = runTest {
        val lightLogo = mockk<Uri>()
        val darkLogo = mockk<Uri>()
        val companyUITheme = CompanyUI.Theme(
            fontFamily = FontFamily.SansSerif,
            defaultStyle = CompanyUI.Theme.DefaultStyle.System,
            day = Style(logo = lightLogo, colors = CompanyUI.Theme.Colors.Seed(color = Color.Black.toArgb())),
            night = Style(logo = darkLogo, colors = null)
        )
        every { KaleyraVideo.theme } returns companyUITheme
        every { company.theme } returns MutableStateFlow(mockk<Company.Theme>(relaxed = true))
        val expected = Theme(
            logo = Theme.Logo(URIResource(lightLogo, darkLogo)),
            palette = Theme.Palette(ColorResource(Color.Black.toArgb(), KaleyraPaletteSeed)),
            typography = Theme.Typography(FontFamily.SansSerif),
            config = Theme.Config(Theme.Config.Style.System)
        )
        assertEquals(expected, company.combinedTheme.drop(1).first())
    }

    @Test
    fun `theme uses remote logo if local logo is not defined`() = runTest {
        val companyUITheme = Theme(
            logo = null,
            palette = Theme.Palette(ColorResource(Color.Blue.toArgb(), Color.Black.toArgb())),
            typography = Theme.Typography(FontFamily.Cursive),
            config = Theme.Config(Theme.Config.Style.Dark)
        )
        every { KaleyraVideo.theme } returns companyUITheme
        val expected = Theme(
            logo = Theme.Logo(URIResource(remoteLightLogo, remoteDarkLogo)),
            palette = Theme.Palette(ColorResource(Color.Blue.toArgb(), Color.Black.toArgb())),
            typography = Theme.Typography(FontFamily.Cursive),
            config = Theme.Config(Theme.Config.Style.Dark)
        )
        assertEquals(expected, company.combinedTheme.drop(1).first())
    }

    @Test
    fun `theme uses remote colors if local colors are not defined`() = runTest {
        val lightLogo = mockk<Uri>()
        val darkLogo = mockk<Uri>()
        val companyUITheme = Theme(
            logo = Theme.Logo(URIResource(lightLogo, darkLogo)),
            palette = null,
            typography = Theme.Typography(FontFamily.SansSerif),
            config = Theme.Config(Theme.Config.Style.System)
        )
        every { KaleyraVideo.theme } returns companyUITheme
        val expected = Theme(
            logo = Theme.Logo(URIResource(lightLogo, darkLogo)),
            palette = Theme.Palette(ColorResource(remoteLightColors.color, remoteDarkColors.color)),
            typography = Theme.Typography(FontFamily.SansSerif),
            config = Theme.Config(Theme.Config.Style.System)
        )
        assertEquals(expected, company.combinedTheme.drop(1).first())
    }

    @Test
    fun `theme uses kaleyra light colors if local and remote light colors are not defined`() = runTest {
        val lightLogo = mockk<Uri>()
        val darkLogo = mockk<Uri>()
        val companyUITheme = Theme(
            logo = Theme.Logo(URIResource(lightLogo, darkLogo)),
            palette = null,
            typography = Theme.Typography(FontFamily.SansSerif),
            config = Theme.Config(Theme.Config.Style.System)
        )
        every { KaleyraVideo.theme } returns companyUITheme
        every { theme.day } returns object : Company.Theme.Style {
            override val logo: Uri? = null
            override val colors: Company.Theme.Style.Colors? = null
        }
        val expected = Theme(
            logo = Theme.Logo(URIResource(lightLogo, darkLogo)),
            palette = Theme.Palette(ColorResource(KaleyraPaletteSeed, remoteDarkColors.color)),
            typography = Theme.Typography(FontFamily.SansSerif),
            config = Theme.Config(Theme.Config.Style.System)
        )
        assertEquals(expected, company.combinedTheme.drop(1).first())
    }

    @Test
    fun `theme uses kaleyra dark colors if local and remote dark colors are not defined`() = runTest {
        val lightLogo = mockk<Uri>()
        val darkLogo = mockk<Uri>()
        val companyUITheme = Theme(
            logo = Theme.Logo(URIResource(lightLogo, darkLogo)),
            palette = null,
            typography = Theme.Typography(FontFamily.SansSerif),
            config = Theme.Config(Theme.Config.Style.System)
        )
        every { KaleyraVideo.theme } returns companyUITheme
        every { theme.night } returns object : Company.Theme.Style {
            override val logo: Uri? = null
            override val colors: Company.Theme.Style.Colors? = null
        }
        val expected = Theme(
            logo = Theme.Logo(URIResource(lightLogo, darkLogo)),
            palette = Theme.Palette(ColorResource(remoteLightColors.color, KaleyraPaletteSeed)),
            typography = Theme.Typography(FontFamily.SansSerif),
            config = Theme.Config(Theme.Config.Style.System)
        )
        assertEquals(expected, company.combinedTheme.drop(1).first())
    }
}