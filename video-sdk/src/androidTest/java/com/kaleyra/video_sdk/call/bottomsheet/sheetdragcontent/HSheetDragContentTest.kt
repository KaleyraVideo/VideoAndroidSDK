package com.kaleyra.video_sdk.call.bottomsheet.sheetdragcontent

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEqualTo
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.PermissionStatus
import com.kaleyra.video_common_ui.utils.extensions.ActivityExtensions
import com.kaleyra.video_common_ui.utils.extensions.ActivityExtensions.unlockDevice
import com.kaleyra.video_sdk.R
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
import com.kaleyra.video_sdk.call.bottomsheet.view.sheetcontent.sheetitemslayout.SheetItemsSpacing
import com.kaleyra.video_sdk.call.bottomsheet.view.sheetdragcontent.HSheetDragContent
import com.kaleyra.video_sdk.call.bottomsheet.view.sheetdragcontent.HSheetDragHorizontalPadding
import com.kaleyra.video_sdk.call.bottomsheet.view.sheetdragcontent.HSheetDragVerticalPadding
import com.kaleyra.video_sdk.call.callactions.model.CallActionsUiState
import com.kaleyra.video_sdk.call.callactions.view.CallActionDefaults
import com.kaleyra.video_sdk.call.callactions.viewmodel.CallActionsViewModel
import com.kaleyra.video_sdk.call.screen.model.InputPermissions
import com.kaleyra.video_sdk.call.screen.model.ModularComponent
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import io.mockk.verify
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalPermissionsApi::class)
class HSheetDragContentTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val callActionsUiState = MutableStateFlow(CallActionsUiState())

    private val callActionsViewModel = mockk<CallActionsViewModel>(relaxed = true) {
        every { uiState } returns callActionsUiState
    }

    @Test
    fun largeScreenAndHangUpActionInBaseBottomSheet_twoItemsPerRowInSheetDragContent() {
        callActionsUiState.value = CallActionsUiState(
            actionList = ImmutableList(listOf(HangUpAction(), FileShareAction(), FlipCameraAction()))
        )
        composeTestRule.setContent {
            HSheetDragContent(
                viewModel = callActionsViewModel,
                callActions = ImmutableList(listOf(FileShareAction(), FlipCameraAction())),
                isLargeScreen = true,
                onAskInputPermissions = {},
                onModularComponentRequest = {}
            )
        }

        val fileShareText = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_file_share)
        val fileShareLeft = composeTestRule.onNodeWithText(fileShareText).getBoundsInRoot().left
        val fileShareRight = composeTestRule.onNodeWithText(fileShareText).getBoundsInRoot().right

        val flipCameraText = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_flip_camera)
        val flipCameraLeft = composeTestRule.onNodeWithText(flipCameraText).getBoundsInRoot().left
        val flipCameraRight = composeTestRule.onNodeWithText(flipCameraText).getBoundsInRoot().right

        assertEquals(0.dp, fileShareLeft)
        assertEquals(fileShareRight + SheetItemsSpacing, flipCameraLeft)
        assertEquals(composeTestRule.onRoot().getBoundsInRoot().right, flipCameraRight)
    }

    @Test
    fun smallScreenAndHangUpActionInBaseBottomSheet_oneItemPerRowInSheetDragContent() {
        callActionsUiState.value = CallActionsUiState(
            actionList = ImmutableList(listOf(HangUpAction()))
        )
        composeTestRule.setContent {
            HSheetDragContent(
                viewModel = callActionsViewModel,
                callActions = ImmutableList(listOf(FileShareAction())),
                isLargeScreen = false,
                onAskInputPermissions = {},
                onModularComponentRequest = {}
            )
        }

        val fileShareText = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_file_share)
        val fileShareLeft = composeTestRule.onNodeWithText(fileShareText).getBoundsInRoot().left
        val fileShareRight = composeTestRule.onNodeWithText(fileShareText).getBoundsInRoot().right

        assertEquals(0.dp, fileShareLeft)
        assertEquals(composeTestRule.onRoot().getBoundsInRoot().right, fileShareRight)
    }

    @Test
    fun largeScreenAndAnswerActionInBaseBottomSheet_threeItemsPerRowInSheetDragContent() {
        callActionsUiState.value = CallActionsUiState(
            actionList = ImmutableList(listOf(FileShareAction(), FlipCameraAction(), WhiteboardAction())),
            isRinging = true
        )
        composeTestRule.setContent {
            HSheetDragContent(
                viewModel = callActionsViewModel,
                callActions = ImmutableList(listOf(FileShareAction(), FlipCameraAction(), WhiteboardAction())),
                isLargeScreen = true,
                onAskInputPermissions = {},
                onModularComponentRequest = {}
            )
        }

        val fileShareText = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_file_share)
        val fileShareLeft = composeTestRule.onNodeWithText(fileShareText).getBoundsInRoot().left
        val fileShareRight = composeTestRule.onNodeWithText(fileShareText).getBoundsInRoot().right

        val flipCameraText = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_flip_camera)
        val flipCameraLeft = composeTestRule.onNodeWithText(flipCameraText).getBoundsInRoot().left
        val flipCameraRight = composeTestRule.onNodeWithText(flipCameraText).getBoundsInRoot().right

        val whiteboardText = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_whiteboard)
        val whiteboardLeft = composeTestRule.onNodeWithText(whiteboardText).getBoundsInRoot().left
        val whiteboardRight = composeTestRule.onNodeWithText(whiteboardText).getBoundsInRoot().right

        assertEquals(0.dp, fileShareLeft)
        assertEquals(fileShareRight + SheetItemsSpacing, flipCameraLeft)
        assertEquals(flipCameraRight + SheetItemsSpacing, whiteboardLeft)
        assertEquals(composeTestRule.onRoot().getBoundsInRoot().right, whiteboardRight)
    }

    @Test
    fun smallScreenAndAnswerActionInBaseBottomSheet_twoItemsPerRowInSheetDragContent() {
        callActionsUiState.value = CallActionsUiState(
            actionList = ImmutableList(listOf(FileShareAction(), FlipCameraAction())),
            isRinging = true
        )
        composeTestRule.setContent {
            HSheetDragContent(
                viewModel = callActionsViewModel,
                callActions = ImmutableList(listOf(FileShareAction(), FlipCameraAction())),
                isLargeScreen = true,
                onAskInputPermissions = {},
                onModularComponentRequest = {}
            )
        }

        val fileShareText = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_file_share)
        val fileShareLeft = composeTestRule.onNodeWithText(fileShareText).getBoundsInRoot().left
        val fileShareRight = composeTestRule.onNodeWithText(fileShareText).getBoundsInRoot().right

        val flipCameraText = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_flip_camera)
        val flipCameraLeft = composeTestRule.onNodeWithText(flipCameraText).getBoundsInRoot().left
        val flipCameraRight = composeTestRule.onNodeWithText(flipCameraText).getBoundsInRoot().right

        assertEquals(0.dp, fileShareLeft)
        assertEquals(fileShareRight + SheetItemsSpacing, flipCameraLeft)
        assertEquals(composeTestRule.onRoot().getBoundsInRoot().right, flipCameraRight)
    }

    @Test
    fun isRingingFalse_oneItemsPerRowInSheetDragContent() {
        callActionsUiState.value = CallActionsUiState(
            actionList = ImmutableList(listOf(FileShareAction(), FlipCameraAction(), WhiteboardAction())),
        )
        composeTestRule.setContent {
            HSheetDragContent(
                viewModel = callActionsViewModel,
                callActions = ImmutableList(listOf(FileShareAction(), FlipCameraAction(), WhiteboardAction())),
                isLargeScreen = true,
                onAskInputPermissions = {},
                onModularComponentRequest = {}
            )
        }

        val flipCameraText = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_flip_camera)
        val flipCameraLeft = composeTestRule.onNodeWithText(flipCameraText).getBoundsInRoot().left
        val flipCameraRight = composeTestRule.onNodeWithText(flipCameraText).getBoundsInRoot().right

        assertEquals(0.dp, flipCameraLeft)
        assertEquals(composeTestRule.onRoot().getBoundsInRoot().right, flipCameraRight)
    }

    @Test
    fun userClicksHangUp_hangUpInvoked() {
        composeTestRule.setContent {
            HSheetDragContent(
                viewModel = callActionsViewModel,
                callActions = ImmutableList(listOf(HangUpAction())),
                isLargeScreen = false,
                onAskInputPermissions = {},
                onModularComponentRequest = {}
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
        val micPermission = mockk<PermissionState>(relaxed = true) {
            every { status } returns PermissionStatus.Granted
        }
        composeTestRule.setContent {
            HSheetDragContent(
                viewModel = callActionsViewModel,
                callActions = ImmutableList(listOf(MicAction())),
                isLargeScreen = false,
                onModularComponentRequest = {},
                onAskInputPermissions = {},
                inputPermissions = InputPermissions(micPermission = micPermission)
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
        composeTestRule.setContent {
            HSheetDragContent(
                viewModel = callActionsViewModel,
                callActions = ImmutableList(listOf(MicAction())),
                isLargeScreen = false,
                onModularComponentRequest = {},
                onAskInputPermissions = {},
                inputPermissions = InputPermissions(micPermission = micPermission)
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
        composeTestRule.setContent {
            HSheetDragContent(
                viewModel = callActionsViewModel,
                callActions = ImmutableList(listOf(CameraAction())),
                isLargeScreen = false,
                onModularComponentRequest = {},
                onAskInputPermissions = {},
                inputPermissions = InputPermissions(cameraPermission = cameraPermission)
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
        composeTestRule.setContent {
            HSheetDragContent(
                viewModel = callActionsViewModel,
                callActions = ImmutableList(listOf(CameraAction())),
                isLargeScreen = false,
                onModularComponentRequest = {},
                onAskInputPermissions = {},
                inputPermissions = InputPermissions(cameraPermission = cameraPermission)
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
        composeTestRule.setContent {
            HSheetDragContent(
                viewModel = callActionsViewModel,
                callActions = ImmutableList(listOf(ChatAction())),
                isLargeScreen = false,
                onAskInputPermissions = {},
                onModularComponentRequest = {}
            )
        }

        val text = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_chat)
        composeTestRule
            .onNodeWithText(text, useUnmergedTree = true)
            .assertIsDisplayed()
            .performClick()

        verify(exactly = 1) { callActionsViewModel.showChat(any()) }
        verify(exactly = 1) { composeTestRule.activity.unlockDevice(any()) }
        unmockkObject(ActivityExtensions)
    }

    @Test
    fun userClicksChatOnLargeScreen_onModularComponentRequestChat() {
        var component: ModularComponent? = null
        composeTestRule.setContent {
            HSheetDragContent(
                viewModel = callActionsViewModel,
                callActions = ImmutableList(listOf(ChatAction())),
                isLargeScreen = true,
                onAskInputPermissions = {},
                onModularComponentRequest = { component = it }
            )
        }

        val text = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_chat)
        composeTestRule
            .onNodeWithText(text, useUnmergedTree = true)
            .assertIsDisplayed()
            .performClick()

        assertEquals(ModularComponent.Chat, component)
    }

    @Test
    fun userClicksFlipCamera_switchCameraInvoked() {
        composeTestRule.setContent {
            HSheetDragContent(
                viewModel = callActionsViewModel,
                callActions = ImmutableList(listOf(FlipCameraAction())),
                isLargeScreen = false,
                onAskInputPermissions = {},
                onModularComponentRequest = {}
            )
        }

        val text = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_flip_camera)
        composeTestRule
            .onNodeWithText(text, useUnmergedTree = true)
            .assertIsDisplayed()
            .performClick()

        verify(exactly = 1) { callActionsViewModel.switchCamera() }
    }

    @Test
    fun userClicksAudio_onModularComponentRequestAudio() {
        var component: ModularComponent? = null
        composeTestRule.setContent {
            HSheetDragContent(
                viewModel = callActionsViewModel,
                callActions = ImmutableList(listOf(AudioAction())),
                isLargeScreen = false,
                onAskInputPermissions = {},
                onModularComponentRequest = { component = it }
            )
        }

        val text = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_audio)
        composeTestRule
            .onNodeWithText(text, useUnmergedTree = true)
            .assertIsDisplayed()
            .performClick()

        assertEquals(ModularComponent.Audio, component)
    }

    @Test
    fun userClicksFileShare_clearFileShareBadgeAndRequestModalFileShare() {
        var component: ModularComponent? = null
        composeTestRule.setContent {
            HSheetDragContent(
                viewModel = callActionsViewModel,
                callActions = ImmutableList(listOf(FileShareAction())),
                isLargeScreen = false,
                onAskInputPermissions = {},
                onModularComponentRequest = { component = it }
            )
        }

        val text = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_file_share)
        composeTestRule
            .onNodeWithText(text, useUnmergedTree = true)
            .assertIsDisplayed()
            .performClick()

        assertEquals(ModularComponent.FileShare, component)
        verify(exactly = 1) { callActionsViewModel.clearFileShareBadge() }
    }

    @Test
    fun userClicksWhiteboard_onModularComponentRequestWhiteboard() {
        var component: ModularComponent? = null
        composeTestRule.setContent {
            HSheetDragContent(
                viewModel = callActionsViewModel,
                callActions = ImmutableList(listOf(WhiteboardAction())),
                isLargeScreen = false,
                onAskInputPermissions = {},
                onModularComponentRequest = { component = it }
            )
        }

        val text = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_whiteboard)
        composeTestRule
            .onNodeWithText(text, useUnmergedTree = true)
            .assertIsDisplayed()
            .performClick()

        assertEquals(ModularComponent.Whiteboard, component)
    }

    @Test
    fun userClicksVirtualBackground_onModularComponentRequestVirtualBackground() {
        var component: ModularComponent? = null
        composeTestRule.setContent {
            HSheetDragContent(
                viewModel = callActionsViewModel,
                callActions = ImmutableList(listOf(VirtualBackgroundAction())),
                isLargeScreen = false,
                onAskInputPermissions = {},
                onModularComponentRequest = { component = it }
            )
        }

        val text = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_virtual_background)
        composeTestRule
            .onNodeWithText(text, useUnmergedTree = true)
            .assertIsDisplayed()
            .performClick()

        assertEquals(ModularComponent.VirtualBackground, component)
    }

    @Test
    fun userClicksScreenShareUserChoiceWhenEnabled_tryStopScreenShareInvoked() {
        every { callActionsViewModel.tryStopScreenShare() } returns true

        composeTestRule.setContent {
            HSheetDragContent(
                viewModel = callActionsViewModel,
                callActions = ImmutableList(listOf(ScreenShareAction.UserChoice())),
                isLargeScreen = false,
                onAskInputPermissions = {},
                onModularComponentRequest = {}
            )
        }

        val text = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_screen_share)
        composeTestRule
            .onNodeWithText(text, useUnmergedTree = true)
            .assertIsDisplayed()
            .performClick()

        verify(exactly = 1) { callActionsViewModel.tryStopScreenShare() }
    }

    @Test
    fun userClicksScreenShareAppWhenEnabled_tryStopScreenShareInvoked() {
        every { callActionsViewModel.tryStopScreenShare() } returns true

        composeTestRule.setContent {
            HSheetDragContent(
                viewModel = callActionsViewModel,
                callActions = ImmutableList(listOf(ScreenShareAction.App())),
                isLargeScreen = false,
                onAskInputPermissions = {},
                onModularComponentRequest = {}
            )
        }

        val text = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_screen_share)
        composeTestRule
            .onNodeWithText(text, useUnmergedTree = true)
            .assertIsDisplayed()
            .performClick()

        verify(exactly = 1) { callActionsViewModel.tryStopScreenShare() }
    }

    @Test
    fun userClicksScreenShareWholeDeviceWhenEnabled_tryStopScreenShareInvoked() {
        every { callActionsViewModel.tryStopScreenShare() } returns true

        composeTestRule.setContent {
            HSheetDragContent(
                viewModel = callActionsViewModel,
                callActions = ImmutableList(listOf(ScreenShareAction.WholeDevice())),
                isLargeScreen = false,
                onAskInputPermissions = {},
                onModularComponentRequest = {}
            )
        }

        val text = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_screen_share)
        composeTestRule
            .onNodeWithText(text, useUnmergedTree = true)
            .assertIsDisplayed()
            .performClick()

        verify(exactly = 1) { callActionsViewModel.tryStopScreenShare() }
    }

    @Test
    fun userClicksScreenShareUserChoiceWhenNotEnabled_onModularComponentRequestScreenShare() {
        every { callActionsViewModel.tryStopScreenShare() } returns false
        callActionsUiState.value = callActionsUiState.value.copy(
            actionList = ImmutableList(listOf(ScreenShareAction.UserChoice()))
        )
        var component: ModularComponent? = null
        composeTestRule.setContent {
            HSheetDragContent(
                viewModel = callActionsViewModel,
                screenShareViewModel = mockk(),
                callActions = ImmutableList(listOf(ScreenShareAction.UserChoice())),
                isLargeScreen = false,
                onAskInputPermissions = {},
                onModularComponentRequest = { component = it }
            )
        }

        val text = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_screen_share)
        composeTestRule
            .onNodeWithText(text, useUnmergedTree = true)
            .assertIsDisplayed()
            .performClick()

        assertEquals(ModularComponent.ScreenShare, component)
    }

    @Test
    fun userClicksScreenShareAppWhenNotEnabled_onModularScreenShareComponentNotRequested() {
        every { callActionsViewModel.tryStopScreenShare() } returns false
        callActionsUiState.value = callActionsUiState.value.copy(
            actionList = ImmutableList(listOf(ScreenShareAction.App()))
        )
        var component: ModularComponent? = null
        composeTestRule.setContent {
            HSheetDragContent(
                viewModel = callActionsViewModel,
                screenShareViewModel = mockk(relaxed = true),
                callActions = ImmutableList(listOf(ScreenShareAction.App())),
                isLargeScreen = false,
                onAskInputPermissions = {},
                onModularComponentRequest = { component = it }
            )
        }

        val text = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_screen_share)
        composeTestRule
            .onNodeWithText(text, useUnmergedTree = true)
            .assertIsDisplayed()
            .performClick()

        assertEquals(null, component)
    }

    @Test
    fun userClicksScreenShareWholeDeviceWhenNotEnabled_onModularScreenShareComponentNotRequested() {
        every { callActionsViewModel.tryStopScreenShare() } returns false
        callActionsUiState.value = callActionsUiState.value.copy(
            actionList = ImmutableList(listOf(ScreenShareAction.WholeDevice()))
        )
        var component: ModularComponent? = null
        var isAskingInputPermission = false
        composeTestRule.setContent {
            HSheetDragContent(
                viewModel = callActionsViewModel,
                screenShareViewModel = mockk(relaxed = true),
                callActions = ImmutableList(listOf(ScreenShareAction.WholeDevice())),
                isLargeScreen = false,
                onAskInputPermissions = { isAskingInputPermission = true },
                onModularComponentRequest = { component = it }
            )
        }

        val text = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_screen_share)
        composeTestRule
            .onNodeWithText(text, useUnmergedTree = true)
            .assertIsDisplayed()
            .performClick()

        assertEquals(true, isAskingInputPermission)
        assertEquals(null, component)
    }

    @Test
    fun testItemsPlacement() {
        val itemsPerRow = 2
        val width = CallActionDefaults.MinButtonSize * itemsPerRow + SheetItemsSpacing
        val flip = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_flip_camera)
        val whiteboard = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_whiteboard)
        val fileshare = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_file_share)
        val chat = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_chat)
        composeTestRule.setContent {
            HSheetDragContent(
                callActions = ImmutableList(
                    listOf(
                        FlipCameraAction(),
                        WhiteboardAction(),
                        FileShareAction(),
                        ChatAction()
                    )
                ),
                labels = false,
                itemsPerRow = itemsPerRow,
                onHangUpClick = { },
                onMicToggle = { },
                onCameraToggle = { },
                onScreenShareToggle = { },
                onFlipCameraClick = { },
                onAudioClick = { },
                onChatClick = { },
                onFileShareClick = { },
                onWhiteboardClick = { },
                onSettingsClick = {},
                onVirtualBackgroundToggle = { },
                onSignatureClick = {},
                modifier = Modifier.width(width)
            )
        }
        val rootBounds = composeTestRule.onRoot().getBoundsInRoot()
        val childBounds1 = composeTestRule.onNodeWithContentDescription(flip).getBoundsInRoot()
        val childBounds2 = composeTestRule.onNodeWithContentDescription(whiteboard).getBoundsInRoot()
        val childBounds3 = composeTestRule.onNodeWithContentDescription(fileshare).getBoundsInRoot()
        val childBounds4 = composeTestRule.onNodeWithContentDescription(chat).getBoundsInRoot()
        childBounds1.left.assertIsEqualTo(rootBounds.left, "child 1 left bound")
        childBounds2.left.assertIsEqualTo(childBounds1.right + HSheetDragHorizontalPadding, "child 2 left bound")
        childBounds2.right.assertIsEqualTo(rootBounds.right, "child 2 right bound")
        childBounds3.left.assertIsEqualTo(rootBounds.left, "child 3 left bound")
        childBounds3.top.assertIsEqualTo(childBounds1.bottom + HSheetDragVerticalPadding, "child 3 top bound")
        childBounds4.left.assertIsEqualTo(childBounds3.right + HSheetDragHorizontalPadding, "child 4 left bound")
        childBounds4.right.assertIsEqualTo(rootBounds.right, "child 4 right bound")
        childBounds4.top.assertIsEqualTo(childBounds2.bottom + HSheetDragVerticalPadding, "child 4 top bound")
    }

    @Test
    fun testLastItemOnOneRow() {
        val itemsPerRow = 3
        val flip = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_flip_camera)
        val whiteboard = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_whiteboard)
        composeTestRule.setContent {
            HSheetDragContent(
                callActions = ImmutableList(
                    listOf(
                        FlipCameraAction(),
                        WhiteboardAction()
                    )
                ),
                itemsPerRow = itemsPerRow,
                onHangUpClick = { },
                onMicToggle = { },
                onCameraToggle = { },
                onScreenShareToggle = { },
                onFlipCameraClick = { },
                onAudioClick = { },
                onChatClick = { },
                onFileShareClick = { },
                onWhiteboardClick = { },
                onSettingsClick = {},
                onVirtualBackgroundToggle = { },
                onSignatureClick = {}
            )
        }
        val rootBounds = composeTestRule.onRoot().getBoundsInRoot()
        val childBounds1 = composeTestRule.onNodeWithText(flip).getBoundsInRoot()
        val childBounds2 = composeTestRule.onNodeWithText(whiteboard).getBoundsInRoot()
        childBounds1.left.assertIsEqualTo(rootBounds.left, "child 1 left bound")
        childBounds2.left.assertIsEqualTo(childBounds1.right + HSheetDragHorizontalPadding, "child 2 left bound")
        childBounds2.right.assertIsEqualTo(rootBounds.right, "child 2 right bound")
    }

    @Test
    fun testOnMicClick() {
        var isClicked: Boolean? = null
        val description = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_description_disable_microphone)
        composeTestRule.setContent {
            HSheetDragContent(
                callActions = ImmutableList(listOf(MicAction())),
                onHangUpClick = { },
                onMicToggle = { isClicked = it },
                onCameraToggle = { },
                onScreenShareToggle = { },
                onFlipCameraClick = { },
                onAudioClick = { },
                onChatClick = { },
                onFileShareClick = { },
                onWhiteboardClick = { },
                onSettingsClick = {},
                onVirtualBackgroundToggle = { },
                onSignatureClick = {}
            )
        }
        composeTestRule.onNodeWithContentDescription(description).assertHasClickAction()
        composeTestRule.onNodeWithContentDescription(description).performClick()
        Assert.assertEquals(true, isClicked)
    }

    @Test
    fun testOnCameraClick() {
        var isClicked: Boolean? = null
        val description = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_description_disable_camera)
        composeTestRule.setContent {
            HSheetDragContent(
                callActions = ImmutableList(listOf(CameraAction())),
                onHangUpClick = { },
                onMicToggle = { },
                onCameraToggle = { isClicked = it },
                onScreenShareToggle = { },
                onFlipCameraClick = { },
                onAudioClick = { },
                onChatClick = { },
                onFileShareClick = { },
                onWhiteboardClick = { },
                onSettingsClick = {},
                onVirtualBackgroundToggle = { },
                onSignatureClick = {}
            )
        }
        composeTestRule.onNodeWithContentDescription(description).assertHasClickAction()
        composeTestRule.onNodeWithContentDescription(description).performClick()
        Assert.assertEquals(true, isClicked)
    }

    @Test
    fun testOnScreenShareClick() {
        var isClicked: Boolean? = null
        val description = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_screen_share)
        composeTestRule.setContent {
            HSheetDragContent(
                callActions = ImmutableList(listOf(ScreenShareAction.UserChoice())),
                onHangUpClick = { },
                onMicToggle = { },
                onCameraToggle = { },
                onScreenShareToggle = { isClicked = true },
                onFlipCameraClick = { },
                onAudioClick = { },
                onChatClick = { },
                onFileShareClick = { },
                onWhiteboardClick = { },
                onSettingsClick = {},
                onVirtualBackgroundToggle = { },
                onSignatureClick = {}
            )
        }
        composeTestRule.onNodeWithText(description).assertHasClickAction()
        composeTestRule.onNodeWithText(description).performClick()
        Assert.assertEquals(true, isClicked)
    }

    @Test
    fun testOnFlipCameraClick() {
        var isClicked = false
        val description = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_flip_camera)
        composeTestRule.setContent {
            HSheetDragContent(
                callActions = ImmutableList(listOf(FlipCameraAction())),
                onHangUpClick = { },
                onMicToggle = { },
                onCameraToggle = { },
                onScreenShareToggle = { },
                onFlipCameraClick = { isClicked = true },
                onAudioClick = { },
                onChatClick = { },
                onFileShareClick = { },
                onWhiteboardClick = { },
                onSettingsClick = {},
                onVirtualBackgroundToggle = { },
                onSignatureClick = {}
            )
        }
        composeTestRule.onNodeWithText(description).assertHasClickAction()
        composeTestRule.onNodeWithText(description).performClick()
        Assert.assertEquals(true, isClicked)
    }

    @Test
    fun testOnAudioClick() {
        var isClicked = false
        val description = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_audio)
        composeTestRule.setContent {
            HSheetDragContent(
                callActions = ImmutableList(listOf(AudioAction())),
                onHangUpClick = { },
                onMicToggle = { },
                onCameraToggle = { },
                onScreenShareToggle = { },
                onFlipCameraClick = { },
                onAudioClick = { isClicked = true },
                onChatClick = { },
                onFileShareClick = { },
                onWhiteboardClick = { },
                onSettingsClick = {},
                onVirtualBackgroundToggle = { },
                onSignatureClick = {}
            )
        }
        composeTestRule.onNodeWithText(description).assertHasClickAction()
        composeTestRule.onNodeWithText(description).performClick()
        Assert.assertEquals(true, isClicked)
    }

    @Test
    fun testOnChatClick() {
        var isClicked = false
        val description = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_chat)
        composeTestRule.setContent {
            HSheetDragContent(
                callActions = ImmutableList(listOf(ChatAction())),
                onHangUpClick = { },
                onMicToggle = { },
                onCameraToggle = { },
                onScreenShareToggle = { },
                onFlipCameraClick = { },
                onAudioClick = { },
                onChatClick = { isClicked = true },
                onFileShareClick = { },
                onWhiteboardClick = { },
                onSettingsClick = {},
                onVirtualBackgroundToggle = { },
                onSignatureClick = {}
            )
        }
        composeTestRule.onNodeWithText(description).assertHasClickAction()
        composeTestRule.onNodeWithText(description).performClick()
        Assert.assertEquals(true, isClicked)
    }

    @Test
    fun testOnFileShareClick() {
        var isClicked = false
        val description = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_file_share)
        composeTestRule.setContent {
            HSheetDragContent(
                callActions = ImmutableList(listOf(FileShareAction())),
                onHangUpClick = { },
                onMicToggle = { },
                onCameraToggle = { },
                onScreenShareToggle = { },
                onFlipCameraClick = { },
                onAudioClick = { },
                onChatClick = { },
                onFileShareClick = { isClicked = true },
                onWhiteboardClick = { },
                onSettingsClick = {},
                onVirtualBackgroundToggle = { },
                onSignatureClick = {}
            )
        }
        composeTestRule.onNodeWithText(description).assertHasClickAction()
        composeTestRule.onNodeWithText(description).performClick()
        Assert.assertEquals(true, isClicked)
    }

    @Test
    fun testOnWhiteboardClick() {
        var isClicked = false
        val description = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_whiteboard)
        composeTestRule.setContent {
            HSheetDragContent(
                callActions = ImmutableList(listOf(WhiteboardAction())),
                onHangUpClick = { },
                onMicToggle = { },
                onCameraToggle = { },
                onScreenShareToggle = { },
                onFlipCameraClick = { },
                onAudioClick = { },
                onChatClick = { },
                onFileShareClick = { },
                onWhiteboardClick = { isClicked = true },
                onSettingsClick = {},
                onVirtualBackgroundToggle = { },
                onSignatureClick = {}
            )
        }
        composeTestRule.onNodeWithText(description).assertHasClickAction()
        composeTestRule.onNodeWithText(description).performClick()
        Assert.assertEquals(true, isClicked)
    }

    @Test
    fun testOnVirtualBackgroundToggle() {
        var isClicked = false
        val description = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_virtual_background)
        composeTestRule.setContent {
            HSheetDragContent(
                callActions = ImmutableList(listOf(VirtualBackgroundAction())),
                onHangUpClick = { },
                onMicToggle = { },
                onCameraToggle = { },
                onScreenShareToggle = { },
                onFlipCameraClick = { },
                onAudioClick = { },
                onChatClick = { },
                onFileShareClick = { },
                onWhiteboardClick = { },
                onSignatureClick = {},
                onSettingsClick = {},
                onVirtualBackgroundToggle = { isClicked = true }
            )
        }
        composeTestRule.onNodeWithText(description).assertHasClickAction()
        composeTestRule.onNodeWithText(description).performClick()
        Assert.assertEquals(true, isClicked)
    }
}