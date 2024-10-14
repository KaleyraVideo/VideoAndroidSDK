package com.kaleyra.video_sdk.ui.call.screen

import androidx.activity.ComponentActivity
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.call.audiooutput.model.AudioOutputUiState
import com.kaleyra.video_sdk.call.audiooutput.viewmodel.AudioOutputViewModel
import com.kaleyra.video_sdk.call.fileshare.model.FileShareUiState
import com.kaleyra.video_sdk.call.fileshare.viewmodel.FileShareViewModel
import com.kaleyra.video_sdk.call.participants.model.ParticipantsUiState
import com.kaleyra.video_sdk.call.participants.viewmodel.ParticipantsViewModel
import com.kaleyra.video_sdk.call.screen.view.CallScreenModalSheet
import com.kaleyra.video_sdk.call.screen.model.ModularComponent
import com.kaleyra.video_sdk.call.screenshare.model.ScreenShareUiState
import com.kaleyra.video_sdk.call.screenshare.viewmodel.ScreenShareViewModel
import com.kaleyra.video_sdk.call.stream.model.StreamUiState
import com.kaleyra.video_sdk.call.stream.viewmodel.StreamViewModel
import com.kaleyra.video_sdk.call.virtualbackground.model.VirtualBackgroundUiState
import com.kaleyra.video_sdk.call.virtualbackground.viewmodel.VirtualBackgroundViewModel
import com.kaleyra.video_sdk.call.whiteboard.model.WhiteboardUiState
import com.kaleyra.video_sdk.call.whiteboard.viewmodel.WhiteboardViewModel
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList
import com.kaleyra.video_sdk.common.usermessages.model.PinScreenshareMessage
import com.kaleyra.video_sdk.common.usermessages.model.StackedSnackbarUiState
import com.kaleyra.video_sdk.common.usermessages.viewmodel.UserMessagesViewModel
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.reflect.KClass

@RunWith(RobolectricTestRunner::class)
@OptIn(ExperimentalMaterial3Api::class)
class CallScreenModalSheetTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Before
    fun setUp() {
        mockkObject(AudioOutputViewModel)
        mockkObject(ScreenShareViewModel)
        mockkObject(FileShareViewModel)
        mockkObject(WhiteboardViewModel)
        mockkObject(VirtualBackgroundViewModel)
        mockkObject(ParticipantsViewModel)
        mockkObject(StreamViewModel)
        mockkObject(UserMessagesViewModel)

        every { AudioOutputViewModel.provideFactory(any()) } returns mockk {
            every { create(any<KClass<AudioOutputViewModel>>(), any()) } returns mockk<AudioOutputViewModel>(relaxed = true) {
                every { uiState } returns MutableStateFlow(AudioOutputUiState())
            }
        }
        every { ScreenShareViewModel.provideFactory(any()) } returns mockk {
            every { create(any<KClass<ScreenShareViewModel>>(), any()) } returns mockk<ScreenShareViewModel>(relaxed = true) {
                every { uiState } returns MutableStateFlow(ScreenShareUiState())
            }
        }
        every { FileShareViewModel.provideFactory(any(), any()) } returns mockk {
            every { create(any<KClass<FileShareViewModel>>(), any()) } returns mockk<FileShareViewModel>(relaxed = true) {
                every { uiState } returns MutableStateFlow(FileShareUiState())
            }
        }
        every { WhiteboardViewModel.provideFactory(any(), any()) } returns mockk {
            every { create(any<KClass<WhiteboardViewModel>>(), any()) } returns mockk<WhiteboardViewModel>(relaxed = true) {
                every { uiState } returns MutableStateFlow(WhiteboardUiState())
            }
        }
        every { VirtualBackgroundViewModel.provideFactory(any()) } returns mockk {
            every { create(any<KClass<VirtualBackgroundViewModel>>(), any()) } returns mockk<VirtualBackgroundViewModel>(relaxed = true) {
                every { uiState } returns MutableStateFlow(VirtualBackgroundUiState())
            }
        }
        every { ParticipantsViewModel.provideFactory(any()) } returns mockk {
            every { create(any<KClass<ParticipantsViewModel>>(), any()) } returns mockk<ParticipantsViewModel>(relaxed = true) {
                every { uiState } returns MutableStateFlow(ParticipantsUiState())
            }
        }
        every { StreamViewModel.provideFactory(any()) } returns mockk {
            every { create(any<KClass<StreamViewModel>>(), any()) } returns mockk<StreamViewModel>(relaxed = true) {
                every { uiState } returns MutableStateFlow(StreamUiState())
            }
        }
        every { UserMessagesViewModel.provideFactory(any(), any()) } returns mockk {
            every { create(any<KClass<UserMessagesViewModel>>(), any()) } returns mockk<UserMessagesViewModel>(relaxed = true) {
                every { uiState } returns MutableStateFlow(StackedSnackbarUiState())
                every { userMessage } returns flowOf(ImmutableList(listOf(PinScreenshareMessage("streamId", "username"))))
            }
        }
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun audioSheetComponent_audioComponentIsDisplayed() {
        var componentDisplayed: ModularComponent? = null
        composeTestRule.setContent {
            CallScreenModalSheet(
                modularComponent = ModularComponent.Audio,
                sheetState = rememberModalBottomSheetState(),
                onRequestDismiss = {},
                onAskInputPermissions = {},
                onUserMessageActionClick = {},
                onComponentDisplayed = { componentDisplayed = it }
            )
        }
        val componentTitle = composeTestRule.activity.getString(R.string.kaleyra_audio_route_title)
        composeTestRule.onNodeWithText(componentTitle).assertIsDisplayed()
        assertEquals(componentDisplayed, ModularComponent.Audio)
    }

    @Test
    fun screenShareSheetComponent_screenShareComponentIsDisplayed() {
        var componentDisplayed: ModularComponent? = null
        composeTestRule.setContent {
            CallScreenModalSheet(
                modularComponent = ModularComponent.ScreenShare,
                sheetState = rememberModalBottomSheetState(),
                onRequestDismiss = {},
                onAskInputPermissions = {},
                onUserMessageActionClick = {},
                onComponentDisplayed = { componentDisplayed = it }
            )
        }
        val componentTitle = composeTestRule.activity.getString(R.string.kaleyra_screenshare_picker_title)
        composeTestRule.onNodeWithText(componentTitle).assertIsDisplayed()
        assertEquals(componentDisplayed, ModularComponent.ScreenShare)
    }

    @Test
    fun fileShareSheetComponent_fileShareComponentIsDisplayed() {
        var componentDisplayed: ModularComponent? = null
        composeTestRule.setContent {
            CallScreenModalSheet(
                modularComponent = ModularComponent.FileShare,
                sheetState = rememberModalBottomSheetState(),
                onRequestDismiss = {},
                onAskInputPermissions = {},
                onUserMessageActionClick = {},
                onComponentDisplayed = { componentDisplayed = it }
            )
        }
        val componentTitle = composeTestRule.activity.getString(R.string.kaleyra_fileshare)
        composeTestRule.onNodeWithText(componentTitle).assertIsDisplayed()
        assertEquals(componentDisplayed, ModularComponent.FileShare)
    }

    @Test
    fun whiteboardSheetComponent_whiteboardComponentIsDisplayed() {
        var componentDisplayed: ModularComponent? = null
        composeTestRule.setContent {
            CallScreenModalSheet(
                modularComponent = ModularComponent.Whiteboard,
                sheetState = rememberModalBottomSheetState(),
                onRequestDismiss = {},
                onAskInputPermissions = {},
                onUserMessageActionClick = {},
                onComponentDisplayed = { componentDisplayed = it }
            )
        }
        val componentTitle = composeTestRule.activity.getString(R.string.kaleyra_whiteboard)
        composeTestRule.onNodeWithText(componentTitle).assertIsDisplayed()
        assertEquals(componentDisplayed, ModularComponent.Whiteboard)
    }

    @Test
    fun virtualBackgroundSheetComponent_virtualBackgroundComponentIsDisplayed() {
        var componentDisplayed: ModularComponent? = null
        composeTestRule.setContent {
            CallScreenModalSheet(
                modularComponent = ModularComponent.VirtualBackground,
                sheetState = rememberModalBottomSheetState(),
                onRequestDismiss = {},
                onAskInputPermissions = {},
                onUserMessageActionClick = {},
                onComponentDisplayed = { componentDisplayed = it }
            )
        }
        val componentTitle = composeTestRule.activity.getString(R.string.kaleyra_virtual_background_picker_title)
        composeTestRule.onNodeWithText(componentTitle).assertIsDisplayed()
        assertEquals(componentDisplayed, ModularComponent.VirtualBackground)
    }

    @Test
    fun participantsSheetComponent_participantsComponentIsDisplayed() {
        var componentDisplayed: ModularComponent? = null
        composeTestRule.setContent {
            CallScreenModalSheet(
                modularComponent = ModularComponent.Participants,
                sheetState = rememberModalBottomSheetState(),
                onRequestDismiss = {},
                onAskInputPermissions = {},
                onUserMessageActionClick = {},
                onComponentDisplayed = { componentDisplayed = it }
            )
        }
        val text = composeTestRule.activity.getString(R.string.kaleyra_participants_component_change_layout)
        composeTestRule.onNodeWithText(text).assertIsDisplayed()
        assertEquals(componentDisplayed, ModularComponent.Participants)
    }

    @Test
    fun audioComponentDisplayed_closeComponentDismissesSheet() {
        var dismissed = false
        var sheetState: SheetState? = null
        composeTestRule.setContent {
            sheetState = rememberModalBottomSheetState()
            CallScreenModalSheet(
                modularComponent = ModularComponent.Audio,
                sheetState = sheetState!!,
                onRequestDismiss = { dismissed = true },
                onAskInputPermissions = {},
                onUserMessageActionClick = {}
            )
        }
        val componentTitle = composeTestRule.activity.getString(R.string.kaleyra_audio_route_title)
        val closeText = composeTestRule.activity.getString(R.string.kaleyra_close)
        composeTestRule.onNodeWithText(componentTitle).assertIsDisplayed()
        composeTestRule
            .onNodeWithContentDescription(closeText)
            .assertIsDisplayed()
            .performClick()
        composeTestRule.waitForIdle()
        assertEquals(true, dismissed)
        assertEquals(SheetValue.Hidden, sheetState!!.currentValue)
    }

    @Test
    fun screenShareComponentDisplayed_closeComponentDismissesSheet() {
        var dismissed = false
        var sheetState: SheetState? = null
        composeTestRule.setContent {
            sheetState = rememberModalBottomSheetState()
            CallScreenModalSheet(
                modularComponent = ModularComponent.ScreenShare,
                sheetState = sheetState!!,
                onRequestDismiss = { dismissed = true },
                onAskInputPermissions = {},
                onUserMessageActionClick = {}
            )
        }
        val componentTitle = composeTestRule.activity.getString(R.string.kaleyra_screenshare_picker_title)
        val closeText = composeTestRule.activity.getString(R.string.kaleyra_close)
        composeTestRule.onNodeWithText(componentTitle).assertIsDisplayed()
        composeTestRule
            .onNodeWithContentDescription(closeText)
            .assertIsDisplayed()
            .performClick()
        composeTestRule.waitForIdle()
        assertEquals(true, dismissed)
        assertEquals(SheetValue.Hidden, sheetState!!.currentValue)
    }

    @Test
    fun fileShareComponentDisplayed_closeComponentDismissesSheet() {
        var dismissed = false
        var sheetState: SheetState? = null
        composeTestRule.setContent {
            sheetState = rememberModalBottomSheetState()
            CallScreenModalSheet(
                modularComponent = ModularComponent.FileShare,
                sheetState = sheetState!!,
                onRequestDismiss = { dismissed = true },
                onAskInputPermissions = {},
                onUserMessageActionClick = {}
            )
        }
        val componentTitle = composeTestRule.activity.getString(R.string.kaleyra_fileshare)
        val closeText = composeTestRule.activity.getString(R.string.kaleyra_close)
        composeTestRule.onNodeWithText(componentTitle).assertIsDisplayed()
        composeTestRule
            .onNodeWithContentDescription(closeText)
            .assertIsDisplayed()
            .performClick()
        composeTestRule.waitForIdle()
        assertEquals(true, dismissed)
        assertEquals(SheetValue.Hidden, sheetState!!.currentValue)
    }

    @Test
    fun screenShareComponentDisplayed_onAskInputPermissionsInvoked() {
        var arePermissionAsked = false
        composeTestRule.setContent {
            CallScreenModalSheet(
                modularComponent = ModularComponent.ScreenShare,
                sheetState = rememberModalBottomSheetState(),
                onRequestDismiss = { },
                onAskInputPermissions = { arePermissionAsked = it },
                onUserMessageActionClick = {}
            )
        }
        val componentTitle = composeTestRule.activity.getString(R.string.kaleyra_screenshare_picker_title)
        val deviceText = composeTestRule.activity.getString(R.string.kaleyra_screenshare_full_device)
        composeTestRule.onNodeWithText(componentTitle).assertIsDisplayed()
        composeTestRule.onNodeWithText(deviceText).performClick()
        assertEquals(true, arePermissionAsked)
    }

    @Test
    fun whiteboardComponentDisplayed_closeComponentDismissesSheet() {
        var dismissed = false
        var sheetState: SheetState? = null
        composeTestRule.setContent {
            sheetState = rememberModalBottomSheetState()
            CallScreenModalSheet(
                modularComponent = ModularComponent.Whiteboard,
                sheetState = sheetState!!,
                onRequestDismiss = { dismissed = true },
                onAskInputPermissions = {},
                onUserMessageActionClick = {}
            )
        }
        val componentTitle = composeTestRule.activity.getString(R.string.kaleyra_whiteboard)
        val closeText = composeTestRule.activity.getString(R.string.kaleyra_close)
        composeTestRule.onNodeWithText(componentTitle).assertIsDisplayed()
        composeTestRule
            .onNodeWithContentDescription(closeText)
            .assertIsDisplayed()
            .performClick()
        composeTestRule.waitForIdle()
        assertEquals(true, dismissed)
        assertEquals(SheetValue.Hidden, sheetState!!.currentValue)
    }

    @Test
    fun virtualBackgroundComponentDisplayed_closeComponentDismissesSheet() {
        var dismissed = false
        var sheetState: SheetState? = null
        composeTestRule.setContent {
            sheetState = rememberModalBottomSheetState()
            CallScreenModalSheet(
                modularComponent = ModularComponent.VirtualBackground,
                sheetState = sheetState!!,
                onRequestDismiss = { dismissed = true },
                onAskInputPermissions = {},
                onUserMessageActionClick = {}
            )
        }
        val componentTitle = composeTestRule.activity.getString(R.string.kaleyra_virtual_background_picker_title)
        val closeText = composeTestRule.activity.getString(R.string.kaleyra_close)
        composeTestRule.onNodeWithText(componentTitle).assertIsDisplayed()
        composeTestRule
            .onNodeWithContentDescription(closeText)
            .assertIsDisplayed()
            .performClick()
        composeTestRule.waitForIdle()
        assertEquals(true, dismissed)
        assertEquals(SheetValue.Hidden, sheetState!!.currentValue)
    }

    @Test
    fun participantsComponentDisplayed_closeComponentDismissesSheet() {
        var dismissed = false
        var sheetState: SheetState? = null
        composeTestRule.setContent {
            sheetState = rememberModalBottomSheetState()
            CallScreenModalSheet(
                modularComponent = ModularComponent.Participants,
                sheetState = sheetState!!,
                onRequestDismiss = { dismissed = true },
                onAskInputPermissions = {},
                onUserMessageActionClick = {}
            )
        }
        val componentTitle = composeTestRule.activity.getString(R.string.kaleyra_participants_component_change_layout)
        val closeText = composeTestRule.activity.getString(R.string.kaleyra_close)
        composeTestRule.onNodeWithText(componentTitle).assertIsDisplayed()
        composeTestRule
            .onNodeWithContentDescription(closeText)
            .assertIsDisplayed()
            .performClick()
        composeTestRule.waitForIdle()
        assertEquals(true, dismissed)
        assertEquals(SheetValue.Hidden, sheetState!!.currentValue)
    }

    @Test
    fun lifecycleStarted_dismissIsNotPerformed() {
        val lifecycleOwner = mockk<LifecycleOwner>(relaxed = true) {
            every { lifecycle.currentState } returns Lifecycle.State.STARTED
        }
        var dismissed = false
        var sheetState: SheetState? = null
        composeTestRule.setContent {
            sheetState = rememberModalBottomSheetState()
            CallScreenModalSheet(
                modularComponent = ModularComponent.VirtualBackground,
                sheetState = sheetState!!,
                onRequestDismiss = { dismissed = true },
                onUserMessageActionClick = {},
                onAskInputPermissions = {},
                lifecycleOwner = lifecycleOwner
            )
        }
        val componentTitle = composeTestRule.activity.getString(R.string.kaleyra_virtual_background_picker_title)
        val closeText = composeTestRule.activity.getString(R.string.kaleyra_close)
        composeTestRule.onNodeWithText(componentTitle).assertIsDisplayed()
        composeTestRule
            .onNodeWithContentDescription(closeText)
            .assertIsDisplayed()
            .performClick()
        composeTestRule.waitForIdle()
        assertEquals(false, dismissed)
        assertEquals(SheetValue.Expanded, sheetState!!.currentValue)
    }

    @Test
    fun whiteboardPinStreamMessage_onUserMessageActionClickInvoked() {
        var clicked = false
        composeTestRule.setContent {
            CallScreenModalSheet(
                modularComponent = ModularComponent.Whiteboard,
                sheetState = rememberModalBottomSheetState(),
                onRequestDismiss = { },
                onAskInputPermissions = {},
                onUserMessageActionClick = { clicked = true }
            )
        }
        val componentTitle = composeTestRule.activity.getString(R.string.kaleyra_whiteboard)
        val pinText = composeTestRule.activity.getString(R.string.kaleyra_participants_component_pin)
        composeTestRule.onNodeWithText(componentTitle).assertIsDisplayed()
        composeTestRule
            .onNodeWithText(pinText)
            .assertIsDisplayed()
            .performClick()
        composeTestRule.waitForIdle()
        assertEquals(true, clicked)
    }

    @Test
    fun fileSharePinStreamMessage_onUserMessageActionClickInvoked() {
        var clicked = false
        composeTestRule.setContent {
            CallScreenModalSheet(
                modularComponent = ModularComponent.FileShare,
                sheetState = rememberModalBottomSheetState(),
                onRequestDismiss = { },
                onAskInputPermissions = {},
                onUserMessageActionClick = { clicked = true }
            )
        }
        val componentTitle = composeTestRule.activity.getString(R.string.kaleyra_fileshare)
        val pinText = composeTestRule.activity.getString(R.string.kaleyra_participants_component_pin)
        composeTestRule.onNodeWithText(componentTitle).assertIsDisplayed()
        composeTestRule
            .onNodeWithText(pinText)
            .assertIsDisplayed()
            .performClick()
        composeTestRule.waitForIdle()
        assertEquals(true, clicked)
    }

    @Test
    fun audioComponent_dragHandleIsDisplayed() {
        val drag = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_drag_description)
        composeTestRule.setContent {
            CallScreenModalSheet(
                modularComponent = ModularComponent.Audio,
                sheetState = rememberModalBottomSheetState(),
                onRequestDismiss = { },
                onAskInputPermissions = { },
                onUserMessageActionClick = { }
            )
        }

        composeTestRule.onNodeWithContentDescription(drag).assertIsDisplayed()
    }

    @Test
    fun screenShareComponent_dragHandleIsDisplayed() {
        val drag = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_drag_description)
        composeTestRule.setContent {
            CallScreenModalSheet(
                modularComponent = ModularComponent.ScreenShare,
                sheetState = rememberModalBottomSheetState(),
                onRequestDismiss = { },
                onAskInputPermissions = { },
                onUserMessageActionClick = { }
            )
        }

        composeTestRule.onNodeWithContentDescription(drag).assertIsDisplayed()
    }

    @Test
    fun fileShareComponent_dragHandleDoesNotExists() {
        val drag = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_drag_description)
        composeTestRule.setContent {
            CallScreenModalSheet(
                modularComponent = ModularComponent.FileShare,
                sheetState = rememberModalBottomSheetState(),
                onRequestDismiss = { },
                onAskInputPermissions = { },
                onUserMessageActionClick = { }
            )
        }

        composeTestRule.onNodeWithContentDescription(drag).assertDoesNotExist()
    }

    @Test
    fun whiteboardComponent_dragHandleDoesNotExists() {
        val drag = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_drag_description)
        composeTestRule.setContent {
            CallScreenModalSheet(
                modularComponent = ModularComponent.Whiteboard,
                sheetState = rememberModalBottomSheetState(),
                onRequestDismiss = { },
                onAskInputPermissions = { },
                onUserMessageActionClick = { }
            )
        }

        composeTestRule.onNodeWithContentDescription(drag).assertDoesNotExist()
    }

    @Test
    fun virtualBackgroundComponent_dragHandleIsDisplayed() {
        val drag = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_drag_description)
        composeTestRule.setContent {
            CallScreenModalSheet(
                modularComponent = ModularComponent.VirtualBackground,
                sheetState = rememberModalBottomSheetState(),
                onRequestDismiss = { },
                onAskInputPermissions = { },
                onUserMessageActionClick = { }
            )
        }

        composeTestRule.onNodeWithContentDescription(drag).assertIsDisplayed()
    }

    @Test
    fun participantsComponent_dragHandleDoesNotExists() {
        val drag = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_drag_description)
        composeTestRule.setContent {
            CallScreenModalSheet(
                modularComponent = ModularComponent.Participants,
                sheetState = rememberModalBottomSheetState(),
                onRequestDismiss = { },
                onAskInputPermissions = { },
                onUserMessageActionClick = { }
            )
        }

        composeTestRule.onNodeWithContentDescription(drag).assertDoesNotExist()
    }
}
