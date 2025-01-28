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
import com.kaleyra.video_sdk.call.stream.model.HiddenStreamUserPreview
import com.kaleyra.video_sdk.call.stream.view.items.HiddenStreamsItem
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class HiddenStreamsItemTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private var hiddenStreamsItem by mutableStateOf(StreamItem.HiddenStreams(users = listOf()))

    @Before
    fun setUp() {
        composeTestRule.setContent {
            HiddenStreamsItem(hiddenStreamsItem = hiddenStreamsItem)
        }
    }

    @After
    fun tearDown() {
        hiddenStreamsItem = StreamItem.HiddenStreams(users = listOf())
    }

    @Test
    fun testAvatarIsDisplayed() {
        hiddenStreamsItem = StreamItem.HiddenStreams(
            users = listOf(HiddenStreamUserPreview("1","john", null),)
        )
        composeTestRule.onNodeWithText("J").assertIsDisplayed()
    }

    @Test
    fun testMaxThreeAvatarsAreDisplayed() {
        hiddenStreamsItem = StreamItem.HiddenStreams(
            users = listOf(
                HiddenStreamUserPreview("1", "john", null),
                HiddenStreamUserPreview("2", "mary", null),
                HiddenStreamUserPreview("3", "alice", null),
                HiddenStreamUserPreview("4", "harry", null),
            )
        )
        composeTestRule.onNodeWithText("J").assertIsDisplayed()
        composeTestRule.onNodeWithText("M").assertIsDisplayed()
        composeTestRule.onNodeWithText("A").assertIsDisplayed()
        composeTestRule.onNodeWithText("H").assertDoesNotExist()
    }

    @Test
    fun testOthersCountIsDisplayed() {
        hiddenStreamsItem = StreamItem.HiddenStreams(
            users = listOf(
                HiddenStreamUserPreview("1", "john", null),
                HiddenStreamUserPreview("2", "mary", null)
            )
        )
        val text = composeTestRule.activity.getString(R.string.kaleyra_stream_other_participants, 2)
        composeTestRule.onNodeWithText(text).assertIsDisplayed()
    }
}
