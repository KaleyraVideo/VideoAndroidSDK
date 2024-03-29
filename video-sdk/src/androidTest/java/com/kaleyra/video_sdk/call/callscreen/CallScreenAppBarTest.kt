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

package com.kaleyra.video_sdk.call.callscreen

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4

import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.call.screen.view.CallScreenAppBar
import com.kaleyra.video_sdk.call.bottomsheet.BottomSheetComponent
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class CallScreenAppBarTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private var sheetComponent by mutableStateOf(BottomSheetComponent.CallActions)

    private var isBackPressed = false

    @Before
    fun setUp() {
        composeTestRule.setContent {
            CallScreenAppBar(
                currentSheetComponent = sheetComponent,
                visible = true,
                onBackPressed = { isBackPressed = true }
            )
        }
        isBackPressed = false
    }

    @Test
    fun whiteboardComponent_whiteboardAppBarDisplayed() {
        sheetComponent = BottomSheetComponent.Whiteboard
        val whiteboard = composeTestRule.activity.getString(R.string.kaleyra_whiteboard)
        composeTestRule.onNodeWithText(whiteboard).assertIsDisplayed()
    }

    @Test
    fun fileShareComponent_fileShareAppBarDisplayed() {
        sheetComponent = BottomSheetComponent.FileShare
        val fileShare = composeTestRule.activity.getString(R.string.kaleyra_fileshare)
        composeTestRule.onNodeWithText(fileShare).assertIsDisplayed()
    }

    @Test
    fun whiteboardComponent_userClicksClose_onBackPressedInvoked() {
        userClicksClose_onBackPressedInvoked(BottomSheetComponent.Whiteboard)
    }

    @Test
    fun fileShareComponent_userClicksClose_onBackPressedInvoked() {
        userClicksClose_onBackPressedInvoked(BottomSheetComponent.FileShare)
    }

    private fun userClicksClose_onBackPressedInvoked(component: BottomSheetComponent) {
        sheetComponent = component
        val close = composeTestRule.activity.getString(R.string.kaleyra_close)
        composeTestRule.onNodeWithContentDescription(close).performClick()
        assert(isBackPressed)
    }
}