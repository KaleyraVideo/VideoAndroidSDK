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
import com.kaleyra.video_sdk.call.callactions.view.MicAction
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class MicActionTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun testOnCheckedChangeTrue() {
        val descr = composeTestRule.activity.getString(R.string.kaleyra_strings_info_disable_microphone)
        var checked = false
        composeTestRule.setContent {
            MicAction(
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
        val descr = composeTestRule.activity.getString(R.string.kaleyra_strings_info_enable_microphone)
        var checked = true
        composeTestRule.setContent {
            MicAction(
                onCheckedChange = { checked = it },
                checked = true
            )
        }
        composeTestRule.onNodeWithContentDescription(descr).assertIsEnabled()
        composeTestRule.onNodeWithContentDescription(descr).performClick()
        Assert.assertEquals(false, checked)
    }

    @Test
    fun micActionIsNotChecked_labelIsDisplayed() {
        val text = composeTestRule.activity.getString(R.string.kaleyra_call_action_mic_mute)
        composeTestRule.setContent {
            MicAction(
                label = true,
                checked = false,
                onCheckedChange = {}
            )
        }
        composeTestRule.onNodeWithText(text).assertIsDisplayed()
    }

    @Test
    fun micActionIsChecked_labelIsDisplayed() {
        val text = composeTestRule.activity.getString(R.string.kaleyra_call_action_mic_unmute)
        composeTestRule.setContent {
            MicAction(
                label = true,
                checked = true,
                onCheckedChange = {}
            )
        }
        composeTestRule.onNodeWithText(text).assertIsDisplayed()
    }

    @Test
    fun testWarningBadge() {
        val badgeDescr = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_description_mic_warning)
        composeTestRule.setContent {
            MicAction(
                onCheckedChange = {},
                checked = false,
                warning = true
            )
        }
        composeTestRule.onNodeWithContentDescription(badgeDescr).assertIsDisplayed()
    }

    @Test
    fun testErrorBadge() {
        val badgeDescr = with(composeTestRule.activity.resources) {
            getQuantityString(R.plurals.kaleyra_strings_info_hardware_permission_error,
                1,
                getString(R.string.kaleyra_strings_action_microphone))
        }
        composeTestRule.setContent {
            MicAction(
                onCheckedChange = {},
                checked = false,
                error = true
            )
        }
        composeTestRule.onNodeWithContentDescription(badgeDescr).assertIsDisplayed()
    }

    @Test
    fun testClickOnButtonDisabled() {
        val descr = composeTestRule.activity.getString(R.string.kaleyra_strings_info_enable_microphone)
        composeTestRule.setContent {
            MicAction(
                enabled = false,
                checked = true,
                onCheckedChange = { }
            )
        }
        composeTestRule.onNodeWithContentDescription(descr).assertIsNotEnabled()
    }
}