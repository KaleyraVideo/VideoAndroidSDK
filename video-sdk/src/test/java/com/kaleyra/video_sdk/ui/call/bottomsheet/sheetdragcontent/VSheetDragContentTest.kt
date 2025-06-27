package com.kaleyra.video_sdk.ui.call.bottomsheet.sheetdragcontent

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.height
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
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.PermissionStatus
import com.kaleyra.video_common_ui.utils.extensions.ActivityExtensions
import com.kaleyra.video_common_ui.utils.extensions.ActivityExtensions.unlockDevice
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.call.bottomsheet.view.sheetcontent.sheetitemslayout.SheetItemsSpacing
import com.kaleyra.video_sdk.call.bottomsheet.view.sheetdragcontent.VSheetDragContent
import com.kaleyra.video_sdk.call.bottomsheet.view.sheetdragcontent.VSheetDragHorizontalPadding
import com.kaleyra.video_sdk.call.bottomsheet.view.sheetdragcontent.VSheetDragVerticalPadding
import com.kaleyra.video_sdk.call.callactions.view.CallActionDefaults
import com.kaleyra.video_sdk.call.callactions.model.CallActionsUiState
import com.kaleyra.video_sdk.call.callactions.viewmodel.CallActionsViewModel
import com.kaleyra.video_sdk.call.bottomsheet.model.AudioAction
import com.kaleyra.video_sdk.call.bottomsheet.model.CameraAction
import com.kaleyra.video_sdk.call.bottomsheet.model.ChatAction
import com.kaleyra.video_sdk.call.bottomsheet.model.FileShareAction
import com.kaleyra.video_sdk.call.bottomsheet.model.FlipCameraAction
import com.kaleyra.video_sdk.call.bottomsheet.model.HangUpAction
import com.kaleyra.video_sdk.call.bottomsheet.model.MicAction
import com.kaleyra.video_sdk.call.screen.model.ModularComponent
import com.kaleyra.video_sdk.call.bottomsheet.model.ScreenShareAction
import com.kaleyra.video_sdk.call.bottomsheet.model.VirtualBackgroundAction
import com.kaleyra.video_sdk.call.bottomsheet.model.WhiteboardAction
import com.kaleyra.video_sdk.call.bottomsheet.view.sheetdragcontent.HSheetDragContent
import com.kaleyra.video_sdk.call.callactions.view.ScreenShareAction
import com.kaleyra.video_sdk.call.screen.model.InputPermissions
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
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalPermissionsApi::class)
@RunWith(RobolectricTestRunner::class)
class VSheetDragContentTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val callActionsUiState = MutableStateFlow(CallActionsUiState())

    private val callActionsViewModel = mockk<CallActionsViewModel>(relaxed = true) {
        every { uiState } returns callActionsUiState
    }

    @Test
    fun userClicksHangUp_hangUpInvoked() {
        composeTestRule.setContent {
            VSheetDragContent(
                viewModel = callActionsViewModel,
                callActions = ImmutableList(listOf(HangUpAction())),
                onModularComponentRequest = {},
                onAskInputPermissions = {}
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
            VSheetDragContent(
                viewModel = callActionsViewModel,
                callActions = ImmutableList(listOf(MicAction())),
                onModularComponentRequest = {},
                inputPermissions = InputPermissions(micPermission = micPermission),
                onAskInputPermissions = {}
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
            VSheetDragContent(
                viewModel = callActionsViewModel,
                callActions = ImmutableList(listOf(MicAction())),
                onModularComponentRequest = {},
                inputPermissions = InputPermissions(micPermission = micPermission),
                onAskInputPermissions = {}
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
            VSheetDragContent(
                viewModel = callActionsViewModel,
                callActions = ImmutableList(listOf(CameraAction())),
                onModularComponentRequest = {},
                inputPermissions = InputPermissions(cameraPermission = cameraPermission),
                onAskInputPermissions = {}
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
            VSheetDragContent(
                viewModel = callActionsViewModel,
                callActions = ImmutableList(listOf(CameraAction())),
                onModularComponentRequest = {},
                inputPermissions = InputPermissions(cameraPermission = cameraPermission),
                onAskInputPermissions = {}
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
            VSheetDragContent(
                viewModel = callActionsViewModel,
                callActions = ImmutableList(listOf(ChatAction())),
                onModularComponentRequest = {},
                onAskInputPermissions = {}
            )
        }

        val text = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_chat)
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
        composeTestRule.setContent {
            VSheetDragContent(
                viewModel = callActionsViewModel,
                callActions = ImmutableList(listOf(FlipCameraAction())),
                onModularComponentRequest = {},
                onAskInputPermissions = {}
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
        var component: ModularComponent? = null
        composeTestRule.setContent {
            VSheetDragContent(
                viewModel = callActionsViewModel,
                callActions = ImmutableList(listOf(AudioAction())),
                onModularComponentRequest = { component = it },
                onAskInputPermissions = {}
            )
        }

        val text = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_audio)
        composeTestRule
            .onNodeWithContentDescription(text, useUnmergedTree = true)
            .assertIsDisplayed()
            .performClick()

        assertEquals(ModularComponent.Audio, component)
    }

    @Test
    fun userClicksFileShare_clearFileShareBadgeAndRequestModalFileShare() {
        var component: ModularComponent? = null
        composeTestRule.setContent {
            VSheetDragContent(
                viewModel = callActionsViewModel,
                callActions = ImmutableList(listOf(FileShareAction())),
                onModularComponentRequest = { component = it },
                onAskInputPermissions = {}
            )
        }

        val text = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_file_share)
        composeTestRule
            .onNodeWithContentDescription(text, useUnmergedTree = true)
            .assertIsDisplayed()
            .performClick()

        assertEquals(ModularComponent.FileShare, component)
        verify(exactly = 1) { callActionsViewModel.clearFileShareBadge() }
    }

    @Test
    fun userClicksWhiteboard_onModularComponentRequestWhiteboard() {
        var component: ModularComponent? = null
        composeTestRule.setContent {
            VSheetDragContent(
                viewModel = callActionsViewModel,
                callActions = ImmutableList(listOf(WhiteboardAction())),
                onModularComponentRequest = { component = it },
                onAskInputPermissions = {}
            )
        }

        val text = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_whiteboard)
        composeTestRule
            .onNodeWithContentDescription(text, useUnmergedTree = true)
            .assertIsDisplayed()
            .performClick()

        assertEquals(ModularComponent.Whiteboard, component)
    }

    @Test
    fun userClicksVirtualBackground_onModularComponentRequestVirtualBackground() {
        var component: ModularComponent? = null
        composeTestRule.setContent {
            VSheetDragContent(
                viewModel = callActionsViewModel,
                callActions = ImmutableList(listOf(VirtualBackgroundAction())),
                onModularComponentRequest = { component = it },
                onAskInputPermissions = {}
            )
        }

        val text = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_virtual_background)
        composeTestRule
            .onNodeWithContentDescription(text, useUnmergedTree = true)
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
        val itemsPerColumn = 2
        val height = CallActionDefaults.MinButtonSize * itemsPerColumn + SheetItemsSpacing
        val flip = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_flip_camera)
        val camera = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_description_disable_camera)
        val mic = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_description_disable_microphone)
        val chat = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_chat)
        composeTestRule.setContent {
            VSheetDragContent(
                callActions = ImmutableList(
                    listOf(
                        FlipCameraAction(),
                        CameraAction(),
                        MicAction(),
                        ChatAction()
                    )
                ),
                itemsPerColumn = itemsPerColumn,
                onHangUpClick = { },
                onMicToggle = { },
                onCameraToggle = { },
                onScreenShareToggle = { },
                onFlipCameraClick = { },
                onAudioClick = { },
                onChatClick = { },
                onFileShareClick = { },
                onWhiteboardClick = { },
                onSignatureClick = { },
                onVirtualBackgroundToggle = { },
                onSettingsClick = { },
                modifier = Modifier.height(height)
            )
        }
        val rootBounds = composeTestRule.onRoot().getBoundsInRoot()
        val childBounds1 = composeTestRule.onNodeWithContentDescription(flip).getBoundsInRoot()
        val childBounds2 = composeTestRule.onNodeWithContentDescription(camera).getBoundsInRoot()
        val childBounds3 = composeTestRule.onNodeWithContentDescription(mic).getBoundsInRoot()
        val childBounds4 = composeTestRule.onNodeWithContentDescription(chat).getBoundsInRoot()
        childBounds1.top.assertIsEqualTo(childBounds2.bottom + VSheetDragVerticalPadding, "child 1 top bound")
        childBounds1.bottom.assertIsEqualTo(rootBounds.bottom, "child 1 bottom bound")
        childBounds2.top.assertIsEqualTo(rootBounds.top, "child 2 top bound")
        childBounds3.top.assertIsEqualTo(childBounds4.bottom + VSheetDragVerticalPadding, "child 3 top bound")
        childBounds3.bottom.assertIsEqualTo(rootBounds.bottom, "child 3 bottom bound")
        childBounds4.top.assertIsEqualTo(rootBounds.top, "child 4 top bound")
        childBounds3.left.assertIsEqualTo(childBounds1.right + VSheetDragHorizontalPadding, "child 3 left bound")
        childBounds4.left.assertIsEqualTo(childBounds2.right + VSheetDragHorizontalPadding, "child 4 left bound")
    }

    @Test
    fun testOnMicClick() {
        var isClicked: Boolean? = null
        val description = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_description_disable_microphone)
        composeTestRule.setContent {
            VSheetDragContent(
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
                onSignatureClick = { },
                onVirtualBackgroundToggle = { },
                onSettingsClick = { }
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
            VSheetDragContent(
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
                onSignatureClick = { },
                onVirtualBackgroundToggle = { },
                onSettingsClick = { },
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
            VSheetDragContent(
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
                onSignatureClick = { },
                onVirtualBackgroundToggle = { },
                onSettingsClick = { }
            )
        }
        composeTestRule.onNodeWithContentDescription(description).assertHasClickAction()
        composeTestRule.onNodeWithContentDescription(description).performClick()
        Assert.assertEquals(true, isClicked)
    }

    @Test
    fun testOnFlipCameraClick() {
        var isClicked = false
        val description = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_flip_camera)
        composeTestRule.setContent {
            VSheetDragContent(
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
                onSignatureClick = { },
                onVirtualBackgroundToggle = { },
                onSettingsClick = { }
            )
        }
        composeTestRule.onNodeWithContentDescription(description).assertHasClickAction()
        composeTestRule.onNodeWithContentDescription(description).performClick()
        Assert.assertEquals(true, isClicked)
    }

    @Test
    fun testOnAudioClick() {
        var isClicked = false
        val description = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_audio)
        composeTestRule.setContent {
            VSheetDragContent(
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
                onSignatureClick = { },
                onVirtualBackgroundToggle = { },
                onSettingsClick = { }
            )
        }
        composeTestRule.onNodeWithContentDescription(description).assertHasClickAction()
        composeTestRule.onNodeWithContentDescription(description).performClick()
        Assert.assertEquals(true, isClicked)
    }

    @Test
    fun testOnChatClick() {
        var isClicked = false
        val description = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_chat)
        composeTestRule.setContent {
            VSheetDragContent(
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
                onSignatureClick = { },
                onVirtualBackgroundToggle = { },
                onSettingsClick = { }
            )
        }
        composeTestRule.onNodeWithContentDescription(description).assertHasClickAction()
        composeTestRule.onNodeWithContentDescription(description).performClick()
        Assert.assertEquals(true, isClicked)
    }

    @Test
    fun testOnFileShareClick() {
        var isClicked = false
        val description = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_file_share)
        composeTestRule.setContent {
            VSheetDragContent(
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
                onSignatureClick = { },
                onVirtualBackgroundToggle = { },
                onSettingsClick = { }
            )
        }
        composeTestRule.onNodeWithContentDescription(description).assertHasClickAction()
        composeTestRule.onNodeWithContentDescription(description).performClick()
        Assert.assertEquals(true, isClicked)
    }

    @Test
    fun testOnWhiteboardClick() {
        var isClicked = false
        val description = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_whiteboard)
        composeTestRule.setContent {
            VSheetDragContent(
                callActions = ImmutableList(listOf(WhiteboardAction())),
                onHangUpClick = { },
                onMicToggle = { },
                onCameraToggle = { },
                onScreenShareToggle = { },
                onFlipCameraClick = { },
                onAudioClick = { },
                onChatClick = { },
                onFileShareClick = { },
                onSignatureClick = { },
                onWhiteboardClick = { isClicked = true },
                onVirtualBackgroundToggle = { },
                onSettingsClick = { }
            )
        }
        composeTestRule.onNodeWithContentDescription(description).assertHasClickAction()
        composeTestRule.onNodeWithContentDescription(description).performClick()
        Assert.assertEquals(true, isClicked)
    }

    @Test
    fun testonVirtualBackgroundToggle() {
        var isClicked = false
        val description = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_virtual_background)
        composeTestRule.setContent {
            VSheetDragContent(
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
                onSignatureClick = { },
                onVirtualBackgroundToggle = { isClicked = true },
                onSettingsClick = { }
            )
        }
        composeTestRule.onNodeWithContentDescription(description).assertHasClickAction()
        composeTestRule.onNodeWithContentDescription(description).performClick()
        Assert.assertEquals(true, isClicked)
    }
}