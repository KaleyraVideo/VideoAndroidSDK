package com.kaleyra.video_sdk.call.stream

import android.view.View
import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.kaleyra.video_sdk.call.stream.model.ImmutableView
import com.kaleyra.video_sdk.call.stream.model.streamUiMock
import com.kaleyra.video_sdk.call.stream.view.StreamItem
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class StreamItemTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private var stream by mutableStateOf(streamUiMock)

    @Before
    fun setUp() {
        composeTestRule.setContent {
            StreamItem(
                stream = stream,
                fullscreen = false,
                pin = false
            )
        }
    }

    @After
    fun tearDown() {
        stream = streamUiMock
    }

    @Test
    fun testUsernameIsDisplayed() {
        composeTestRule.onNodeWithText(stream.username).assertIsDisplayed()
    }

    @Test
    fun viewNull_avatarIsDisplayed() {
        stream = stream.copy(video = stream.video?.copy(view = null))
        composeTestRule.onNodeWithText(stream.username[0].uppercase()).assertIsDisplayed()
    }

    @Test
    fun videoNotEnabled_avatarIsDisplayed() {
        stream = stream.copy(video = stream.video?.copy(view = ImmutableView(View(composeTestRule.activity)), isEnabled = false))
        composeTestRule.onNodeWithText(stream.username[0].uppercase()).assertIsDisplayed()
    }

    @Test
    fun videoEnabled_avatarIsNotDisplayed() {
        stream = stream.copy(video = stream.video?.copy(view = ImmutableView(View(composeTestRule.activity)), isEnabled = true))
        composeTestRule.onNodeWithText(stream.username[0].uppercase()).assertDoesNotExist()
    }
}