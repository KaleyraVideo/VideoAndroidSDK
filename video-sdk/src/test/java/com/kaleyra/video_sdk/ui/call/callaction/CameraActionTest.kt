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
import com.kaleyra.video_sdk.call.callactions.view.CameraAction
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
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
        val badgeDescr = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_description_camera_warning)
        composeTestRule.setContent {
            CameraAction(
                onCheckedChange = {},
                checked = false,
                warning = true
            )
        }
        composeTestRule.onNodeWithContentDescription(badgeDescr).assertIsDisplayed()
    }

    @Test
    fun testErrorBadge() {
        val badgeDescr = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_description_camera_error)
        composeTestRule.setContent {
            CameraAction(
                onCheckedChange = {},
                checked = false,
                error = true
            )
        }
        composeTestRule.onNodeWithContentDescription(badgeDescr).assertIsDisplayed()
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