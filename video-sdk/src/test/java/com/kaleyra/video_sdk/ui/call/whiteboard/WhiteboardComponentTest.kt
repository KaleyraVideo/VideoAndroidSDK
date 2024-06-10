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

package com.kaleyra.video_sdk.ui.call.whiteboard

import android.view.View
import androidx.activity.ComponentActivity
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.call.whiteboard.WhiteboardComponent
import com.kaleyra.video_sdk.call.whiteboard.model.WhiteboardUiState
import com.kaleyra.video_sdk.call.whiteboard.model.WhiteboardUploadUi
import com.kaleyra.video_sdk.call.whiteboard.view.LinearProgressIndicatorTag
import com.kaleyra.video_sdk.call.whiteboard.view.WhiteboardViewTag
import com.kaleyra.video_sdk.common.usermessages.model.RecordingMessage
import com.kaleyra.video_sdk.common.usermessages.model.UserMessage
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalMaterial3Api::class)
@RunWith(RobolectricTestRunner::class)
class WhiteboardComponentTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private var showWhiteboardComponent by mutableStateOf(true)

    private var uiState by mutableStateOf(WhiteboardUiState())

    private lateinit var sheetState: SheetState

    private var userMessage by mutableStateOf<UserMessage?>(null)

    private var isReloadClicked = false

    private var confirmedText: String? = null

    private var isTextDismissed = false

    private var isWhiteboardClosed = false

    private var isBackPressed = false

    private var isUploadClicked = false

    @Before
    fun setUp() {
        composeTestRule.setContent {
            sheetState = rememberModalBottomSheetState()
            if (showWhiteboardComponent) {
                WhiteboardComponent(
                    uiState = uiState,
                    userMessage = userMessage,
                    onReloadClick = { isReloadClicked = true },
                    onWhiteboardClosed = { isWhiteboardClosed = true },
                    onBackPressed = { isBackPressed = true },
                    onUploadClick = { isUploadClicked = true }
                )
            }
        }
    }

    @After
    fun tearDown() {
        showWhiteboardComponent = true
        uiState = WhiteboardUiState()
        confirmedText = null
        userMessage = null
        isTextDismissed = false
        isReloadClicked = false
        isWhiteboardClosed = false
        isBackPressed = false
        isUploadClicked = false
    }

    @Test
    fun whiteboardTopAppBarShown() {
        val fileShare = composeTestRule.activity.getString(R.string.kaleyra_whiteboard)
        composeTestRule.onNodeWithText(fileShare).assertIsDisplayed()
    }

    @Test
    fun whiteBoardAppBarCloseButtonShown() {
        val close = composeTestRule.activity.getString(R.string.kaleyra_close)
        composeTestRule.onNodeWithContentDescription(close).assertIsDisplayed()
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
    fun userClicksBack_onBackPressedInvoked() {
        uiState = WhiteboardUiState(isOffline = false)
        val close = composeTestRule.activity.getString(R.string.kaleyra_close)
        composeTestRule.onNodeWithContentDescription(close).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(close).performClick()
        assert(isBackPressed)
    }

    @Test
    fun userClicksUpload_onUploadInvoked() {
        uiState = WhiteboardUiState(isOffline = false, isFileSharingSupported = true)
        val upload = composeTestRule.activity.getString(R.string.kaleyra_upload_file)
        composeTestRule.onNodeWithContentDescription(upload).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(upload).performClick()
        assert(isUploadClicked)
    }

    @Test
    fun userClicksUpload_fileSharingNotSupported_uploadNotShown() {
        uiState = WhiteboardUiState(isOffline = false, isFileSharingSupported = false)
        val upload = composeTestRule.activity.getString(R.string.kaleyra_upload_file)
        composeTestRule.onNodeWithContentDescription(upload).assertDoesNotExist()
    }

    @Test
    fun userClicksUpload_whiteboardIsOffline_uploadNotShown() {
        uiState = WhiteboardUiState(isOffline = true, isFileSharingSupported = true)
        val upload = composeTestRule.activity.getString(R.string.kaleyra_upload_file)
        composeTestRule.onNodeWithContentDescription(upload).assertDoesNotExist()
    }

    @Test
    fun userClicksUpload_whiteboardIsLoading_uploadNotShown() {
        uiState = WhiteboardUiState(isLoading = true, isFileSharingSupported = true)
        val upload = composeTestRule.activity.getString(R.string.kaleyra_upload_file)
        composeTestRule.onNodeWithContentDescription(upload).assertDoesNotExist()
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