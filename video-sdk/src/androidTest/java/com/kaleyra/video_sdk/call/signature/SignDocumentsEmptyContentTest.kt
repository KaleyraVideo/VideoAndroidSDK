package com.kaleyra.video_sdk.call.signature

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.call.signature.view.SignDocumentsEmptyContent
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class SignDocumentsEmptyContentTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Before
    fun setUp() {
        composeTestRule.setContent {
            SignDocumentsEmptyContent()
        }
    }

    @Test
    fun titleDisplayed() {
        val title = composeTestRule.activity.getString(R.string.kaleyra_signature_no_documents_shared)
        composeTestRule.onNodeWithText(title).assertIsDisplayed()
    }
}
