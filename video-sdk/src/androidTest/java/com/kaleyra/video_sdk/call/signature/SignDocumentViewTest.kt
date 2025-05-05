package com.kaleyra.video_sdk.call.signature

import android.view.View
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.kaleyra.video_sdk.call.signature.view.SignDocumentView
import com.kaleyra.video_sdk.call.signature.view.SignViewTag
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class SignDocumentViewTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Before
    fun setUp() {
        composeTestRule.setContent {
            SignDocumentView(
                signView = View(composeTestRule.activity.baseContext)
            )
        }
    }

    @Test
    fun testSignViewAdded() {
        composeTestRule.onNodeWithTag(SignViewTag)
    }
}
