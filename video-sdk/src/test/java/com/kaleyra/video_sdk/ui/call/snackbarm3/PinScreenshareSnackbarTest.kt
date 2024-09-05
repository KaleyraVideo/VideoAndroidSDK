package com.kaleyra.video_sdk.ui.call.snackbarm3

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.common.snackbar.view.PinScreenshareSnackbarM3
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class PinScreenshareSnackbarTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun testPinScreenshareSnackbar() {
        val userDisplayName = "userDisplayName"
        composeTestRule.setContent { PinScreenshareSnackbarM3(userDisplayName, {}) }
        val message = composeTestRule.activity.resources.getString(R.string.kaleyra_stream_screenshare_received, userDisplayName)
        val pin = composeTestRule.activity.resources.getString(R.string.kaleyra_participants_component_pin)
        composeTestRule.onNodeWithText(message).assertIsDisplayed()
        composeTestRule.onNodeWithText(pin).assertIsDisplayed()
    }

    @Test
    fun testPinScreenshareSnackbarDismissNotShown() {
        val userDisplayName = "userDisplayName"
        composeTestRule.setContent { PinScreenshareSnackbarM3(userDisplayName, {}) }
        val close = composeTestRule.activity.resources.getString(R.string.kaleyra_close)
        composeTestRule.onNodeWithContentDescription(close).assertIsNotDisplayed()
    }

    @Test
    fun testPinScreenshareSnackbarActionClicked() {
        var pinClicked = false
        val userDisplayName = "userDisplayName"
        composeTestRule.setContent { PinScreenshareSnackbarM3(userDisplayName, { pinClicked = true }) }
        val pin = composeTestRule.activity.resources.getString(R.string.kaleyra_participants_component_pin)
        composeTestRule.onNodeWithText(pin).performClick()
        Assert.assertEquals(true, pinClicked)
    }
}