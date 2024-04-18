package com.kaleyra.video_sdk

import androidx.activity.ComponentActivity
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kaleyra.video_sdk.common.avatar.model.ImmutableUri
import com.kaleyra.video_sdk.common.avatar.view.Avatar
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AvatarTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun uriIsValid_avatarIsLoaded() {
        val uri = composeTestRule.activity.loadResourceUri(com.kaleyra.video_sdk.test.R.drawable.kaleyra_logo_clipped)
        val success = MutableStateFlow(false)
        composeTestRule.setContent {
            Avatar(
                text = "us",
                uri = ImmutableUri(uri),
                contentDescription = "",
                backgroundColor = Color.Black,
                onSuccess = { success.value = true }
            )
        }
        runBlocking {
            success.first { it }
            composeTestRule.onNodeWithText("us").assertDoesNotExist()
        }
    }

    @Test
    fun imageNotLoaded_textIsDisplayed() {
        composeTestRule.setContent {
            Avatar(
                text = "us",
                uri = null,
                contentDescription = "",
                backgroundColor = Color.Black
            )
        }
        composeTestRule.onNodeWithText("us").assertIsDisplayed()
    }

}