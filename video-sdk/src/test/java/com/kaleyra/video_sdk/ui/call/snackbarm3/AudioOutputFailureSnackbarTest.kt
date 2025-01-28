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

package com.kaleyra.video_sdk.ui.call.snackbarm3

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.common.snackbar.view.AudioOutputGenericFailureSnackbarM3
import com.kaleyra.video_sdk.common.snackbar.view.AudioOutputInSystemCallFailureSnackbarM3
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AudioOutputOutputFailureSnackbarM3Test {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun testGenericAudioFailureSnackbar() {
        composeTestRule.setContent { AudioOutputGenericFailureSnackbarM3({}) }
        val text = composeTestRule.activity.resources.getString(R.string.kaleyra_generic_audio_routing_error)
        composeTestRule.onNodeWithText(text).assertIsDisplayed()
    }

    @Test
    fun tesInSystemCallAudioFailureSnackbar() {
        composeTestRule.setContent { AudioOutputInSystemCallFailureSnackbarM3({}) }
        val text = composeTestRule.activity.resources.getString(R.string.kaleyra_already_in_system_call_audio_routing_error)
        composeTestRule.onNodeWithText(text).assertIsDisplayed()
    }

    @Test
    fun testGenericAudioFailureSnackbarDismissCLicked() {
        var onDismiss = false
        composeTestRule.setContent { AudioOutputGenericFailureSnackbarM3({ onDismiss = true }) }
        val close = composeTestRule.activity.resources.getString(R.string.kaleyra_close)
        composeTestRule.onNodeWithContentDescription(close).performClick()
        composeTestRule.waitForIdle()
        Assert.assertEquals(true, onDismiss)
    }

    @Test
    fun tesInSystemCallAudioFailureSnackbarDismissClicked() {
        var onDismiss = false
        composeTestRule.setContent { AudioOutputInSystemCallFailureSnackbarM3({ onDismiss = true }) }
        val close = composeTestRule.activity.resources.getString(R.string.kaleyra_close)
        composeTestRule.onNodeWithContentDescription(close).performClick()
        composeTestRule.waitForIdle()
        Assert.assertEquals(true, onDismiss)
    }
}
