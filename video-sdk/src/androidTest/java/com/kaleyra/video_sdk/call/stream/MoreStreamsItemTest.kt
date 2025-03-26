package com.kaleyra.video_sdk.call.stream

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.call.stream.layoutsystem.model.StreamItem
import com.kaleyra.video_sdk.call.stream.view.items.MoreStreamsItem
import com.kaleyra.video_sdk.common.avatar.model.ImmutableUri
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList
import com.kaleyra.video_sdk.common.immutablecollections.toImmutableList
import com.kaleyra.video_sdk.common.user.UserInfo
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class MoreStreamsItemTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private var moreStreamsItem by mutableStateOf(StreamItem.MoreStreams(userInfos = ImmutableList()))

    @Before
    fun setUp() {
        composeTestRule.setContent {
            MoreStreamsItem(moreStreamsItem = moreStreamsItem)
        }
    }

    @After
    fun tearDown() {
        moreStreamsItem = StreamItem.MoreStreams(userInfos = ImmutableList())
    }

    @Test
    fun testAvatarIsDisplayed() {
        moreStreamsItem = StreamItem.MoreStreams(
            userInfos = listOf(UserInfo("1","john", ImmutableUri())).toImmutableList()
        )
        composeTestRule.onNodeWithText("J").assertIsDisplayed()
    }

    @Test
    fun testMultiAvatarIsDisplayed() {
        moreStreamsItem = StreamItem.MoreStreams(
            userInfos = ImmutableList(
                listOf(
                    UserInfo("userId1", "Alice", ImmutableUri()),
                    UserInfo("userId2", "John", ImmutableUri()),
                    UserInfo("userId3", "Mario", ImmutableUri())
                )
            )
        )
        composeTestRule.onNodeWithText("A").assertIsDisplayed()
        composeTestRule.onNodeWithText("J").assertIsDisplayed()
        composeTestRule.onNodeWithText("M").assertIsDisplayed()
    }

    @Test
    fun testMultiAvatarOverflowIsDisplayed() {
        moreStreamsItem = StreamItem.MoreStreams(
            userInfos = ImmutableList(
                listOf(
                    UserInfo("userId1", "Alice", ImmutableUri()),
                    UserInfo("userId2", "John", ImmutableUri()),
                    UserInfo("userId3", "Mario", ImmutableUri()),
                    UserInfo("userId4", "Oliver", ImmutableUri()),
                    UserInfo("userId5", "Federico", ImmutableUri())
                )
            )
        )
        composeTestRule.onNodeWithText("A").assertIsDisplayed()
        composeTestRule.onNodeWithText("J").assertIsDisplayed()
        composeTestRule.onNodeWithText("+3").assertIsDisplayed()
    }

    @Test
    fun testOthersCountIsDisplayed() {
        moreStreamsItem = StreamItem.MoreStreams(
            userInfos = listOf(
                UserInfo("1", "john", ImmutableUri()),
                UserInfo("2", "mary", ImmutableUri())
            ).toImmutableList()
        )
        val text = composeTestRule.activity.getString(R.string.kaleyra_stream_other_participants, 2)
        composeTestRule.onNodeWithText(text).assertIsDisplayed()
    }
}
