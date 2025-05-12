package com.kaleyra.video_sdk.ui.call.bottomsheet.sheetpanel

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.call.bottomsheet.model.AudioAction
import com.kaleyra.video_sdk.call.bottomsheet.model.CallActionUI
import com.kaleyra.video_sdk.call.bottomsheet.model.CameraAction
import com.kaleyra.video_sdk.call.bottomsheet.model.ChatAction
import com.kaleyra.video_sdk.call.bottomsheet.model.CustomAction
import com.kaleyra.video_sdk.call.bottomsheet.model.CustomCallAction
import com.kaleyra.video_sdk.call.bottomsheet.model.FileShareAction
import com.kaleyra.video_sdk.call.bottomsheet.model.FlipCameraAction
import com.kaleyra.video_sdk.call.bottomsheet.model.HangUpAction
import com.kaleyra.video_sdk.call.bottomsheet.model.MicAction
import com.kaleyra.video_sdk.call.bottomsheet.model.ScreenShareAction
import com.kaleyra.video_sdk.call.bottomsheet.model.VirtualBackgroundAction
import com.kaleyra.video_sdk.call.bottomsheet.model.WhiteboardAction
import com.kaleyra.video_sdk.call.bottomsheet.view.sheetpanel.SheetPanelItem
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class SheetPanelItemTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private var callAction by mutableStateOf<CallActionUI>(HangUpAction())

    @Before
    fun setUp() {
        composeTestRule.setContent {
            SheetPanelItem(callAction = callAction)
        }
    }

    @After
    fun tearDown() {
        callAction = HangUpAction()
    }

    @Test
    fun flipCameraAction_flipCameraTextIsDisplayed() {
        callAction = FlipCameraAction()
        val text = composeTestRule.activity.getString(R.string.kaleyra_strings_action_flip_camera)
        composeTestRule.onNodeWithText(text).assertIsDisplayed()
    }

    @Test
    fun audioAction_audioTextIsDisplayed() {
        callAction = AudioAction()
        val text = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_audio)
        composeTestRule.onNodeWithText(text).assertIsDisplayed()
    }

    @Test
    fun chatAction_chatTextIsDisplayed() {
        callAction = ChatAction()
        val text = composeTestRule.activity.getString(R.string.kaleyra_strings_action_chat)
        composeTestRule.onNodeWithText(text).assertIsDisplayed()
    }

    @Test
    fun fileShareAction_fileShareTextIsDisplayed() {
        callAction = FileShareAction()
        val text = composeTestRule.activity.getString(R.string.kaleyra_strings_action_files)
        composeTestRule.onNodeWithText(text).assertIsDisplayed()
    }

    @Test
    fun whiteboardAction_whiteboardTextIsDisplayed() {
        callAction = WhiteboardAction()
        val text = composeTestRule.activity.getString(R.string.kaleyra_strings_action_board)
        composeTestRule.onNodeWithText(text).assertIsDisplayed()
    }

    @Test
    fun virtualBackgroundAction_virtualBackgroundTextIsDisplayed() {
        callAction = VirtualBackgroundAction()
        val text = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_virtual_background)
        composeTestRule.onNodeWithText(text).assertIsDisplayed()
    }

    @Test
    fun screenShareAction_screenShareTextIsDisplayed() {
        callAction = ScreenShareAction.UserChoice()
        val text = composeTestRule.activity.getString(R.string.kaleyra_strings_action_start_screen_share)
        composeTestRule.onNodeWithText(text).assertIsDisplayed()
    }

    @Test
    fun screenShareActionToggled_stopScreenShareTextIsDisplayed() {
        callAction = ScreenShareAction.UserChoice(isToggled = true)
        val text = composeTestRule.activity.getString(R.string.kaleyra_strings_action_stop_screen_share)
        composeTestRule.onNodeWithText(text).assertIsDisplayed()
    }

    @Test
    fun micActionNotToggled_muteTextIsDisplayed() {
        callAction = MicAction(isToggled = false)
        val text = composeTestRule.activity.getString(R.string.kaleyra_call_action_mic_mute)
        composeTestRule.onNodeWithText(text).assertIsDisplayed()
    }

    @Test
    fun micActionToggled_unMuteTextIsDisplayed() {
        callAction = MicAction(isToggled = true)
        val text = composeTestRule.activity.getString(R.string.kaleyra_call_action_mic_unmute)
        composeTestRule.onNodeWithText(text).assertIsDisplayed()
    }

    @Test
    fun cameraActionNotToggled_disableTextIsDisplayed() {
        callAction = CameraAction(isToggled = false)
        val text = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_disable)
        composeTestRule.onNodeWithText(text).assertIsDisplayed()
    }

    @Test
    fun cameraActionToggled_enableTextIsDisplayed() {
        callAction = CameraAction(isToggled = true)
        val text = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_enable)
        composeTestRule.onNodeWithText(text).assertIsDisplayed()
    }

    @Test
    fun hangUpAction_endTextIsDisplayed() {
        callAction = HangUpAction()
        val text = composeTestRule.activity.getString(R.string.kaleyra_strings_action_call_end)
        composeTestRule.onNodeWithText(text).assertIsDisplayed()
    }

    @Test
    fun chatActionBadgeCountIsZero_actionBadgeDoesNotExists() {
        callAction = ChatAction()
        composeTestRule.onNodeWithText("0").assertDoesNotExist()
    }

    @Test
    fun chatActionBadgeCountIsHigherThanZero_actionBadgeIsDisplayed() {
        callAction = ChatAction(notificationCount = 3)
        composeTestRule.onNodeWithText("3").assertIsDisplayed()
    }

    @Test
    fun fileShareActionBadgeCountIsZero_actionBadgeDoesNotExists() {
        callAction = FileShareAction()
        composeTestRule.onNodeWithText("0").assertDoesNotExist()
    }

    @Test
    fun fileShareActionBadgeCountIsHigherThanZero_actionBadgeIsDisplayed() {
        callAction = FileShareAction(notificationCount = 3)
        composeTestRule.onNodeWithText("3").assertIsDisplayed()
    }

    @Test
    fun whiteboardActionBadgeCountIsZero_actionBadgeDoesNotExists() {
        callAction = WhiteboardAction()
        composeTestRule.onNodeWithText("0").assertDoesNotExist()
    }

    @Test
    fun whiteboardActionBadgeCountIsHigherThanZero_actionBadgeIsDisplayed() {
        callAction = WhiteboardAction(notificationCount = 3)
        composeTestRule.onNodeWithText("3").assertIsDisplayed()
    }

    @Test
    fun customActionBadgeCountIsZero_actionBadgeDoesNotExists() {
        callAction = CustomAction(
            icon = R.drawable.ic_kaleyra_call_sheet_error,
            buttonTexts = CustomCallAction.ButtonTexts(text = "testText", "descr"),
            onClick = { },
            notificationCount = 0
        )
        composeTestRule.onNodeWithText("0").assertDoesNotExist()
    }

    @Test
    fun customActionBadgeCountIsHigherThanZero_actionBadgeIsDisplayed() {
        callAction = CustomAction(
            icon = R.drawable.ic_kaleyra_call_sheet_error,
            buttonTexts = CustomCallAction.ButtonTexts(text = "testText", "descr"),
            onClick = { },
            notificationCount = 5
        )
        composeTestRule.onNodeWithText("5").assertIsDisplayed()
    }
}
