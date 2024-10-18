package com.kaleyra.video_common_ui.company

import android.net.Uri
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.kaleyra.video.Company
import com.kaleyra.video_common_ui.CompanyUI
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class CompanyUITest {

    @Test
    fun testThemeMapping() = runTest {
        val remoteDayColors = Company.Theme.Style.Colors.Seed(Color.Red.toArgb())
        val remoteNightColors = Company.Theme.Style.Colors.Seed(Color.Blue.toArgb())
        val remoteDayStyle = object : Company.Theme.Style {
            override val logo: Uri = mockk<Uri>()
            override val colors: Company.Theme.Style.Colors = remoteDayColors
        }
        val remoteNightStyle = object : Company.Theme.Style {
            override val logo: Uri = mockk<Uri>()
            override val colors: Company.Theme.Style.Colors = remoteNightColors
        }
        val remoteTheme = object : Company.Theme {
            override val day: Company.Theme.Style = remoteDayStyle
            override val night: Company.Theme.Style = remoteNightStyle
        }

        val company = mockk<Company>(relaxed = true) {
            every { theme } returns MutableStateFlow(remoteTheme)
        }
        val companyUI = CompanyUI(company = company, coroutineScope = backgroundScope)

        val expected = CompanyUI.Theme(
            day = CompanyUI.Theme.Style(remoteDayStyle.logo, CompanyUI.Theme.Colors.Seed(remoteDayColors.color)),
            night = CompanyUI.Theme.Style(remoteNightStyle.logo, CompanyUI.Theme.Colors.Seed(remoteNightColors.color))
        )
        assertEquals(expected, companyUI.theme.first())
    }
}