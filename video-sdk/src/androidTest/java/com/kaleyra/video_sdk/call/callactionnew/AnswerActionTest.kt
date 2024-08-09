package com.kaleyra.video_sdk.call.callactionnew

import androidx.activity.ComponentActivity
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.call.bottomsheet.view.sheetcontent.sheetitemslayout.SheetItemsSpacing
import com.kaleyra.video_sdk.call.callactions.view.AnswerAction
import com.kaleyra.video_sdk.call.callactions.view.AnswerActionExtendedMultiplier
import com.kaleyra.video_sdk.call.callactions.view.AnswerActionMultiplier
import com.kaleyra.video_sdk.call.callactions.view.CallActionDefaults
import org.junit.Assert
import org.junit.Rule
import org.junit.Test

class AnswerActionTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun testDefaultButton() {
        val testTag = "testTag"
        composeTestRule.setContent {
            AnswerAction(
                extended = false,
                onClick = { },
                modifier = Modifier.testTag(testTag)
            )
        }
        val width = CallActionDefaults.MinButtonSize * AnswerActionMultiplier + SheetItemsSpacing
        composeTestRule.onNodeWithTag(testTag).assertWidthIsEqualTo(width)
    }

    @Test
    fun testExtendedButton() {
        val testTag = "testTag"
        composeTestRule.setContent {
            AnswerAction(
                extended = true,
                onClick = { },
                modifier = Modifier.testTag(testTag)
            )
        }
        val width = CallActionDefaults.MinButtonSize * AnswerActionExtendedMultiplier + SheetItemsSpacing * (AnswerActionExtendedMultiplier - 1)
        composeTestRule.onNodeWithTag(testTag).assertWidthIsEqualTo(width)
    }

    @Test
    fun testOnClickInvoked() {
        val text = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_answer)
        var clicked = false
        composeTestRule.setContent {
            AnswerAction(onClick = { clicked = true }, extended = true)
        }
        composeTestRule.onNodeWithText(text).assertIsEnabled()
        composeTestRule.onNodeWithText(text).performClick()
        Assert.assertEquals(true, clicked)
    }
}