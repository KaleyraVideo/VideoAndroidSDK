package com.kaleyra.video_sdk.ui.call.screen

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.kaleyra.video_sdk.call.screen.VCallScreenTestTag
import com.kaleyra.video_sdk.common.immutablecollections.toImmutableList
import kotlinx.coroutines.flow.update
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
internal class CallScreenMediumTest: CallScreenBaseTest() {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Config(qualifiers = "w360dp-h640dp")
    @Test
    fun mediumHeight_verticalCallScreenIsDisplayed() {
        callActionsUiState.update {
            it.copy(actionList = allActions.toImmutableList())
        }
        composeTestRule.setUpCallScreen()

        composeTestRule.onNodeWithTag(VCallScreenTestTag).assertIsDisplayed()
    }
}