package com.kaleyra.video_sdk.call.stream

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.call.stream.view.ScreenShareItem
import junit.framework.TestCase.assertEquals
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ScreenShareItemTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private var clicked = false

    @Before
    fun setUp() {
        composeTestRule.setContent {
            ScreenShareItem(
                onStopClick = {
                    clicked = true
                }
            )
        }
    }

    @After
    fun tearDown() {
        clicked = false
    }

    @Test
    fun testTitleIsDisplayed() {
        val text = composeTestRule.activity.getString(R.string.kaleyra_stream_screenshare_message)
        composeTestRule.onNodeWithText(text).assertIsDisplayed()
    }

    @Test
    fun testButtonIsDisplayed() {
        val text = composeTestRule.activity.getString(R.string.kaleyra_stream_screenshare_action)
        composeTestRule
            .onNodeWithText(text)
            .assertHasClickAction()
            .assertIsDisplayed()
    }

    @Test
    fun testButtonIsClicked() {
        val text = composeTestRule.activity.getString(R.string.kaleyra_stream_screenshare_action)
        composeTestRule.onNodeWithText(text).performClick()
        assertEquals(true, clicked)
    }

}
