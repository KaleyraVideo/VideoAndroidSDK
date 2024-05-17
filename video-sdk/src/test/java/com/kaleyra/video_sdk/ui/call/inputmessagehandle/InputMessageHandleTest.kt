package com.kaleyra.video_sdk.ui.call.inputmessagehandle

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.call.bottomsheetnew.inputmessage.model.MicMessage
import com.kaleyra.video_sdk.call.bottomsheetnew.inputmessage.view.InputMessageDuration
import com.kaleyra.video_sdk.call.screennew.InputMessageDragHandleTag
import com.kaleyra.video_sdk.call.screennew.InputMessageHandle
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class InputMessageHandleTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun nullInputMessage_dragHandleIsDisplayed() {
        composeTestRule.setContent {
            InputMessageHandle(null)
        }
        composeTestRule.onNodeWithTag(InputMessageDragHandleTag).assertIsDisplayed()
    }

    @Test
    fun testInputMessageHandleOnInputMessage() {
        val microphone = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_microphone)
        val on = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_on)
        composeTestRule.setContent {
            InputMessageHandle(MicMessage.Enabled)
        }
        composeTestRule.onNodeWithText(microphone).assertIsDisplayed()
        composeTestRule.onNodeWithText(on).assertIsDisplayed()
        composeTestRule.onNodeWithTag(InputMessageDragHandleTag).assertDoesNotExist()
        composeTestRule.mainClock.advanceTimeBy(InputMessageDuration)
        composeTestRule.onNodeWithText(microphone).assertDoesNotExist()
        composeTestRule.onNodeWithText(on).assertDoesNotExist()
        composeTestRule.onNodeWithTag(InputMessageDragHandleTag).assertIsDisplayed()
    }
}