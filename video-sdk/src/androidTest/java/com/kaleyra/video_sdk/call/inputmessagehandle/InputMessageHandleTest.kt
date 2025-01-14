package com.kaleyra.video_sdk.call.inputmessagehandle

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import com.kaleyra.video_common_ui.CollaborationViewModel
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.call.bottomsheet.view.inputmessage.view.InputMessageDuration
import com.kaleyra.video_sdk.call.callactions.viewmodel.CallActionsViewModel
import com.kaleyra.video_sdk.call.screen.view.vcallscreen.InputMessageDragHandleTag
import com.kaleyra.video_sdk.call.screen.view.vcallscreen.InputMessageHandle
import com.kaleyra.video_sdk.common.usermessages.model.CameraMessage
import com.kaleyra.video_sdk.common.usermessages.model.FullScreenMessage
import com.kaleyra.video_sdk.common.usermessages.model.MicMessage
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.unmockkAll
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class InputMessageHandleTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private lateinit var callActionsViewModel: CallActionsViewModel

    @Before
    fun setup() {
        callActionsViewModel = spyk(
            CallActionsViewModel {
                CollaborationViewModel.Configuration.Success(mockk(relaxed = true), mockk(relaxed = true), mockk(relaxed = true), MutableStateFlow(mockk()))
            }
        )
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun nullInputMessage_dragHandleIsDisplayed() {
        composeTestRule.setContent {
            InputMessageHandle()
        }
        composeTestRule.onNodeWithTag(InputMessageDragHandleTag).assertIsDisplayed()
    }

    @Test
    fun testInputMessageHandleOnMicInputMessage() {
        every { callActionsViewModel.userMessage } returns MutableStateFlow(MicMessage.Enabled)
        val microphone = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_microphone)
        val on = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_on)
        composeTestRule.setContent {
            InputMessageHandle(callActionsViewModel)
        }

        composeTestRule.onNodeWithText(microphone).assertIsDisplayed()
        composeTestRule.onNodeWithText(on).assertIsDisplayed()
        composeTestRule.onNodeWithTag(InputMessageDragHandleTag).assertDoesNotExist()
        composeTestRule.mainClock.advanceTimeBy(InputMessageDuration)
        composeTestRule.onNodeWithText(microphone).assertDoesNotExist()
        composeTestRule.onNodeWithText(on).assertDoesNotExist()
        composeTestRule.onNodeWithTag(InputMessageDragHandleTag).assertIsDisplayed()
    }

    @Test
    fun testInputMessageHandleOnCameraInputMessage() {
        every { callActionsViewModel.userMessage } returns MutableStateFlow(CameraMessage.Enabled)
        val camera = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_camera)
        val on = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_on)
        composeTestRule.setContent {
            InputMessageHandle(callActionsViewModel)
        }

        composeTestRule.onNodeWithText(camera).assertIsDisplayed()
        composeTestRule.onNodeWithText(on).assertIsDisplayed()
        composeTestRule.onNodeWithTag(InputMessageDragHandleTag).assertDoesNotExist()
        composeTestRule.mainClock.advanceTimeBy(InputMessageDuration)
        composeTestRule.onNodeWithText(camera).assertDoesNotExist()
        composeTestRule.onNodeWithText(on).assertDoesNotExist()
        composeTestRule.onNodeWithTag(InputMessageDragHandleTag).assertIsDisplayed()
    }

    @Test
    fun testInputMessageHandleOffMicInputMessage() {
        every { callActionsViewModel.userMessage } returns MutableStateFlow(MicMessage.Disabled)
        val microphone = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_microphone)
        val off = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_off)
        composeTestRule.setContent {
            InputMessageHandle(callActionsViewModel)
        }

        composeTestRule.onNodeWithText(microphone).assertIsDisplayed()
        composeTestRule.onNodeWithText(off).assertIsDisplayed()
        composeTestRule.onNodeWithTag(InputMessageDragHandleTag).assertDoesNotExist()
        composeTestRule.mainClock.advanceTimeBy(InputMessageDuration)
        composeTestRule.onNodeWithText(microphone).assertDoesNotExist()
        composeTestRule.onNodeWithText(off).assertDoesNotExist()
        composeTestRule.onNodeWithTag(InputMessageDragHandleTag).assertIsDisplayed()
    }

    @Test
    fun testInputMessageHandleOffCameraInputMessage() {
        every { callActionsViewModel.userMessage } returns MutableStateFlow(CameraMessage.Disabled)
        val camera = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_camera)
        val off = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_off)
        composeTestRule.setContent {
            InputMessageHandle(callActionsViewModel)
        }

        composeTestRule.onNodeWithText(camera).assertIsDisplayed()
        composeTestRule.onNodeWithText(off).assertIsDisplayed()
        composeTestRule.onNodeWithTag(InputMessageDragHandleTag).assertDoesNotExist()
        composeTestRule.mainClock.advanceTimeBy(InputMessageDuration)
        composeTestRule.onNodeWithText(camera).assertDoesNotExist()
        composeTestRule.onNodeWithText(off).assertDoesNotExist()
        composeTestRule.onNodeWithTag(InputMessageDragHandleTag).assertIsDisplayed()
    }

    @Test
    fun testUserMessageFullscreenActive() {
        every { callActionsViewModel.userMessage } returns MutableStateFlow(FullScreenMessage.Enabled)
        val camera = composeTestRule.activity.getString(R.string.kaleyra_fullscreen)
        val off = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_active)
        composeTestRule.setContent {
            InputMessageHandle(callActionsViewModel)
        }

        composeTestRule.onNodeWithText(camera).assertIsDisplayed()
        composeTestRule.onNodeWithText(off).assertIsDisplayed()
        composeTestRule.onNodeWithTag(InputMessageDragHandleTag).assertDoesNotExist()
        composeTestRule.mainClock.advanceTimeBy(InputMessageDuration)
        composeTestRule.onNodeWithText(camera).assertDoesNotExist()
        composeTestRule.onNodeWithText(off).assertDoesNotExist()
        composeTestRule.onNodeWithTag(InputMessageDragHandleTag).assertIsDisplayed()
    }

    @Test
    fun testUserMessageFullscreenDisabled() {
        every { callActionsViewModel.userMessage } returns MutableStateFlow(FullScreenMessage.Disabled)
        val camera = composeTestRule.activity.getString(R.string.kaleyra_fullscreen)
        val off = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_disabled)
        composeTestRule.setContent {
            InputMessageHandle(callActionsViewModel)
        }

        composeTestRule.onNodeWithText(camera).assertIsDisplayed()
        composeTestRule.onNodeWithText(off).assertIsDisplayed()
        composeTestRule.onNodeWithTag(InputMessageDragHandleTag).assertDoesNotExist()
        composeTestRule.mainClock.advanceTimeBy(InputMessageDuration)
        composeTestRule.onNodeWithText(camera).assertDoesNotExist()
        composeTestRule.onNodeWithText(off).assertDoesNotExist()
        composeTestRule.onNodeWithTag(InputMessageDragHandleTag).assertIsDisplayed()
    }
}
