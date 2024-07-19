package com.kaleyra.video_sdk.ui.call.screennew

import android.content.res.Configuration
import androidx.activity.ComponentActivity
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.rules.ActivityScenarioRule
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.call.appbar.model.CallAppBarUiState
import com.kaleyra.video_sdk.call.appbar.viewmodel.CallAppBarViewModel
import com.kaleyra.video_sdk.call.audiooutput.model.AudioOutputUiState
import com.kaleyra.video_sdk.call.audiooutput.viewmodel.AudioOutputViewModel
import com.kaleyra.video_sdk.call.bottomsheetnew.CallSheetState
import com.kaleyra.video_sdk.call.bottomsheetnew.CallSheetValue
import com.kaleyra.video_sdk.call.callactions.model.CallActionsUiState
import com.kaleyra.video_sdk.call.callactions.viewmodel.CallActionsViewModel
import com.kaleyra.video_sdk.call.callinfo.model.CallInfoUiState
import com.kaleyra.video_sdk.call.callinfo.viewmodel.CallInfoViewModel
import com.kaleyra.video_sdk.call.fileshare.model.FileShareUiState
import com.kaleyra.video_sdk.call.fileshare.viewmodel.FileShareViewModel
import com.kaleyra.video_sdk.call.participants.model.ParticipantsUiState
import com.kaleyra.video_sdk.call.participants.viewmodel.ParticipantsViewModel
import com.kaleyra.video_sdk.call.screennew.AudioAction
import com.kaleyra.video_sdk.call.screennew.CameraAction
import com.kaleyra.video_sdk.call.screennew.ChatAction
import com.kaleyra.video_sdk.call.screennew.CompactScreenMaxActions
import com.kaleyra.video_sdk.call.screennew.FileShareAction
import com.kaleyra.video_sdk.call.screennew.FlipCameraAction
import com.kaleyra.video_sdk.call.screennew.HangUpAction
import com.kaleyra.video_sdk.call.screennew.InputMessageDragHandleTag
import com.kaleyra.video_sdk.call.screennew.LargeScreenMaxActions
import com.kaleyra.video_sdk.call.screennew.MicAction
import com.kaleyra.video_sdk.call.screennew.ModalSheetComponent
import com.kaleyra.video_sdk.call.screennew.PanelTestTag
import com.kaleyra.video_sdk.call.screennew.ScreenShareAction
import com.kaleyra.video_sdk.call.screennew.StreamMenuContentTestTag
import com.kaleyra.video_sdk.call.screennew.VCallScreen
import com.kaleyra.video_sdk.call.screennew.VirtualBackgroundAction
import com.kaleyra.video_sdk.call.screennew.WhiteboardAction
import com.kaleyra.video_sdk.call.screenshare.model.ScreenShareUiState
import com.kaleyra.video_sdk.call.screenshare.viewmodel.ScreenShareViewModel
import com.kaleyra.video_sdk.call.streamnew.MaxFeaturedStreamsCompact
import com.kaleyra.video_sdk.call.streamnew.model.StreamUiState
import com.kaleyra.video_sdk.call.streamnew.model.core.StreamUi
import com.kaleyra.video_sdk.call.streamnew.model.core.VideoUi
import com.kaleyra.video_sdk.call.streamnew.viewmodel.StreamViewModel
import com.kaleyra.video_sdk.call.virtualbackground.model.VirtualBackgroundUiState
import com.kaleyra.video_sdk.call.virtualbackground.viewmodel.VirtualBackgroundViewModel
import com.kaleyra.video_sdk.call.whiteboard.model.WhiteboardUiState
import com.kaleyra.video_sdk.call.whiteboard.viewmodel.WhiteboardViewModel
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList
import com.kaleyra.video_sdk.common.immutablecollections.toImmutableList
import com.kaleyra.video_sdk.common.usermessages.model.StackedSnackbarUiState
import com.kaleyra.video_sdk.common.usermessages.viewmodel.UserMessagesViewModel
import com.kaleyra.video_sdk.utils.WindowSizeClassUtil
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import io.mockk.verify
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalMaterial3Api::class)
@RunWith(RobolectricTestRunner::class)
class VCallScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val compactScreenConfiguration = Configuration().apply {
        screenWidthDp = 480
        screenHeightDp = 600
    }

    private val largeScreenConfiguration = Configuration().apply {
        screenWidthDp = 600
        screenHeightDp = 900
    }

    private val callActionsUiState = MutableStateFlow(CallActionsUiState())

    private val streamUiState = MutableStateFlow(StreamUiState())

    private val callViewModel = mockk<CallActionsViewModel>(relaxed = true) {
        every { uiState } returns callActionsUiState
    }

    private val streamViewModel = mockk<StreamViewModel>(relaxed = true) {
        every { uiState } returns streamUiState
    }

    private val audioOutputViewModel = mockk<AudioOutputViewModel>(relaxed = true) {
        every { uiState } returns MutableStateFlow(AudioOutputUiState())
    }

    private val screenShareViewModel = mockk<ScreenShareViewModel>(relaxed = true) {
        every { uiState } returns MutableStateFlow(ScreenShareUiState())
    }

    private val fileShareViewModel = mockk<FileShareViewModel>(relaxed = true) {
        every { uiState } returns MutableStateFlow(FileShareUiState())
    }

    private val whiteboardViewModel = mockk<WhiteboardViewModel>(relaxed = true) {
        every { uiState } returns MutableStateFlow(WhiteboardUiState())
    }

    private val virtualBackgroundViewModel = mockk<VirtualBackgroundViewModel>(relaxed = true) {
        every { uiState } returns MutableStateFlow(VirtualBackgroundUiState())
    }

    private val callInfoViewModel = mockk<CallInfoViewModel>(relaxed = true) {
        every { uiState } returns MutableStateFlow(CallInfoUiState())
    }

    private val callAppBarViewModel = mockk<CallAppBarViewModel>(relaxed = true) {
        every { uiState } returns MutableStateFlow(CallAppBarUiState())
    }

    private val userMessagesViewModel = mockk<UserMessagesViewModel>(relaxed = true) {
        every { uiState } returns MutableStateFlow(StackedSnackbarUiState())
    }

    private val allActions = listOf(
        HangUpAction(),
        FlipCameraAction(),
        AudioAction(),
        ChatAction(),
        FileShareAction(),
        WhiteboardAction(),
        VirtualBackgroundAction(),
        MicAction(),
        CameraAction(),
        ScreenShareAction(),
    )

    @Before
    fun setUp() {
        mockkObject(CallActionsViewModel)
        mockkObject(StreamViewModel)
        mockkObject(AudioOutputViewModel)
        mockkObject(ScreenShareViewModel)
        mockkObject(FileShareViewModel)
        mockkObject(WhiteboardViewModel)
        mockkObject(VirtualBackgroundViewModel)
        mockkObject(CallInfoViewModel)
        mockkObject(CallAppBarViewModel)
        mockkObject(ParticipantsViewModel)
        mockkObject(UserMessagesViewModel)

        every { CallActionsViewModel.provideFactory(any()) } returns mockk {
            every { create<CallActionsViewModel>(any(), any()) } returns callViewModel
        }

        every { StreamViewModel.provideFactory(any()) } returns mockk {
            every { create<StreamViewModel>(any(), any()) } returns streamViewModel
        }
        every { AudioOutputViewModel.provideFactory(any()) } returns mockk {
            every { create<AudioOutputViewModel>(any(), any()) } returns audioOutputViewModel
        }
        every { ScreenShareViewModel.provideFactory(any()) } returns mockk {
            every { create<ScreenShareViewModel>(any(), any()) } returns screenShareViewModel
        }
        every { FileShareViewModel.provideFactory(any(), any()) } returns mockk {
            every { create<FileShareViewModel>(any(), any()) } returns fileShareViewModel
        }
        every { WhiteboardViewModel.provideFactory(any(), any()) } returns mockk {
            every { create<WhiteboardViewModel>(any(), any()) } returns whiteboardViewModel
        }
        every { VirtualBackgroundViewModel.provideFactory(any()) } returns mockk {
            every {
                create<VirtualBackgroundViewModel>(
                    any(),
                    any()
                )
            } returns virtualBackgroundViewModel
        }
        every { CallInfoViewModel.provideFactory(any()) } returns mockk {
            every { create<CallInfoViewModel>(any(), any()) } returns callInfoViewModel
        }
        every { CallAppBarViewModel.provideFactory(any()) } returns mockk {
            every { create<CallAppBarViewModel>(any(), any()) } returns callAppBarViewModel
        }
        every { ParticipantsViewModel.provideFactory(any()) } returns mockk {
            every { create<ParticipantsViewModel>(any(), any()) } returns mockk(relaxed = true) {
                every { uiState } returns MutableStateFlow(ParticipantsUiState())
            }
        }
        every { UserMessagesViewModel.provideFactory(any(), any()) } returns mockk {
            every { create<UserMessagesViewModel>(any(), any()) } returns userMessagesViewModel
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
        composeTestRule.setUpVCallScreen()
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
        composeTestRule.setUpVCallScreen()
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
        composeTestRule.setUpVCallScreen()
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
        var component: ModalSheetComponent? = null
        composeTestRule.setUpVCallScreen(
            onModalSheetComponentChange = { component = it }
        )
        callActionsUiState.value = CallActionsUiState(
            actionList = listOf(ScreenShareAction(isToggled = false)).toImmutableList()
        )

        val buttonText =
            composeTestRule.activity.getString(R.string.kaleyra_call_sheet_screen_share)
        composeTestRule
            .onNodeWithContentDescription(buttonText, useUnmergedTree = true)
            .assertIsDisplayed()
            .performClick()

        assertEquals(ModalSheetComponent.ScreenShare, component)
    }

    @Test
    fun testSheetActions_onModalSheetComponentChangeToAudio() {
        var component: ModalSheetComponent? = null
        composeTestRule.setUpVCallScreen(
            modalSheetComponent = null,
            onModalSheetComponentChange = { component = it }
        )
        callActionsUiState.value = CallActionsUiState(
            actionList = listOf(AudioAction()).toImmutableList()
        )

        val buttonText = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_audio)
        composeTestRule
            .onNodeWithContentDescription(buttonText, useUnmergedTree = true)
            .assertIsDisplayed()
            .performClick()

        assertEquals(ModalSheetComponent.Audio, component)
    }

    @Test
    fun testModalSheetAudio_audioComponentIsDisplayed() {
        composeTestRule.setUpVCallScreen(
            modalSheetComponent = ModalSheetComponent.Audio,
        )

        val componentTitle = composeTestRule.activity.getString(R.string.kaleyra_audio_route_title)
        composeTestRule.onNodeWithText(componentTitle).assertIsDisplayed()
    }

    @Test
    fun testSheetActions_onModalSheetComponentChangeToFileShare() {
        var component: ModalSheetComponent? = null
        composeTestRule.setUpVCallScreen(
            modalSheetComponent = null,
            onModalSheetComponentChange = { component = it }
        )
        callActionsUiState.value = CallActionsUiState(
            actionList = listOf(FileShareAction()).toImmutableList()
        )

        val buttonText = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_file_share)
        composeTestRule
            .onNodeWithContentDescription(buttonText, useUnmergedTree = true)
            .assertIsDisplayed()
            .performClick()
        assertEquals(ModalSheetComponent.FileShare, component)
    }

    @Test
    fun testModalSheetFileShare_fileShareComponentIsDisplayed() {
        composeTestRule.setUpVCallScreen(
            modalSheetComponent = ModalSheetComponent.FileShare,
        )

        val componentTitle = composeTestRule.activity.getString(R.string.kaleyra_fileshare)
        composeTestRule.onNodeWithText(componentTitle).assertIsDisplayed()
    }

    @Test
    fun testSheetActions_onModalSheetComponentChangeToWhiteboard() {
        var component: ModalSheetComponent? = null
        composeTestRule.setUpVCallScreen(
            modalSheetComponent = null,
            onModalSheetComponentChange = { component = it }
        )
        callActionsUiState.value = CallActionsUiState(
            actionList = listOf(WhiteboardAction()).toImmutableList()
        )

        val buttonText = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_whiteboard)
        composeTestRule
            .onNodeWithContentDescription(buttonText, useUnmergedTree = true)
            .assertIsDisplayed()
            .performClick()

        assertEquals(ModalSheetComponent.Whiteboard, component)
    }

    @Test
    fun testModalSheetWhiteboard_whiteboardComponentIsDisplayed() {
        composeTestRule.setUpVCallScreen(
            modalSheetComponent = ModalSheetComponent.Whiteboard,
        )

        val componentTitle = composeTestRule.activity.getString(R.string.kaleyra_whiteboard)
        composeTestRule.onNodeWithText(componentTitle).assertIsDisplayed()
    }

    @Test
    fun testSheetActions_virtualBackgroundComponentIsDisplayed() {
        var component: ModalSheetComponent? = null
        composeTestRule.setUpVCallScreen(
            modalSheetComponent = null,
            onModalSheetComponentChange = { component = it }
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

        assertEquals(ModalSheetComponent.VirtualBackground, component)
    }

    @Test
    fun testModalSheetVirtualBackground_virtualBackgroundComponentIsDisplayed() {
        composeTestRule.setUpVCallScreen(
            modalSheetComponent = ModalSheetComponent.VirtualBackground,
        )

        val componentTitle =
            composeTestRule.activity.getString(R.string.kaleyra_virtual_background_picker_title)
        composeTestRule.onNodeWithText(componentTitle).assertIsDisplayed()
    }

    @Test
    fun testModalSheetParticipants_participantsComponentIsDisplayed() {
        composeTestRule.setUpVCallScreen(
            modalSheetComponent = ModalSheetComponent.Participants,
        )

        val componentTitle =
            composeTestRule.activity.getString(R.string.kaleyra_participants_component_change_layout)
        composeTestRule.onNodeWithText(componentTitle).assertIsDisplayed()
    }

    @Test
    fun testSheetActionsWithLargeScreen_moreShowPanel() {
        composeTestRule.setUpVCallScreen(configuration = largeScreenConfiguration)
        callActionsUiState.value = CallActionsUiState(
            actionList = allActions.toImmutableList()
        )

        val moreText = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_description_more_actions)
        composeTestRule
            .onNodeWithContentDescription(moreText, useUnmergedTree = true)
            .assertIsDisplayed()
            .performClick()

        composeTestRule.onNodeWithTag(PanelTestTag).assertIsDisplayed()
    }

    @Test
    fun testSheetActionsWithLargeScreen_moreHidePanel() {
        composeTestRule.setUpVCallScreen(configuration = largeScreenConfiguration)
        callActionsUiState.value = CallActionsUiState(
            actionList = allActions.toImmutableList()
        )

        val moreText = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_description_more_actions)
        composeTestRule.onNodeWithContentDescription(moreText, useUnmergedTree = true).performClick()

        composeTestRule.onNodeWithTag(PanelTestTag).assertIsDisplayed()

        val hideText = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_description_hide_actions)
        composeTestRule.onNodeWithContentDescription(hideText, useUnmergedTree = true).performClick()

        composeTestRule.onNodeWithText(PanelTestTag).assertDoesNotExist()
    }

    @Test
    fun testSheetDragActions_hangUp() {
        val actions = allActions.filterNot { it is  HangUpAction}.take(CompactScreenMaxActions)
        composeTestRule.setUpVCallScreen(
            sheetState = CallSheetState(CallSheetValue.Expanded),
            configuration = compactScreenConfiguration
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
        val actions = allActions.filterNot { it is  MicAction}.take(CompactScreenMaxActions)
        composeTestRule.setUpVCallScreen(
            sheetState = CallSheetState(CallSheetValue.Expanded),
            configuration = compactScreenConfiguration
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
        val actions = allActions.filterNot { it is  MicAction}.take(CompactScreenMaxActions)
        composeTestRule.setUpVCallScreen(
            sheetState = CallSheetState(CallSheetValue.Expanded),
            configuration = compactScreenConfiguration
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
        val actions = allActions.filterNot { it is CameraAction}.take(CompactScreenMaxActions)
        composeTestRule.setUpVCallScreen(
            sheetState = CallSheetState(CallSheetValue.Expanded),
            configuration = compactScreenConfiguration
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
        val actions = allActions.filterNot { it is  CameraAction}.take(CompactScreenMaxActions)
        composeTestRule.setUpVCallScreen(
            sheetState = CallSheetState(CallSheetValue.Expanded),
            configuration = compactScreenConfiguration
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
        val actions = allActions.filterNot { it is  FlipCameraAction}.take(CompactScreenMaxActions)
        composeTestRule.setUpVCallScreen(
            sheetState = CallSheetState(CallSheetValue.Expanded),
            configuration = compactScreenConfiguration
        )
        callActionsUiState.value = CallActionsUiState(
            actionList = (actions + FlipCameraAction()).toImmutableList()
        )

        val flipText = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_flip_camera)
        // Check the button contained in the draggable part of the bottom sheet is displayed
        // The first of the list is the button contained in the fixed part of the bottom sheet, but not rendered by the internal adaptive layout.
        composeTestRule
            .onAllNodesWithContentDescription(flipText, useUnmergedTree = true)[0]
            .assertIsDisplayed()
            .performClick()

        verify(exactly = 1) { callViewModel.switchCamera() }
    }

    @Test
    fun testSheetDragActions_chat() {
        val actions = allActions.filterNot { it is  ChatAction}.take(CompactScreenMaxActions)
        composeTestRule.setUpVCallScreen(
            sheetState = CallSheetState(CallSheetValue.Expanded),
            configuration = compactScreenConfiguration
        )
        callActionsUiState.value = CallActionsUiState(
            actionList = (actions + ChatAction()).toImmutableList()
        )

        val chatText = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_chat)
        // Check the button contained in the draggable part of the bottom sheet is displayed
        // The first of the list is the button contained in the fixed part of the bottom sheet, but not rendered by the internal adaptive layout.
        composeTestRule
            .onAllNodesWithContentDescription(chatText, useUnmergedTree = true)[0]
            .assertIsDisplayed()
            .performClick()

        verify(exactly = 1) { callViewModel.showChat(any()) }
    }

    @Test
    fun testSheetDragActions_screenShareToggleOff() {
        val actions = allActions.filterNot { it is  ScreenShareAction}.take(CompactScreenMaxActions)
        composeTestRule.setUpVCallScreen(
            sheetState = CallSheetState(CallSheetValue.Expanded),
            configuration = compactScreenConfiguration
        )
        callActionsUiState.value = CallActionsUiState(
            actionList = (actions + ScreenShareAction(isToggled = true)).toImmutableList()
        )

        val screenShareText =
            composeTestRule.activity.getString(R.string.kaleyra_call_sheet_description_stop_screen_share)
        // Check the button contained in the draggable part of the bottom sheet is displayed
        // The first of the list is the button contained in the fixed part of the bottom sheet, but not rendered by the internal adaptive layout.
        composeTestRule
            .onAllNodesWithContentDescription(screenShareText, useUnmergedTree = true)[0]
            .assertIsDisplayed()
            .performClick()

        verify(exactly = 1) { callViewModel.tryStopScreenShare() }
    }

    @Test
    fun testSheetDragActions_screenShareToggleOn() {
        val actions = allActions.filterNot { it is  ScreenShareAction}.take(CompactScreenMaxActions)
        var component: ModalSheetComponent? = null
        composeTestRule.setUpVCallScreen(
            sheetState = CallSheetState(CallSheetValue.Expanded),
            configuration = compactScreenConfiguration,
            onModalSheetComponentChange = { component = it }
        )
        callActionsUiState.value = CallActionsUiState(
            actionList = (actions + ScreenShareAction(isToggled = false)).toImmutableList()
        )

        val screenShareText =
            composeTestRule.activity.getString(R.string.kaleyra_call_sheet_screen_share)
        // Check the button contained in the draggable part of the bottom sheet is displayed
        // The first of the list is the button contained in the fixed part of the bottom sheet, but not rendered by the internal adaptive layout.
        composeTestRule
            .onAllNodesWithContentDescription(screenShareText, useUnmergedTree = true)[0]
            .assertIsDisplayed()
            .performClick()

        assertEquals(ModalSheetComponent.ScreenShare, component)
    }

    @Test
    fun testSheetDragActions_audioComponentIsDisplayed() {
        val actions = (allActions - AudioAction()).take(CompactScreenMaxActions)
        var component: ModalSheetComponent? = null
        composeTestRule.setUpVCallScreen(
            sheetState = CallSheetState(CallSheetValue.Expanded),
            configuration = compactScreenConfiguration,
            onModalSheetComponentChange = { component = it }
        )
        callActionsUiState.value = CallActionsUiState(
            actionList = (actions + AudioAction()).toImmutableList()
        )

        val audioText = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_audio)
        // Check the button contained in the draggable part of the bottom sheet is displayed
        // The first of the list is the button contained in the fixed part of the bottom sheet, but not rendered by the internal adaptive layout.
        composeTestRule
            .onAllNodesWithContentDescription(audioText, useUnmergedTree = true)[0]
            .assertIsDisplayed()
            .performClick()

        assertEquals(ModalSheetComponent.Audio, component)
    }

    @Test
    fun testSheetDragActions_fileShareComponentIsDisplayed() {
        val actions = (allActions - FileShareAction()).take(CompactScreenMaxActions)
        var component: ModalSheetComponent? = null
        composeTestRule.setUpVCallScreen(
            sheetState = CallSheetState(CallSheetValue.Expanded),
            configuration = compactScreenConfiguration,
            onModalSheetComponentChange = { component = it }
        )
        callActionsUiState.value = CallActionsUiState(
            actionList = (actions + FileShareAction()).toImmutableList()
        )

        val fileShareText =
            composeTestRule.activity.getString(R.string.kaleyra_call_sheet_file_share)
        // Check the button contained in the draggable part of the bottom sheet is displayed
        // The first of the list is the button contained in the fixed part of the bottom sheet, but not rendered by the internal adaptive layout.
        composeTestRule
            .onAllNodesWithContentDescription(fileShareText, useUnmergedTree = true)[0]
            .assertIsDisplayed()
            .performClick()

        assertEquals(ModalSheetComponent.FileShare, component)
    }

    @Test
    fun testSheetDragActions_onModalSheetComponentChangeToWhiteboard() {
        val actions = allActions.filterNot { it is  WhiteboardAction}.take(CompactScreenMaxActions)
        var component: ModalSheetComponent? = null
        composeTestRule.setUpVCallScreen(
            sheetState = CallSheetState(CallSheetValue.Expanded),
            configuration = compactScreenConfiguration,
            onModalSheetComponentChange = { component = it }
        )
        callActionsUiState.value = CallActionsUiState(
            actionList = (actions + WhiteboardAction()).toImmutableList()
        )

        val whiteboardText =
            composeTestRule.activity.getString(R.string.kaleyra_call_sheet_whiteboard)
        // Check the button contained in the draggable part of the bottom sheet is displayed
        // The first of the list is the button contained in the fixed part of the bottom sheet, but not rendered by the internal adaptive layout.
        composeTestRule
            .onAllNodesWithContentDescription(whiteboardText, useUnmergedTree = true)[0]
            .assertIsDisplayed()
            .performClick()

        assertEquals(ModalSheetComponent.Whiteboard, component)
    }

    @Test
    fun testSheetDragActions_virtualBackgroundComponentChange() {
        val actions = allActions.filterNot { it is  VirtualBackgroundAction}.take(CompactScreenMaxActions)
        var component: ModalSheetComponent? = null
        composeTestRule.setUpVCallScreen(
            sheetState = CallSheetState(CallSheetValue.Expanded),
            configuration = compactScreenConfiguration,
            onModalSheetComponentChange = { component = it }
        )
        callActionsUiState.value = CallActionsUiState(
            actionList = (actions + VirtualBackgroundAction()).toImmutableList()
        )

        val virtualBgText =
            composeTestRule.activity.getString(R.string.kaleyra_call_sheet_virtual_background)
        // Check the button contained in the draggable part of the bottom sheet is displayed
        // The first of the list is the button contained in the fixed part of the bottom sheet, but not rendered by the internal adaptive layout.
        composeTestRule
            .onAllNodesWithContentDescription(virtualBgText, useUnmergedTree = true)[0]
            .assertIsDisplayed()
            .performClick()

        assertEquals(ModalSheetComponent.VirtualBackground, component)
    }

    @Test
    fun testSheetPanelActions_flipCamera() {
        val actions = allActions.filterNot { it is  FlipCameraAction }.take(LargeScreenMaxActions)
        composeTestRule.setUpVCallScreen(configuration = largeScreenConfiguration)
        callActionsUiState.value = CallActionsUiState(
            actionList = (actions + FlipCameraAction()).toImmutableList()
        )

        val moreText =
            composeTestRule.activity.getString(R.string.kaleyra_call_sheet_description_more_actions)
        composeTestRule
            .onNodeWithContentDescription(moreText, useUnmergedTree = true)
            .assertIsDisplayed()
            .performClick()

        composeTestRule.waitForIdle()

        val flipText = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_flip_camera)
        composeTestRule
            .onAllNodesWithText(flipText, useUnmergedTree = true)[0]
            .assertIsDisplayed()
            .performClick()

        verify(exactly = 1) { callViewModel.switchCamera() }
    }

    @Test
    fun testSheetPanelActions_audioComponentIsDisplayed() {
        val actions = allActions.filterNot { it is  AudioAction}.take(LargeScreenMaxActions)
        var component: ModalSheetComponent? = null
        composeTestRule.setUpVCallScreen(
            configuration = largeScreenConfiguration,
            onModalSheetComponentChange = { component = it }
        )
        callActionsUiState.value = CallActionsUiState(
            actionList = (actions + AudioAction()).toImmutableList()
        )

        val moreText =
            composeTestRule.activity.getString(R.string.kaleyra_call_sheet_description_more_actions)
        composeTestRule
            .onNodeWithContentDescription(moreText, useUnmergedTree = true)
            .assertIsDisplayed()
            .performClick()

        composeTestRule.waitForIdle()

        val audioText = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_audio)
        composeTestRule
            .onAllNodesWithText(audioText, useUnmergedTree = true)[0]
            .assertIsDisplayed()
            .performClick()

        assertEquals(ModalSheetComponent.Audio, component)
    }

    @Test
    fun testSheetPanelActions_chat() {
        val actions = (allActions - ChatAction()).take(LargeScreenMaxActions)
        composeTestRule.setUpVCallScreen(configuration = largeScreenConfiguration)
        callActionsUiState.value = CallActionsUiState(
            actionList = (actions + ChatAction()).toImmutableList(),
        )

        val moreText =
            composeTestRule.activity.getString(R.string.kaleyra_call_sheet_description_more_actions)
        composeTestRule
            .onNodeWithContentDescription(moreText, useUnmergedTree = true)
            .assertIsDisplayed()
            .performClick()

        composeTestRule.waitForIdle()

        val chatText = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_chat)
        composeTestRule
            .onAllNodesWithText(chatText, useUnmergedTree = true)[0]
            .assertIsDisplayed()
            .performClick()

        verify(exactly = 1) { callViewModel.showChat(any()) }
    }

    @Test
    fun testSheetPanelActions_screenShareToggleOff() {
        val actions = (allActions - ScreenShareAction()).take(LargeScreenMaxActions)
        composeTestRule.setUpVCallScreen(
            configuration = largeScreenConfiguration,
        )
        callActionsUiState.value = CallActionsUiState(
            actionList = (actions + ScreenShareAction(isToggled = true)).toImmutableList(),
        )

        val moreText =
            composeTestRule.activity.getString(R.string.kaleyra_call_sheet_description_more_actions)
        composeTestRule
            .onNodeWithContentDescription(moreText, useUnmergedTree = true)
            .assertIsDisplayed()
            .performClick()

        composeTestRule.waitForIdle()

        val screenShareText = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_description_stop_screen_share)
        composeTestRule
            .onAllNodesWithText(screenShareText, useUnmergedTree = true)[0]
            .assertIsDisplayed()
            .performClick()

        verify(exactly = 1) { callViewModel.tryStopScreenShare() }
    }

    @Test
    fun testSheetPanelActions_screenShareToggleOn() {
        val actions = (allActions - ScreenShareAction()).take(LargeScreenMaxActions)
        var component: ModalSheetComponent? = null
        composeTestRule.setUpVCallScreen(
            configuration = largeScreenConfiguration,
            onModalSheetComponentChange = { component = it }
        )
        callActionsUiState.value = CallActionsUiState(
            actionList = (actions + ScreenShareAction()).toImmutableList(),
        )

        val moreText =
            composeTestRule.activity.getString(R.string.kaleyra_call_sheet_description_more_actions)
        composeTestRule
            .onNodeWithContentDescription(moreText, useUnmergedTree = true)
            .assertIsDisplayed()
            .performClick()

        composeTestRule.waitForIdle()

        val screenShareText = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_screen_share)
        composeTestRule
            .onAllNodesWithText(screenShareText, useUnmergedTree = true)[0]
            .assertIsDisplayed()
            .performClick()

        assertEquals(ModalSheetComponent.ScreenShare, component)
    }

    @Test
    fun testSheetPanelActions_fileShareComponentIsDisplayed() {
        val actions = (allActions - FileShareAction()).take(LargeScreenMaxActions)
        var component: ModalSheetComponent? = null
        composeTestRule.setUpVCallScreen(
            configuration = largeScreenConfiguration,
            onModalSheetComponentChange = { component = it }
        )
        callActionsUiState.value = CallActionsUiState(
            actionList = (actions + FileShareAction()).toImmutableList()
        )

        val moreText =
            composeTestRule.activity.getString(R.string.kaleyra_call_sheet_description_more_actions)
        composeTestRule
            .onNodeWithContentDescription(moreText, useUnmergedTree = true)
            .assertIsDisplayed()
            .performClick()

        composeTestRule.waitForIdle()

        val fileShareText =
            composeTestRule.activity.getString(R.string.kaleyra_call_sheet_file_share)
        composeTestRule
            .onAllNodesWithText(fileShareText, useUnmergedTree = true)[0]
            .assertIsDisplayed()
            .performClick()

        assertEquals(ModalSheetComponent.FileShare, component)
    }

    @Test
    fun testSheetPanelActions_whiteboardComponentIsDisplayed() {
        val actions = (allActions - WhiteboardAction()).take(LargeScreenMaxActions)
        var component: ModalSheetComponent? = null
        composeTestRule.setUpVCallScreen(
            configuration = largeScreenConfiguration,
            onModalSheetComponentChange = { component = it }
        )
        callActionsUiState.value = CallActionsUiState(
            actionList = (actions + WhiteboardAction()).toImmutableList()
        )

        val moreText =
            composeTestRule.activity.getString(R.string.kaleyra_call_sheet_description_more_actions)
        composeTestRule
            .onNodeWithContentDescription(moreText, useUnmergedTree = true)
            .assertIsDisplayed()
            .performClick()

        composeTestRule.waitForIdle()

        val whiteboardText =
            composeTestRule.activity.getString(R.string.kaleyra_call_sheet_whiteboard)
        composeTestRule
            .onAllNodesWithText(whiteboardText, useUnmergedTree = true)[0]
            .assertIsDisplayed()
            .performClick()

        assertEquals(ModalSheetComponent.Whiteboard, component)
    }

    @Test
    fun testSheetPanelActions_virtualBackgroundComponentIsDisplayed() {
        val actions = allActions.filterNot { it is  VirtualBackgroundAction }.take(LargeScreenMaxActions)
        var component: ModalSheetComponent? = null
        composeTestRule.setUpVCallScreen(
            configuration = largeScreenConfiguration,
            onModalSheetComponentChange = { component = it }
        )
        callActionsUiState.value = CallActionsUiState(
            actionList = (actions + VirtualBackgroundAction()).toImmutableList()
        )

        val moreText =
            composeTestRule.activity.getString(R.string.kaleyra_call_sheet_description_more_actions)
        composeTestRule
            .onNodeWithContentDescription(moreText, useUnmergedTree = true)
            .assertIsDisplayed()
            .performClick()

        composeTestRule.waitForIdle()

        val virtualBgText =
            composeTestRule.activity.getString(R.string.kaleyra_call_sheet_virtual_background)
        composeTestRule
            .onAllNodesWithText(virtualBgText, useUnmergedTree = true)[0]
            .assertIsDisplayed()
            .performClick()

        assertEquals(ModalSheetComponent.VirtualBackground, component)
    }

    @Test
    fun audioModalSheetComponent_closeInvokeModalSheetComponentChangeToNull() {
        var component: ModalSheetComponent? = ModalSheetComponent.Audio
        composeTestRule.setUpVCallScreen(
            modalSheetComponent = ModalSheetComponent.Audio,
            onModalSheetComponentChange = { component = it }
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
        var component: ModalSheetComponent? = ModalSheetComponent.ScreenShare
        composeTestRule.setUpVCallScreen(
            modalSheetComponent = ModalSheetComponent.ScreenShare,
            onModalSheetComponentChange = { component = it }
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
        var component: ModalSheetComponent? = ModalSheetComponent.FileShare
        composeTestRule.setUpVCallScreen(
            modalSheetComponent = ModalSheetComponent.FileShare,
            onModalSheetComponentChange = { component = it }
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
        var component: ModalSheetComponent? = ModalSheetComponent.Whiteboard
        composeTestRule.setUpVCallScreen(
            modalSheetComponent = ModalSheetComponent.Whiteboard,
            onModalSheetComponentChange = { component = it }
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
        var component: ModalSheetComponent? = ModalSheetComponent.VirtualBackground
        composeTestRule.setUpVCallScreen(
            modalSheetComponent = ModalSheetComponent.VirtualBackground,
            onModalSheetComponentChange = { component = it }
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
        var component: ModalSheetComponent? = ModalSheetComponent.Participants
        composeTestRule.setUpVCallScreen(
            modalSheetComponent = ModalSheetComponent.Participants,
            onModalSheetComponentChange = { component = it }
        )

        val closeText = composeTestRule.activity.getString(R.string.kaleyra_participants_component_close)
        composeTestRule
            .onNodeWithContentDescription(closeText, useUnmergedTree = true)
            .assertIsDisplayed()
            .performClick()

        composeTestRule.waitForIdle()

        assertEquals(null, component)
    }

    @Test
    fun selectedStreamIdSet_streamMenuIsDisplayed() {
        val streams = StreamUi(id = "streamId", username = "username")
        streamUiState.value = StreamUiState(
            streams = listOf(streams).toImmutableList()
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
        val streams = StreamUi(id = "streamId", username = "username")
        streamUiState.value = StreamUiState(
            streams = listOf(streams).toImmutableList()
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
        val streams = StreamUi(id = "streamId", username = "username")
        streamUiState.value = StreamUiState(
            streams = listOf(streams).toImmutableList()
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
        val streams = StreamUi(
            id = "streamId",
            username = "username",
            video = VideoUi(id = "videoId", isScreenShare = true),
            isMine = true
        )
        streamUiState.value = StreamUiState(
            streams = listOf(streams).toImmutableList()
        )
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
        val streams = StreamUi(id = "streamId", username = "username")
        streamUiState.value = StreamUiState(
            streams = listOf(streams).toImmutableList()
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
    fun largeScreen_dragHandleIsNotDisplayed() {
        callActionsUiState.value = CallActionsUiState(
            actionList = allActions.toImmutableList()
        )
        composeTestRule.setUpVCallScreen(
            configuration = largeScreenConfiguration
        )

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
            configuration = compactScreenConfiguration,
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
            configuration = compactScreenConfiguration,
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
    fun userClicksMoreParticipantsStream_onModalSheetComponentChangeToParticipants() {
        val streams = (1..MaxFeaturedStreamsCompact + 1).map { index ->
            StreamUi(id = "streamId$index", username = "username$index" )
        }
        streamUiState.value = StreamUiState(streams = streams.toImmutableList())

        var component: ModalSheetComponent? = null
        composeTestRule.setUpVCallScreen(
            configuration = compactScreenConfiguration,
            onModalSheetComponentChange = { component = it }
        )

        val otherText = composeTestRule.activity.getString(R.string.kaleyra_stream_other_participants, 2)
        composeTestRule
            .onNodeWithText(otherText, useUnmergedTree = true)
            .assertIsDisplayed()
            .performClick()
        assertEquals(ModalSheetComponent.Participants, component)
    }

    @Test
    fun userClicksParticipantsButton_onModalSheetComponentChangeToParticipants() {
        var component: ModalSheetComponent? = null
        composeTestRule.setUpVCallScreen(
            onModalSheetComponentChange = { component = it }
        )

        val text = composeTestRule.activity.getString(R.string.kaleyra_show_participants_descr)
        composeTestRule.onNodeWithContentDescription(text).performClick()

        assertEquals(ModalSheetComponent.Participants, component)
    }

    private fun AndroidComposeTestRule<ActivityScenarioRule<ComponentActivity>, ComponentActivity>.setUpVCallScreen(
        configuration: Configuration = compactScreenConfiguration,
        sheetState: CallSheetState = CallSheetState(),
        onChangeSheetState: (Boolean) -> Unit = {},
        selectedStreamId: String? = null,
        onStreamSelected: (String?) -> Unit = {},
        modalSheetComponent: ModalSheetComponent? = null,
        onModalSheetComponentChange: (ModalSheetComponent?) -> Unit = { },
        onAskInputPermissions: (Boolean) -> Unit = {},
        onBackPressed: () -> Unit = { },
    ) {
        setContent {
            VCallScreen(
                windowSizeClass = WindowSizeClassUtil.currentWindowAdaptiveInfo(configuration),
                sheetState = sheetState,
                modalSheetComponent = modalSheetComponent,
                onChangeSheetState = onChangeSheetState,
                selectedStreamId = selectedStreamId,
                onStreamSelected = onStreamSelected,
                modalSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                onModalSheetComponentRequest = onModalSheetComponentChange,
                onAskInputPermissions = onAskInputPermissions,
                onBackPressed = onBackPressed,
            )
        }
    }
}
