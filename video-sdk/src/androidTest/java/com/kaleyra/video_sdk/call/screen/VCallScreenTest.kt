package com.kaleyra.video_sdk.call.screen

import android.content.res.Configuration
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.ui.test.assertAll
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onAncestors
import androidx.compose.ui.test.onChildAt
import androidx.compose.ui.test.onChildren
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.rules.ActivityScenarioRule
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.PermissionStatus
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.call.appbar.model.CallAppBarUiState
import com.kaleyra.video_sdk.call.appbar.viewmodel.CallAppBarViewModel
import com.kaleyra.video_sdk.call.audiooutput.model.AudioOutputUiState
import com.kaleyra.video_sdk.call.audiooutput.viewmodel.AudioOutputViewModel
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
import com.kaleyra.video_sdk.call.screen.view.CallScreenModalSheetTag
import com.kaleyra.video_sdk.call.screen.view.vcallscreen.InputMessageDragHandleTag
import com.kaleyra.video_sdk.call.screen.view.vcallscreen.PanelTestTag
import com.kaleyra.video_sdk.call.screen.view.vcallscreen.StreamMenuContentTestTag
import com.kaleyra.video_sdk.call.screen.view.vcallscreen.VCallScreen
import com.kaleyra.video_sdk.call.screenshare.model.ScreenShareUiState
import com.kaleyra.video_sdk.call.screenshare.viewmodel.ScreenShareViewModel
import com.kaleyra.video_sdk.call.stream.MaxFeaturedStreamsCompact
import com.kaleyra.video_sdk.call.stream.model.StreamUiState
import com.kaleyra.video_sdk.call.stream.model.core.StreamUi
import com.kaleyra.video_sdk.call.stream.model.core.VideoUi
import com.kaleyra.video_sdk.call.stream.viewmodel.StreamViewModel
import com.kaleyra.video_sdk.call.virtualbackground.model.VirtualBackgroundUiState
import com.kaleyra.video_sdk.call.virtualbackground.viewmodel.VirtualBackgroundViewModel
import com.kaleyra.video_sdk.call.whiteboard.model.WhiteboardUiState
import com.kaleyra.video_sdk.call.whiteboard.viewmodel.WhiteboardViewModel
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList
import com.kaleyra.video_sdk.common.immutablecollections.toImmutableList
import com.kaleyra.video_sdk.common.usermessages.model.PinScreenshareMessage
import com.kaleyra.video_sdk.common.usermessages.model.RecordingMessage
import com.kaleyra.video_sdk.common.usermessages.model.StackedSnackbarUiState
import com.kaleyra.video_sdk.common.usermessages.model.UserMessage
import com.kaleyra.video_sdk.common.usermessages.viewmodel.UserMessagesViewModel
import com.kaleyra.video_sdk.utils.WindowSizeClassUtil
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import io.mockk.verify
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.reflect.KClass

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
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
        every { uiState } returns MutableStateFlow(CallInfoUiState(callStateUi = CallStateUi.Disconnected.Ended, displayState = TextRef.StringResource(R.string.kaleyra_call_status_connecting)))
    }

    private val callAppBarViewModel = mockk<CallAppBarViewModel>(relaxed = true) {
        every { uiState } returns MutableStateFlow(CallAppBarUiState())
    }

    private val userMessagesViewModel = mockk<UserMessagesViewModel>(relaxed = true) {
        every { uiState } returns MutableStateFlow(StackedSnackbarUiState())
    }

    private val companyLogo = Logo(light = Uri.parse("https://www.example.com/light.png"), dark = Uri.parse("https://www.example.com/dark.png"))
    private val brandLogoUiState = MutableStateFlow(BrandLogoState())
    private val brandLogoViewModel = mockk<BrandLogoViewModel>(relaxed = true) {
        every { uiState } returns brandLogoUiState
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
        ScreenShareAction.UserChoice(),
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
        mockkObject(BrandLogoViewModel)

        every { CallActionsViewModel.provideFactory(any()) } returns mockk {
            every { create(any<KClass<CallActionsViewModel>>(), any()) } returns callViewModel
        }

        every { StreamViewModel.provideFactory(any()) } returns mockk {
            every { create(any<KClass<StreamViewModel>>(), any()) } returns streamViewModel
        }
        every { AudioOutputViewModel.provideFactory(any()) } returns mockk {
            every { create(any<KClass<AudioOutputViewModel>>(), any()) } returns audioOutputViewModel
        }
        every { ScreenShareViewModel.provideFactory(any()) } returns mockk {
            every { create(any<KClass<ScreenShareViewModel>>(), any()) } returns screenShareViewModel
        }
        every { FileShareViewModel.provideFactory(any(), any()) } returns mockk {
            every { create(any<KClass<FileShareViewModel>>(), any()) } returns fileShareViewModel
        }
        every { WhiteboardViewModel.provideFactory(any(), any()) } returns mockk {
            every { create(any<KClass<WhiteboardViewModel>>(), any()) } returns whiteboardViewModel
        }
        every { VirtualBackgroundViewModel.provideFactory(any()) } returns mockk {
            every {
                create(any<KClass<VirtualBackgroundViewModel>>(), any())
            } returns virtualBackgroundViewModel
        }
        every { CallInfoViewModel.provideFactory(any()) } returns mockk {
            every { create(any<KClass<CallInfoViewModel>>(), any()) } returns callInfoViewModel
        }
        every { CallAppBarViewModel.provideFactory(any()) } returns mockk {
            every { create(any<KClass<CallAppBarViewModel>>(), any()) } returns callAppBarViewModel
        }
        every { ParticipantsViewModel.provideFactory(any()) } returns mockk {
            every { create(any<KClass<ParticipantsViewModel>>(), any()) } returns mockk(relaxed = true) {
                every { uiState } returns MutableStateFlow(ParticipantsUiState())
            }
        }
        every { UserMessagesViewModel.provideFactory(any(), any()) } returns mockk {
            every { create(any<KClass<UserMessagesViewModel>>(), any()) } returns userMessagesViewModel
        }

        every { BrandLogoViewModel.provideFactory(any()) } returns mockk {
            every { create(any<KClass<BrandLogoViewModel>>(), any()) } returns brandLogoViewModel
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
    fun testSheetActionsOnLargeScreen_sidePanelChatComponentIsDisplayed() {
        var component: ModularComponent? = null
        composeTestRule.setUpVCallScreen(
            configuration = largeScreenConfiguration,
            onSidePanelComponentRequest = { component = it}
        )
        callActionsUiState.value = CallActionsUiState(
            actionList = listOf(ChatAction()).toImmutableList()
        )

        val text = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_chat)
        composeTestRule
            .onNodeWithContentDescription(text, useUnmergedTree = true)
            .assertIsDisplayed()
            .performClick()

        composeTestRule.onNodeWithTag(CallScreenModalSheetTag).assertDoesNotExist()
        assertEquals(ModularComponent.Chat, component)
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
    fun testSheetActionsOnLargeScreen_onSidePanelComponentChangeToFileShare() {
        var component: ModularComponent? = null
        composeTestRule.setUpVCallScreen(
            configuration = largeScreenConfiguration,
            modalSheetComponent = null,
            onSidePanelComponentRequest = { component = it }
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
    fun testSheetActionsOnLargeScreen_onSidePanelComponentChangeToWhiteboard() {
        var component: ModularComponent? = null
        composeTestRule.setUpVCallScreen(
            configuration = largeScreenConfiguration,
            modalSheetComponent = null,
            onSidePanelComponentRequest = { component = it }
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
    fun testRingingAndLargeScreen_sheetDragActionsAreDisplayed() {
        callActionsUiState.value = CallActionsUiState(
            actionList = allActions.toImmutableList(),
            isRinging = true
        )
        composeTestRule.setUpVCallScreen(configuration = largeScreenConfiguration)

        composeTestRule.onNodeWithTag(InputMessageDragHandleTag, useUnmergedTree = true).assertIsDisplayed()
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
    fun testLargeScreenAndNoMoreRingingState_sheetActionsIsCollapsed() {
        val sheetState = CallSheetState(CallSheetValue.Expanded)
        callActionsUiState.value = CallActionsUiState(
            actionList = allActions.toImmutableList(),
            isRinging = true
        )
        composeTestRule.setUpVCallScreen(
            sheetState = sheetState,
            configuration = largeScreenConfiguration
        )

        assertEquals(CallSheetValue.Expanded, sheetState.currentValue)

        callActionsUiState.value = CallActionsUiState(
            actionList = allActions.toImmutableList(),
            isRinging = false
        )
        composeTestRule.waitForIdle()

        assertEquals(CallSheetValue.Collapsed, sheetState.currentValue)
    }

    @Test
    fun testSheetDragActions_hangUp() {
        val actions = allActions.filterNot { it is HangUpAction }.take(CompactScreenMaxActions)
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
        val actions = allActions.filterNot { it is MicAction }.take(CompactScreenMaxActions)
        val micPermission = mockk<PermissionState>(relaxed = true) {
            every { status } returns PermissionStatus.Granted
        }
        composeTestRule.setUpVCallScreen(
            sheetState = CallSheetState(CallSheetValue.Expanded),
            configuration = compactScreenConfiguration,
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
            configuration = compactScreenConfiguration,
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
            configuration = compactScreenConfiguration,
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
            configuration = compactScreenConfiguration,
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
            configuration = compactScreenConfiguration
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
            configuration = compactScreenConfiguration
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
    fun testSheetDragActionsOnLargeScreen_sidePanelChatComponentIsDisplayed() {
        val actions = allActions.filterNot { it is ChatAction }.take(LargeScreenMaxActions)
        var component: ModularComponent? = null
        callActionsUiState.value = CallActionsUiState(
            actionList = (actions + ChatAction()).toImmutableList(),
            isRinging = true
        )
        composeTestRule.setUpVCallScreen(
            sheetState = CallSheetState(CallSheetValue.Expanded),
            configuration = largeScreenConfiguration,
            onSidePanelComponentRequest = { component = it }
        )

        val chatText = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_chat)
        // Check the button contained in the draggable part of the bottom sheet is displayed
        // The first of the list is the button contained in the fixed part of the bottom sheet, but not rendered by the internal adaptive layout.
        composeTestRule
            .onAllNodesWithText(chatText, useUnmergedTree = true)[0]
            .assertIsDisplayed()
            .performClick()

        assertEquals(ModularComponent.Chat, component)
    }

    @Test
    fun testSheetDragActions_screenShareToggleOff() {
        val actions = allActions.filterNot { it is ScreenShareAction }.take(CompactScreenMaxActions)
        composeTestRule.setUpVCallScreen(
            sheetState = CallSheetState(CallSheetValue.Expanded),
            configuration = compactScreenConfiguration
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
            configuration = compactScreenConfiguration,
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
            configuration = compactScreenConfiguration,
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
    fun testSheetDragActionsOnSmallScreen_modalSheetFileShareComponentIsDisplayed() {
        val actions = (allActions - FileShareAction()).take(CompactScreenMaxActions)
        var component: ModularComponent? = null
        composeTestRule.setUpVCallScreen(
            sheetState = CallSheetState(CallSheetValue.Expanded),
            configuration = compactScreenConfiguration,
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
    fun testSheetDragActionsOnLargeScreen_sidePanelFileShareComponentIsDisplayed() {
        val actions = (allActions - FileShareAction()).take(LargeScreenMaxActions)
        var component: ModularComponent? = null
        callActionsUiState.value = CallActionsUiState(
            isRinging = true,
            actionList = (actions + FileShareAction()).toImmutableList()
        )
        composeTestRule.setUpVCallScreen(
            sheetState = CallSheetState(CallSheetValue.Expanded),
            configuration = largeScreenConfiguration,
            onSidePanelComponentRequest = { component = it }
        )

        val fileShareText =
            composeTestRule.activity.getString(R.string.kaleyra_call_sheet_file_share)
        // Check the button contained in the draggable part of the bottom sheet is displayed
        // The first of the list is the button contained in the fixed part of the bottom sheet, but not rendered by the internal adaptive layout.
        composeTestRule
            .onAllNodesWithContentDescription(fileShareText, useUnmergedTree = true)[0]
            .assertIsDisplayed()
            .performClick()

        assertEquals(ModularComponent.FileShare, component)
    }

    @Test
    fun testSheetDragActionsOnSmallScreen_modalSheetWhiteboardComponentIsDisplayed() {
        val actions = allActions.filterNot { it is WhiteboardAction }.take(CompactScreenMaxActions)
        var component: ModularComponent? = null
        composeTestRule.setUpVCallScreen(
            sheetState = CallSheetState(CallSheetValue.Expanded),
            configuration = compactScreenConfiguration,
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
    fun testSheetDragActionsOnLargeScreen_sidePanelWhiteboardComponentIsDisplayed() {
        val actions = allActions.filterNot { it is WhiteboardAction }.take(LargeScreenMaxActions)
        var component: ModularComponent? = null
        callActionsUiState.value = CallActionsUiState(
            isRinging = true,
            actionList = (actions + WhiteboardAction()).toImmutableList()
        )
        composeTestRule.setUpVCallScreen(
            sheetState = CallSheetState(CallSheetValue.Expanded),
            configuration = largeScreenConfiguration,
            onSidePanelComponentRequest = { component = it }
        )

        val whiteboardText =
            composeTestRule.activity.getString(R.string.kaleyra_call_sheet_whiteboard)
        // Check the button contained in the draggable part of the bottom sheet is displayed
        // The first of the list is the button contained in the fixed part of the bottom sheet, but not rendered by the internal adaptive layout.
        composeTestRule
            .onAllNodesWithContentDescription(whiteboardText, useUnmergedTree = true)[0]
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
            configuration = compactScreenConfiguration,
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
    fun testSheetPanelActions_flipCamera() {
        val actions = allActions.filterNot { it is FlipCameraAction }.take(LargeScreenMaxActions)
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
        val actions = allActions.filterNot { it is AudioAction }.take(LargeScreenMaxActions)
        var component: ModularComponent? = null
        composeTestRule.setUpVCallScreen(
            configuration = largeScreenConfiguration,
            onModalSheetComponentRequest = { component = it }
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

        assertEquals(ModularComponent.Audio, component)
    }

    @Test
    fun testSheetPanelActions_chatComponentIsDisplayed() {
        val actions = (allActions - ChatAction()).take(LargeScreenMaxActions)
        var component: ModularComponent? = null
        composeTestRule.setUpVCallScreen(
            configuration = largeScreenConfiguration,
            onSidePanelComponentRequest = { component = it }
        )
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

        assertEquals(ModularComponent.Chat, component)
    }

    @Test
    fun testSheetPanelActions_screenShareToggleOff() {
        val actions = (allActions - ScreenShareAction.UserChoice()).take(LargeScreenMaxActions)
        composeTestRule.setUpVCallScreen(
            configuration = largeScreenConfiguration,
        )
        callActionsUiState.value = CallActionsUiState(
            actionList = (actions + ScreenShareAction.UserChoice(isToggled = true)).toImmutableList(),
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
        val actions = (allActions - ScreenShareAction.UserChoice()).take(LargeScreenMaxActions)
        var component: ModularComponent? = null
        composeTestRule.setUpVCallScreen(
            configuration = largeScreenConfiguration,
            onModalSheetComponentRequest = { component = it }
        )
        callActionsUiState.value = CallActionsUiState(
            actionList = (actions + ScreenShareAction.UserChoice()).toImmutableList(),
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

        assertEquals(ModularComponent.ScreenShare, component)
    }

    @Test
    fun testSheetPanelActions_fileShareComponentIsDisplayed() {
        val actions = (allActions - FileShareAction()).take(LargeScreenMaxActions)
        var component: ModularComponent? = null
        composeTestRule.setUpVCallScreen(
            configuration = largeScreenConfiguration,
            onSidePanelComponentRequest = { component = it }
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

        assertEquals(ModularComponent.FileShare, component)
    }

    @Test
    fun testSheetPanelActions_whiteboardComponentIsDisplayed() {
        val actions = (allActions - WhiteboardAction()).take(LargeScreenMaxActions)
        var component: ModularComponent? = null
        composeTestRule.setUpVCallScreen(
            configuration = largeScreenConfiguration,
            onSidePanelComponentRequest = { component = it }
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

        assertEquals(ModularComponent.Whiteboard, component)
    }

    @Test
    fun testSheetPanelActions_virtualBackgroundComponentIsDisplayed() {
        val actions = allActions.filterNot { it is VirtualBackgroundAction }.take(LargeScreenMaxActions)
        var component: ModularComponent? = null
        composeTestRule.setUpVCallScreen(
            configuration = largeScreenConfiguration,
            onModalSheetComponentRequest = { component = it }
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
    fun userClicksMoreParticipantsStream_onModularComponentChangeToParticipants() {
        val streams = (1..MaxFeaturedStreamsCompact + 1).map { index ->
            StreamUi(id = "streamId$index", username = "username$index" )
        }
        streamUiState.value = StreamUiState(streams = streams.toImmutableList())

        var component: ModularComponent? = null
        composeTestRule.setUpVCallScreen(
            configuration = compactScreenConfiguration,
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
    fun userMessageIsNotDisplayedOnSidePanelFileShareComponent() {
        every { userMessagesViewModel.userMessage } returns flowOf(ImmutableList(listOf(RecordingMessage.Started)))
        composeTestRule.setUpVCallScreen(
            configuration = largeScreenConfiguration,
            sidePanelComponent = ModularComponent.FileShare
        )

        val text = composeTestRule.activity.getString(R.string.kaleyra_recording_started_message)
        val fileShareText = composeTestRule.activity.getString(R.string.kaleyra_fileshare)
        composeTestRule
            .onNodeWithText(text, useUnmergedTree = true)
            .onAncestors()[2]
            .onChildAt(2)
            .onChildren()
            .assertAll(hasText(fileShareText).not())
    }

    @Test
    fun userMessageIsNotDisplayedOnSidePanelWhiteboardComponent() {
        every { userMessagesViewModel.userMessage } returns flowOf(ImmutableList(listOf(RecordingMessage.Started)))
        composeTestRule.setUpVCallScreen(
            configuration = largeScreenConfiguration,
            sidePanelComponent = ModularComponent.Whiteboard
        )

        val text = composeTestRule.activity.getString(R.string.kaleyra_recording_started_message)
        val whiteboardText = composeTestRule.activity.getString(R.string.kaleyra_whiteboard)
        composeTestRule
            .onNodeWithText(text, useUnmergedTree = true)
            .onAncestors()[2]
            .onChildAt(0)
            .onChildren()
            .assertAll(hasText(whiteboardText).not())
    }

    @Test
    fun userClicksScreenShareMessagePin_streamPinIsInvoked() {
        every { userMessagesViewModel.userMessage } returns flowOf(ImmutableList(listOf(PinScreenshareMessage("streamId", "username"))))
        composeTestRule.setUpVCallScreen()

        val text = composeTestRule.activity.getString(R.string.kaleyra_stream_screenshare_received, "username")
        val pinText = composeTestRule.activity.getString(R.string.kaleyra_participants_component_pin)
        composeTestRule.onNodeWithText(text, useUnmergedTree = true).assertIsDisplayed()
        composeTestRule
            .onNodeWithText(pinText, useUnmergedTree = true)
            .assertIsDisplayed()
            .performClick()
        verify(exactly = 1) { streamViewModel.pin("streamId", prepend = true, force = true) }
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
        val pinText = composeTestRule.activity.getString(R.string.kaleyra_participants_component_pin)
        composeTestRule.onNodeWithText(text, useUnmergedTree = true).assertIsDisplayed()
        composeTestRule
            .onNodeWithText(pinText, useUnmergedTree = true)
            .assertIsDisplayed()
            .performClick()
        verify(exactly = 1) { streamViewModel.pin("streamId", prepend = true, force = true) }
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
        val pinText = composeTestRule.activity.getString(R.string.kaleyra_participants_component_pin)
        composeTestRule.onNodeWithText(text, useUnmergedTree = true).assertIsDisplayed()
        composeTestRule
            .onNodeWithText(pinText, useUnmergedTree = true)
            .assertIsDisplayed()
            .performClick()
        verify(exactly = 1) { streamViewModel.pin("streamId", prepend = true, force = true) }
    }

    @Test
    fun userClicksOnSheetPanelItem_sheetPanelIsDismissed() {
        val actions = allActions.filterNot { it is ChatAction }.take(LargeScreenMaxActions)
        composeTestRule.setUpVCallScreen(configuration = largeScreenConfiguration)
        callActionsUiState.value = CallActionsUiState(
            actionList = (actions + ChatAction()).toImmutableList()
        )

        val moreText = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_description_more_actions)
        composeTestRule.onNodeWithContentDescription(moreText, useUnmergedTree = true).performClick()

        val audioText = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_chat)
        composeTestRule.onAllNodesWithText(audioText, useUnmergedTree = true)[0].performClick()

        composeTestRule.onNodeWithTag(PanelTestTag).assertDoesNotExist()
    }

    @Test
    fun largeDevice_companyLogoSet_callDisconnected_brandLogoDisplayed() = runTest {
        brandLogoUiState.emit(BrandLogoState(callStateUi = CallStateUi.Disconnected.Companion, logo = companyLogo))
        composeTestRule.setUpVCallScreen(configuration = largeScreenConfiguration)
        composeTestRule.waitForIdle()

        val companyLogo = composeTestRule.activity.getString(R.string.kaleyra_company_logo)
        composeTestRule.onNodeWithContentDescription(companyLogo, useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun largeDevice_companyLogoSet_callEndedNeverConnected_brandLogoDisplayed() = runTest {
        brandLogoUiState.emit(BrandLogoState(callStateUi = CallStateUi.Connecting, logo = companyLogo))
        brandLogoUiState.emit(BrandLogoState(callStateUi = CallStateUi.Disconnected.Ended, logo = companyLogo))

        composeTestRule.setUpVCallScreen(configuration = largeScreenConfiguration)
        composeTestRule.waitForIdle()

        val companyLogo = composeTestRule.activity.getString(R.string.kaleyra_company_logo)
        composeTestRule.onNodeWithContentDescription(companyLogo, useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun largeDevice_companyLogoSet_callDisconnectingNeverConnected_brandLogoDisplayed() = runTest {
        brandLogoUiState.emit(BrandLogoState(callStateUi = CallStateUi.Connecting, logo = companyLogo))
        brandLogoUiState.emit(BrandLogoState(callStateUi = CallStateUi.Disconnecting, logo = companyLogo))

        composeTestRule.setUpVCallScreen(configuration = largeScreenConfiguration)
        composeTestRule.waitForIdle()

        val companyLogo = composeTestRule.activity.getString(R.string.kaleyra_company_logo)
        composeTestRule.onNodeWithContentDescription(companyLogo, useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun largeDevice_companyLogoSet_callConnected_brandLogoDisplayed() = runTest {
        val logo = Logo(light = Uri.parse("https://www.example.com/light.png"), dark = Uri.parse("https://www.example.com/dark.png"))
        brandLogoUiState.emit(BrandLogoState(callStateUi = CallStateUi.Connected, logo = logo))

        composeTestRule.setUpVCallScreen(configuration = largeScreenConfiguration)
        composeTestRule.waitForIdle()

        val companyLogo = composeTestRule.activity.getString(R.string.kaleyra_company_logo)
        composeTestRule.onNodeWithContentDescription(companyLogo, useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun largeDevice_companyLogoSet_callReconnecting_brandLogoDisplayed() = runTest {
        brandLogoUiState.emit(BrandLogoState(callStateUi = CallStateUi.Connected, logo = companyLogo))
        brandLogoUiState.emit(BrandLogoState(callStateUi = CallStateUi.Reconnecting, logo = companyLogo))

        composeTestRule.setUpVCallScreen(configuration = largeScreenConfiguration)
        composeTestRule.waitForIdle()

        val companyLogo = composeTestRule.activity.getString(R.string.kaleyra_company_logo)
        composeTestRule.onNodeWithContentDescription(companyLogo, useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun largeDevice_companyLogoNotSet_brandLogoNotDisplayed() = runTest {
        brandLogoUiState.emit(BrandLogoState())
        composeTestRule.setUpVCallScreen(configuration = largeScreenConfiguration)
        composeTestRule.waitForIdle()

        val companyLogo = composeTestRule.activity.getString(R.string.kaleyra_company_logo)
        composeTestRule.onNodeWithContentDescription(companyLogo).assertDoesNotExist()
    }

    @Test
    fun compactDevice_companyLogoSet_brandLogoDisplayed() = runTest {
        composeTestRule.setUpVCallScreen(configuration = compactScreenConfiguration)
        brandLogoUiState.emit(BrandLogoState(callStateUi = CallStateUi.Disconnected.Companion, logo = companyLogo))
        composeTestRule.waitForIdle()

        val companyLogo = composeTestRule.activity.getString(R.string.kaleyra_company_logo)
        composeTestRule.onNodeWithContentDescription(companyLogo, useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun compactDevice_companyLogoSet_callEndedNeverConnected_brandLogoDisplayed() = runTest {
        brandLogoUiState.emit(BrandLogoState(callStateUi = CallStateUi.Connecting, logo = companyLogo))
        brandLogoUiState.emit(BrandLogoState(callStateUi = CallStateUi.Disconnected.Ended, logo = companyLogo))

        composeTestRule.setUpVCallScreen(configuration = compactScreenConfiguration)
        composeTestRule.waitForIdle()

        val companyLogo = composeTestRule.activity.getString(R.string.kaleyra_company_logo)
        composeTestRule.onNodeWithContentDescription(companyLogo, useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun compactDevice_companyLogoSet_callDisconnectingNeverConnected_brandLogoDisplayed() = runTest {
        brandLogoUiState.emit(BrandLogoState(callStateUi = CallStateUi.Connecting, logo = companyLogo))
        brandLogoUiState.emit(BrandLogoState(callStateUi = CallStateUi.Disconnecting, logo = companyLogo))

        composeTestRule.setUpVCallScreen(configuration = compactScreenConfiguration)
        composeTestRule.waitForIdle()

        val companyLogo = composeTestRule.activity.getString(R.string.kaleyra_company_logo)
        composeTestRule.onNodeWithContentDescription(companyLogo, useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun compactDevice_companyLogoSet_callConnected_brandLogoDisplayed() = runTest {
        brandLogoUiState.emit(BrandLogoState(callStateUi = CallStateUi.Connected, logo = companyLogo))

        composeTestRule.setUpVCallScreen(configuration = compactScreenConfiguration)
        composeTestRule.waitForIdle()

        val companyLogo = composeTestRule.activity.getString(R.string.kaleyra_company_logo)
        composeTestRule.onNodeWithContentDescription(companyLogo, useUnmergedTree = true).assertIsNotDisplayed()
    }

    @Test
    fun compactDevice_companyLogoSet_callReconnecting_brandLogoDisplayed() = runTest {
        brandLogoUiState.emit(BrandLogoState(callStateUi = CallStateUi.Connected, logo = companyLogo))
        brandLogoUiState.emit(BrandLogoState(callStateUi = CallStateUi.Reconnecting, logo = companyLogo))

        composeTestRule.setUpVCallScreen(configuration = compactScreenConfiguration)
        composeTestRule.waitForIdle()

        val companyLogo = composeTestRule.activity.getString(R.string.kaleyra_company_logo)
        composeTestRule.onNodeWithContentDescription(companyLogo, useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun compactDevice_companyLogoNotSet_brandLogoNotDisplayed() = runTest {
        brandLogoUiState.emit(BrandLogoState())
        composeTestRule.setUpVCallScreen(configuration = compactScreenConfiguration)
        composeTestRule.waitForIdle()

        val companyLogo = composeTestRule.activity.getString(R.string.kaleyra_company_logo)
        composeTestRule.onNodeWithContentDescription(companyLogo).assertDoesNotExist()
    }

    private fun AndroidComposeTestRule<ActivityScenarioRule<ComponentActivity>, ComponentActivity>.setUpVCallScreen(
        configuration: Configuration = compactScreenConfiguration,
        sheetState: CallSheetState = CallSheetState(),
        onChangeSheetState: (Boolean) -> Unit = {},
        selectedStreamId: String? = null,
        onStreamSelected: (String?) -> Unit = {},
        modalSheetComponent: ModularComponent? = null,
        sidePanelComponent: ModularComponent? = null,
        onModalSheetComponentRequest: (ModularComponent?) -> Unit = { },
        onSidePanelComponentRequest: (ModularComponent?) -> Unit = { },
        onModularComponentDisplayed: (ModularComponent?) -> Unit = { },
        onAskInputPermissions: (Boolean) -> Unit = {},
        onBackPressed: () -> Unit = { },
        inputPermissions: InputPermissions = InputPermissions()
    ) {
        setContent {
            VCallScreen(
                windowSizeClass = WindowSizeClassUtil.currentWindowAdaptiveInfo(configuration),
                sheetState = sheetState,
                modalSheetComponent = modalSheetComponent,
                sidePanelComponent = sidePanelComponent,
                onChangeSheetState = onChangeSheetState,
                selectedStreamId = selectedStreamId,
                onStreamSelected = onStreamSelected,
                inputPermissions = inputPermissions,
                modalSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                onModalSheetComponentRequest = onModalSheetComponentRequest,
                onSidePanelComponentRequest = onSidePanelComponentRequest,
                onModularComponentDisplayed = onModularComponentDisplayed,
                onAskInputPermissions = onAskInputPermissions,
                onBackPressed = onBackPressed,
                onChatDeleted = {},
                onChatCreationFailed = {}
            )
        }
    }
}
