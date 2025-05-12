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

package com.kaleyra.video_sdk.ui.call.signature

import androidx.activity.ComponentActivity
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.call.appbar.view.SearchInputTag
import com.kaleyra.video_sdk.call.signature.view.SignDocumentsAppBar
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class SignDocumentsAppBarTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private var isLargeScreen by mutableStateOf(false)

    private var isBackPressed = false

    private var queryString = ""

    @Before
    fun setUp() {
        composeTestRule.setContent {
            SignDocumentsAppBar(
                onBackPressed = { isBackPressed = true },
                lazyGridState = rememberLazyGridState(),
                isLargeScreen = isLargeScreen,
                enableSearch = true,
                onSearch = { queryString = it }
            )
        }
    }

    @After
    fun tearDown() {
        isLargeScreen = false
        isBackPressed = false
        queryString = ""
    }

    @Test
    fun userClicksCollapse_backPressedInvoked() {
        val close = composeTestRule.activity.getString(R.string.kaleyra_strings_action_close)
        composeTestRule.onNodeWithContentDescription(close).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(close).performClick()
        assert(isBackPressed)
    }

    @Test
    fun signTextDisplayed() {
        val sign = composeTestRule.activity.getString(R.string.kaleyra_signature_sign)
        composeTestRule.onNodeWithText(sign).assertIsDisplayed()
    }

    @Test
    fun searchIconDisplayed() {
        val searchDescription = composeTestRule.activity.getString(R.string.kaleyra_strings_action_search)
        composeTestRule.onNodeWithContentDescription(searchDescription).assertIsDisplayed()
    }

    @Test
    fun searchIconClicked_searchBarDisplayed() {
        val searchDescription = composeTestRule.activity.getString(R.string.kaleyra_strings_action_search)
        composeTestRule.onNodeWithContentDescription(searchDescription).performClick()
        composeTestRule.onNodeWithText(searchDescription).assertIsDisplayed()
    }

    @Test
    fun searchBarDisplayed_textInputInserted_onSearchCalled() {
        val searchDescription = composeTestRule.activity.getString(R.string.kaleyra_strings_action_search)
        composeTestRule.onNodeWithContentDescription(searchDescription).performClick()
        val query = "test"
        composeTestRule.onNodeWithText(searchDescription).performTextInput(query)
        Assert.assertEquals(query, queryString)
    }

    @Test
    fun textInputInserted_clearQueryClicked_queryTextCleared() {
        val searchDescription = composeTestRule.activity.getString(R.string.kaleyra_strings_action_search)
        val clearDescription = composeTestRule.activity.getString(R.string.kaleyra_strings_action_clear)
        composeTestRule.onNodeWithContentDescription(searchDescription).performClick()
        val query = "test"
        composeTestRule.onNodeWithText(searchDescription).performTextInput(query)
        composeTestRule.onNodeWithContentDescription(clearDescription).performClick()
        Assert.assertTrue(queryString.isEmpty())
    }

    @Test
    fun textInputInserted_backPressed_queryTextCleared() {
        val searchDescription = composeTestRule.activity.getString(R.string.kaleyra_strings_action_search)
        val closeDescription = composeTestRule.activity.getString(R.string.kaleyra_strings_action_close)
        composeTestRule.onNodeWithContentDescription(searchDescription).performClick()
        val query = "test"
        composeTestRule.onNodeWithText(searchDescription).performTextInput(query)
        composeTestRule.onNodeWithContentDescription(closeDescription).performClick()
        Assert.assertTrue(queryString.isEmpty())
    }

    @Test
    fun searchBarShown_backPressed_searchBarHidden() {
        val searchDescription = composeTestRule.activity.getString(R.string.kaleyra_strings_action_search)
        val closeDescription = composeTestRule.activity.getString(R.string.kaleyra_strings_action_close)
        composeTestRule.onNodeWithContentDescription(searchDescription).performClick()
        val query = "test"
        composeTestRule.onNodeWithText(searchDescription).performTextInput(query)
        composeTestRule.onNodeWithContentDescription(closeDescription).performClick()
        composeTestRule.onNodeWithTag(SearchInputTag).assertIsNotDisplayed()
    }

    @Test
    fun userClickCloseOnLargeScreen_backPressedInvoked() {
        isLargeScreen = true
        val close = composeTestRule.activity.getString(R.string.kaleyra_strings_action_close)
        composeTestRule.onNodeWithContentDescription(close).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(close).performClick()
        assert(isBackPressed)
    }
}