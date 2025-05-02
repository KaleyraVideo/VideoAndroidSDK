@file:OptIn(ExperimentalMaterial3Api::class)

package com.kaleyra.video_sdk.call.appbar

import androidx.activity.ComponentActivity
import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.kaleyra.video_sdk.call.appbar.view.ComponentAppBar
import com.kaleyra.video_sdk.call.appbar.view.SearchInputTag
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ComponentAppBarTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private var isLargeScreen by mutableStateOf(false)

    private var isBackPressed = false

    private var queryString = ""

    @Before
    fun setUp() {
        composeTestRule.setContent {
            ComponentAppBar(
                title = "title",
                onBackPressed = { isBackPressed = true },
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
        val close = composeTestRule.activity.getString(R.string.kaleyra_close)
        composeTestRule.onNodeWithContentDescription(close).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(close).performClick()
        assert(isBackPressed)
    }

    @Test
    fun titleDisplayed() {
        composeTestRule.onNodeWithText("title").assertIsDisplayed()
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
        val closeDescription = composeTestRule.activity.getString(R.string.kaleyra_close)
        composeTestRule.onNodeWithContentDescription(searchDescription).performClick()
        val query = "test"
        composeTestRule.onNodeWithText(searchDescription).performTextInput(query)
        composeTestRule.onNodeWithContentDescription(closeDescription).performClick()
        Assert.assertTrue(queryString.isEmpty())
    }

    @Test
    fun searchBarShown_backPressed_searchBarHidden() {
        val searchDescription = composeTestRule.activity.getString(R.string.kaleyra_strings_action_search)
        val closeDescription = composeTestRule.activity.getString(R.string.kaleyra_close)
        composeTestRule.onNodeWithContentDescription(searchDescription).performClick()
        val query = "test"
        composeTestRule.onNodeWithText(searchDescription).performTextInput(query)
        composeTestRule.onNodeWithContentDescription(closeDescription).performClick()
        composeTestRule.onNodeWithTag(SearchInputTag).assertIsNotDisplayed()
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