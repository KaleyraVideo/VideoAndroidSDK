package com.kaleyra.video_sdk.call.screennew

import android.content.res.Configuration
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.rules.ActivityScenarioRule
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.call.bottomsheetnew.CallSheetState
import com.kaleyra.video_sdk.call.bottomsheetnew.inputmessage.model.InputMessage
import com.kaleyra.video_sdk.call.callactions.model.CallActionsUiState
import com.kaleyra.video_sdk.call.callactions.viewmodel.CallActionsViewModel
import com.kaleyra.video_sdk.call.streamnew.model.StreamUiState
import com.kaleyra.video_sdk.call.streamnew.viewmodel.StreamViewModel
import com.kaleyra.video_sdk.common.immutablecollections.toImmutableList
import com.kaleyra.video_sdk.utils.WindowSizeClassUtil
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class VCallScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val callActionsUiState = MutableStateFlow(CallActionsUiState())

    private val streamUiState = MutableStateFlow(StreamUiState())

    private val callViewModel = mockk<CallActionsViewModel>(relaxed = true) {
        every { uiState } returns callActionsUiState
    }

    private val streamViewModel = mockk<StreamViewModel>(relaxed = true) {
        every { uiState } returns streamUiState
    }

    @Before
    fun setUp() {
        mockkObject(CallActionsViewModel)
        mockkObject(StreamViewModel)

        every { CallActionsViewModel.provideFactory(any()) } returns mockk {
            every { create<CallActionsViewModel>(any(), any()) } returns callViewModel
        }
        every { StreamViewModel.provideFactory(any()) } returns mockk {
            every { create<StreamViewModel>(any(), any()) } returns streamViewModel
        }
    }

    @After
    fun tearDown() {
        unmockkAll()
    }


    @Test
    fun testSheetActions_accept() {
        composeTestRule.setUpVCallScreen()
        callActionsUiState.value = CallActionsUiState(isRinging = true)

        val text = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_answer)
        composeTestRule
            .onNodeWithText(text, useUnmergedTree = true)
            .assertIsDisplayed()
            .performClick()

        verify(exactly = 1) { callViewModel.accept() }
    }

    @Test
    fun testSheetActions_hangUp() {
        composeTestRule.setUpVCallScreen()
        callActionsUiState.value = CallActionsUiState(
            actionList = listOf(HangUpAction()).toImmutableList()
        )

        val text = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_hang_up)
        composeTestRule
            .onNodeWithContentDescription(text, useUnmergedTree = true)
            .assertIsDisplayed()
            .performClick()

        verify(exactly = 1) { callViewModel.hangUp() }
    }

    @Test
    fun testSheetActions_micToggleOn() {
        composeTestRule.setUpVCallScreen()
        callActionsUiState.value = CallActionsUiState(
            actionList = listOf(MicAction()).toImmutableList()
        )

        val text = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_description_disable_microphone)
        composeTestRule
            .onNodeWithContentDescription(text, useUnmergedTree = true)
            .assertIsDisplayed()
            .performClick()

        verify(exactly = 1) { callViewModel.toggleMic(any()) }
    }

    @Test
    fun testSheetActions_micToggleOff() {
        composeTestRule.setUpVCallScreen()
        callActionsUiState.value = CallActionsUiState(
            actionList = listOf(MicAction(isToggled = true)).toImmutableList()
        )

        val text = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_description_enable_microphone)
        composeTestRule
            .onNodeWithContentDescription(text, useUnmergedTree = true)
            .assertIsDisplayed()
            .performClick()

        verify(exactly = 1) { callViewModel.toggleMic(any()) }
    }

    @Test
    fun testSheetActions_cameraToggleOn() {
        composeTestRule.setUpVCallScreen()
        callActionsUiState.value = CallActionsUiState(
            actionList = listOf(CameraAction()).toImmutableList()
        )

        val text = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_description_disable_camera)
        composeTestRule
            .onNodeWithContentDescription(text, useUnmergedTree = true)
            .assertIsDisplayed()
            .performClick()

        verify(exactly = 1) { callViewModel.toggleCamera(any()) }
    }

    @Test
    fun testSheetActions_cameraToggleOff() {
        composeTestRule.setUpVCallScreen()
        callActionsUiState.value = CallActionsUiState(
            actionList = listOf(CameraAction(isToggled = true)).toImmutableList()
        )

        val text = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_description_enable_camera)
        composeTestRule
            .onNodeWithContentDescription(text, useUnmergedTree = true)
            .assertIsDisplayed()
            .performClick()

        verify(exactly = 1) { callViewModel.toggleCamera(any()) }
    }

    @Test
    fun testSheetActions_flipCamera() {
        composeTestRule.setUpVCallScreen()
        callActionsUiState.value = CallActionsUiState(
            actionList = listOf(FlipCameraAction()).toImmutableList()
        )

        val text = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_flip_camera)
        composeTestRule
            .onNodeWithContentDescription(text, useUnmergedTree = true)
            .assertIsDisplayed()
            .performClick()

        verify(exactly = 1) { callViewModel.switchCamera() }
    }

    @Test
    fun testSheetActions_chat() {
        composeTestRule.setUpVCallScreen()
        callActionsUiState.value = CallActionsUiState(
            actionList = listOf(ChatAction()).toImmutableList()
        )

        val text = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_chat)
        composeTestRule
            .onNodeWithContentDescription(text, useUnmergedTree = true)
            .assertIsDisplayed()
            .performClick()

        verify(exactly = 1) { callViewModel.showChat(any()) }
    }

    @Test
    fun testSheetActions_screenShareToggleOff() {
        composeTestRule.setUpVCallScreen()
        callActionsUiState.value = CallActionsUiState(
            actionList = listOf(ScreenShareAction(isToggled = true)).toImmutableList()
        )

        val text = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_description_stop_screen_share)
        composeTestRule
            .onNodeWithContentDescription(text, useUnmergedTree = true)
            .assertIsDisplayed()
            .performClick()

        verify(exactly = 1) { callViewModel.tryStopScreenShare() }
    }

    private fun  AndroidComposeTestRule<ActivityScenarioRule<ComponentActivity>, ComponentActivity>.setUpVCallScreen(
        configuration: Configuration = Configuration().apply {
            screenWidthDp = 480
            screenHeightDp = 600
        },
        sheetState: CallSheetState = CallSheetState(),
        inputMessage: InputMessage? = null,
        onChangeSheetState: () -> Unit = { },
        onBackPressed: () -> Unit = { },
    ) {
        setContent {
            VCallScreen(
                windowSizeClass = WindowSizeClassUtil.currentWindowAdaptiveInfo(configuration),
                sheetState = sheetState,
                inputMessage = inputMessage,
                onChangeSheetState = onChangeSheetState,
                onBackPressed = onBackPressed,
            )
        }
    }
}
