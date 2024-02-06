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

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4

import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.call.callactions.model.CallAction
import com.kaleyra.video_sdk.call.callactionsm3.view.CallActionM3
import com.kaleyra.video_sdk.call.callactionsm3.view.CallActionM3Configuration
import com.kaleyra.video_sdk.call.callactionsm3.view.callActionToggleableTestTag
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ToggleableallActionM3Test {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private var toggleableAction by mutableStateOf<CallAction.Toggleable>(CallAction.Microphone(isEnabled = true, isToggled = false))

    private var isToggled by mutableStateOf(false)

    @Before
    fun setUp() {
        composeTestRule.setContent {
            Column {
                CallActionM3(buttonWidth = 48.dp, containerWidth = 96.dp, configuration = CallActionM3Configuration.Toggleable(action = toggleableAction, onToggle = { isToggled = it }))
            }
        }
    }

    @After
    fun tearDown() {
        toggleableAction = CallAction.Microphone()
        isToggled = false
    }

    @Test
    fun buttonIsToggleable() {
        composeTestRule.onRoot().onChildAt(0).assertIsToggleable()
    }

    @Test
    fun cameraAction_cameraActionTextAndIconDisplayed() {
        toggleableAction = CallAction.Camera()
        val disableVideo = composeTestRule.activity.getString(R.string.kaleyra_call_action_video_disable)
        val disableVideoDesc = composeTestRule.activity.getString(R.string.kaleyra_call_action_disable_camera_description)
        composeTestRule.onNodeWithText(disableVideo).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(disableVideoDesc).assertIsDisplayed()
    }

    @Test
    fun microphoneAction_micActionTextAndIconDisplayed() {
        toggleableAction = CallAction.Microphone()
        val muteMic = composeTestRule.activity.getString(R.string.kaleyra_call_action_mic_mute)
        val muteMicDesc = composeTestRule.activity.getString(R.string.kaleyra_call_action_disable_mic_description)
        composeTestRule.onNodeWithText(muteMic).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(muteMicDesc).assertIsDisplayed()
    }

    @Test
    fun screenShareAction_screenShareActionTextAndIconDisplayed() {
        toggleableAction = CallAction.ScreenShare()
        val screenShare = composeTestRule.activity.getString(R.string.kaleyra_call_action_screen_share)
        composeTestRule.onNodeWithText(screenShare).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(screenShare).assertIsDisplayed()
    }

    @Test
    fun virtualBackgroundAction_virtualBackgroundActionTextAndIconDisplayed() {
        toggleableAction = CallAction.VirtualBackground()
        val virtualBackground = composeTestRule.activity.getString(R.string.kaleyra_call_action_virtual_background)
        composeTestRule.onNodeWithText(virtualBackground).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(virtualBackground).assertIsDisplayed()
    }

    @Test
    fun userPerformsClick_onToggledInvoked() {
        toggleableAction = CallAction.Microphone(isEnabled = true, isToggled = false)
        composeTestRule.onNodeWithTag(callActionToggleableTestTag).performClick()
        Assert.assertEquals(true, isToggled)
    }

    @Test
    fun isToggledTrue_actionIsToggled() {
        toggleableAction = CallAction.Microphone(isToggled = false)
        composeTestRule.onRoot().onChildAt(0).assertIsOff()
        toggleableAction = CallAction.Microphone(isToggled = true)
        composeTestRule.onRoot().onChildAt(0).assertIsOn()
    }

    @Test
    fun isEnabledFalse_actionDisabled() {
        toggleableAction = CallAction.Microphone(isEnabled = true)
        composeTestRule.onRoot().onChildAt(0).assertIsEnabled()
        toggleableAction = CallAction.Microphone(isEnabled = false)
        composeTestRule.onRoot().onChildAt(0).assertIsNotEnabled()
    }
}