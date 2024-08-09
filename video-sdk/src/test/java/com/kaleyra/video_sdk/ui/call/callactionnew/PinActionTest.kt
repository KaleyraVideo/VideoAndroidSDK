package com.kaleyra.video_sdk.ui.call.callactionnew

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.call.callactions.view.PinAction
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class PinActionTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun labelFalse_pinLabelDoesNotExists() {
        val text = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_pin)
        composeTestRule.setContent {
            PinAction(
                pin = false,
                label = false,
                onClick = {}
            )
        }
        composeTestRule.onNodeWithText(text).assertDoesNotExist()
    }

    @Test
    fun labelTrue_pinLabelDoesIsDisplayed() {
        val text = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_pin)
        composeTestRule.setContent {
            PinAction(
                pin = false,
                label = true,
                onClick = {}
            )
        }
        composeTestRule.onNodeWithText(text).assertIsDisplayed()
    }

    @Test
    fun labelFalse_unpinLabelDoesNotExists() {
        val text = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_unpin)
        composeTestRule.setContent {
            PinAction(
                pin = true,
                label = false,
                onClick = {}
            )
        }
        composeTestRule.onNodeWithText(text).assertDoesNotExist()
    }

    @Test
    fun labelTrue_unpinLabelDoesIsDisplayed() {
        val text = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_unpin)
        composeTestRule.setContent {
            PinAction(
                pin = true,
                label = true,
                onClick = {}
            )
        }
        composeTestRule.onNodeWithText(text).assertIsDisplayed()
    }

    @Test
    fun testOnClickInvoked() {
        val text = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_pin)
        var clicked = false
        composeTestRule.setContent {
            PinAction(onClick = { clicked = true })
        }
        composeTestRule.onNodeWithContentDescription(text).assertIsEnabled()
        composeTestRule.onNodeWithContentDescription(text).performClick()
        Assert.assertEquals(true, clicked)
    }

    @Test
    fun testClickOnButtonDisabled() {
        val text = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_pin)
        composeTestRule.setContent {
            PinAction(
                enabled = false,
                onClick = { }
            )
        }
        composeTestRule.onNodeWithContentDescription(text).assertIsNotEnabled()
    }
}