package com.kaleyra.video_sdk.call.callactionnew

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
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
import org.junit.Assert
import org.junit.Rule
import org.junit.Test

class CameraActionTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun testOnCheckedChangeTrue() {
        val descr = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_description_disable_camera)
        var checked = false
        composeTestRule.setContent {
            CameraAction(
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
        val descr = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_description_enable_camera)
        var checked = true
        composeTestRule.setContent {
            CameraAction(
                onCheckedChange = { checked = it },
                checked = true
            )
        }
        composeTestRule.onNodeWithContentDescription(descr).assertIsEnabled()
        composeTestRule.onNodeWithContentDescription(descr).performClick()
        Assert.assertEquals(false, checked)
    }

    @Test
    fun testWarningBadge() {
        val badgeText = "!"
        composeTestRule.setContent {
            CameraAction(
                onCheckedChange = {},
                checked = false,
                warning = true
            )
        }
        composeTestRule.onNodeWithText(badgeText).assertIsDisplayed()
    }

    @Test
    fun testErrorBadge() {
        val badgeText = "!"
        composeTestRule.setContent {
            CameraAction(
                onCheckedChange = {},
                checked = false,
                error = true
            )
        }
        composeTestRule.onNodeWithText(badgeText).assertIsDisplayed()
    }

    @Test
    fun testClickOnButtonDisabled() {
        val descr = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_description_enable_camera)
        composeTestRule.setContent {
            CameraAction(
                enabled = false,
                checked = true,
                onCheckedChange = { }
            )
        }
        composeTestRule.onNodeWithContentDescription(descr).assertIsNotEnabled()
    }
}