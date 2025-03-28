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

package com.kaleyra.video_sdk.call.fileshare

import android.content.Context
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kaleyra.video_common_ui.utils.extensions.ContextExtensions
import com.kaleyra.video_common_ui.utils.extensions.ContextExtensions.tryToOpenFile
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.call.fileshare.filepick.FilePickActivity
import com.kaleyra.video_sdk.call.fileshare.model.FileShareUiState
import com.kaleyra.video_sdk.call.fileshare.model.SharedFileUi
import com.kaleyra.video_sdk.call.fileshare.model.mockDownloadSharedFile
import com.kaleyra.video_sdk.call.fileshare.model.mockUploadSharedFile
import com.kaleyra.video_sdk.call.fileshare.view.FileShareItemTag
import com.kaleyra.video_sdk.common.avatar.model.ImmutableUri
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

class FileShareComponentTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private var fileShareUiState by mutableStateOf(FileShareUiState())

    private var showUnableToOpenFileSnackBar by mutableStateOf(false)

    private var showCancelledFileSnackBar by mutableStateOf(false)

    private var showFileSizeLimitAlertDialog by mutableStateOf(false)

    private var largeScreen by mutableStateOf(false)

    private var openFailure: Boolean = false

    private var alertDialogDismissed: Boolean = false

    private var uploadUri: Uri? = null

    private var downloadId: String? = null

    private var cancelId: String? = null

    @Before
    fun setUp() {
        composeTestRule.setContent {
            FileShareComponent(
                uiState = fileShareUiState,
                userMessageComponent = { Text("User message") },
                showUnableToOpenFileSnackBar = showUnableToOpenFileSnackBar,
                showCancelledFileSnackBar = showCancelledFileSnackBar,
                onFileOpenFailure = { openFailure = true },
                onAlertDialogDismiss = { alertDialogDismissed = true },
                onUpload = { uploadUri = it },
                onDownload = { downloadId = it },
                onShareCancel = { cancelId = it },
                snackBarHostState = SnackbarHostState(),
                isLargeScreen = largeScreen
            )
        }
    }

    @After
    fun tearDown() {
        fileShareUiState = FileShareUiState(sharedFiles = ImmutableList(emptyList()))
        showUnableToOpenFileSnackBar = false
        showCancelledFileSnackBar = false
        showFileSizeLimitAlertDialog = false
        openFailure = false
        alertDialogDismissed = false
        uploadUri = null
        downloadId = null
        cancelId = null
        unmockkAll()
    }

    @Test
    fun fileShareTopAppBarShown() {
        val fileShare = composeTestRule.activity.getString(R.string.kaleyra_fileshare)
        composeTestRule.onNodeWithText(fileShare).assertIsDisplayed()
    }

    @Test
    fun fileShareAppBarCloseButtonShown() {
        val close = composeTestRule.activity.getString(R.string.kaleyra_close)
        composeTestRule.onNodeWithContentDescription(close).assertIsDisplayed()
    }

    @Test
    fun emptyItems_noItemsUIDisplayed() {
        val title = composeTestRule.activity.getString(R.string.kaleyra_no_file_shared)
        val subtitle = composeTestRule.activity.getString(R.string.kaleyra_click_to_share_file)
        fileShareUiState = FileShareUiState(sharedFiles = ImmutableList(listOf(mockUploadSharedFile)))
        composeTestRule.onNodeWithText(title).assertDoesNotExist()
        composeTestRule.onNodeWithText(subtitle).assertDoesNotExist()
        fileShareUiState = fileShareUiState.copy(sharedFiles = ImmutableList(listOf()))
        composeTestRule.onNodeWithText(title).assertIsDisplayed()
        composeTestRule.onNodeWithText(subtitle).assertIsDisplayed()
    }

    @Test
    fun emptyItems_fabTextDisplayed() {
        val iconDescription = composeTestRule.activity.getString(R.string.kaleyra_fileshare_add_description)
        val text = composeTestRule.activity.getString(R.string.kaleyra_fileshare_add)
        composeTestRule.onNodeWithContentDescription(iconDescription).assertIsDisplayed()
        composeTestRule.onNodeWithText(text).assertIsDisplayed()
    }

    @Test
    fun atLeastOneItem_fabTextNotExist() {
        fileShareUiState = FileShareUiState(sharedFiles = ImmutableList(listOf(mockDownloadSharedFile)))
        val iconDescription = composeTestRule.activity.getString(R.string.kaleyra_fileshare_add_description)
        val text = composeTestRule.activity.getString(R.string.kaleyra_fileshare_add)
        composeTestRule.onNodeWithContentDescription(iconDescription).assertIsDisplayed()
        composeTestRule.onNodeWithText(text).assertDoesNotExist()
    }

    @Test
    fun oneSharedFileItem_itemDisplayed() {
        fileShareUiState = FileShareUiState(sharedFiles = ImmutableList(listOf(mockDownloadSharedFile)))
        composeTestRule.onNodeWithTag(FileShareItemTag).assertIsDisplayed()
    }

    @Test
    fun showUnableToOpenFileSnackBarTrue_snackBarIsDisplayed() {
        val text = composeTestRule.activity.getString(R.string.kaleyra_fileshare_impossible_open_file)
        showUnableToOpenFileSnackBar = false
        composeTestRule.onNodeWithText(text).assertDoesNotExist()
        showUnableToOpenFileSnackBar = true
        composeTestRule.onNodeWithText(text).assertIsDisplayed()
    }

    @Test
    fun showCancelledFileSnackBarTrue_snackBarIsDisplayed() {
        val text = composeTestRule.activity.getString(R.string.kaleyra_fileshare_file_cancelled)
        showCancelledFileSnackBar = false
        composeTestRule.onNodeWithText(text).assertDoesNotExist()
        showCancelledFileSnackBar = true
        composeTestRule.onNodeWithText(text).assertIsDisplayed()
    }

    @Test
    fun showFileSizeLimitAlertDialogTrue_dialogIsDisplayed() {
        val text = composeTestRule.activity.getString(R.string.kaleyra_max_bytes_dialog_title)
        fileShareUiState = FileShareUiState(showFileSizeLimit = false)
        composeTestRule.onNodeWithText(text).assertDoesNotExist()
        fileShareUiState = FileShareUiState(showFileSizeLimit = true)
        composeTestRule.onNodeWithText(text).assertIsDisplayed()
    }

    @Test
    fun userClicksFab_filePickActivityLaunched() {
        Intents.init()
        val add = composeTestRule.activity.getString(R.string.kaleyra_fileshare_add_description)
        composeTestRule.onNodeWithContentDescription(add).performClick()
        intended(hasComponent(FilePickActivity::class.java.name))
        Intents.release()
    }

    @Test
    fun userClicksSuccessStateItem_tryToOpenFileInvoked() {
        mockkObject(ContextExtensions)
        every { any<Context>().tryToOpenFile(any(), any()) } returns Unit
        val sharedFile = mockDownloadSharedFile.copy(state = SharedFileUi.State.Success(uri = ImmutableUri(Uri.EMPTY)))
        fileShareUiState = FileShareUiState(sharedFiles = ImmutableList(listOf(sharedFile)))
        composeTestRule.onNodeWithTag(FileShareItemTag).performClick()
        verify { any<Context>().tryToOpenFile(any(), any()) }
    }

    @Test
    fun userClicksSuccessStateAction_tryToOpenFileInvoked() {
        mockkObject(ContextExtensions)
        every { any<Context>().tryToOpenFile(any(), any()) } returns Unit
        val sharedFile = mockDownloadSharedFile.copy(state = SharedFileUi.State.Success(uri = ImmutableUri(Uri.EMPTY)))
        fileShareUiState = FileShareUiState(sharedFiles = ImmutableList(listOf(sharedFile)))
        val openFile = composeTestRule.activity.getString(R.string.kaleyra_fileshare_open_file)
        composeTestRule.onNodeWithContentDescription(openFile).performClick()
        verify { any<Context>().tryToOpenFile(any(), any()) }
    }

    @Test
    fun userClicksAvailableStateAction_onDownloadInvoked() {
        val sharedFile = mockDownloadSharedFile.copy(id = "fileId", state = SharedFileUi.State.Available)
        fileShareUiState = FileShareUiState(sharedFiles = ImmutableList(listOf(sharedFile)))
        val startDownload = composeTestRule.activity.getString(R.string.kaleyra_fileshare_download_descr)
        composeTestRule.onNodeWithContentDescription(startDownload).performClick()
        assertEquals("fileId", downloadId)
    }

    @Test
    fun userClicksPendingStateAction_onShareCancelInvoked() {
        val sharedFile = mockDownloadSharedFile.copy(id = "fileId", state = SharedFileUi.State.Pending)
        fileShareUiState = FileShareUiState(sharedFiles = ImmutableList(listOf(sharedFile)))
        val cancel = composeTestRule.activity.getString(R.string.kaleyra_fileshare_cancel)
        composeTestRule.onNodeWithContentDescription(cancel).performClick()
        assertEquals("fileId", cancelId)
    }

    @Test
    fun userClicksErrorStateActionOnUpload_onUploadInvoked() {
        val uriMock = mockk<Uri>()
        val sharedFile = mockUploadSharedFile.copy(uri = ImmutableUri(uriMock), state = SharedFileUi.State.Error)
        fileShareUiState = FileShareUiState(sharedFiles = ImmutableList(listOf(sharedFile)))
        val retry = composeTestRule.activity.getString(R.string.kaleyra_fileshare_retry)
        composeTestRule.onNodeWithContentDescription(retry).performClick()
        assertEquals(uriMock, uploadUri)
    }

    @Test
    fun userClicksErrorStateActionOnDownload_onDownloadInvoked() {
        val sharedFile = mockDownloadSharedFile.copy(id = "fileId", state = SharedFileUi.State.Error)
        fileShareUiState = FileShareUiState(sharedFiles = ImmutableList(listOf(sharedFile)))
        val retry = composeTestRule.activity.getString(R.string.kaleyra_fileshare_retry)
        composeTestRule.onNodeWithContentDescription(retry).performClick()
        assertEquals("fileId", downloadId)
    }

    @Test
    fun userClicksSuccessStateActionAndFileFailsToOpen_onFileOpenFailureInvoked() {
        mockkObject(ContextExtensions)
        every { any<Context>().tryToOpenFile(any(), any()) } answers {
            thirdArg<(Boolean) -> Unit>().invoke(true)
        }
        val sharedFile = mockDownloadSharedFile.copy(state = SharedFileUi.State.Success(uri = ImmutableUri(Uri.EMPTY)))
        fileShareUiState = FileShareUiState(sharedFiles = ImmutableList(listOf(sharedFile)))
        val openFile = composeTestRule.activity.getString(R.string.kaleyra_fileshare_open_file)
        composeTestRule.onNodeWithContentDescription(openFile).performClick()
        assertEquals(true, openFailure)
    }

    @Test
    fun onAlertDialogDismissedInvoked() {
        fileShareUiState = FileShareUiState(showFileSizeLimit = true)
        val ok = composeTestRule.activity.getString(R.string.kaleyra_button_ok)
        composeTestRule.onNodeWithText(ok).performClick()
        assertEquals(true, alertDialogDismissed)
    }

    @Test
    fun smallScreen_userMessageComponentIsDisplayed() {
        largeScreen = false
        composeTestRule.onNodeWithText("User message").assertIsDisplayed()
    }

    @Test
    fun largeScreen_userMessageComponentDoesNotExists() {
        largeScreen = true
        composeTestRule.onNodeWithText("User message").assertDoesNotExist()
    }
}