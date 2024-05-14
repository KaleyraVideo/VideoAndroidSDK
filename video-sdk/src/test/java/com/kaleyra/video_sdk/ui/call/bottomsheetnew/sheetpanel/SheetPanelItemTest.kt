package com.kaleyra.video_sdk.ui.call.bottomsheetnew.sheetpanel

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.call.bottomsheetnew.sheetpanel.SheetPanelItem
import com.kaleyra.video_sdk.call.screennew.AudioAction
import com.kaleyra.video_sdk.call.screennew.CallActionUI
import com.kaleyra.video_sdk.call.screennew.ChatAction
import com.kaleyra.video_sdk.call.screennew.FileShareAction
import com.kaleyra.video_sdk.call.screennew.FlipCameraAction
import com.kaleyra.video_sdk.call.screennew.HangUpAction
import com.kaleyra.video_sdk.call.screennew.VirtualBackgroundAction
import com.kaleyra.video_sdk.call.screennew.WhiteboardAction
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

}