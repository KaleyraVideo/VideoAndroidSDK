package com.kaleyra.video_sdk.ui.call.screen

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.PermissionStatus
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.call.bottomsheet.CallSheetState
import com.kaleyra.video_sdk.call.bottomsheet.CallSheetValue
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
import com.kaleyra.video_sdk.call.callactions.model.CallActionsUiState
import com.kaleyra.video_sdk.call.screen.CompactScreenMaxActions
import com.kaleyra.video_sdk.call.screen.model.CallStateUi
import com.kaleyra.video_sdk.call.screen.model.InputPermissions
import com.kaleyra.video_sdk.call.screen.model.ModularComponent
import com.kaleyra.video_sdk.call.screen.view.CallScreenModalSheetTag
import com.kaleyra.video_sdk.call.screen.view.vcallscreen.InputMessageDragHandleTag
import com.kaleyra.video_sdk.call.screen.view.vcallscreen.StreamMenuContentTestTag
import com.kaleyra.video_sdk.call.stream.layoutsystem.model.MoreStreamsUserPreview
import com.kaleyra.video_sdk.call.stream.layoutsystem.model.StreamItem
import com.kaleyra.video_sdk.call.stream.model.StreamUiState
import com.kaleyra.video_sdk.call.stream.model.core.StreamUi
import com.kaleyra.video_sdk.chat.screen.model.ChatUiState
import com.kaleyra.video_sdk.common.avatar.model.ImmutableUri
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList
import com.kaleyra.video_sdk.common.immutablecollections.toImmutableList
import com.kaleyra.video_sdk.common.user.UserInfo
import com.kaleyra.video_sdk.common.usermessages.model.PinScreenshareMessage
import com.kaleyra.video_sdk.common.usermessages.model.UserMessage
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test

// Test for the VCallScreen using a smartphone (both in portrait and landscape)
@OptIn(ExperimentalPermissionsApi::class)
internal abstract class VCallScreenTest: VCallScreenBaseTest() {

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
        val micPermission = mockk<PermissionState>(relaxed = true) {
            every { status } returns PermissionStatus.Granted
        }
        composeTestRule.setUpVCallScreen(
            inputPermissions = InputPermissions(micPermission = micPermission)
        )
        callActionsUiState.value = CallActionsUiState(
            actionList = listOf(MicAction()).toImmutableList()
        )

        val text =
            composeTestRule.activity.getString(R.string.kaleyra_call_sheet_description_disable_microphone)
        composeTestRule
            .onNodeWithContentDescription(text, useUnmergedTree = true)
            .assertIsDisplayed()
            .performClick()

        verify(exactly = 1) { callViewModel.toggleMic(any()) }
    }

    @Test
    fun testSheetActions_micToggleOff() {
        val micPermission = mockk<PermissionState>(relaxed = true) {
            every { status } returns PermissionStatus.Granted
        }
        composeTestRule.setUpVCallScreen(
            inputPermissions = InputPermissions(micPermission = micPermission)
        )
        callActionsUiState.value = CallActionsUiState(
            actionList = listOf(MicAction(isToggled = true)).toImmutableList()
        )

        val text =
            composeTestRule.activity.getString(R.string.kaleyra_call_sheet_description_enable_microphone)
        composeTestRule
            .onNodeWithContentDescription(text, useUnmergedTree = true)
            .assertIsDisplayed()
            .performClick()

        verify(exactly = 1) { callViewModel.toggleMic(any()) }
    }

    @Test
    fun testSheetActions_cameraToggleOn() {
        val cameraPermission = mockk<PermissionState>(relaxed = true) {
            every { status } returns PermissionStatus.Granted
        }
        composeTestRule.setUpVCallScreen(
            inputPermissions = InputPermissions(cameraPermission = cameraPermission)
        )
        callActionsUiState.value = CallActionsUiState(
            actionList = listOf(CameraAction()).toImmutableList()
        )

        val text =
            composeTestRule.activity.getString(R.string.kaleyra_call_sheet_description_disable_camera)
        composeTestRule
            .onNodeWithContentDescription(text, useUnmergedTree = true)
            .assertIsDisplayed()
            .performClick()

        verify(exactly = 1) { callViewModel.toggleCamera(any()) }
    }

    @Test
    fun testSheetActions_cameraToggleOff() {
        val cameraPermission = mockk<PermissionState>(relaxed = true) {
            every { status } returns PermissionStatus.Granted
        }
        composeTestRule.setUpVCallScreen(
            inputPermissions = InputPermissions(cameraPermission = cameraPermission)
        )
        callActionsUiState.value = CallActionsUiState(
            actionList = listOf(CameraAction(isToggled = true)).toImmutableList()
        )

        val text =
            composeTestRule.activity.getString(R.string.kaleyra_call_sheet_description_enable_camera)
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
    fun testSheetActionsOnSmallScreen_chat() {
        composeTestRule.setUpVCallScreen()
        callActionsUiState.value = CallActionsUiState(
            actionList = listOf(ChatAction()).toImmutableList()
        )

        val text = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_chat)
        composeTestRule
            .onNodeWithContentDescription(text, useUnmergedTree = true)
            .assertIsDisplayed()
            .performClick()

        composeTestRule.onNodeWithTag(CallScreenModalSheetTag).assertDoesNotExist()
        verify(exactly = 1) { callViewModel.showChat(any()) }
    }

    @Test
    fun testSheetActions_screenShareToggleOff() {
        composeTestRule.setUpVCallScreen()
        callActionsUiState.value = CallActionsUiState(
            actionList = listOf(ScreenShareAction.UserChoice(isToggled = true)).toImmutableList()
        )

        val text =
            composeTestRule.activity.getString(R.string.kaleyra_call_sheet_description_stop_screen_share)
        composeTestRule
            .onNodeWithContentDescription(text, useUnmergedTree = true)
            .assertIsDisplayed()
            .performClick()

        verify(exactly = 1) { callViewModel.tryStopScreenShare() }
    }

    @Test
    fun testSheetActions_screenShareToggleOn() {
        var component: ModularComponent? = null
        composeTestRule.setUpVCallScreen(
            onModalSheetComponentRequest = { component = it }
        )
        callActionsUiState.value = CallActionsUiState(
            actionList = listOf(ScreenShareAction.UserChoice(isToggled = false)).toImmutableList()
        )

        val buttonText =
            composeTestRule.activity.getString(R.string.kaleyra_call_sheet_screen_share)
        composeTestRule
            .onNodeWithContentDescription(buttonText, useUnmergedTree = true)
            .assertIsDisplayed()
            .performClick()

        assertEquals(ModularComponent.ScreenShare, component)
    }

    @Test
    fun testSheetActions_onModularComponentChangeToAudio() {
        var component: ModularComponent? = null
        composeTestRule.setUpVCallScreen(
            modalSheetComponent = null,
            onModalSheetComponentRequest = { component = it }
        )
        callActionsUiState.value = CallActionsUiState(
            actionList = listOf(AudioAction()).toImmutableList()
        )

        val buttonText = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_audio)
        composeTestRule
            .onNodeWithContentDescription(buttonText, useUnmergedTree = true)
            .assertIsDisplayed()
            .performClick()

        assertEquals(ModularComponent.Audio, component)
    }

    @Test
    fun testModalSheetAudio_audioComponentIsDisplayed() {
        var componentDisplayed: ModularComponent? = null
        composeTestRule.setUpVCallScreen(
            modalSheetComponent = ModularComponent.Audio,
            onModularComponentDisplayed = { componentDisplayed = it }
        )

        val componentTitle = composeTestRule.activity.getString(R.string.kaleyra_audio_route_title)
        composeTestRule.onNodeWithText(componentTitle).assertIsDisplayed()
        composeTestRule.onNodeWithTag(CallScreenModalSheetTag).assertIsDisplayed()
        assertEquals(ModularComponent.Audio, componentDisplayed)
    }

    @Test
    fun testSheetActionsOnSmallScreen_onModalSheetComponentChangeToFileShare() {
        var component: ModularComponent? = null
        composeTestRule.setUpVCallScreen(
            modalSheetComponent = null,
            onModalSheetComponentRequest = { component = it }
        )
        callActionsUiState.value = CallActionsUiState(
            actionList = listOf(FileShareAction()).toImmutableList()
        )

        val buttonText = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_file_share)
        composeTestRule
            .onNodeWithContentDescription(buttonText, useUnmergedTree = true)
            .assertIsDisplayed()
            .performClick()
        assertEquals(ModularComponent.FileShare, component)
    }

    @Test
    fun testModalSheetFileShare_fileShareComponentIsDisplayed() {
        var componentDisplayed: ModularComponent? = null
        composeTestRule.setUpVCallScreen(
            modalSheetComponent = ModularComponent.FileShare,
            onModularComponentDisplayed = { componentDisplayed = it }
        )

        val componentTitle = composeTestRule.activity.getString(R.string.kaleyra_fileshare)
        composeTestRule.onNodeWithText(componentTitle).assertIsDisplayed()
        composeTestRule.onNodeWithTag(CallScreenModalSheetTag).assertIsDisplayed()
        assertEquals(ModularComponent.FileShare, componentDisplayed)
    }

    @Test
    fun testSidePanelFileShare_fileShareComponentIsDisplayed() {
        var componentDisplayed: ModularComponent? = null
        composeTestRule.setUpVCallScreen(
            sidePanelComponent = ModularComponent.FileShare,
            onModularComponentDisplayed = { componentDisplayed = it }
        )

        val componentTitle = composeTestRule.activity.getString(R.string.kaleyra_fileshare)
        composeTestRule.onNodeWithText(componentTitle, useUnmergedTree = true).assertIsDisplayed()
        composeTestRule.onNodeWithTag(CallScreenModalSheetTag).assertDoesNotExist()
        assertEquals(ModularComponent.FileShare, componentDisplayed)
    }

    @Test
    fun testSheetActionsOnSmallScreen_onModalSheetComponentChangeToWhiteboard() {
        var component: ModularComponent? = null
        composeTestRule.setUpVCallScreen(
            modalSheetComponent = null,
            onModalSheetComponentRequest = { component = it }
        )
        callActionsUiState.value = CallActionsUiState(
            actionList = listOf(WhiteboardAction()).toImmutableList()
        )

        val buttonText = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_whiteboard)
        composeTestRule
            .onNodeWithContentDescription(buttonText, useUnmergedTree = true)
            .assertIsDisplayed()
            .performClick()

        assertEquals(ModularComponent.Whiteboard, component)
    }

    @Test
    fun testModalSheetWhiteboard_whiteboardComponentIsDisplayed() {
        var componentDisplayed: ModularComponent? = null
        composeTestRule.setUpVCallScreen(
            modalSheetComponent = ModularComponent.Whiteboard,
            onModularComponentDisplayed = { componentDisplayed = it }
        )

        val componentTitle = composeTestRule.activity.getString(R.string.kaleyra_whiteboard)
        composeTestRule.onNodeWithText(componentTitle).assertIsDisplayed()
        composeTestRule.onNodeWithTag(CallScreenModalSheetTag).assertIsDisplayed()
        assertEquals(ModularComponent.Whiteboard, componentDisplayed)
    }

    @Test
    fun testSidePanelWhiteboard_whiteboardComponentIsDisplayed() {
        var componentDisplayed: ModularComponent? = null
        composeTestRule.setUpVCallScreen(
            sidePanelComponent = ModularComponent.Whiteboard,
            onModularComponentDisplayed = { componentDisplayed = it }
        )

        val componentTitle = composeTestRule.activity.getString(R.string.kaleyra_whiteboard)
        composeTestRule.onNodeWithText(componentTitle, useUnmergedTree = true).assertIsDisplayed()
        composeTestRule.onNodeWithTag(CallScreenModalSheetTag).assertDoesNotExist()
        assertEquals(ModularComponent.Whiteboard, componentDisplayed)
    }

    @Test
    fun testSheetActions_onModalSheetComponentChangeToVirtualBackground() {
        var component: ModularComponent? = null
        composeTestRule.setUpVCallScreen(
            modalSheetComponent = null,
            onModalSheetComponentRequest = { component = it }
        )
        callActionsUiState.value = CallActionsUiState(
            actionList = listOf(VirtualBackgroundAction()).toImmutableList()
        )

        val buttonText =
            composeTestRule.activity.getString(R.string.kaleyra_call_sheet_virtual_background)
        composeTestRule
            .onNodeWithContentDescription(buttonText, useUnmergedTree = true)
            .assertIsDisplayed()
            .performClick()

        assertEquals(ModularComponent.VirtualBackground, component)
    }

    @Test
    fun testModalSheetVirtualBackground_virtualBackgroundComponentIsDisplayed() {
        var componentDisplayed: ModularComponent? = null
        composeTestRule.setUpVCallScreen(
            modalSheetComponent = ModularComponent.VirtualBackground,
            onModularComponentDisplayed = { componentDisplayed = it }
        )

        val componentTitle =
            composeTestRule.activity.getString(R.string.kaleyra_virtual_background_picker_title)
        composeTestRule.onNodeWithText(componentTitle).assertIsDisplayed()
        assertEquals(ModularComponent.VirtualBackground, componentDisplayed)
    }

    @Test
    fun testModalSheetParticipants_participantsComponentIsDisplayed() {
        var componentDisplayed: ModularComponent? = null
        composeTestRule.setUpVCallScreen(
            modalSheetComponent = ModularComponent.Participants,
            onModularComponentDisplayed = { componentDisplayed = it }
        )

        val componentTitle =
            composeTestRule.activity.getString(R.string.kaleyra_participants_component_change_layout)
        composeTestRule.onNodeWithText(componentTitle).assertIsDisplayed()
        composeTestRule.onNodeWithTag(CallScreenModalSheetTag).assertIsDisplayed()
        assertEquals(ModularComponent.Participants, componentDisplayed)
    }

    @Test
    fun testSidePanelParticipants_participantsComponentIsDisplayed() {
        var componentDisplayed: ModularComponent? = null
        composeTestRule.setUpVCallScreen(
            sidePanelComponent = ModularComponent.Participants,
            onModularComponentDisplayed = { componentDisplayed = it }
        )

        val componentTitle =
            composeTestRule.activity.getString(R.string.kaleyra_participants_component_change_layout)
        composeTestRule.onNodeWithText(componentTitle, useUnmergedTree = true).assertIsDisplayed()
        composeTestRule.onNodeWithTag(CallScreenModalSheetTag).assertDoesNotExist()
        assertEquals(ModularComponent.Participants, componentDisplayed)
    }

    @Test
    fun testSheetDragActions_hangUp() {
        val actions = allActions.filterNot { it is HangUpAction }.take(CompactScreenMaxActions)
        composeTestRule.setUpVCallScreen(
            sheetState = CallSheetState(CallSheetValue.Expanded),
        )
        callActionsUiState.value = CallActionsUiState(
            actionList = (actions + HangUpAction()).toImmutableList()
        )

        val hangUpText = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_hang_up)
        // Check the button contained in the draggable part of the bottom sheet is displayed
        // The first of the list is the button contained in the fixed part of the bottom sheet, but not rendered by the internal adaptive layout.
        composeTestRule
            .onAllNodesWithContentDescription(hangUpText, useUnmergedTree = true)[0]
            .assertIsDisplayed()
            .performClick()

        verify(exactly = 1) { callViewModel.hangUp() }
    }

    @Test
    fun testSheetDragActions_micToggleOn() {
        val actions = allActions.filterNot { it is MicAction }.take(CompactScreenMaxActions)
        val micPermission = mockk<PermissionState>(relaxed = true) {
            every { status } returns PermissionStatus.Granted
        }
        composeTestRule.setUpVCallScreen(
            sheetState = CallSheetState(CallSheetValue.Expanded),
            inputPermissions = InputPermissions(micPermission = micPermission)
        )
        callActionsUiState.value = CallActionsUiState(
            actionList = (actions + MicAction()).toImmutableList()
        )

        val micText =
            composeTestRule.activity.getString(R.string.kaleyra_call_sheet_description_disable_microphone)
        // Check the button contained in the draggable part of the bottom sheet is displayed
        // The first of the list is the button contained in the fixed part of the bottom sheet, but not rendered by the internal adaptive layout.
        composeTestRule
            .onAllNodesWithContentDescription(micText, useUnmergedTree = true)[0]
            .assertIsDisplayed()
            .performClick()

        verify(exactly = 1) { callViewModel.toggleMic(any()) }
    }

    @Test
    fun testSheetDragActions_micToggleOff() {
        val micPermission = mockk<PermissionState>(relaxed = true) {
            every { status } returns PermissionStatus.Granted
        }
        val actions = allActions.filterNot { it is MicAction }.take(CompactScreenMaxActions)
        composeTestRule.setUpVCallScreen(
            sheetState = CallSheetState(CallSheetValue.Expanded),
            inputPermissions = InputPermissions(micPermission = micPermission)
        )
        callActionsUiState.value = CallActionsUiState(
            actionList = (actions + MicAction(isToggled = true)).toImmutableList()
        )

        val micText =
            composeTestRule.activity.getString(R.string.kaleyra_call_sheet_description_enable_microphone)
        // Check the button contained in the draggable part of the bottom sheet is displayed
        // The first of the list is the button contained in the fixed part of the bottom sheet, but not rendered by the internal adaptive layout.
        composeTestRule
            .onAllNodesWithContentDescription(micText, useUnmergedTree = true)[0]
            .assertIsDisplayed()
            .performClick()

        verify(exactly = 1) { callViewModel.toggleMic(any()) }
    }

    @Test
    fun testSheetDragActions_cameraToggleOn() {
        val cameraPermission = mockk<PermissionState>(relaxed = true) {
            every { status } returns PermissionStatus.Granted
        }
        val actions = allActions.filterNot { it is CameraAction }.take(CompactScreenMaxActions)
        composeTestRule.setUpVCallScreen(
            sheetState = CallSheetState(CallSheetValue.Expanded),
            inputPermissions = InputPermissions(cameraPermission = cameraPermission)
        )
        callActionsUiState.value = CallActionsUiState(
            actionList = (actions + CameraAction()).toImmutableList()
        )

        val cameraText =
            composeTestRule.activity.getString(R.string.kaleyra_call_sheet_description_disable_camera)
        // Check the button contained in the draggable part of the bottom sheet is displayed
        // The first of the list is the button contained in the fixed part of the bottom sheet, but not rendered by the internal adaptive layout.
        composeTestRule
            .onAllNodesWithContentDescription(cameraText, useUnmergedTree = true)[0]
            .assertIsDisplayed()
            .performClick()

        verify(exactly = 1) { callViewModel.toggleCamera(any()) }
    }

    @Test
    fun testSheetDragActions_cameraToggleOff() {
        val cameraPermission = mockk<PermissionState>(relaxed = true) {
            every { status } returns PermissionStatus.Granted
        }
        val actions = allActions.filterNot { it is CameraAction }.take(CompactScreenMaxActions)
        composeTestRule.setUpVCallScreen(
            sheetState = CallSheetState(CallSheetValue.Expanded),
            inputPermissions = InputPermissions(cameraPermission = cameraPermission)
        )
        callActionsUiState.value = CallActionsUiState(
            actionList = (actions + CameraAction(isToggled = true)).toImmutableList()
        )

        val cameraText =
            composeTestRule.activity.getString(R.string.kaleyra_call_sheet_description_enable_camera)
        // Check the button contained in the draggable part of the bottom sheet is displayed
        // The first of the list is the button contained in the fixed part of the bottom sheet, but not rendered by the internal adaptive layout.
        composeTestRule
            .onAllNodesWithContentDescription(cameraText, useUnmergedTree = true)[0]
            .assertIsDisplayed()
            .performClick()

        verify(exactly = 1) { callViewModel.toggleCamera(any()) }
    }

    @Test
    fun testSheetDragActions_flipCamera() {
        val actions = allActions.filterNot { it is FlipCameraAction }.take(CompactScreenMaxActions)
        composeTestRule.setUpVCallScreen(
            sheetState = CallSheetState(CallSheetValue.Expanded),
        )
        callActionsUiState.value = CallActionsUiState(
            actionList = (actions + FlipCameraAction()).toImmutableList()
        )

        val flipText = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_flip_camera)
        // Check the button contained in the draggable part of the bottom sheet is displayed
        // The first of the list is the button contained in the fixed part of the bottom sheet, but not rendered by the internal adaptive layout.
        composeTestRule
            .onAllNodesWithText(flipText, useUnmergedTree = true)[0]
            .assertIsDisplayed()
            .performClick()

        verify(exactly = 1) { callViewModel.switchCamera() }
    }

    @Test
    fun testSheetDragActionsOnSmallScreen_chat() {
        val actions = allActions.filterNot { it is ChatAction }.take(CompactScreenMaxActions)
        composeTestRule.setUpVCallScreen(
            sheetState = CallSheetState(CallSheetValue.Expanded),
        )
        callActionsUiState.value = CallActionsUiState(
            actionList = (actions + ChatAction()).toImmutableList()
        )

        val chatText = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_chat)
        // Check the button contained in the draggable part of the bottom sheet is displayed
        // The first of the list is the button contained in the fixed part of the bottom sheet, but not rendered by the internal adaptive layout.
        composeTestRule
            .onAllNodesWithText(chatText, useUnmergedTree = true)[0]
            .assertIsDisplayed()
            .performClick()

        verify(exactly = 1) { callViewModel.showChat(any()) }
    }

    @Test
    fun testSheetDragActions_screenShareToggleOff() {
        val actions = allActions.filterNot { it is ScreenShareAction }.take(CompactScreenMaxActions)
        composeTestRule.setUpVCallScreen(
            sheetState = CallSheetState(CallSheetValue.Expanded),
        )
        callActionsUiState.value = CallActionsUiState(
            actionList = (actions + ScreenShareAction.UserChoice(isToggled = true)).toImmutableList()
        )

        val screenShareText =
            composeTestRule.activity.getString(R.string.kaleyra_call_sheet_description_stop_screen_share)
        // Check the button contained in the draggable part of the bottom sheet is displayed
        // The first of the list is the button contained in the fixed part of the bottom sheet, but not rendered by the internal adaptive layout.
        composeTestRule
            .onAllNodesWithText(screenShareText, useUnmergedTree = true)[0]
            .assertIsDisplayed()
            .performClick()

        verify(exactly = 1) { callViewModel.tryStopScreenShare() }
    }

    @Test
    fun testSheetDragActions_screenShareToggleOn() {
        val actions = allActions.filterNot { it is ScreenShareAction }.take(CompactScreenMaxActions)
        var component: ModularComponent? = null
        composeTestRule.setUpVCallScreen(
            sheetState = CallSheetState(CallSheetValue.Expanded),
            onModalSheetComponentRequest = { component = it }
        )
        callActionsUiState.value = CallActionsUiState(
            actionList = (actions + ScreenShareAction.UserChoice(isToggled = false)).toImmutableList()
        )

        val screenShareText =
            composeTestRule.activity.getString(R.string.kaleyra_call_sheet_screen_share)
        // Check the button contained in the draggable part of the bottom sheet is displayed
        // The first of the list is the button contained in the fixed part of the bottom sheet, but not rendered by the internal adaptive layout.
        composeTestRule
            .onAllNodesWithText(screenShareText, useUnmergedTree = true)[0]
            .assertIsDisplayed()
            .performClick()

        assertEquals(ModularComponent.ScreenShare, component)
    }

    @Test
    fun testSheetDragActions_audioComponentIsDisplayed() {
        val actions = (allActions - AudioAction()).take(CompactScreenMaxActions)
        var component: ModularComponent? = null
        composeTestRule.setUpVCallScreen(
            sheetState = CallSheetState(CallSheetValue.Expanded),
            onModalSheetComponentRequest = { component = it }
        )
        callActionsUiState.value = CallActionsUiState(
            actionList = (actions + AudioAction()).toImmutableList()
        )

        val audioText = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_audio)
        // Check the button contained in the draggable part of the bottom sheet is displayed
        // The first of the list is the button contained in the fixed part of the bottom sheet, but not rendered by the internal adaptive layout.
        composeTestRule
            .onAllNodesWithText(audioText, useUnmergedTree = true)[0]
            .assertIsDisplayed()
            .performClick()

        assertEquals(ModularComponent.Audio, component)
    }

    @Test
    fun testSheetDragActions_modalSheetFileShareComponentIsDisplayed() {
        val actions = (allActions - FileShareAction()).take(CompactScreenMaxActions)
        var component: ModularComponent? = null
        composeTestRule.setUpVCallScreen(
            sheetState = CallSheetState(CallSheetValue.Expanded),
            onModalSheetComponentRequest = { component = it }
        )
        callActionsUiState.value = CallActionsUiState(
            actionList = (actions + FileShareAction()).toImmutableList()
        )

        val fileShareText =
            composeTestRule.activity.getString(R.string.kaleyra_call_sheet_file_share)
        // Check the button contained in the draggable part of the bottom sheet is displayed
        // The first of the list is the button contained in the fixed part of the bottom sheet, but not rendered by the internal adaptive layout.
        composeTestRule
            .onAllNodesWithText(fileShareText, useUnmergedTree = true)[0]
            .assertIsDisplayed()
            .performClick()

        assertEquals(ModularComponent.FileShare, component)
    }

    @Test
    fun testSheetDragActions_modalSheetWhiteboardComponentIsDisplayed() {
        val actions = allActions.filterNot { it is WhiteboardAction }.take(CompactScreenMaxActions)
        var component: ModularComponent? = null
        composeTestRule.setUpVCallScreen(
            sheetState = CallSheetState(CallSheetValue.Expanded),
            onModalSheetComponentRequest = { component = it }
        )
        callActionsUiState.value = CallActionsUiState(
            actionList = (actions + WhiteboardAction()).toImmutableList()
        )

        val whiteboardText =
            composeTestRule.activity.getString(R.string.kaleyra_call_sheet_whiteboard)
        // Check the button contained in the draggable part of the bottom sheet is displayed
        // The first of the list is the button contained in the fixed part of the bottom sheet, but not rendered by the internal adaptive layout.
        composeTestRule
            .onAllNodesWithText(whiteboardText, useUnmergedTree = true)[0]
            .assertIsDisplayed()
            .performClick()

        assertEquals(ModularComponent.Whiteboard, component)
    }

    @Test
    fun testSheetDragActions_virtualBackgroundComponentChange() {
        val actions = allActions.filterNot { it is VirtualBackgroundAction }.take(CompactScreenMaxActions)
        var component: ModularComponent? = null
        composeTestRule.setUpVCallScreen(
            sheetState = CallSheetState(CallSheetValue.Expanded),
            onModalSheetComponentRequest = { component = it }
        )
        callActionsUiState.value = CallActionsUiState(
            actionList = (actions + VirtualBackgroundAction()).toImmutableList()
        )

        val virtualBgText =
            composeTestRule.activity.getString(R.string.kaleyra_call_sheet_virtual_background)
        // Check the button contained in the draggable part of the bottom sheet is displayed
        // The first of the list is the button contained in the fixed part of the bottom sheet, but not rendered by the internal adaptive layout.
        composeTestRule
            .onAllNodesWithText(virtualBgText, useUnmergedTree = true)[0]
            .assertIsDisplayed()
            .performClick()

        assertEquals(ModularComponent.VirtualBackground, component)
    }

    @Test
    fun audioModalSheetComponent_closeInvokeModalSheetComponentChangeToNull() {
        var component: ModularComponent? = ModularComponent.Audio
        composeTestRule.setUpVCallScreen(
            modalSheetComponent = ModularComponent.Audio,
            onModalSheetComponentRequest = { component = it }
        )

        val closeText = composeTestRule.activity.getString(R.string.kaleyra_close)
        composeTestRule
            .onNodeWithContentDescription(closeText, useUnmergedTree = true)
            .assertIsDisplayed()
            .performClick()

        composeTestRule.waitForIdle()

        assertEquals(null, component)
    }

    @Test
    fun screenShareModalSheetComponent_closeInvokeModalSheetComponentChangeToNull() {
        var component: ModularComponent? = ModularComponent.ScreenShare
        composeTestRule.setUpVCallScreen(
            modalSheetComponent = ModularComponent.ScreenShare,
            onModalSheetComponentRequest = { component = it }
        )

        val closeText = composeTestRule.activity.getString(R.string.kaleyra_close)
        composeTestRule
            .onNodeWithContentDescription(closeText, useUnmergedTree = true)
            .assertIsDisplayed()
            .performClick()

        composeTestRule.waitForIdle()

        assertEquals(null, component)
    }

    @Test
    fun fileShareModalSheetComponent_closeInvokeModalSheetComponentChangeToNull() {
        var component: ModularComponent? = ModularComponent.FileShare
        composeTestRule.setUpVCallScreen(
            modalSheetComponent = ModularComponent.FileShare,
            onModalSheetComponentRequest = { component = it }
        )

        val closeText = composeTestRule.activity.getString(R.string.kaleyra_close)
        composeTestRule
            .onNodeWithContentDescription(closeText, useUnmergedTree = true)
            .assertIsDisplayed()
            .performClick()

        composeTestRule.waitForIdle()

        assertEquals(null, component)
    }

    @Test
    fun fileShareSidePanelComponent_closeInvokeSidePanelComponentChangeToNull() {
        var component: ModularComponent? = ModularComponent.FileShare
        composeTestRule.setUpVCallScreen(
            sidePanelComponent = ModularComponent.FileShare,
            onSidePanelComponentRequest = { component = it }
        )

        val closeText = composeTestRule.activity.getString(R.string.kaleyra_close)
        composeTestRule
            .onNodeWithContentDescription(closeText, useUnmergedTree = true)
            .assertIsDisplayed()
            .performClick()

        composeTestRule.waitForIdle()

        assertEquals(null, component)
    }

    @Test
    fun whiteboardModalSheetComponent_closeInvokeModalSheetComponentChangeToNull() {
        var component: ModularComponent? = ModularComponent.Whiteboard
        composeTestRule.setUpVCallScreen(
            modalSheetComponent = ModularComponent.Whiteboard,
            onModalSheetComponentRequest = { component = it }
        )

        val closeText = composeTestRule.activity.getString(R.string.kaleyra_close)
        composeTestRule
            .onNodeWithContentDescription(closeText, useUnmergedTree = true)
            .assertIsDisplayed()
            .performClick()

        composeTestRule.waitForIdle()

        assertEquals(null, component)
    }

    @Test
    fun whiteboardSidePanelComponent_closeInvokeSidePanelComponentChangeToNull() {
        var component: ModularComponent? = ModularComponent.Whiteboard
        composeTestRule.setUpVCallScreen(
            sidePanelComponent = ModularComponent.Whiteboard,
            onSidePanelComponentRequest = { component = it }
        )

        val closeText = composeTestRule.activity.getString(R.string.kaleyra_close)
        composeTestRule
            .onNodeWithContentDescription(closeText, useUnmergedTree = true)
            .assertIsDisplayed()
            .performClick()

        composeTestRule.waitForIdle()

        assertEquals(null, component)
    }

    @Test
    fun virtualBgModalSheetComponent_closeInvokeModalSheetComponentChangeToNull() {
        var component: ModularComponent? = ModularComponent.VirtualBackground
        composeTestRule.setUpVCallScreen(
            modalSheetComponent = ModularComponent.VirtualBackground,
            onModalSheetComponentRequest = { component = it }
        )

        val closeText = composeTestRule.activity.getString(R.string.kaleyra_close)
        composeTestRule
            .onNodeWithContentDescription(closeText, useUnmergedTree = true)
            .assertIsDisplayed()
            .performClick()

        composeTestRule.waitForIdle()

        assertEquals(null, component)
    }

    @Test
    fun participantsModalSheetComponent_closeInvokeModalSheetComponentChangeToNull() {
        var component: ModularComponent? = ModularComponent.Participants
        composeTestRule.setUpVCallScreen(
            modalSheetComponent = ModularComponent.Participants,
            onModalSheetComponentRequest = { component = it }
        )

        val closeText = composeTestRule.activity.getString(R.string.kaleyra_close)
        composeTestRule
            .onNodeWithContentDescription(closeText, useUnmergedTree = true)
            .assertIsDisplayed()
            .performClick()

        composeTestRule.waitForIdle()

        assertEquals(null, component)
    }

    @Test
    fun participantsSidePanelComponent_closeInvokeSidePanelComponentChangeToNull() {
        var component: ModularComponent? = ModularComponent.Participants
        composeTestRule.setUpVCallScreen(
            sidePanelComponent = ModularComponent.Participants,
            onSidePanelComponentRequest = { component = it }
        )

        val closeText = composeTestRule.activity.getString(R.string.kaleyra_close)
        composeTestRule
            .onNodeWithContentDescription(closeText, useUnmergedTree = true)
            .assertIsDisplayed()
            .performClick()

        composeTestRule.waitForIdle()

        assertEquals(null, component)
    }

    @Test
    fun selectedStreamIdSet_streamMenuIsDisplayed() {
        val streamItem = StreamItem.Stream(
            id = "streamId",
            stream = StreamUi(id = "streamId", userInfo = UserInfo("userId", "username", ImmutableUri())),
        )
        streamUiState.value = StreamUiState(
            streamItems = listOf(streamItem).toImmutableList()
        )
        composeTestRule.setUpVCallScreen(selectedStreamId = "streamId")

        composeTestRule
            .onNodeWithText("username", useUnmergedTree = true)
            .assertIsDisplayed()
            .performClick()

        composeTestRule.onNodeWithTag(StreamMenuContentTestTag).assertIsDisplayed()
    }

    @Test
    fun selectedStreamIdNotSet_onSelectedStreamIdInvoked() {
        val streamItem = StreamItem.Stream(
            id = "streamId",
            stream = StreamUi(id = "streamId", userInfo = UserInfo("userId", "username", ImmutableUri())),
        )
        streamUiState.value = StreamUiState(
            streamItems = listOf(streamItem).toImmutableList()
        )
        var selectedStreamId: String? = null
        composeTestRule.setUpVCallScreen(
            selectedStreamId = null,
            onStreamSelected = { selectedStreamId = it }
        )

        composeTestRule
            .onNodeWithText("username", useUnmergedTree = true)
            .assertIsDisplayed()
            .performClick()

        assertEquals("streamId", selectedStreamId)
    }

    @Test
    fun userClicksOnStreamMenuCancel_onStreamSelectedCallbackToNull() {
        val streamItem = StreamItem.Stream(
            id = "streamId",
            stream = StreamUi(id = "streamId", userInfo = UserInfo("userId", "username", ImmutableUri())),
        )
        streamUiState.value = StreamUiState(
            streamItems = listOf(streamItem).toImmutableList()
        )
        var selectedStream: String? = "streamId"
        composeTestRule.setUpVCallScreen(
            sheetState = CallSheetState(CallSheetValue.Expanded),
            selectedStreamId = "streamId",
            onStreamSelected = { selectedStream = null }
        )

        val cancelText = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_cancel)
        composeTestRule
            .onNodeWithContentDescription(cancelText, useUnmergedTree = true)
            .assertIsDisplayed()
            .performClick()

        assertEquals(null, selectedStream)
    }

    @Test
    fun userClicksOnStopScreenShareOnStreamItem_stopScreenShareInvoked() {
        streamUiState.value = StreamUiState(isScreenShareActive = true)
        composeTestRule.setUpVCallScreen()

        val text = composeTestRule.activity.getString(R.string.kaleyra_stream_screenshare_action)
        composeTestRule
            .onNodeWithText(text, useUnmergedTree = true)
            .assertIsDisplayed()
            .performClick()

        verify(exactly = 1) { streamViewModel.tryStopScreenShare()  }
    }

    @Test
    fun selectedStreamIdSet_dragHandleIsNotDisplayed() {
        val streamItem = StreamItem.Stream(
            id = "streamId",
            stream = StreamUi(id = "streamId", userInfo = UserInfo("userId", "username", ImmutableUri())),
        )
        streamUiState.value = StreamUiState(
            streamItems = listOf(streamItem).toImmutableList()
        )
        callActionsUiState.value = CallActionsUiState(
            actionList = allActions.toImmutableList()
        )
        composeTestRule.setUpVCallScreen(
            selectedStreamId = "streamId"
        )

        composeTestRule.onNodeWithTag(StreamMenuContentTestTag).assertIsDisplayed()

        composeTestRule.onNodeWithTag(InputMessageDragHandleTag, useUnmergedTree = true).assertDoesNotExist()
    }


    @Test
    fun emptySheetDragActions_dragHandleIsNotDisplayed() {
        callActionsUiState.value = CallActionsUiState(
            actionList = ImmutableList(listOf(MicAction()))
        )
        composeTestRule.setUpVCallScreen()

        composeTestRule.onNodeWithTag(InputMessageDragHandleTag, useUnmergedTree = true).assertDoesNotExist()
    }

    @Test
    fun userClicksAppBarBack_onBackPressedInvoked() {
        var clicked = false
        composeTestRule.setUpVCallScreen(
            onBackPressed = { clicked = true }
        )

        val text = composeTestRule.activity.getString(R.string.kaleyra_back)
        composeTestRule
            .onNodeWithContentDescription(text, useUnmergedTree = true)
            .assertIsDisplayed()
            .performClick()

        assertEquals(true, clicked)
    }

    @Test
    fun userClicksMoreActionWhenSheetExpanded_onChangeSheetStateToNotToggled() {
        val sheetState = CallSheetState(initialValue = CallSheetValue.Expanded)
        var isToggled: Boolean? = null
        composeTestRule.setUpVCallScreen(
            sheetState = sheetState,
            onChangeSheetState = { isToggled = it }
        )
        callActionsUiState.value = CallActionsUiState(
            actionList = allActions.toImmutableList()
        )

        val moreText =
            composeTestRule.activity.getString(R.string.kaleyra_call_sheet_description_hide_actions)
        composeTestRule
            .onNodeWithContentDescription(moreText, useUnmergedTree = true)
            .assertIsDisplayed()
            .performClick()

        assertEquals(false, isToggled)
    }

    @Test
    fun userClicksMoreActionWhenSheetCollapsed_onChangeSheetStateToNotToggled() {
        val sheetState = CallSheetState(initialValue = CallSheetValue.Collapsed)
        var isToggled: Boolean? = null
        composeTestRule.setUpVCallScreen(
            sheetState = sheetState,
            onChangeSheetState = { isToggled = it }
        )
        callActionsUiState.value = CallActionsUiState(
            actionList = allActions.toImmutableList()
        )

        val moreText =
            composeTestRule.activity.getString(R.string.kaleyra_call_sheet_description_more_actions)
        composeTestRule
            .onNodeWithContentDescription(moreText, useUnmergedTree = true)
            .assertIsDisplayed()
            .performClick()

        assertEquals(true, isToggled)
    }

    @Test
    fun userClicksMoreParticipantsStream_onModularComponentChangeToParticipants() {
        val streams = listOf(
            StreamItem.MoreStreams(
                users = listOf(
                    MoreStreamsUserPreview("1", "user1", null),
                    MoreStreamsUserPreview("2", "user2", null),
                )
            )
        )
        streamUiState.value = StreamUiState(streamItems = streams.toImmutableList())

        var component: ModularComponent? = null
        composeTestRule.setUpVCallScreen(
            onModalSheetComponentRequest = { component = it }
        )

        val otherText = composeTestRule.activity.getString(R.string.kaleyra_stream_other_participants, 2)
        composeTestRule
            .onNodeWithText(otherText, useUnmergedTree = true)
            .assertIsDisplayed()
            .performClick()
        assertEquals(ModularComponent.Participants, component)
    }

    @Test
    fun userClicksParticipantsButton_onModularComponentChangeToParticipants() {
        var component: ModularComponent? = null
        composeTestRule.setUpVCallScreen(
            onModalSheetComponentRequest = { component = it }
        )

        val text = composeTestRule.activity.getString(R.string.kaleyra_show_participants_descr)
        composeTestRule.onNodeWithContentDescription(text).performClick()

        assertEquals(ModularComponent.Participants, component)
    }

    @Test
    fun testCallInfoComponentIsDisplayed() {
        composeTestRule.setUpVCallScreen()

        val title = composeTestRule.activity.getString(R.string.kaleyra_call_status_ended)
        val subtitle = composeTestRule.activity.getString(R.string.kaleyra_call_status_connecting)
        // check the content description because it's a TextView
        composeTestRule.onNodeWithText(title, useUnmergedTree = true).assertIsDisplayed()
        composeTestRule.onNodeWithText(subtitle, useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun userClicksScreenShareMessagePin_streamPinIsInvoked() {
        every { userMessagesViewModel.userMessage } returns flowOf(ImmutableList(listOf(PinScreenshareMessage("streamId", "username"))))
        composeTestRule.setUpVCallScreen()

        val text = composeTestRule.activity.getString(R.string.kaleyra_stream_screenshare_received, "username")
        val pinText = composeTestRule.activity.getString(R.string.kaleyra_user_message_pin)
        composeTestRule.onNodeWithText(text, useUnmergedTree = true).assertIsDisplayed()
        composeTestRule
            .onNodeWithText(pinText, useUnmergedTree = true)
            .assertIsDisplayed()
            .performClick()
        verify(exactly = 1) { streamViewModel.pinStream("streamId", prepend = true, force = true) }
    }

    @Test
    fun userClicksScreenShareMessagePinOnWhiteboard_streamPinIsInvoked() {
        val userMessageFlow = MutableStateFlow(ImmutableList<UserMessage>())
        every { userMessagesViewModel.userMessage } returns userMessageFlow

        composeTestRule.setUpVCallScreen(modalSheetComponent = ModularComponent.Whiteboard)

        val componentTitle = composeTestRule.activity.getString(R.string.kaleyra_whiteboard)
        composeTestRule.onNodeWithText(componentTitle).assertIsDisplayed()

        userMessageFlow.value = ImmutableList(listOf(PinScreenshareMessage("streamId", "username")))
        composeTestRule.waitForIdle()

        val text = composeTestRule.activity.getString(R.string.kaleyra_stream_screenshare_received, "username")
        val pinText = composeTestRule.activity.getString(R.string.kaleyra_user_message_pin)
        composeTestRule.onNodeWithText(text, useUnmergedTree = true).assertIsDisplayed()
        composeTestRule
            .onNodeWithText(pinText, useUnmergedTree = true)
            .assertIsDisplayed()
            .performClick()
        verify(exactly = 1) { streamViewModel.pinStream("streamId", prepend = true, force = true) }
    }

    @Test
    fun userClicksScreenShareMessagePinOnFileShare_streamPinIsInvoked() {
        val userMessageFlow = MutableStateFlow(ImmutableList<UserMessage>())
        every { userMessagesViewModel.userMessage } returns userMessageFlow

        composeTestRule.setUpVCallScreen(modalSheetComponent = ModularComponent.FileShare)

        val componentTitle = composeTestRule.activity.getString(R.string.kaleyra_fileshare)
        composeTestRule.onNodeWithText(componentTitle).assertIsDisplayed()

        userMessageFlow.value = ImmutableList(listOf(PinScreenshareMessage("streamId", "username")))
        composeTestRule.waitForIdle()

        val text = composeTestRule.activity.getString(R.string.kaleyra_stream_screenshare_received, "username")
        val pinText = composeTestRule.activity.getString(R.string.kaleyra_user_message_pin)
        composeTestRule.onNodeWithText(text, useUnmergedTree = true).assertIsDisplayed()
        composeTestRule
            .onNodeWithText(pinText, useUnmergedTree = true)
            .assertIsDisplayed()
            .performClick()
        verify(exactly = 1) { streamViewModel.pinStream("streamId", prepend = true, force = true) }
    }

    @Test
    fun compactDevice_companyLogoSet_brandLogoDisplayed() = runTest {
        composeTestRule.setUpVCallScreen()
        brandLogoUiState.emit(BrandLogoState(callStateUi = CallStateUi.Disconnected.Companion, logo = companyLogo))
        composeTestRule.waitForIdle()

        val companyLogo = composeTestRule.activity.getString(R.string.kaleyra_company_logo)
        composeTestRule.onNodeWithContentDescription(companyLogo, useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun compactDevice_companyLogoSet_callEndedNeverConnected_brandLogoDisplayed() = runTest {
        brandLogoUiState.emit(BrandLogoState(callStateUi = CallStateUi.Connecting, logo = companyLogo))
        brandLogoUiState.emit(BrandLogoState(callStateUi = CallStateUi.Disconnected.Ended, logo = companyLogo))

        composeTestRule.setUpVCallScreen()
        composeTestRule.waitForIdle()

        val companyLogo = composeTestRule.activity.getString(R.string.kaleyra_company_logo)
        composeTestRule.onNodeWithContentDescription(companyLogo, useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun compactDevice_companyLogoSet_callDisconnectingNeverConnected_brandLogoDisplayed() = runTest {
        brandLogoUiState.emit(BrandLogoState(callStateUi = CallStateUi.Connecting, logo = companyLogo))
        brandLogoUiState.emit(BrandLogoState(callStateUi = CallStateUi.Disconnecting, logo = companyLogo))

        composeTestRule.setUpVCallScreen()
        composeTestRule.waitForIdle()

        val companyLogo = composeTestRule.activity.getString(R.string.kaleyra_company_logo)
        composeTestRule.onNodeWithContentDescription(companyLogo, useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun compactDevice_companyLogoSet_callConnected_brandLogoNotDisplayed() = runTest {
        brandLogoUiState.emit(BrandLogoState(callStateUi = CallStateUi.Connected, logo = companyLogo))

        composeTestRule.setUpVCallScreen()
        composeTestRule.waitForIdle()

        val companyLogo = composeTestRule.activity.getString(R.string.kaleyra_company_logo)
        composeTestRule.onNodeWithContentDescription(companyLogo, useUnmergedTree = true).assertIsNotDisplayed()
    }

    @Test
    fun compactDevice_companyLogoSet_callReconnecting_brandLogoDisplayed() = runTest {
        brandLogoUiState.emit(BrandLogoState(callStateUi = CallStateUi.Connected, logo = companyLogo))
        brandLogoUiState.emit(BrandLogoState(callStateUi = CallStateUi.Reconnecting, logo = companyLogo))

        composeTestRule.setUpVCallScreen()
        composeTestRule.waitForIdle()

        val companyLogo = composeTestRule.activity.getString(R.string.kaleyra_company_logo)
        composeTestRule.onNodeWithContentDescription(companyLogo, useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun compactDevice_companyLogoNotSet_brandLogoNotDisplayed() = runTest {
        brandLogoUiState.emit(BrandLogoState())
        composeTestRule.setUpVCallScreen()
        composeTestRule.waitForIdle()

        val companyLogo = composeTestRule.activity.getString(R.string.kaleyra_company_logo)
        composeTestRule.onNodeWithContentDescription(companyLogo).assertDoesNotExist()
    }

    @Test
    fun chatIsDeleted_isChatDeletedInvoked() = runTest {
        phoneChatViewModelState.tryEmit(ChatUiState.OneToOne(isDeleted = true))
        var onChatDeleted = false
        composeTestRule.setUpVCallScreen(
            sidePanelComponent = ModularComponent.Chat,
            onChatDeleted = {
                onChatDeleted = true
            }
        )
        assert(onChatDeleted)
    }

    @Test
    fun chatHasFailedCreation_isChatDeletedInvoked() {
        phoneChatViewModelState.tryEmit(ChatUiState.OneToOne(hasFailedCreation = true))
        var onChatCreationFailed = false
        composeTestRule.setUpVCallScreen(
            sidePanelComponent = ModularComponent.Chat,
            onChatCreationFailed = {
                onChatCreationFailed = true
            }
        )
        assert(onChatCreationFailed)
    }
}
