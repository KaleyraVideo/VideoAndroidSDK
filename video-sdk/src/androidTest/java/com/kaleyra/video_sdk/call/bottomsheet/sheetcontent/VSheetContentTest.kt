package com.kaleyra.video_sdk.call.bottomsheet.sheetcontent

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.height
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEqualTo
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.PermissionStatus
import com.kaleyra.video_common_ui.utils.extensions.ActivityExtensions
import com.kaleyra.video_common_ui.utils.extensions.ActivityExtensions.unlockDevice
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.call.bottomsheet.model.AudioAction
import com.kaleyra.video_sdk.call.bottomsheet.model.CallActionUI
import com.kaleyra.video_sdk.call.bottomsheet.model.CameraAction
import com.kaleyra.video_sdk.call.bottomsheet.model.ChatAction
import com.kaleyra.video_sdk.call.bottomsheet.model.FileShareAction
import com.kaleyra.video_sdk.call.bottomsheet.model.FlipCameraAction
import com.kaleyra.video_sdk.call.bottomsheet.model.HangUpAction
import com.kaleyra.video_sdk.call.bottomsheet.model.MicAction
import com.kaleyra.video_sdk.call.bottomsheet.model.ScreenShareAction
import com.kaleyra.video_sdk.call.bottomsheet.model.VirtualBackgroundAction
import com.kaleyra.video_sdk.call.bottomsheet.model.WhiteboardAction
import com.kaleyra.video_sdk.call.bottomsheet.view.sheetcontent.VSheetContent
import com.kaleyra.video_sdk.call.bottomsheet.view.sheetcontent.sheetitemslayout.SheetItemsSpacing
import com.kaleyra.video_sdk.call.callactions.model.CallActionsUiState
import com.kaleyra.video_sdk.call.callactions.viewmodel.CallActionsViewModel
import com.kaleyra.video_sdk.call.screen.model.InputPermissions
import com.kaleyra.video_sdk.call.screen.model.ModularComponent
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList
import com.kaleyra.video_sdk.common.immutablecollections.toImmutableList
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import io.mockk.verify
import junit.framework.TestCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalPermissionsApi::class)
class VSheetContentTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()


    private val callActionsUiState = MutableStateFlow(CallActionsUiState())

    private val callActionsViewModel = mockk<CallActionsViewModel>(relaxed = true) {
        every { uiState } returns callActionsUiState
    }

    @Test
    fun actionsOverflow_onActionsPlacedHaveOverflown() {
        callActionsUiState.value = CallActionsUiState(
            actionList = listOf(HangUpAction(), MicAction(), CameraAction(), ScreenShareAction.UserChoice(), FlipCameraAction(), WhiteboardAction()).toImmutableList()
        )
        var overflowedActions: ImmutableList<CallActionUI>? = null
        composeTestRule.setContent {
            VSheetContent(
                viewModel = callActionsViewModel,
                isMoreToggled = false,
                onMoreToggle = {},
                onAskInputPermissions = {},
                onActionsOverflow = { overflowedActions = it },
                onModularComponentRequest = {},
                modifier = Modifier.height(300.dp)
            )
        }

        val expected = ImmutableList(listOf(ScreenShareAction.UserChoice(), FlipCameraAction(), WhiteboardAction()))
        assertEquals(expected, overflowedActions)
    }

    @Test
    fun userClicksAccept_acceptInvoked() {
        callActionsUiState.value = CallActionsUiState(isRinging = true)
        composeTestRule.setContent {
            VSheetContent(
                viewModel = callActionsViewModel,
                isMoreToggled = false,
                onMoreToggle = {},
                onActionsOverflow = {},
                onModularComponentRequest = {},
                onAskInputPermissions = {},
            )
        }

        val text = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_answer)
        composeTestRule
            .onNodeWithContentDescription(text, useUnmergedTree = true)
            .assertIsDisplayed()
            .performClick()

        verify(exactly = 1) { callActionsViewModel.accept() }
    }

    @Test
    fun userClicksHangUp_hangUpInvoked() {
        callActionsUiState.value = CallActionsUiState(actionList = listOf(HangUpAction()).toImmutableList())
        composeTestRule.setContent {
            VSheetContent(
                viewModel = callActionsViewModel,
                isMoreToggled = false,
                onMoreToggle = {},
                onActionsOverflow = {},
                onModularComponentRequest = {},
                onAskInputPermissions = {},
            )
        }

        val text = composeTestRule.activity.getString(R.string.kaleyra_strings_action_call_end)
        composeTestRule
            .onNodeWithContentDescription(text, useUnmergedTree = true)
            .assertIsDisplayed()
            .performClick()

        verify(exactly = 1) { callActionsViewModel.hangUp() }
    }

    @Test
    fun userTogglesMic_toggleMicInvoked() {
        val micPermission = mockk<PermissionState>(relaxed = true) {
            every { status } returns PermissionStatus.Granted
        }
        callActionsUiState.value = CallActionsUiState(actionList = listOf(MicAction()).toImmutableList())
        composeTestRule.setContent {
            VSheetContent(
                viewModel = callActionsViewModel,
                isMoreToggled = false,
                inputPermissions = InputPermissions(micPermission = micPermission),
                onMoreToggle = {},
                onActionsOverflow = {},
                onModularComponentRequest = {},
                onAskInputPermissions = {},
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
    fun userTogglesMicWithoutPermission_micPermissionLaunched() {
        val micPermission = mockk<PermissionState>(relaxed = true) {
            every { status } returns PermissionStatus.Denied(false)
        }
        callActionsUiState.value = CallActionsUiState(actionList = listOf(MicAction()).toImmutableList())
        composeTestRule.setContent {
            VSheetContent(
                viewModel = callActionsViewModel,
                isMoreToggled = false,
                inputPermissions = InputPermissions(micPermission = micPermission),
                onMoreToggle = {},
                onActionsOverflow = {},
                onModularComponentRequest = {},
                onAskInputPermissions = {},
            )
        }

        val text = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_description_disable_microphone)
        composeTestRule
            .onNodeWithContentDescription(text, useUnmergedTree = true)
            .assertIsDisplayed()
            .performClick()

        verify(exactly = 1) { micPermission.launchPermissionRequest() }
    }

    @Test
    fun userTogglesCamera_toggleCameraInvoked() {
        val cameraPermission = mockk<PermissionState>(relaxed = true) {
            every { status } returns PermissionStatus.Granted
        }
        callActionsUiState.value = CallActionsUiState(actionList = listOf(CameraAction()).toImmutableList())
        composeTestRule.setContent {
            VSheetContent(
                viewModel = callActionsViewModel,
                isMoreToggled = false,
                inputPermissions = InputPermissions(cameraPermission = cameraPermission),
                onMoreToggle = {},
                onActionsOverflow = {},
                onModularComponentRequest = {},
                onAskInputPermissions = {},
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
    fun userTogglesCameraWithoutPermission_cameraPermissionLaunched() {
        val cameraPermission = mockk<PermissionState>(relaxed = true) {
            every { status } returns PermissionStatus.Denied(false)
        }
        callActionsUiState.value = CallActionsUiState(actionList = listOf(CameraAction()).toImmutableList())
        composeTestRule.setContent {
            VSheetContent(
                viewModel = callActionsViewModel,
                isMoreToggled = false,
                inputPermissions = InputPermissions(cameraPermission = cameraPermission),
                onMoreToggle = {},
                onActionsOverflow = {},
                onModularComponentRequest = {},
                onAskInputPermissions = {},
            )
        }

        val text = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_description_disable_camera)
        composeTestRule
            .onNodeWithContentDescription(text, useUnmergedTree = true)
            .assertIsDisplayed()
            .performClick()

        verify(exactly = 1) { cameraPermission.launchPermissionRequest() }
    }

    @Test
    fun userClicksChat_showChatInvoked() {
        mockkObject(ActivityExtensions)
        callActionsUiState.value = CallActionsUiState(actionList = listOf(ChatAction()).toImmutableList())
        composeTestRule.setContent {
            VSheetContent(
                viewModel = callActionsViewModel,
                isMoreToggled = false,
                onMoreToggle = {},
                onActionsOverflow = {},
                onModularComponentRequest = {},
                onAskInputPermissions = {},
            )
        }

        val text = composeTestRule.activity.getString(R.string.kaleyra_strings_action_chat)
        composeTestRule
            .onNodeWithContentDescription(text, useUnmergedTree = true)
            .assertIsDisplayed()
            .performClick()

        verify(exactly = 1) { callActionsViewModel.showChat(any()) }
        verify(exactly = 1) { composeTestRule.activity.unlockDevice(any()) }
        unmockkObject(ActivityExtensions)
    }

    @Test
    fun userClicksFlipCamera_switchCameraInvoked() {
        callActionsUiState.value = CallActionsUiState(actionList = listOf(FlipCameraAction()).toImmutableList())
        composeTestRule.setContent {
            VSheetContent(
                viewModel = callActionsViewModel,
                isMoreToggled = false,
                onMoreToggle = {},
                onActionsOverflow = {},
                onModularComponentRequest = {},
                onAskInputPermissions = {},
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
    fun userClicksAudio_onModularComponentRequestAudio() {
        callActionsUiState.value = CallActionsUiState(actionList = listOf(AudioAction()).toImmutableList())
        var component: ModularComponent? = null
        composeTestRule.setContent {
            VSheetContent(
                viewModel = callActionsViewModel,
                isMoreToggled = false,
                onMoreToggle = {},
                onActionsOverflow = {},
                onModularComponentRequest = { component = it },
                onAskInputPermissions = {},
            )
        }

        val text = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_audio)
        composeTestRule
            .onNodeWithContentDescription(text, useUnmergedTree = true)
            .assertIsDisplayed()
            .performClick()

        TestCase.assertEquals(ModularComponent.Audio, component)
    }

    @Test
    fun userClicksFileShare_onModularComponentRequestFileShare() {
        callActionsUiState.value = CallActionsUiState(actionList = listOf(FileShareAction()).toImmutableList())
        var component: ModularComponent? = null
        composeTestRule.setContent {
            VSheetContent(
                viewModel = callActionsViewModel,
                isMoreToggled = false,
                onMoreToggle = {},
                onActionsOverflow = {},
                onModularComponentRequest = { component = it },
                onAskInputPermissions = {},
            )
        }

        val text = composeTestRule.activity.getString(R.string.kaleyra_strings_action_files)
        composeTestRule
            .onNodeWithContentDescription(text, useUnmergedTree = true)
            .assertIsDisplayed()
            .performClick()

        TestCase.assertEquals(ModularComponent.FileShare, component)
        verify(exactly = 1) { callActionsViewModel.clearFileShareBadge() }
    }

    @Test
    fun userClicksWhiteboard_onModularComponentRequestWhiteboard() {
        callActionsUiState.value = CallActionsUiState(actionList = listOf(WhiteboardAction()).toImmutableList())
        var component: ModularComponent? = null
        composeTestRule.setContent {
            VSheetContent(
                viewModel = callActionsViewModel,
                isMoreToggled = false,
                onMoreToggle = {},
                onActionsOverflow = {},
                onModularComponentRequest = { component = it },
                onAskInputPermissions = {},
            )
        }

        val text = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_whiteboard)
        composeTestRule
            .onNodeWithContentDescription(text, useUnmergedTree = true)
            .assertIsDisplayed()
            .performClick()

        TestCase.assertEquals(ModularComponent.Whiteboard, component)
    }

    @Test
    fun userClicksVirtualBackground_onModularComponentRequestVirtualBackground() {
        callActionsUiState.value = CallActionsUiState(actionList = listOf(VirtualBackgroundAction()).toImmutableList())
        var component: ModularComponent? = null
        composeTestRule.setContent {
            VSheetContent(
                viewModel = callActionsViewModel,
                isMoreToggled = false,
                onMoreToggle = {},
                onActionsOverflow = {},
                onModularComponentRequest = { component = it },
                onAskInputPermissions = {},
            )
        }

        val text = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_virtual_background)
        composeTestRule
            .onNodeWithContentDescription(text, useUnmergedTree = true)
            .assertIsDisplayed()
            .performClick()

        TestCase.assertEquals(ModularComponent.VirtualBackground, component)
    }

    @Test
    fun userClicksScreenShareUserChoiceWhenEnabled_tryStopScreenShareInvoked() {
        callActionsUiState.value = CallActionsUiState(actionList = listOf(ScreenShareAction.UserChoice()).toImmutableList())
        every { callActionsViewModel.tryStopScreenShare() } returns true
        composeTestRule.setContent {
            VSheetContent(
                viewModel = callActionsViewModel,
                isMoreToggled = false,
                onMoreToggle = {},
                onActionsOverflow = {},
                onModularComponentRequest = {},
                onAskInputPermissions = {},
            )
        }

        val text = composeTestRule.activity.getString(R.string.kaleyra_strings_action_start_screen_share)
        composeTestRule
            .onNodeWithContentDescription(text, useUnmergedTree = true)
            .assertIsDisplayed()
            .performClick()

        verify(exactly = 1) { callActionsViewModel.tryStopScreenShare() }
    }

    @Test
    fun userClicksScreenShareAppWhenEnabled_tryStopScreenShareInvoked() {
        callActionsUiState.value = CallActionsUiState(actionList = listOf(ScreenShareAction.App()).toImmutableList())
        every { callActionsViewModel.tryStopScreenShare() } returns true
        composeTestRule.setContent {
            VSheetContent(
                viewModel = callActionsViewModel,
                isMoreToggled = false,
                onMoreToggle = {},
                onActionsOverflow = {},
                onModularComponentRequest = {},
                onAskInputPermissions = {},
            )
        }

        val text = composeTestRule.activity.getString(R.string.kaleyra_strings_action_start_screen_share)
        composeTestRule
            .onNodeWithContentDescription(text, useUnmergedTree = true)
            .assertIsDisplayed()
            .performClick()

        verify(exactly = 1) { callActionsViewModel.tryStopScreenShare() }
    }

    @Test
    fun userClicksScreenShareWholeDeviceWhenEnabled_tryStopScreenShareInvoked() {
        callActionsUiState.value = CallActionsUiState(actionList = listOf(ScreenShareAction.WholeDevice()).toImmutableList())
        every { callActionsViewModel.tryStopScreenShare() } returns true
        composeTestRule.setContent {
            VSheetContent(
                viewModel = callActionsViewModel,
                isMoreToggled = false,
                onMoreToggle = {},
                onActionsOverflow = {},
                onModularComponentRequest = {},
                onAskInputPermissions = {},
            )
        }

        val text = composeTestRule.activity.getString(R.string.kaleyra_strings_action_start_screen_share)
        composeTestRule
            .onNodeWithContentDescription(text, useUnmergedTree = true)
            .assertIsDisplayed()
            .performClick()

        verify(exactly = 1) { callActionsViewModel.tryStopScreenShare() }
    }

    @Test
    fun userClicksScreenShareUserChoiceWhenNotEnabled_onModularComponentRequestScreenShare() {
        callActionsUiState.value = CallActionsUiState(actionList = listOf(ScreenShareAction.UserChoice()).toImmutableList())
        every { callActionsViewModel.tryStopScreenShare() } returns false
        var component: ModularComponent? = null
        composeTestRule.setContent {
            VSheetContent(
                viewModel = callActionsViewModel,
                isMoreToggled = false,
                onMoreToggle = {},
                onActionsOverflow = {},
                onModularComponentRequest = { component = it },
                onAskInputPermissions = {},
            )
        }

        val text = composeTestRule.activity.getString(R.string.kaleyra_strings_action_start_screen_share)
        composeTestRule
            .onNodeWithContentDescription(text, useUnmergedTree = true)
            .assertIsDisplayed()
            .performClick()

        TestCase.assertEquals(ModularComponent.ScreenShare, component)
    }

    @Test
    fun userClicksScreenShareUserAppWhenNotEnabled_onModularScreenShareComponentNotRequested() {
        callActionsUiState.value = CallActionsUiState(actionList = listOf(ScreenShareAction.App()).toImmutableList())
        every { callActionsViewModel.tryStopScreenShare() } returns false
        var component: ModularComponent? = null
        composeTestRule.setContent {
            VSheetContent(
                viewModel = callActionsViewModel,
                isMoreToggled = false,
                onMoreToggle = {},
                onActionsOverflow = {},
                onModularComponentRequest = { component = it },
                onAskInputPermissions = {},
            )
        }

        val text = composeTestRule.activity.getString(R.string.kaleyra_strings_action_start_screen_share)
        composeTestRule
            .onNodeWithContentDescription(text, useUnmergedTree = true)
            .assertIsDisplayed()
            .performClick()

        TestCase.assertEquals(null, component)
    }

    @Test
    fun userClicksScreenShareUserWholeDeviceWhenNotEnabled_onModularScreenShareComponentNotRequested() {
        callActionsUiState.value = CallActionsUiState(actionList = listOf(ScreenShareAction.WholeDevice()).toImmutableList())
        every { callActionsViewModel.tryStopScreenShare() } returns false
        var component: ModularComponent? = null
        var onAskingInputPermissions = false
        composeTestRule.setContent {
            VSheetContent(
                viewModel = callActionsViewModel,
                screenShareViewModel = mockk(relaxed = true),
                isMoreToggled = false,
                onMoreToggle = {},
                onActionsOverflow = {},
                onModularComponentRequest = { component = it },
                onAskInputPermissions = { onAskingInputPermissions = it },
            )
        }

        val text = composeTestRule.activity.getString(R.string.kaleyra_strings_action_start_screen_share)
        composeTestRule
            .onNodeWithContentDescription(text, useUnmergedTree = true)
            .assertIsDisplayed()
            .performClick()

        assertEquals(true, onAskingInputPermissions)
        TestCase.assertEquals(null, component)
    }

    @Test
    fun testMoreActionNotToggled() {
        composeTestRule.setContent {
            VSheetContent(
                modifier = Modifier.height(150.dp),
                callActions = ImmutableList(
                    listOf(
                        FlipCameraAction(),
                        FlipCameraAction(),
                        FlipCameraAction(),
                        FlipCameraAction(),
                    )
                ),
                showAnswerAction = false,
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
                onVirtualBackgroundToggle = { },
                onMoreToggle = { },
                onSignatureClick = {}
            )
        }
        val text = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_description_more_actions)
        composeTestRule.onNodeWithContentDescription(text).assertIsDisplayed()
    }

    @Test
    fun testMoreActionToggled() {
        composeTestRule.setContent {
            VSheetContent(
                modifier = Modifier.height(150.dp),
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
                onVirtualBackgroundToggle = { },
                onMoreToggle = { },
                onSignatureClick = {}
            )
        }
        val text = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_description_hide_actions)
        composeTestRule.onNodeWithContentDescription(text).assertIsDisplayed()
    }

    @Test
    fun testMoreActionNotificationCountIsDisplayed() = runTest {
        composeTestRule.setContent {
            VSheetContent(
                modifier = Modifier.height(150.dp),
                callActions = ImmutableList(
                    listOf(
                        FileShareAction(notificationCount = 2),
                        FlipCameraAction(),
                        WhiteboardAction(notificationCount = 3),
                        ChatAction(notificationCount = 5),
                        FlipCameraAction()
                    )
                ),
                showAnswerAction = false,
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
                onVirtualBackgroundToggle = { },
                onMoreToggle = { },
                onSignatureClick = {}
            )
        }
        composeTestRule.onNodeWithText("8").assertIsDisplayed()
    }

    @Test
    fun zeroNotificationCount_moreActionNotificationCountDoesNotExists() {
        composeTestRule.setContent {
            VSheetContent(
                modifier = Modifier.height(150.dp),
                callActions = ImmutableList(
                    listOf(
                        FileShareAction(notificationCount = 0),
                        FlipCameraAction(),
                        WhiteboardAction(notificationCount = 0),
                        FlipCameraAction()
                    )
                ),
                showAnswerAction = false,
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
                onVirtualBackgroundToggle = { },
                onMoreToggle = { },
                onSignatureClick = {}
            )
        }
        composeTestRule.onNodeWithText("0").assertDoesNotExist()
    }

    @Test
    fun overNinetyNineNotificationCount_moreActionNotificationCountIsNinetyNinePlus() = runTest {
        composeTestRule.setContent {
            VSheetContent(
                modifier = Modifier.height(150.dp),
                callActions = ImmutableList(
                    listOf(
                        FileShareAction(notificationCount = 2),
                        FlipCameraAction(),
                        WhiteboardAction(notificationCount = 50),
                        ChatAction(notificationCount = 50),
                        FlipCameraAction()
                    )
                ),
                showAnswerAction = false,
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
                onVirtualBackgroundToggle = { },
                onMoreToggle = { },
                onSignatureClick = {}
            )
        }
        val text = composeTestRule.activity.getString(R.string.kaleyra_call_badge_count_overflow)
        composeTestRule.onNodeWithText(text).assertIsDisplayed()
    }

    @Test
    fun showAnswerActionTrue_answerActionIsDisplayed() {
        val answerDescription =
            composeTestRule.activity.getString(R.string.kaleyra_call_sheet_answer)
        composeTestRule.setContent {
            VSheetContent(
                callActions = ImmutableList(),
                showAnswerAction = true,
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
                onVirtualBackgroundToggle = { },
                onMoreToggle = { },
                onSignatureClick = {}
            )
        }
        composeTestRule.onNodeWithContentDescription(answerDescription).assertIsDisplayed()
    }

    @Test
    fun showAnswerActionFalse_answerActionIsNotDisplayed() {
        val answerDescription =
            composeTestRule.activity.getString(R.string.kaleyra_call_sheet_answer)
        composeTestRule.setContent {
            VSheetContent(
                callActions = ImmutableList(),
                showAnswerAction = false,
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
                onVirtualBackgroundToggle = { },
                onMoreToggle = { },
                onSignatureClick = {}
            )
        }
        composeTestRule.onNodeWithText(answerDescription).assertDoesNotExist()
    }

    @Test
    fun testOnHangUpActionClick() {
        var isHangUpClicked = false
        val hangUpDescription = composeTestRule.activity.getString(R.string.kaleyra_strings_action_call_end)
        composeTestRule.setContent {
            VSheetContent(
                callActions = ImmutableList(listOf(HangUpAction())),
                showAnswerAction = true,
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
                onVirtualBackgroundToggle = { },
                onMoreToggle = { },
                onSignatureClick = {}
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
            VSheetContent(
                callActions = ImmutableList(listOf(MicAction())),
                showAnswerAction = true,
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
                onVirtualBackgroundToggle = { },
                onMoreToggle = { },
                onSignatureClick = {}
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
            VSheetContent(
                callActions = ImmutableList(listOf(CameraAction())),
                showAnswerAction = true,
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
                onVirtualBackgroundToggle = { },
                onMoreToggle = { },
                onSignatureClick = {}
            )
        }
        composeTestRule.onNodeWithContentDescription(cameraDescription).assertHasClickAction()
        composeTestRule.onNodeWithContentDescription(cameraDescription).performClick()
        assertEquals(true, isCameraClicked)
    }

    @Test
    fun testOnScreenShareActionClick() {
        var isScreenShareClicked: Boolean? = null
        val screenShareDescription = composeTestRule.activity.getString(R.string.kaleyra_strings_action_start_screen_share)
        composeTestRule.setContent {
            VSheetContent(
                callActions = ImmutableList(listOf(ScreenShareAction.UserChoice())),
                showAnswerAction = true,
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
                onVirtualBackgroundToggle = { },
                onMoreToggle = { },
                onSignatureClick = {}
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
            VSheetContent(
                callActions = ImmutableList(listOf(FlipCameraAction())),
                showAnswerAction = true,
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
                onVirtualBackgroundToggle = { },
                onMoreToggle = { },
                onSignatureClick = {}
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
            VSheetContent(
                callActions = ImmutableList(listOf(AudioAction())),
                showAnswerAction = true,
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
                onVirtualBackgroundToggle = { },
                onMoreToggle = { },
                onSignatureClick = {}
            )
        }
        composeTestRule.onNodeWithContentDescription(audioDescription).assertHasClickAction()
        composeTestRule.onNodeWithContentDescription(audioDescription).performClick()
        assertEquals(true, isAudioClicked)
    }

    @Test
    fun testOnChatActionClick() {
        var isChatClicked = false
        val chatDescription = composeTestRule.activity.getString(R.string.kaleyra_strings_action_chat)
        composeTestRule.setContent {
            VSheetContent(
                callActions = ImmutableList(listOf(ChatAction())),
                showAnswerAction = true,
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
                onVirtualBackgroundToggle = { },
                onMoreToggle = { },
                onSignatureClick = {}
            )
        }
        composeTestRule.onNodeWithContentDescription(chatDescription).assertHasClickAction()
        composeTestRule.onNodeWithContentDescription(chatDescription).performClick()
        assertEquals(true, isChatClicked)
    }

    @Test
    fun testOnFileShareActionClick() {
        var isFileShareClicked = false
        val fileShareDescription = composeTestRule.activity.getString(R.string.kaleyra_strings_action_files)
        composeTestRule.setContent {
            VSheetContent(
                callActions = ImmutableList(listOf(FileShareAction())),
                showAnswerAction = true,
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
                onVirtualBackgroundToggle = { },
                onMoreToggle = { },
                onSignatureClick = {}
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
            VSheetContent(
                callActions = ImmutableList(listOf(WhiteboardAction())),
                showAnswerAction = true,
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
                onVirtualBackgroundToggle = { },
                onMoreToggle = { },
                onSignatureClick = {}
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
            VSheetContent(
                callActions = ImmutableList(listOf(VirtualBackgroundAction())),
                showAnswerAction = true,
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
                onVirtualBackgroundToggle = { isVirtualClicked = true },
                onMoreToggle = { },
                onSignatureClick = {}
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
            VSheetContent(
                callActions = ImmutableList(),
                showAnswerAction = true,
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
                onVirtualBackgroundToggle = { },
                onMoreToggle = { },
                onSignatureClick = {}
            )
        }
        composeTestRule.onNodeWithContentDescription(answerDescription).assertHasClickAction()
        composeTestRule.onNodeWithContentDescription(answerDescription).performClick()
        assertEquals(true, isAnswerClicked)
    }

    @Test
    fun testonMoreToggle() {
        var isMoreClicked = false
        val moreDescription =
            composeTestRule.activity.getString(R.string.kaleyra_call_sheet_description_more_actions)
        composeTestRule.setContent {
            VSheetContent(
                modifier = Modifier.height(100.dp),
                callActions = ImmutableList(
                    listOf(
                        FlipCameraAction(),
                        FlipCameraAction(),
                        FlipCameraAction(),
                    )
                ),
                showAnswerAction = false,
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
                onVirtualBackgroundToggle = { },
                onSignatureClick = {},
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
            VSheetContent(
                modifier = Modifier.height(150.dp),
                callActions = ImmutableList(
                    listOf(
                        FlipCameraAction(),
                        FlipCameraAction(),
                        FlipCameraAction(),
                        FlipCameraAction()
                    )
                ),
                showAnswerAction = false,
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
                onVirtualBackgroundToggle = { },
                onMoreToggle = { },
                onSignatureClick = {}
            )
        }
        composeTestRule.onNodeWithContentDescription(moreDescription).assertIsDisplayed()
    }

    @Test
    fun allActionsCanBeDisplayed_moreActionDoesNotExists() {
        val moreDescription =
            composeTestRule.activity.getString(R.string.kaleyra_call_sheet_description_more_actions)
        composeTestRule.setContent {
            VSheetContent(
                modifier = Modifier.height(150.dp),
                callActions = ImmutableList(
                    listOf(
                        FlipCameraAction(),
                        FlipCameraAction()
                    )
                ),
                showAnswerAction = false,
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
                onVirtualBackgroundToggle = { },
                onMoreToggle = { },
                onSignatureClick = {}
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
            VSheetContent(
                modifier = Modifier.height(200.dp),
                callActions = ImmutableList(
                    listOf(
                        FlipCameraAction(),
                        FlipCameraAction(),
                        FlipCameraAction(),
                        FlipCameraAction()
                    )
                ),
                showAnswerAction = true,
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
                onVirtualBackgroundToggle = { },
                onMoreToggle = { },
                onSignatureClick = {}
            )
        }
        composeTestRule.onNodeWithContentDescription(answerDescription).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(moreDescription).assertDoesNotExist()
    }

    @Test
    fun testSheetContentActionsPlacing() {
        val moreDescription = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_description_more_actions)
        val flip = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_flip_camera)
        val camera = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_description_disable_camera)
        composeTestRule.setContent {
            VSheetContent(
                modifier = Modifier.height(200.dp),
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
                onVirtualBackgroundToggle = { },
                onMoreToggle = { },
                onSignatureClick = {}
            )
        }
        val childBounds1 = composeTestRule.onNodeWithContentDescription(flip).getBoundsInRoot()
        val childBounds2 = composeTestRule.onNodeWithContentDescription(camera).getBoundsInRoot()
        val moreChild = composeTestRule.onNodeWithContentDescription(moreDescription).getBoundsInRoot()
        childBounds2.bottom.assertIsEqualTo(childBounds1.top - SheetItemsSpacing, "child 2 bottom")
        moreChild.bottom.assertIsEqualTo(childBounds2.top - SheetItemsSpacing, "more child top")
    }

    @Test
    fun testOnActionsPlacedCallback() {
        var itemsCount = -1
        composeTestRule.setContent {
            VSheetContent(
                modifier = Modifier.height(200.dp),
                callActions = ImmutableList(
                    listOf(
                        FlipCameraAction(),
                        FlipCameraAction(),
                        FlipCameraAction(),
                        FlipCameraAction()
                    )
                ),
                showAnswerAction = false,
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
                onVirtualBackgroundToggle = { },
                onMoreToggle = { },
                onSignatureClick = {}
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
        val chat = composeTestRule.activity.getString(R.string.kaleyra_strings_action_chat)
        var itemsCount = -1
        val maxActions = 3
        composeTestRule.setContent {
            VSheetContent(
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
                onVirtualBackgroundToggle = { },
                onMoreToggle = { },
                onSignatureClick = {}
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
        val chat = composeTestRule.activity.getString(R.string.kaleyra_strings_action_chat)
        var itemsCount = -1
        val maxActions = 4
        composeTestRule.setContent {
            VSheetContent(
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
                onVirtualBackgroundToggle = { },
                onMoreToggle = { },
                onSignatureClick = {}
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
    fun answerActionIsDisplayed_actionsAreOneLess() {
        val answerDescription = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_answer)
        val flip = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_flip_camera)
        val camera = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_description_disable_camera)
        val mic = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_description_disable_microphone)
        var itemsCount = -1
        val maxActions = 2
        composeTestRule.setContent {
            VSheetContent(
                maxActions = maxActions,
                callActions = ImmutableList(
                    listOf(
                        FlipCameraAction(),
                        CameraAction(),
                        MicAction()
                    )
                ),
                showAnswerAction = true,
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
                onVirtualBackgroundToggle = { },
                onMoreToggle = { },
                onSignatureClick = {}
            )
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithContentDescription(flip).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(camera).assertIsNotDisplayed()
        composeTestRule.onNodeWithContentDescription(mic).assertIsNotDisplayed()
        composeTestRule.onNodeWithContentDescription(answerDescription).assertIsDisplayed()
        assertEquals(maxActions - 1, itemsCount)
    }
}