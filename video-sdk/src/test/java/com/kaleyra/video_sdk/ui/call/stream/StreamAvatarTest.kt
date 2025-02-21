package com.kaleyra.video_sdk.ui.call.stream

import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.kaleyra.video.User
import com.kaleyra.video_sdk.call.stream.view.core.StreamAvatar
import com.kaleyra.video_sdk.common.avatar.model.ImmutableUri
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList
import com.kaleyra.video_sdk.common.immutablecollections.toImmutableList
import com.kaleyra.video_sdk.common.user.UserInfo
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class StreamAvatarTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private var userInfos by mutableStateOf(ImmutableList<UserInfo>())

    private var avatarCount by mutableIntStateOf(1)

    @Before
    fun setUp() {
        composeTestRule.setContent {
            StreamAvatar(
                userInfos = userInfos,
                avatarCount = avatarCount
            )
        }
    }

    @Test
    fun testMultiAvatarIsDisplayed() {
        userInfos = listOf(
            UserInfo("userId1", "John", ImmutableUri(Uri.EMPTY)),
            UserInfo("userId2", "Mario", ImmutableUri(Uri.EMPTY)),
            UserInfo("userId3", "Alice", ImmutableUri(Uri.EMPTY)),
        ).toImmutableList()
        avatarCount = 3
        composeTestRule.onNodeWithText("A").assertIsDisplayed()
        composeTestRule.onNodeWithText("J").assertIsDisplayed()
        composeTestRule.onNodeWithText("M").assertIsDisplayed()
    }

    @Test
    fun testMultiAvatarOverflowIsDisplayed() {
        userInfos = listOf(
            UserInfo("userId1", "John", ImmutableUri(Uri.EMPTY)),
            UserInfo("userId2", "Mario", ImmutableUri(Uri.EMPTY)),
            UserInfo("userId3", "Alice", ImmutableUri(Uri.EMPTY)),
            UserInfo("userId3", "Alice", ImmutableUri(Uri.EMPTY)),
            UserInfo("userId3", "Alice", ImmutableUri(Uri.EMPTY)),
        ).toImmutableList()
        avatarCount = 4
        composeTestRule.onNodeWithText("A").assertIsDisplayed()
        composeTestRule.onNodeWithText("J").assertIsDisplayed()
        composeTestRule.onNodeWithText("M").assertIsDisplayed()
        composeTestRule.onNodeWithText("+2").assertIsDisplayed()
    }
}