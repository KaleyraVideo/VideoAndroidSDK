package com.kaleyra.video_sdk.ui.call.logo

import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.call.brandlogo.model.BrandLogoState
import com.kaleyra.video_sdk.call.brandlogo.model.Logo
import com.kaleyra.video_sdk.call.brandlogo.view.BrandLogoComponent
import com.kaleyra.video_sdk.call.brandlogo.viewmodel.BrandLogoViewModel
import com.kaleyra.video_sdk.call.screen.model.CallStateUi
import com.kaleyra.video_sdk.call.screen.view.vcallscreen.shouldDisplayBrandLogo
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class BrandLogoComponentTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()
    private val brandLogoViewModel = mockk<BrandLogoViewModel>()

    @Test
    fun companyUriLogoNull_brandLogoNotDisplayed() {
        every { brandLogoViewModel.uiState } returns MutableStateFlow(
            BrandLogoState(logo = Logo())
        )
        composeTestRule.setContent {
            BrandLogoComponent(viewModel = brandLogoViewModel)
        }

        val text = composeTestRule.activity.getString(R.string.kaleyra_company_logo)

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithContentDescription(text).assertIsNotDisplayed()
    }

    @Test
    fun companyUriLogoEmpty_brandLogoNotDisplayed() {
        every { brandLogoViewModel.uiState } returns MutableStateFlow(
            BrandLogoState(
                logo = Logo(
                    light = Uri.EMPTY,
                    dark = Uri.EMPTY
                )
            )
        )
        composeTestRule.setContent {
            BrandLogoComponent(viewModel = brandLogoViewModel)
        }

        val text = composeTestRule.activity.getString(R.string.kaleyra_company_logo)

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithContentDescription(text).assertIsNotDisplayed()
    }

    @Test
    fun companyUriLogoSet_brandLogoNotDisplayed() {
        every { brandLogoViewModel.uiState } returns MutableStateFlow(
            BrandLogoState(
                logo = Logo(
                    light = Uri.parse("https://www.example.com/logoLight.png"),
                    dark = Uri.parse("https://www.example.com/logoDark.png")
                )
            )
        )
        composeTestRule.setContent {
            BrandLogoComponent(viewModel = brandLogoViewModel)
        }
        val text = composeTestRule.activity.getString(R.string.kaleyra_company_logo)

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithContentDescription(text).assertIsDisplayed()
    }

    @Test
    fun callStateUiDisconnected_shouldDisplayBrandLogo() {
        Assert.assertEquals(true, shouldDisplayBrandLogo(CallStateUi.Disconnected, false))
    }

    @Test
    fun callStateUiConnecting_shouldDisplayBrandLogo() {
        Assert.assertEquals(true, shouldDisplayBrandLogo(CallStateUi.Connecting, false))
    }

    @Test
    fun callStateUiDialing_shouldDisplayBrandLogo() {
        Assert.assertEquals(true, shouldDisplayBrandLogo(CallStateUi.Dialing, false))
    }

    @Test
    fun callStateUiRingingRemotely_shouldNotDisplayBrandLogo() {
        Assert.assertEquals(true, shouldDisplayBrandLogo(CallStateUi.RingingRemotely, false))
    }

    @Test
    fun callStateUiNeverBeenConnected_shouldNotDisplayBrandLogo() {
        Assert.assertEquals(false, shouldDisplayBrandLogo(CallStateUi.Disconnected.Ended, true))
    }
}
