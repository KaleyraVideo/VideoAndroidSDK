package com.kaleyra.video_sdk.call.bottomsheet.sheetpanel

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.call.bottomsheet.view.sheetpanel.SheetPanelItem
import com.kaleyra.video_sdk.call.bottomsheet.model.AudioAction
import com.kaleyra.video_sdk.call.bottomsheet.model.CallActionUI
import com.kaleyra.video_sdk.call.bottomsheet.model.ChatAction
import com.kaleyra.video_sdk.call.bottomsheet.model.FileShareAction
import com.kaleyra.video_sdk.call.bottomsheet.model.FlipCameraAction
import com.kaleyra.video_sdk.call.bottomsheet.model.HangUpAction
import com.kaleyra.video_sdk.call.bottomsheet.model.ScreenShareAction
import com.kaleyra.video_sdk.call.bottomsheet.model.VirtualBackgroundAction
import com.kaleyra.video_sdk.call.bottomsheet.model.WhiteboardAction
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

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
        val text = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_flip_camera)
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
        val text = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_chat)
        composeTestRule.onNodeWithText(text).assertIsDisplayed()
    }

    @Test
    fun fileShareAction_fileShareTextIsDisplayed() {
        callAction = FileShareAction()
        val text = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_file_share)
        composeTestRule.onNodeWithText(text).assertIsDisplayed()
    }

    @Test
    fun whiteboardAction_whiteboardTextIsDisplayed() {
        callAction = WhiteboardAction()
        val text = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_whiteboard)
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
        val text = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_screen_share)
        composeTestRule.onNodeWithText(text).assertIsDisplayed()
    }

    @Test
    fun screenShareActionToggled_stopScreenShareTextIsDisplayed() {
        callAction = ScreenShareAction.UserChoice(isToggled = true)
        val text = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_description_stop_screen_share)
        composeTestRule.onNodeWithText(text).assertIsDisplayed()
    }

}