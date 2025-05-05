package com.kaleyra.video_sdk.call.bottomsheet

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.PermissionStatus
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.call.bottomsheet.model.AudioAction
import com.kaleyra.video_sdk.call.bottomsheet.model.CameraAction
import com.kaleyra.video_sdk.call.bottomsheet.model.ChatAction
import com.kaleyra.video_sdk.call.bottomsheet.model.CustomAction
import com.kaleyra.video_sdk.call.bottomsheet.model.CustomCallAction
import com.kaleyra.video_sdk.call.bottomsheet.model.FileShareAction
import com.kaleyra.video_sdk.call.bottomsheet.model.FlipCameraAction
import com.kaleyra.video_sdk.call.bottomsheet.model.HangUpAction
import com.kaleyra.video_sdk.call.bottomsheet.model.InputCallAction
import com.kaleyra.video_sdk.call.bottomsheet.model.MicAction
import com.kaleyra.video_sdk.call.bottomsheet.model.ScreenShareAction
import com.kaleyra.video_sdk.call.bottomsheet.model.VirtualBackgroundAction
import com.kaleyra.video_sdk.call.bottomsheet.model.WhiteboardAction
import com.kaleyra.video_sdk.call.bottomsheet.view.CallSheetItem
import com.kaleyra.video_sdk.call.callactions.view.HangUpActionExtendedWidth
import com.kaleyra.video_sdk.call.callactions.view.HangUpActionWidth
import com.kaleyra.video_sdk.call.screen.model.InputPermissions
import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalPermissionsApi::class)
class CallSheetItemTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun hangUpActionEnabled_actionIsEnabled() {
        val description = composeTestRule.activity.getString(R.string.kaleyra_strings_action_call_end)
        composeTestRule.setContent {
            CallSheetItem(
                callAction = HangUpAction(isEnabled = true),
                label = false,
                extended = false,
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
                onVirtualBackgroundToggle = { }
            )
        }
        composeTestRule.onNodeWithContentDescription(description).assertHasClickAction()
        composeTestRule.onNodeWithContentDescription(description).assertIsEnabled()
    }

    @Test
    fun hangUpActionDisabled_actionIsNotEnabled() {
        val description = composeTestRule.activity.getString(R.string.kaleyra_strings_action_call_end)
        composeTestRule.setContent {
            CallSheetItem(
                callAction = HangUpAction(isEnabled = false),
                label = false,
                extended = false,
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
                onVirtualBackgroundToggle = { }
            )
        }
        composeTestRule.onNodeWithContentDescription(description).assertHasClickAction()
        composeTestRule.onNodeWithContentDescription(description).assertIsNotEnabled()
    }

    @Test
    fun micActionEnabled_actionIsEnabled() {
        val description = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_description_disable_microphone)
        composeTestRule.setContent {
            CallSheetItem(
                callAction = MicAction(isEnabled = true),
                label = false,
                extended = false,
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
                onVirtualBackgroundToggle = { }
            )
        }
        composeTestRule.onNodeWithContentDescription(description).assertHasClickAction()
        composeTestRule.onNodeWithContentDescription(description).assertIsEnabled()
    }

    @Test
    fun micActionDisabled_actionIsNotEnabled() {
        val description = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_description_disable_microphone)
        composeTestRule.setContent {
            CallSheetItem(
                callAction = MicAction(isEnabled = false),
                label = false,
                extended = false,
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
                onVirtualBackgroundToggle = { }
            )
        }
        composeTestRule.onNodeWithContentDescription(description).assertHasClickAction()
        composeTestRule.onNodeWithContentDescription(description).assertIsNotEnabled()
    }

    @Test
    fun cameraActionEnabled_actionIsEnabled() {
        val description = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_description_disable_camera)
        composeTestRule.setContent {
            CallSheetItem(
                callAction = CameraAction(isEnabled = true),
                label = false,
                extended = false,
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
                onVirtualBackgroundToggle = { }
            )
        }
        composeTestRule.onNodeWithContentDescription(description).assertHasClickAction()
        composeTestRule.onNodeWithContentDescription(description).assertIsEnabled()
    }

    @Test
    fun cameraActionDisabled_actionIsNotEnabled() {
        val description = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_description_disable_camera)
        composeTestRule.setContent {
            CallSheetItem(
                callAction = CameraAction(isEnabled = false),
                label = false,
                extended = false,
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
                onVirtualBackgroundToggle = { }
            )
        }
        composeTestRule.onNodeWithContentDescription(description).assertHasClickAction()
        composeTestRule.onNodeWithContentDescription(description).assertIsNotEnabled()
    }

    @Test
    fun flipCameraActionEnabled_actionIsEnabled() {
        val description = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_flip_camera)
        composeTestRule.setContent {
            CallSheetItem(
                callAction = FlipCameraAction(isEnabled = true),
                label = false,
                extended = false,
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
                onVirtualBackgroundToggle = { }
            )
        }
        composeTestRule.onNodeWithContentDescription(description).assertHasClickAction()
        composeTestRule.onNodeWithContentDescription(description).assertIsEnabled()
    }

    @Test
    fun flipCameraActionDisabled_actionIsNotEnabled() {
        val description = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_flip_camera)
        composeTestRule.setContent {
            CallSheetItem(
                callAction = FlipCameraAction(isEnabled = false),
                label = false,
                extended = false,
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
                onVirtualBackgroundToggle = { }
            )
        }
        composeTestRule.onNodeWithContentDescription(description).assertHasClickAction()
        composeTestRule.onNodeWithContentDescription(description).assertIsNotEnabled()
    }

    @Test
    fun audioActionEnabled_actionIsEnabled() {
        val description = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_audio)
        composeTestRule.setContent {
            CallSheetItem(
                callAction = AudioAction(isEnabled = true),
                label = false,
                extended = false,
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
                onVirtualBackgroundToggle = { }
            )
        }
        composeTestRule.onNodeWithContentDescription(description).assertHasClickAction()
        composeTestRule.onNodeWithContentDescription(description).assertIsEnabled()
    }

    @Test
    fun audioActionDisabled_actionIsNotEnabled() {
        val description = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_audio)
        composeTestRule.setContent {
            CallSheetItem(
                callAction = AudioAction(isEnabled = false),
                label = false,
                extended = false,
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
                onVirtualBackgroundToggle = { }
            )
        }
        composeTestRule.onNodeWithContentDescription(description).assertHasClickAction()
        composeTestRule.onNodeWithContentDescription(description).assertIsNotEnabled()
    }

    @Test
    fun chatActionEnabled_actionIsEnabled() {
        val description = composeTestRule.activity.getString(R.string.kaleyra_strings_action_chat)
        composeTestRule.setContent {
            CallSheetItem(
                callAction = ChatAction(isEnabled = true),
                label = false,
                extended = false,
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
                onVirtualBackgroundToggle = { }
            )
        }
        composeTestRule.onNodeWithContentDescription(description).assertHasClickAction()
        composeTestRule.onNodeWithContentDescription(description).assertIsEnabled()
    }

    @Test
    fun chatActionDisabled_actionIsNotEnabled() {
        val description = composeTestRule.activity.getString(R.string.kaleyra_strings_action_chat)
        composeTestRule.setContent {
            CallSheetItem(
                callAction = ChatAction(isEnabled = false),
                label = false,
                extended = false,
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
                onVirtualBackgroundToggle = { }
            )
        }
        composeTestRule.onNodeWithContentDescription(description).assertHasClickAction()
        composeTestRule.onNodeWithContentDescription(description).assertIsNotEnabled()
    }

    @Test
    fun fileShareActionEnabled_actionIsEnabled() {
        val description = composeTestRule.activity.getString(R.string.kaleyra_strings_action_files)
        composeTestRule.setContent {
            CallSheetItem(
                callAction = FileShareAction(isEnabled = true),
                label = false,
                extended = false,
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
                onVirtualBackgroundToggle = { }
            )
        }
        composeTestRule.onNodeWithContentDescription(description).assertHasClickAction()
        composeTestRule.onNodeWithContentDescription(description).assertIsEnabled()
    }

    @Test
    fun fileShareActionDisabled_actionIsNotEnabled() {
        val description = composeTestRule.activity.getString(R.string.kaleyra_strings_action_files)
        composeTestRule.setContent {
            CallSheetItem(
                callAction = FileShareAction(isEnabled = false),
                label = false,
                extended = false,
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
                onVirtualBackgroundToggle = { }
            )
        }
        composeTestRule.onNodeWithContentDescription(description).assertHasClickAction()
        composeTestRule.onNodeWithContentDescription(description).assertIsNotEnabled()
    }

    @Test
    fun screenShareActionEnabled_actionIsEnabled() {
        val description = composeTestRule.activity.getString(R.string.kaleyra_strings_action_start_screen_share)
        composeTestRule.setContent {
            CallSheetItem(
                callAction = ScreenShareAction.UserChoice(isEnabled = true),
                label = false,
                extended = false,
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
                onVirtualBackgroundToggle = { }
            )
        }
        composeTestRule.onNodeWithContentDescription(description).assertHasClickAction()
        composeTestRule.onNodeWithContentDescription(description).assertIsEnabled()
    }

    @Test
    fun screenShareActionDisabled_actionIsNotEnabled() {
        val description = composeTestRule.activity.getString(R.string.kaleyra_strings_action_start_screen_share)
        composeTestRule.setContent {
            CallSheetItem(
                callAction = ScreenShareAction.UserChoice(isEnabled = false),
                label = false,
                extended = false,
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
                onVirtualBackgroundToggle = { }
            )
        }
        composeTestRule.onNodeWithContentDescription(description).assertHasClickAction()
        composeTestRule.onNodeWithContentDescription(description).assertIsNotEnabled()
    }

    @Test
    fun whiteboardActionEnabled_actionIsEnabled() {
        val description = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_whiteboard)
        composeTestRule.setContent {
            CallSheetItem(
                callAction = WhiteboardAction(isEnabled = true),
                label = false,
                extended = false,
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
                onVirtualBackgroundToggle = { }
            )
        }
        composeTestRule.onNodeWithContentDescription(description).assertHasClickAction()
        composeTestRule.onNodeWithContentDescription(description).assertIsEnabled()
    }

    @Test
    fun whiteboardActionDisabled_actionIsNotEnabled() {
        val description = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_whiteboard)
        composeTestRule.setContent {
            CallSheetItem(
                callAction = WhiteboardAction(isEnabled = false),
                label = false,
                extended = false,
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
                onVirtualBackgroundToggle = { }
            )
        }
        composeTestRule.onNodeWithContentDescription(description).assertHasClickAction()
        composeTestRule.onNodeWithContentDescription(description).assertIsNotEnabled()
    }

    @Test
    fun virtualBackgroundActionEnabled_actionIsEnabled() {
        val description = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_virtual_background)
        composeTestRule.setContent {
            CallSheetItem(
                callAction = VirtualBackgroundAction(isEnabled = true),
                label = false,
                extended = false,
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
                onVirtualBackgroundToggle = { }
            )
        }
        composeTestRule.onNodeWithContentDescription(description).assertHasClickAction()
        composeTestRule.onNodeWithContentDescription(description).assertIsEnabled()
    }

    @Test
    fun virtualBackgroundActionDisabled_actionIsNotEnabled() {
        val description = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_virtual_background)
        composeTestRule.setContent {
            CallSheetItem(
                callAction = VirtualBackgroundAction(isEnabled = false),
                label = false,
                extended = false,
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
                onVirtualBackgroundToggle = { }
            )
        }
        composeTestRule.onNodeWithContentDescription(description).assertHasClickAction()
        composeTestRule.onNodeWithContentDescription(description).assertIsNotEnabled()
    }

    @Test
    fun testOnHangUpActionClick() {
        var clicked = false
        val description = composeTestRule.activity.getString(R.string.kaleyra_strings_action_call_end)
        composeTestRule.setContent {
            CallSheetItem(
                callAction = HangUpAction(),
                label = false,
                extended = false,
                onHangUpClick = { clicked = true },
                onMicToggle = { },
                onCameraToggle = { },
                onScreenShareToggle = { },
                onFlipCameraClick = { },
                onAudioClick = { },
                onChatClick = { },
                onFileShareClick = { },
                onWhiteboardClick = { },
                onSignatureClick = {},
                onVirtualBackgroundToggle = { }
            )
        }
        composeTestRule.onNodeWithContentDescription(description).assertHasClickAction()
        composeTestRule.onNodeWithContentDescription(description).performClick()
        assertEquals(true, clicked)
    }

    @Test
    fun testOnMicActionToggled() {
        var clicked = false
        val description = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_description_disable_microphone)
        composeTestRule.setContent {
            CallSheetItem(
                callAction = MicAction(),
                label = false,
                extended = false,
                onHangUpClick = { },
                onMicToggle = { clicked = it },
                onCameraToggle = { },
                onScreenShareToggle = { },
                onFlipCameraClick = { },
                onAudioClick = { },
                onChatClick = { },
                onFileShareClick = { },
                onWhiteboardClick = { },
                onSignatureClick = {},
                onVirtualBackgroundToggle = { }
            )
        }
        composeTestRule.onNodeWithContentDescription(description).assertHasClickAction()
        composeTestRule.onNodeWithContentDescription(description).performClick()
        assertEquals(true, clicked)
    }

    @Test
    fun testOnCameraActionToggled() {
        var clicked = false
        val description = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_description_disable_camera)
        composeTestRule.setContent {
            CallSheetItem(
                callAction = CameraAction(),
                label = false,
                extended = false,
                onHangUpClick = { },
                onMicToggle = { },
                onCameraToggle = { clicked = it },
                onScreenShareToggle = { },
                onFlipCameraClick = { },
                onAudioClick = { },
                onChatClick = { },
                onFileShareClick = { },
                onWhiteboardClick = { },
                onSignatureClick = {},
                onVirtualBackgroundToggle = { }
            )
        }
        composeTestRule.onNodeWithContentDescription(description).assertHasClickAction()
        composeTestRule.onNodeWithContentDescription(description).performClick()
        assertEquals(true, clicked)
    }

    @Test
    fun testOnFlipCameraActionClick() {
        var clicked = false
        val description = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_flip_camera)
        composeTestRule.setContent {
            CallSheetItem(
                callAction = FlipCameraAction(),
                label = false,
                extended = false,
                onHangUpClick = { },
                onMicToggle = { },
                onCameraToggle = { },
                onScreenShareToggle = { },
                onFlipCameraClick = { clicked = true },
                onAudioClick = { },
                onChatClick = { },
                onFileShareClick = { },
                onWhiteboardClick = { },
                onSignatureClick = {},
                onVirtualBackgroundToggle = { }
            )
        }
        composeTestRule.onNodeWithContentDescription(description).assertHasClickAction()
        composeTestRule.onNodeWithContentDescription(description).performClick()
        assertEquals(true, clicked)
    }

    @Test
    fun testOnAudioActionClick() {
        var clicked = false
        val description = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_audio)
        composeTestRule.setContent {
            CallSheetItem(
                callAction = AudioAction(),
                label = false,
                extended = false,
                onHangUpClick = { },
                onMicToggle = { },
                onCameraToggle = { },
                onScreenShareToggle = { },
                onFlipCameraClick = { },
                onAudioClick = { clicked = true },
                onChatClick = { },
                onFileShareClick = { },
                onWhiteboardClick = { },
                onSignatureClick = {},
                onVirtualBackgroundToggle = { }
            )
        }
        composeTestRule.onNodeWithContentDescription(description).assertHasClickAction()
        composeTestRule.onNodeWithContentDescription(description).performClick()
        assertEquals(true, clicked)
    }

    @Test
    fun testOnChatActionClick() {
        var clicked = false
        val description = composeTestRule.activity.getString(R.string.kaleyra_strings_action_chat)
        composeTestRule.setContent {
            CallSheetItem(
                callAction = ChatAction(),
                label = false,
                extended = false,
                onHangUpClick = { },
                onMicToggle = { },
                onCameraToggle = { },
                onScreenShareToggle = { },
                onFlipCameraClick = { },
                onAudioClick = { },
                onChatClick = { clicked = true },
                onFileShareClick = { },
                onWhiteboardClick = { },
                onSignatureClick = {},
                onVirtualBackgroundToggle = { }
            )
        }
        composeTestRule.onNodeWithContentDescription(description).assertHasClickAction()
        composeTestRule.onNodeWithContentDescription(description).performClick()
        assertEquals(true, clicked)
    }

    @Test
    fun testOnFileShareActionClick() {
        var clicked = false
        val description = composeTestRule.activity.getString(R.string.kaleyra_strings_action_files)
        composeTestRule.setContent {
            CallSheetItem(
                callAction = FileShareAction(),
                label = false,
                extended = false,
                onHangUpClick = { },
                onMicToggle = { },
                onCameraToggle = { },
                onScreenShareToggle = { },
                onFlipCameraClick = { },
                onAudioClick = { },
                onChatClick = { },
                onFileShareClick = { clicked = true },
                onWhiteboardClick = { },
                onSignatureClick = {},
                onVirtualBackgroundToggle = { }
            )
        }
        composeTestRule.onNodeWithContentDescription(description).assertHasClickAction()
        composeTestRule.onNodeWithContentDescription(description).performClick()
        assertEquals(true, clicked)
    }

    @Test
    fun testOnScreenShareActionClick() {
        var clicked = false
        val description = composeTestRule.activity.getString(R.string.kaleyra_strings_action_start_screen_share)
        composeTestRule.setContent {
            CallSheetItem(
                callAction = ScreenShareAction.UserChoice(),
                label = false,
                extended = false,
                onHangUpClick = { },
                onMicToggle = { },
                onCameraToggle = { },
                onScreenShareToggle = { clicked = it },
                onFlipCameraClick = { },
                onAudioClick = { },
                onChatClick = { },
                onFileShareClick = { },
                onWhiteboardClick = { },
                onSignatureClick = {},
                onVirtualBackgroundToggle = { }
            )
        }
        composeTestRule.onNodeWithContentDescription(description).assertHasClickAction()
        composeTestRule.onNodeWithContentDescription(description).performClick()
        assertEquals(true, clicked)
    }

    @Test
    fun testOnWhiteboardActionClick() {
        var clicked = false
        val description = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_whiteboard)
        composeTestRule.setContent {
            CallSheetItem(
                callAction = WhiteboardAction(),
                label = false,
                extended = false,
                onHangUpClick = { },
                onMicToggle = { },
                onCameraToggle = { },
                onScreenShareToggle = { },
                onFlipCameraClick = { },
                onAudioClick = { },
                onChatClick = { },
                onFileShareClick = { },
                onWhiteboardClick = { clicked = true },
                onVirtualBackgroundToggle = { },
                onSignatureClick = {}
            )
        }
        composeTestRule.onNodeWithContentDescription(description).assertHasClickAction()
        composeTestRule.onNodeWithContentDescription(description).performClick()
        assertEquals(true, clicked)
    }

    @Test
    fun testOnVirtualBackgroundActionClick() {
        var clicked = false
        val description = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_virtual_background)
        composeTestRule.setContent {
            CallSheetItem(
                callAction = VirtualBackgroundAction(),
                label = false,
                extended = false,
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
                onVirtualBackgroundToggle = { clicked = true }
            )
        }
        composeTestRule.onNodeWithContentDescription(description).assertHasClickAction()
        composeTestRule.onNodeWithContentDescription(description).performClick()
        assertEquals(true, clicked)
    }

    @Test
    fun hangUpActionNotExtended_actionHasDefaultWidth() {
        val description = composeTestRule.activity.getString(R.string.kaleyra_strings_action_call_end)
        composeTestRule.setContent {
            CallSheetItem(
                callAction = HangUpAction(),
                label = false,
                extended = false,
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
                onVirtualBackgroundToggle = { }
            )
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithContentDescription(description).assertWidthIsEqualTo(
            HangUpActionWidth
        )
    }

    @Test
    fun hangUpActionExtended_actionHasExtendedWidth() {
        val description = composeTestRule.activity.getString(R.string.kaleyra_strings_action_call_end)
        composeTestRule.setContent {
            CallSheetItem(
                callAction = HangUpAction(),
                label = false,
                extended = true,
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
                onVirtualBackgroundToggle = { }
            )
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithContentDescription(description).assertWidthIsEqualTo(
            HangUpActionExtendedWidth
        )
    }

    @Test
    fun flipCameraActionLabelTrue_actionHasLabel() {
        val text = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_flip_camera)
        composeTestRule.setContent {
            CallSheetItem(
                callAction = FlipCameraAction(),
                label = true,
                extended = false,
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
                onVirtualBackgroundToggle = { }
            )
        }
        composeTestRule.onNodeWithText(text).assertIsDisplayed()
    }

    @Test
    fun flipCameraActionLabelFalse_actionHasNoLabel() {
        val text = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_flip_camera)
        composeTestRule.setContent {
            CallSheetItem(
                callAction = FlipCameraAction(),
                label = false,
                extended = false,
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
                onVirtualBackgroundToggle = { }
            )
        }
        composeTestRule.onNodeWithText(text).assertDoesNotExist()
    }

    @Test
    fun audioActionLabelTrue_actionHasLabel() {
        val text = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_audio)
        composeTestRule.setContent {
            CallSheetItem(
                callAction = AudioAction(),
                label = true,
                extended = false,
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
                onVirtualBackgroundToggle = { }
            )
        }
        composeTestRule.onNodeWithText(text).assertIsDisplayed()
    }

    @Test
    fun audioActionLabelFalse_actionHasNoLabel() {
        val text = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_audio)
        composeTestRule.setContent {
            CallSheetItem(
                callAction = AudioAction(),
                label = false,
                extended = false,
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
                onVirtualBackgroundToggle = { }
            )
        }
        composeTestRule.onNodeWithText(text).assertDoesNotExist()
    }

    @Test
    fun chatActionLabelTrue_actionHasLabel() {
        val text = composeTestRule.activity.getString(R.string.kaleyra_strings_action_chat)
        composeTestRule.setContent {
            CallSheetItem(
                callAction = ChatAction(),
                label = true,
                extended = false,
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
                onVirtualBackgroundToggle = { }
            )
        }
        composeTestRule.onNodeWithText(text).assertIsDisplayed()
    }

    @Test
    fun chatActionLabelFalse_actionHasNoLabel() {
        val text = composeTestRule.activity.getString(R.string.kaleyra_strings_action_chat)
        composeTestRule.setContent {
            CallSheetItem(
                callAction = ChatAction(),
                label = false,
                extended = false,
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
                onVirtualBackgroundToggle = { }
            )
        }
        composeTestRule.onNodeWithText(text).assertDoesNotExist()
    }

    @Test
    fun fileShareActionLabelTrue_actionHasLabel() {
        val text = composeTestRule.activity.getString(R.string.kaleyra_strings_action_files)
        composeTestRule.setContent {
            CallSheetItem(
                callAction = FileShareAction(),
                label = true,
                extended = false,
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
                onVirtualBackgroundToggle = { }
            )
        }
        composeTestRule.onNodeWithText(text).assertIsDisplayed()
    }

    @Test
    fun fileShareActionLabelFalse_actionHasNoLabel() {
        val text = composeTestRule.activity.getString(R.string.kaleyra_strings_action_files)
        composeTestRule.setContent {
            CallSheetItem(
                callAction = FileShareAction(),
                label = false,
                extended = false,
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
                onVirtualBackgroundToggle = { }
            )
        }
        composeTestRule.onNodeWithText(text).assertDoesNotExist()
    }

    @Test
    fun screenShareActionLabelTrue_actionHasLabel() {
        val text = composeTestRule.activity.getString(R.string.kaleyra_strings_action_start_screen_share)
        composeTestRule.setContent {
            CallSheetItem(
                callAction = ScreenShareAction.UserChoice(),
                label = true,
                extended = false,
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
                onVirtualBackgroundToggle = { }
            )
        }
        composeTestRule.onNodeWithText(text).assertIsDisplayed()
    }

    @Test
    fun screenShareActionLabelFalse_actionHasNoLabel() {
        val text = composeTestRule.activity.getString(R.string.kaleyra_strings_action_start_screen_share)
        composeTestRule.setContent {
            CallSheetItem(
                callAction = ScreenShareAction.UserChoice(),
                label = false,
                extended = false,
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
                onVirtualBackgroundToggle = { }
            )
        }
        composeTestRule.onNodeWithText(text).assertDoesNotExist()
    }

    @Test
    fun whiteboardActionLabelTrue_actionHasLabel() {
        val text = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_whiteboard)
        composeTestRule.setContent {
            CallSheetItem(
                callAction = WhiteboardAction(),
                label = true,
                extended = false,
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
                onVirtualBackgroundToggle = { }
            )
        }
        composeTestRule.onNodeWithText(text).assertIsDisplayed()
    }

    @Test
    fun whiteboardActionLabelFalse_actionHasNoLabel() {
        val text = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_whiteboard)
        composeTestRule.setContent {
            CallSheetItem(
                callAction = WhiteboardAction(),
                label = false,
                extended = false,
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
                onVirtualBackgroundToggle = { }
            )
        }
        composeTestRule.onNodeWithText(text).assertDoesNotExist()
    }

    @Test
    fun virtualBackgroundActionLabelTrue_actionHasLabel() {
        val text = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_virtual_background)
        composeTestRule.setContent {
            CallSheetItem(
                callAction = VirtualBackgroundAction(),
                label = true,
                extended = false,
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
                onVirtualBackgroundToggle = { }
            )
        }
        composeTestRule.onNodeWithText(text).assertIsDisplayed()
    }

    @Test
    fun virtualBackgroundActionLabelFalse_actionHasNoLabel() {
        val text = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_virtual_background)
        composeTestRule.setContent {
            CallSheetItem(
                callAction = VirtualBackgroundAction(),
                label = false,
                extended = false,
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
                onVirtualBackgroundToggle = { }
            )
        }
        composeTestRule.onNodeWithText(text).assertDoesNotExist()
    }

    @Test
    fun chatActionBadgeCountIsZero_actionBadgeDoesNotExists() {
        composeTestRule.setContent {
            CallSheetItem(
                callAction = ChatAction(),
                label = false,
                extended = false,
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
                onVirtualBackgroundToggle = { }
            )
        }
        composeTestRule.onNodeWithText("0").assertDoesNotExist()
    }

    @Test
    fun chatActionBadgeCountIsHigherThanZero_actionBadgeIsDisplayed() {
        composeTestRule.setContent {
            CallSheetItem(
                callAction = ChatAction(notificationCount = 3),
                label = false,
                extended = false,
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
                onVirtualBackgroundToggle = { }
            )
        }
        composeTestRule.onNodeWithText("3").assertIsDisplayed()
    }

    @Test
    fun fileShareActionBadgeCountIsZero_actionBadgeDoesNotExists() {
        composeTestRule.setContent {
            CallSheetItem(
                callAction = FileShareAction(),
                label = false,
                extended = false,
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
                onVirtualBackgroundToggle = { }
            )
        }
        composeTestRule.onNodeWithText("0").assertDoesNotExist()
    }

    @Test
    fun fileShareActionBadgeCountIsHigherThanZero_actionBadgeIsDisplayed() {
        composeTestRule.setContent {
            CallSheetItem(
                callAction = FileShareAction(notificationCount = 3),
                label = false,
                extended = false,
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
                onVirtualBackgroundToggle = { }
            )
        }
        composeTestRule.onNodeWithText("3").assertIsDisplayed()
    }

    @Test
    fun whiteboardActionBadgeCountIsZero_actionBadgeDoesNotExists() {
        composeTestRule.setContent {
            CallSheetItem(
                callAction = WhiteboardAction(),
                label = false,
                extended = false,
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
                onVirtualBackgroundToggle = { }
            )
        }
        composeTestRule.onNodeWithText("0").assertDoesNotExist()
    }

    @Test
    fun whiteboardActionBadgeCountIsHigherThanZero_actionBadgeIsDisplayed() {
        composeTestRule.setContent {
            CallSheetItem(
                callAction = WhiteboardAction(notificationCount = 3),
                label = false,
                extended = false,
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
                onVirtualBackgroundToggle = { }
            )
        }
        composeTestRule.onNodeWithText("3").assertIsDisplayed()
    }

    @Test
    fun micActionWarning_warningBadgeIsDisplayed() {
        composeTestRule.setContent {
            CallSheetItem(
                callAction = MicAction(state = InputCallAction.State.Warning),
                label = false,
                extended = false,
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
                onVirtualBackgroundToggle = { }
            )
        }
        val description = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_description_enable_microphone)
        val warning = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_description_mic_warning)
        composeTestRule.onNodeWithContentDescription(warning).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(description).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(description).assertIsOn()
    }

    @Test
    fun micPermissionNotAsked_warningBadgeIsDisplayed() {
        composeTestRule.setContent {
            CallSheetItem(
                callAction = MicAction(),
                inputPermissions = InputPermissions(
                    micPermission = object : PermissionState {
                        override val permission: String = "testPermission"
                        override val status: PermissionStatus = PermissionStatus.Denied(shouldShowRationale = true)
                        override fun launchPermissionRequest() = Unit
                    },
                    wasMicPermissionAsked = false
                ),
                label = false,
                extended = false,
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
                onVirtualBackgroundToggle = { }
            )
        }
        val description = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_description_enable_microphone)
        val warning = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_description_mic_warning)
        composeTestRule.onNodeWithContentDescription(warning).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(description).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(description).assertIsOn()
    }

    @Test
    fun micPermissionShouldShowRationale_warningBadgeIsDisplayed() {
        val permission = mockk<PermissionState> {
            every { status } returns PermissionStatus.Denied(true)
        }
        composeTestRule.setContent {
            CallSheetItem(
                callAction = MicAction(),
                inputPermissions = InputPermissions(
                    wasMicPermissionAsked = true,
                    micPermission = permission
                ),
                label = false,
                extended = false,
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
                onVirtualBackgroundToggle = { }
            )
        }
        val description = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_description_enable_microphone)
        val warning = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_description_mic_warning)
        composeTestRule.onNodeWithContentDescription(warning).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(description).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(description).assertIsOn()
    }

    @Test
    fun micPermissionDeniedForever_errorBadgeIsDisplayed() {
        val permission = mockk<PermissionState> {
            every { status } returns PermissionStatus.Denied(false)
        }
        composeTestRule.setContent {
            CallSheetItem(
                callAction = MicAction(),
                inputPermissions = InputPermissions(
                    wasMicPermissionAsked = true,
                    micPermission = permission
                ),
                label = false,
                extended = false,
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
                onVirtualBackgroundToggle = { }
            )
        }
        val description = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_description_enable_microphone)
        val error = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_description_mic_error)
        composeTestRule.onNodeWithContentDescription(error).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(description).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(description).assertIsOn()
    }

    @Test
    fun cameraPermissionNotAskedAndShouldAskCameraPermission_warningBadgeIsDisplayed() {
        composeTestRule.setContent {
            CallSheetItem(
                callAction = CameraAction(),
                inputPermissions = InputPermissions(
                    cameraPermission = object : PermissionState {
                        override val permission: String = "testPermission"
                        override val status: PermissionStatus = PermissionStatus.Denied(shouldShowRationale = true)
                        override fun launchPermissionRequest() = Unit
                    },
                    shouldAskCameraPermission = true,
                    wasCameraPermissionAsked = false
                ),
                label = false,
                extended = false,
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
                onVirtualBackgroundToggle = { }
            )
        }
        val description = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_description_enable_camera)
        val warning = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_description_camera_warning)
        composeTestRule.onNodeWithContentDescription(warning).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(description).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(description).assertIsOn()
    }

    @Test
    fun cameraPermissionShouldShowRationale_warningBadgeIsDisplayed() {
        val permission = mockk<PermissionState> {
            every { status } returns PermissionStatus.Denied(true)
        }
        composeTestRule.setContent {
            CallSheetItem(
                callAction = CameraAction(),
                inputPermissions = InputPermissions(
                    shouldAskCameraPermission = true,
                    wasCameraPermissionAsked = true,
                    cameraPermission = permission
                ),
                label = false,
                extended = false,
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
                onVirtualBackgroundToggle = { }
            )
        }
        val description = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_description_enable_camera)
        val warning = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_description_camera_warning)
        composeTestRule.onNodeWithContentDescription(warning).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(description).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(description).assertIsOn()
    }

    @Test
    fun cameraPermissionDeniedForever_errorBadgeIsDisplayed() {
        val permission = mockk<PermissionState> {
            every { status } returns PermissionStatus.Denied(false)
        }
        composeTestRule.setContent {
            CallSheetItem(
                callAction = CameraAction(),
                inputPermissions = InputPermissions(
                    wasCameraPermissionAsked = true,
                    cameraPermission = permission
                ),
                label = false,
                extended = false,
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
                onVirtualBackgroundToggle = { }
            )
        }
        val description = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_description_enable_camera)
        val error = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_description_camera_error)
        composeTestRule.onNodeWithContentDescription(error).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(description).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(description).assertIsOn()
    }

    @Test
    fun micActionError_errorBadgeIsDisplayed() {
        composeTestRule.setContent {
            CallSheetItem(
                callAction = MicAction(state = InputCallAction.State.Error),
                label = false,
                extended = false,
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
                onVirtualBackgroundToggle = { }
            )
        }
        val description = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_description_enable_microphone)
        val error = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_description_mic_error)
        composeTestRule.onNodeWithContentDescription(error).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(description).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(description).assertIsOn()
    }

    @Test
    fun cameraActionWarning_warningBadgeIsDisplayed() {
        composeTestRule.setContent {
            CallSheetItem(
                callAction = CameraAction(state = InputCallAction.State.Warning),
                label = false,
                extended = false,
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
                onVirtualBackgroundToggle = { }
            )
        }
        val description = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_description_enable_camera)
        val warning = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_description_camera_warning)
        composeTestRule.onNodeWithContentDescription(warning).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(description).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(description).assertIsOn()
    }

    @Test
    fun cameraActionError_errorBadgeIsDisplayed() {
        composeTestRule.setContent {
            CallSheetItem(
                callAction = CameraAction(state = InputCallAction.State.Error),
                label = false,
                extended = false,
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
                onVirtualBackgroundToggle = { }
            )
        }
        val description = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_description_enable_camera)
        val error = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_description_camera_error)
        composeTestRule.onNodeWithContentDescription(error).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(description).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(description).assertIsOn()
    }

    @Test
    fun customActionEnabled_actionIsEnabled() {
        composeTestRule.setContent {
            CallSheetItem(
                callAction = CustomAction(
                    icon = R.drawable.ic_kaleyra_call_sheet_error,
                    isEnabled = true,
                    buttonTexts = CustomCallAction.ButtonTexts(text = "testText", null)
                ),
                label = false,
                extended = false,
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
                onVirtualBackgroundToggle = { }
            )
        }
        composeTestRule.onNodeWithContentDescription("testText").assertHasClickAction()
        composeTestRule.onNodeWithContentDescription("testText").assertIsEnabled()
    }

    @Test
    fun customActionDisabled_actionIsNotEnabled() {
        composeTestRule.setContent {
            CallSheetItem(
                callAction = CustomAction(
                    icon = R.drawable.ic_kaleyra_call_sheet_error,
                    isEnabled = false,
                    buttonTexts = CustomCallAction.ButtonTexts(text = "testText", null)
                ),
                label = false,
                extended = false,
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
                onVirtualBackgroundToggle = { }
            )
        }
        composeTestRule.onNodeWithContentDescription("testText").assertHasClickAction()
        composeTestRule.onNodeWithContentDescription("testText").assertIsNotEnabled()
    }

    @Test
    fun customActionLabelTrue_actionHasLabel() {
        composeTestRule.setContent {
            CallSheetItem(
                callAction = CustomAction(
                    icon = R.drawable.ic_kaleyra_call_sheet_error,
                    isEnabled = false,
                    buttonTexts = CustomCallAction.ButtonTexts(text = "testText", null)
                ),
                label = true,
                extended = false,
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
                onVirtualBackgroundToggle = { }
            )
        }
        composeTestRule.onNodeWithText("testText").assertIsDisplayed()
    }

    @Test
    fun customActionLabelFalse_actionHasNoLabel() {
        composeTestRule.setContent {
            CallSheetItem(
                callAction = CustomAction(
                    icon = R.drawable.ic_kaleyra_call_sheet_error,
                    isEnabled = false,
                    buttonTexts = CustomCallAction.ButtonTexts(text = "testText", null)
                ),
                label = false,
                extended = false,
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
                onVirtualBackgroundToggle = { }
            )
        }
        composeTestRule.onNodeWithText("testText").assertDoesNotExist()
    }

    @Test
    fun userClicksCustomAction_onClickInvoked() {
        var clicked = false
        composeTestRule.setContent {
            CallSheetItem(
                callAction = CustomAction(
                    icon = R.drawable.ic_kaleyra_call_sheet_error,
                    isEnabled = true,
                    buttonTexts = CustomCallAction.ButtonTexts(text = "testText", "descr"),
                    onClick = { clicked = true }
                ),
                label = false,
                extended = false,
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
                onVirtualBackgroundToggle = { }
            )
        }
        composeTestRule
            .onNodeWithContentDescription("descr")
            .assertHasClickAction()
            .assertIsDisplayed()
            .performClick()
        assertEquals(true, clicked)
    }

    @Test
    fun customActionBadgeCountIsZero_actionBadgeDoesNotExists() {
        composeTestRule.setContent {
            CallSheetItem(
                callAction = CustomAction(
                    icon = R.drawable.ic_kaleyra_call_sheet_error,
                    buttonTexts = CustomCallAction.ButtonTexts(text = "testText", "descr"),
                    onClick = { },
                    notificationCount = 0
                ),
                label = false,
                extended = false,
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
                onVirtualBackgroundToggle = { }
            )
        }
        composeTestRule.onNodeWithText("0").assertDoesNotExist()
    }

    @Test
    fun customActionBadgeCountIsHigherThanZero_actionBadgeIsDisplayed() {
        composeTestRule.setContent {
            CallSheetItem(
                callAction = CustomAction(
                    icon = R.drawable.ic_kaleyra_call_sheet_error,
                    buttonTexts = CustomCallAction.ButtonTexts(text = "testText", "descr"),
                    onClick = { },
                    notificationCount = 5
                ),
                label = false,
                extended = false,
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
                onVirtualBackgroundToggle = { }
            )
        }
        composeTestRule.onNodeWithText("5").assertIsDisplayed()
    }
}