package com.kaleyra.video_sdk.call.callactionnew

import androidx.activity.ComponentActivity
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.call.bottomsheet.view.sheetcontent.sheetitemslayout.SheetItemsSpacing
import com.kaleyra.video_sdk.call.callactions.view.CallActionDefaults
import com.kaleyra.video_sdk.call.callactions.view.HangUpAction
import com.kaleyra.video_sdk.call.callactions.view.HangUpActionExtendedMultiplier
import org.junit.Assert
import org.junit.Rule
import org.junit.Test

class HangUpActionTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun testExtendedButton() {
        val testTag = "testTag"
        composeTestRule.setContent {
            HangUpAction(
                extended = true,
                onClick = { },
                modifier = Modifier.testTag(testTag)
            )
        }
        val width = CallActionDefaults.MinButtonSize * HangUpActionExtendedMultiplier + SheetItemsSpacing * (HangUpActionExtendedMultiplier - 1)
        composeTestRule.onNodeWithTag(testTag).assertWidthIsEqualTo(width)
    }

    @Test
    fun testOnClickInvoked() {
        val text = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_hang_up)
        var clicked = false
        composeTestRule.setContent {
            HangUpAction(onClick = { clicked = true })
        }
        composeTestRule.onNodeWithContentDescription(text).assertIsEnabled()
        composeTestRule.onNodeWithContentDescription(text).performClick()
        Assert.assertEquals(true, clicked)
    }

    @Test
    fun testClickOnButtonDisabled() {
        val text = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_hang_up)
        composeTestRule.setContent {
            HangUpAction(
                enabled = false,
                onClick = { }
            )
        }
        composeTestRule.onNodeWithContentDescription(text).assertIsNotEnabled()
    }
}