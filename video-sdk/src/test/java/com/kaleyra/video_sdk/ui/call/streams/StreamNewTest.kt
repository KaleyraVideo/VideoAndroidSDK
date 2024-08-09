package com.kaleyra.video_sdk.ui.call.streams

import android.view.View
import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import com.kaleyra.video_sdk.call.stream.model.core.ImmutableView
import com.kaleyra.video_sdk.call.stream.view.core.Stream
import com.kaleyra.video_sdk.call.stream.view.core.StreamViewTestTag
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class StreamNewTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private var showStreamView by mutableStateOf(true)

    @Before
    fun setUp() {
        composeTestRule.setContent {
            Stream(
                streamView = ImmutableView(View(composeTestRule.activity)),
                username = "username",
                avatar = null,
                showStreamView = showStreamView,
            )
        }
    }

    @After
    fun tearDown() {
        showStreamView = true
    }

    @Test
    fun streamViewIsDisplayed() {
        composeTestRule.onNodeWithTag(StreamViewTestTag).assertIsDisplayed()
    }

    @Test
    fun showStreamViewTrue_avatarIsNotDisplayed() {
        showStreamView = true
        composeTestRule.onNodeWithText("U").assertDoesNotExist()
    }

    @Test
    fun showStreamViewFalse_avatarIsDisplayed() {
        showStreamView = false
        composeTestRule.onNodeWithText("U").assertIsDisplayed()
    }
}