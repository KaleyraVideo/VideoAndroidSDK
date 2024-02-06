package com.kaleyra.video_sdk.call.callactionsm3

import android.content.pm.ActivityInfo
import androidx.activity.ComponentActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.call.callactions.model.CallAction
import com.kaleyra.video_sdk.call.callactionsm3.view.CallActionLabelM3
import com.kaleyra.video_sdk.call.callactionsm3.view.CallActionM3
import com.kaleyra.video_sdk.call.callactionsm3.view.CallActionM3Configuration
import com.kaleyra.video_sdk.call.callactionsm3.view.OrientationAwareComponent
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class OrientationAwareComponentTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private var action by mutableStateOf<CallAction>(CallAction.Answer())

    @Test
    fun isLandscape_answerActionShown_labelDisplayed() {
        composeTestRule.activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        composeTestRule.setContent {
            OrientationAwareComponent(
                portraitContent = {
                    CallActionM3(
                        modifier = Modifier,
                        configuration = CallActionM3Configuration.Clickable(action, {}),
                        isDarkTheme = isSystemInDarkTheme(),
                        displayLabel = false
                    )
                },
                landscapeContent = {
                    CallActionLabelM3(
                        modifier = Modifier,
                        action = CallAction.Answer(),
                        onClick = {},
                        isDarkTheme = isSystemInDarkTheme()
                    )
                }
            )
        }
        val answerLabel = composeTestRule.activity.getString(R.string.kaleyra_call_answer)
        composeTestRule.onNodeWithText(answerLabel).assertIsDisplayed()
    }
}