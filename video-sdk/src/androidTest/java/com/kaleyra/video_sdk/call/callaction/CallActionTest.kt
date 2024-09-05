package com.kaleyra.video_sdk.call.callaction

import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.call.callactions.view.CallAction
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class CallActionTest {

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
    fun buttonTextIsDisplayed_buttonHasNoContentDescription() {
        val contentDescrText = "contentDescrText"
        val buttonText = "text"
        composeTestRule.setContent {
            CallAction(
                modifier = Modifier.width(200.dp),
                icon = painterResource(id = R.drawable.ic_kaleyra_mic_on),
                contentDescription = "contentDescrText",
                buttonText = buttonText,
                onClick = {}
            )
        }
        composeTestRule.onNodeWithText(buttonText).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(contentDescrText).assertDoesNotExist()
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
        composeTestRule.onNodeWithContentDescription(descr).assertIsEnabled()
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
                badgeText = badgeText,
            )
        }
        composeTestRule.onNodeWithText(badgeText).assertIsDisplayed()
    }

    @Test
    fun testClickOnButtonDisabled() {
        val descr = "enable mic"
        composeTestRule.setContent {
            CallAction(
                icon = painterResource(id = R.drawable.ic_kaleyra_mic_on),
                contentDescription = descr,
                enabled = false,
                onClick = { }
            )
        }
        composeTestRule.onNodeWithContentDescription(descr).assertIsNotEnabled()
    }
}