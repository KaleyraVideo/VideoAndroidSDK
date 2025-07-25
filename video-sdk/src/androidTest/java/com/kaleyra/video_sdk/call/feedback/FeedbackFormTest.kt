/*
 * Copyright 2023 Kaleyra @ https://www.kaleyra.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kaleyra.video_sdk.call.feedback

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.input.key.NativeKeyEvent
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertRangeInfoEquals
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.hasProgressBarRangeInfo
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performKeyPress
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.requestFocus
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.height
import androidx.test.ext.junit.runners.AndroidJUnit4

import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.call.feedback.model.FeedbackUiRating
import com.kaleyra.video_sdk.call.feedback.model.FeedbackUiState
import com.kaleyra.video_sdk.call.feedback.view.FeedbackForm
import com.kaleyra.video_sdk.call.feedback.view.StarSliderTag
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FeedbackFormTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private var isDismissed = false

    private var feedbackUiState by mutableStateOf(FeedbackUiState.Display())

    private var rating: FeedbackUiRating = FeedbackUiRating.Awful

    private var comment = ""

    @Before
    fun setUp() {
        composeTestRule.setContent {
            FeedbackForm(
                feedbackUiState = feedbackUiState,
                onUserFeedback = { rating, comment ->
                    this@FeedbackFormTest.comment = comment
                    this@FeedbackFormTest.rating = rating
                },
                onDismiss = { isDismissed = true }
            )
        }
    }

    @After
    fun tearDown() {
        isDismissed = false
        rating = FeedbackUiRating.Awful
        comment = ""
    }

    @Test
    fun titleIsDisplayed() {
        val text = composeTestRule.activity.getString(R.string.kaleyra_feedback_evaluate_call)
        composeTestRule.onNodeWithText(text).assertIsDisplayed()
    }

    @Test
    fun cancelButtonIsDisplayed() {
        val cancel = composeTestRule.activity.getString(R.string.kaleyra_action_cancel)
        composeTestRule.onNodeWithText(cancel).assertIsDisplayed()
    }

    @Test
    fun userClicksCancel_onDismissInvoked() {
        val cancel = composeTestRule.activity.getString(R.string.kaleyra_action_cancel)
        composeTestRule.onNodeWithText(cancel).assertHasClickAction()
        composeTestRule.onNodeWithText(cancel).performClick()
        assertEquals(true, isDismissed)
    }

    @Test
    fun testSliderDefaultSettings() {
        composeTestRule.onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo(5f, 1f.rangeTo(5f), 3))).assertIsDisplayed()
    }

    @Test
    fun rightArrowDown_sliderValueIsIncreased() {
        feedbackUiState = FeedbackUiState.Display(rating = FeedbackUiRating.Awful)
        composeTestRule.onNodeWithTag(StarSliderTag).assertRangeInfoEquals(ProgressBarRangeInfo(1f, 1f.rangeTo(5f), 3))
        val text = composeTestRule.activity.getString(R.string.kaleyra_feedback_poor)
        val keyEvent = androidx.compose.ui.input.key.KeyEvent(NativeKeyEvent(0,0, NativeKeyEvent.ACTION_DOWN, NativeKeyEvent.KEYCODE_DPAD_RIGHT, 0))
        composeTestRule.onNodeWithTag(StarSliderTag).requestFocus()
        composeTestRule.onNodeWithTag(StarSliderTag).performKeyPress(keyEvent)
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(StarSliderTag).assertRangeInfoEquals(ProgressBarRangeInfo(2f, 1f.rangeTo(5f), 3))
        composeTestRule.onNodeWithText(text).assertIsDisplayed()
    }

    @Test
    fun leftArrowDown_sliderValueIsDecreased() {
        feedbackUiState = FeedbackUiState.Display(rating = FeedbackUiRating.Excellent)
        composeTestRule.onNodeWithTag(StarSliderTag).assertRangeInfoEquals(ProgressBarRangeInfo(5f, 1f.rangeTo(5f), 3))
        val text = composeTestRule.activity.getString(R.string.kaleyra_feedback_good)
        val keyEvent = androidx.compose.ui.input.key.KeyEvent(NativeKeyEvent(0,0, NativeKeyEvent.ACTION_DOWN, NativeKeyEvent.KEYCODE_DPAD_LEFT, 0))
        composeTestRule.onNodeWithTag(StarSliderTag).requestFocus()
        composeTestRule.onNodeWithTag(StarSliderTag).performKeyPress(keyEvent)
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(StarSliderTag).assertRangeInfoEquals(ProgressBarRangeInfo(4f, 1f.rangeTo(5f), 3))
        composeTestRule.onNodeWithText(text).assertIsDisplayed()
    }

    @Test
    fun leftArrowDown_atMinValue_doesNotChangeSliderPosition() {
        feedbackUiState = FeedbackUiState.Display(rating = FeedbackUiRating.Awful)
        composeTestRule.onNodeWithTag(StarSliderTag).assertRangeInfoEquals(ProgressBarRangeInfo(1f, 1f.rangeTo(5f), 3))
        val text = composeTestRule.activity.getString(R.string.kaleyra_feedback_bad)
        val keyEvent = androidx.compose.ui.input.key.KeyEvent(NativeKeyEvent(0,0, NativeKeyEvent.ACTION_DOWN, NativeKeyEvent.KEYCODE_DPAD_LEFT, 0))
        composeTestRule.onNodeWithTag(StarSliderTag).requestFocus()
        composeTestRule.onNodeWithTag(StarSliderTag).performKeyPress(keyEvent)
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(StarSliderTag).assertRangeInfoEquals(ProgressBarRangeInfo(1f, 1f.rangeTo(5f), 3))
        composeTestRule.onNodeWithText(text).assertIsDisplayed()
    }

    @Test
    fun rightArrowDown_atMinValue_doesNotChangeSliderPosition() {
        feedbackUiState = FeedbackUiState.Display(rating = FeedbackUiRating.Excellent)
        composeTestRule.onNodeWithTag(StarSliderTag).assertRangeInfoEquals(ProgressBarRangeInfo(5f, 1f.rangeTo(5f), 3))
        val text = composeTestRule.activity.getString(R.string.kaleyra_feedback_excellent)
        val keyEvent = androidx.compose.ui.input.key.KeyEvent(NativeKeyEvent(0,0, NativeKeyEvent.ACTION_DOWN, NativeKeyEvent.KEYCODE_DPAD_RIGHT, 0))
        composeTestRule.onNodeWithTag(StarSliderTag).requestFocus()
        composeTestRule.onNodeWithTag(StarSliderTag).performKeyPress(keyEvent)
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(StarSliderTag).assertRangeInfoEquals(ProgressBarRangeInfo(5f, 1f.rangeTo(5f), 3))
        composeTestRule.onNodeWithText(text).assertIsDisplayed()
    }

    @Test
    fun oneStar_awfulTextIsDisplayed() {
        val text = composeTestRule.activity.getString(R.string.kaleyra_feedback_bad)
        composeTestRule.onNodeWithTag(StarSliderTag).performTouchInput {
            swipeLeft(right, left)
        }
        composeTestRule.onNodeWithTag(StarSliderTag).assertRangeInfoEquals(ProgressBarRangeInfo(1f, 1f.rangeTo(5f), 3))
        composeTestRule.onNodeWithText(text).assertIsDisplayed()
    }

    @Test
    fun twoStars_poorTextIsDisplayed() {
        val text = composeTestRule.activity.getString(R.string.kaleyra_feedback_poor)
        composeTestRule.onNodeWithTag(StarSliderTag).performTouchInput {
            val step = width / 4f
            swipeLeft(right, right - step * 3)
        }
        composeTestRule.onNodeWithTag(StarSliderTag).assertRangeInfoEquals(ProgressBarRangeInfo(2f, 1f.rangeTo(5f), 3))
        composeTestRule.onNodeWithText(text).assertIsDisplayed()
    }

    @Test
    fun threeStars_neutralTextIsDisplayed() {
        val text = composeTestRule.activity.getString(R.string.kaleyra_feedback_neutral)
        composeTestRule.onNodeWithTag(StarSliderTag).performTouchInput {
            val step = width / 4f
            swipeLeft(right, right - step * 2)
        }
        composeTestRule.onNodeWithTag(StarSliderTag).assertRangeInfoEquals(ProgressBarRangeInfo(3f, 1f.rangeTo(5f), 3))
        composeTestRule.onNodeWithText(text).assertIsDisplayed()
    }

    @Test
    fun fourStars_goodTextIsDisplayed() {
        val text = composeTestRule.activity.getString(R.string.kaleyra_feedback_good)
        composeTestRule.onNodeWithTag(StarSliderTag).performTouchInput {
            val step = width / 4f
            swipeLeft(right, right - step)
        }
        composeTestRule.onNodeWithTag(StarSliderTag).assertRangeInfoEquals(ProgressBarRangeInfo(4f, 1f.rangeTo(5f), 3))
        composeTestRule.onNodeWithText(text).assertIsDisplayed()
    }

    @Test
    fun fiveStars_excellentTextIsDisplayed() {
        val text = composeTestRule.activity.getString(R.string.kaleyra_feedback_excellent)
        composeTestRule.onNodeWithTag(StarSliderTag).assertRangeInfoEquals(ProgressBarRangeInfo(5f, 1f.rangeTo(5f), 3))
        composeTestRule.onNodeWithText(text).assertIsDisplayed()
    }

    @Test
    fun textFieldHasFocus_titleIsNotDisplayed() {
        val text = composeTestRule.activity.getString(R.string.kaleyra_feedback_evaluate_call)
        val textField = composeTestRule.onNode(hasSetTextAction())
        textField.assertIsDisplayed()
        textField.performClick()
        composeTestRule.onNodeWithText(text).assertDoesNotExist()
    }

    @Test
    fun textFieldHasFocus_heightIncrease() {
        val textField = composeTestRule.onNode(hasSetTextAction())
        val before = textField.getBoundsInRoot().height
        textField.performClick()
        textField.assertHeightIsAtLeast(before + 1.dp)
    }

    @Test
    fun voteButtonIsDisplayed() {
        val text = composeTestRule.activity.getString(R.string.kaleyra_feedback_vote)
        composeTestRule.onNodeWithText(text).assertIsDisplayed()
    }

    @Test
    fun userClicksVote_onUserFeedbackInvoked() {
        val text = composeTestRule.activity.getString(R.string.kaleyra_feedback_vote)
        val button = composeTestRule.onNodeWithText(text)
        val textField = composeTestRule.onNode(hasSetTextAction())
        textField.performTextInput("text")
        button.assertHasClickAction()
        button.performClick()
        assertEquals(FeedbackUiRating.Excellent, rating)
        assertEquals("text", comment)
    }
}
