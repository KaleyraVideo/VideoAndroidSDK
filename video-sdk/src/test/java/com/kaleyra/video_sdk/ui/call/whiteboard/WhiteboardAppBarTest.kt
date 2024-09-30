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

package com.kaleyra.video_sdk.ui.call.whiteboard

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.call.whiteboard.view.WhiteboardAppBar
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner


@RunWith(RobolectricTestRunner::class)
class WhiteboardAppBarTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private var embeddedComponent by mutableStateOf(false)

    private var isFileSharingSupported by mutableStateOf(true)

    private var isBackPressed = false

    private var isUploadClicked = false

    @Before
    fun setUp() {
        composeTestRule.setContent {
            WhiteboardAppBar(
                isFileSharingSupported = isFileSharingSupported,
                onBackPressed = { isBackPressed = true },
                onUploadClick = { isUploadClicked = true },
                embeddedComponent = embeddedComponent
            )
        }
    }

    @After
    fun tearDown() {
        isFileSharingSupported = true
        isBackPressed = false
        isUploadClicked = false
        embeddedComponent = false
    }

    @Test
    fun isFileSharingSupportedFalse_uploadButtonDoesNotExists() {
        isFileSharingSupported = false
        val upload = composeTestRule.activity.getString(R.string.kaleyra_upload_file)
        composeTestRule.onNodeWithContentDescription(upload).assertDoesNotExist()
    }

    @Test
    fun isFileSharingSupportedTrue_uploadButtonIsDisplayed() {
        isFileSharingSupported = true
        val upload = composeTestRule.activity.getString(R.string.kaleyra_upload_file)
        composeTestRule.onNodeWithContentDescription(upload).assertHasClickAction()
        composeTestRule.onNodeWithContentDescription(upload).assertIsDisplayed()
    }

    @Test
    fun userClicksCollapse_backPressedInvoked() {
        val close = composeTestRule.activity.getString(R.string.kaleyra_close)
        composeTestRule.onNodeWithContentDescription(close).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(close).performClick()
        assert(isBackPressed)
    }

    @Test
    fun userClicksUpload_uploadClickInvoked() {
        val upload = composeTestRule.activity.getString(R.string.kaleyra_upload_file)
        composeTestRule.onNodeWithContentDescription(upload).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(upload).performClick()
        assert(isUploadClicked)
    }

    @Test
    fun fileShareTextDisplayed() {
        val whiteboard = composeTestRule.activity.getString(R.string.kaleyra_whiteboard)
        composeTestRule.onNodeWithText(whiteboard).assertIsDisplayed()
    }

    @Test
    fun embeddedComponentUserClickClose_backPressedInvoked() {
        embeddedComponent = true
        val close = composeTestRule.activity.getString(R.string.kaleyra_close)
        composeTestRule.onNodeWithContentDescription(close).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(close).performClick()
        assert(isBackPressed)
    }
}