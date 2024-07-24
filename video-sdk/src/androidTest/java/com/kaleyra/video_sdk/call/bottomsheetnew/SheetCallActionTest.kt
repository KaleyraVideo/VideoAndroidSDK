package com.kaleyra.video_sdk.call.bottomsheetnew

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.call.callactionnew.HangUpActionExtendedWidth
import com.kaleyra.video_sdk.call.callactionnew.HangUpActionWidth
import com.kaleyra.video_sdk.call.screennew.AudioAction
import com.kaleyra.video_sdk.call.screennew.CameraAction
import com.kaleyra.video_sdk.call.screennew.ChatAction
import com.kaleyra.video_sdk.call.screennew.FileShareAction
import com.kaleyra.video_sdk.call.screennew.FlipCameraAction
import com.kaleyra.video_sdk.call.screennew.HangUpAction
import com.kaleyra.video_sdk.call.screennew.InputCallAction
import com.kaleyra.video_sdk.call.screennew.MicAction
import com.kaleyra.video_sdk.call.screennew.ScreenShareAction
import com.kaleyra.video_sdk.call.screennew.VirtualBackgroundAction
import com.kaleyra.video_sdk.call.screennew.WhiteboardAction
import junit.framework.TestCase.assertEquals
import org.junit.Rule
import org.junit.Test

class SheetCallActionTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun hangUpActionEnabled_actionIsEnabled() {
        val description = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_hang_up)
        composeTestRule.setContent {
            CallSheetItem(
                callAction = HangUpAction(isEnabled = true),
                label = false,
                extended = false,
                onHangUpClick = {  },
                onMicToggle = { },
                onCameraToggle = { },
                onScreenShareToggle = {  },
                onFlipCameraClick = { },
                onAudioClick = { },
                onChatClick = { },
                onFileShareClick = { },
                onWhiteboardClick = { },
                onVirtualBackgroundToggle = { }
            )
        }
        composeTestRule.onNodeWithContentDescription(description).assertHasClickAction()
        composeTestRule.onNodeWithContentDescription(description).assertIsEnabled()
    }

    @Test
    fun hangUpActionDisabled_actionIsNotEnabled() {
        val description = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_hang_up)
        composeTestRule.setContent {
            CallSheetItem(
                callAction = HangUpAction(isEnabled = false),
                label = false,
                extended = false,
                onHangUpClick = {  },
                onMicToggle = { },
                onCameraToggle = { },
                onScreenShareToggle = {  },
                onFlipCameraClick = { },
                onAudioClick = { },
                onChatClick = { },
                onFileShareClick = { },
                onWhiteboardClick = { },
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
                onHangUpClick = {  },
                onMicToggle = { },
                onCameraToggle = { },
                onScreenShareToggle = {  },
                onFlipCameraClick = { },
                onAudioClick = { },
                onChatClick = { },
                onFileShareClick = { },
                onWhiteboardClick = { },
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
                onHangUpClick = {  },
                onMicToggle = { },
                onCameraToggle = { },
                onScreenShareToggle = {  },
                onFlipCameraClick = { },
                onAudioClick = { },
                onChatClick = { },
                onFileShareClick = { },
                onWhiteboardClick = { },
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
                onHangUpClick = {  },
                onMicToggle = { },
                onCameraToggle = { },
                onScreenShareToggle = {  },
                onFlipCameraClick = { },
                onAudioClick = { },
                onChatClick = { },
                onFileShareClick = { },
                onWhiteboardClick = { },
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
                onHangUpClick = {  },
                onMicToggle = { },
                onCameraToggle = { },
                onScreenShareToggle = {  },
                onFlipCameraClick = { },
                onAudioClick = { },
                onChatClick = { },
                onFileShareClick = { },
                onWhiteboardClick = { },
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
                onHangUpClick = {  },
                onMicToggle = { },
                onCameraToggle = { },
                onScreenShareToggle = {  },
                onFlipCameraClick = { },
                onAudioClick = { },
                onChatClick = { },
                onFileShareClick = { },
                onWhiteboardClick = { },
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
                onHangUpClick = {  },
                onMicToggle = { },
                onCameraToggle = { },
                onScreenShareToggle = {  },
                onFlipCameraClick = { },
                onAudioClick = { },
                onChatClick = { },
                onFileShareClick = { },
                onWhiteboardClick = { },
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
                onHangUpClick = {  },
                onMicToggle = { },
                onCameraToggle = { },
                onScreenShareToggle = {  },
                onFlipCameraClick = { },
                onAudioClick = { },
                onChatClick = { },
                onFileShareClick = { },
                onWhiteboardClick = { },
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
                onHangUpClick = {  },
                onMicToggle = { },
                onCameraToggle = { },
                onScreenShareToggle = {  },
                onFlipCameraClick = { },
                onAudioClick = { },
                onChatClick = { },
                onFileShareClick = { },
                onWhiteboardClick = { },
                onVirtualBackgroundToggle = { }
            )
        }
        composeTestRule.onNodeWithContentDescription(description).assertHasClickAction()
        composeTestRule.onNodeWithContentDescription(description).assertIsNotEnabled()
    }

    @Test
    fun chatActionEnabled_actionIsEnabled() {
        val description = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_chat)
        composeTestRule.setContent {
            CallSheetItem(
                callAction = ChatAction(isEnabled = true),
                label = false,
                extended = false,
                onHangUpClick = {  },
                onMicToggle = { },
                onCameraToggle = { },
                onScreenShareToggle = {  },
                onFlipCameraClick = { },
                onAudioClick = { },
                onChatClick = { },
                onFileShareClick = { },
                onWhiteboardClick = { },
                onVirtualBackgroundToggle = { }
            )
        }
        composeTestRule.onNodeWithContentDescription(description).assertHasClickAction()
        composeTestRule.onNodeWithContentDescription(description).assertIsEnabled()
    }

    @Test
    fun chatActionDisabled_actionIsNotEnabled() {
        val description = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_chat)
        composeTestRule.setContent {
            CallSheetItem(
                callAction = ChatAction(isEnabled = false),
                label = false,
                extended = false,
                onHangUpClick = {  },
                onMicToggle = { },
                onCameraToggle = { },
                onScreenShareToggle = {  },
                onFlipCameraClick = { },
                onAudioClick = { },
                onChatClick = { },
                onFileShareClick = { },
                onWhiteboardClick = { },
                onVirtualBackgroundToggle = { }
            )
        }
        composeTestRule.onNodeWithContentDescription(description).assertHasClickAction()
        composeTestRule.onNodeWithContentDescription(description).assertIsNotEnabled()
    }

    @Test
    fun fileShareActionEnabled_actionIsEnabled() {
        val description = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_file_share)
        composeTestRule.setContent {
            CallSheetItem(
                callAction = FileShareAction(isEnabled = true),
                label = false,
                extended = false,
                onHangUpClick = {  },
                onMicToggle = { },
                onCameraToggle = { },
                onScreenShareToggle = {  },
                onFlipCameraClick = { },
                onAudioClick = { },
                onChatClick = { },
                onFileShareClick = { },
                onWhiteboardClick = { },
                onVirtualBackgroundToggle = { }
            )
        }
        composeTestRule.onNodeWithContentDescription(description).assertHasClickAction()
        composeTestRule.onNodeWithContentDescription(description).assertIsEnabled()
    }

    @Test
    fun fileShareActionDisabled_actionIsNotEnabled() {
        val description = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_file_share)
        composeTestRule.setContent {
            CallSheetItem(
                callAction = FileShareAction(isEnabled = false),
                label = false,
                extended = false,
                onHangUpClick = {  },
                onMicToggle = { },
                onCameraToggle = { },
                onScreenShareToggle = {  },
                onFlipCameraClick = { },
                onAudioClick = { },
                onChatClick = { },
                onFileShareClick = { },
                onWhiteboardClick = { },
                onVirtualBackgroundToggle = { }
            )
        }
        composeTestRule.onNodeWithContentDescription(description).assertHasClickAction()
        composeTestRule.onNodeWithContentDescription(description).assertIsNotEnabled()
    }

    @Test
    fun screenShareActionEnabled_actionIsEnabled() {
        val description = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_screen_share)
        composeTestRule.setContent {
            CallSheetItem(
                callAction = ScreenShareAction(isEnabled = true),
                label = false,
                extended = false,
                onHangUpClick = {  },
                onMicToggle = { },
                onCameraToggle = { },
                onScreenShareToggle = {  },
                onFlipCameraClick = { },
                onAudioClick = { },
                onChatClick = { },
                onFileShareClick = { },
                onWhiteboardClick = { },
                onVirtualBackgroundToggle = { }
            )
        }
        composeTestRule.onNodeWithContentDescription(description).assertHasClickAction()
        composeTestRule.onNodeWithContentDescription(description).assertIsEnabled()
    }

    @Test
    fun screenShareActionDisabled_actionIsNotEnabled() {
        val description = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_screen_share)
        composeTestRule.setContent {
            CallSheetItem(
                callAction = ScreenShareAction(isEnabled = false),
                label = false,
                extended = false,
                onHangUpClick = {  },
                onMicToggle = { },
                onCameraToggle = { },
                onScreenShareToggle = {  },
                onFlipCameraClick = { },
                onAudioClick = { },
                onChatClick = { },
                onFileShareClick = { },
                onWhiteboardClick = { },
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
                onHangUpClick = {  },
                onMicToggle = { },
                onCameraToggle = { },
                onScreenShareToggle = {  },
                onFlipCameraClick = { },
                onAudioClick = { },
                onChatClick = { },
                onFileShareClick = { },
                onWhiteboardClick = { },
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
                onHangUpClick = {  },
                onMicToggle = { },
                onCameraToggle = { },
                onScreenShareToggle = {  },
                onFlipCameraClick = { },
                onAudioClick = { },
                onChatClick = { },
                onFileShareClick = { },
                onWhiteboardClick = { },
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
                onHangUpClick = {  },
                onMicToggle = { },
                onCameraToggle = { },
                onScreenShareToggle = {  },
                onFlipCameraClick = { },
                onAudioClick = { },
                onChatClick = { },
                onFileShareClick = { },
                onWhiteboardClick = { },
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
                onHangUpClick = {  },
                onMicToggle = { },
                onCameraToggle = { },
                onScreenShareToggle = {  },
                onFlipCameraClick = { },
                onAudioClick = { },
                onChatClick = { },
                onFileShareClick = { },
                onWhiteboardClick = { },
                onVirtualBackgroundToggle = { }
            )
        }
        composeTestRule.onNodeWithContentDescription(description).assertHasClickAction()
        composeTestRule.onNodeWithContentDescription(description).assertIsNotEnabled()
    }

    @Test
    fun testOnHangUpActionClick() {
        var clicked = false
        val description = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_hang_up)
        composeTestRule.setContent {
            CallSheetItem(
                callAction = HangUpAction(),
                label = false,
                extended = false,
                onHangUpClick = { clicked = true },
                onMicToggle = { },
                onCameraToggle = { },
                onScreenShareToggle = {  },
                onFlipCameraClick = { },
                onAudioClick = { },
                onChatClick = { },
                onFileShareClick = { },
                onWhiteboardClick = { },
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
                onScreenShareToggle = {  },
                onFlipCameraClick = { },
                onAudioClick = { },
                onChatClick = { },
                onFileShareClick = { },
                onWhiteboardClick = { },
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
                onScreenShareToggle = {  },
                onFlipCameraClick = { },
                onAudioClick = { },
                onChatClick = { },
                onFileShareClick = { },
                onWhiteboardClick = { },
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
                onScreenShareToggle = {  },
                onFlipCameraClick = { clicked = true },
                onAudioClick = { },
                onChatClick = { },
                onFileShareClick = { },
                onWhiteboardClick = { },
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
                onScreenShareToggle = {  },
                onFlipCameraClick = { },
                onAudioClick = { clicked = true },
                onChatClick = { },
                onFileShareClick = { },
                onWhiteboardClick = { },
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
        val description = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_chat)
        composeTestRule.setContent {
            CallSheetItem(
                callAction = ChatAction(),
                label = false,
                extended = false,
                onHangUpClick = { },
                onMicToggle = { },
                onCameraToggle = { },
                onScreenShareToggle = {  },
                onFlipCameraClick = { },
                onAudioClick = { },
                onChatClick = { clicked = true },
                onFileShareClick = { },
                onWhiteboardClick = { },
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
        val description = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_file_share)
        composeTestRule.setContent {
            CallSheetItem(
                callAction = FileShareAction(),
                label = false,
                extended = false,
                onHangUpClick = { },
                onMicToggle = { },
                onCameraToggle = { },
                onScreenShareToggle = {  },
                onFlipCameraClick = { },
                onAudioClick = { },
                onChatClick = { },
                onFileShareClick = { clicked = true },
                onWhiteboardClick = { },
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
        val description = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_screen_share)
        composeTestRule.setContent {
            CallSheetItem(
                callAction = ScreenShareAction(),
                label = false,
                extended = false,
                onHangUpClick = { },
                onMicToggle = { },
                onCameraToggle = { },
                onScreenShareToggle = { clicked = it  },
                onFlipCameraClick = { },
                onAudioClick = { },
                onChatClick = { },
                onFileShareClick = { },
                onWhiteboardClick = { },
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
                onVirtualBackgroundToggle = { }
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
                onScreenShareToggle = {  },
                onFlipCameraClick = { },
                onAudioClick = { },
                onChatClick = { },
                onFileShareClick = { },
                onWhiteboardClick = { },
                onVirtualBackgroundToggle = { clicked = true }
            )
        }
        composeTestRule.onNodeWithContentDescription(description).assertHasClickAction()
        composeTestRule.onNodeWithContentDescription(description).performClick()
        assertEquals(true, clicked)
    }

    @Test
    fun hangUpActionNotExtended_actionHasDefaultWidth() {
        val description = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_hang_up)
        composeTestRule.setContent {
            CallSheetItem(
                callAction = HangUpAction(),
                label = false,
                extended = false,
                onHangUpClick = {  },
                onMicToggle = { },
                onCameraToggle = { },
                onScreenShareToggle = {  },
                onFlipCameraClick = { },
                onAudioClick = { },
                onChatClick = { },
                onFileShareClick = { },
                onWhiteboardClick = { },
                onVirtualBackgroundToggle = { }
            )
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithContentDescription(description).assertWidthIsEqualTo(HangUpActionWidth)
    }

    @Test
    fun hangUpActionExtended_actionHasExtendedWidth() {
        val description = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_hang_up)
        composeTestRule.setContent {
            CallSheetItem(
                callAction = HangUpAction(),
                label = false,
                extended = true,
                onHangUpClick = {  },
                onMicToggle = { },
                onCameraToggle = { },
                onScreenShareToggle = {  },
                onFlipCameraClick = { },
                onAudioClick = { },
                onChatClick = { },
                onFileShareClick = { },
                onWhiteboardClick = { },
                onVirtualBackgroundToggle = { }
            )
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithContentDescription(description).assertWidthIsEqualTo(HangUpActionExtendedWidth)
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
                onScreenShareToggle = {  },
                onFlipCameraClick = { },
                onAudioClick = { },
                onChatClick = { },
                onFileShareClick = { },
                onWhiteboardClick = { },
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
                onScreenShareToggle = {  },
                onFlipCameraClick = { },
                onAudioClick = { },
                onChatClick = { },
                onFileShareClick = { },
                onWhiteboardClick = { },
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
                onScreenShareToggle = {  },
                onFlipCameraClick = { },
                onAudioClick = { },
                onChatClick = { },
                onFileShareClick = { },
                onWhiteboardClick = { },
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
                onScreenShareToggle = {  },
                onFlipCameraClick = { },
                onAudioClick = { },
                onChatClick = { },
                onFileShareClick = { },
                onWhiteboardClick = { },
                onVirtualBackgroundToggle = { }
            )
        }
        composeTestRule.onNodeWithText(text).assertDoesNotExist()
    }

    @Test
    fun chatActionLabelTrue_actionHasLabel() {
        val text = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_chat)
        composeTestRule.setContent {
            CallSheetItem(
                callAction = ChatAction(),
                label = true,
                extended = false,
                onHangUpClick = { },
                onMicToggle = { },
                onCameraToggle = { },
                onScreenShareToggle = {  },
                onFlipCameraClick = { },
                onAudioClick = { },
                onChatClick = { },
                onFileShareClick = { },
                onWhiteboardClick = { },
                onVirtualBackgroundToggle = { }
            )
        }
        composeTestRule.onNodeWithText(text).assertIsDisplayed()
    }

    @Test
    fun chatActionLabelFalse_actionHasNoLabel() {
        val text = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_chat)
        composeTestRule.setContent {
            CallSheetItem(
                callAction = ChatAction(),
                label = false,
                extended = false,
                onHangUpClick = { },
                onMicToggle = { },
                onCameraToggle = { },
                onScreenShareToggle = {  },
                onFlipCameraClick = { },
                onAudioClick = { },
                onChatClick = { },
                onFileShareClick = { },
                onWhiteboardClick = { },
                onVirtualBackgroundToggle = { }
            )
        }
        composeTestRule.onNodeWithText(text).assertDoesNotExist()
    }

    @Test
    fun fileShareActionLabelTrue_actionHasLabel() {
        val text = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_file_share)
        composeTestRule.setContent {
            CallSheetItem(
                callAction = FileShareAction(),
                label = true,
                extended = false,
                onHangUpClick = { },
                onMicToggle = { },
                onCameraToggle = { },
                onScreenShareToggle = {  },
                onFlipCameraClick = { },
                onAudioClick = { },
                onChatClick = { },
                onFileShareClick = { },
                onWhiteboardClick = { },
                onVirtualBackgroundToggle = { }
            )
        }
        composeTestRule.onNodeWithText(text).assertIsDisplayed()
    }

    @Test
    fun fileShareActionLabelFalse_actionHasNoLabel() {
        val text = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_file_share)
        composeTestRule.setContent {
            CallSheetItem(
                callAction = FileShareAction(),
                label = false,
                extended = false,
                onHangUpClick = { },
                onMicToggle = { },
                onCameraToggle = { },
                onScreenShareToggle = {  },
                onFlipCameraClick = { },
                onAudioClick = { },
                onChatClick = { },
                onFileShareClick = { },
                onWhiteboardClick = { },
                onVirtualBackgroundToggle = { }
            )
        }
        composeTestRule.onNodeWithText(text).assertDoesNotExist()
    }

    @Test
    fun screenShareActionLabelTrue_actionHasLabel() {
        val text = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_screen_share)
        composeTestRule.setContent {
            CallSheetItem(
                callAction = ScreenShareAction(),
                label = true,
                extended = false,
                onHangUpClick = { },
                onMicToggle = { },
                onCameraToggle = { },
                onScreenShareToggle = {  },
                onFlipCameraClick = { },
                onAudioClick = { },
                onChatClick = { },
                onFileShareClick = { },
                onWhiteboardClick = { },
                onVirtualBackgroundToggle = { }
            )
        }
        composeTestRule.onNodeWithText(text).assertIsDisplayed()
    }

    @Test
    fun screenShareActionLabelFalse_actionHasNoLabel() {
        val text = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_screen_share)
        composeTestRule.setContent {
            CallSheetItem(
                callAction = ScreenShareAction(),
                label = false,
                extended = false,
                onHangUpClick = { },
                onMicToggle = { },
                onCameraToggle = { },
                onScreenShareToggle = {  },
                onFlipCameraClick = { },
                onAudioClick = { },
                onChatClick = { },
                onFileShareClick = { },
                onWhiteboardClick = { },
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
                onScreenShareToggle = {  },
                onFlipCameraClick = { },
                onAudioClick = { },
                onChatClick = { },
                onFileShareClick = { },
                onWhiteboardClick = { },
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
                onScreenShareToggle = {  },
                onFlipCameraClick = { },
                onAudioClick = { },
                onChatClick = { },
                onFileShareClick = { },
                onWhiteboardClick = { },
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
                onScreenShareToggle = {  },
                onFlipCameraClick = { },
                onAudioClick = { },
                onChatClick = { },
                onFileShareClick = { },
                onWhiteboardClick = { },
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
                onScreenShareToggle = {  },
                onFlipCameraClick = { },
                onAudioClick = { },
                onChatClick = { },
                onFileShareClick = { },
                onWhiteboardClick = { },
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
                onScreenShareToggle = {  },
                onFlipCameraClick = { },
                onAudioClick = { },
                onChatClick = { },
                onFileShareClick = { },
                onWhiteboardClick = { },
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
                onScreenShareToggle = {  },
                onFlipCameraClick = { },
                onAudioClick = { },
                onChatClick = { },
                onFileShareClick = { },
                onWhiteboardClick = { },
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
                onScreenShareToggle = {  },
                onFlipCameraClick = { },
                onAudioClick = { },
                onChatClick = { },
                onFileShareClick = { },
                onWhiteboardClick = { },
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
                onScreenShareToggle = {  },
                onFlipCameraClick = { },
                onAudioClick = { },
                onChatClick = { },
                onFileShareClick = { },
                onWhiteboardClick = { },
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
                onScreenShareToggle = {  },
                onFlipCameraClick = { },
                onAudioClick = { },
                onChatClick = { },
                onFileShareClick = { },
                onWhiteboardClick = { },
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
                onScreenShareToggle = {  },
                onFlipCameraClick = { },
                onAudioClick = { },
                onChatClick = { },
                onFileShareClick = { },
                onWhiteboardClick = { },
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
                onHangUpClick = {  },
                onMicToggle = { },
                onCameraToggle = { },
                onScreenShareToggle = {  },
                onFlipCameraClick = { },
                onAudioClick = { },
                onChatClick = { },
                onFileShareClick = { },
                onWhiteboardClick = { },
                onVirtualBackgroundToggle = { }
            )
        }
        composeTestRule.onNodeWithText("!").assertIsDisplayed()
    }

    @Test
    fun micActionError_errorBadgeIsDisplayed() {
        composeTestRule.setContent {
            CallSheetItem(
                callAction = MicAction(state = InputCallAction.State.Error),
                label = false,
                extended = false,
                onHangUpClick = {  },
                onMicToggle = { },
                onCameraToggle = { },
                onScreenShareToggle = {  },
                onFlipCameraClick = { },
                onAudioClick = { },
                onChatClick = { },
                onFileShareClick = { },
                onWhiteboardClick = { },
                onVirtualBackgroundToggle = { }
            )
        }
        composeTestRule.onNodeWithText("!").assertIsDisplayed()
    }

    @Test
    fun cameraActionWarning_warningBadgeIsDisplayed() {
        composeTestRule.setContent {
            CallSheetItem(
                callAction = CameraAction(state = InputCallAction.State.Warning),
                label = false,
                extended = false,
                onHangUpClick = {  },
                onMicToggle = { },
                onCameraToggle = { },
                onScreenShareToggle = {  },
                onFlipCameraClick = { },
                onAudioClick = { },
                onChatClick = { },
                onFileShareClick = { },
                onWhiteboardClick = { },
                onVirtualBackgroundToggle = { }
            )
        }
        composeTestRule.onNodeWithText("!").assertIsDisplayed()
    }

    @Test
    fun cameraActionError_errorBadgeIsDisplayed() {
        composeTestRule.setContent {
            CallSheetItem(
                callAction = CameraAction(state = InputCallAction.State.Error),
                label = false,
                extended = false,
                onHangUpClick = {  },
                onMicToggle = { },
                onCameraToggle = { },
                onScreenShareToggle = {  },
                onFlipCameraClick = { },
                onAudioClick = { },
                onChatClick = { },
                onFileShareClick = { },
                onWhiteboardClick = { },
                onVirtualBackgroundToggle = { }
            )
        }
        composeTestRule.onNodeWithText("!").assertIsDisplayed()
    }
}