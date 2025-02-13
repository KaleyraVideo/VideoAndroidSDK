package com.kaleyra.video_sdk.call.screen

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.kaleyra.video_sdk.common.immutablecollections.toImmutableList
import kotlinx.coroutines.flow.update
import org.junit.Rule
import org.junit.Test

internal class CallScreenMediumTest: CallScreenBaseTest() {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun mediumHeight_verticalCallScreenIsDisplayed() {
        callActionsUiState.update {
            it.copy(actionList = allActions.toImmutableList())
        }
        composeTestRule.setUpCallScreen()

        composeTestRule.onNodeWithTag(VCallScreenTestTag).assertIsDisplayed()
    }
}