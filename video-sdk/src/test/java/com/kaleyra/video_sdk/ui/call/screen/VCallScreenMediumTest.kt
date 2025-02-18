package com.kaleyra.video_sdk.ui.call.screen

import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.test.ext.junit.rules.ActivityScenarioRule
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.call.appbar.model.CallAppBarUiState
import com.kaleyra.video_sdk.call.appbar.viewmodel.CallAppBarViewModel
import com.kaleyra.video_sdk.call.audiooutput.model.AudioOutputUiState
import com.kaleyra.video_sdk.call.audiooutput.viewmodel.AudioOutputViewModel
import com.kaleyra.video_sdk.call.bottomsheet.CallSheetState
import com.kaleyra.video_sdk.call.bottomsheet.model.AudioAction
import com.kaleyra.video_sdk.call.bottomsheet.model.CameraAction
import com.kaleyra.video_sdk.call.bottomsheet.model.ChatAction
import com.kaleyra.video_sdk.call.bottomsheet.model.FileShareAction
import com.kaleyra.video_sdk.call.bottomsheet.model.FlipCameraAction
import com.kaleyra.video_sdk.call.bottomsheet.model.HangUpAction
import com.kaleyra.video_sdk.call.bottomsheet.model.MicAction
import com.kaleyra.video_sdk.call.bottomsheet.model.ScreenShareAction
import com.kaleyra.video_sdk.call.bottomsheet.model.VirtualBackgroundAction
import com.kaleyra.video_sdk.call.bottomsheet.model.WhiteboardAction
import com.kaleyra.video_sdk.call.brandlogo.model.BrandLogoState
import com.kaleyra.video_sdk.call.brandlogo.model.Logo
import com.kaleyra.video_sdk.call.brandlogo.viewmodel.BrandLogoViewModel
import com.kaleyra.video_sdk.call.callactions.model.CallActionsUiState
import com.kaleyra.video_sdk.call.callactions.viewmodel.CallActionsViewModel
import com.kaleyra.video_sdk.call.callinfo.model.CallInfoUiState
import com.kaleyra.video_sdk.call.callinfo.model.TextRef
import com.kaleyra.video_sdk.call.callinfo.viewmodel.CallInfoViewModel
import com.kaleyra.video_sdk.call.fileshare.model.FileShareUiState
import com.kaleyra.video_sdk.call.fileshare.viewmodel.FileShareViewModel
import com.kaleyra.video_sdk.call.participants.model.ParticipantsUiState
import com.kaleyra.video_sdk.call.participants.viewmodel.ParticipantsViewModel
import com.kaleyra.video_sdk.call.screen.model.CallStateUi
import com.kaleyra.video_sdk.call.screen.model.InputPermissions
import com.kaleyra.video_sdk.call.screen.model.ModularComponent
import com.kaleyra.video_sdk.call.screen.view.vcallscreen.VCallScreen
import com.kaleyra.video_sdk.call.screenshare.model.ScreenShareUiState
import com.kaleyra.video_sdk.call.screenshare.viewmodel.ScreenShareViewModel
import com.kaleyra.video_sdk.call.stream.model.StreamUiState
import com.kaleyra.video_sdk.call.stream.viewmodel.StreamViewModel
import com.kaleyra.video_sdk.call.virtualbackground.model.VirtualBackgroundUiState
import com.kaleyra.video_sdk.call.virtualbackground.viewmodel.VirtualBackgroundViewModel
import com.kaleyra.video_sdk.call.whiteboard.model.WhiteboardUiState
import com.kaleyra.video_sdk.call.whiteboard.viewmodel.WhiteboardViewModel
import com.kaleyra.video_sdk.common.usermessages.model.StackedSnackbarUiState
import com.kaleyra.video_sdk.common.usermessages.viewmodel.UserMessagesViewModel
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.reflect.KClass

@Config(qualifiers = "w820dp-h900dp")
@RunWith(RobolectricTestRunner::class)
internal class VCallScreenMediumTest: VCallScreenBaseTest() {

    @Test
    fun largeDevice_companyLogoSet_callDisconnected_brandLogoDisplayed() = runTest {
        brandLogoUiState.emit(BrandLogoState(callStateUi = CallStateUi.Disconnected.Companion, logo = companyLogo))
        composeTestRule.setUpVCallScreen()
        composeTestRule.waitForIdle()

        val companyLogo = composeTestRule.activity.getString(R.string.kaleyra_company_logo)
        composeTestRule.onNodeWithContentDescription(companyLogo, useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun largeDevice_companyLogoSet_callEndedNeverConnected_brandLogoDisplayed() = runTest {
        brandLogoUiState.emit(BrandLogoState(callStateUi = CallStateUi.Connecting, logo = companyLogo))
        brandLogoUiState.emit(BrandLogoState(callStateUi = CallStateUi.Disconnected.Ended, logo = companyLogo))

        composeTestRule.setUpVCallScreen()
        composeTestRule.waitForIdle()

        val companyLogo = composeTestRule.activity.getString(R.string.kaleyra_company_logo)
        composeTestRule.onNodeWithContentDescription(companyLogo, useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun largeDevice_companyLogoSet_callDisconnectingNeverConnected_brandLogoDisplayed() = runTest {
        brandLogoUiState.emit(BrandLogoState(callStateUi = CallStateUi.Connecting, logo = companyLogo))
        brandLogoUiState.emit(BrandLogoState(callStateUi = CallStateUi.Disconnecting, logo = companyLogo))

        composeTestRule.setUpVCallScreen()
        composeTestRule.waitForIdle()

        val companyLogo = composeTestRule.activity.getString(R.string.kaleyra_company_logo)
        composeTestRule.onNodeWithContentDescription(companyLogo, useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun largeDevice_companyLogoSet_callConnected_brandLogoDisplayed() = runTest {
        val logo = Logo(light = Uri.parse("https://www.example.com/light.png"), dark = Uri.parse("https://www.example.com/dark.png"))
        brandLogoUiState.emit(BrandLogoState(callStateUi = CallStateUi.Connected, logo = logo))

        composeTestRule.setUpVCallScreen()
        composeTestRule.waitForIdle()

        val companyLogo = composeTestRule.activity.getString(R.string.kaleyra_company_logo)
        composeTestRule.onNodeWithContentDescription(companyLogo, useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun largeDevice_companyLogoSet_callReconnecting_brandLogoDisplayed() = runTest {
        brandLogoUiState.emit(BrandLogoState(callStateUi = CallStateUi.Connected, logo = companyLogo))
        brandLogoUiState.emit(BrandLogoState(callStateUi = CallStateUi.Reconnecting, logo = companyLogo))

        composeTestRule.setUpVCallScreen()
        composeTestRule.waitForIdle()

        val companyLogo = composeTestRule.activity.getString(R.string.kaleyra_company_logo)
        composeTestRule.onNodeWithContentDescription(companyLogo, useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun largeDevice_companyLogoNotSet_brandLogoNotDisplayed() = runTest {
        brandLogoUiState.emit(BrandLogoState())
        composeTestRule.setUpVCallScreen()
        composeTestRule.waitForIdle()

        val companyLogo = composeTestRule.activity.getString(R.string.kaleyra_company_logo)
        composeTestRule.onNodeWithContentDescription(companyLogo).assertDoesNotExist()
    }
}