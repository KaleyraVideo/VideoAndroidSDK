package com.kaleyra.video_sdk

import androidx.activity.ComponentActivity
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onParent
import androidx.compose.ui.unit.dp
import com.kaleyra.video_sdk.common.avatar.model.ImmutableUri
import com.kaleyra.video_sdk.common.avatar.view.Avatar
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test

// TODO add this as robolectric test
class AvatarTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun uriIsValid_avatarIsLoaded() {
        val uri = composeTestRule.activity.loadResourceUri(com.kaleyra.video_sdk.test.R.drawable.kaleyra_logo_clipped)
        val success = MutableStateFlow(false)
        composeTestRule.setContent {
            Avatar(
                username = "us",
                uri = ImmutableUri(uri),
                backgroundColor = Color.Black,
                onSuccess = { success.value = true }
            )
        }
        runBlocking {
            success.first { it }
            composeTestRule.onNodeWithText("U").assertDoesNotExist()
        }
    }

    @Test
    fun imageNotLoaded_textIsDisplayed() {
        composeTestRule.setContent {
            Avatar(
                username = "us",
                uri = null,
                backgroundColor = Color.Black
            )
        }
        composeTestRule.onNodeWithText("U").assertIsDisplayed()
    }

    @Test
    fun imageNotLoadedAndTextBlank_placeholderIsDisplayed() {
        composeTestRule.setContent {
            Avatar(
                username = "",
                uri = null,
                backgroundColor = Color.Black
            )
        }
        val text = composeTestRule.activity.getString(R.string.kaleyra_avatar)
        composeTestRule.onNodeWithContentDescription(text).assertIsDisplayed()
    }

    @Test
    fun blankUsername_doesNotThrowAnyException() {
        composeTestRule.setContent {
            Avatar(
                username = "",
                uri = null,
                backgroundColor = Color.Black
            )
        }
    }

    @Test
    fun testSize() {
        val size = 40.dp
        composeTestRule.setContent {
            Avatar(
                username = "username",
                uri = null,
                size = size,
                backgroundColor = Color.Black
            )
        }
        val avatar = composeTestRule.onNodeWithText("U").onParent()
        avatar.assertWidthIsEqualTo(size)
        avatar.assertHeightIsEqualTo(size)
    }

}