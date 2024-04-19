package com.kaleyra.video_sdk.call.callactionnew

import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import com.kaleyra.video_sdk.R
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class CallActionTest {

    // TODO test enabled, update badge test

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testLabelIsDisplayed() {
        val labelText = "labelText"
        composeTestRule.setContent {
            CallAction(
                icon = painterResource(id = R.drawable.ic_kaleyra_mic_on),
                contentDescription = "",
                label = labelText,
                onClick = {}
            )
        }
        composeTestRule.onNodeWithText(labelText).assertIsDisplayed()
    }

    @Test
    fun buttonHasEnoughSpace_buttonTextIsDisplayed() {
        val buttonText = "text"
        composeTestRule.setContent {
            CallAction(
                modifier = Modifier.width(200.dp),
                icon = painterResource(id = R.drawable.ic_kaleyra_mic_on),
                contentDescription = "",
                buttonText = buttonText,
                onClick = {}
            )
        }
        composeTestRule.onNodeWithText(buttonText).assertIsDisplayed()
    }

    @Test
    fun buttonTextIsDisplayed_labelDoesNotExist() {
        val buttonText = "text"
        val labelText = "labelText"
        composeTestRule.setContent {
            CallAction(
                modifier = Modifier.width(200.dp),
                icon = painterResource(id = R.drawable.ic_kaleyra_mic_on),
                contentDescription = "",
                buttonText = buttonText,
                label = labelText,
                onClick = {}
            )
        }
        composeTestRule.onNodeWithText(buttonText).assertIsDisplayed()
        composeTestRule.onNodeWithText(labelText).assertDoesNotExist()
    }

    @Test
    fun buttonTextHasNotEnoughSpace_buttonTextDoesNotExits() {
        val buttonText = "text"
        composeTestRule.setContent {
            CallAction(
                modifier = Modifier.width(48.dp),
                icon = painterResource(id = R.drawable.ic_kaleyra_mic_on),
                contentDescription = "",
                buttonText = buttonText,
                onClick = {}
            )
        }
        composeTestRule.onNodeWithText(buttonText).assertDoesNotExist()
    }

    @Test
    fun buttonTextHasNotEnoughSpace_labelIsDisplayed() {
        val buttonText = "text"
        val labelText = "labelText"
        composeTestRule.setContent {
            CallAction(
                modifier = Modifier.width(48.dp),
                icon = painterResource(id = R.drawable.ic_kaleyra_mic_on),
                contentDescription = "",
                buttonText = buttonText,
                label = labelText,
                onClick = {}
            )
        }
        composeTestRule.onNodeWithText(buttonText).assertDoesNotExist()
        composeTestRule.onNodeWithText(labelText).assertIsDisplayed()
    }

    @Test
    fun textButtonContentDescription() {
        val descr = "enable mic"
        composeTestRule.setContent {
            CallAction(
                icon = painterResource(id = R.drawable.ic_kaleyra_mic_on),
                contentDescription = descr,
                onClick = {}
            )
        }
        composeTestRule.onNodeWithContentDescription(descr).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(descr).assertHasClickAction()
    }

    @Test
    fun testOnClickInvoked() {
        val descr = "enable mic"
        var clicked = false
        composeTestRule.setContent {
            CallAction(
                icon = painterResource(id = R.drawable.ic_kaleyra_mic_on),
                contentDescription = descr,
                onClick = { clicked = true }
            )
        }
        composeTestRule.onNodeWithContentDescription(descr).performClick()
        assertEquals(true, clicked)
    }

    @Test
    fun testBadgeIsDisplayed() {
        val badgeText = "badgeText"
        composeTestRule.setContent {
            CallAction(
                icon = painterResource(id = R.drawable.ic_kaleyra_mic_on),
                contentDescription = "",
                onClick = {},
                badge = { Text(badgeText) }
            )
        }
        composeTestRule.onNodeWithText(badgeText).assertIsDisplayed()
    }
}