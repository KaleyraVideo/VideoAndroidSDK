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
import com.kaleyra.video_sdk.call.callactions.view.CallToggleAction
import org.junit.Assert
import org.junit.Rule
import org.junit.Test

class CallToggleActionTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testLabelIsDisplayed() {
        val labelText = "labelText"
        composeTestRule.setContent {
            CallToggleAction(
                icon = painterResource(id = R.drawable.ic_kaleyra_mic_on),
                contentDescription = "",
                label = labelText,
                onCheckedChange = {},
                checked = false
            )
        }
        composeTestRule.onNodeWithText(labelText).assertIsDisplayed()
    }

    @Test
    fun buttonHasEnoughSpace_buttonTextIsDisplayed() {
        val buttonText = "text"
        composeTestRule.setContent {
            CallToggleAction(
                modifier = Modifier.width(200.dp),
                icon = painterResource(id = R.drawable.ic_kaleyra_mic_on),
                contentDescription = "",
                buttonText = buttonText,
                onCheckedChange = {},
                checked = false
            )
        }
        composeTestRule.onNodeWithText(buttonText).assertIsDisplayed()
    }

    @Test
    fun buttonTextIsDisplayed_labelDoesNotExist() {
        val buttonText = "text"
        val labelText = "labelText"
        composeTestRule.setContent {
            CallToggleAction(
                modifier = Modifier.width(200.dp),
                icon = painterResource(id = R.drawable.ic_kaleyra_mic_on),
                contentDescription = "",
                buttonText = buttonText,
                label = labelText,
                onCheckedChange = {},
                checked = false
            )
        }
        composeTestRule.onNodeWithText(buttonText).assertIsDisplayed()
        composeTestRule.onNodeWithText(labelText).assertDoesNotExist()
    }

    @Test
    fun buttonTextHasNotEnoughSpace_buttonTextDoesNotExits() {
        val buttonText = "text"
        composeTestRule.setContent {
            CallToggleAction(
                modifier = Modifier.width(48.dp),
                icon = painterResource(id = R.drawable.ic_kaleyra_mic_on),
                contentDescription = "",
                buttonText = buttonText,
                onCheckedChange = {},
                checked = false
            )
        }
        composeTestRule.onNodeWithText(buttonText).assertDoesNotExist()
    }

    @Test
    fun buttonTextHasNotEnoughSpace_labelIsDisplayed() {
        val buttonText = "text"
        val labelText = "labelText"
        composeTestRule.setContent {
            CallToggleAction(
                modifier = Modifier.width(48.dp),
                icon = painterResource(id = R.drawable.ic_kaleyra_mic_on),
                contentDescription = "",
                buttonText = buttonText,
                label = labelText,
                onCheckedChange = {},
                checked = false
            )
        }
        composeTestRule.onNodeWithText(buttonText).assertDoesNotExist()
        composeTestRule.onNodeWithText(labelText).assertIsDisplayed()
    }

    @Test
    fun textButtonContentDescription() {
        val descr = "enable mic"
        composeTestRule.setContent {
            CallToggleAction(
                icon = painterResource(id = R.drawable.ic_kaleyra_mic_on),
                contentDescription = descr,
                onCheckedChange = {},
                checked = false
            )
        }
        composeTestRule.onNodeWithContentDescription(descr).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(descr).assertHasClickAction()
    }

    @Test
    fun testOnCheckedChangeTrue() {
        val descr = "enable mic"
        var checked = false
        composeTestRule.setContent {
            CallToggleAction(
                icon = painterResource(id = R.drawable.ic_kaleyra_mic_on),
                contentDescription = descr,
                onCheckedChange = { checked = it },
                checked = false
            )
        }
        composeTestRule.onNodeWithContentDescription(descr).assertIsEnabled()
        composeTestRule.onNodeWithContentDescription(descr).performClick()
        Assert.assertEquals(true, checked)
    }

    @Test
    fun testOnCheckedChangeFalse() {
        val descr = "enable mic"
        var checked = true
        composeTestRule.setContent {
            CallToggleAction(
                icon = painterResource(id = R.drawable.ic_kaleyra_mic_on),
                contentDescription = descr,
                onCheckedChange = { checked = it },
                checked = true
            )
        }
        composeTestRule.onNodeWithContentDescription(descr).assertIsEnabled()
        composeTestRule.onNodeWithContentDescription(descr).performClick()
        Assert.assertEquals(false, checked)
    }

    @Test
    fun testBadgeIsDisplayed() {
        val badgeText = "badgeText"
        composeTestRule.setContent {
            CallToggleAction(
                icon = painterResource(id = R.drawable.ic_kaleyra_mic_on),
                contentDescription = "",
                onCheckedChange = {},
                checked = false,
                badgeText = badgeText
            )
        }
        composeTestRule.onNodeWithText(badgeText).assertIsDisplayed()
    }

    @Test
    fun testClickOnButtonDisabled() {
        val descr = "enable mic"
        composeTestRule.setContent {
            CallToggleAction(
                icon = painterResource(id = R.drawable.ic_kaleyra_mic_on),
                contentDescription = descr,
                enabled = false,
                checked = false,
                onCheckedChange = { }
            )
        }
        composeTestRule.onNodeWithContentDescription(descr).assertIsNotEnabled()
    }
}