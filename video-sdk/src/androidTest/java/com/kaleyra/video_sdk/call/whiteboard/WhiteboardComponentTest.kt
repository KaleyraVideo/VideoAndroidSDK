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

package com.kaleyra.video_sdk.call.whiteboard

import android.view.View
import androidx.activity.ComponentActivity
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertRangeInfoEquals
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeDown
import androidx.compose.ui.text.input.TextFieldValue
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.call.whiteboard.model.WhiteboardUiState
import com.kaleyra.video_sdk.call.whiteboard.model.WhiteboardUploadUi
import com.kaleyra.video_sdk.call.whiteboard.view.LinearProgressIndicatorTag
import com.kaleyra.video_sdk.call.whiteboard.view.TextEditorState
import com.kaleyra.video_sdk.call.whiteboard.view.TextEditorValue
import com.kaleyra.video_sdk.call.whiteboard.view.WhiteboardViewTag
import com.kaleyra.video_sdk.common.usermessages.model.RecordingMessage
import com.kaleyra.video_sdk.common.usermessages.model.UserMessage
import com.kaleyra.video_sdk.pressBack
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@OptIn(ExperimentalMaterialApi::class)
@RunWith(AndroidJUnit4::class)
class WhiteboardComponentTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private var showWhiteboardComponent by mutableStateOf(true)

    private var uiState by mutableStateOf(WhiteboardUiState())

    private var sheetState by mutableStateOf(ModalBottomSheetState(ModalBottomSheetValue.Hidden))

    private var textEditorState by mutableStateOf(TextEditorState(TextEditorValue.Empty))

    private var userMessage by mutableStateOf<UserMessage?>(null)

    private var isReloadClicked = false

    private var confirmedText: String? = null

    private var isTextDismissed = false

    private var isWhiteboardClosed = false

    @Before
    fun setUp() {
        composeTestRule.setContent {
            if (showWhiteboardComponent) {
                WhiteboardComponent(
                    uiState = uiState,
                    editorSheetState = sheetState,
                    textEditorState = textEditorState,
                    userMessage = userMessage,
                    onReloadClick = { isReloadClicked = true },
                    onTextConfirmed = { confirmedText = it },
                    onTextDismissed = { isTextDismissed = true },
                    onWhiteboardClosed = { isWhiteboardClosed = true }
                )
            }
        }
    }

    @After
    fun tearDown() {
        showWhiteboardComponent = true
        uiState = WhiteboardUiState()
        sheetState = ModalBottomSheetState(ModalBottomSheetValue.Hidden)
        textEditorState = TextEditorState(TextEditorValue.Empty)
        confirmedText = null
        userMessage = null
        isTextDismissed = false
        isReloadClicked = false
        isWhiteboardClosed = false
    }

    @Test
    fun whiteboardViewNull_whiteboardViewDoesNotExist() {
        uiState = WhiteboardUiState(whiteboardView = null)
        composeTestRule.onNodeWithTag(WhiteboardViewTag).assertDoesNotExist()
    }

    @Test
    fun whiteboardViewNotNull_whiteboardViewIsDisplayed() {
        uiState = WhiteboardUiState(whiteboardView = View(composeTestRule.activity))
        composeTestRule.onNodeWithTag(WhiteboardViewTag).assertIsDisplayed()
    }

    @Test
    fun textNull_whiteboardIsDisplayed() {
        sheetState = ModalBottomSheetState(ModalBottomSheetValue.Expanded)
        uiState = WhiteboardUiState(whiteboardView = View(composeTestRule.activity), text = null)
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(WhiteboardViewTag).assertIsDisplayed()
    }

    @Test
    fun textNull_sheetStateIsHidden() {
        sheetState = ModalBottomSheetState(ModalBottomSheetValue.Expanded)
        uiState = WhiteboardUiState(text = null)
        composeTestRule.waitForIdle()
        runBlocking {
            val currentValue = snapshotFlow { sheetState.currentValue }.first()
            assertEquals(ModalBottomSheetValue.Hidden, currentValue)
        }
    }

    @Test
    fun textNull_textEditorCleaned() {
        // Check text editor before
        uiState = WhiteboardUiState(text = "text")
        composeTestRule.onNodeWithText("text").assertIsDisplayed()
        // Check after setting text to null
        uiState = WhiteboardUiState(text = null)
        val hint = composeTestRule.activity.getString(R.string.kaleyra_edit_text_input_placeholder)
        composeTestRule.onNodeWithText(hint).assertExists()
        composeTestRule.onNode(hasSetTextAction()).assert(hasText(""))
    }

    @Test
    fun textNotNull_textEditorIsDisplayed() {
        uiState = WhiteboardUiState(text = "text")
        composeTestRule.onNodeWithText("text").assertIsDisplayed()
    }

    @Test
    fun textNotNull_sheetStateIsExpanded() {
        sheetState = ModalBottomSheetState(ModalBottomSheetValue.Hidden)
        uiState = WhiteboardUiState(text = "")
        composeTestRule.waitForIdle()
        runBlocking {
            val currentValue = snapshotFlow { sheetState.currentValue }.first()
            assertEquals(ModalBottomSheetValue.Expanded, currentValue)
        }
    }

    @Test
    fun userConfirmsEditorText_onTextConfirmedInvoked() {
        uiState = WhiteboardUiState(text = "text")
        val confirm = composeTestRule.activity.getString(R.string.kaleyra_action_confirm)
        composeTestRule.onNodeWithContentDescription(confirm).performClick()
        assertEquals("text", confirmedText)
    }

    @Test
    fun userDismissesEditorText_onTextDismissInvoked() {
        uiState = WhiteboardUiState(text = "")
        val dismiss = composeTestRule.activity.getString(R.string.kaleyra_action_dismiss)
        composeTestRule.onNodeWithContentDescription(dismiss).performClick()
        assert(isTextDismissed)
    }

    @Test
    fun userDismissEditorText_onTextDismissInvoked() {
        uiState = WhiteboardUiState(text = "text")
        composeTestRule.onNodeWithText("text").assertIsDisplayed()
    }

    @Test
    fun textEditorIsDisplayed_userClicksDismissButton_onTextDismissInvoked() {
        uiState = WhiteboardUiState(text = "text")
        composeTestRule.onNodeWithText("text").assertIsDisplayed()
    }

    @Test
    fun offlineTrue_offlineUIDisplayed() {
        val title = composeTestRule.activity.getString(R.string.kaleyra_error_title)
        val subtitle = composeTestRule.activity.getString(R.string.kaleyra_error_subtitle)
        val reload = composeTestRule.activity.getString(R.string.kaleyra_error_button_reload)
        composeTestRule.onNodeWithText(title).assertDoesNotExist()
        composeTestRule.onNodeWithText(subtitle).assertDoesNotExist()
        composeTestRule.onNodeWithContentDescription(reload).assertDoesNotExist()
        uiState =   WhiteboardUiState(isOffline = true)
        composeTestRule.onNodeWithText(title).assertIsDisplayed()
        composeTestRule.onNodeWithText(subtitle).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(reload).assertIsDisplayed()
    }

    @Test
    fun userClicksReload_onReloadClickInvoked() {
        uiState = WhiteboardUiState(isOffline = true)
        val reload = composeTestRule.activity.getString(R.string.kaleyra_error_button_reload)
        composeTestRule.onNodeWithContentDescription(reload).performClick()
        assert(isReloadClicked)
    }

    @Test
    fun whiteboardUploadError_errorCardDisplayed() {
        val title = composeTestRule.activity.getString(R.string.kaleyra_whiteboard_error_title)
        val subtitle = composeTestRule.activity.getString(R.string.kaleyra_whiteboard_error_subtitle)
        composeTestRule.onNodeWithText(title).assertDoesNotExist()
        composeTestRule.onNodeWithText(subtitle).assertDoesNotExist()
        uiState = WhiteboardUiState(whiteboardView = View(composeTestRule.activity), upload = WhiteboardUploadUi.Error)
        composeTestRule.onNodeWithText(title).assertIsDisplayed()
        composeTestRule.onNodeWithText(subtitle).assertIsDisplayed()
    }

    @Test
    fun whiteboardUploadUploading_uploadingCardDisplayed() {
        val title = composeTestRule.activity.getString(R.string.kaleyra_whiteboard_uploading_file)
        val subtitle = composeTestRule.activity.getString(R.string.kaleyra_whiteboard_compressing)
        val percentage = composeTestRule.activity.getString(R.string.kaleyra_file_upload_percentage, 70)
        composeTestRule.onNodeWithText(title).assertDoesNotExist()
        composeTestRule.onNodeWithText(subtitle).assertDoesNotExist()
        composeTestRule.onNodeWithText(percentage).assertDoesNotExist()
        uiState = WhiteboardUiState(whiteboardView = View(composeTestRule.activity), upload = WhiteboardUploadUi.Uploading(.7f))
        composeTestRule.onNodeWithText(title).assertIsDisplayed()
        composeTestRule.onNodeWithText(subtitle).assertIsDisplayed()
        composeTestRule.onNodeWithText(percentage).assertIsDisplayed()
    }

    @Test
    fun isLoadingTrue_indeterminateProgressIndicatorDisplayed() {
        uiState = WhiteboardUiState(whiteboardView = View(composeTestRule.activity), isLoading = true, isOffline = false)
        composeTestRule
            .onNodeWithTag(LinearProgressIndicatorTag)
            .assertIsDisplayed()
            .assertRangeInfoEquals(ProgressBarRangeInfo.Indeterminate)
    }

    @Test
    fun isLoadingFalse_indeterminateProgressIndicatorDoesNotExists() {
        uiState = WhiteboardUiState(whiteboardView = View(composeTestRule.activity), isLoading = false, isOffline = false)
        composeTestRule
            .onNodeWithTag(LinearProgressIndicatorTag)
            .assertDoesNotExist()
    }

    @Test
    fun textEditorInEditingState_userPerformsBack_textEditorInDiscardState() {
        sheetState = ModalBottomSheetState(ModalBottomSheetValue.Expanded)
        textEditorState = TextEditorState(TextEditorValue.Editing(TextFieldValue()))
        uiState = WhiteboardUiState(text = "text")
        composeTestRule.waitForIdle()
        composeTestRule.pressBack()
        assertEquals(TextEditorValue.Discard, textEditorState.currentValue)
    }

    @Test
    fun textEditorInDiscardState_userPerformsBack_textEditorInEditingState() {
        sheetState = ModalBottomSheetState(ModalBottomSheetValue.Expanded)
        textEditorState = TextEditorState(TextEditorValue.Discard)
        uiState = WhiteboardUiState(text = "")
        composeTestRule.waitForIdle()
        composeTestRule.pressBack()
        assertEquals(TextEditorValue.Editing(TextFieldValue("")), textEditorState.currentValue)
    }

    @Test
    fun textEditorInEmptyState_userPerformsBack_onTextDismissedInvoked() {
        sheetState = ModalBottomSheetState(ModalBottomSheetValue.Expanded)
        textEditorState = TextEditorState(TextEditorValue.Empty)
        uiState = WhiteboardUiState(text = "")
        composeTestRule.waitForIdle()
        composeTestRule.pressBack()
        assert(isTextDismissed)
    }

    @Test
    fun textEditorDisplayed_userSwipeDownBottomSheet_textEditorIsStillDisplayed() {
        sheetState = ModalBottomSheetState(ModalBottomSheetValue.Expanded)
        uiState = WhiteboardUiState(text = "")
        composeTestRule.onNode(hasSetTextAction()).performTouchInput { swipeDown(startY = 0f, endY = 3000f) }
        composeTestRule.waitForIdle()
        runBlocking {
            val currentValue = snapshotFlow { sheetState.currentValue }.first()
            assertEquals(ModalBottomSheetValue.Expanded, currentValue)
        }
    }

    @Test
    fun whiteboardComponentDispose_onWhiteboardClosedInvoked() {
        showWhiteboardComponent = false
        composeTestRule.waitForIdle()
        assertEquals(true, isWhiteboardClosed)
    }

    @Test
    fun userMessage_userMessageSnackbarIsDisplayed() {
        userMessage = RecordingMessage.Started
        val title = composeTestRule.activity.getString(R.string.kaleyra_recording_started)
        composeTestRule.onNodeWithText(title).assertIsDisplayed()
    }

}