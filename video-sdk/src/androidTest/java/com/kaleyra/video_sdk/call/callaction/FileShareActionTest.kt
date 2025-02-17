package com.kaleyra.video_sdk.call.callaction

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.call.callactions.view.FileShareAction
import org.junit.Assert
import org.junit.Rule
import org.junit.Test

class FileShareActionTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun testButtonTextIsNotDisplayedOnDefaultWidth() {
        val text = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_file_share)
        composeTestRule.setContent {
            FileShareAction(
                onClick = {}
            )
        }
        composeTestRule.onNodeWithText(text).assertDoesNotExist()
    }

    @Test
    fun testLabelIsDisplayed() {
        val text = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_file_share)
        composeTestRule.setContent {
            FileShareAction(
                label = true,
                onClick = {}
            )
        }
        composeTestRule.onNodeWithText(text).assertIsDisplayed()
    }

    @Test
    fun testButtonTextIsDisplayedOnWideWidth() {
        val text = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_file_share)
        composeTestRule.setContent {
            FileShareAction(
                modifier = Modifier.width(200.dp),
                onClick = {}
            )
        }
        composeTestRule.onNodeWithText(text).assertIsDisplayed()
    }

    @Test
    fun testBadgeIsDisplayed() {
        val badgeCount = 10
        composeTestRule.setContent {
            FileShareAction(
                onClick = {},
                badgeCount = badgeCount,
            )
        }
        composeTestRule.onNodeWithText(badgeCount.toString()).assertIsDisplayed()
    }

    @Test
    fun testOnClickInvoked() {
        val text = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_file_share)
        var clicked = false
        composeTestRule.setContent {
            FileShareAction(onClick = { clicked = true })
        }
        composeTestRule.onNodeWithContentDescription(text).assertIsEnabled()
        composeTestRule.onNodeWithContentDescription(text).performClick()
        Assert.assertEquals(true, clicked)
    }

    @Test
    fun testClickOnButtonDisabled() {
        val text = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_file_share)
        composeTestRule.setContent {
            FileShareAction(
                enabled = false,
                onClick = { }
            )
        }
        composeTestRule.onNodeWithContentDescription(text).assertIsNotEnabled()
    }
}