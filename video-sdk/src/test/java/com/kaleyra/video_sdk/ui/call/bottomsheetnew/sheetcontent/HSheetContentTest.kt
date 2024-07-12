package com.kaleyra.video_sdk.ui.call.bottomsheetnew.sheetcontent

import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEqualTo
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.call.bottomsheetnew.sheetcontent.HSheetContent
import com.kaleyra.video_sdk.call.bottomsheetnew.sheetcontent.sheetitemslayout.SheetItemsSpacing
import com.kaleyra.video_sdk.call.callactionnew.AnswerActionExtendedMultiplier
import com.kaleyra.video_sdk.call.callactionnew.AnswerActionExtendedWidth
import com.kaleyra.video_sdk.call.callactionnew.AnswerActionMultiplier
import com.kaleyra.video_sdk.call.callactionnew.AnswerActionWidth
import com.kaleyra.video_sdk.call.callactionnew.HangUpActionExtendedWidth
import com.kaleyra.video_sdk.call.callactionnew.HangUpActionWidth
import com.kaleyra.video_sdk.call.callactions.model.CallActionsUiState
import com.kaleyra.video_sdk.call.callactions.viewmodel.CallActionsViewModel
import com.kaleyra.video_sdk.call.screennew.AudioAction
import com.kaleyra.video_sdk.call.screennew.CallActionUI
import com.kaleyra.video_sdk.call.screennew.CameraAction
import com.kaleyra.video_sdk.call.screennew.ChatAction
import com.kaleyra.video_sdk.call.screennew.FileShareAction
import com.kaleyra.video_sdk.call.screennew.FlipCameraAction
import com.kaleyra.video_sdk.call.screennew.HangUpAction
import com.kaleyra.video_sdk.call.screennew.MicAction
import com.kaleyra.video_sdk.call.screennew.ModalSheetComponent
import com.kaleyra.video_sdk.call.screennew.ScreenShareAction
import com.kaleyra.video_sdk.call.screennew.VirtualBackgroundAction
import com.kaleyra.video_sdk.call.screennew.WhiteboardAction
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList
import com.kaleyra.video_sdk.common.immutablecollections.toImmutableList
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import junit.framework.TestCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class HSheetContentTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()


    private val callActionsUiState = MutableStateFlow(CallActionsUiState())

    private val callActionsViewModel = mockk<CallActionsViewModel>(relaxed = true) {
        every { uiState } returns callActionsUiState
    }

    @Test
    fun actionsOverflow_onActionsPlacedHaveOverflown() {
        callActionsUiState.value = CallActionsUiState(
            actionList = listOf(HangUpAction(), MicAction(), CameraAction(), ScreenShareAction(), FlipCameraAction(), WhiteboardAction()).toImmutableList()
        )
        var overflowedActions: ImmutableList<CallActionUI>? = null
        composeTestRule.setContent {
            HSheetContent(
                viewModel = callActionsViewModel,
                isLargeScreen = false,
                isMoreToggled = false,
                onMoreToggle = {},
                onActionsOverflow = { overflowedActions = it },
                onModalSheetComponentRequest = {},
                modifier = Modifier.width(300.dp)
            )
        }

        val expected = ImmutableList(listOf(ScreenShareAction(), FlipCameraAction(), WhiteboardAction()))
        assertEquals(expected, overflowedActions)
    }

    @Test
    fun userClicksAccept_acceptInvoked() {
        callActionsUiState.value = CallActionsUiState(isRinging = true)
        composeTestRule.setContent {
            HSheetContent(
                viewModel = callActionsViewModel,
                isLargeScreen = false,
                isMoreToggled = false,
                onMoreToggle = {},
                onActionsOverflow = {},
                onModalSheetComponentRequest = {},
            )
        }

        val text = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_answer)
        composeTestRule
            .onNodeWithText(text, useUnmergedTree = true)
            .assertIsDisplayed()
            .performClick()

        verify(exactly = 1) { callActionsViewModel.accept() }
    }

    @Test
    fun userClicksHangUp_hangUpInvoked() {
        callActionsUiState.value = CallActionsUiState(actionList = listOf(HangUpAction()).toImmutableList())
        composeTestRule.setContent {
            HSheetContent(
                viewModel = callActionsViewModel,
                isLargeScreen = false,
                isMoreToggled = false,
                onMoreToggle = {},
                onActionsOverflow = {},
                onModalSheetComponentRequest = {},
            )
        }

        val text = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_hang_up)
        composeTestRule
            .onNodeWithContentDescription(text, useUnmergedTree = true)
            .assertIsDisplayed()
            .performClick()

        verify(exactly = 1) { callActionsViewModel.hangUp() }
    }

    @Test
    fun userTogglesMic_toggleMicInvoked() {
        callActionsUiState.value = CallActionsUiState(actionList = listOf(MicAction()).toImmutableList())
        composeTestRule.setContent {
            HSheetContent(
                viewModel = callActionsViewModel,
                isLargeScreen = false,
                isMoreToggled = false,
                onMoreToggle = {},
                onActionsOverflow = {},
                onModalSheetComponentRequest = {},
            )
        }

        val text = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_description_disable_microphone)
        composeTestRule
            .onNodeWithContentDescription(text, useUnmergedTree = true)
            .assertIsDisplayed()
            .performClick()

        verify(exactly = 1) { callActionsViewModel.toggleMic(any()) }
    }

    @Test
    fun userTogglesCamera_toggleCameraInvoked() {
        callActionsUiState.value = CallActionsUiState(actionList = listOf(CameraAction()).toImmutableList())
        composeTestRule.setContent {
            HSheetContent(
                viewModel = callActionsViewModel,
                isLargeScreen = false,
                isMoreToggled = false,
                onMoreToggle = {},
                onActionsOverflow = {},
                onModalSheetComponentRequest = {},
            )
        }

        val text = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_description_disable_camera)
        composeTestRule
            .onNodeWithContentDescription(text, useUnmergedTree = true)
            .assertIsDisplayed()
            .performClick()

        verify(exactly = 1) { callActionsViewModel.toggleCamera(any()) }
    }

    @Test
    fun userClicksChat_showChatInvoked() {
        callActionsUiState.value = CallActionsUiState(actionList = listOf(ChatAction()).toImmutableList())
        composeTestRule.setContent {
            HSheetContent(
                viewModel = callActionsViewModel,
                isLargeScreen = false,
                isMoreToggled = false,
                onMoreToggle = {},
                onActionsOverflow = {},
                onModalSheetComponentRequest = {},
            )
        }

        val text = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_chat)
        composeTestRule
            .onNodeWithContentDescription(text, useUnmergedTree = true)
            .assertIsDisplayed()
            .performClick()

        verify(exactly = 1) { callActionsViewModel.showChat(any()) }
    }

    @Test
    fun userClicksFlipCamera_switchCameraInvoked() {
        callActionsUiState.value = CallActionsUiState(actionList = listOf(FlipCameraAction()).toImmutableList())
        composeTestRule.setContent {
            HSheetContent(
                viewModel = callActionsViewModel,
                isLargeScreen = false,
                isMoreToggled = false,
                onMoreToggle = {},
                onActionsOverflow = {},
                onModalSheetComponentRequest = {},
            )
        }

        val text = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_flip_camera)
        composeTestRule
            .onNodeWithContentDescription(text, useUnmergedTree = true)
            .assertIsDisplayed()
            .performClick()

        verify(exactly = 1) { callActionsViewModel.switchCamera() }
    }

    @Test
    fun userClicksAudio_onModalSheetComponentRequestAudio() {
        callActionsUiState.value = CallActionsUiState(actionList = listOf(AudioAction()).toImmutableList())
        var component: ModalSheetComponent? = null
        composeTestRule.setContent {
            HSheetContent(
                viewModel = callActionsViewModel,
                isLargeScreen = false,
                isMoreToggled = false,
                onMoreToggle = {},
                onActionsOverflow = {},
                onModalSheetComponentRequest = { component = it },
            )
        }

        val text = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_audio)
        composeTestRule
            .onNodeWithContentDescription(text, useUnmergedTree = true)
            .assertIsDisplayed()
            .performClick()

        TestCase.assertEquals(ModalSheetComponent.Audio, component)
    }

    @Test
    fun userClicksFileShare_onModalSheetComponentRequestFileShare() {
        callActionsUiState.value = CallActionsUiState(actionList = listOf(FileShareAction()).toImmutableList())
        var component: ModalSheetComponent? = null
        composeTestRule.setContent {
            HSheetContent(
                viewModel = callActionsViewModel,
                isLargeScreen = false,
                isMoreToggled = false,
                onMoreToggle = {},
                onActionsOverflow = {},
                onModalSheetComponentRequest = { component = it },
            )
        }

        val text = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_file_share)
        composeTestRule
            .onNodeWithContentDescription(text, useUnmergedTree = true)
            .assertIsDisplayed()
            .performClick()

        TestCase.assertEquals(ModalSheetComponent.FileShare, component)
    }

    @Test
    fun userClicksWhiteboard_onModalSheetComponentRequestWhiteboard() {
        callActionsUiState.value = CallActionsUiState(actionList = listOf(WhiteboardAction()).toImmutableList())
        var component: ModalSheetComponent? = null
        composeTestRule.setContent {
            HSheetContent(
                viewModel = callActionsViewModel,
                isLargeScreen = false,
                isMoreToggled = false,
                onMoreToggle = {},
                onActionsOverflow = {},
                onModalSheetComponentRequest = { component = it },
            )
        }

        val text = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_whiteboard)
        composeTestRule
            .onNodeWithContentDescription(text, useUnmergedTree = true)
            .assertIsDisplayed()
            .performClick()

        TestCase.assertEquals(ModalSheetComponent.Whiteboard, component)
    }

    @Test
    fun userClicksVirtualBackground_onModalSheetComponentRequestVirtualBackground() {
        callActionsUiState.value = CallActionsUiState(actionList = listOf(VirtualBackgroundAction()).toImmutableList())
        var component: ModalSheetComponent? = null
        composeTestRule.setContent {
            HSheetContent(
                viewModel = callActionsViewModel,
                isLargeScreen = false,
                isMoreToggled = false,
                onMoreToggle = {},
                onActionsOverflow = {},
                onModalSheetComponentRequest = { component = it },
            )
        }

        val text = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_virtual_background)
        composeTestRule
            .onNodeWithContentDescription(text, useUnmergedTree = true)
            .assertIsDisplayed()
            .performClick()

        TestCase.assertEquals(ModalSheetComponent.VirtualBackground, component)
    }

    @Test
    fun userClicksScreenShareWhenEnabled_tryStopScreenShareInvoked() {
        callActionsUiState.value = CallActionsUiState(actionList = listOf(ScreenShareAction()).toImmutableList())
        every { callActionsViewModel.tryStopScreenShare() } returns true
        composeTestRule.setContent {
            HSheetContent(
                viewModel = callActionsViewModel,
                isLargeScreen = false,
                isMoreToggled = false,
                onMoreToggle = {},
                onActionsOverflow = {},
                onModalSheetComponentRequest = {},
            )
        }

        val text = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_screen_share)
        composeTestRule
            .onNodeWithContentDescription(text, useUnmergedTree = true)
            .assertIsDisplayed()
            .performClick()

        verify(exactly = 1) { callActionsViewModel.tryStopScreenShare() }
    }

    @Test
    fun userClicksScreenShareWhenNotEnabled_onModalSheetComponentRequestScreenShare() {
        callActionsUiState.value = CallActionsUiState(actionList = listOf(ScreenShareAction()).toImmutableList())
        every { callActionsViewModel.tryStopScreenShare() } returns false
        var component: ModalSheetComponent? = null
        composeTestRule.setContent {
            HSheetContent(
                viewModel = callActionsViewModel,
                isLargeScreen = false,
                isMoreToggled = false,
                onMoreToggle = {},
                onActionsOverflow = {},
                onModalSheetComponentRequest = { component = it },
            )
        }

        val text = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_screen_share)
        composeTestRule
            .onNodeWithContentDescription(text, useUnmergedTree = true)
            .assertIsDisplayed()
            .performClick()

        TestCase.assertEquals(ModalSheetComponent.ScreenShare, component)
    }

    @Test
    fun testMoreActionNotToggled() {
        composeTestRule.setContent {
            HSheetContent(
                modifier = Modifier.width(150.dp),
                callActions = ImmutableList(
                    listOf(
                        FlipCameraAction(),
                        FlipCameraAction(),
                        FlipCameraAction(),
                        FlipCameraAction(),
                        FlipCameraAction()
                    )
                ),
                showAnswerAction = false,
                isMoreToggled = false,
                isLargeScreen = false,
                onActionsPlaced = { },
                onAnswerClick = { },
                onHangUpClick = { },
                onMicToggle = { },
                onCameraToggle = { },
                onScreenShareToggle = { },
                onFlipCameraClick = { },
                onAudioClick = { },
                onChatClick = { },
                onFileShareClick = { },
                onWhiteboardClick = { },
                onVirtualBackgroundClick = { },
                onMoreToggle = { }
            )
        }
        val text = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_description_more_actions)
        composeTestRule.onNodeWithContentDescription(text).assertIsDisplayed()
    }

    @Test
    fun testMoreActionToggled() {
        composeTestRule.setContent {
            HSheetContent(
                modifier = Modifier.width(150.dp),
                callActions = ImmutableList(
                    listOf(
                        FlipCameraAction(),
                        FlipCameraAction(),
                        FlipCameraAction(),
                        FlipCameraAction(),
                        FlipCameraAction()
                    )
                ),
                showAnswerAction = false,
                isMoreToggled = true,
                isLargeScreen = false,
                onActionsPlaced = { },
                onAnswerClick = { },
                onHangUpClick = { },
                onMicToggle = { },
                onCameraToggle = { },
                onScreenShareToggle = { },
                onFlipCameraClick = { },
                onAudioClick = { },
                onChatClick = { },
                onFileShareClick = { },
                onWhiteboardClick = { },
                onVirtualBackgroundClick = { },
                onMoreToggle = { }
            )
        }
        val text = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_description_hide_actions)
        composeTestRule.onNodeWithContentDescription(text).assertIsDisplayed()
    }

    @Test
    fun testMoreActionNotificationCountIsDisplayed() {
        composeTestRule.setContent {
            HSheetContent(
                modifier = Modifier.width(150.dp),
                callActions = ImmutableList(
                    listOf(
                        FileShareAction(notificationCount = 2),
                        WhiteboardAction(notificationCount = 3),
                        FlipCameraAction(),
                        FlipCameraAction()
                    )
                ),
                showAnswerAction = false,
                isMoreToggled = false,
                isLargeScreen = false,
                onActionsPlaced = { },
                onAnswerClick = { },
                onHangUpClick = { },
                onMicToggle = { },
                onCameraToggle = { },
                onScreenShareToggle = { },
                onFlipCameraClick = { },
                onAudioClick = { },
                onChatClick = { },
                onFileShareClick = { },
                onWhiteboardClick = { },
                onVirtualBackgroundClick = { },
                onMoreToggle = { }
            )
        }
        composeTestRule.onNodeWithText("5").assertIsDisplayed()
    }

    @Test
    fun zeroNotificationCount_moreActionNotificationCountDoesNotExists() {
        composeTestRule.setContent {
            HSheetContent(
                modifier = Modifier.width(150.dp),
                callActions = ImmutableList(
                    listOf(
                        FileShareAction(notificationCount = 0),
                        WhiteboardAction(notificationCount = 0),
                        FlipCameraAction(),
                        FlipCameraAction()
                    )
                ),
                showAnswerAction = false,
                isMoreToggled = false,
                isLargeScreen = false,
                onActionsPlaced = { },
                onAnswerClick = { },
                onHangUpClick = { },
                onMicToggle = { },
                onCameraToggle = { },
                onScreenShareToggle = { },
                onFlipCameraClick = { },
                onAudioClick = { },
                onChatClick = { },
                onFileShareClick = { },
                onWhiteboardClick = { },
                onVirtualBackgroundClick = { },
                onMoreToggle = { },
            )
        }
        composeTestRule.onNodeWithText("0").assertDoesNotExist()
    }

    @Test
    fun showAnswerActionTrue_answerActionIsDisplayed() {
        val answerDescription =
            composeTestRule.activity.getString(R.string.kaleyra_call_sheet_answer)
        composeTestRule.setContent {
            HSheetContent(
                callActions = ImmutableList(),
                showAnswerAction = true,
                isLargeScreen = false,
                isMoreToggled = false,
                onActionsPlaced = { },
                onAnswerClick = { },
                onHangUpClick = { },
                onMicToggle = { },
                onCameraToggle = { },
                onScreenShareToggle = { },
                onFlipCameraClick = { },
                onAudioClick = { },
                onChatClick = { },
                onFileShareClick = { },
                onWhiteboardClick = { },
                onVirtualBackgroundClick = { },
                onMoreToggle = { }
            )
        }
        composeTestRule.onNodeWithText(answerDescription).assertIsDisplayed()
    }

    @Test
    fun showAnswerActionFalse_answerActionIsNotDisplayed() {
        val answerDescription =
            composeTestRule.activity.getString(R.string.kaleyra_call_sheet_answer)
        composeTestRule.setContent {
            HSheetContent(
                callActions = ImmutableList(),
                showAnswerAction = false,
                isLargeScreen = false,
                isMoreToggled = false,
                onActionsPlaced = { },
                onAnswerClick = { },
                onHangUpClick = { },
                onMicToggle = { },
                onCameraToggle = { },
                onScreenShareToggle = { },
                onFlipCameraClick = { },
                onAudioClick = { },
                onChatClick = { },
                onFileShareClick = { },
                onWhiteboardClick = { },
                onVirtualBackgroundClick = { },
                onMoreToggle = { }
            )
        }
        composeTestRule.onNodeWithText(answerDescription).assertDoesNotExist()
    }

    @Test
    fun testOnHangUpActionClick() {
        var isHangUpClicked = false
        val hangUpDescription = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_hang_up)
        composeTestRule.setContent {
            HSheetContent(
                callActions = ImmutableList(listOf(HangUpAction())),
                showAnswerAction = true,
                isLargeScreen = false,
                isMoreToggled = false,
                onActionsPlaced = { },
                onAnswerClick = { },
                onHangUpClick = { isHangUpClicked = true },
                onMicToggle = { },
                onCameraToggle = { },
                onScreenShareToggle = { },
                onFlipCameraClick = { },
                onAudioClick = { },
                onChatClick = { },
                onFileShareClick = { },
                onWhiteboardClick = { },
                onVirtualBackgroundClick = { },
                onMoreToggle = { }
            )
        }
        composeTestRule.onNodeWithContentDescription(hangUpDescription).assertHasClickAction()
        composeTestRule.onNodeWithContentDescription(hangUpDescription).performClick()
        assertEquals(true, isHangUpClicked)
    }

    @Test
    fun testOnMicActionClick() {
        var isMicClicked: Boolean? = null
        val micDescription = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_description_disable_microphone)
        composeTestRule.setContent {
            HSheetContent(
                callActions = ImmutableList(listOf(MicAction())),
                showAnswerAction = true,
                isLargeScreen = false,
                isMoreToggled = false,
                onActionsPlaced = { },
                onAnswerClick = { },
                onHangUpClick = { },
                onMicToggle = { isMicClicked = it },
                onCameraToggle = { },
                onScreenShareToggle = { },
                onFlipCameraClick = { },
                onAudioClick = { },
                onChatClick = { },
                onFileShareClick = { },
                onWhiteboardClick = { },
                onVirtualBackgroundClick = { },
                onMoreToggle = { }
            )
        }
        composeTestRule.onNodeWithContentDescription(micDescription).assertHasClickAction()
        composeTestRule.onNodeWithContentDescription(micDescription).performClick()
        assertEquals(true, isMicClicked)
    }

    @Test
    fun testOnCameraActionClick() {
        var isCameraClicked: Boolean? = null
        val cameraDescription = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_description_disable_camera)
        composeTestRule.setContent {
            HSheetContent(
                callActions = ImmutableList(listOf(CameraAction())),
                showAnswerAction = true,
                isLargeScreen = false,
                isMoreToggled = false,
                onActionsPlaced = { },
                onAnswerClick = { },
                onHangUpClick = { },
                onMicToggle = { },
                onCameraToggle = { isCameraClicked = it },
                onScreenShareToggle = { },
                onFlipCameraClick = { },
                onAudioClick = { },
                onChatClick = { },
                onFileShareClick = { },
                onWhiteboardClick = { },
                onVirtualBackgroundClick = { },
                onMoreToggle = { }
            )
        }
        composeTestRule.onNodeWithContentDescription(cameraDescription).assertHasClickAction()
        composeTestRule.onNodeWithContentDescription(cameraDescription).performClick()
        assertEquals(true, isCameraClicked)
    }

    @Test
    fun testOnScreenShareActionClick() {
        var isScreenShareClicked: Boolean? = null
        val screenShareDescription = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_screen_share)
        composeTestRule.setContent {
            HSheetContent(
                callActions = ImmutableList(listOf(ScreenShareAction())),
                showAnswerAction = true,
                isLargeScreen = false,
                isMoreToggled = false,
                onActionsPlaced = { },
                onAnswerClick = { },
                onHangUpClick = { },
                onMicToggle = { },
                onCameraToggle = { },
                onScreenShareToggle = { isScreenShareClicked = true },
                onFlipCameraClick = { },
                onAudioClick = { },
                onChatClick = { },
                onFileShareClick = { },
                onWhiteboardClick = { },
                onVirtualBackgroundClick = { },
                onMoreToggle = { }
            )
        }
        composeTestRule.onNodeWithContentDescription(screenShareDescription).assertHasClickAction()
        composeTestRule.onNodeWithContentDescription(screenShareDescription).performClick()
        assertEquals(true, isScreenShareClicked)
    }

    @Test
    fun testOnFlipCameraActionClick() {
        var isFlipCameraClicked = false
        val flipDescription = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_flip_camera)
        composeTestRule.setContent {
            HSheetContent(
                callActions = ImmutableList(listOf(FlipCameraAction())),
                showAnswerAction = true,
                isLargeScreen = false,
                isMoreToggled = false,
                onActionsPlaced = { },
                onAnswerClick = { },
                onHangUpClick = { },
                onMicToggle = { },
                onCameraToggle = { },
                onScreenShareToggle = { },
                onFlipCameraClick = { isFlipCameraClicked = true },
                onAudioClick = { },
                onChatClick = { },
                onFileShareClick = { },
                onWhiteboardClick = { },
                onVirtualBackgroundClick = { },
                onMoreToggle = { }
            )
        }
        composeTestRule.onNodeWithContentDescription(flipDescription).assertHasClickAction()
        composeTestRule.onNodeWithContentDescription(flipDescription).performClick()
        assertEquals(true, isFlipCameraClicked)
    }

    @Test
    fun testOnAudioActionClick() {
        var isAudioClicked = false
        val audioDescription = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_audio)
        composeTestRule.setContent {
            HSheetContent(
                callActions = ImmutableList(listOf(AudioAction())),
                showAnswerAction = true,
                isLargeScreen = false,
                isMoreToggled = false,
                onActionsPlaced = { },
                onAnswerClick = { },
                onHangUpClick = { },
                onMicToggle = { },
                onCameraToggle = { },
                onScreenShareToggle = { },
                onFlipCameraClick = { },
                onAudioClick = { isAudioClicked = true },
                onChatClick = { },
                onFileShareClick = { },
                onWhiteboardClick = { },
                onVirtualBackgroundClick = { },
                onMoreToggle = { }
            )
        }
        composeTestRule.onNodeWithContentDescription(audioDescription).assertHasClickAction()
        composeTestRule.onNodeWithContentDescription(audioDescription).performClick()
        assertEquals(true, isAudioClicked)
    }

    @Test
    fun testOnChatActionClick() {
        var isChatClicked = false
        val chatDescription = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_chat)
        composeTestRule.setContent {
            HSheetContent(
                callActions = ImmutableList(listOf(ChatAction())),
                showAnswerAction = true,
                isLargeScreen = false,
                isMoreToggled = false,
                onActionsPlaced = { },
                onAnswerClick = { },
                onHangUpClick = { },
                onMicToggle = { },
                onCameraToggle = { },
                onScreenShareToggle = { },
                onFlipCameraClick = { },
                onAudioClick = { },
                onChatClick = { isChatClicked = true },
                onFileShareClick = { },
                onWhiteboardClick = { },
                onVirtualBackgroundClick = { },
                onMoreToggle = { }
            )
        }
        composeTestRule.onNodeWithContentDescription(chatDescription).assertHasClickAction()
        composeTestRule.onNodeWithContentDescription(chatDescription).performClick()
        assertEquals(true, isChatClicked)
    }

    @Test
    fun testOnFileShareActionClick() {
        var isFileShareClicked = false
        val fileShareDescription = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_file_share)
        composeTestRule.setContent {
            HSheetContent(
                callActions = ImmutableList(listOf(FileShareAction())),
                showAnswerAction = true,
                isLargeScreen = false,
                isMoreToggled = false,
                onActionsPlaced = { },
                onAnswerClick = { },
                onHangUpClick = { },
                onMicToggle = { },
                onCameraToggle = { },
                onScreenShareToggle = { },
                onFlipCameraClick = { },
                onAudioClick = { },
                onChatClick = { },
                onFileShareClick = { isFileShareClicked = true },
                onWhiteboardClick = { },
                onVirtualBackgroundClick = { },
                onMoreToggle = { }
            )
        }
        composeTestRule.onNodeWithContentDescription(fileShareDescription).assertHasClickAction()
        composeTestRule.onNodeWithContentDescription(fileShareDescription).performClick()
        assertEquals(true, isFileShareClicked)
    }

    @Test
    fun testOnWhiteboardActionClick() {
        var isWhiteboardClicked = false
        val whiteboardDescription = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_whiteboard)
        composeTestRule.setContent {
            HSheetContent(
                callActions = ImmutableList(listOf(WhiteboardAction())),
                showAnswerAction = true,
                isLargeScreen = false,
                isMoreToggled = false,
                onActionsPlaced = { },
                onAnswerClick = { },
                onHangUpClick = { },
                onMicToggle = { },
                onCameraToggle = { },
                onScreenShareToggle = { },
                onFlipCameraClick = { },
                onAudioClick = { },
                onChatClick = { },
                onFileShareClick = { },
                onWhiteboardClick = { isWhiteboardClicked = true },
                onVirtualBackgroundClick = { },
                onMoreToggle = { }
            )
        }
        composeTestRule.onNodeWithContentDescription(whiteboardDescription).assertHasClickAction()
        composeTestRule.onNodeWithContentDescription(whiteboardDescription).performClick()
        assertEquals(true, isWhiteboardClicked)
    }

    @Test
    fun testOnVirtualBackgroundActionClick() {
        var isVirtualClicked = false
        val virtualDescription = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_virtual_background)
        composeTestRule.setContent {
            HSheetContent(
                callActions = ImmutableList(listOf(VirtualBackgroundAction())),
                showAnswerAction = true,
                isLargeScreen = false,
                isMoreToggled = false,
                onActionsPlaced = { },
                onAnswerClick = { },
                onHangUpClick = { },
                onMicToggle = { },
                onCameraToggle = { },
                onScreenShareToggle = { },
                onFlipCameraClick = { },
                onAudioClick = { },
                onChatClick = { },
                onFileShareClick = { },
                onWhiteboardClick = { },
                onVirtualBackgroundClick = { isVirtualClicked = true },
                onMoreToggle = { }
            )
        }
        composeTestRule.onNodeWithContentDescription(virtualDescription).assertHasClickAction()
        composeTestRule.onNodeWithContentDescription(virtualDescription).performClick()
        assertEquals(true, isVirtualClicked)
    }

    @Test
    fun testOnAnswerActionClick() {
        var isAnswerClicked = false
        val answerDescription =
            composeTestRule.activity.getString(R.string.kaleyra_call_sheet_answer)
        composeTestRule.setContent {
            HSheetContent(
                callActions = ImmutableList(),
                showAnswerAction = true,
                isLargeScreen = false,
                isMoreToggled = false,
                onActionsPlaced = { },
                onAnswerClick = { isAnswerClicked = true },
                onHangUpClick = { },
                onMicToggle = { },
                onCameraToggle = { },
                onScreenShareToggle = { },
                onFlipCameraClick = { },
                onAudioClick = { },
                onChatClick = { },
                onFileShareClick = { },
                onWhiteboardClick = { },
                onVirtualBackgroundClick = { },
                onMoreToggle = { }
            )
        }
        composeTestRule.onNodeWithText(answerDescription).assertHasClickAction()
        composeTestRule.onNodeWithText(answerDescription).performClick()
        assertEquals(true, isAnswerClicked)
    }

    @Test
    fun testonMoreToggle() {
        var isMoreClicked = false
        val moreDescription =
            composeTestRule.activity.getString(R.string.kaleyra_call_sheet_description_more_actions)
        composeTestRule.setContent {
            HSheetContent(
                modifier = Modifier.width(100.dp),
                callActions = ImmutableList(
                    listOf(
                        FlipCameraAction(),
                        FlipCameraAction(),
                        FlipCameraAction(),
                    )
                ),
                showAnswerAction = false,
                isLargeScreen = false,
                isMoreToggled = false,
                onActionsPlaced = { },
                onAnswerClick = { },
                onHangUpClick = { },
                onMicToggle = { },
                onCameraToggle = { },
                onScreenShareToggle = { },
                onFlipCameraClick = { },
                onAudioClick = { },
                onChatClick = { },
                onFileShareClick = { },
                onWhiteboardClick = { },
                onVirtualBackgroundClick = { },
                onMoreToggle = { isMoreClicked = true }
            )
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithContentDescription(moreDescription).assertHasClickAction()
        composeTestRule.onNodeWithContentDescription(moreDescription).performClick()
        assertEquals(true, isMoreClicked)
    }

    @Test
    fun onlySomeActionsCanBeDisplayed_moreActionIsDisplayed() {
        val moreDescription =
            composeTestRule.activity.getString(R.string.kaleyra_call_sheet_description_more_actions)
        composeTestRule.setContent {
            HSheetContent(
                modifier = Modifier.width(150.dp),
                callActions = ImmutableList(
                    listOf(
                        FlipCameraAction(),
                        FlipCameraAction(),
                        FlipCameraAction(),
                        FlipCameraAction()
                    )
                ),
                showAnswerAction = false,
                isLargeScreen = false,
                isMoreToggled = false,
                onActionsPlaced = { },
                onAnswerClick = { },
                onHangUpClick = { },
                onMicToggle = { },
                onCameraToggle = { },
                onScreenShareToggle = { },
                onFlipCameraClick = { },
                onAudioClick = { },
                onChatClick = { },
                onFileShareClick = { },
                onWhiteboardClick = { },
                onVirtualBackgroundClick = { },
                onMoreToggle = { }
            )
        }
        composeTestRule.onNodeWithContentDescription(moreDescription).assertIsDisplayed()
    }

    @Test
    fun allActionsCanBeDisplayed_moreActionDoesNotExists() {
        val moreDescription =
            composeTestRule.activity.getString(R.string.kaleyra_call_sheet_description_more_actions)
        composeTestRule.setContent {
            HSheetContent(
                modifier = Modifier.width(150.dp),
                callActions = ImmutableList(
                    listOf(
                        FlipCameraAction(),
                        FlipCameraAction()
                    )
                ),
                showAnswerAction = false,
                isLargeScreen = false,
                isMoreToggled = false,
                onActionsPlaced = { },
                onAnswerClick = { },
                onHangUpClick = { },
                onMicToggle = { },
                onCameraToggle = { },
                onScreenShareToggle = { },
                onFlipCameraClick = { },
                onAudioClick = { },
                onChatClick = { },
                onFileShareClick = { },
                onWhiteboardClick = { },
                onVirtualBackgroundClick = { },
                onMoreToggle = { }
            )
        }
        composeTestRule.onNodeWithContentDescription(moreDescription).assertDoesNotExist()
    }

    @Test
    fun answerActionIsDisplayed_moreActionIsNotDisplayed() {
        val answerDescription =
            composeTestRule.activity.getString(R.string.kaleyra_call_sheet_answer)
        val moreDescription =
            composeTestRule.activity.getString(R.string.kaleyra_call_sheet_description_more_actions)
        composeTestRule.setContent {
            HSheetContent(
                modifier = Modifier.width(200.dp),
                callActions = ImmutableList(
                    listOf(
                        FlipCameraAction(),
                        FlipCameraAction(),
                        FlipCameraAction(),
                        FlipCameraAction()
                    )
                ),
                showAnswerAction = true,
                isLargeScreen = false,
                isMoreToggled = false,
                onActionsPlaced = { },
                onAnswerClick = { },
                onHangUpClick = { },
                onMicToggle = { },
                onCameraToggle = { },
                onScreenShareToggle = { },
                onFlipCameraClick = { },
                onAudioClick = { },
                onChatClick = { },
                onFileShareClick = { },
                onWhiteboardClick = { },
                onVirtualBackgroundClick = { },
                onMoreToggle = { }
            )
        }
        composeTestRule.onNodeWithText(answerDescription).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(moreDescription).assertDoesNotExist()
    }

    @Test
    fun testSheetContentActionsPlacing() {
        val moreDescription = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_description_more_actions)
        val flip = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_flip_camera)
        val camera = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_description_disable_camera)
        composeTestRule.setContent {
            HSheetContent(
                modifier = Modifier.width(200.dp),
                callActions = ImmutableList(
                    listOf(
                        FlipCameraAction(),
                        CameraAction(),
                        MicAction(),
                        ChatAction()
                    )
                ),
                showAnswerAction = false,
                isLargeScreen = false,
                isMoreToggled = false,
                onActionsPlaced = { },
                onAnswerClick = { },
                onHangUpClick = { },
                onMicToggle = { },
                onCameraToggle = { },
                onScreenShareToggle = { },
                onFlipCameraClick = { },
                onAudioClick = { },
                onChatClick = { },
                onFileShareClick = { },
                onWhiteboardClick = { },
                onVirtualBackgroundClick = { },
                onMoreToggle = { }
            )
        }
        val childBounds1 = composeTestRule.onNodeWithContentDescription(flip).getBoundsInRoot()
        val childBounds2 = composeTestRule.onNodeWithContentDescription(camera).getBoundsInRoot()
        val moreChild = composeTestRule.onNodeWithContentDescription(moreDescription).getBoundsInRoot()
        childBounds2.left.assertIsEqualTo(childBounds1.right + SheetItemsSpacing, "child 2 left")
        moreChild.left.assertIsEqualTo(childBounds2.right + SheetItemsSpacing, "more child left")
    }

    @Test
    fun testOnActionsPlacedCallback() {
        var itemsCount = -1
        composeTestRule.setContent {
            HSheetContent(
                modifier = Modifier.width(200.dp),
                callActions = ImmutableList(
                    listOf(
                        FlipCameraAction(),
                        FlipCameraAction(),
                        FlipCameraAction(),
                        FlipCameraAction()
                    )
                ),
                showAnswerAction = false,
                isLargeScreen = false,
                isMoreToggled = false,
                onActionsPlaced = { itemsCount = it },
                onAnswerClick = { },
                onHangUpClick = { },
                onMicToggle = { },
                onCameraToggle = { },
                onScreenShareToggle = { },
                onFlipCameraClick = { },
                onAudioClick = { },
                onChatClick = { },
                onFileShareClick = { },
                onWhiteboardClick = { },
                onVirtualBackgroundClick = { },
                onMoreToggle = { }
            )
        }
        composeTestRule.waitForIdle()
        assertEquals(2, itemsCount)
    }

    @Test
    fun testMaxActionsLessThanActualActions() {
        val moreDescription = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_description_more_actions)
        val flip = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_flip_camera)
        val camera = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_description_disable_camera)
        val mic = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_description_disable_microphone)
        val chat = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_chat)
        var itemsCount = -1
        val maxActions = 3
        composeTestRule.setContent {
            HSheetContent(
                maxActions = maxActions,
                callActions = ImmutableList(
                    listOf(
                        FlipCameraAction(),
                        CameraAction(),
                        MicAction(),
                        ChatAction()
                    )
                ),
                showAnswerAction = false,
                isLargeScreen = false,
                isMoreToggled = false,
                onActionsPlaced = { itemsCount = it },
                onAnswerClick = { },
                onHangUpClick = { },
                onMicToggle = { },
                onCameraToggle = { },
                onScreenShareToggle = { },
                onFlipCameraClick = { },
                onAudioClick = { },
                onChatClick = { },
                onFileShareClick = { },
                onWhiteboardClick = { },
                onVirtualBackgroundClick = { },
                onMoreToggle = { }
            )
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithContentDescription(flip).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(camera).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(mic).assertIsNotDisplayed()
        composeTestRule.onNodeWithContentDescription(chat).assertIsNotDisplayed()
        composeTestRule.onNodeWithContentDescription(moreDescription).assertIsDisplayed()
        assertEquals(maxActions - 1, itemsCount)
    }

    @Test
    fun testMaxActionsEqualToActualActions() {
        val moreDescription = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_description_more_actions)
        val flip = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_flip_camera)
        val camera = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_description_disable_camera)
        val mic = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_description_disable_microphone)
        val chat = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_chat)
        var itemsCount = -1
        val maxActions = 4
        composeTestRule.setContent {
            HSheetContent(
                maxActions = maxActions,
                callActions = ImmutableList(
                    listOf(
                        FlipCameraAction(),
                        CameraAction(),
                        MicAction(),
                        ChatAction()
                    )
                ),
                showAnswerAction = false,
                isMoreToggled = false,
                isLargeScreen = false,
                onActionsPlaced = { itemsCount = it },
                onAnswerClick = { },
                onHangUpClick = { },
                onMicToggle = { },
                onCameraToggle = { },
                onScreenShareToggle = { },
                onFlipCameraClick = { },
                onAudioClick = { },
                onChatClick = { },
                onFileShareClick = { },
                onWhiteboardClick = { },
                onVirtualBackgroundClick = { },
                onMoreToggle = { }
            )
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithContentDescription(flip).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(camera).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(mic).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(chat).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(moreDescription).assertDoesNotExist()
        assertEquals(maxActions, itemsCount)
    }

    @Test
    fun answerActionIsDisplayed_answerActionMultiplierAppliedToMaxActions() {
        val answerDescription = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_answer)
        val flip = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_flip_camera)
        val camera = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_description_disable_camera)
        val mic = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_description_disable_microphone)
        var itemsCount = -1
        val maxActions = 3
        composeTestRule.setContent {
            HSheetContent(
                maxActions = maxActions,
                callActions = ImmutableList(
                    listOf(
                        FlipCameraAction(),
                        CameraAction(),
                        MicAction()
                    )
                ),
                showAnswerAction = true,
                isLargeScreen = false,
                isMoreToggled = false,
                onActionsPlaced = { itemsCount = it },
                onAnswerClick = {  },
                onHangUpClick = { },
                onMicToggle = { },
                onCameraToggle = { },
                onScreenShareToggle = { },
                onFlipCameraClick = { },
                onAudioClick = { },
                onChatClick = { },
                onFileShareClick = { },
                onWhiteboardClick = { },
                onVirtualBackgroundClick = { },
                onMoreToggle = { }
            )
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithContentDescription(flip).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(camera).assertIsNotDisplayed()
        composeTestRule.onNodeWithContentDescription(mic).assertIsNotDisplayed()
        composeTestRule.onNodeWithText(answerDescription).assertIsDisplayed()
        assertEquals(maxActions - AnswerActionMultiplier, itemsCount)
    }

    @Test
    fun largeScreenTrueAndAnswerActionIsDisplayed_answerActionMultiplierAppliedToMaxActions() {
        val answerDescription = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_answer)
        val flip = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_flip_camera)
        val camera = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_description_disable_camera)
        val mic = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_description_disable_microphone)
        val whiteboard = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_whiteboard)
        var itemsCount = -1
        val maxActions = 4
        composeTestRule.setContent {
            HSheetContent(
                maxActions = maxActions,
                callActions = ImmutableList(
                    listOf(
                        FlipCameraAction(),
                        CameraAction(),
                        MicAction(),
                        WhiteboardAction()
                    )
                ),
                showAnswerAction = true,
                isLargeScreen = true,
                isMoreToggled = false,
                onActionsPlaced = { itemsCount = it },
                onAnswerClick = {  },
                onHangUpClick = { },
                onMicToggle = { },
                onCameraToggle = { },
                onScreenShareToggle = { },
                onFlipCameraClick = { },
                onAudioClick = { },
                onChatClick = { },
                onFileShareClick = { },
                onWhiteboardClick = { },
                onVirtualBackgroundClick = { },
                onMoreToggle = { }
            )
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithContentDescription(flip).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(camera).assertIsNotDisplayed()
        composeTestRule.onNodeWithContentDescription(mic).assertIsNotDisplayed()
        composeTestRule.onNodeWithContentDescription(whiteboard).assertIsNotDisplayed()
        composeTestRule.onNodeWithText(answerDescription).assertIsDisplayed()
        assertEquals(maxActions - AnswerActionExtendedMultiplier, itemsCount)
    }

    @Test
    fun isLargeScreenFalse_answerActionWidthIsRegular() {
        val answerDescription =
            composeTestRule.activity.getString(R.string.kaleyra_call_sheet_answer)
        composeTestRule.setContent {
            HSheetContent(
                callActions = ImmutableList(),
                showAnswerAction = true,
                isLargeScreen = false,
                isMoreToggled = false,
                onActionsPlaced = { },
                onAnswerClick = { },
                onHangUpClick = { },
                onMicToggle = { },
                onCameraToggle = { },
                onScreenShareToggle = { },
                onFlipCameraClick = { },
                onAudioClick = { },
                onChatClick = { },
                onFileShareClick = { },
                onWhiteboardClick = { },
                onVirtualBackgroundClick = { },
                onMoreToggle = { }
            )
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText(answerDescription).assertWidthIsEqualTo(AnswerActionWidth)
    }

    @Test
    fun isLargeScreenTrue_answerActionWidthIsExtended() {
        val answerDescription =
            composeTestRule.activity.getString(R.string.kaleyra_call_sheet_answer)
        composeTestRule.setContent {
            HSheetContent(
                callActions = ImmutableList(),
                showAnswerAction = true,
                isLargeScreen = true,
                isMoreToggled = false,
                onActionsPlaced = { },
                onAnswerClick = { },
                onHangUpClick = { },
                onMicToggle = { },
                onCameraToggle = { },
                onScreenShareToggle = { },
                onFlipCameraClick = { },
                onAudioClick = { },
                onChatClick = { },
                onFileShareClick = { },
                onWhiteboardClick = { },
                onVirtualBackgroundClick = { },
                onMoreToggle = { }
            )
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText(answerDescription).assertWidthIsEqualTo(AnswerActionExtendedWidth)
    }

    @Test
    fun isLargeScreenFalse_hangUpActionWidthIsRegular() {
        val hangUpDescription = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_hang_up)
        composeTestRule.setContent {
            HSheetContent(
                callActions = ImmutableList(listOf(HangUpAction())),
                showAnswerAction = true,
                isLargeScreen = false,
                isMoreToggled = false,
                onActionsPlaced = { },
                onAnswerClick = { },
                onHangUpClick = { },
                onMicToggle = { },
                onCameraToggle = { },
                onScreenShareToggle = { },
                onFlipCameraClick = { },
                onAudioClick = { },
                onChatClick = { },
                onFileShareClick = { },
                onWhiteboardClick = { },
                onVirtualBackgroundClick = { },
                onMoreToggle = { }
            )
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithContentDescription(hangUpDescription).assertWidthIsEqualTo(HangUpActionWidth)
    }

    @Test
    fun isLargeScreenTrue_hangUpActionWidthIsExtended() {
        val hangUpDescription = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_hang_up)
        composeTestRule.setContent {
            HSheetContent(
                callActions = ImmutableList(listOf(HangUpAction())),
                showAnswerAction = true,
                isLargeScreen = true,
                isMoreToggled = false,
                onActionsPlaced = { },
                onAnswerClick = { },
                onHangUpClick = { },
                onMicToggle = { },
                onCameraToggle = { },
                onScreenShareToggle = { },
                onFlipCameraClick = { },
                onAudioClick = { },
                onChatClick = { },
                onFileShareClick = { },
                onWhiteboardClick = { },
                onVirtualBackgroundClick = { },
                onMoreToggle = { }
            )
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithContentDescription(hangUpDescription).assertWidthIsEqualTo(HangUpActionExtendedWidth)
    }
}