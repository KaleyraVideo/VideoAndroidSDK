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

package com.kaleyra.video_sdk.call.signature

import androidx.activity.ComponentActivity
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.call.signature.view.SignDocumentsAppBar
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class SignDocumentsAppBarTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private var isLargeScreen by mutableStateOf(false)

    private var isBackPressed = false

    @Before
    fun setUp() {
        composeTestRule.setContent {
            SignDocumentsAppBar(
                onBackPressed = { isBackPressed = true },
                lazyGridState = rememberLazyGridState(),
                isLargeScreen = isLargeScreen
            )
        }
    }

    @After
    fun tearDown() {
        isLargeScreen = false
        isBackPressed = false
    }

    @Test
    fun userClicksCollapse_backPressedInvoked() {
        val close = composeTestRule.activity.getString(R.string.kaleyra_close)
        composeTestRule.onNodeWithContentDescription(close).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(close).performClick()
        assert(isBackPressed)
    }

    @Test
    fun fileShareTextDisplayed() {
        val fileShare = composeTestRule.activity.getString(R.string.kaleyra_signature_sign)
        composeTestRule.onNodeWithText(fileShare).assertIsDisplayed()
    }

    @Test
    fun userClickCloseOnLargeScreen_backPressedInvoked() {
        isLargeScreen = true
        val close = composeTestRule.activity.getString(R.string.kaleyra_close)
        composeTestRule.onNodeWithContentDescription(close).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(close).performClick()
        assert(isBackPressed)
    }
}