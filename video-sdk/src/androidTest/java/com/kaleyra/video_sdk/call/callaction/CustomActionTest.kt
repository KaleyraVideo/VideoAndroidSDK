package com.kaleyra.video_sdk.call.callaction

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.call.bottomsheet.model.CustomCallAction
import com.kaleyra.video_sdk.call.callactions.view.CustomAction
import org.junit.Assert
import org.junit.Rule
import org.junit.Test

class CustomActionTest {


    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun testButtonTextIsNotDisplayedOnDefaultWidth() {
        composeTestRule.setContent {
            CustomAction(
                label = false,
                icon = R.drawable.ic_kaleyra_call_sheet_error,
                buttonTexts = CustomCallAction.ButtonTexts("test", null),
                onClick = {}
            )
        }
        composeTestRule.onNodeWithText("test").assertDoesNotExist()
    }

    @Test
    fun testButtonContentDescription() {
        composeTestRule.setContent {
            CustomAction(
                label = false,
                icon = R.drawable.ic_kaleyra_call_sheet_error,
                buttonTexts = CustomCallAction.ButtonTexts("test", "contentDescriptionTest"),
                onClick = {}
            )
        }
        composeTestRule
            .onNodeWithContentDescription("contentDescriptionTest")
            .assertHasClickAction()
            .assertIsDisplayed()
    }

    @Test
    fun testLabelIsDisplayed() {
        composeTestRule.setContent {
            CustomAction(
                label = true,
                icon = R.drawable.ic_kaleyra_call_sheet_error,
                buttonTexts = CustomCallAction.ButtonTexts("test", null),
                onClick = {}
            )
        }
        composeTestRule.onNodeWithText("test").assertIsDisplayed()
    }

    @Test
    fun testButtonTextIsDisplayedOnWideWidth() {
        composeTestRule.setContent {
            CustomAction(
                modifier = Modifier.width(200.dp),
                icon = R.drawable.ic_kaleyra_call_sheet_error,
                buttonTexts = CustomCallAction.ButtonTexts("test", null),
                onClick = {}
            )
        }
        composeTestRule.onNodeWithText("test").assertIsDisplayed()
    }

    @Test
    fun testOnClickInvoked() {
        var clicked = false
        composeTestRule.setContent {
            CustomAction(
                icon = R.drawable.ic_kaleyra_call_sheet_error,
                buttonTexts = CustomCallAction.ButtonTexts("test", null),
                onClick = { clicked = true }
            )
        }
        composeTestRule.onNodeWithContentDescription("test").assertIsEnabled()
        composeTestRule.onNodeWithContentDescription("test").performClick()
        Assert.assertEquals(true, clicked)
    }

    @Test
    fun testClickOnButtonDisabled() {
        composeTestRule.setContent {
            CustomAction(
                enabled = false,
                icon = R.drawable.ic_kaleyra_call_sheet_error,
                buttonTexts = CustomCallAction.ButtonTexts("test", null),
                onClick = { }
            )
        }
        composeTestRule.onNodeWithContentDescription("test").assertIsNotEnabled()
    }
}