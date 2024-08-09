/*
 * Copyright 2023 Kaleyra @ https://www.kaleyra.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kaleyra.video_sdk.call.snackbarm3

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.common.snackbar.view.UsbConnectedSnackbarM3
import com.kaleyra.video_sdk.common.snackbar.view.UsbDisconnectedSnackbarM3
import com.kaleyra.video_sdk.common.snackbar.view.UsbNotSupportedSnackbarM3
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UsbCameraSnackbarM3Test {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun testGenericUsbConnectedSnackbar() {
        composeTestRule.setContent { UsbConnectedSnackbarM3("", {}) }
        val title = composeTestRule.activity.getString(R.string.kaleyra_generic_external_camera_connected)
        composeTestRule.onNodeWithText(title).assertIsDisplayed()
    }

    @Test
    fun testUsbConnectedSnackbar() {
        composeTestRule.setContent { UsbConnectedSnackbarM3("name", {}) }
        val title = composeTestRule.activity.getString(R.string.kaleyra_external_camera_connected, "name")
        composeTestRule.onNodeWithText(title).assertIsDisplayed()
    }

    @Test
    fun testUsbDisconnectedSnackbar() {
        composeTestRule.setContent { UsbDisconnectedSnackbarM3({}) }
        val title = composeTestRule.activity.getString(R.string.kaleyra_external_camera_disconnected)
        composeTestRule.onNodeWithText(title).assertIsDisplayed()
    }

    @Test
    fun testUsbNotSupportedSnackbar() {
        composeTestRule.setContent { UsbNotSupportedSnackbarM3({}) }
        val title = composeTestRule.activity.getString(R.string.kaleyra_external_camera_unsupported)
        composeTestRule.onNodeWithText(title).assertIsDisplayed()
    }

    @Test
    fun testUsbConnectedSnackbarDismissClicked() {
        var dismissClicked = false
        composeTestRule.setContent { UsbConnectedSnackbarM3("name", { dismissClicked = true }) }
        val close = composeTestRule.activity.getString(R.string.kaleyra_close)
        composeTestRule.onNodeWithContentDescription(close).performClick()
        Assert.assertEquals(true, dismissClicked)
    }

    @Test
    fun testUsbDisconnectedSnackbarDismissClicked() {
        var dismissClicked = false
        composeTestRule.setContent { UsbDisconnectedSnackbarM3({ dismissClicked = true }) }
        val close = composeTestRule.activity.getString(R.string.kaleyra_close)
        composeTestRule.onNodeWithContentDescription(close).performClick()
        Assert.assertEquals(true, dismissClicked)
    }

    @Test
    fun testUsbNotSupportedSnackbarDismissClicked() {
        var dismissClicked = false
        composeTestRule.setContent { UsbNotSupportedSnackbarM3({ dismissClicked = true }) }
        val close = composeTestRule.activity.getString(R.string.kaleyra_close)
        composeTestRule.onNodeWithContentDescription(close).performClick()
        Assert.assertEquals(true, dismissClicked)
    }
}