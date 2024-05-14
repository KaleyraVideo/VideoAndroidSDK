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

package com.kaleyra.video_sdk.ui.call.bottomsheetnew.sheetpanel

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.call.bottomsheetnew.sheetpanel.SheetPanelContent
import com.kaleyra.video_sdk.call.screennew.AudioAction
import com.kaleyra.video_sdk.call.screennew.CallActionUI
import com.kaleyra.video_sdk.call.screennew.ChatAction
import com.kaleyra.video_sdk.call.screennew.FileShareAction
import com.kaleyra.video_sdk.call.screennew.FlipCameraAction
import com.kaleyra.video_sdk.call.screennew.HangUpAction
import com.kaleyra.video_sdk.call.screennew.VirtualBackgroundAction
import com.kaleyra.video_sdk.call.screennew.WhiteboardAction
import com.kaleyra.video_sdk.call.screenshare.model.ScreenShareTargetUi
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ScreenPanelContentTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private var items by mutableStateOf(ImmutableList(listOf<CallActionUI>()))

    private var callAction: CallActionUI? = null

    @Before
    fun setUp() {
        composeTestRule.setContent {
            SheetPanelContent(
                items = items,
                onItemClick = { callAction = it }
            )
        }
        callAction = null
    }

    @After
    fun tearDown() {
        items = ImmutableList(listOf())
        callAction = null
    }

    @Test
    fun flipCameraAction_flipCameraItemIsDisplayed() {
        items = ImmutableList(listOf(FlipCameraAction()))
        val text = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_flip_camera)
        composeTestRule.onNodeWithText(text).assertIsDisplayed()
    }

    @Test
    fun audioAction_audioItemIsDisplayed() {
        items = ImmutableList(listOf(AudioAction()))
        val text = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_audio)
        composeTestRule.onNodeWithText(text).assertIsDisplayed()
    }

    @Test
    fun chatAction_chatItemIsDisplayed() {
        items = ImmutableList(listOf(ChatAction()))
        val text = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_chat)
        composeTestRule.onNodeWithText(text).assertIsDisplayed()
    }

    @Test
    fun fileShareAction_fileShareItemIsDisplayed() {
        items = ImmutableList(listOf(FileShareAction()))
        val text = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_file_share)
        composeTestRule.onNodeWithText(text).assertIsDisplayed()
    }

    @Test
    fun whiteboardAction_whiteboardItemIsDisplayed() {
        items = ImmutableList(listOf(WhiteboardAction()))
        val text = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_whiteboard)
        composeTestRule.onNodeWithText(text).assertIsDisplayed()
    }

    @Test
    fun virtualBackgroundAction_virtualBackgroundItemIsDisplayed() {
        items = ImmutableList(listOf(VirtualBackgroundAction()))
        val text =
            composeTestRule.activity.getString(R.string.kaleyra_call_sheet_virtual_background)
        composeTestRule.onNodeWithText(text).assertIsDisplayed()
    }

    @Test
    fun userClicksOnItem_onItemClickInvoked() {
        val action = ChatAction()
        items = ImmutableList(
            listOf(
                FlipCameraAction(),
                AudioAction(),
                action,
                FileShareAction(),
                WhiteboardAction(),
                VirtualBackgroundAction()
            )
        )
        val text = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_chat)
        composeTestRule.onNodeWithText(text).performClick()
        Assert.assertEquals(action, callAction)
    }
}