package com.kaleyra.video_sdk.call.stream

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.call.stream.model.StreamItem
import com.kaleyra.video_sdk.call.stream.model.MoreStreamsUserPreview
import com.kaleyra.video_sdk.call.stream.view.items.MoreStreamsItem
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class MoreStreamsItemTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private var moreStreamsItem by mutableStateOf(StreamItem.MoreStreams(users = listOf()))

    @Before
    fun setUp() {
        composeTestRule.setContent {
            MoreStreamsItem(moreStreamsItem = moreStreamsItem)
        }
    }

    @After
    fun tearDown() {
        moreStreamsItem = StreamItem.MoreStreams(users = listOf())
    }

    @Test
    fun testAvatarIsDisplayed() {
        moreStreamsItem = StreamItem.MoreStreams(
            users = listOf(MoreStreamsUserPreview("1","john", null),)
        )
        composeTestRule.onNodeWithText("J").assertIsDisplayed()
    }

    @Test
    fun testMaxThreeAvatarsAreDisplayed() {
        moreStreamsItem = StreamItem.MoreStreams(
            users = listOf(
                MoreStreamsUserPreview("1", "john", null),
                MoreStreamsUserPreview("2", "mary", null),
                MoreStreamsUserPreview("3", "alice", null),
                MoreStreamsUserPreview("4", "harry", null),
            )
        )
        composeTestRule.onNodeWithText("J").assertIsDisplayed()
        composeTestRule.onNodeWithText("M").assertIsDisplayed()
        composeTestRule.onNodeWithText("A").assertIsDisplayed()
        composeTestRule.onNodeWithText("H").assertDoesNotExist()
    }

    @Test
    fun testOthersCountIsDisplayed() {
        moreStreamsItem = StreamItem.MoreStreams(
            users = listOf(
                MoreStreamsUserPreview("1", "john", null),
                MoreStreamsUserPreview("2", "mary", null)
            )
        )
        val text = composeTestRule.activity.getString(R.string.kaleyra_stream_other_participants, 2)
        composeTestRule.onNodeWithText(text).assertIsDisplayed()
    }
}
