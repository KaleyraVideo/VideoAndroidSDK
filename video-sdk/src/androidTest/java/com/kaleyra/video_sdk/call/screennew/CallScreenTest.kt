package com.kaleyra.video_sdk.call.screennew

import android.content.res.Configuration
import androidx.activity.ComponentActivity
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
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
import com.kaleyra.video_sdk.call.screennew.model.MainUiState
import com.kaleyra.video_sdk.call.screenshare.model.ScreenShareUiState
import com.kaleyra.video_sdk.call.screenshare.viewmodel.ScreenShareViewModel
import com.kaleyra.video_sdk.call.streamnew.model.StreamUiState
import com.kaleyra.video_sdk.call.streamnew.model.core.StreamUi
import com.kaleyra.video_sdk.call.streamnew.viewmodel.StreamViewModel
import com.kaleyra.video_sdk.call.virtualbackground.model.VirtualBackgroundUiState
import com.kaleyra.video_sdk.call.virtualbackground.viewmodel.VirtualBackgroundViewModel
import com.kaleyra.video_sdk.call.whiteboard.model.WhiteboardRequest
import com.kaleyra.video_sdk.call.whiteboard.model.WhiteboardUiState
import com.kaleyra.video_sdk.call.whiteboard.viewmodel.WhiteboardViewModel
import com.kaleyra.video_sdk.common.immutablecollections.toImmutableList
import com.kaleyra.video_sdk.common.usermessages.model.StackedSnackbarUiState
import com.kaleyra.video_sdk.common.usermessages.model.WhiteboardHideRequestMessage
import com.kaleyra.video_sdk.common.usermessages.model.WhiteboardShowRequestMessage
import com.kaleyra.video_sdk.common.usermessages.provider.CallUserMessagesProvider
import com.kaleyra.video_sdk.common.usermessages.viewmodel.UserMessagesViewModel
import com.kaleyra.video_sdk.findBackButton
import com.kaleyra.video_sdk.pressBack
import com.kaleyra.video_sdk.utils.WindowSizeClassUtil
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import io.mockk.verify
import junit.framework.TestCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class CallScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val compactScreenConfiguration = Configuration().apply {
        screenWidthDp = 480
        screenHeightDp = 600
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
        every { UserMessagesViewModel.provideFactory(any(), any()) } returns mockk {
            every { create<UserMessagesViewModel>(any(), any()) } returns userMessagesViewModel
        }
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun callBottomSheetExpanded_userPerformsBack_sheetIsCollapsed() {
        callActionsUiState.value = CallActionsUiState(
            actionList = allActions.toImmutableList()
        )
        val sheetState = CallSheetState(CallSheetValue.Expanded)
        composeTestRule.setUpCallScreen(callSheetState = sheetState)

        composeTestRule.pressBack()
        composeTestRule.waitForIdle()

        assertEquals(CallSheetValue.Collapsed, sheetState.currentValue)
    }

    @Test
    fun streamMenuDisplayed_userPerformsBack_streamMenuIsDismissed() {
        val streams = StreamUi(id = "streamId", username = "username")
        streamUiState.value = StreamUiState(
            streams = listOf(streams).toImmutableList()
        )
        composeTestRule.setUpCallScreen()

        composeTestRule
            .onNodeWithText("username", useUnmergedTree = true)
            .assertIsDisplayed()
            .performClick()

        composeTestRule.onNodeWithTag(StreamMenuContentTestTag).assertIsDisplayed()

        composeTestRule.pressBack()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag(StreamMenuContentTestTag).assertDoesNotExist()
    }

    @Test
    fun callBottomSheetCollapsed_userPerformsBack_activityIsFinishing() {
        val sheetState = CallSheetState(CallSheetValue.Collapsed)
        composeTestRule.setUpCallScreen(callSheetState = sheetState)

        composeTestRule.pressBack()
        composeTestRule.waitForIdle()

        assertEquals(true, composeTestRule.activity.isFinishing)
    }

    @Test
    fun callStateEnded_userPerformsBack_onCallEndedBackInvoked() {
        var isInvoked = false
        composeTestRule.setUpCallScreen(
            uiState = MainUiState(isCallEnded = true),
            onCallEndedBack = { isInvoked = true }
        )

        composeTestRule.pressBack()
        composeTestRule.waitForIdle()

        assertEquals(true, isInvoked)
    }

    @Test
    fun userClicksFileShareAction_fileShareComponentDisplayed() {
        callActionsUiState.value = CallActionsUiState(
            actionList = listOf(FileShareAction()).toImmutableList()
        )
        composeTestRule.setUpCallScreen(configuration = compactScreenConfiguration)

        val fileShareText = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_file_share)
        // Check the button contained in the draggable part of the bottom sheet is displayed
        // The first of the list is the button contained in the fixed part of the bottom sheet, but not rendered by the internal adaptive layout.
        composeTestRule
            .onAllNodesWithContentDescription(fileShareText, useUnmergedTree = true)[0]
            .assertIsDisplayed()
            .performClick()

        val text = composeTestRule.activity.getString(R.string.kaleyra_fileshare)
        composeTestRule.onNodeWithText(text).assertIsDisplayed()
    }

    @Test
    fun userClicksAudioAction_audioComponentDisplayed() {
        callActionsUiState.value = CallActionsUiState(
            actionList = listOf(AudioAction()).toImmutableList()
        )
        composeTestRule.setUpCallScreen(configuration = compactScreenConfiguration)

        val audioText = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_audio)
        // Check the button contained in the draggable part of the bottom sheet is displayed
        // The first of the list is the button contained in the fixed part of the bottom sheet, but not rendered by the internal adaptive layout.
        composeTestRule
            .onAllNodesWithContentDescription(audioText, useUnmergedTree = true)[0]
            .assertIsDisplayed()
            .performClick()

        val text = composeTestRule.activity.getString(R.string.kaleyra_audio_route_title)
        composeTestRule.onNodeWithText(text).assertIsDisplayed()
    }

    @Test
    fun userClicksScreenShareAction_screenShareComponentDisplayed() {
        callActionsUiState.value = CallActionsUiState(
            actionList = listOf(ScreenShareAction()).toImmutableList()
        )
        composeTestRule.setUpCallScreen(configuration = compactScreenConfiguration)

        val screenShareText = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_screen_share)
        // Check the button contained in the draggable part of the bottom sheet is displayed
        // The first of the list is the button contained in the fixed part of the bottom sheet, but not rendered by the internal adaptive layout.
        composeTestRule
            .onAllNodesWithContentDescription(screenShareText, useUnmergedTree = true)[0]
            .assertIsDisplayed()
            .performClick()

        val text = composeTestRule.activity.getString(R.string.kaleyra_screenshare_picker_title)
        composeTestRule.onNodeWithText(text).assertIsDisplayed()
    }

    @Test
    fun userClicksWhiteboardAction_whiteboardComponentDisplayed() {
        callActionsUiState.value = CallActionsUiState(
            actionList = listOf(WhiteboardAction()).toImmutableList()
        )
        composeTestRule.setUpCallScreen(configuration = compactScreenConfiguration)

        val whiteboardText = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_whiteboard)
        // Check the button contained in the draggable part of the bottom sheet is displayed
        // The first of the list is the button contained in the fixed part of the bottom sheet, but not rendered by the internal adaptive layout.
        composeTestRule
            .onAllNodesWithContentDescription(whiteboardText, useUnmergedTree = true)[0]
            .assertIsDisplayed()
            .performClick()

        val text = composeTestRule.activity.getString(R.string.kaleyra_whiteboard)
        composeTestRule.onNodeWithText(text).assertIsDisplayed()
    }

    @Test
    fun userClicksVirtualBackgroundAction_virtualBackgroundComponentDisplayed() {
        callActionsUiState.value = CallActionsUiState(
            actionList = listOf(VirtualBackgroundAction()).toImmutableList()
        )
        composeTestRule.setUpCallScreen(configuration = compactScreenConfiguration)

        val virtualBgText = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_virtual_background)
        // Check the button contained in the draggable part of the bottom sheet is displayed
        // The first of the list is the button contained in the fixed part of the bottom sheet, but not rendered by the internal adaptive layout.
        composeTestRule
            .onAllNodesWithContentDescription(virtualBgText, useUnmergedTree = true)[0]
            .assertIsDisplayed()
            .performClick()

        val text = composeTestRule.activity.getString(R.string.kaleyra_virtual_background_picker_title)
        composeTestRule.onNodeWithText(text).assertIsDisplayed()
    }

    @Test
    fun pipModeEnabled_pipCallScreenIsDisplayed() {
        composeTestRule.setUpCallScreen(isPipMode = true)
        composeTestRule.onNodeWithTag(PipScreenTestTag).assertIsDisplayed()
    }

    @Test
    fun compactHeight_horizontalCallScreenIsDisplayed() {
        callActionsUiState.update {
            it.copy(actionList = allActions.toImmutableList())
        }
        val configuration = Configuration().apply {
            screenWidthDp = 600
            screenHeightDp = 200
        }
        composeTestRule.setUpCallScreen(configuration = configuration)

        composeTestRule.onNodeWithTag(HCallScreenTestTag).assertIsDisplayed()
    }

    @Test
    fun mediumHeight_horizontalCallScreenIsDisplayed() {
        callActionsUiState.update {
            it.copy(actionList = allActions.toImmutableList())
        }
        val configuration = Configuration().apply {
            screenWidthDp = 600
            screenHeightDp = 480
        }
        composeTestRule.setUpCallScreen(configuration = configuration)

        composeTestRule.onNodeWithTag(VCallScreenTestTag).assertIsDisplayed()
    }

    @Test
    fun selectedStream_streamMenuIsDisplayed() {
        val streams = StreamUi(id = "streamId", username = "username")
        streamUiState.value = StreamUiState(
            streams = listOf(streams).toImmutableList()
        )
        composeTestRule.setUpCallScreen()

        composeTestRule
            .onNodeWithText("username", useUnmergedTree = true)
            .assertIsDisplayed()
            .performClick()

        composeTestRule.onNodeWithTag(StreamMenuContentTestTag).assertIsDisplayed()
    }

    @Test
    fun collapsedCallSheet_userClicksMoreButton_sheetIsExpanded() {
        callActionsUiState.update {
            it.copy(actionList = allActions.toImmutableList())
        }
        val sheetState = CallSheetState()
        composeTestRule.setUpCallScreen(callSheetState = sheetState)

        assertEquals(CallSheetValue.Collapsed, sheetState.currentValue)

        val text = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_description_more_actions)
        composeTestRule.onNodeWithContentDescription(text).performClick()

        composeTestRule.waitForIdle()

        assertEquals(CallSheetValue.Expanded, sheetState.currentValue)
    }

    @Test
    fun expandedCallSheet_userClicksMoreButton_sheetIsCollapsed() {
        callActionsUiState.update {
            it.copy(actionList = allActions.toImmutableList())
        }
        val sheetState = CallSheetState(initialValue = CallSheetValue.Expanded)
        composeTestRule.setUpCallScreen(callSheetState = sheetState)

        assertEquals(CallSheetValue.Expanded, sheetState.currentValue)

        val text = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_description_hide_actions)
        composeTestRule.onNodeWithContentDescription(text).performClick()

        composeTestRule.waitForIdle()

        assertEquals(CallSheetValue.Collapsed, sheetState.currentValue)
    }

    @Test
    fun shouldShowFileShareComponentTrue_fileShareComponentDisplayed() {
        val shouldShowFileShareComponent = mutableStateOf(false)
        composeTestRule.setUpCallScreen(
            shouldShowFileShareComponent = shouldShowFileShareComponent
        )

        val fileShareTitle = composeTestRule.activity.getString(R.string.kaleyra_fileshare)
        composeTestRule.onNodeWithText(fileShareTitle).assertDoesNotExist()

        shouldShowFileShareComponent.value = true

        composeTestRule.onNodeWithText(fileShareTitle).assertIsDisplayed()
    }

    @Test
    fun fileShareComponentIsDisplayed_onFileShareVisibilityInvoked() {
        var isFileShareDisplayed = false
        callActionsUiState.update {
            it.copy(actionList = listOf(FileShareAction()).toImmutableList())
        }
        composeTestRule.setUpCallScreen(
            onFileShareVisibility = { isFileShareDisplayed = it }
        )

        val buttonText = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_file_share)
        composeTestRule
            .onNodeWithContentDescription(buttonText, useUnmergedTree = true)
            .assertIsDisplayed()
            .performClick()

        composeTestRule.waitForIdle()

        assertEquals(true, isFileShareDisplayed)
    }

    @Test
    fun whiteboardComponentIsDisplayed_onWhiteboardVisibilityInvoked() {
        var isWhiteboardDisplayed = false
        callActionsUiState.update {
            it.copy(actionList = listOf(WhiteboardAction()).toImmutableList())
        }
        composeTestRule.setUpCallScreen(
            onWhiteboardVisibility = { isWhiteboardDisplayed = it }
        )

        val buttonText = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_whiteboard)
        composeTestRule
            .onNodeWithContentDescription(buttonText, useUnmergedTree = true)
            .assertIsDisplayed()
            .performClick()

        composeTestRule.waitForIdle()

        assertEquals(true, isWhiteboardDisplayed)
    }

    @Test
    fun userClicksBackButton_onBackPressedInvoked() {
        var isBackPressed = false
        composeTestRule.setUpCallScreen(
            onBackPressed = { isBackPressed = true }
        )

        composeTestRule.findBackButton().performClick()
        assertEquals(true, isBackPressed)
    }

    @Test
    fun showWhiteboardRequestReceived_whiteboardDisplayed() {
        val whiteboardRequest = mutableStateOf(WhiteboardRequest.Show("username"))
        composeTestRule.setUpCallScreen(
            whiteboardRequest = whiteboardRequest
        )

        val text = composeTestRule.activity.getString(R.string.kaleyra_whiteboard)
        composeTestRule.onNodeWithText(text).assertIsDisplayed()
    }

    @Test
    fun showWhiteboardRequestReceived_whiteboardUserMessageDisplayed() = mockkObject(CallUserMessagesProvider) {
        val whiteboardRequest = mutableStateOf(WhiteboardRequest.Show("username"))
        every { CallUserMessagesProvider.sendUserMessage(any()) } returns Unit
        composeTestRule.setUpCallScreen(
            whiteboardRequest = whiteboardRequest
        )
        composeTestRule.waitForIdle()
        verify(exactly = 1) {
            CallUserMessagesProvider.sendUserMessage(withArg<WhiteboardShowRequestMessage> {
                assertEquals("username", it.username)
            })
        }
    }

    @Test
    fun hideWhiteboardRequestReceived_whiteboardNotDisplayed() {
        callActionsUiState.update {
            it.copy(actionList = listOf(WhiteboardAction()).toImmutableList())
        }
        val whiteboardRequest = mutableStateOf<WhiteboardRequest?>(null)
        composeTestRule.setUpCallScreen(
            whiteboardRequest = whiteboardRequest
        )

        val buttonText = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_whiteboard)
        composeTestRule
            .onNodeWithContentDescription(buttonText, useUnmergedTree = true)
            .assertIsDisplayed()
            .performClick()

        val text = composeTestRule.activity.getString(R.string.kaleyra_whiteboard)
        composeTestRule.onNodeWithText(text).assertIsDisplayed()

        whiteboardRequest.value = WhiteboardRequest.Hide("username")

        composeTestRule.onNodeWithText(text).assertDoesNotExist()
    }

    @Test
    fun hideWhiteboardRequestReceived_whiteboardUserMessageDisplayed() = mockkObject(CallUserMessagesProvider) {
        every { CallUserMessagesProvider.sendUserMessage(any()) } returns Unit

        callActionsUiState.update {
            it.copy(actionList = listOf(WhiteboardAction()).toImmutableList())
        }
        val whiteboardRequest = mutableStateOf<WhiteboardRequest?>(null)
        composeTestRule.setUpCallScreen(
            whiteboardRequest = whiteboardRequest
        )

        val buttonText = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_whiteboard)
        composeTestRule
            .onNodeWithContentDescription(buttonText, useUnmergedTree = true)
            .assertIsDisplayed()
            .performClick()

        val text = composeTestRule.activity.getString(R.string.kaleyra_whiteboard)
        composeTestRule.onNodeWithText(text).assertIsDisplayed()

        whiteboardRequest.value = WhiteboardRequest.Hide("username")

        composeTestRule.waitForIdle()

        verify(exactly = 1) {
            CallUserMessagesProvider.sendUserMessage(withArg<WhiteboardHideRequestMessage> {
                assertEquals("username", it.username)
            })
        }
    }

    @Test
    fun testUserClicksDeviceScreenShare_onAskInputPermissionsInvoked() {
        var arePermissionAsked = false
       composeTestRule.setUpCallScreen(
            onAskInputPermissions = { arePermissionAsked = true }
        )
        callActionsUiState.value = CallActionsUiState(
            actionList = listOf(ScreenShareAction()).toImmutableList()
        )

        val screenShareText = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_screen_share)
        composeTestRule.onNodeWithContentDescription(screenShareText, useUnmergedTree = true).performClick()

        composeTestRule.waitForIdle()

        val deviceText = composeTestRule.activity.getString(R.string.kaleyra_screenshare_full_device)
        composeTestRule.onNodeWithText(deviceText).performClick()

        TestCase.assertEquals(true, arePermissionAsked)
    }

    private fun AndroidComposeTestRule<ActivityScenarioRule<ComponentActivity>, ComponentActivity>.setUpCallScreen(
        configuration: Configuration? = null,
        uiState: MainUiState = MainUiState(),
        callSheetState: CallSheetState = CallSheetState(),
        shouldShowFileShareComponent: State<Boolean> = mutableStateOf(false),
        whiteboardRequest: State<WhiteboardRequest?> = mutableStateOf(null),
        onCallEndedBack: () -> Unit = {},
        onBackPressed: () -> Unit = {},
        onAskInputPermissions: (Boolean) -> Unit = {},
        onFileShareVisibility: (Boolean) -> Unit = {},
        onWhiteboardVisibility: (Boolean) -> Unit = {},
        isPipMode: Boolean = false,
    ) {
        setContent {
            CallScreen(
                uiState = uiState,
                windowSizeClass = configuration?.let {
                    WindowSizeClassUtil.currentWindowAdaptiveInfo(it)
                } ?:  WindowSizeClassUtil.currentWindowAdaptiveInfo(),
                shouldShowFileShareComponent = shouldShowFileShareComponent.value,
                whiteboardRequest = whiteboardRequest.value,
                callSheetState = callSheetState,
                onBackPressed = onBackPressed,
                onAskInputPermissions = onAskInputPermissions,
                onCallEndedBack = onCallEndedBack,
                onFileShareVisibility = onFileShareVisibility,
                onWhiteboardVisibility = onWhiteboardVisibility,
                isInPipMode = isPipMode
            )
        }
    }
}