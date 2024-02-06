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

package com.kaleyra.video_sdk.call.callactionsm3

import android.content.pm.ActivityInfo
import androidx.activity.ComponentActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4

import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.call.callactions.model.CallAction
import com.kaleyra.video_sdk.call.callactionsm3.view.CallActionLabelM3
import com.kaleyra.video_sdk.call.callactionsm3.view.CallActionM3
import com.kaleyra.video_sdk.call.callactionsm3.view.CallActionM3Configuration
import com.kaleyra.video_sdk.call.callactionsm3.view.OrientationAwareComponent
import com.kaleyra.video_sdk.call.callactionsm3.view.callActionToggleableTestTag
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ClickableCallActionM3Test {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private var action by mutableStateOf<CallAction>(CallAction.Audio())

    private var hasClicked by mutableStateOf(false)

    @Before
    fun setUp() {
        composeTestRule.setContent {
            Column {
                CallActionM3(configuration = CallActionM3Configuration.Clickable(action = action, onClick = { hasClicked = true }))
            }
        }
    }

    @After
    fun tearDown() {
        action = CallAction.Audio()
        hasClicked = false
    }

    @Test
    fun switchCameraAction_switchCameraActionTextAndIconDisplayed() {
        action = CallAction.SwitchCamera()
        val switchCamera = composeTestRule.activity.getString(R.string.kaleyra_call_action_switch_camera)
        val switchCameraDesc = composeTestRule.activity.getString(R.string.kaleyra_call_action_switch_camera_description)
        composeTestRule.onNodeWithText(switchCamera).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(switchCameraDesc).assertIsDisplayed()
    }

    @Test
    fun hangUpAction_hangUpActionTextAndIconDisplayed() {
        action = CallAction.HangUp()
        val hangUp = composeTestRule.activity.getString(R.string.kaleyra_call_hangup)
        composeTestRule.onNodeWithText(hangUp).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(hangUp).assertIsDisplayed()
    }

    @Test
    fun chatAction_chatItemActionTextAndIconDisplayed() {
        action = CallAction.Chat()
        val chat = composeTestRule.activity.getString(R.string.kaleyra_call_action_chat)
        composeTestRule.onNodeWithText(chat).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(chat).assertIsDisplayed()
    }

    @Test
    fun whiteboardAction_whiteboardActionTextAndIconDisplayed() {
        action = CallAction.Whiteboard()
        val whiteboard = composeTestRule.activity.getString(R.string.kaleyra_call_action_whiteboard)
        composeTestRule.onNodeWithText(whiteboard).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(whiteboard).assertIsDisplayed()
    }

    @Test
    fun fileShareAction_fileShareActionTextAndIconDisplayed() {
        action = CallAction.FileShare()
        val fileShare = composeTestRule.activity.getString(R.string.kaleyra_call_action_file_share)
        composeTestRule.onNodeWithText(fileShare).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(fileShare).assertIsDisplayed()
    }

    @Test
    fun audioAction_audioActionTextAndIconDisplayed() {
        action = CallAction.Audio()
        val audioRoute = composeTestRule.activity.getString(R.string.kaleyra_call_action_audio_route)
        composeTestRule.onNodeWithText(audioRoute).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(audioRoute).assertIsDisplayed()
    }

    @Test
    fun userPerformsClick_onClickedInvoked() {
        action = CallAction.Audio()
        val audioActionDescription = composeTestRule.activity.getString(R.string.kaleyra_call_action_audio_route)
        composeTestRule.onNodeWithContentDescription(audioActionDescription).performClick()
        Assert.assertEquals(true, hasClicked)
    }
}
