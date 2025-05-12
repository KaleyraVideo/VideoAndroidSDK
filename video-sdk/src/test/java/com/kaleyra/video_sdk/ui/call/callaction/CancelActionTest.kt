package com.kaleyra.video_sdk.ui.call.callaction

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.call.callactions.view.CancelAction
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class CancelActionTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun labelFalse_labelDoesNotExists() {
        val text = composeTestRule.activity.getString(R.string.kaleyra_strings_action_cancel)
        composeTestRule.setContent {
            CancelAction(
                label = false,
                onClick = {}
            )
        }
        composeTestRule.onNodeWithText(text).assertDoesNotExist()
    }

    @Test
    fun labelTrue_labelDoesIsDisplayed() {
        val text = composeTestRule.activity.getString(R.string.kaleyra_strings_action_cancel)
        composeTestRule.setContent {
            CancelAction(
                label = true,
                onClick = {}
            )
        }
        composeTestRule.onNodeWithText(text).assertIsDisplayed()
    }

    @Test
    fun testOnClickInvoked() {
        val text = composeTestRule.activity.getString(R.string.kaleyra_strings_action_cancel)
        var clicked = false
        composeTestRule.setContent {
            CancelAction(onClick = { clicked = true })
        }
        composeTestRule.onNodeWithContentDescription(text).assertIsEnabled()
        composeTestRule.onNodeWithContentDescription(text).performClick()
        Assert.assertEquals(true, clicked)
    }

    @Test
    fun testClickOnButtonDisabled() {
        val text = composeTestRule.activity.getString(R.string.kaleyra_strings_action_cancel)
        composeTestRule.setContent {
            CancelAction(
                enabled = false,
                onClick = { }
            )
        }
        composeTestRule.onNodeWithContentDescription(text).assertIsNotEnabled()
    }
}