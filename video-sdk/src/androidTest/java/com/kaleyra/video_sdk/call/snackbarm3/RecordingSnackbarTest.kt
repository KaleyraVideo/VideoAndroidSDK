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
import com.kaleyra.video_sdk.common.snackbarm3.view.RecordingEndedSnackbarM3
import com.kaleyra.video_sdk.common.snackbarm3.view.RecordingErrorSnackbarM3
import com.kaleyra.video_sdk.common.snackbarm3.view.RecordingStartedSnackbarM3
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RecordingSnackbarM3Test {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun testRecordingStartedSnackbar() {
        composeTestRule.setContent { RecordingStartedSnackbarM3({}) }
        val message = composeTestRule.activity.getString(R.string.kaleyra_recording_started_message)
        composeTestRule.onNodeWithText(message).assertIsDisplayed()
    }

    @Test
    fun testRecordingEndedSnackbar() {
        composeTestRule.setContent { RecordingEndedSnackbarM3({}) }
        val message = composeTestRule.activity.getString(R.string.kaleyra_recording_stopped_message)
        composeTestRule.onNodeWithText(message).assertIsDisplayed()
    }

    @Test
    fun testRecordingErrorSnackbar() {
        composeTestRule.setContent { RecordingErrorSnackbarM3({}) }
        val message = composeTestRule.activity.getString(R.string.kaleyra_recording_failed_message)
        composeTestRule.onNodeWithText(message).assertIsDisplayed()
    }

    @Test
    fun testRecordingStartedSnackbarDismissClicked() {
        var dismissClicked = false
        composeTestRule.setContent { RecordingStartedSnackbarM3({ dismissClicked = true }) }
        val close = composeTestRule.activity.getString(R.string.kaleyra_close)
        composeTestRule.onNodeWithContentDescription(close).performClick()
        Assert.assertEquals(true, dismissClicked)
    }

    @Test
    fun testRecordingEndedSnackbarDismissClicked() {
        var dismissClicked = false
        composeTestRule.setContent { RecordingEndedSnackbarM3({ dismissClicked = true }) }
        val close = composeTestRule.activity.getString(R.string.kaleyra_close)
        composeTestRule.onNodeWithContentDescription(close).performClick()
        Assert.assertEquals(true, dismissClicked)
    }

    @Test
    fun testRecordingErrorSnackbarDismissClicked() {
        var dismissClicked = false
        composeTestRule.setContent { RecordingErrorSnackbarM3({ dismissClicked = true }) }
        val close = composeTestRule.activity.getString(R.string.kaleyra_close)
        composeTestRule.onNodeWithContentDescription(close).performClick()
        Assert.assertEquals(true, dismissClicked)
    }
}